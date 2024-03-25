import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Gateway extends UnicastRemoteObject implements MetodosRMIGateway, Serializable {
    // Gateway constructor
    public Gateway() throws RemoteException {
        super();
    }

    List<String> toBeIndexed = new ArrayList<>();
    List<String> listaPesquisas = new ArrayList<>();

    private static PrintWriter queueManager;

    public static void main(String[] args) {
        try {
            Gateway gateway = new Gateway();
            LocateRegistry.createRegistry(PortasEIPs.GATEWAY.getPorta()).rebind("gateway", gateway);
        } catch (IOException re) {
            System.out.println("Exception in Gateway RMI: " + re);
        }

        try {
            // Ligar ao QueueManager via TCP
            Socket socket = new Socket(PortasEIPs.QUEUE_MANAGER.getIP(), PortasEIPs.QUEUE_MANAGER.getPorta());
            queueManager = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Ligação ao QueueManager de sucesso! IP: " + PortasEIPs.QUEUE_MANAGER);
        } catch (Exception re) {
            System.out.println("Exception in Gateway Socket: " + re);
        }

        PortasEIPs.GATEWAY.printINIT("Gateway");
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
    public HashSet<URLData> pesquisar(String palavras) throws RemoteException {
        listaPesquisas.add(palavras);
        HashSet<URLData> resultado = new HashSet<>();

        // 10 no maximo
        resultado.add(new URLData("www.google.com", "Google"));
        resultado.add(new URLData("www.facebook.com", "Facebook"));
        resultado.add(new URLData("www.twitter.com", "Twitter"));
        resultado.add(new URLData("www.instagram.com", "Instagram"));
        resultado.add(new URLData("www.linkedin.com", "LinkedIn"));
        resultado.add(new URLData("www.linkedin.com", "LinkedIn"));
        resultado.add(new URLData("www.linkedin.com", "LinkedIn"));
        resultado.add(new URLData("www.linkedin.com", "LinkedIn"));
        resultado.add(new URLData("www.linkedin.com", "LinkedIn"));
        resultado.add(new URLData("www.linkedin.com", "LinkedIn"));

        // TODO: Enviar resultado para o downloader
        //...

        System.out.println("Pesquisa por '" + palavras + "' realizada.");
        return resultado;
    }

}