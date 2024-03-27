import java.io.IOException;
import java.rmi.Remote;
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
    String indexarURL(String url) throws IOException;

    /**
     * Searches for URLs based on input.
     *
     * @param palavras the input to search for
     * @return a list of URLData objects that match the search input
     * @throws java.rmi.RemoteException if an error occurs during remote method invocation.
     */
    List<URLData> pesquisar(String palavras) throws java.rmi.RemoteException;

    /**
     * Lists the indexed pages.
     *
     * @return a list of URLData objects representing the indexed pages
     * @throws java.rmi.RemoteException if an error occurs during remote method invocation.
     */
    List<URLData> listarPaginasIndexadas() throws java.rmi.RemoteException;
}