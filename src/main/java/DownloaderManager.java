import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class DownloaderManager {

    private static PrintWriter queueManager;

    public static void main(String[] args) throws IOException {
        if (!socketDownloadManagerToQueue()) {
            System.out.println("Failed to connect to QueueManager.");
            return;
        }
        socketQueueManagerToDownloadManager();

        // download manager ready
        System.out.println("DownloaderManager ready.");


        // TODO 1: Fetch url from URL queue
        // TODO 2: URL -> new thread -> Downloader.crawl(url) -> URLData

        //queueLinks.offer("https://www.sapo.pt/");

        //while (!queueLinks.isEmpty()) {
        //    crawl(queueLinks.poll());
        //}
    }

    private static boolean socketDownloadManagerToQueue() {
        final int MAX_RETRIES = 5; // Maximum number of retries
        int attempt = 0; // Current attempt counter

        while (attempt < MAX_RETRIES) {
            try {
                // Attempt to connect to QueueManager via TCP
                Socket socket = new Socket("127.0.0.1", 3569);
                queueManager = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("Ligação ao QueueManager de sucesso!");
                return true;
            } catch (Exception re) {
                System.out.println("Exception in DownloadManager Socket on attempt " + (attempt + 1) + ": " + re);

                attempt++; // Increment the attempt counter

                if (attempt < MAX_RETRIES) {
                    try {
                        // Wait for 1 second before retrying
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        System.out.println("An interruption occurred while waiting to retry: " + ie);
                    }
                } else {
                    System.out.println("Failed to connect after " + MAX_RETRIES + " attempts.");
                }
            }
        }
        return false;
    }


    private static void socketQueueManagerToDownloadManager() throws IOException {
        ServerSocket serverSocket = new ServerSocket(3570);
        System.out.println("DownloadManager a escutar na porta 3570");

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
                    String clientSentence;
                    while ((clientSentence = inFromClient.readLine()) != null) {
                        System.out.println("Recebido novo URL para Crawl: " + clientSentence);
                        queueManager.println(clientSentence);
                        System.out.println("DownloadManager enviou para QueueManager: " + clientSentence);
                    }

                    //connectionSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
