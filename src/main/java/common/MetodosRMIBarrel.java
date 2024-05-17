package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * The MetodosRMIBarrel interface extends the Remote interface.
 * This interface defines the methods that a Barrel object must implement.
 * These methods allow the Barrel to archive URLs and search for URLs based on input.
 */
public interface MetodosRMIBarrel extends Remote {

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
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    void saveBarrelContent() throws RemoteException;

    /**
     * Lists indexed pages of a specific URL.
     *
     * @param url the URL to list indexed pages for
     * @return a list of strings representing the indexed pages
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    List<String> linksListForURL(String url) throws RemoteException;

    /**
     * Shuts down the barrel.
     *
     * @param motive the reason for the shutdown
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    void shutdown(String motive) throws RemoteException;

    /**
     * Returns the barrel ID.
     *
     * @return the barrel ID
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String getBarrelID() throws RemoteException;

    /**
     * Copies the barrel's content to another connection.
     *
     * @param c the connection to copy the barrel's content to
     * @return a string representing the outcome of the operation
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String copyBarrelContents(Connection c) throws RemoteException;

    /**
     * Receives the barrel's content from another connection.
     *
     * @param barrelID               the ID of the barrel to receive content from
     * @param index                  the index of the barrel to receive content from
     * @param urlEApontadoresParaURL a map of URLs and the URLs that point to them
     * @return a string representing the outcome of the operation
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String receiveBarrelContents(String barrelID, HashMap<String, HashSet<URLData>> index, HashMap<String, HashSet<String>> urlEApontadoresParaURL) throws RemoteException;
}