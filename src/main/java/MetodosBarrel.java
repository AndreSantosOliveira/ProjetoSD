import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MetodosBarrel extends Remote {

    public String searchUrl(String url) throws RemoteException;
}
