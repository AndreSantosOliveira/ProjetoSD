import javax.swing.*;
import java.rmi.registry.LocateRegistry;

public class ClienteRMI {
    public static void main(String[] args) {
        /* This might be necessary if you ever need to download classes:
        System.getProperties().put("java.security.policy", "policy.all");
        System.setSecurityManager(new RMISecurityManager()); */
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
        }
    }
}