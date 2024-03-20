
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

// Interface
        /*
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Googol();
            }
        });
        */
public class ClienteRMI implements Serializable, Remote {

    // Client constructor
    protected ClienteRMI() throws RemoteException {
        super();
    }

    // Main
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            // Remote Method Invocation (RMI)
            ClienteRMI clienteRMI = new ClienteRMI();
            LocateRegistry.createRegistry(4000).rebind("Client", clienteRMI);

            MetodosGateway metodosGateway = (MetodosGateway) LocateRegistry.getRegistry(2000).lookup("Gateway");

            String command;
            System.out.println("Welcome to Googol, your favourite search engine. For additional information type 'help'.");


            // Menu
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
                            // If last element, don't add space
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