import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * The MetodosRMIBarrel interface extends the Remote interface.
 * This interface defines the methods that a Barrel object must implement.
 * These methods allow the Barrel to search for URLs based on input, save the content of the barrel,
 * list indexed pages of a specific URL, shut down the barrel, get the barrel's ID, copy the barrel's content to a connection,
 * and receive the barrel's content from a connection.
 */
public interface MetodosRMIBarrel extends Remote {

    /**
     * Searches for URLs based on input.
     *
     * @param url the input to search for
     * @return a Tuple containing the input URL and a list of URLData objects that match the search input
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    Tuple<String, List<URLData>> searchInput(String url) throws RemoteException;

    /**
     * Saves the content of the barrel to a file.
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
     * Returns the barrel's ID.
     *
     * @return the barrel's ID
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String getBarrelID() throws RemoteException;

    /**
     * Copies the barrel's content to a connection.
     *
     * @param c the connection to copy the barrel's content to
     * @return a string representing the outcome of the copy operation
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String copyBarrelContents(Connection c) throws RemoteException;

    /**
     * Receives the barrel's content from a connection.
     *
     * @param barrelID               the ID of the barrel to receive content from
     * @param index                  the index to store the received content
     * @param urlEApontadoresParaURL the map to store the received URLs and their pointers
     * @return a string representing the outcome of the receive operation
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String receiveBarrelContents(String barrelID, HashMap<String, HashSet<URLData>> index, HashMap<String, HashSet<String>> urlEApontadoresParaURL) throws RemoteException;
}