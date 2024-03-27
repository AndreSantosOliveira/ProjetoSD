import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

/**
 * DownloaderManager class.
 * This class is responsible for managing a collection of downloaders.
 * It loads the downloaders from a text file, attempts to connect to each downloader,
 * and sets up a socket to receive URLs from the QueueManager for scraping.
 */
public class DownloaderManager {

    // Map to store downloaders
    private static Map<DescritorIPPorta, MetodosRMIDownloader> downloaders = new HashMap<>();
    private static int downloadersON;

    /**
     * Main method for the DownloaderManager class.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) throws IOException {
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

                        downloaders.put(new DescritorIPPorta(ip, porta, rmiName), null);
                        System.out.println("Downloader added: " + rmiName + " (" + ip + ":" + porta + ")");
                    } catch (NumberFormatException e) {
                        System.err.println("Error processing the port for a downloader: " + line);
                    }
                } else {
                    System.err.println("Line in invalid format: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the downloader file.");
            e.printStackTrace();
        }

        for (DescritorIPPorta descritorIPPorta : downloaders.keySet()) {
            MetodosRMIDownloader res = tentarLigarADownloader(descritorIPPorta);
            if (res != null) {
                downloaders.put(descritorIPPorta, res);
                ++downloadersON;
            }
        }

        if (downloadersON == 0) {
            System.err.println("No barrel has been connected. Exiting...");
            System.exit(1);
        }

        socketQueueManagerToDownloadManager();
    }

    /**
     * Attempts to connect to a downloader.
     *
     * @param descritorIPPorta descriptor of the downloader to connect to
     * @return MetodosRMIDownloader object if the connection is successful, null otherwise.
     */
    private static MetodosRMIDownloader tentarLigarADownloader(DescritorIPPorta descritorIPPorta) {
        MetodosRMIDownloader metodosGateway = null;
        int retryCount = 0;
        int maxRetries = 5;
        while (metodosGateway == null && retryCount < maxRetries) {
            try {
                metodosGateway = (MetodosRMIDownloader) LocateRegistry.getRegistry(descritorIPPorta.getPorta()).lookup(descritorIPPorta.getRMIName());
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
            }
        }
        System.out.println("Failed to connect to Downloader: " + descritorIPPorta.getRMIName() + ". :(");
        return null;
    }

    /**
     * Sets up a socket to receive URLs from the QueueManager for scraping.
     */
    private static void socketQueueManagerToDownloadManager() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PortasEIPs.DOWNLOAD_MANAGER.getPorta());

        // download manager ready
        System.out.println("[" + PortasEIPs.DOWNLOAD_MANAGER + "] DownloadManager ready.");

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

                        synchronized (downloaders) {
                            for (Map.Entry<DescritorIPPorta, MetodosRMIDownloader> downloader : downloaders.entrySet()) {
                                if (downloader.getValue() != null) {
                                    System.out.println("Downloader: " + downloader.getKey().getRMIName() + " - " + downloader.getValue().isBusy());
                                    if (!downloader.getValue().isBusy()) {
                                        downloader.getValue().crawlURL(urlParaScrape);
                                        break;
                                    }
                                }
                            }
                        }

                        // thread for each link received by the download manager
                        //new Downloader(urlParaScrape, queueManager).start();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}