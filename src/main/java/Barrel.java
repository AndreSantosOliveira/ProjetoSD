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
 * Barrel class extends UnicastRemoteObject and implements MetodosRMIBarrel and Serializable.
 * This class is responsible for managing a collection of URLData objects.
 */
public class Barrel extends UnicastRemoteObject implements MetodosRMIBarrel, Serializable {

    // HashMap to store URLData objects
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
     * Main method for the Barrel class.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Barrel <PORTA> <ID>");
            System.exit(1);
        }

        try {
            int porta = Integer.parseInt(args[0]);
            String dlID = args[1];

            Barrel barr = new Barrel();
            LocateRegistry.createRegistry(porta).rebind(dlID, barr);

            System.out.println("Barrel " + dlID + " ready: 127.0.0.1:" + porta);

        } catch (IOException re) {
            System.out.println("Exception in Gateway RMI: " + re);
        }
    }


    /**
     * Method to archive URLData objects.
     *
     * @param data URLData object to be archived
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    @Override
    public void arquivarURL(URLData data) throws RemoteException {
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
     * Method to search for URLData objects.
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