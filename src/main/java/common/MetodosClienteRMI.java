package common;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MetodosClienteRMI extends Remote {
    public void print_on_client(String s) throws RemoteException;

}
