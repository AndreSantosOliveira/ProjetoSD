import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DownloaderTest {

    private Downloader downloader;

    @Mock
    private Socket mockSocket;

    @Mock
    private PrintWriter mockPrintWriter;

    @BeforeEach
    void setUp() throws RemoteException {
        MockitoAnnotations.initMocks(this);
        downloader = new Downloader();
    }

    @Test
    void testCrawlURL_ValidURL() throws RemoteException, IOException {
        // Arrange
        String validURL = "https://sapo.pt";
        OutputStream outputStream = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(outputStream);
        when(mockSocket.isConnected()).thenReturn(true);
        when(mockPrintWriter.checkError()).thenReturn(false);
        downloader.queueManager = mockPrintWriter;

        // Act
        downloader.crawlURL(validURL, 0);

        // Figure out if crawl was successful
        Connection.Response response = Jsoup.connect(validURL).execute();
        Document document = response.parse();
        Elements links = document.select("a[href]");
        for (Element link : links) {
            String linkURL = link.attr("abs:href");
            if (linkURL.equals(validURL)) {
                // Assert
                assertTrue(outputStream.toString().contains(validURL));
                return;
            }
        }
    }

    @Test
    void testCrawlURL_InvalidURL() throws RemoteException {
        // Arrange
        String invalidURL = "invalid_url";
        downloader.queueManager = mockPrintWriter;

        // Act
        downloader.crawlURL(invalidURL, 0);

        // Assert
        verifyNoInteractions(mockPrintWriter);
    }

    @Test
    void testSocketDownloadManagerToQueue_SuccessfulConnection() throws IOException {
        // Arrange
        when(mockSocket.isConnected()).thenReturn(true);
        when(mockSocket.getOutputStream()).thenReturn(System.out);
        Downloader.queueManager = mockPrintWriter;

        // Run queueManager concurently with test downloader
        new Thread(() -> {
            QueueManager.main(new String[0]);
        }).start();

        // Act
        boolean result = downloader.socketDownloadManagerToQueue();

        // Assert
        assertTrue(result);
        verifyNoInteractions(mockPrintWriter); // No need to interact with PrintWriter
    }

    @Test
    void testSocketDownloadManagerToQueue_FailedConnection() {
        // Arrange
        when(mockSocket.isConnected()).thenReturn(false);

        // Act
        boolean result = downloader.socketDownloadManagerToQueue();

        // Assert
        assertFalse(result);
    }
}
