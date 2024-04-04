import java.io.*;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DownloaderManager class.
 * This class is responsible for managing a collection of downloaders.
 * It loads the downloaders from a text file, attempts to connect to each downloader,
 * and sets up a socket to receive URLs from the QueueManager for scraping.
 */
public class DownloaderManager implements Serializable {
    // Map to store downloaders
    private static List<MetodosRMIDownloader> downloaders = new ArrayList<>();

    // Counter to keep track of the downloader to send the URL to
    static AtomicInteger downloaderCounter = new AtomicInteger();


    /**
     * Connects to the downloaders.
     * It loads downloaders from the text file downloaders.txt (IP, port, rmiName).
     * For each downloader, it attempts to connect to the downloader and adds it to the list of downloaders.
     * If no downloaders are connected, it exits the program.
     *
     * @see Connection
     * @see MetodosRMIDownloader
     * @see #tentarLigarADownloader(Connection)
     */
    public static void connectToDownloaders() {
        downloaderCounter.set(0);
        downloaders.clear();

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

                        MetodosRMIDownloader res = tentarLigarADownloader(new Connection(ip, porta, rmiName));
                        if (res != null) {
                            downloaders.add(res);
                        }

                    } catch (NumberFormatException e) {
                        System.err.println("Error processing the port for a downloader: " + line);
                    }
                } else {
                    System.err.println("Line in invalid format: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the downloader file: " + e.getMessage());
        }

        if (downloaders.isEmpty()) {
            System.err.println("No barrel has been connected. Exiting...");
            System.exit(1);
        }
    }

    /**
     * Main method for the DownloaderManager class.
     * It loads downloaders from a text file and attempts to connect to each downloader.
     * It also sets up a socket to receive URLs from the QueueManager for scraping.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) throws IOException {
        connectToDownloaders();
        socketQueueManagerToDownloadManager();
    }

    /**
     * Attempts to connect to a downloader.
     * It tries to connect to the downloader up to 5 times.
     * If the connection is successful, it returns the MetodosRMIDownloader object.
     * If the connection fails, it returns null.
     *
     * @param descritorIPPorta descriptor of the downloader to connect to
     * @return MetodosRMIDownloader object if the connection is successful, null otherwise.
     */
    private static MetodosRMIDownloader tentarLigarADownloader(Connection descritorIPPorta) {
        MetodosRMIDownloader metodosGateway = null;
        int retryCount = 0;
        int maxRetries = 5;
        while (metodosGateway == null && retryCount < maxRetries) {
            try {
                metodosGateway = (MetodosRMIDownloader) Naming.lookup("rmi://" + descritorIPPorta.getIP() + ":" + descritorIPPorta.getPorta() + "/" + descritorIPPorta.getRMIName());
                System.out.println("Connected to Downloader " + descritorIPPorta.getRMIName() + "!");
                return metodosGateway;
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
     * It sends the URL to the downloader at the current index in the list of downloaders.
     * If an error occurs while sending the URL, it prints an error message.
     *
     * @param urlParaScrape the URL to scrape
     */
    private static void synchronizeDownloaders(String urlParaScrape) {
        synchronized (downloaders) {

            if (downloaderCounter.get() >= downloaders.size()) {
                downloaderCounter.set(0);
            }

            MetodosRMIDownloader downloader = downloaders.get(downloaderCounter.get());
            try {
                if (downloader != null && !downloader.isBusy()) {
                    new Thread(() -> {
                        try {
                            downloader.crawlURL(urlParaScrape);
                            downloaderCounter.incrementAndGet();
                        } catch (RemoteException e) {
                            System.out.println("Failed to send URL to Downloader : " + urlParaScrape);
                        }
                    }).start();
                }
            } catch (RemoteException e) {
                System.out.println("Failed to connect to a downloader for indexing. Retrying in 1 second...");
                //TODO: Reconnect to downloaders
                connectToDownloaders();
                synchronizeDownloaders(urlParaScrape);
            }
        }
    }

    /**
     * Sets up a socket to receive URLs from the QueueManager for scraping.
     * It creates a server socket and continuously accepts connections.
     * For each connection, it creates a new thread to handle the connection.
     */
    private static void socketQueueManagerToDownloadManager() throws IOException {
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private static void shutdownDownloaders() {
        synchronized (downloaders) {
            for (MetodosRMIDownloader downloader : downloaders) {
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