package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The MetodosClienteRMI interface defines the methods that a remote object
 * implementing this interface must provide. It extends the Remote interface,
 * which is the root interface in the RMI hierarchy.
 */
public interface MetodosClienteRMI extends Remote {

    /**
     * Prints a string on the client.
     *
     * @param s the string to be printed
     * @throws RemoteException if the remote invocation fails
     */
    public void print_on_client(String s) throws RemoteException;

}