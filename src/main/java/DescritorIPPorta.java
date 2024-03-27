/**
 * DescritorIPPorta class.
 * This class is used to describe an IP address and port, along with an optional RMI name.
 */
public class DescritorIPPorta {

    private String ip, rmiName;
    private final int porta;

    /**
     * Constructor for DescritorIPPorta.
     *
     * @param ip    the IP address
     * @param porta the port number
     */
    public DescritorIPPorta(String ip, int porta) {
        this.ip = ip;
        this.porta = porta;
    }

    /**
     * Constructor for DescritorIPPorta.
     *
     * @param ip      the IP address
     * @param porta   the port number
     * @param rmiName the RMI name
     */
    public DescritorIPPorta(String ip, int porta, String rmiName) {
        this.ip = ip;
        this.porta = porta;
        this.rmiName = rmiName;
    }

    /**
     * Getter for the IP address.
     *
     * @return the IP address
     */
    public String getIP() {
        return ip;
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
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return ip + ':' + porta;
    }

}