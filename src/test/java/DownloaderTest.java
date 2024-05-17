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

/**
 * This class contains unit tests for the Downloader class.
 */
class DownloaderTest {

    // The Downloader instance used for testing
    private Downloader downloader;

    // Mock objects used for testing
    @Mock
    private Socket mockSocket;
    @Mock
    private PrintWriter mockPrintWriter;

    /**
     * This method sets up the testing environment before each test.
     * It initializes the mock objects and the Downloader instance.
     */
    @BeforeEach
    void setUp() throws RemoteException {
        MockitoAnnotations.initMocks(this);
        downloader = new Downloader();
    }

    /**
     * This test checks the crawlURL method of the Downloader class with a valid URL.
     * It mocks the necessary objects and checks if the URL is correctly crawled.
     */
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

    /**
     * This test checks the crawlURL method of the Downloader class with an invalid URL.
     * It mocks the necessary objects and checks if the method correctly handles the invalid URL.
     */
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

    /**
     * This test checks the socketDownloadManagerToQueue method of the Downloader class with a successful connection.
     * It mocks the necessary objects and checks if the method correctly handles the successful connection.
     */
    @Test
    void testSocketDownloadManagerToQueue_SuccessfulConnection() throws IOException {
        // Arrange
        when(mockSocket.isConnected()).thenReturn(true);
        when(mockSocket.getOutputStream()).thenReturn(System.out);
        Downloader.queueManager = mockPrintWriter;

        // Run queueManager concurently with test downloader
        new Thread(() -> QueueManager.main(new String[0])).start();

        // Act
        boolean result = downloader.socketDownloadManagerToQueue();

        // Assert
        assertTrue(result);
        verifyNoInteractions(mockPrintWriter); // No need to interact with PrintWriter
    }

    /**
     * This test checks the socketDownloadManagerToQueue method of the Downloader class with a failed connection.
     * It mocks the necessary objects and checks if the method correctly handles the failed connection.
     */
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