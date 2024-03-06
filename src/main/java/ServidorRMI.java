import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ServidorRMI extends UnicastRemoteObject implements MetodosRMI {
    private static final long serialVersionUID = 1L;

    HashMap<String, HashSet<URLData>> index;
    ArrayList<String> toBeIndexed;
    ArrayList<String> listaPesquisas;

    public ServidorRMI() throws RemoteException {
        super();
    }

    public static void main(String args[]) {
        try {
            ServidorRMI h = new ServidorRMI();
            Registry r = LocateRegistry.createRegistry(1000);
            r.rebind("test", h);
            System.out.println("Server ready.");
        } catch (RemoteException re) {
            System.out.println("Exception in ServidorRMI.main: " + re);
        }
    }

    @Override
    public void indexarURL(String url) throws RemoteException {
        toBeIndexed.add(url);
    }

    @Override
    public HashSet<URLData> pesquisar(String palavras) throws RemoteException {
        listaPesquisas.add(palavras);
        return null;
    }

    @Override
    public String sayHello() throws RemoteException {
        System.out.println("Message received from client!");
        return "Hello!";
    }
}