import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BarrelManager class implements MetodosRMIBarrel and Serializable.
 * This class is responsible for managing a collection of barrels.
 */
public class BarrelManager implements MetodosRMIBarrel, Serializable {

    // Map to store barrels
    private static Map<Connection, MetodosRMIBarrel> barrels = new HashMap<>();
    private static int barrelsON;

    /**
     * Default constructor for BarrelManager.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    public BarrelManager() throws RemoteException {
        super();
    }

    /**
     * Main method for the BarrelManager class.
     *
     * @param args command line arguments
     */
    public static void main(String args[]) throws RemoteException {
        try {
            BarrelManager barrelManager = new BarrelManager();
            LocateRegistry.createRegistry(ConnectionsEnum.BARREL_MANAGER.getPort()).rebind("barrelmanager", barrelManager);
        } catch (IOException re) {
            System.out.println("Exception in Gateway RMI: " + re);
        }

        // Load barrels from the text file barrels.txt (IP, port, rmiName)
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/barrels.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    try {
                        String ip = parts[0];
                        int porta = Integer.parseInt(parts[1]);
                        String rmiName = parts[2];

                        barrels.put(new Connection(ip, porta, rmiName), null);
                        System.out.println("Barrel added: " + rmiName + " (" + ip + ":" + porta + ")");
                    } catch (NumberFormatException e) {
                        System.err.println("Error processing the port for a barrel: " + line);
                    }
                } else {
                    System.err.println("Line in invalid format: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the barrel file.");
            e.printStackTrace();
        }

        for (Connection descritorIPPorta : barrels.keySet()) {
            MetodosRMIBarrel res = tentarLigarABarrel(descritorIPPorta);
            if (res != null) {
                barrels.put(descritorIPPorta, res);
                ++barrelsON;
            }
        }

        if (barrelsON == 0) {
            System.err.println("No barrel has been connected. Exiting...");
            System.exit(1);
        }

        ConnectionsEnum.BARREL_MANAGER.printINIT("BarrelManager");


    }

    /**
     * Attempts to connect to a barrel.
     *
     * @param descritorIPPorta descriptor of the barrel to connect to
     * @return MetodosRMIBarrel object if the connection is successful, null otherwise.
     */
    private static MetodosRMIBarrel tentarLigarABarrel(Connection descritorIPPorta) {
        MetodosRMIBarrel metodosBarrel = null;
        int retryCount = 0;
        int maxRetries = 5;
        while (metodosBarrel == null && retryCount < maxRetries) {
            try {
                metodosBarrel = (MetodosRMIBarrel) LocateRegistry.getRegistry(descritorIPPorta.getPorta()).lookup(descritorIPPorta.getRMIName());
                System.out.println("Connected to Barrel " + descritorIPPorta.getRMIName() + "!");
                return metodosBarrel;
            } catch (RemoteException | NotBoundException e) {
                ++retryCount;
                if (retryCount < maxRetries) {
                    System.out.println("Failed to connect to Barrel: " + descritorIPPorta.getRMIName() + " (" + retryCount + "/" + maxRetries + "). Retrying...");
                    // Sleep to avoid consecutive connection attempts
                    try {
                        Thread.sleep(1001);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        System.out.println("Failed to connect to Barrel: " + descritorIPPorta.getRMIName() + ". :(");
        return null;
    }

    /**
     * Searches for URLData objects.
     *
     * @param pesquisa String of words to search for
     * @return List of URLData objects that match the search criteria
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    @Override
    public List<URLData> searchInput(String pesquisa) throws RemoteException {
        List<URLData> dados = new ArrayList<>();
        for (MetodosRMIBarrel value : barrels.values()) {
            if (value != null) {
                try {
                    List<URLData> dadosDownloader = value.searchInput(pesquisa);
                    if (dadosDownloader != null) {
                        dados.addAll(dadosDownloader);
                    }
                } catch (RemoteException e) {
                    return Collections.singletonList(new URLData("?", "Error searching for: " + pesquisa));
                }
            }
        }
        return dados;
    }
}