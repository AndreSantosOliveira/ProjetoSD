import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Gateway class extends UnicastRemoteObject and implements MetodosRMIGateway and Serializable.
 * This class is responsible for managing the communication between the client and the server.
 * It maintains a list of URLs to be indexed, establishes a connection to the BarrelManager via RMI,
 * and sets up a socket to communicate with the QueueManager.
 */
public class Gateway extends UnicastRemoteObject implements MetodosRMIGateway, Serializable {

    /**
     * Default constructor for Gateway.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    public Gateway() throws RemoteException {
        super();
    }

    // PrintWriter to communicate with the QueueManager
    private static PrintWriter queueManager;

    // MetodosRMIBarrel object to communicate with the BarrelManager
    private static MetodosRMIBarrel metodosBarrelManager = null;

    /**
     * Main method for the Gateway class.
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
                    System.out.println("Exception in QueueManager Socket: " + re);
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
     *
     * @param words the terms to search for
     * @return a list of URLData objects that match the search terms
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    @Override
    public List<URLData> search(String words) throws RemoteException {
        return metodosBarrelManager.searchInput(words);
    }

    /**
     * Lists the indexed pages.
     *
     * @return a list of URLData objects representing the indexed pages
     */
    @Override
    public List<URLData> listIndexedPages() {
        return null;
    }

    @Override
    public void saveBarrelsContent() throws RemoteException {
        metodosBarrelManager.saveBarrelsContent();
    }
}