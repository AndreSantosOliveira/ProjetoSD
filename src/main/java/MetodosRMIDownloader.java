/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 1 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

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
     * Gets the downloder ID.
     *
     * @return downloader id
     */
    String getDownloaderID() throws RemoteException;
}