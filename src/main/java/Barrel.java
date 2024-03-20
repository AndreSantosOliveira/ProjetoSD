import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Barrel implements RMIBarrelGateway, Serializable {

    public Barrel() throws RemoteException {
        super();
    }

    public String comunicarPesquisaParaGateway(String resultado) throws RemoteException {
        return resultado;
    }

    // =======================================================

    public static void main(String args[])  {
        try {
            RMIBarrelGateway obj = (RMIBarrelGateway) Naming.lookup("rmi://localhost:1001/gateway");
            System.out.println(obj.comunicarPesquisaParaGateway("Hello, world!"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}