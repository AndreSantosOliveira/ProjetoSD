import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;

public interface MetodosRMI extends Remote {
    void indexarURL(String url) throws java.rmi.RemoteException;

    HashSet<URLData> pesquisar(String palavras) throws java.rmi.RemoteException;

    public String sayHello() throws RemoteException;

}