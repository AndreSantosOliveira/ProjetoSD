
import static java.lang.System.exit;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class ClienteRMI implements Serializable, Remote {
    public static void main(String[] args) {
        /*
        // Interface
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Googol();
            }
        });
        */

        Scanner scanner = new Scanner(System.in);
        String option;
        System.out.println("Welcome to Googol, your favourite search engine. For additional information type 'help'.");

        do {
            System.out.println("Enter an option:");
            System.out.print("> ");

            option = scanner.nextLine().toLowerCase();

            if (option.length() <= 1) {
                System.out.println("Invalid option. For additional information type 'help'");
                continue;
            }

            switch (option) {
                case "index":
                    System.out.println("Indexar novo URL");
                    break;
                case "search":
                    System.out.println("Pesquisar páginas que contenham um conjunto de termos");
                    break;
                case "help":
                    System.out.println("Available options:");
                    System.out.println("index url - Indexar novo URL");
                    System.out.println("terms search - Pesquisar páginas que contenham um conjunto de termos");
                    break;
                case "exit":
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid option. For additional help type 'help'");
            }
        } while (!option.equals("exit"));

        scanner.close();

        /*
        try {
            MetodosRMI h = (MetodosRMI) LocateRegistry.getRegistry(1000).lookup("test");
            String message = h.sayHello();
            System.out.println("HelloClient: " + message);

            // Interface
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new Googol();
                }
            });
        } catch (Exception e) {
            System.out.println("Exception in ClienteRMI.main: " + e);
            e.printStackTrace();
        }*/
    }
}