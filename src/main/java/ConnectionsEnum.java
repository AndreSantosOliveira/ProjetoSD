/**
 * PortasEIPs is an enumeration that represents different types of ports.
 * Each port is associated with a Connection object that contains the IP address and port number.
 * This enumeration provides methods to get the IP address and port number, print initialization status, and convert the port to a string.
 */
public enum ConnectionsEnum {

    // Portas
    MULTICAST(new Connection("230.0.0.1", 6900)),
    GATEWAY(new Connection("127.0.0.1", 1000)),
    QUEUE_MANAGER(new Connection("127.0.0.1", 3300)),
    BARREL_MANAGER(new Connection("127.0.0.1", 4200)),
    DOWNLOAD_MANAGER(new Connection("127.0.0.1", 3570));

    private final Connection descritor;

    /**
     * Constructor for PortasEIPs.
     *
     * @param descritor the DescritorIPPorta object associated with the port
     */
    ConnectionsEnum(Connection descritor) {
        this.descritor = descritor;
    }

    /**
     * Gets the IP address of the port.
     *
     * @return the IP address of the port
     */
    public String getIP() {
        return descritor.getIP();
    }

    /**
     * Gets the port number of the port.
     *
     * @return the port number of the port
     */
    public int getPort() {
        return descritor.getPorta();
    }

    /**
     * Prints the initialization status of the port.
     *
     * @param name the name of the port
     */
    public void printINIT(String name) {
        System.out.println("[" + this + "] " + name + " ready.");
    }

    /**
     * Converts the port to a string.
     *
     * @return the string representation of the port
     */
    @Override
    public String toString() {
        return descritor.toString();
    }
}