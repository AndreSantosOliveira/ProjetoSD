/**
 * ConnectionsEnum is an enumeration that represents different types of connections.
 * Each connection is associated with a Connection object that contains the IP address and port number.
 * This enumeration provides methods to get the IP address and port number, print initialization status, and convert the connection to a string.
 */
public enum ConnectionsEnum {

    // Connections
    MULTICAST(new Connection("230.0.0.1", 6900)),
    GATEWAY(new Connection("127.0.0.1", 1000)),
    QUEUE_MANAGER(new Connection("127.0.0.1", 3300)),
    BARREL_MANAGER(new Connection("127.0.0.1", 4200)),
    DOWNLOAD_MANAGER(new Connection("127.0.0.1", 3570));

    private final Connection descritor;

    /**
     * Constructor for ConnectionsEnum.
     *
     * @param descritor the Connection object associated with the connection
     */
    ConnectionsEnum(Connection descritor) {
        this.descritor = descritor;
    }

    /**
     * Gets the IP address of the connection.
     *
     * @return the IP address of the connection
     */
    public String getIP() {
        return descritor.getIP();
    }

    /**
     * Gets the port number of the connection.
     *
     * @return the port number of the connection
     */
    public int getPort() {
        return descritor.getPorta();
    }

    /**
     * Prints the initialization status of the connection.
     *
     * @param name the name of the connection
     */
    public void printINIT(String name) {
        System.out.println("[" + this + "] " + name + " ready.");
    }

    /**
     * Converts the connection to a string.
     *
     * @return the string representation of the connection
     */
    @Override
    public String toString() {
        return descritor.toString();
    }
}