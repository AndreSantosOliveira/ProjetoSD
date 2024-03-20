import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class QueueManager {

    static ServerSocket serverSocket = null;

    static UniqueQueue<String> queueLinks = new UniqueQueue<>(50);

    public static void main(String[] args) throws IOException {
        try {
            ServerSocket serverSocket = new ServerSocket(5001);
            System.out.println("Server Listening on port 5001...");

            while (true) {
                Socket connectionSocket = serverSocket.accept();
                new Thread(() -> {

                    try {
                        DataInputStream dataIn = new DataInputStream(connectionSocket.getInputStream());

                        while (dataIn.available() > 0) {
                            String url = dataIn.readUTF();

                            queueLinks.offer(url);
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