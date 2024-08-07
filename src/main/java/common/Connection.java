package common;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import java.io.Serializable;

/**
 * The Connection class is used to describe an IP address and port, along with an optional RMI name.
 * It implements Serializable interface.
 */
public class Connection implements Serializable {

    // The IP address
    private final String ip;
    // The RMI name
    private final String rmiName;
    // The external IP address
    private final String externalIP;
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
     * @param ip         the IP address
     * @param porta      the port number
     * @param rmiName    the RMI name
     * @param externalIP the external IP address
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

    /**
     * Getter for the external IP address.
     *
     * @return the external IP address
     */
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