import java.io.IOException;
import java.rmi.Remote;
import java.util.HashSet;

public interface MetodosRMIGateway extends Remote {
    String indexarURL(String url) throws IOException;

    HashSet<URLData> pesquisar(String palavras) throws java.rmi.RemoteException;
}