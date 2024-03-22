import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MetodosRMIBarrel extends Remote {

    public String searchUrl(String url) throws RemoteException;
}
