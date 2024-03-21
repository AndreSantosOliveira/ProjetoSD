import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

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
                    // Criar stream de entrada
                    DataInputStream dataIn = new DataInputStream(connectionSocket.getInputStream());

                    // Enquanto houver dados para ler
                    while (dataIn.available() > 0) {
                        String url = dataIn.readUTF(); // Ler URL

                        System.out.println("DownloadManager recebeu para crawl: " + url);
                    }
                    connectionSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
