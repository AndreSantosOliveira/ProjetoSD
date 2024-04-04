import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class QueueManagerTest {

    private QueueManager queueManager;
    private Socket mockSocket;
    private ServerSocket mockServerSocket;

    @BeforeEach
    public void setup() throws IOException {
        queueManager = new QueueManager();
        mockSocket = mock(Socket.class);
        mockServerSocket = mock(ServerSocket.class);

        when(mockSocket.getOutputStream()).thenReturn(System.out);
        when(mockSocket.getInputStream()).thenReturn(System.in);
        when(mockServerSocket.accept()).thenReturn(mockSocket);
    }

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

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();


        assertTrue(queueManager.connectToDownloadManager());
    }


    @Test
    public void shouldNotConnectToDownloadManagerAfterMaxAttempts() throws IOException {
        when(mockSocket.getOutputStream()).thenThrow(new IOException());
        assertFalse(queueManager.connectToDownloadManager());
    }


}