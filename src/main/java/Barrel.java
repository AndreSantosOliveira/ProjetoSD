import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * The Barrel class is responsible for managing a collection of URLData objects.
 * It extends UnicastRemoteObject and implements MetodosRMIBarrel and Serializable.
 */
public class Barrel extends UnicastRemoteObject implements MetodosRMIBarrel, Serializable {

    // A HashMap to store URLData objects, where the key is a String and the value is a HashSet of URLData objects.
    static HashMap<String, HashSet<URLData>> index = new HashMap<>();

    /**
     * Default constructor for Barrel.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    protected Barrel() throws RemoteException {
        super();
    }

    static String barrelID;

    /**
     * The main method for the Barrel class.
     * It expects two command line arguments: the port number and the ID of the barrel.
     * It creates a new Barrel object and binds it to the RMI registry.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Barrel <PORT> <ID>");
            System.exit(1);
        }

        try {
            int porta = Integer.parseInt(args[0]);
            barrelID = args[1];

            Barrel barr = new Barrel();
            LocateRegistry.createRegistry(porta).rebind(barrelID, barr);

            // verify if there is a file with content available
            try {
                // get the file creation date
                File file = new File("src/main/java/barrelContent.barrel");
                if (file.exists()) {
                    long lastModified = file.lastModified();
                    Date date = new Date(lastModified);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    System.out.println("Syncing myself with previous barrel content from: " + sdf.format(date));

                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    index = (HashMap<String, HashSet<URLData>>) ois.readObject();
                    ois.close();
                    fis.close();
                    System.out.println("Sucessfully synced with the barrel content available in the directory! Loaded " + index.size() + " words and their links.");
                }
            } catch (IOException | ClassNotFoundException c) {
                System.out.println("O conteúdo do ficheiro da barrel está desatualizado.");
                System.exit(1);
            }

            System.out.println("Barrel " + barrelID + " ready: 127.0.0.1:" + porta);

            // Receives multicast from downloader
            receiveResultFromDownloaderviaMulticast();
        } catch (IOException re) {
            System.out.println("Exception in Barrel RMI: " + re);
        }
    }

    /**
     * This method is used to archive URLData objects.
     * It splits the page title of the URLData object into words and adds the URLData object to the HashSet associated with each word in the index HashMap.
     *
     * @param data URLData object to be archived
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    public static void archiveURL(URLData data) throws RemoteException {
        System.out.println("Received " + data + " to index.");

        for (String palavra : data.getPageTitle().split(" ")) {
            palavra = palavra.toLowerCase();
            if (index.containsKey(palavra)) {
                index.get(palavra).add(data);
            } else {
                HashSet<URLData> urls = new HashSet<>();
                urls.add(data);
                index.put(palavra, urls);
            }
        }
    }

    /**
     * This method is used to search for URLData objects.
     * It splits the input string into words and returns a list of URLData objects that match any of the words.
     *
     * @param palavras String of words to search for
     * @return List of URLData objects that match the search criteria
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    @Override
    public List<URLData> searchInput(String palavras) throws RemoteException {
        List<URLData> dadosBarrel = new ArrayList<>();

        for (String s : palavras.split(" ")) {
            for (String chaves : index.keySet()) {
                if (chaves.toLowerCase().contains(s.toLowerCase())) {
                    dadosBarrel.addAll(index.get(chaves));
                }
            }
        }

        return dadosBarrel;
    }

    @Override
    public void saveBarrelsContent() throws RemoteException {
        // Write contents of this barrel (index) to a object file
        try {
            FileOutputStream fos = new FileOutputStream("src/main/java/barrelContent.barrel");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(index);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Receives multicast from downloader.
     */
    public static void receiveResultFromDownloaderviaMulticast() {
        // Receives multicast from downloader
        try {
            // Create a multicast socket
            MulticastSocket multicastSocket = new MulticastSocket(ConnectionsEnum.MULTICAST.getPort());
            multicastSocket.joinGroup(InetAddress.getByName(ConnectionsEnum.MULTICAST.getIP()));

            byte[] buffer = new byte[1024];

            // Receive the multicast packet
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                // Convert the packet data to string and process it
                String message = new String(packet.getData(), 0, packet.getLength());

                // message is equal to url|title
                String[] parts = message.split("\\|");
                if (parts.length == 2) {
                    String url = parts[0];
                    String title = parts[1];

                    archiveURL(new URLData(url, title));
                    //System.out.println("Success in sending to archive URL: " + url + " with title: " + title);
                } else { //there are strings that arrive cut off..
                    System.err.println("Received invalid message: " + message);
                }
            }

        } catch (IOException e) { //there are strings that arrive cut off..
            // e.printStackTrace();
            //System.out.println("Error receiving multicast message: " + e.getMessage());
        }
    }
}