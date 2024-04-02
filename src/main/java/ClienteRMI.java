import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
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
     * Separares a bigger list into smaller lists.
     *
     * @param inputList
     * @param sublistSize
     * @return
     */
    public static List<List<URLData>> separateList(List<URLData> inputList, int sublistSize) {
        List<List<URLData>> result = new ArrayList<>();

        for (int i = 0; i < inputList.size(); ++i) {
            inputList.get(i).addPageNumber(i + 1);
        }

        for (int i = 0; i < inputList.size(); i += sublistSize) {
            int end = Math.min(inputList.size(), i + sublistSize);
            result.add(new ArrayList<>(inputList.subList(i, end)));
        }

        return result;
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

            // Register client via RMI
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

            MetodosRMIGateway finalMetodosGateway = metodosGateway;
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        finalMetodosGateway.getAdministrativeStatistics();
                    } catch (RemoteException | InterruptedException e) {
                        System.out.println("Gateway went offline. Exiting...");
                        System.exit(0);
                    }
                }
            }).start();

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
                if (splitOption.length < 2 && !splitOption[0].equals("help") && !splitOption[0].equals("exit") && !splitOption[0].equals("list") && !splitOption[0].equals("admin") && !splitOption[0].equals("save") && !splitOption[0].equals("clear") && !splitOption[0].equals("cls") && !splitOption[0].equals("shutdown")) {
                    System.out.println("Invalid option. For additional information type 'help'");
                    continue;
                }

                switch (splitOption[0]) {
                    case "shutdown":
                        try {
                            System.out.println("Shutting down all components...");
                            metodosGateway.shutdown("ClientRMI ordered shutdown.");
                        } catch (RemoteException ignored) {
                        }
                        break;

                    case "clear":
                    case "cls":
                        for (int i = 0; i < 40; i++) {
                            System.out.println();
                        }
                        break;

                    case "index": //     index https://sapo.pt
                        StringBuilder url = new StringBuilder(splitOption[1]);
                        //only add https:// if it is not already there
                        if (!url.toString().contains("https://")) {
                            url.insert(0, "https://");
                        }
                        System.out.println(metodosGateway.indexURLString(url.toString()));
                        break;

                    case "search":
                        StringBuilder pesquisa = new StringBuilder();
                        for (int i = 1; i < splitOption.length; ++i) {
                            if (i == splitOption.length - 1) {
                                pesquisa.append(splitOption[i]);
                                break;
                            }
                            pesquisa.append(splitOption[i]).append(" ");
                        }

                        List<URLData> lista = metodosGateway.search(pesquisa.toString());

                        /*
                        Resultados de pesquisa ordenados por importância. Os resultados de uma pes- quisa (funcionalidade anterior) devem ser apresentados por ordem de relevância.
                        Para simplificar, considera-se que uma página é mais relevante se tiver mais liga- ções de outras páginas.
                        Assim, o indexador automático deve manter, para cada URL, a lista de outros URLs que fazem ligação para ele.
                         */

                        List<List<URLData>> resultados = separateList(lista, 10);

                        if (resultados.isEmpty()) {
                            System.out.println("No results found for your search.");
                            break;
                        }
                        if (resultados.size() == 1) {
                            for (URLData urlData : resultados.get(0)) {
                                System.out.println(urlData.getPageTitle() + " (" + urlData.getRelevance() + " references)");
                                System.out.println(" -> " + urlData.getURL());
                            }
                            break;
                        }

                        int paginaSelecionada = 0;
                        String input;
                        boolean invalid = false;

                        do {
                            if (!invalid) {
                                for (URLData urlData : resultados.get(paginaSelecionada)) {
                                    System.out.println(urlData.getPageTitle() + " (" + urlData.getRelevance() + " references)");
                                    System.out.println(" -> " + urlData.getURL());
                                }

                                System.out.println("\n" + lista.size() + " results | Page " + (paginaSelecionada + 1) + " of " + resultados.size());
                                System.out.println("Enter a page number, or 'q' to quit search:");
                            }
                            System.out.print(">> ");
                            input = scanner.nextLine().trim();

                            if (input.equalsIgnoreCase("q")) {
                                break;
                            } else {
                                try {
                                    int pageNumber = Integer.parseInt(input);
                                    if (pageNumber < 1) {
                                        System.out.println("\nInvalid input. Please enter a valid page number, or 'q'\n");
                                        paginaSelecionada = 0;
                                        invalid = true;
                                    } else if (pageNumber > resultados.size()) {
                                        System.out.println("\nInvalid input. Please enter a valid page number, or 'q'\n");
                                        paginaSelecionada = resultados.size() - 1;
                                        invalid = true;

                                    } else {
                                        invalid = false;
                                        paginaSelecionada = pageNumber - 1;
                                    }

                                } catch (NumberFormatException e) {
                                    invalid = true;
                                    System.out.println("\nInvalid input. Please enter a page number, or 'q'\n");
                                }
                            }
                        } while (true);

                        break;

                    case "save":
                        metodosGateway.saveBarrelsContent();
                        System.out.println("Saved barrels content to file");
                        break;

                    case "list":
                        StringBuilder pesquisaLista = new StringBuilder();
                        for (int i = 1; i < splitOption.length; ++i) {
                            if (i == splitOption.length - 1) {
                                pesquisaLista.append(splitOption[i]);
                                break;
                            }
                            pesquisaLista.append(splitOption[i]).append(" ");
                        }

                        //only add https:// if it is not already there
                        if (!pesquisaLista.toString().contains("https://")) {
                            pesquisaLista.insert(0, "https://");
                        }


                        List<String> links = metodosGateway.linksListForURL(pesquisaLista.toString());
                        if (links.isEmpty()) {
                            System.out.println("No links found for this URL.");
                            break;
                        }

                        links.sort(String::compareTo);
                        System.out.println("Links that reference: " + pesquisaLista);

                        for (int i = 0; i < links.size(); ++i) {
                            System.out.println(i + 1 + ". " + metodosGateway.linksListForURL(pesquisaLista.toString()).get(i));
                        }

                        System.out.println();
                        break;

                    case "admin":
                        System.out.println(metodosGateway.getAdministrativeStatistics());
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
        System.out.println("list <url> - List pages with a link to a specific page");
        System.out.println("admin - Access the administration page");
        System.out.println("exit - Terminate the connection and exit the client\n");
    }
}