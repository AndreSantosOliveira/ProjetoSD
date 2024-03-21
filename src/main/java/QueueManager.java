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
        try {
            // Criar socket de receção Gateway->QueueManager
            ServerSocket serverSocket = new ServerSocket(3569);
            System.out.println("QueueManager a escutar na porta 3569");

            if (!connectToDownloadManager()) {
                System.out.println("Failed to connect to DownloadManager.");
                return;
            }

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

    private static boolean connectToDownloadManager() {
        final int maxTentativa = 10;
        int tentativa = 0;

        while (tentativa < 10) {
            try {
                // Attempt to connect to DownloadManager via TCP
                Socket socket = new Socket("127.0.0.1", 3570);
                System.out.println("Ligação ao DownloadManager de sucesso!");

                downloadManager = new PrintWriter(socket.getOutputStream(), true);
                return true;
            } catch (IOException re) {
                System.out.println("Erro ao ligar ao DownloadManager - tentativa nº" + (tentativa + 1) + ": " + re);
                ++tentativa;
                if (tentativa < maxTentativa) {
                    try {
                        Thread.sleep(1000);
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