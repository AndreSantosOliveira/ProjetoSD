import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Gateway class extends UnicastRemoteObject and implements MetodosRMIGateway and Serializable.
 * This class is responsible for managing the communication between the client and the server.
 * It maintains a list of URLs to be indexed, establishes a connection to the BarrelManager via RMI,
 * and sets up a socket to communicate with the QueueManager.
 */
public class Gateway extends UnicastRemoteObject implements MetodosRMIGateway, Serializable {

    // Map to store the response time of each barrel
    Map<String, Double> barrelResponseTime = new HashMap<>();

    // Map to store the number of requests to each barrel
    Map<String, Integer> barrelRequestCount = new HashMap<>();

    /**
     * Default constructor for Gateway.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    public Gateway() throws RemoteException {
        super();
    }

    // PrintWriter to communicate with the QueueManager
    static PrintWriter queueManager;

    // MetodosRMIBarrel object to communicate with the BarrelManager
    static MetodosRMIBarrel metodosBarrelManager = null;

    /**
     * Main method for the Gateway class.
     * It creates a new Gateway object and binds it to the RMI registry.
     * It also establishes a connection to the BarrelManager via RMI,
     * and sets up a socket to communicate with the QueueManager.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            Gateway gateway = new Gateway();
            LocateRegistry.createRegistry(ConnectionsEnum.GATEWAY.getPort()).rebind("gateway", gateway);
        } catch (IOException re) {
            System.out.println("Exception in Gateway RMI: " + re);
        }

        int retryCount = 0;
        int maxRetries = 10;
        while (metodosBarrelManager == null && retryCount < maxRetries) {
            try {
                metodosBarrelManager = (MetodosRMIBarrel) LocateRegistry.getRegistry(ConnectionsEnum.BARREL_MANAGER.getPort()).lookup("barrelmanager");
                System.out.println("Connected to BarrelManager!");

                try {
                    // Ligar ao QueueManager via TCP
                    Socket socket = new Socket(ConnectionsEnum.QUEUE_MANAGER.getIP(), ConnectionsEnum.QUEUE_MANAGER.getPort());
                    queueManager = new PrintWriter(socket.getOutputStream(), true);

                    System.out.println("Sucessfull Connection to QueueManager! IP: " + ConnectionsEnum.QUEUE_MANAGER);
                } catch (Exception re) {
                    System.out.println("Could not connect to QueueManager: " + re);
                    //System.exit(1);
                }

                ConnectionsEnum.GATEWAY.printINIT("Gateway");

            } catch (RemoteException | NotBoundException e) {
                ++retryCount;
                if (retryCount < maxRetries) {
                    System.out.println("Failed to connect to BarrelManager (" + retryCount + "/" + maxRetries + "). Retrying...");
                    // Sleep para evitar tentativas de ligação consecutivas
                    try {
                        Thread.sleep(1001);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    /**
     * Indexes a new URL.
     * It adds the URL to the list of URLs to be indexed and sends it to the QueueManager.
     *
     * @param url the URL to index
     * @return a string indicating the success of the operation
     */
    @Override
    public String indexURLString(String url) {
        queueManager.println(url);

        String txt = url + " enviado para o QueueManager.";
        System.out.println(txt);
        return txt;
    }

    /**
     * Searches for pages that contain a set of terms.
     * It sends the search terms to the BarrelManager and returns the results.
     *
     * @param words the terms to search for
     * @return a list of URLData objects that match the search terms
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    @Override
    public List<URLData> search(String words) throws RemoteException {
        addSearch(words);
        //measure time in miliseconds
        long startTime = System.currentTimeMillis();
        Tuple<String, List<URLData>> res = metodosBarrelManager.searchInput(words);
        //sort the list of URLData
        List<URLData> lista = res.getSecond();
        //check if any urldata has relevance bigger than 0
        //if so, sort the list by relevance
        if (lista.stream().anyMatch(urlData -> urlData.getRelevance() > 0)) {
            lista.sort(Comparator.comparingInt(URLData::getRelevance).reversed());
        }

        long endTime = System.currentTimeMillis();

        String barrelId = res.getFirst();

        // Update the response time
        if (barrelResponseTime.containsKey(barrelId)) {
            barrelResponseTime.put(barrelId, barrelResponseTime.get(res.getFirst()) + (endTime - startTime));
        } else {
            barrelResponseTime.put(barrelId, (double) (endTime - startTime));
        }

        // Update the request count
        if (barrelRequestCount.containsKey(barrelId)) {
            barrelRequestCount.put(barrelId, barrelRequestCount.get(barrelId) + 1);
        } else {
            barrelRequestCount.put(barrelId, 1);
        }

        return lista;
    }

    @Override
    public void saveBarrelsContent() throws RemoteException {
        metodosBarrelManager.saveBarrelsContent();
    }

    // PARTE DE ESTATÍSTICAS DE ADMINISTRAÇÃO

    final Map<String, Integer> top10Searches = new HashMap<>();

    @Override
    public String getAdministrativeStatistics() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        sb.append("\nTop 10 searches:\n");
        if (top10Searches.isEmpty()) {
            sb.append("No searches yet.\n");
        } else {
            top10Searches.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));
        }
        if (metodosBarrelManager == null) {
            sb.append("BarrelManager is not connected. Something went wrong.");
        } else {
            sb.append("\nActive Barrels:\n").append(metodosBarrelManager.getActiveBarrels()).append("\n");
            sb.append("\nAverage Barrel Response Time:\n");
            if (barrelResponseTime.isEmpty()) {
                sb.append("No response times recorded yet.");
            } else
                barrelResponseTime.forEach((key, value) -> sb.append(" - ").append(key).append(" -> ").append((value / barrelRequestCount.get(key)) / 1000).append("s\n"));
        }
        sb.append("\n");

        return sb.toString();
    }

    @Override
    public List<String> linksListForURL(String url) throws RemoteException {
        return metodosBarrelManager.linksListForURL(url);
    }

    @Override
    public void shutdown(String motive) throws RemoteException {
        queueManager.println("shutdown");
        System.out.println(motive + ". Shutting down.");
        try {
            metodosBarrelManager.shutdown(motive);
        } catch (RemoteException ignored) {
        }
        System.exit(0);
    }

    @Override
    public int autenticarCliente(String username, String password) {
        String line;
        int authResult = -1; // Default result if user is not found

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/accounts.txt"))) {
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    String storedUsername = parts[0];
                    String storedPassword = parts[1];
                    int storedValue = Integer.parseInt(parts[2]);

                    if (storedUsername.equalsIgnoreCase(username) && storedPassword.equalsIgnoreCase(password)) {
                        authResult = storedValue;
                        break; // Exit loop once user is found
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return authResult;
    }

    public void addSearch(String search) {
        int count = top10Searches.getOrDefault(search, 0);
        top10Searches.put(search, count + 1);
        if (top10Searches.size() > 10) {
            // Remove the least common search if more than 10 searches are stored
            String leastCommon = top10Searches.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .orElseThrow()
                    .getKey();
            top10Searches.remove(leastCommon);
        }
    }
}