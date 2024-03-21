import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class DownloaderManager {

    public static void main(String[] args) throws IOException {
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

                    }

                    //connectionSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
