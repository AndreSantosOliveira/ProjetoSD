import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class Downloader extends UnicastRemoteObject implements MetodosRMIDownloader {

    static HashMap<String, HashSet<URLData>> index = new HashMap<>();
    static Set<String> alreadyScraped = new HashSet<>();

    private boolean busy = false;

    protected Downloader() throws RemoteException {
        super();
    }

    public static Boolean doesIndexHaveURL(String chave, String url) {
        List<URLData> urlList = new ArrayList<>(index.get(chave)); // Create a copy
        return urlList.stream().anyMatch(urlData -> urlData.getURL().equalsIgnoreCase(url));
    }

    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("Downloader <PORTA> <ID>");
                System.exit(1);
            }
            int porta = Integer.parseInt(args[0]);
            String dlID = args[1];

            Downloader gateway = new Downloader();
            LocateRegistry.createRegistry(porta).rebind(dlID, gateway);

            System.out.println("Downloader " + dlID + " ready: 127.0.0.1:" + porta);
        } catch (IOException re) {
            System.out.println("Exception in Gateway RMI: " + re);
        }
    }

    @Override
    public String crawlURL(String url, PrintWriter queueManager) throws RemoteException {
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
                        //System.out.println(titulo + "\n" + link + "\n");

                        // TODO 3: o título pode não conter o conteúdo essencial da página, encontrar uma maneira mais otimizada de o procurar
                        for (String s : titulo.split(" ")) {
                            s = s.toLowerCase();
                            if (s.length() > 3) {
                                if (index.containsKey(s) && !doesIndexHaveURL(s, link)) {
                                    URLData r = new URLData(link, titulo);
                                    index.get(s).add(r);
                                } else {
                                    HashSet<URLData> newHashSet = new HashSet<>(Collections.singletonList(new URLData(link, titulo)));
                                    index.put(s, newHashSet);
                                }

                                //System.out.println("DownloadManager enviou para QueueManager: " + link);
                                alreadyScraped.add(link);
                            }
                        }
                    }
                }
            }

            alreadyScraped.forEach(queueManager::println);

            // Send dummy results via multicas
            sendResultToISBviaMulticast(alreadyScraped.stream().map(s -> new URLData(s, "Dummy")).collect(Collectors.toCollection(HashSet::new)));

            System.out.println("Scraping done! " + url + "\n " + alreadyScraped.size() + " -> unique URLs sent to QueueManager.");
            alreadyScraped.clear();
            busy = false;
        } catch (IOException e) {
            System.out.println("Error while trying to scrape data from: " + url + " -> " + e.getMessage());
        }
        return "Sucesso!";
    }

    @Override
    public boolean isBusy() throws RemoteException {
        return busy;
    }


    // Define the multicast address and port
    static String multicastAddress = "230.0.0.1";
    static int multicastPort = 6900;

    // Send the result to ISB via multicast
    public static void sendResultToISBviaMulticast(HashSet<URLData> resultado) {

        try {
            // Create a multicast socket
            MulticastSocket multicastSocket = new MulticastSocket();

            // Convert the message to bytes
            StringBuilder messageBuilder = new StringBuilder();
            for (URLData data : resultado) {
                messageBuilder.append(data.toString()).append("\n");
            }
            String mensagem = messageBuilder.toString();
            byte[] buffer = mensagem.getBytes();

            // Get the multicast address
            InetAddress group = InetAddress.getByName(multicastAddress);

            // Create a datagram packet to send
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastPort);

            // Send the packet
            multicastSocket.send(packet);

            // Close the socket
            multicastSocket.close();

            System.out.println("Multicast message sent successfully.");

        } catch (IOException e) {
            System.out.println("Error sending multicast message: " + e.getMessage());
        }
    }
}
