import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

public class Barrel implements MetodosBarrel, Serializable {
    // Define the multicast address and port
    static String multicastAddress = "230.0.0.1";
    static int multicastPort = 6900;

    // Constructor
    public Barrel() throws RemoteException {
        super();
    }

    // Receives multicast from downloader
    public static void receiveResultFromDownloaderviaMulticast() {
        // Receives multicast from downloader
        try {
            // Create a multicast socket
            MulticastSocket multicastSocket = new MulticastSocket(multicastPort);

            // Get the multicast group address
            InetAddress group = InetAddress.getByName(multicastAddress);

            // Join the multicast group
            multicastSocket.joinGroup(group);

            byte[] buffer = new byte[1024];

            // Receive the multicast packet
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                // Convert the packet data to string and process it
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received multicast message: " + message);
                // Process the message here as needed
            }

        } catch (IOException e) {
            System.out.println("Error receiving multicast message: " + e.getMessage());
        }
    }

    // SearchURL
    public String searchUrl(String url) throws RemoteException {
        return null;
    }

    public static void main(String args[]) {
        // Receives multicast from downloader
        receiveResultFromDownloaderviaMulticast();
    }

}