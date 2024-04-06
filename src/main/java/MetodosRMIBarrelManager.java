/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 1 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * MetodosRMIBarrel interface extends Remote.
 * This interface defines the methods that a Barrel object must implement.
 * These methods allow the Barrel to archive URLs and search for URLs based on input.
 */
public interface MetodosRMIBarrelManager extends Remote {

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
     * Lists indexed pages of a specific.
     *
     * @return a list of URLData objects representing the indexed pages
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    List<String> linksListForURL(String url) throws RemoteException;

    /**
     * Copies Barrel content to another Barrel.
     * @param from barrel id source
     * @param to barrel id destination
     * @return outcome of the operation
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String copyBarrel(String from, String to) throws RemoteException;

}