import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClienteRMI implements Serializable, Remote {

    protected ClienteRMI() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        int startPort = 4000;
        int endPort = 4010;
        int currentPort = startPort;
        int maxRetries = 3; // Maximum number of retry attempts
        int retryCount = 0; // Counter for retry attempts
        boolean portFound = false;

        try (Scanner scanner = new Scanner(System.in)) {
            Registry registry = null;

            while (currentPort <= endPort && retryCount < maxRetries) {
                try {
                    registry = LocateRegistry.createRegistry(currentPort);
                    portFound = true;
                    break; // If the port is successfully bound, break the loop
                } catch (RemoteException e) {
                    // If the port is already in use, try the next one
                    currentPort++;
                    retryCount++;
                }
            }

            if (!portFound) {
                System.out.println("Maximum capacity reached. Cannot start the client.");
                return;
            }

            if (retryCount == maxRetries) {
                System.out.println("Maximum retry attempts reached. Cannot start the client.");
                return;
            }

            ClienteRMI clienteRMI = new ClienteRMI();
            registry.rebind("Client", clienteRMI);

            MetodosGateway metodosGateway = (MetodosGateway) LocateRegistry.getRegistry(2000).lookup("Gateway");

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

                switch (splitOption[0]) {
                    case "index":
                        System.out.println(metodosGateway.indexarURL(splitOption[1]));
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
                        for (URLData urlData : metodosGateway.pesquisar(pesquisa.toString())) {
                            System.out.println(urlData.toString());
                        }
                        break;

                    case "list":
                        System.out.println("Listar páginas indexadas");
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

        } catch (NotBoundException | RemoteException e) {
            System.out.println("Exception in RMI client: " + e.getMessage());
        }
    }

    private static void help() {
        System.out.println("Available options:");
        System.out.println("index <url> - Indexar novo URL");
        System.out.println("search <terms> - Pesquisar páginas que contenham um conjunto de termos");
        System.out.println("list - Listar páginas com ligação para uma página específica");
        System.out.println("admin - Aceder à página de administração");
        System.out.println("exit - Terminar o programa");
    }
}
