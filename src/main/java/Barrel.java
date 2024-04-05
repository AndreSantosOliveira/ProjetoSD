import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
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

    // Store in this hashmap the main URL and the urls that link to it
    static HashMap<String, HashSet<String>> urlEApontadoresParaURL = new HashMap<>();

    // The ID of the barrel
    static String barrelID;

    /**
     * Default constructor for Barrel.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    protected Barrel() throws RemoteException {
        super();
    }


    /**
     * The main method for the Barrel class.
     * It expects two command line arguments: the port number and the ID of the barrel.
     * It creates a new Barrel object and binds it to the RMI registry.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        // Verify if the command line arguments are valid
        if (args.length < 2) {
            System.out.println("Barrel <PORT> <ID>");
            System.exit(1);
        }

        try {
            int porta = Integer.parseInt(args[0]);
            barrelID = args[1];

            Barrel barr = new Barrel();
            LocateRegistry.createRegistry(porta).rebind(barrelID, barr);

            // Verify if there is a file with content available
            try {
                // Get the file creation date
                File file = new File("src/main/java/barrelContent." + barrelID);
                if (file.exists()) {
                    long lastModified = file.lastModified();
                    Date date = new Date(lastModified);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    System.out.println("Syncing myself with previous barrel content from: " + sdf.format(date));

                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    Tuple<HashMap<String, HashSet<URLData>>, HashMap<String, HashSet<String>>> tup = (Tuple<HashMap<String, HashSet<URLData>>, HashMap<String, HashSet<String>>>) ois.readObject();
                    index = tup.getFirst();
                    urlEApontadoresParaURL = tup.getSecond();
                    ois.close();
                    fis.close();
                    System.out.println("Sucessfully synced with the barrel content available in the directory! Loaded " + index.size() + " words and " + urlEApontadoresParaURL.size() + " references to URLs.");
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

        // Split the page title into words and add the URLData object to the HashSet associated with each word in the index HashMap
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

        // Save the URL and the URL where it was found
        if (urlEApontadoresParaURL.containsKey(data.getURL())) {
            urlEApontadoresParaURL.get(data.getURL()).add(data.getURLWhereItWasFound());
        } else {
            HashSet<String> urls = new HashSet<>();
            urls.add(data.getURLWhereItWasFound());
            urlEApontadoresParaURL.put(data.getURL(), urls);
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
    public Tuple<String, List<URLData>> searchInput(String palavras) throws RemoteException {
        List<URLData> dadosBarrel = new ArrayList<>();

        for (String s : palavras.split(" ")) {
            for (String chaves : index.keySet()) {
                if (chaves.toLowerCase().contains(s.toLowerCase())) {
                    HashSet<URLData> urlData = index.get(chaves);
                    //Add relevance to each URLData object
                    urlData.forEach(urlData1 -> urlData1.setRelevance(urlEApontadoresParaURL.containsKey(urlData1.getURL()) ? urlEApontadoresParaURL.get(urlData1.getURL()).size() : 0));
                    dadosBarrel.addAll(urlData);
                }
            }
        }

        return new Tuple<>(barrelID, dadosBarrel);
    }

    /**
     * This method is used to save the contents of the barrel to a file.
     *
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    @Override
    public void saveBarrelContent() throws RemoteException {
        // Write contents of this barrel (index) to a object file
        try {
            FileOutputStream fos = new FileOutputStream("src/main/java/barrelContent." + barrelID);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(new Tuple<>(new HashMap<>(index), new HashMap<>(urlEApontadoresParaURL)));
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * This method is used to get the list of indexes for an url.
     *
     * @return HashMap of the index of the barrel
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    @Override
    public List<String> linksListForURL(String url) throws RemoteException {
        // Print the URL and the URLs that link to it
        return urlEApontadoresParaURL.containsKey(url) ? new ArrayList<>(urlEApontadoresParaURL.get(url)) : new ArrayList<>();
    }

    @Override
    public void shutdown(String motive) throws RemoteException {
        System.out.println("Barrel " + barrelID + " is shutting down. Reason: " + motive);
        saveBarrelContent();
        System.exit(0);
    }

    @Override
    public String getBarrelID() throws RemoteException {
        return barrelID;
    }

    @Override
    public String copyBarrelContents(Connection connection) throws RemoteException {
        try {
            System.out.println("Copying barrel contents to destination barrel " + connection.getRMIName() + " @ " + connection);
            MetodosRMIBarrel res = (MetodosRMIBarrel) Naming.lookup("rmi://" + connection.getIP() + ":" + connection.getPorta() + "/" + connection.getRMIName());
            return res.receiveBarrelContents(barrelID, new HashMap<>(index), new HashMap<>(urlEApontadoresParaURL));
        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            // try with external ip
            try {
                System.out.println("Trying to connect to external IP: " + connection.getExternalIP());
                MetodosRMIBarrel res = (MetodosRMIBarrel) Naming.lookup("rmi://" + connection.getExternalIP() + ":" + connection.getPorta() + "/" + connection.getRMIName());
                return res.receiveBarrelContents(barrelID, new HashMap<>(index), new HashMap<>(urlEApontadoresParaURL));
            } catch (MalformedURLException | NotBoundException | RemoteException e2) {
                return "Error connecting to destination barrel: " + e.getMessage();
            }
        }
    }

    @Override
    public String receiveBarrelContents(String barrelID, HashMap<String, HashSet<URLData>> indexCopy, HashMap<String, HashSet<String>> urlEApontadoresParaURLCopy) throws RemoteException {
        index = indexCopy;
        urlEApontadoresParaURL = urlEApontadoresParaURLCopy;
        System.out.printf("Synced barrel contents with barrel %s: %d words and %d references to URLs.%n", barrelID, indexCopy.size(), urlEApontadoresParaURLCopy.size());
        saveBarrelContent();
        return "Copy of barrel contents successful!";
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
                String[] parts = message.split("§±");
                if (parts.length == 3) {
                    String url = parts[0];
                    String title = parts[1];
                    String urlOndeFoiEncontrado = parts[2];

                    archiveURL(new URLData(url, title, urlOndeFoiEncontrado));
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