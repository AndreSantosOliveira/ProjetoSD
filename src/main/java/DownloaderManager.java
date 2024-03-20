import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class DownloaderManager extends UnicastRemoteObject implements RMIDownloaderMGR {

    static UniqueQueue<String> queueLinks = new UniqueQueue<>(50);

    protected DownloaderManager() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        try {
            DownloaderManager downloaderManager = new DownloaderManager();
            LocateRegistry.createRegistry(3510).rebind("downloaderManager", downloaderManager);
        } catch (IOException re) {
            System.out.println("Exception in Gateway RMI: " + re);
        }

        // download manager ready
        System.out.println("DownloaderManager ready.");


        // TODO 1: Fetch url from URL queue
        // TODO 2: URL -> new thread -> Downloader.crawl(url) -> URLData

        //queueLinks.offer("https://www.sapo.pt/");

        //while (!queueLinks.isEmpty()) {
        //    crawl(queueLinks.poll());
        //}
    }

    @Override
    public void receberLinkParaDownloadManager(String url) throws RemoteException {

    }
}
