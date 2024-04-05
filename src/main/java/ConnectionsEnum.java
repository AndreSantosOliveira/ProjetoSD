import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

/**
 * ConnectionsEnum is an enumeration that represents different types of connections.
 * Each connection is associated with a Connection object that contains the IP address and port number.
 * This enumeration provides methods to get the IP address and port number, print initialization status, and convert the connection to a string.
 */
public enum ConnectionsEnum implements Serializable {

    // Enumerations representing different types of connections
    MULTICAST, GATEWAY, QUEUE_MANAGER, BARREL_MANAGER, DOWNLOAD_MANAGER;

    // The IP address of the connection
    private String IP;
    // The port number of the connection
    private int port;

    /**
     * Constructor for ConnectionsEnum.
     * This constructor calls the loadConnectionInfo method to load the IP address and port number from the connections.txt file.
     */
    ConnectionsEnum() {
        // Load IP address and port number from connections.txt
        loadConnectionInfo();
    }

    /**
     * Loads IP address and port number from the connections.txt file.
     * The file is expected to have each connection on a separate line in the format "connectionName|ipAddress:portNumber".
     * If the connection name matches the name of the enumeration, the IP address and port number are stored.
     */
    private void loadConnectionInfo() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/connections.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    String connectionName = parts[0];
                    String[] address = parts[1].split(":");
                    String ipAddress = address[0];
                    int portNumber = Integer.parseInt(address[1]);

                    if (this.name().equals(connectionName)) {
                        IP = ipAddress;
                        port = portNumber;
                        break;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the IP address of the connection.
     *
     * @return the IP address of the connection
     */
    public String getIP() {
        return IP;
    }

    /**
     * Gets the port number of the connection.
     *
     * @return the port number of the connection
     */
    public int getPort() {
        return port;
    }

    /**
     * Prints the initialization status of the connection.
     * The status is printed in the format "[connection] name ready."
     *
     * @param name the name of the connection
     */
    public void printINIT(String name) {
        System.out.println("[" + this + "] " + name + " ready.");
    }

    /**
     * Converts the connection to a string.
     * The string is formatted as "ip:port".
     *
     * @return the string representation of the connection
     */
    @Override
    public String toString() {
        return getIP() + ":" + getPort();
    }
}