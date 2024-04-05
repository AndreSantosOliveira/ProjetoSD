import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Downloader class extends UnicastRemoteObject and implements MetodosRMIDownloader and Serializable.
 * This class is responsible for crawling URLs and sending the results to the QueueManager.
 * It maintains a map of URLData objects and a flag to indicate if the Downloader is busy.
 */
public class Downloader extends UnicastRemoteObject implements MetodosRMIDownloader, Serializable {

    // Map to store URLData objects
    Map<String, URLData> urlData = new HashMap<>();

    // Flag to indicate if the Downloader is busy
    private boolean busy = false;

    // The ID of the Downloader
    static String dlID;
    // PrintWriter to send messages to the QueueManager
    static PrintWriter queueManager;

    /**
     * Default constructor for Downloader.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    protected Downloader() throws RemoteException {
        super();
    }

    /**
     * Returns the ID of the Downloader.
     *
     * @return the ID of the Downloader
     */
    @Override
    public String getDownloaderID() {
        return dlID;
    }

    /**
     * Main method for the Downloader class.
     * It creates a new Downloader object and binds it to the RMI registry.
     * It also establishes a socket connection to the QueueManager.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            // Check if the command line arguments are valid
            if (args.length < 2) {
                System.out.println("Downloader <PORT> <ID>");
                System.exit(1);
            }
            int porta = Integer.parseInt(args[0]);
            dlID = args[1];

            Downloader gateway = new Downloader();
            LocateRegistry.createRegistry(porta).rebind(dlID, gateway);

            if (!socketDownloadManagerToQueue()) {
                System.out.println("Failed to connect to QueueManager.");
                System.exit(1);
            }

            System.out.println("Downloader " + dlID + " ready: 127.0.0.1:" + porta);
        } catch (IOException re) {
            System.out.println("Exception in Gateway RMI: " + re);
        }
    }

    /**
     * Establishes a socket connection to the QueueManager.
     * It tries to connect to the QueueManager up to 10 times.
     * If the connection is successful, it returns true.
     * If the connection fails after 10 attempts, it returns false.
     *
     * @return true if the connection is successful, false otherwise.
     */
    static boolean socketDownloadManagerToQueue() {
        final int maxTentativa = 10; // Maximum number of retries
        int tentativa = 0; // Current attempt counter

        while (tentativa < maxTentativa) {
            try {
                // Attempt to connect to QueueManager via TCP
                Socket socket = new Socket(ConnectionsEnum.QUEUE_MANAGER.getIP(), ConnectionsEnum.QUEUE_MANAGER.getPort());
                queueManager = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("QueueManager connection has been established! IP: " + ConnectionsEnum.QUEUE_MANAGER);
                return true;
            } catch (Exception re) {
                System.out.println("Error trying to connect to QueueManager - try nº" + (tentativa + 1) + ": " + re);
                ++tentativa;
                if (tentativa < maxTentativa) {
                    try {
                        Thread.sleep(1001);
                    } catch (InterruptedException ie) {
                        System.out.println("Problem ocorred in Thread Sleep: " + ie);
                    }
                } else {
                    System.out.println("Failed to connect to QueueManager after " + tentativa + " tries.");
                }
            }
        }
        return false;
    }

    /**
     * Checks if the provided URL is valid.
     *
     * @param urlString the URL to check
     * @return true if the URL is valid, false otherwise
     */
    public static boolean isValidURL(String urlString) {
        try {
            // Attempt to create a URL object
            new URL(urlString).toURI();
            return true;
        } catch (MalformedURLException | java.net.URISyntaxException e) {
            return false;
        }
    }

    /**
     * Crawls a URL and sends the results to the QueueManager.
     * It uses Jsoup to connect to the URL and parse the HTML document.
     * It then extracts the links from the document and sends them to the QueueManager.
     * It also sends the results to the ISB via multicast.
     *
     * @param url       the URL to crawl
     * @param tentativa the number of attempts made to crawl the URL
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    @Override
    public void crawlURL(String url, int tentativa) throws RemoteException {
        // Max retrys exceeded
        if (tentativa == 3) {
            busy = false;
            return;
        }
        if (!isValidURL(url)) {
            System.out.println("Invalid URL: " + url + " - discarding.");
            return;
        }

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        try {
            busy = true;
            Document doc = Jsoup.connect(url).get();
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int countTokens = 0;
            while (tokens.hasMoreElements() && countTokens++ < 100) {
                Elements links = doc.select("a[href]");
                for (Element elementoLink : links) {
                    String titulo = elementoLink.text();
                    String link = elementoLink.attr("abs:href");
                    if (link.endsWith(".onion")) continue; //ignorar links da dark web xD

                    if (titulo.length() > 3 && link.startsWith("http")) {
                        for (String s : titulo.split(" ")) {
                            s = s.toLowerCase();
                            if (s.length() > 3) {

                                synchronized (urlData) {
                                    if (!urlData.containsKey(link)) {
                                        urlData.put(link, new URLData(link, titulo, url));
                                    }
                                }

                            }
                        }
                    }
                }
            }

            // Url keys

            synchronized (urlData) {
                Map<String, URLData> urlDataCopia = new HashMap<>(urlData);
                urlDataCopia.keySet().forEach(queueManager::println);

                sendResultToISBviaMulticast(new ArrayList<>(urlDataCopia.values()));

                System.out.println("Scraping done! " + url + "\n " + urlDataCopia.size() + " -> unique URLs sent to QueueManager.");
                urlData.clear();
            }


            // Downloader is no longer busy
            busy = false;
        } catch (IOException e) {
            if (!e.getMessage().contains("PKIX") && !e.getMessage().contains("404")) { // ignore problems with security certs lol
                System.out.println("Error while trying to scrape data -> " + e.getMessage());
                crawlURL(url, tentativa + 1);
            }
            /*
            queueManager.println(url);
            System.out.println("Re-added " + url + " to the queue.");
            */
        }
    }

    /**
     * Checks if the Downloader is busy.
     *
     * @return true if the Downloader is busy, false otherwise.
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    @Override
    public boolean isBusy() throws RemoteException {
        return busy;
    }

    /**
     * Shuts down the Downloader.
     *
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    @Override
    public void shutdown() throws RemoteException {
        System.out.println("Shutting down Downloader " + dlID);
        System.exit(0);
    }

    /**
     * Sends the result to ISB via multicast.
     * It creates a multicast socket and sends a datagram packet for each URLData object in the result.
     *
     * @param resultado the result to send
     */
    public static void sendResultToISBviaMulticast(List<URLData> resultado) {
        try {
            // Create a multicast socket
            MulticastSocket multicastSocket = new MulticastSocket();

            // Convert the message to bytes
            for (URLData data : resultado) {
                byte[] buffer = data.toStringDataPacket().getBytes();

                // Get the multicast address
                InetAddress group = InetAddress.getByName(ConnectionsEnum.MULTICAST.getIP());

                // Create a datagram packet to send
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, ConnectionsEnum.MULTICAST.getPort());
                // Send the packet
                multicastSocket.send(packet);
            }

            // Close the socket
            multicastSocket.close();

            System.out.println("Multicast message sent successfully.");
        } catch (IOException e) {
            System.out.println("Error sending multicast message: " + e.getMessage());
        }
    }
}