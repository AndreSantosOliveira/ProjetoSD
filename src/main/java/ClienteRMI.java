import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/*
 * ClienteRMI é a classe que permite ao utilizador interagir com o sistema de pesquisa
 * através de um terminal de comandos.
 *
 * O utilizador pode indexar um novo URL, pesquisar páginas que contenham um conjunto de termos,
 * listar páginas com ligação para uma página específica, aceder à página de administração e terminar o programa.
 *
 * O ClienteRMI é responsável por criar um novo registo RMI para o cliente, ligar-se ao Gateway por RMI e
 * enviar comandos para a Queue.
 *
 */
public class ClienteRMI implements Serializable, Remote {

    // ClienteRMI constructor
    protected ClienteRMI() throws RemoteException {
        super();
    }

    // Main
    public static void main(String[] args) {
        // Sistema de fail over para multiplos clientes
        int startPort = 4000;
        int endPort = 4010;
        int currentPort = startPort;
        boolean portFound = false;

        // Sistema de failover caso o downloader não esteja disponível
        int maxRetries = 3; // Número máximo de tentativas de ligação ao Gateway
        int retryCount = 0; // Contador de tentativas de ligação ao Gateway


        // Tentar encontrar um porto disponível para o cliente
        try (Scanner scanner = new Scanner(System.in)) {
            Registry registry = null;
            while (currentPort < endPort) {
                try {
                    registry = LocateRegistry.createRegistry(currentPort);
                    portFound = true;
                    break; // Se a ligação for bem sucedida, sair do loop
                } catch (RemoteException e) {
                    currentPort++;
                }
            }

            // Se não for possível encontrar um porto disponível, terminar o programa
            if (!portFound) {
                System.out.println("Maximum capacity reached. Cannot start the client.");
                return;
            }

            // Conectar cliente por RMI
            ClienteRMI clienteRMI = new ClienteRMI();
            registry.rebind("Client", clienteRMI);

            // Tentar conectar ao Gateway por RMI
            MetodosGateway metodosGateway = null;
            while (metodosGateway == null && retryCount < maxRetries) {
                try {
                    metodosGateway = (MetodosGateway) LocateRegistry.getRegistry(1000).lookup("Gateway");
                } catch (RemoteException | NotBoundException e) {
                    retryCount++;
                    if (retryCount < maxRetries) {
                        System.out.println("Failed to connect to Gateway. Retrying...");
                        // Sleep para evitar tentativas de ligação consecutivas
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }

            // Se não for possível conectar ao Gateway, terminar o programa
            if (metodosGateway == null) {
                System.out.println("Failed to connect to Gateway after maximum retry attempts. Exiting...");
                return;
            }

            // Ler input e mandar para a Queue
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
                if (splitOption.length < 2 && !splitOption[0].equals("help") && !splitOption[0].equals("exit") && !splitOption[0].equals("list") && !splitOption[0].equals("admin")) {
                    System.out.println("Invalid option. For additional information type 'help'");
                    continue;
                }
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

        } catch (RemoteException e) {
            System.out.println("Exception in RMI client: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
