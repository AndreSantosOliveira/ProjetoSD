import java.rmi.Remote;
import java.rmi.RemoteException;
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
    List<URLData> searchInput(String url) throws RemoteException;

    /**
     * Saves the content of the barrels to a file.
     *
     * @throws RemoteException
     */
    void saveBarrelsContent() throws RemoteException;
}