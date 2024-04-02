import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * MetodosRMIGateway interface extends Remote.
 * This interface defines the methods that a Gateway object must implement.
 * These methods allow the Gateway to index a URL, search for URLs based on input, and list indexed pages.
 */
public interface MetodosRMIGateway extends Remote {

    /**
     * Indexes a URL.
     *
     * @param url the URL to index
     * @return a string indicating the success of the operation
     * @throws IOException if an error occurs during the operation.
     */
    String indexURLString(String url) throws IOException;

    /**
     * Searches for URLs based on input.
     *
     * @param words the input to search for
     * @return a list of URLData objects that match the search input
     * @throws java.rmi.RemoteException if an error occurs during remote method invocation.
     */
    List<URLData> search(String words) throws java.rmi.RemoteException;

    /**
     * Saves the content of the barrels.
     */
    void saveBarrelsContent() throws RemoteException;

    /**
     * Gets the administrative statistics.
     *
     * @return a string containing the administrative statistics
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String getAdministrativeStatistics() throws RemoteException;

    /**
     * Lists indexed pages of a specific.
     *
     * @return a list of URLData objects representing the indexed pages
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    List<String> linksListForURL(String url) throws RemoteException;

    /**
     * Lists indexed pages.
     *
     * @param motive the reason for the shutdown
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    void shutdown(String motive) throws RemoteException;
}