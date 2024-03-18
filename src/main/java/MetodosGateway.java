import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;

public interface MetodosGateway extends Remote {
    String indexarURL(String url) throws java.rmi.RemoteException;

    HashSet<URLData> pesquisar(String palavras) throws java.rmi.RemoteException;

}