import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RMIDownloaderMGR extends Remote {

    void receberLinkParaDownloadManager(String url) throws RemoteException;

}