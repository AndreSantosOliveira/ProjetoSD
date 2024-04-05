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
 * The DownloaderManager class is responsible for managing a collection of downloaders.
 * It implements the Serializable interface and maintains a map of downloaders.
 * It provides methods to connect to downloaders, perform heartbeats, attempt connections, synchronize downloaders,
 * get active downloaders, reconnect to downloaders, setup a socket to receive URLs from the QueueManager for scraping,
 * and shutdown downloaders.
 */
public class DownloaderManager implements Serializable {
    // Map to store downloaders
    private Map<Connection, MetodosRMIDownloader> downloaders = new HashMap<>();

    // Counter to keep track of the downloader to send the URL to
    static AtomicInteger downloaderCounter = new AtomicInteger();

    /**
     * Default constructor for DownloaderManager.
     * It calls the connectToDownloaders method to establish connections with downloaders.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    public DownloaderManager() throws RemoteException {
        super();
        connectToDownloaders();
    }

    /**
     * This method is used to establish connections with downloaders.
     * It reads the downloader details from a text file and attempts to connect to each downloader.
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
            e.printStackTrace();
        }

    }

    /**
     * The main method for the DownloaderManager class.
     * It creates an instance of DownloaderManager, binds it to the RMI registry,
     * and starts the heartbeat system for each downloader in separate threads.
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
     * This method is used to check the heartbeat of a downloader.
     * It continuously checks if the downloader is alive and tries to reconnect if it's offline.
     *
     * @param downloaderCon The connection details of the downloader
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
     * This method is used to attempt a connection to a downloader.
     * It tries to connect to the downloader a specified number of times and returns the downloader object if successful.
     *
     * @param descritorIPPorta descriptor of the downloader to connect to
     * @param retrySystemOff   flag to indicate if the retry system is off
     * @return MetodosRMIDownloader object if the connection is successful, null otherwise.
     */
    private static MetodosRMIDownloader tentarLigarADownloader(Connection descritorIPPorta, boolean retrySystemOff) {
        MetodosRMIDownloader metodosDownloader = null;
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
     * This method is used to synchronize the downloaders to send a URL to scrape.
     * It sends the URL to the downloader at the current index in the list of downloaders.
     * If an error occurs while sending the URL, it prints an error message.
     *
     * @param urlParaScrape the URL to scrape
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
     * This method is used to get the number of active downloaders.
     *
     * @return the number of active downloaders
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
     * This method is used to reconnect to a downloader that is offline.
     * It continuously tries to connect to the downloader until it's successful.
     *
     * @param downloaderCon The connection details of the downloader
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
     * This method is used to setup a socket to receive URLs from the QueueManager for scraping.
     * It creates a server socket and continuously accepts connections.
     * For each connection, it creates a new thread to handle the connection.
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
                    e.printStackTrace();
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
                } catch (RemoteException e) {
                    continue;
                }
            }
        }
        System.exit(0);
    }
}