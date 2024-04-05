import java.io.Serializable;

/**
 * Connection class.
 * This class is used to describe an IP address and port, along with an optional RMI name.
 */
public class Connection implements Serializable {

    // The IP address and rmi name
    private final String ip, rmiName, externalIP;
    // The port number
    private final int porta;

    /**
     * Constructor for Connection.
     * This constructor initializes the IP address, port number, and RMI name.
     *
     * @param ip      the IP address
     * @param porta   the port number
     * @param rmiName the RMI name
     */
    public Connection(String ip, int porta, String rmiName) {
        this.ip = ip;
        this.porta = porta;
        this.rmiName = rmiName;
        this.externalIP = ip;
    }

    /**
     * Constructor for Connection.
     * This constructor initializes the IP address, port number, RMI name and external IP address.
     *
     * @param ip      the IP address
     * @param porta   the port number
     * @param rmiName the RMI name
     */
    public Connection(String ip, int porta, String rmiName, String externalIP) {
        this.ip = ip;
        this.porta = porta;
        this.rmiName = rmiName;
        this.externalIP = externalIP;
    }

    /**
     * Getter for the IP address.
     *
     * @return the IP address
     */
    public String getIP() {
        return ip;
    }

    public String getExternalIP() {
        return externalIP;
    }

    /**
     * Getter for the port number.
     *
     * @return the port number
     */
    public int getPorta() {
        return porta;
    }

    /**
     * Getter for the RMI name.
     *
     * @return the RMI name
     */
    public String getRMIName() {
        return rmiName;
    }

    /**
     * Returns a string representation of the object.
     * The string is formatted as "ip:port".
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return ip + ':' + porta;
    }

}