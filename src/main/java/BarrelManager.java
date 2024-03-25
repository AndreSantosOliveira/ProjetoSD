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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarrelManager implements MetodosRMIBarrel, Serializable {

    private static Map<DescritorIPPorta, MetodosRMIBarrel> barrels = new HashMap<>();
    private static int barrelsON;

    public BarrelManager() throws RemoteException {
        super();
    }

    public static void main(String args[]) throws RemoteException {
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
            System.err.println("Erro ao ler o arquivo de barrels.");
            e.printStackTrace();
        }

        for (DescritorIPPorta descritorIPPorta : barrels.keySet()) {
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

        PortasEIPs.BARREL_MANAGER.printINIT("BarrelManager");

        // Receives multicast from downloader
        receiveResultFromDownloaderviaMulticast();
    }

    private static MetodosRMIBarrel tentarLigarABarrel(DescritorIPPorta descritorIPPorta) {
        MetodosRMIBarrel metodosGateway = null;
        int retryCount = 0;
        int maxRetries = 5;
        while (metodosGateway == null && retryCount < maxRetries) {
            try {
                metodosGateway = (MetodosRMIBarrel) LocateRegistry.getRegistry(5430).lookup("br1");
                System.out.println("Ligado à Barrel " + descritorIPPorta.getRMIName() + "!");
                return metodosGateway;
            } catch (RemoteException | NotBoundException e) {
                ++retryCount;
                if (retryCount < maxRetries) {
                    System.out.println("Failed to connect to Barrel: " + descritorIPPorta.getRMIName() + " (" + retryCount + "/" + maxRetries + "). Retrying...");
                    // Sleep para evitar tentativas de ligação consecutivas
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

                // message is equal to url|title
                String[] parts = message.split("\\|");
                if (parts.length == 2) {
                    String url = parts[0];
                    String title = parts[1];
                    gerirArquivamentoURLs(new URLData(url, title));
                    System.out.println("Sucesso ao mandar arquivar URL: " + url + " com título: " + title);
                } else { //há strings q chegam cortadas..
                    System.err.println("Received invalid message: " + message);
                }
            }

        } catch (IOException e) { //há strings q chegam cortadas..
            // e.printStackTrace();
            //System.out.println("Error receiving multicast message: " + e.getMessage());
        }
    }

    private static void gerirArquivamentoURLs(URLData dados) {
        for (MetodosRMIBarrel value : barrels.values()) {
            if (value != null) {
                try {
                    value.arquivarURL(dados);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void arquivarURL(URLData data) throws RemoteException {
    }

    @Override
    public List<URLData> searchUrl(String url) throws RemoteException {
        List<URLData> dados = new ArrayList<>();
        for (MetodosRMIBarrel value : barrels.values()) {
            if (value != null) {
                try {
                    List<URLData> dadosDownloader = value.searchUrl(url);
                    if (dadosDownloader != null) {
                        dados.addAll(dadosDownloader);
                    }
                } catch (RemoteException e) {
                    System.err.println("Erro ao pesquisar URL no barrel.");
                    e.printStackTrace();
                }
            }
        }
        return dados;
    }
}