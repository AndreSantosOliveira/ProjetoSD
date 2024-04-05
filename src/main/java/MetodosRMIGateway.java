import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * The MetodosRMIGateway interface extends the Remote interface.
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
     *
     * @throws RemoteException       if an error occurs during remote method invocation.
     * @throws MalformedURLException if the URL is not formatted correctly.
     * @throws NotBoundException     if an attempt is made to lookup or unbind in the registry a name that has no associated binding.
     */
    void saveBarrelsContent() throws RemoteException, MalformedURLException, NotBoundException;

    /**
     * Gets the administrative statistics.
     *
     * @return a string containing the administrative statistics
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String getAdministrativeStatistics() throws RemoteException;

    /**
     * Lists indexed pages of a specific URL.
     *
     * @param url the URL to list indexed pages for
     * @return a list of strings representing the indexed pages
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    List<String> linksListForURL(String url) throws RemoteException;

    /**
     * Shuts down the system for a specific reason.
     *
     * @param motive the reason for the shutdown
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    void shutdown(String motive) throws RemoteException;

    /**
     * Authenticates a client.
     *
     * @param username the username of the client
     * @param password the password of the client
     * @return an integer indicating account type, or -1 if the authentication failed
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    int autenticarCliente(String username, String password) throws RemoteException;

    /**
     * Copies the content of a barrel to another barrel.
     *
     * @param from the barrel to copy from
     * @param to   the barrel to copy to
     * @return a string representing the result of the copy operation
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    String copyBarrel(String from, String to) throws RemoteException;

    /**
     * Sends a heartbeat signal to indicate that the system is still running.
     *
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    void heartBeat() throws RemoteException;
}