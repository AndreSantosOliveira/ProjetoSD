import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

public class QueueManager extends UnicastRemoteObject implements Serializable {

    protected QueueManager() throws RemoteException {
        super();
    }

    private static PrintWriter downloadManager;

    // queue manager
    static final UniqueQueue<String> queue = new UniqueQueue<>(50);

    static Set<String> alreadyCrawled = new HashSet<>();

    public static void main(String[] args) {
        try {
            // Criar socket de receção Gateway->QueueManager
            ServerSocket serverSocket = new ServerSocket(3569);
            System.out.println("QueueManager a escutar na porta 3569");

            if (!connectToDownloadManager()) {
                System.out.println("Failed to connect to DownloadManager.");
                return;
            }

            new Thread(() -> {
                try {
                    while (true) {
                        synchronized (queue) {
                            String url = queue.poll();
                            if (url != null) {
                                downloadManager.println(url);
                                System.out.println("QueueManager enviou para DownloadManager: " + url);
                                Thread.sleep(1000);
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

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
                            //System.out.println("Recebido da Gateway para indexar: " + dados);
                            synchronized (alreadyCrawled) {
                                if (alreadyCrawled.contains(dados)) {
                                    continue;
                                }
                                alreadyCrawled.add(dados);
                                synchronized (queue) {
                                    queue.offer(dados);
                                }
                                //queue size
                                System.out.println("URLs para indexar: " + queue.size());
                            }
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