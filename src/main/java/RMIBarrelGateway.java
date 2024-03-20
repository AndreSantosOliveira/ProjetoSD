import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RMIBarrelGateway extends Remote {

    String comunicarPesquisaParaGateway(String resultado) throws RemoteException;

}