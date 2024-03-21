import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/*--------------------------------------------DOWNLOADERMANAGER--------------------------------------------*/
public class DownloaderManager {

    private static PrintWriter queueManager;

    public static void main(String[] args) throws IOException {
        socketQueueManagerToDownloadManager();

        System.out.println("DownloadManager ready.");
    }

    // Receive from QueueManager and crawl
    private static void socketQueueManagerToDownloadManager() throws IOException {
        ServerSocket serverSocket = new ServerSocket(3570);
        System.out.println("DownloadManager a escutar na porta 3570");

        if (!socketDownloadManagerToQueue()) {
            System.out.println("Failed to connect to QueueManager.");
            return;
        }

        // download manager ready
        System.out.println("DownloaderManager ready.");

        // Aceitar ligações
        while (true) {
            // Aceitar ligação
            Socket connectionSocket = serverSocket.accept();
            // Criar thread para tratar a conexão
            new Thread(() -> {
                try {
                    // setup bufferedreader para ler mensagens de clientes
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                    // ler mensagens do cliente
                    String urlParaCrawl;
                    while ((urlParaCrawl = inFromClient.readLine()) != null) {
                        //queueManager.println(clientSentence);
                        //System.out.println("DownloadManager enviou para QueueManager: " + urlParaCrawl);

                        crawlDownloader(urlParaCrawl);
                    }

                    //connectionSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    // Send to QueueManager
    private static boolean socketDownloadManagerToQueue() {
        final int maxTentativa = 10; // Maximum number of retries
        int tentativa = 0; // Current attempt counter

        while (tentativa < maxTentativa) {
            try {
                // Attempt to connect to QueueManager via TCP
                Socket socket = new Socket("127.0.0.1", 3569);
                queueManager = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("Ligação ao QueueManager de sucesso!");
                return true;
            } catch (Exception re) {
                System.out.println("Erro ao ligar ao QueueManager - tentativa nº" + (tentativa + 1) + ": " + re);
                ++tentativa;
                if (tentativa < maxTentativa) {
                    try {
                        Thread.sleep(1001);
                    } catch (InterruptedException ie) {
                        System.out.println("Ocorreu um problema no sleep: " + ie);
                    }
                } else {
                    System.out.println("Falha ao ligar ao DownloadManager após " + tentativa + " tentativas.");
                }
            }
        }
        return false;
    }

    /*--------------------------------------------DOWNLOADER--------------------------------------------*/

    static HashMap<String, HashSet<URLData>> index = new HashMap<>();
    static Set<String> alreadyCrawled = new HashSet<>();

    // Define the multicast address and port
    static String multicastAddress = "230.0.0.1";
    static int multicastPort = 6900;

    public static Boolean doesIndexHaveURL(String chave, String url) {
        return index.get(chave).stream().anyMatch(urlData -> urlData.getURL().equalsIgnoreCase(url));
    }

    public static void crawlDownloader(String url) {
        System.out.println("Crawling: " + url);
        try {
            Document doc = Jsoup.connect(url).get();
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int countTokens = 0;
            while (tokens.hasMoreElements() && countTokens++ < 100) {
                //System.out.println(tokens.nextToken().toLowerCase());
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
                                // queueLinks.offer(link); //TODO: Enviar para a queue

                                if (!alreadyCrawled.contains(link)) {
                                    alreadyCrawled.add(link);
                                    //
                                    //System.out.println("DownloadManager enviou para QueueManager: " + link);
                                }
                            }
                        }
                    }
                }
            }

            alreadyCrawled.forEach(s -> queueManager.println(s));

            // Send dummy results via multicas
            sendResultToISBviaMulticast(alreadyCrawled.stream().map(s -> new URLData(s, "Dummy")).collect(Collectors.toCollection(HashSet::new)));
            
            System.out.println("Crawling done! - " + alreadyCrawled.size() + " unique URLs sent to QueueManager.");
            alreadyCrawled.clear();
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no Downloader!");
            throw new RuntimeException(e);
        }
    }

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
