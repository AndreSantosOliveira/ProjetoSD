/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 1 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * MetodosRMIBarrel interface extends Remote.
 * This interface defines the methods that a Barrel object must implement.
 * These methods allow the Barrel to archive URLs and search for URLs based on input.
 */
public interface MetodosRMIBarrel extends Remote {

    /**
     * Searches for URLs based on input.
     *
     * @param url the input to search for
     * @return a list of URLData objects that match the search input
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
     * Lists indexed pages of a specific.
     *
     * @return a list of URLData objects representing the indexed pages
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
     */
    String getBarrelID() throws RemoteException;

    /**
     * Copy barrel's content to connection
     *
     * @return outcome of the operation
     */
    String copyBarrelContents(Connection c) throws RemoteException;

    /**
     * Receive barrel's content from connection
     *
     * @return outcome of the operation
     */
    String receiveBarrelContents(String barrelID, HashMap<String, HashSet<URLData>> index, HashMap<String, HashSet<String>> urlEApontadoresParaURL) throws RemoteException;
}