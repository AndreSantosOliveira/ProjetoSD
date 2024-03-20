import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Gateway extends UnicastRemoteObject implements MetodosGateway, Serializable {
    // Gateway constructor
    public Gateway() throws RemoteException {
        super();
    }

    HashMap<String, HashSet<URLData>> index;
    List<String> toBeIndexed = new ArrayList<>();
    List<String> listaPesquisas = new ArrayList<>();

    // Main
    public static void main(String[] args) {
        try {
            Gateway gateway = new Gateway();
            LocateRegistry.createRegistry(2000).rebind("Gateway", gateway);
            System.out.println("Gateway ready.");
        } catch (RemoteException re) {
            System.out.println("Exception in Gateway.main: " + re);
        }
    }


    // Indexar novo URL
    @Override
    public String indexarURL(String url) throws RemoteException {
        toBeIndexed.add(url);
        // TODO: Enviar URL para o downloader
        //...
        System.out.println(url + " adicionado à lista de indexação.");
        return url + " adicionado à lista de indexação.";
    }

    // Pesquisar páginas que contenham um conjunto de termos
    @Override
    public HashSet<URLData> pesquisar(String palavras) throws RemoteException {
        listaPesquisas.add(palavras);
        HashSet<URLData> resultado = new HashSet<>();

        resultado.add(new URLData("www.google.com", "Google"));
        resultado.add(new URLData("www.facebook.com", "Facebook"));
        resultado.add(new URLData("www.twitter.com", "Twitter"));
        resultado.add(new URLData("www.instagram.com", "Instagram"));
        resultado.add(new URLData("www.linkedin.com", "LinkedIn"));

        // TODO: Enviar resultado para o downloader
        //...

        System.out.println("Pesquisa por '" + palavras + "' realizada.");
        return resultado;
    }

}