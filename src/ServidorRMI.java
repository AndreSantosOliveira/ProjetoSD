import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ServidorRMI extends UnicastRemoteObject implements MetodosRMI {
    private static final long serialVersionUID = 1L;

    HashMap<String, HashSet<URLRecord>> index;
    ArrayList<String> toBeIndexed;
    ArrayList<String> listaPesquisas;

    public ServidorRMI() throws RemoteException {
        super();
    }

    public static void main(String args[]) {
        try {
            ServidorRMI h = new ServidorRMI();
            Registry r = LocateRegistry.createRegistry(1000);
            r.rebind("benfica", h);
            System.out.println("Hello Server ready.");
        } catch (RemoteException re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
    }

    @Override
    public void indexarURL(String url) throws RemoteException {
        toBeIndexed.add(url);
    }

    @Override
    public HashSet<URLRecord> pesquisar(String palavras) throws RemoteException {
        listaPesquisas.add(palavras);
        return null;
    }

    @Override
    public String sayHello() throws RemoteException {
        return null;
    }
}