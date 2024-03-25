import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Barrel implements MetodosRMIBarrel {

    static HashMap<String, HashSet<URLData>> index = new HashMap<>();

    public static void main(String[] args) {
        try { // TODO WORK IN PROGRESS
            Barrel barrel = new Barrel();
            MetodosRMIBarrel stub = (MetodosRMIBarrel) UnicastRemoteObject.exportObject(barrel, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Barrel", stub);
            System.out.println("Barrel ready");
        } catch (Exception e) {
            System.err.println("Barrel exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void arquivarURL(URLData data) throws RemoteException {
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
    public void arquivarURLs(List<URLData> data) throws RemoteException {
        data.forEach(urlData -> {
            try {
                arquivarURL(urlData);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public List<URLData> searchUrl(String url) throws RemoteException {
        return null;
    }
}
