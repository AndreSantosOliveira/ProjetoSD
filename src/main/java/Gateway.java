import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Gateway extends UnicastRemoteObject implements MetodosRMIGateway, Serializable {
    // Gateway constructor
    public Gateway() throws RemoteException {
        super();
    }

    List<String> toBeIndexed = new ArrayList<>();

    private static PrintWriter queueManager;
    private static MetodosRMIBarrel metodosBarrelManager = null;

    public static void main(String[] args) {
        try {
            Gateway gateway = new Gateway();
            LocateRegistry.createRegistry(PortasEIPs.GATEWAY.getPorta()).rebind("gateway", gateway);
        } catch (IOException re) {
            System.out.println("Exception in Gateway RMI: " + re);
        }

        int retryCount = 0;
        int maxRetries = 5;
        while (metodosBarrelManager == null && retryCount < maxRetries) {
            try {
                metodosBarrelManager = (MetodosRMIBarrel) LocateRegistry.getRegistry(PortasEIPs.BARREL_MANAGER.getPorta()).lookup("barrelmanager");
                System.out.println("Ligado ao BarrelManager!");

                try {
                    // Ligar ao QueueManager via TCP
                    Socket socket = new Socket(PortasEIPs.QUEUE_MANAGER.getIP(), PortasEIPs.QUEUE_MANAGER.getPorta());
                    queueManager = new PrintWriter(socket.getOutputStream(), true);

                    System.out.println("Ligação ao QueueManager de sucesso! IP: " + PortasEIPs.QUEUE_MANAGER);
                } catch (Exception re) {
                    System.out.println("Exception in Gateway Socket: " + re);
                }

                PortasEIPs.GATEWAY.printINIT("Gateway");

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


    // Indexar novo URL
    @Override
    public String indexarURL(String url) {
        toBeIndexed.add(url);

        queueManager.println(url);

        String txt = url + " enviado para o QueueManager.";
        System.out.println(txt);
        return txt;
    }

    // Pesquisar páginas que contenham um conjunto de termos
    @Override
    public List<URLData> pesquisar(String palavras) throws RemoteException {
        return metodosBarrelManager.searchInput(palavras);
    }

    @Override
    public List<URLData> listarPaginasIndexadas() {
        return null;
    }

}