import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

/*--------------------------------------------DOWNLOADERMANAGER--------------------------------------------*/
public class DownloaderManager {

    //definir IPs e Portas dos Downloaders com base no DescritorIPPorta

    private static Map<DescritorIPPorta, MetodosRMIDownloader> downloaders = new HashMap<>();

    private static PrintWriter queueManager;

    public static void main(String[] args) throws IOException {
        downloaders.put(new DescritorIPPorta("127.0.0.1", 5432, "dl1"), null);

        for (DescritorIPPorta descritorIPPorta : downloaders.keySet()) {
            MetodosRMIDownloader res = tentarLigarADownloader(descritorIPPorta);
            if (res != null) {
                downloaders.put(descritorIPPorta, res);
            }
        }

        socketQueueManagerToDownloadManager();
    }

    private static MetodosRMIDownloader tentarLigarADownloader(DescritorIPPorta descritorIPPorta) {
        MetodosRMIDownloader metodosGateway = null;
        int retryCount = 0;
        int maxRetries = 5;
        while (metodosGateway == null && retryCount < maxRetries) {
            try {
                metodosGateway = (MetodosRMIDownloader) LocateRegistry.getRegistry(descritorIPPorta.getPorta()).lookup("gatewau");
                return metodosGateway;
            } catch (RemoteException | NotBoundException e) {
                ++retryCount;
                if (retryCount < maxRetries) {
                    System.out.println("Failed to connect to Downloader: " + descritorIPPorta.getRMIName() + ". Retrying...");
                    // Sleep para evitar tentativas de ligação consecutivas
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

                        synchronized (downloaders) {
                            for (Map.Entry<DescritorIPPorta, MetodosRMIDownloader> downloader : downloaders.entrySet()) {
                                if (downloader.getValue() != null && !downloader.getValue().isBusy()) {
                                    downloader.getValue().crawlURL(urlParaScrape, queueManager);
                                }
                            }
                        }

                        // thread para cada link recebido pelo download manager
                        //new Downloader(urlParaScrape, queueManager).start();
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
