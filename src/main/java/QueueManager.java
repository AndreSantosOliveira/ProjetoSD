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

    // queue manager
    static final UniqueQueue<String> queue = new UniqueQueue<>(50);

    public static void main(String[] args) {
        try {
            // Criar socket de receção Gateway->QueueManager
            ServerSocket serverSocket = new ServerSocket(PortasEIPs.PORTA_QUEUE_MANAGER.getPorta());

            PortasEIPs.PORTA_QUEUE_MANAGER.printINIT("DownloadManager");

            if (!connectToDownloadManager()) {
                System.out.println("Failed to connect to DownloadManager.");
                return;
            }

            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    synchronized (queue) {
                        if (!queue.isEmpty()) {
                            String url = queue.poll();
                            if (url != null) {
                                downloadManager.println(url);
                                System.out.println("QueueManager enviou para Scraping: " + url);
                            }
                        }
                    }
                }

            }).start();

            //fim carregamento do QueueManager
            PortasEIPs.PORTA_DOWNLOAD_MANAGER.printINIT("QueueManager");

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
                        while ((dados = inFromClient.readLine()) != null) {
                            synchronized (queue) {
                                if (queue.offer(dados)) {
                                    System.out.println("Recebido novo URL para indexar: " + dados);
                                    //queue size
                                    System.out.println("URLs para indexar: " + queue.size());
                                }
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
                Socket socket = new Socket(PortasEIPs.PORTA_DOWNLOAD_MANAGER.getIP(), PortasEIPs.PORTA_DOWNLOAD_MANAGER.getPorta());
                System.out.println("Ligação ao DownloadManager com sucesso! IP: " + PortasEIPs.PORTA_DOWNLOAD_MANAGER);

                downloadManager = new PrintWriter(socket.getOutputStream(), true);
                return true;
            } catch (IOException re) {
                System.out.println("Erro ao ligar ao DownloadManager - tentativa nº" + (tentativa + 1) + ": " + re);
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