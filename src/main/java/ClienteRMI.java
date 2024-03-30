import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * ClienteRMI class implements Serializable and Remote.
 * This class allows the user to interact with the search system through a command terminal.
 * The user can index a new URL, search for pages that contain a set of terms,
 * list pages with a link to a specific page, access the administration page and terminate the program.
 * ClienteRMI is responsible for creating a new RMI registry for the client, connecting to the Gateway via RMI and
 * sending commands to the Queue.
 */
public class ClienteRMI implements Serializable, Remote {

    /**
     * Default constructor for ClienteRMI.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    protected ClienteRMI() throws RemoteException {
        super();
    }

    /**
     * Main method for the ClienteRMI class.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Failover system for multiple clients
        int startPort = 4000;
        int endPort = 4010;
        int currentPort = startPort;
        boolean portFound = false;

        // Failover system in case the downloader is not available
        int maxRetries = 3; // Maximum number of connection attempts to the Gateway
        int retryCount = 0; // Counter of connection attempts to the Gateway

        // Try to find an available port for the client
        try (Scanner scanner = new Scanner(System.in)) {
            Registry registry = null;
            while (currentPort < endPort) {
                try {
                    registry = LocateRegistry.createRegistry(currentPort);
                    portFound = true;
                    break; // If the connection is successful, exit the loop
                } catch (RemoteException e) {
                    currentPort++;
                }
            }

            // If it is not possible to find an available port, terminate the program
            if (!portFound) {
                System.out.println("Maximum capacity reached. Cannot start the client.");
                return;
            }

            // Connect client via RMI
            ClienteRMI clienteRMI = new ClienteRMI();
            registry.rebind("Client", clienteRMI);

            // Try to connect to the Gateway via RMI
            MetodosRMIGateway metodosGateway = null;
            while (metodosGateway == null && retryCount < maxRetries) {
                try {
                    metodosGateway = (MetodosRMIGateway) LocateRegistry.getRegistry(ConnectionsEnum.GATEWAY.getPort()).lookup("gateway");
                } catch (RemoteException | NotBoundException e) {
                    ++retryCount;
                    if (retryCount < maxRetries) {
                        System.out.println("Failed to connect to Gateway. Retrying...");
                        // Sleep to avoid consecutive connection attempts
                        try {
                            Thread.sleep(1001);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }

            // If it is not possible to connect to the Gateway, terminate the program
            if (metodosGateway == null) {
                System.out.println("Failed to connect to Gateway after maximum retry attempts. Exiting...");
                return;
            }

            // Read input and send to the Queue
            String command;
            System.out.println("Welcome to Googol, your favourite search engine. For additional information type 'help'.");
            do {
                System.out.println("Enter an option:");
                System.out.print("> ");

                command = scanner.nextLine().toLowerCase();

                if (command.length() <= 1) {
                    System.out.println("Invalid option. For additional information type 'help'");
                    continue;
                }

                String[] splitOption = command.split(" ");
                if (splitOption.length < 2 && !splitOption[0].equals("help") && !splitOption[0].equals("exit") && !splitOption[0].equals("list") && !splitOption[0].equals("admin") && !splitOption[0].equals("save")) {
                    System.out.println("Invalid option. For additional information type 'help'");
                    continue;
                }
                switch (splitOption[0]) {

                    case "index": //     index https://sapo.pt
                        System.out.println(metodosGateway.indexURLString("https://" + splitOption[1]));
                        break;

                    case "search":
                        StringBuilder pesquisa = new StringBuilder();
                        for (int i = 1; i < splitOption.length; i++) {
                            if (i == splitOption.length - 1) {
                                pesquisa.append(splitOption[i]);
                                break;
                            }
                            pesquisa.append(splitOption[i]).append(" ");
                        }

                        for (URLData urlData : metodosGateway.search(pesquisa.toString())) {
                            System.out.println(urlData.toString());
                        }
                        break;

                    case "save":
                        metodosGateway.saveBarrelsContent();
                        System.out.println("Saved barrels content to file");
                        break;

                    case "list":
                        System.out.println("list");

                        for (URLData urlData : metodosGateway.listIndexedPages()) {
                            System.out.println(urlData.toString());
                        }
                        break;

                    case "admin":
                        System.out.println("Admin");
                        break;

                    case "help":
                        help();
                        break;

                    case "exit":
                        System.out.println("Exiting...");
                        break;

                    default:
                        System.out.println("Invalid option. For additional help type 'help'");
                }
            } while (!command.equals("exit"));

        } catch (RemoteException e) {
            System.out.println("Exception in RMI client: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints the help message.
     */
    private static void help() {
        System.out.println("Available options:");
        System.out.println("index <url> - Index new URL");
        System.out.println("search <terms> - Search for pages that contain a set of terms");
        System.out.println("list - List pages with a link to a specific page");
        System.out.println("admin - Access the administration page");
        System.out.println("exit - Terminate the program");
    }
}