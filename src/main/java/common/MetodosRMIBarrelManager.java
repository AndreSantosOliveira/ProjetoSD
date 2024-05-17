package common;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * The MetodosRMIBarrelManager interface extends the Remote interface.
 * This interface defines the methods that a BarrelManager object must implement.
 * These methods allow the BarrelManager to archive URLs, search for URLs based on input, save the content of the barrels to a file,
 * get the active barrels, shut down the barrels, list indexed pages of a specific URL, and copy barrel content to another barrel.
 */
public interface MetodosRMIBarrelManager extends Remote {

    /**
     * Searches for URLs based on input.
     *
     * @param url the input to search for
     * @return a tuple containing the original URL and a list of URLData objects that match the search input
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    Tuple<String, List<URLData>> searchInput(String url) throws RemoteException;

    /**
     * Saves the content of the barrels to a file.
     *
     * @throws RemoteException       if an error occurs during remote method invocation.
     * @throws MalformedURLException if the specified URL is not formatted correctly.
     * @throws NotBoundException     if an attempt is made to lookup or unbind in the registry a name that has no associated binding.
     */
    void saveBarrelsContent() throws RemoteException, MalformedURLException, NotBoundException;

    /**
     * Gets the active barrels.
     *
     * @return a string representing the active barrels
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String getActiveBarrels() throws RemoteException;

    /**
     * Shuts down the barrels.
     *
     * @param motive the reason for the shutdown
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    void shutdownBarrels(String motive) throws RemoteException;

    /**
     * Lists indexed pages of a specific URL.
     *
     * @param url the URL to list indexed pages for
     * @return a list of strings representing the indexed pages
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    List<String> linksListForURL(String url) throws RemoteException;

    /**
     * Copies Barrel content to another Barrel.
     *
     * @param from the ID of the source barrel
     * @param to   the ID of the destination barrel
     * @return a string representing the outcome of the operation
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String copyBarrel(String from, String to) throws RemoteException;

}