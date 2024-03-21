import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class QueueManager extends UnicastRemoteObject implements Serializable {

    protected QueueManager() throws RemoteException {
        super();
    }

    private static PrintWriter downloadManager;

    public static void main(String[] args) {
        connectToDownloadManager();

        try {
            // Criar socket de receção Gateway->QueueManager

            ServerSocket serverSocket = new ServerSocket(3569);
            System.out.println("QueueManager a escutar na porta 3569");

            //fim carregamento do QueueManager
            System.out.println("QueueManager ready.");

            // Aceitar ligações
            while (true) {
                // Aceitar ligação
                Socket connectionSocket = serverSocket.accept();
                // Informação sobre a ligação:
                System.out.println("QueueManager recebeu ligação de: " + connectionSocket.getInetAddress().getHostAddress() + ":" + connectionSocket.getPort());
                // Criar thread para tratar a conexão
                new Thread(() -> {
                    try {
                        // setup bufferedreader para ler mensagens de clientes
                        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                        // ler mensagens do cliente
                        String dados;
                        while ((dados = inFromClient.readLine()) != null) { // Continuously read lines sent from client
                            System.out.println("Recebido da Gateway para indexar: " + dados);
                            downloadManager.println(dados);
                            System.out.println("QueueManager enviou para crawl: " + dados);
                        }

                        //connectionSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao criar socket de receção Gateway->QueueManager: " + e.getMessage());
        }
    }

    private static void connectToDownloadManager() {
        final int MAX_RETRIES = 5; // Maximum number of retries
        int attempt = 0; // Current attempt counter

        while (attempt < MAX_RETRIES) {
            try {
                // Attempt to connect to DownloadManager via TCP
                Socket socket = new Socket("127.0.0.1", 3570);
                System.out.println("Ligação ao DownloadManager de sucesso!");

                downloadManager = new PrintWriter(socket.getOutputStream(), true);
                break; // Exit loop if connection is successful
            } catch (IOException re) {
                System.out.println("Exception in Gateway Socket on attempt " + (attempt + 1) + ": " + re);

                attempt++; // Increment attempt counter

                if (attempt < MAX_RETRIES) {
                    try {
                        // Wait for 1 second before retrying
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        System.out.println("An interruption occurred while waiting to retry: " + ie);
                        // Optional: handle the InterruptedException, for example, restore the interrupted status
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.out.println("Failed to connect to DownloadManager after " + MAX_RETRIES + " attempts.");
                }
            }
        }
    }

}