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
import java.util.stream.Collectors;

/**
 * BarrelManager class implements MetodosRMIBarrel and Serializable.
 * This class is responsible for managing a collection of barrels.
 */
public class BarrelManager implements MetodosRMIBarrel, Serializable {

    // Map to store barrels
    private final List<MetodosRMIBarrel> barrels = new ArrayList<>();
    private final Map<String, String> activeBarrelsIDIP = new HashMap<>();

    /**
     * Default constructor for BarrelManager.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    public BarrelManager() throws RemoteException {
        super();

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

                        MetodosRMIBarrel res = tentarLigarABarrel(new Connection(ip, porta, rmiName));
                        if (res != null) {
                            this.barrels.add(res);
                            activeBarrelsIDIP.put(res.getBarrelID(), ip + ":" + porta);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error processing the port for a barrel: " + line);
                    }
                } else {
                    System.err.println("Line in invalid format: " + line);
                }
            }

            if (this.barrels.isEmpty()) {
                System.err.println("No barrel has been connected. Exiting...");
                System.exit(1);
            }

            ConnectionsEnum.BARREL_MANAGER.printINIT("BarrelManager");

        } catch (IOException e) {
            System.err.println("Error reading the barrel file: ");
            e.printStackTrace();
        }
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
            while (true) {
            }
        } catch (IOException re) {
            System.out.println("Exception in Gateway RMI: " + re);
        }
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
    public Tuple<String, List<URLData>> searchInput(String pesquisa) throws RemoteException {
        String id = "none";
        Map<String, String> urlTitulo = new HashMap<>();
        Map<String, Integer> relevace = new HashMap<>();
        synchronized (barrels) {
            for (MetodosRMIBarrel value : barrels) {
                if (value != null) {
                    try {
                        Tuple<String, List<URLData>> dadosDownloader = value.searchInput(pesquisa);
                        if (dadosDownloader != null) {
                            id = dadosDownloader.getFirst();
                            for (URLData urlData : dadosDownloader.getSecond()) {
                                if (!urlTitulo.containsKey(urlData.getURL())) {
                                    urlTitulo.put(urlData.getURL(), urlData.getPageTitle());
                                    relevace.put(urlData.getURL(), urlData.getRelevance());
                                }
                            }
                        }
                    } catch (RemoteException e) {
                        return new Tuple<>(id, Collections.singletonList(new URLData(e.getMessage(), "Error searching for: " + pesquisa, "none")));
                    }
                    break; // so precisamos de um barrel funcional
                }
            }

            return new Tuple<>(id, urlTitulo.entrySet()
                    .stream()
                    .map(entry -> new URLData(entry.getKey(), entry.getValue(), relevace.get(entry.getKey())))
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public void saveBarrelsContent() throws RemoteException {
        synchronized (barrels) {
            for (MetodosRMIBarrel value : barrels) {
                if (value != null) {
                    try {
                        value.saveBarrelsContent();
                    } catch (RemoteException e) {
                        System.out.println("Error saving barrels content");
                    }
                }
                break; // so precisamos de um barrel funcional
            }
        }
    }

    @Override
    public String getActiveBarrels() throws RemoteException {
        try {
            return activeBarrelsIDIP.entrySet().stream()
                    .map(entry -> " - " + entry.getKey() + " @ " + entry.getValue())
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "No barrels connected.";
        }
    }

    @Override
    public String getBarrelID() {
        return null;
    }

    @Override
    public List<String> linksListForURL(String url) throws RemoteException {
        synchronized (barrels) {
            for (MetodosRMIBarrel value : barrels) {
                if (value != null) {
                    try {
                        return value.linksListForURL(url);
                    } catch (RemoteException e) {
                        System.out.println("Error searching for links list for URL: " + url + " -> " + e.getMessage());
                    }
                }
                break; // so precisamos de um barrel funcional
            }
        }
        return Collections.emptyList();
    }
}