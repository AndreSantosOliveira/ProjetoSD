import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * This class contains unit tests for the QueueManager class.
 */
public class QueueManagerTest {

    // The QueueManager instance used for testing
    private QueueManager queueManager;

    // Mock objects used for testing
    private Socket mockSocket;
    private ServerSocket mockServerSocket;

    // The DownloaderManager instance used for testing
    private DownloaderManager downloaderManager;

    /**
     * This method sets up the testing environment before each test.
     * It initializes the mock objects and the QueueManager and DownloaderManager instances.
     */
    @BeforeEach
    public void setup() throws IOException {
        queueManager = new QueueManager();
        mockSocket = mock(Socket.class);
        mockServerSocket = mock(ServerSocket.class);
        downloaderManager = new DownloaderManager();
        when(mockSocket.getOutputStream()).thenReturn(System.out);
        when(mockSocket.getInputStream()).thenReturn(System.in);
        when(mockServerSocket.accept()).thenReturn(mockSocket);
    }

    /**
     * This test checks the connectToDownloadManager method of the QueueManager class with a successful connection.
     * It runs the QueueManager, Downloader, and DownloaderManager in separate threads and checks if the connection is successful.
     */
    @Test
    public void shouldConnectToDownloadManagerSuccessfully() throws IOException {
        // Run QueueManager in a thread separately
        new Thread(() -> {
            QueueManager.main(new String[0]);
        }).start();

        // Run dl1 in a thread separately
        new Thread(() -> {
            Downloader.main(new String[]{"5436", "dl1"});
            Downloader.main(new String[]{"5434", "dl2"});
        }).start();


        // Run a downloadManager in a thread separately
        new Thread(() -> {
            try {
                DownloaderManager.main(new String[0]);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }).start();

        assertTrue(queueManager.connectToDownloadManager());
    }

    /**
     * This test checks the connectToDownloadManager method of the QueueManager class with a failed connection.
     * It mocks the necessary objects and checks if the method correctly handles the failed connection.
     */
    @Test
    public void shouldNotConnectToDownloadManagerAfterMaxAttempts() throws IOException {
        when(mockSocket.getOutputStream()).thenThrow(new IOException());
        assertFalse(queueManager.connectToDownloadManager());
    }
}