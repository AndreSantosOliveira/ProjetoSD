import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class QueueManager extends UnicastRemoteObject implements Serializable {

    static UniqueQueue<String> queueLinks = new UniqueQueue<>(50);

    protected QueueManager() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws IOException {
        try {

            RMIDownloaderMGR ci = null;

            try {
                QueueManager queueManager = new QueueManager();
                LocateRegistry.createRegistry(3500).rebind("queuemanager", queueManager);
            } catch (IOException re) {
                System.out.println("Exception in Gateway RMI: " + re);
            }

            try {
                ci = (RMIDownloaderMGR) Naming.lookup("rmi://localhost:3510/downloaderManager");
            } catch (IOException | NotBoundException e) {
                System.out.println("Exception in Gateway RMI: " + e);
            }

            // Criar socket de receção Gateway->QueueManager
            ServerSocket serverSocket = new ServerSocket(3569);
            System.out.println("QueueManager TCP a escutar na porta 3003");

            RMIDownloaderMGR finalCi = ci;
            new Thread(() -> {
                try {
                    while (!queueLinks.isEmpty()) {
                        finalCi.receberLinkParaDownloadManager(queueLinks.poll());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();



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

                            queueLinks.offer(url); // Adicionar URL à fila
                            System.out.println("QueueManager recebeu para indexação: " + url + " | " + queueLinks.size() + " links na fila.");
                        }
                        connectionSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao criar socket de receção Gateway->QueueManager: " + e.getMessage());
        }
    }
}