import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class QueueManager {

    static UniqueQueue<String> queueLinks = new UniqueQueue<>(50);

    public static void main(String[] args) throws IOException {
        try {
            // Criar socket de receção Gateway->QueueManager
            ServerSocket serverSocket = new ServerSocket(5001);
            System.out.println("QueueManager TCP a escutar na porta 5001");

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