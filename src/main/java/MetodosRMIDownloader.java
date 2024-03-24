import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MetodosRMIDownloader extends Remote {

    String crawlURL(String url) throws RemoteException;

    boolean isBusy() throws RemoteException;
}
