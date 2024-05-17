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
    private final Map<Connection, MetodosRMIBarrel> barrels = new HashMap<>();

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
     */
    private void reconnectToBarrel(Connection barrelCon) throws InterruptedException, RemoteException, MalformedURLException {
        System.out.println("Trying to reconnect to Barrel " + barrelCon.getRMIName() + "...");

        try {
            // Invoke the search method on the remote Gateway service
            MetodosRMIGateway metodosGateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");
            metodosGateway.dynamicallyUpdate();
            metodosGateway.getAdministrativeStatistics();
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
                    metodosGateway.getAdministrativeStatistics();
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
                        metodosGateway.getAdministrativeStatistics();

                    } catch (Exception e1) {
                        e1.printStackTrace();
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
            metodosGateway.getAdministrativeStatistics();

        } catch (Exception e) {
            e.printStackTrace();
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

    /**
     * Saves the content of all barrels.
     * <p>
     * This method is used to save the content of all barrels managed by the BarrelManager.
     * It iterates over all barrels and calls the saveBarrelContent method on each barrel.
     * If an Exception is thrown during the saving of a barrel's content, it prints an error message and continues with the next barrel.
     *
     * @throws RemoteException if an error occurs during remote method invocation.
     */
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

    /**
     * Retrieves the active barrels.
     * <p>
     * This method is used to retrieve the active barrels managed by the BarrelManager.
     * It iterates over all barrels and tries to get the ID of each barrel.
     * If a RemoteException is thrown during the retrieval of a barrel's ID, it is ignored and the method continues with the next barrel.
     * It returns a formatted string containing the RMI names and addresses of the active barrels, or a message indicating that there are no active barrels.
     *
     * @return a String representing the active barrels
     * @throws RemoteException if an error occurs during remote method invocation.
     */
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

    /**
     * Retrieves a list of links for a given URL.
     * <p>
     * This method is used to retrieve a list of links for a given URL from the barrels managed by the BarrelManager.
     * It iterates over all barrels until it finds a functional one and calls the linksListForURL method on it.
     * If a RemoteException is thrown during the retrieval of links, it prints an error message and continues with the next barrel.
     * If no functional barrels are found, it returns an empty list.
     *
     * @param url the URL for which to retrieve the list of links
     * @return a List of Strings representing the links for the given URL
     * @throws RemoteException if an error occurs during remote method invocation.
     */
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

    /**
     * Shuts down all barrels.
     * <p>
     * This method is used to shut down all barrels managed by the BarrelManager.
     * It iterates over all barrels and calls the shutdown method on each barrel.
     * If a RemoteException is thrown during the shutdown of a barrel, it is ignored and the method continues with the next barrel.
     * After all barrels have been shut down, it prints a message indicating that the BarrelManager is shutting down.
     *
     * @param motive the reason for shutting down the barrels
     * @throws RemoteException if an error occurs during remote method invocation.
     */
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

    /**
     * Copies the contents of one barrel to another.
     * <p>
     * This method is used to copy the contents of one barrel (identified by the 'from' parameter) to another barrel (identified by the 'to' parameter).
     * It first checks if the 'to' barrel exists and is online. If not, it returns an error message.
     * Then it checks if the 'from' barrel exists and is online. If not, it returns an error message.
     * If both barrels exist and are online, it calls the copyBarrelContents method on the 'from' barrel, passing the 'to' barrel as a parameter.
     *
     * @param from the RMI name of the barrel from which to copy the contents
     * @param to   the RMI name of the barrel to which to copy the contents
     * @return a String message indicating the result of the operation
     * @throws RemoteException if an error occurs during remote method invocation.
     */
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