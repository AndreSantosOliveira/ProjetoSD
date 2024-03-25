import java.io.IOException;
import java.rmi.Remote;
import java.util.List;

public interface MetodosRMIGateway extends Remote {
    String indexarURL(String url) throws IOException;

    List<URLData> pesquisar(String palavras) throws java.rmi.RemoteException;

    List<URLData> listarPaginasIndexadas() throws java.rmi.RemoteException;
}