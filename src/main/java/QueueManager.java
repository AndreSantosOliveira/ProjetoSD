import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class QueueManager {

    static ServerSocket serverSocket = null;

    public static void main(String[] args) throws IOException {
        receive();
    }

    static UniqueQueue<String> queueLinks = new UniqueQueue<>(50);

    // Set up receive socket
    public static void receive() throws IOException {
        ServerSocket serverSocket = new ServerSocket(5001);
        System.out.println("Server Listening on port 5001...");

        while (true) {
            Socket connectionSocket = serverSocket.accept();
            new Thread(() -> {

                try {
                    DataInputStream dataIn = new DataInputStream(connectionSocket.getInputStream());

                    String operator = dataIn.readUTF();

                    System.out.println(operator);

                    connectionSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).start();
        }
    }
}