package common;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * The MetodosRMIGateway interface extends the Remote interface.
 * This interface defines the methods that a Gateway object must implement.
 * These methods allow the Gateway to index a URL, search for URLs based on input, save the content of the barrels,
 * get administrative statistics, list indexed pages, shut down the Gateway, authenticate a client, copy barrel content,
 * check if the Gateway is alive, dynamically update the Gateway, subscribe and unsubscribe a client, and find a client.
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
     * @return a list of common.URLData objects that match the search input
     * @throws java.rmi.RemoteException if an error occurs during remote method invocation.
     */
    List<URLData> search(String words) throws IOException, NotBoundException;

    /**
     * Saves the content of the barrels.
     *
     * @throws RemoteException       if an error occurs during remote method invocation.
     * @throws MalformedURLException if the specified URL is not formatted correctly.
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
     * Shuts down the Gateway.
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
     * @return an integer indicating account type, or if the authentication failed
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
     * Heartbeat method used to check if the gateway is still alive.
     *
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    void heartBeat() throws RemoteException;

    /**
     * Dynamically updates the Gateway.
     *
     * @throws IOException       if an error occurs during the operation.
     * @throws NotBoundException if an attempt is made to lookup or unbind in the registry a name that has no associated binding.
     */
    void dynamicallyUpdate() throws IOException, NotBoundException;

    /**
     * Subscribes a client to the Gateway.
     *
     * @param c the client to subscribe
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    public void subscribeClient(MetodosClienteRMI c) throws RemoteException;

    /**
     * Unsubscribes a client from the Gateway.
     *
     * @param c the client to unsubscribe
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    public void unsubscribeClient(MetodosClienteRMI c) throws RemoteException;

    /**
     * Finds a client in the Gateway.
     *
     * @param c the client to find
     * @return an integer representing the index of the client in the list of clients, or -1 if the client is not found
     * @throws RemoteException if an error occurs during remote method invocation.
     */
    public int findClient(MetodosClienteRMI c) throws RemoteException;

}