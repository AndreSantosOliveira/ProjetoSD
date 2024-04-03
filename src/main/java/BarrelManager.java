import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
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
    private Map<Connection, MetodosRMIBarrel> barrels = new HashMap<>();

    /**
     * Default constructor for BarrelManager.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    public BarrelManager() throws RemoteException {
        super();
        connectToBarrels();
    }

    private void connectToBarrels() {

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
                        Connection descritor = new Connection(ip, porta, rmiName);
                        MetodosRMIBarrel res = tentarLigarABarrel(descritor, true);
                        barrels.put(descritor, res);
                    } catch (NumberFormatException e) {
                        System.err.println("Error processing the port for a barrel: " + line);
                    }
                } else {
                    System.err.println("Line in invalid format: " + line);
                }
            }

            if (this.barrels.isEmpty()) {
                System.err.println("No barrel has been connected. Shutting down...");
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
    public static void main(String[] args) throws RemoteException {
        try {
            BarrelManager barrelManager = new BarrelManager();
            LocateRegistry.createRegistry(ConnectionsEnum.BARREL_MANAGER.getPort()).rebind("barrelmanager", barrelManager);

            for (Connection connection : barrelManager.barrels.keySet()) {
                // Make individual heartbeat system for each barrel in separate threads
                new Thread(() -> {
                    try {
                        barrelManager.heartbeat(connection);
                    } catch (RemoteException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }

        } catch (IOException re) {
            System.out.println("Exception in BarrelManager: " + re);
        }
    }

    private void heartbeat(Connection barrelCon) throws RemoteException, InterruptedException {
        while (true) {
            Thread.sleep(5000);
            MetodosRMIBarrel barrel = tentarLigarABarrel(barrelCon, false);
            if (barrel != null) {
                System.out.println("Barrel " + barrelCon.getRMIName() + " is alive.");
                //barrels.put(barrelCon, barrel);
            } else {
                System.out.println("Barrel " + barrelCon.getRMIName() + " is offline.");
                //barrels.remove(barrelCon);
                reconnectToBarrel(barrelCon);
            }
        }
    }

    private void reconnectToBarrel(Connection barrelCon) throws InterruptedException {
        System.out.println("Trying to reconnect to Barrel " + barrelCon.getRMIName() + "...");
        while (true) {
            Thread.sleep(5000);
            MetodosRMIBarrel barrel = tentarLigarABarrel(barrelCon, false);
            if (barrel != null) {
                System.out.println("Reconnected to Barrel " + barrelCon.getRMIName() + "!");
                //barrels.put(barrelCon, barrel);
                break;
            }
        }
    }


    /**
     * Attempts to connect to a barrel.
     *
     * @param descritorIPPorta descriptor of the barrel to connect to
     * @return MetodosRMIBarrel object if the connection is successful, null otherwise.
     */
    private static MetodosRMIBarrel tentarLigarABarrel(Connection descritorIPPorta, boolean retrySystemOff) {
        MetodosRMIBarrel metodosBarrel = null;
        int retryCount = 0;
        int maxRetries = 5;
        while (retryCount < maxRetries) {
            try {
                metodosBarrel = (MetodosRMIBarrel) Naming.lookup("rmi://" + descritorIPPorta.getIP() + ":" + descritorIPPorta.getPorta() + "/" + descritorIPPorta.getRMIName());
                if (retrySystemOff) System.out.println("Connected to Barrel " + descritorIPPorta.getRMIName() + "!");
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
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
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
    //TODO: Distribuir carga entre barrels.
    public Tuple<String, List<URLData>> searchInput(String pesquisa) throws RemoteException {
        boolean error = true;
        String id = "none";
        Map<String, String> urlTitulo = new HashMap<>();
        Map<String, Integer> relevace = new HashMap<>();
        synchronized (barrels) {
            for (MetodosRMIBarrel barrel : barrels.values()) {
                if (barrel != null) {
                    try {
                        Tuple<String, List<URLData>> dadosBarrel = barrel.searchInput(pesquisa);
                        if (dadosBarrel != null) {
                            id = dadosBarrel.getFirst();
                            for (URLData urlData : dadosBarrel.getSecond()) {
                                if (!urlTitulo.containsKey(urlData.getURL())) {
                                    urlTitulo.put(urlData.getURL(), urlData.getPageTitle());
                                    relevace.put(urlData.getURL(), urlData.getRelevance());
                                }
                            }
                            error = false;
                        }
                    } catch (RemoteException e) {
                        // vamos tentar o próximo barrel
                        continue;
                    }
                    break; // so precisamos de um barrel funcional
                }
            }

            if (error) {
                return new Tuple<>(id, Collections.singletonList(new URLData("Please wait...", "Trying to reconnect to the barrels..", 0)));
            } else {
                return new Tuple<>(id, urlTitulo.entrySet().stream().map(entry -> new URLData(entry.getKey(), entry.getValue(), relevace.get(entry.getKey()))).collect(Collectors.toList()));
            }
        }
    }

    @Override
    public void saveBarrelsContent() throws RemoteException {
        synchronized (barrels) {
            for (MetodosRMIBarrel value : barrels.values()) {
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
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        synchronized (barrels) {
            for (Connection connection : barrels.keySet()) {
                try {
                    MetodosRMIBarrel res = (MetodosRMIBarrel) Naming.lookup("rmi://" + connection.getIP() + ":" + connection.getPorta() + "/" + connection.getRMIName());
                    res.getBarrelID();
                    sb.append("- ").append(connection.getRMIName()).append(" - ").append(connection.getIP()).append(":").append(connection.getPorta()).append("\n");
                } catch (MalformedURLException | NotBoundException | RemoteException e) {
                    continue;
                }
            }
        }
        return sb.toString();
    }

    @Override
    public List<String> linksListForURL(String url) throws RemoteException {
        synchronized (barrels) {
            for (MetodosRMIBarrel value : barrels.values()) {
                if (value != null) {
                    try {
                        return value.linksListForURL(url);
                    } catch (RemoteException e) {
                        System.out.println("Error searching for links list for URL: " + url + " -> " + e.getMessage());
                    }
                }
                break; // we only need one functional barrels
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void shutdown(String motive) throws RemoteException {
        synchronized (barrels) {
            for (MetodosRMIBarrel value : barrels.values()) {
                if (value != null) {
                    try {
                        value.shutdown(motive);
                    } catch (RemoteException e) {
                        continue;
                    }
                }
            }
        }
        System.exit(0);
    }

    // Implement the remaining methods from the MetodosRMIBarrel interface   ----------- DUMMY METHODS ------------
    @Override
    public String getBarrelID() throws RemoteException {
        return null;
    }
}