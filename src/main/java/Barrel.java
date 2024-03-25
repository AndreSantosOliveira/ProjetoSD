import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Barrel extends UnicastRemoteObject implements MetodosRMIBarrel, Serializable {

    static HashMap<String, HashSet<URLData>> index = new HashMap<>();

    protected Barrel() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("Barrel <PORTA> <ID>");
                System.exit(1);
            }
            int porta = Integer.parseInt(args[0]);
            String dlID = args[1];

            Barrel barr = new Barrel();
            LocateRegistry.createRegistry(porta).rebind(dlID, barr);

            System.out.println("Barrel " + dlID + " ready: 127.0.0.1:" + porta);

            while (true) {
            }
        } catch (IOException re) {
            System.out.println("Exception in Gateway RMI: " + re);
        }
    }


    @Override
    public void arquivarURL(URLData data) throws RemoteException {
        System.out.println("Recieved " + data + " to index :D");

        for (String palavra : data.getPageTitle().split(" ")) {
            if (index.containsKey(palavra)) {
                index.get(palavra).add(data);
            } else {
                HashSet<URLData> urls = new HashSet<>();
                urls.add(data);
                index.put(palavra, urls);
            }
        }
    }

    @Override
    public List<URLData> searchUrl(String url) throws RemoteException {
        return null;
    }
}
