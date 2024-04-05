import java.io.IOException;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The ClienteRMI class is responsible for interacting with the search system through a command terminal.
 * It implements the Serializable and Remote interfaces.
 * It allows the user to index a new URL, search for pages that contain a set of terms,
 * list pages with a link to a specific page, access the administration page and terminate the program.
 */
public class ClienteRMI implements Serializable, Remote {

    //-1 -> não logado
    //0 -> user
    //1 -> admin
    static int admin = -1;

    /**
     * Default constructor for ClienteRMI.
     *
     * @throws RemoteException if an error occurs during remote object initialization.
     */
    protected ClienteRMI() throws RemoteException {
        super();
    }

    /**
     * This method is used to separate a larger list into smaller lists.
     *
     * @param inputList   The list to be separated
     * @param sublistSize The size of the smaller lists
     * @return A list of lists
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
     * The main method for the ClienteRMI class.
     * It creates an instance of ClienteRMI, binds it to the RMI registry,
     * and starts the interaction with the search system.
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
                    metodosGateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");
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

            MetodosRMIGateway finalMetodosGateway = metodosGateway;
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        if (finalMetodosGateway != null)
                            finalMetodosGateway.getAdministrativeStatistics();
                    } catch (RemoteException | InterruptedException e) {
                        System.out.println("Gateway went offline. Exiting...");
                        System.exit(0);
                    }
                }
            }).start();

            // Read input and send to the Queue
            String command;
            System.out.println("Welcome to Googol, your favourite search engine. For additional information type 'help'.");
            System.out.println("Please enter your login details:");
            do {
                if (admin != -1) {
                    System.out.println("Enter an option:");
                }
                System.out.print("> ");

                command = scanner.nextLine().toLowerCase();

                if (admin == -1) {
                    // Authentication
                    String[] splitOption = command.split(" ");
                    if (splitOption.length != 2) {
                        System.out.println("Invalid syntax: <username> <password>");
                    } else {
                        String username = splitOption[0];
                        String password = splitOption[1];

                        int res = metodosGateway.autenticarCliente(username, password);
                        if (res == -1) {
                            System.out.println("Invalid username or password.");
                        } else {
                            admin = res;
                            System.out.println("Login successful as " + (admin == 0 ? "user" : "admin") + ". Welcome, " + username + "!");
                        }
                    }
                } else {
                    if (command.length() <= 1) {
                        System.out.println("Invalid option. For additional information type 'help'");
                        continue;
                    }

                    String[] splitOption = command.split(" ");

                    if (splitOption.length < 2 && !splitOption[0].equals("help") && !splitOption[0].equals("exit") && !splitOption[0].equals("list") && !splitOption[0].equals("admin") && !splitOption[0].equals("save") && !splitOption[0].equals("clear") && !splitOption[0].equals("cls") && !splitOption[0].equals("shutdown") && !splitOption[0].equals("logout")) {
                        System.out.println("Invalid option. For additional information type 'help'");
                        continue;
                    }

                    switch (splitOption[0]) {
                        case "copy":
                            if (admin == 1) {
                                if (splitOption.length != 3) {
                                    System.out.println("Invalid syntax: copy <from> <to>");
                                    break;
                                }

                                // get 2 barrel IDs
                                String de = splitOption[1];
                                if (de == null || de.isEmpty()) {
                                    System.out.println("Invalid barrel ID. copy <from> <to>");
                                    break;
                                }

                                String para = splitOption[2];
                                if (para == null || para.isEmpty()) {
                                    System.out.println("Invalid barrel ID. copy <from> <to>");
                                    break;
                                }

                                try {
                                    System.out.printf("Copying barrel %s -> %s...\n", de, para);
                                    System.out.println(metodosGateway.copyBarrel(de, para));
                                } catch (RemoteException ignored) {
                                }
                            } else {
                                System.out.println("You do not have permission to perform this action.");
                            }
                            break;

                        case "shutdown":
                            if (admin == 1) {
                                try {
                                    System.out.println("Shutting down all components...");
                                    metodosGateway.shutdown("ClientRMI ordered shutdown.");
                                } catch (RemoteException ignored) {
                                }
                            } else {
                                System.out.println("You do not have permission to perform this action.");
                            }
                            break;

                        case "clear":
                        case "cls":
                            for (int i = 0; i < 40; i++) {
                                System.out.println();
                            }
                            break;

                        case "index": //     index https://sapo.pt
                            // index https://crawler-test.com/links/page_with_external_links
                            StringBuilder url = new StringBuilder(splitOption[1]);
                            // Only add https:// if it is not already there
                            if (!url.toString().contains("https://")) {
                                url.insert(0, "https://");
                            }
                            System.out.println(metodosGateway.indexURLString(url.toString()));
                            break;

                        case "search":
                            // Parse the search query
                            StringBuilder pesquisa = new StringBuilder();
                            for (int i = 1; i < splitOption.length; ++i) {
                                if (i == splitOption.length - 1) {
                                    pesquisa.append(splitOption[i]);
                                    break;
                                }
                                pesquisa.append(splitOption[i]).append(" ");
                            }


                            List<URLData> lista = metodosGateway.search(pesquisa.toString());

                            List<List<URLData>> resultados = separateList(lista, 10);

                            if (resultados.isEmpty()) {
                                System.out.println("No results found for your search.");
                                break;
                            }
                            if (resultados.size() == 1) {
                                for (URLData urlData : resultados.get(0)) {
                                    System.out.println(urlData.getPageTitle() + (urlData.getRelevance() != -1 ? " (" + urlData.getRelevance() + " references)" : ""));
                                    System.out.println(" -> " + urlData.getURL());
                                }
                                break;
                            }

                            int paginaSelecionada = 0;
                            String input;
                            boolean invalid = false;
                            // Loop to navigate through the pages of the search results
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
                            if (admin == 1) {
                                System.out.println(metodosGateway.getAdministrativeStatistics());
                            } else {
                                System.out.println("You do not have permission to perform this action.");
                            }
                            break;

                        case "help":
                            help();
                            break;

                        case "logout":
                            System.out.println("Logging out...");
                            System.out.println("Please enter your login details:");
                            admin = -1;
                            break;
                        case "exit":
                            System.out.println("Exiting...");
                            System.exit(0);
                            break;
                        default:
                            System.out.println("Invalid option. For additional help type 'help'");
                    }
                }

            } while (!command.equals("exit"));

        } catch (RemoteException e) {
            System.out.println("Exception in RMI client: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to print the help message.
     */
    private static void help() {
        System.out.println("Available commands:");
        System.out.println("index <url> - Indexes a URL");
        System.out.println("search <words> - Searches for pages that contain the specified words");
        System.out.println("list <url> - Lists pages that contain a link to the specified URL");
        System.out.println("copy <from> <to> - Copies the content of one barrel to another");
        System.out.println("clear/cls - Clears the console");
        System.out.println("save - Saves the content of the barrels");
        System.out.println("admin - Displays administrative statistics");
        System.out.println("shutdown - Shuts down all components");
        System.out.println("logout - Logs out of the system");
        System.out.println("exit - Exits the system");
    }
}