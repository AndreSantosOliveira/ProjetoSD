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
    private PrintWriter mockPrintWriter;
    private BufferedReader mockBufferedReader;
    private Socket mockSocket;
    private ServerSocket mockServerSocket;

    @BeforeEach
    public void setup() throws IOException {
        queueManager = new QueueManager();
        mockPrintWriter = mock(PrintWriter.class);
        mockBufferedReader = mock(BufferedReader.class);
        mockSocket = mock(Socket.class);
        mockServerSocket = mock(ServerSocket.class);

        when(mockSocket.getOutputStream()).thenReturn(System.out);
        when(mockSocket.getInputStream()).thenReturn(System.in);
        when(mockServerSocket.accept()).thenReturn(mockSocket);
    }

    @Test
    public void shouldConnectToDownloadManagerSuccessfully() throws IOException {


        // Run a downloadManager in a thread separately
        new Thread(() -> {
            try {
                DownloaderManager.main(new String[0]);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        // Run dl1 in a thread separately
        new Thread(() -> {
            Downloader.main(new String[]{"5436", "dl1"});
        }).start();

        // Run dl2 in a thread separately
        new Thread(() -> {
            Downloader.main(new String[]{"5434", "dl2"});
        }).start();

        assertTrue(queueManager.connectToDownloadManager());
    }


    @Test
    public void shouldNotConnectToDownloadManagerAfterMaxAttempts() throws IOException {
        when(mockSocket.getOutputStream()).thenThrow(new IOException());
        assertFalse(queueManager.connectToDownloadManager());
    }

    /*
    @Test
    public void shouldAddUrlToQueueWhenReceivedFromGateway() throws IOException {
        when(mockBufferedReader.readLine()).thenReturn("http://example.com");
        queueManager.handleGatewayConnection(mockBufferedReader);
        assertEquals(1, queueManager.queue.size());
    }

    @Test
    public void shouldNotAddUrlToQueueWhenQueueIsFull() throws IOException {
        for (int i = 0; i < 50; ++i) {
            queueManager.queue.offer("http://example.com/" + i);
        }
        when(mockBufferedReader.readLine()).thenReturn("http://example.com/extra");
        queueManager.handleGatewayConnection(mockBufferedReader);
        assertEquals(50, queueManager.queue.size());
    }*/
}