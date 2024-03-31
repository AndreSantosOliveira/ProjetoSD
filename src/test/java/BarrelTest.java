import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BarrelTest {
    static Barrel metodosBarrelManager;

    static {
        try {
            metodosBarrelManager = new Barrel();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void archiveURLTest() {
        URLData testData = new URLData("http://example.com", "Example Title", "Example Description");
        try {
            Barrel.archiveURL(testData);
        } catch (RemoteException e) {
            fail("RemoteException thrown while archiving URLData");
        }

        assertTrue(Barrel.index.containsKey("example"));
        assertTrue(Barrel.index.get("example").contains(testData));
    }

    @Test
    void searchInputTest() {
        Barrel.index.clear();
        URLData testData1 = new URLData("http://example1.com", "Example Title 1", "Example Description 1");
        URLData testData2 = new URLData("http://example2.com", "Example Title 2", "Example Description 2");
        try {
            Barrel.archiveURL(testData1);
            Barrel.archiveURL(testData2);
        } catch (RemoteException e) {
            fail("RemoteException thrown while archiving URLData");
        }

        try {

            List<URLData> result = metodosBarrelManager.searchInput("Example").getSecond();
            assertNotNull(result);
            assertEquals(2, result.size());
        } catch (RemoteException e) {
            fail("RemoteException thrown while searching input");
        }
    }
}
