import java.io.IOException;
import java.rmi.Remote;
import java.util.HashSet;

public interface MetodosGateway extends Remote {
    String indexarURL(String url) throws IOException;

    HashSet<URLData> pesquisar(String palavras) throws java.rmi.RemoteException;

}