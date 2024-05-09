/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import common.Connection;
import common.ConnectionsEnum;
import common.MetodosRMIBarrel;
import common.MetodosRMIBarrelManager;
import common.MetodosRMIGateway;
import common.Tuple;
import common.URLData;

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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * BarrelManager class implements common.MetodosRMIBarrel and Serializable.
 * This class is responsible for managing a collection of barrels.
 */
public class BarrelManager implements MetodosRMIBarrelManager, Serializable {

    // Map to store barrels
    private Map<Connection, MetodosRMIBarrel> barrels = new HashMap<>();

    static BarrelManager barrelManager;

    /**
     * Default constructor for BarrelManager.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    public BarrelManager() throws RemoteException {
        super();
        connectToBarrels();
    }

    boolean killSwitch = false;

    /**
     * Connects to the barrels.
     */
    private void connectToBarrels() {
        // Load barrels from the text file barrels.txt (IP, port, rmiName)
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/barrels.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 4) {
                    try {
                        String ip = parts[0];
                        int porta = Integer.parseInt(parts[1]);
                        String rmiName = parts[2];
                        String externalIP = parts[3];
                        Connection descritor = new Connection(ip, porta, rmiName, externalIP);
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
            System.err.println("Error reading the barrel file: " + e.getMessage());
        }
    }

    /**
     * Main method for the BarrelManager class.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) throws RemoteException {
        try {
            barrelManager = new BarrelManager();
            LocateRegistry.createRegistry(ConnectionsEnum.BARREL_MANAGER.getPort()).rebind("barrelmanager", barrelManager);

            for (Connection connection : barrelManager.barrels.keySet()) {
                // Make individual heartbeat system for each barrel in separate threads
                new Thread(() -> {
                    if (barrelManager.killSwitch) {
                        System.exit(0);
                    }

                    try {
                        barrelManager.heartbeat(connection);
                    } catch (RemoteException | InterruptedException | MalformedURLException | NotBoundException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }

            while (true) {
                MetodosRMIGateway gateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");
                gateway.heartBeat();
            }

        } catch (Exception e) {
            System.exit(0);
        }
    }

    /**
     * Heartbeat system for the barrels.
     *
     * @param barrelCon common.Connection object of the barrel
     * @throws RemoteException       if an error occurs during remote method invocation.
     * @throws InterruptedException  if an error occurs during thread sleep.
     * @throws MalformedURLException if an error occurs during URL creation.
     * @throws NotBoundException     if an error occurs during RMI binding.
     */
    private void heartbeat(Connection barrelCon) throws RemoteException, InterruptedException, MalformedURLException, NotBoundException {
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

    /**
     * Reconnects to a barrel.
     *
     * @param barrelCon common.Connection object of the barrel
     * @throws InterruptedException  if an error occurs during thread sleep.
     * @throws RemoteException       if an error occurs during remote method invocation.
     * @throws MalformedURLException if an error occurs during URL creation.
     * @throws NotBoundException     if an error occurs during RMI binding.
     */
    private void reconnectToBarrel(Connection barrelCon) throws InterruptedException, RemoteException, MalformedURLException, NotBoundException {
        System.out.println("Trying to reconnect to Barrel " + barrelCon.getRMIName() + "...");

        try {
            // Invoke the search method on the remote Gateway service
            MetodosRMIGateway metodosGateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");
            metodosGateway.dynamicallyUpdate();
        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace();
        }

        while (true) {
            Thread.sleep(5000);
            MetodosRMIBarrel barrel = tentarLigarABarrel(barrelCon, false);
            if (barrel != null) {
                System.out.println("Reconnected to Barrel " + barrelCon.getRMIName() + "!");
                //barrels.put(barrelCon, barrel);
                //find a barrel with different barrelCon

                try {
                    // Invoke the search method on the remote Gateway service
                    MetodosRMIGateway metodosGateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");
                    metodosGateway.dynamicallyUpdate();
                } catch (Exception e) {
                    // Handle any exceptions
                    e.printStackTrace();
                }

                for (Connection connection : barrels.keySet()) {
                    if (!connection.equals(barrelCon)) {

                        try {
                            MetodosRMIBarrel res = (MetodosRMIBarrel) Naming.lookup("rmi://" + connection.getIP() + ":" + connection.getPorta() + "/" + connection.getRMIName());
                            if (res != null) res.copyBarrelContents(barrelCon);
                        } catch (RemoteException | NotBoundException e) {
                            continue; // found another barrel different from itself, but the barrel wasn't on, so it can't copy
                        }
                        break;
                    }
                }
                break;
            }
        }
    }


    /**
     * Attempts to connect to a barrel.
     *
     * @param descritorIPPorta descriptor of the barrel to connect to
     * @return common.MetodosRMIBarrel object if the connection is successful, null otherwise.
     */
    private static MetodosRMIBarrel tentarLigarABarrel(Connection descritorIPPorta, boolean retrySystemOff) {
        MetodosRMIBarrel metodosBarrel;
        int retryCount = 0;
        int maxRetries = retrySystemOff ? 5 : 1;

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
                        // Invoke the search method on the remote Gateway service
                        MetodosRMIGateway metodosGateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");
                        metodosGateway.dynamicallyUpdate();
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }

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

        try {
            // Invoke the search method on the remote Gateway service
            MetodosRMIGateway metodosGateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");
            metodosGateway.dynamicallyUpdate();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    // Counter to keep track of the barrel index
    static AtomicInteger barrelCounter = new AtomicInteger();

    /**
     * Searches for common.URLData objects.
     *
     * @param pesquisa String of words to search for
     * @return List of common.URLData objects that match the search criteria
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    @Override
    public Tuple<String, List<URLData>> searchInput(String pesquisa) throws RemoteException {
        if (Objects.equals(getActiveBarrels(), "\nNone.\n"))
            return new Tuple<>("none", Collections.singletonList(new URLData("Please wait...", "Trying to reconnect to the barrels...", -1)));

        String id = "none";
        Map<String, String> urlTitulo = new HashMap<>();
        Map<String, Integer> relevace = new HashMap<>();
        synchronized (barrels) {

            if (barrelCounter.get() >= barrels.size()) {
                barrelCounter.set(0);
            }

            try {

                // Get barrel at the current index from counter
                Connection connection = (Connection) barrels.keySet().toArray()[barrelCounter.get()];
                MetodosRMIBarrel barrel = (MetodosRMIBarrel) Naming.lookup("rmi://" + connection.getIP() + ":" + connection.getPorta() + "/" + connection.getRMIName());

                System.out.println("Searching in barrel " + barrel.getBarrelID() + "...");
                Tuple<String, List<URLData>> dadosBarrel = barrel.searchInput(pesquisa);
                if (dadosBarrel != null) {
                    id = dadosBarrel.getFirst();
                    for (URLData urlData : dadosBarrel.getSecond()) {
                        if (!urlTitulo.containsKey(urlData.getURL())) {
                            urlTitulo.put(urlData.getURL(), urlData.getPageTitle());
                            relevace.put(urlData.getURL(), urlData.getRelevance());
                        }
                    }
                    barrelCounter.incrementAndGet();
                    return new Tuple<>(id, urlTitulo.entrySet().stream().map(entry -> new URLData(entry.getKey(), entry.getValue(), relevace.get(entry.getKey()))).collect(Collectors.toList()));
                }
            } catch (MalformedURLException | NotBoundException | RemoteException e) {
                // Barrel is offline, return with the next one
                barrelCounter.incrementAndGet();
                return searchInput(pesquisa);
            }

            return new Tuple<>(id, Collections.singletonList(new URLData("Please wait...", "Trying to reconnect to the barrels...", -1)));
        }
    }

    @Override
    public void saveBarrelsContent() throws RemoteException {
        synchronized (barrels) {
            for (Connection connection : barrels.keySet()) {
                try {
                    MetodosRMIBarrel res = (MetodosRMIBarrel) Naming.lookup("rmi://" + connection.getIP() + ":" + connection.getPorta() + "/" + connection.getRMIName());
                    res.saveBarrelContent();
                } catch (Exception e) {
                    System.out.println("Error saving barrels content. Barrel is offline.");
                }
            }
        }
    }

    @Override
    public String getActiveBarrels() throws RemoteException {
        // Count the number of active barrels
        int ctr = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        synchronized (barrels) {
            for (Connection connection : barrels.keySet()) {
                try {
                    MetodosRMIBarrel res = (MetodosRMIBarrel) Naming.lookup("rmi://" + connection.getIP() + ":" + connection.getPorta() + "/" + connection.getRMIName());
                    res.getBarrelID();
                    ++ctr;
                    sb.append("- ").append(connection.getRMIName()).append(" @ ").append(connection.getIP()).append(":").append(connection.getPorta()).append("\n");
                } catch (MalformedURLException | NotBoundException | RemoteException ignored) {
                }
            }
        }
        return ctr == 0 ? "\nNone.\n" : sb.toString();
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
    public void shutdownBarrels(String motive) throws RemoteException {
        synchronized (barrels) {
            for (MetodosRMIBarrel value : barrels.values()) {
                if (value != null) {
                    try {
                        value.shutdown(motive);
                    } catch (RemoteException ignored) {
                    }
                }
            }
        }
        System.out.println("Shutting down BarrelManager: " + motive);
    }

    /**
     * Gets the barrel connection for the given rmiName
     *
     * @param rmiName RMI name of the barrel
     * @return common.Connection of the barrel
     */
    public Connection getBarrelConnection(String rmiName) {
        synchronized (barrels) {
            for (Connection connection : barrels.keySet()) {
                if (connection.getRMIName().equalsIgnoreCase(rmiName)) {
                    return connection;
                }
            }
        }
        return null;
    }

    @Override
    public String copyBarrel(String from, String to) throws RemoteException {
        // get the barrels
        Connection toBarrel = getBarrelConnection(to);

        if (toBarrel == null) return "Barrel to: " + to + " not found.";
        try {
            MetodosRMIBarrel res = (MetodosRMIBarrel) Naming.lookup("rmi://" + toBarrel.getIP() + ":" + toBarrel.getPorta() + "/" + toBarrel.getRMIName());
            res.getBarrelID();

        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            return "Barrel to: " + to + " is offline.";
        }

        Connection fromBarrel = getBarrelConnection(from);

        if (fromBarrel == toBarrel) return "Barrel from and to can't be the same.";

        if (fromBarrel == null) return "Barrel from: " + from + " not found.";
        try {
            MetodosRMIBarrel res = (MetodosRMIBarrel) Naming.lookup("rmi://" + fromBarrel.getIP() + ":" + fromBarrel.getPorta() + "/" + fromBarrel.getRMIName());
            return res.copyBarrelContents(toBarrel);
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            return "Barrel from: " + from + " is offline.";
        }
    }
}