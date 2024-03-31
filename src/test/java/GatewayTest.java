import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GatewayTest {

    @Mock
    Socket socketMock;

    @Mock
    PrintWriter printWriterMock;

    @Mock
    MetodosRMIBarrel metodosRMIBarrelMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void indexURLString() throws IOException {
        Gateway gateway = new Gateway();
        gateway.queueManager = printWriterMock;

        String url = "http://example.com";
        gateway.indexURLString(url);

        verify(printWriterMock).println(url);
    }

    @Test
    void search() throws RemoteException {
        Gateway gateway = new Gateway();
        gateway.metodosBarrelManager = metodosRMIBarrelMock;

        String words = "search terms";

        when(metodosRMIBarrelMock.searchInput(anyString())).thenReturn(new Tuple<>("barrelId", null));

        gateway.search(words);

        verify(metodosRMIBarrelMock).searchInput(words);
    }

    @Test
    void getAdministrativeStatistics() throws RemoteException {
        Gateway gateway = new Gateway();
        gateway.metodosBarrelManager = metodosRMIBarrelMock;

        gateway.getAdministrativeStatistics();

        verify(metodosRMIBarrelMock, atLeastOnce()).getActiveBarrels();
    }

    @Test
    void addSearch() throws RemoteException {
        Gateway gateway = new Gateway();

        gateway.addSearch("term1");
        gateway.addSearch("term1");
        gateway.addSearch("term2");

        assertEquals(2, gateway.top10Searches.get("term1"));
        assertEquals(1, gateway.top10Searches.get("term2"));
    }

    // Helper method to mock PrintWriter creation
    private PrintWriter createPrintWriter(Object any) {
        return printWriterMock;
    }
}
