import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * This class contains unit tests for the Gateway class.
 */
class GatewayTest {

    // Mock objects used for testing
    @Mock
    Socket socketMock;
    @Mock
    PrintWriter printWriterMock;
    @Mock
    MetodosRMIBarrel metodosRMIBarrelMock;

    /**
     * This method sets up the testing environment before each test.
     * It initializes the mock objects.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * This test checks the indexURLString method of the Gateway class.
     * It mocks the necessary objects and checks if the URL is correctly indexed.
     */
    @Test
    void indexURLString() throws IOException {
        Gateway gateway = new Gateway();
        gateway.queueManager = printWriterMock;

        String url = "http://example.com";
        gateway.indexURLString(url);

        verify(printWriterMock).println(url);
    }

    /**
     * This test checks the addSearch method of the Gateway class.
     * It adds searches and checks if they are correctly added to the top10Searches map.
     */
    @Test
    void addSearch() throws RemoteException {
        Gateway gateway = new Gateway();

        gateway.addSearch("term1");
        gateway.addSearch("term1");
        gateway.addSearch("term2");

        assertEquals(2, gateway.top10Searches.get("term1"));
        assertEquals(1, gateway.top10Searches.get("term2"));
    }

    /**
     * This is a helper method used for testing.
     * It mocks the creation of a PrintWriter object.
     */
    private PrintWriter createPrintWriter(Object any) {
        return printWriterMock;
    }
}