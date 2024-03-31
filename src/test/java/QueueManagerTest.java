import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
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

    /*
    @Test
    public void shouldConnectToDownloadManagerSuccessfully() throws IOException {
        assertTrue(queueManager.connectToDownloadManager());
        verify(mockPrintWriter, times(1)).println(anyString());
    }
    */


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
        for (int i = 0; i < 50; i++) {
            queueManager.queue.offer("http://example.com/" + i);
        }
        when(mockBufferedReader.readLine()).thenReturn("http://example.com/extra");
        queueManager.handleGatewayConnection(mockBufferedReader);
        assertEquals(50, queueManager.queue.size());
    }*/
}