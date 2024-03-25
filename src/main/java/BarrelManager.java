import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarrelManager implements MetodosRMIBarrel, Serializable {

    private static Map<DescritorIPPorta, MetodosRMIDownloader> barrels = new HashMap<>();

    public BarrelManager() throws RemoteException {
        super();
    }

    public static void main(String args[]) {
        // Carregar barrels do ficheiro de texto barrels.txt (IP, porta, rmiName)
        try (BufferedReader br = new BufferedReader(new FileReader("/Users/joserod/IdeaProjects/ProjetoSD/src/main/java/barrels.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    try {
                        String ip = parts[0];
                        int porta = Integer.parseInt(parts[1]);
                        String rmiName = parts[2];

                        barrels.put(new DescritorIPPorta(ip, porta, rmiName), null);
                        System.out.println("Barrel adicionada: " + rmiName + " (" + ip + ":" + porta + ")");
                    } catch (NumberFormatException e) {
                        System.err.println("Erro ao processar a porta para uma barrel: " + line);
                    }
                } else {
                    System.err.println("Linha em formato inválido: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo de downloaders.");
            e.printStackTrace();
        }

        for (DescritorIPPorta descritorIPPorta : barrels.keySet()) {
            MetodosRMIDownloader res = tentarLigarABarrel(descritorIPPorta);
            if (res != null) {
                barrels.put(descritorIPPorta, res);
            }
        }

        PortasEIPs.BARREL_MANAGER.printINIT("BarrelManager");

        // Receives multicast from downloader
        receiveResultFromDownloaderviaMulticast();
    }

    private static MetodosRMIDownloader tentarLigarABarrel(DescritorIPPorta descritorIPPorta) {
        MetodosRMIDownloader metodosGateway = null;
        int retryCount = 0;
        int maxRetries = 5;
        while (metodosGateway == null && retryCount < maxRetries) {
            try {
                metodosGateway = (MetodosRMIDownloader) LocateRegistry.getRegistry(descritorIPPorta.getPorta()).lookup(descritorIPPorta.getRMIName());
                System.out.println("Ligado ao Downloader " + descritorIPPorta.getRMIName() + "!");
                return metodosGateway;
            } catch (RemoteException | NotBoundException e) {
                ++retryCount;
                if (retryCount < maxRetries) {
                    System.out.println("Failed to connect to Downloader: " + descritorIPPorta.getRMIName() + ". Retrying...");
                    // Sleep para evitar tentativas de ligação consecutivas
                    try {
                        Thread.sleep(1001);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        System.out.println("Failed to connect to Downloader: " + descritorIPPorta.getRMIName() + ". :(");
        return null;
    }

    // Receives multicast from downloader
    public static void receiveResultFromDownloaderviaMulticast() {
        // Receives multicast from downloader
        try {
            // Create a multicast socket
            MulticastSocket multicastSocket = new MulticastSocket(PortasEIPs.MULTICAST.getPorta());
            multicastSocket.joinGroup(InetAddress.getByName(PortasEIPs.MULTICAST.getIP()));

            byte[] buffer = new byte[1024];

            // Receive the multicast packet
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                // Convert the packet data to string and process it
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received multicast message: " + message);
                // Process the message here as needed
            }

        } catch (IOException e) {
            System.out.println("Error receiving multicast message: " + e.getMessage());
        }
    }

    @Override
    public List<URLData> searchUrl(String url) throws RemoteException {
        return null;
    }
}