import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/*--------------------------------------------DOWNLOADERMANAGER--------------------------------------------*/
public class DownloaderManager {

    private static PrintWriter queueManager;

    public static void main(String[] args) throws IOException {
        socketQueueManagerToDownloadManager();
    }

    // Receive from QueueManager and scrape
    private static void socketQueueManagerToDownloadManager() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PortasEIPs.PORTA_DOWNLOAD_MANAGER.getPorta());

        if (!socketDownloadManagerToQueue()) {
            System.out.println("Failed to connect to QueueManager.");
            return;
        }

        // download manager ready
        System.out.println("[" + PortasEIPs.PORTA_DOWNLOAD_MANAGER + "] DownloadManager ready.");

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
                    String urlParaScrape;
                    while ((urlParaScrape = inFromClient.readLine()) != null) {

                        // thread para cada link recebido pelo download manager
                        new DownloaderThread(urlParaScrape, queueManager).start();
                    }

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
                Socket socket = new Socket(PortasEIPs.PORTA_QUEUE_MANAGER.getIP(), PortasEIPs.PORTA_QUEUE_MANAGER.getPorta());
                queueManager = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("Ligação ao QueueManager de sucesso! IP: " + PortasEIPs.PORTA_QUEUE_MANAGER);
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

}
