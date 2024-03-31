import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
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
            String dlID = args[1];

            Barrel barr = new Barrel();
            LocateRegistry.createRegistry(porta).rebind(dlID, barr);

            System.out.println("Barrel " + dlID + " ready: 127.0.0.1:" + porta);

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
    @Override
    public void archiveURL(URLData data) throws RemoteException {
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
}