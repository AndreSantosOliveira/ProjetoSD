import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * MetodosRMIDownloader interface extends Remote.
 * This interface defines the methods that a Downloader object must implement.
 * These methods allow the Downloader to crawl a URL and check if it is busy.
 */
public interface MetodosRMIDownloader extends Remote {

    /**
     * Crawls a URL.
     *
     * @param url the URL to crawl
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    void crawlURL(String url) throws RemoteException;

    /**
     * Checks if the Downloader is busy.
     *
     * @return a boolean indicating whether the Downloader is busy
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    boolean isBusy() throws RemoteException;

    /**
     * Shuts down the Downloader.
     *
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    void shutdown() throws RemoteException;

    String getDownloaderID() throws RemoteException;
}