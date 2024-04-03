import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Objects;


/**
 * QueueManager class extends UnicastRemoteObject and implements Serializable.
 * This class is responsible for managing a queue of URLs to be downloaded.
 * It maintains a connection with the DownloadManager and accepts connections from the Gateway.
 */
public class QueueManager extends UnicastRemoteObject implements Serializable {

    /**
     * Default constructor for QueueManager.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    protected QueueManager() throws RemoteException {
        super();
    }

    // PrintWriter object to communicate with the DownloadManager
    private static PrintWriter downloadManager;

    // Queue to hold unique URLs, with a maximum size of 50
    static UniqueQueue<String> queue = new UniqueQueue<>(50);
    // HashSet to store visited URLs
    static HashSet<String> jaVisitados = new HashSet<>();

    /**
     * Main method for the QueueManager class.
     * It creates a server socket for receiving connections from the Gateway,
     * attempts to connect to the DownloadManager, and starts a new thread to handle the queue.
     * It also accepts connections from the Gateway and creates a new thread to handle each connection.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            // Create a server socket for receiving connections from the Gateway
            ServerSocket serverSocket = new ServerSocket(ConnectionsEnum.QUEUE_MANAGER.getPort());

            ConnectionsEnum.QUEUE_MANAGER.printINIT("DownloadManager");

            // Attempt to connect to the DownloadManager
            if (!connectToDownloadManager()) {
                System.out.println("Failed to connect to DownloadManager.");
                return;
            }

            // Start a new thread to handle the queue
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (queue == null) {
                        downloadManager.println("shutdown");
                        System.exit(0);
                    } else {
                        synchronized (queue) {
                            if (!queue.isEmpty()) {
                                String url = queue.poll();
                                if (url != null) {
                                    downloadManager.println(url);
                                    System.out.println("QueueManager sent for Scraping: " + url);
                                }
                            }
                        }
                    }

                }

            }).start();

            // End of QueueManager loading
            ConnectionsEnum.DOWNLOAD_MANAGER.printINIT("QueueManager");

            // Accept connections
            while (true) {
                // Accept a connection
                Socket connectionSocket = serverSocket.accept();

                if (!Objects.equals(connectionSocket.getInetAddress().getHostAddress(), "127.0.0.1"))
                    // Information about the connection:
                    System.out.println("QueueManager received connection from: " + connectionSocket.getInetAddress().getHostAddress() + ":" + connectionSocket.getPort());
                // Create a thread to handle the connection
                new Thread(() -> {
                    try {
                        // Setup BufferedReader to read messages from clients
                        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                        // Read messages from the client
                        String dados;
                        while ((dados = inFromClient.readLine()) != null) {
                            if (dados.endsWith("/")) {
                                dados = dados.substring(0, dados.length() - 1);
                            }

                            // Check if dado is already in jaVisitados
                            if (jaVisitados.contains(dados)) {
                                System.out.println("URL already visited: " + dados);
                                break;
                            }

                            if (dados.equalsIgnoreCase("shutdown")) {
                                System.out.println("QueueManager received shutdown command.");
                                queue.clear();
                                queue = null;
                                break;
                            }

                            synchronized (queue) {
                                if (queue.offer(dados)) {
                                    System.out.println("New url to index: " + dados);
                                    // Queue size
                                    System.out.println("URLs to index: " + queue.size());
                                    // Add to visited
                                    jaVisitados.add(dados);
                                }
                            }
                        }

                        //connectionSocket.close();
                    } catch (IOException e) {
                        System.out.println("Error reading message from Gateway: " + e.getMessage());
                    }
                }).start();
            }
        } catch (IOException e) {
            System.out.println("Error creating connection socket Gateway->QueueManager: " + e.getMessage());
        }
    }

    /**
     * Attempts to connect to the DownloadManager.
     * It tries to connect to the DownloadManager up to 10 times.
     * If the connection is successful, it returns true.
     * If the connection fails after 10 attempts, it returns false.
     *
     * @return true if the connection is successful, false otherwise.
     */
    static boolean connectToDownloadManager() {
        final int maxTentativa = 10;
        int tentativa = 0;

        while (tentativa < maxTentativa) {
            try {
                // Attempt to connect to DownloadManager via TCP
                Socket socket = new Socket(ConnectionsEnum.DOWNLOAD_MANAGER.getIP(), ConnectionsEnum.DOWNLOAD_MANAGER.getPort());
                System.out.println("DownloadManager connected sucessfully. IP: " + ConnectionsEnum.DOWNLOAD_MANAGER);

                downloadManager = new PrintWriter(socket.getOutputStream(), true);
                return true;
            } catch (IOException re) {
                System.out.println("Connecting to DownloadManager failed - Atempt nº" + (tentativa + 1) + ": " + re + ". Retrying...");
                ++tentativa;
                if (tentativa < maxTentativa) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        System.out.println("Error in connectToDownloadManager sleep: " + ie);
                    }
                } else {
                    System.out.println("Failed to connect to downloadManager after: " + tentativa + " attempts.");
                }
            }
        }

        return false;
    }
}