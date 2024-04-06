/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 1 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DownloaderManager class implements Serializable.
 * This class is responsible for managing a collection of downloaders.
 * It loads the downloaders from a text file, attempts to connect to each downloader,
 * and sets up a socket to receive URLs from the QueueManager for scraping.
 */
public class DownloaderManager implements Serializable {
    // Map to store downloaders
    private Map<Connection, MetodosRMIDownloader> downloaders = new HashMap<>();

    // Counter to keep track of the downloader to send the URL to
    static AtomicInteger downloaderCounter = new AtomicInteger();

    /**
     * Default constructor for DownloaderManager.
     * This constructor initializes the DownloaderManager and connects to the downloaders.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    public DownloaderManager() throws RemoteException {
        super();
        connectToDownloaders();
    }

    /**
     * Connects to the downloaders.
     * This method loads the downloaders from the downloaders.txt file and attempts to connect to each downloader.
     */
    private void connectToDownloaders() {
        // Load downloaders from the text file downloaders.txt (IP, port, rmiName)
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/downloaders.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    try {
                        String ip = parts[0];
                        int porta = Integer.parseInt(parts[1]);
                        String rmiName = parts[2];
                        Connection descritor = new Connection(ip, porta, rmiName);
                        MetodosRMIDownloader res = tentarLigarADownloader(descritor, true);
                        downloaders.put(new Connection(ip, porta, rmiName), res);
                    } catch (NumberFormatException e) {
                        System.err.println("Error processing the port for a downloader: " + line);
                    }
                } else {
                    System.err.println("Line in invalid format: " + line);
                }
            }

            if (this.downloaders.isEmpty()) {
                System.err.println("No downloaders connected. Exiting program.");
                System.exit(1);
            }


        } catch (IOException e) {
            System.err.println("Error reading the downloader file: " + e.getMessage());
        }

    }

    /**
     * Main method for the DownloaderManager class.
     * It creates a new DownloaderManager object, connects to the downloaders, and sets up a socket to receive URLs from the QueueManager for scraping.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) throws RemoteException {
        try {
            DownloaderManager downloaderManager = new DownloaderManager();

            // Create heartbeat system for each downloader
            for (Connection connection : downloaderManager.downloaders.keySet()) {
                // Make individual heartbeat system for each downloader in separate threads
                new Thread(() -> {
                    try {
                        downloaderManager.heartbeat(connection);
                    } catch (InterruptedException | RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }

            downloaderManager.socketQueueManagerToDownloadManager();

        } catch (IOException e) {
            System.err.println("Error creating the DownloaderManager: " + e.getMessage());
        }
    }

    /**
     * Sends a heartbeat to a downloader.
     * This method sends a heartbeat to a downloader every 5 seconds to check if the downloader is alive.
     *
     * @param downloaderCon the connection to the downloader
     * @throws RemoteException if an error occurs during remote method invocation.
     * @throws InterruptedException if the thread is interrupted while sleeping.
     */
    private void heartbeat(Connection downloaderCon) throws RemoteException, InterruptedException {
        while (true) {
            Thread.sleep(5000);
            MetodosRMIDownloader downloader = tentarLigarADownloader(downloaderCon, false);
            if (downloader != null) {
                System.out.println("Downloader " + downloaderCon.getRMIName() + " is alive.");
            } else {
                System.out.println("Downloader " + downloaderCon.getRMIName() + " is offline.");
                reconnectToDownloader(downloaderCon);
            }
        }
    }

    /**
     * Attempts to connect to a downloader.
     * This method attempts to connect to a downloader up to 5 times.
     * If the connection is successful, it returns the MetodosRMIDownloader object.
     * If the connection fails after 5 attempts, it returns null.
     *
     * @param descritorIPPorta the descriptor of the downloader to connect to
     * @param retrySystemOff a flag to indicate if the retry system is off
     * @return the MetodosRMIDownloader object if the connection is successful, null otherwise.
     */
    private static MetodosRMIDownloader tentarLigarADownloader(Connection descritorIPPorta, boolean retrySystemOff) {
        MetodosRMIDownloader metodosDownloader;
        int retryCount = 0;
        int maxRetries = 5;
        while (retryCount < maxRetries) {
            try {
                metodosDownloader = (MetodosRMIDownloader) Naming.lookup("rmi://" + descritorIPPorta.getIP() + ":" + descritorIPPorta.getPorta() + "/" + descritorIPPorta.getRMIName());
                if (retrySystemOff)
                    System.out.println("Connected to Downloader " + descritorIPPorta.getRMIName() + "!");
                return metodosDownloader;
            } catch (RemoteException | NotBoundException e) {
                ++retryCount;
                if (retryCount < maxRetries) {
                    System.out.println("Failed to connect to Downloader: " + descritorIPPorta.getRMIName() + " (" + retryCount + "/" + maxRetries + "). Retrying...");
                    // Sleep to avoid consecutive connection attempts
                    try {
                        Thread.sleep(1001);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Failed to connect to Downloader: " + descritorIPPorta.getRMIName() + ". :(");
        return null;
    }

    /**
     * Synchronizes the downloaders to send a URL to scrape.
     * This method sends the URL to the downloader at the current index in the list of downloaders.
     * If an error occurs while sending the URL, it prints an error message.
     *
     * @param urlParaScrape the URL to scrape
     * @throws MalformedURLException if the URL is not in a valid format.
     * @throws NotBoundException if the RMI name is not currently bound.
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    public void synchronizeDownloaders(String urlParaScrape) throws MalformedURLException, NotBoundException, RemoteException {
        if (getActiveDownloaders() == 0) {
            System.out.println("No downloader available to scrape URL. Please wait for a downloader to be available.");
            return;
        }

        synchronized (downloaders) {
            if (downloaderCounter.get() >= downloaders.size()) {
                downloaderCounter.set(0);
            }


            try {
                // Get downloader and connection
                Connection connection = (Connection) downloaders.keySet().toArray()[downloaderCounter.get()];
                MetodosRMIDownloader downloader = (MetodosRMIDownloader) Naming.lookup("rmi://" + connection.getIP() + ":" + connection.getPorta() + "/" + connection.getRMIName());

                if (!downloader.isBusy()) {
                    new Thread(() -> {
                        try {
                            downloader.crawlURL(urlParaScrape, 0);
                            downloaderCounter.incrementAndGet();
                        } catch (RemoteException e) {
                            System.out.println("Failed to send URL to Downloader : " + urlParaScrape);
                        }
                    }).start();
                } else {
                    downloaderCounter.incrementAndGet();
                    //synchronizeDownloaders(urlParaScrape);
                }
            } catch (RemoteException e) {
                System.out.println("Failed to connect to a downloader for indexing. Redirecting to another downloader.");
                downloaderCounter.incrementAndGet();
                synchronizeDownloaders(urlParaScrape);
            }
        }
    }

    /**
     * Gets the number of active downloaders.
     *
     * @return the number of active downloaders.
     */
    public int getActiveDownloaders() {
        // Count the number of active downloaders
        int ctr = 0;
        synchronized (downloaders) {
            for (Connection connection : downloaders.keySet()) {
                try {
                    MetodosRMIDownloader res = (MetodosRMIDownloader) Naming.lookup("rmi://" + connection.getIP() + ":" + connection.getPorta() + "/" + connection.getRMIName());
                    res.getDownloaderID();
                    ++ctr;
                } catch (MalformedURLException | NotBoundException | RemoteException ignored) {

                }
            }
        }
        return ctr;
    }

    /**
     * Reconnects to a downloader.
     * This method attempts to reconnect to a downloader every 5 seconds until the connection is successful.
     *
     * @param downloaderCon the connection to the downloader
     * @throws InterruptedException if the thread is interrupted while sleeping.
     */
    private void reconnectToDownloader(Connection downloaderCon) throws InterruptedException {
        System.out.println("Trying to reconnect to Downloader " + downloaderCon.getRMIName() + "...");
        while (true) {
            Thread.sleep(5000);
            MetodosRMIDownloader downloader = tentarLigarADownloader(downloaderCon, false);
            if (downloader != null) {
                System.out.println("Reconnected to Downloader " + downloaderCon.getRMIName() + "!");
                break;
            }
        }
    }

    /**
     * Sets up a socket to receive URLs from the QueueManager for scraping.
     * This method creates a server socket and continuously accepts connections.
     * For each connection, it creates a new thread to handle the connection.
     *
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    private void socketQueueManagerToDownloadManager() throws IOException {
        ServerSocket serverSocket = new ServerSocket(ConnectionsEnum.DOWNLOAD_MANAGER.getPort());

        // download manager ready
        System.out.println("[" + ConnectionsEnum.DOWNLOAD_MANAGER + "] DownloadManager ready.");

        // Accept connections
        while (true) {
            // Accept connection
            Socket connectionSocket = serverSocket.accept();
            // Create thread to handle the connection
            new Thread(() -> {
                try {
                    // setup bufferedreader to read messages from clients
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));


                    // read messages from the client
                    String urlParaScrape;
                    while ((urlParaScrape = inFromClient.readLine()) != null) {
                        if (urlParaScrape.equalsIgnoreCase("shutdown")) {
                            System.out.println("DownloadManager received shutdown command.");
                            shutdownDownloaders();
                            break;
                        }
                        if (urlParaScrape.endsWith("/")) {
                            urlParaScrape = urlParaScrape.substring(0, urlParaScrape.length() - 1);
                        }

                        synchronizeDownloaders(urlParaScrape);
                    }
                } catch (IOException | NotBoundException e) {
                    System.out.println("Error reading message from QueueManager: " + e.getMessage());
                }
            }).start();
        }
    }

    private void shutdownDownloaders() {
        // Shutdown all downloaders
        synchronized (downloaders) {
            for (MetodosRMIDownloader downloader : downloaders.values()) {
                try {
                    downloader.shutdown();
                } catch (RemoteException ignored) {
                }
            }
        }
        System.exit(0);
    }
}