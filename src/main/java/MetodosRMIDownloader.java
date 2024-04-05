import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The MetodosRMIDownloader interface extends the Remote interface.
 * This interface defines the methods that a Downloader object must implement.
 * These methods allow the Downloader to crawl a URL, check if it is busy, shut down the Downloader, and get the Downloader's ID.
 */
public interface MetodosRMIDownloader extends Remote {

    /**
     * Crawls a URL.
     *
     * @param url       the URL to crawl
     * @param tentativa the attempt number
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    void crawlURL(String url, int tentativa) throws RemoteException;

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

    /**
     * Gets the Downloader's ID.
     *
     * @return the Downloader's ID
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String getDownloaderID() throws RemoteException;
}