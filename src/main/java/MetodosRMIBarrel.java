import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MetodosRMIBarrel extends Remote {

    void arquivarURL(URLData data) throws RemoteException;

    void arquivarURLs(List<URLData> data) throws RemoteException;


    List<URLData> searchUrl(String url) throws RemoteException;
}
