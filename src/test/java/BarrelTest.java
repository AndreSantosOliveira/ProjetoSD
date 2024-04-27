import common.URLData;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains unit tests for the Barrel class.
 */
public class BarrelTest {
    // The Barrel instance used for testing
    static Barrel metodosBarrelManager;

    // Initialize the Barrel instance
    static {
        try {
            metodosBarrelManager = new Barrel();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This test checks the archiveURL method of the Barrel class.
     * It creates a common.URLData instance, archives it using the Barrel class,
     * and then checks if the common.URLData instance was correctly archived.
     */
    @Test
    void archiveURLTest() {
        URLData testData = new URLData("http://example.com", "Example Title", "Example Description");
        try {
            Barrel.archiveURL(testData);
        } catch (RemoteException e) {
            fail("RemoteException thrown while archiving common.URLData");
        }

        assertTrue(Barrel.index.containsKey("example"));
        assertTrue(Barrel.index.get("example").contains(testData));
    }

    /**
     * This test checks the searchInput method of the Barrel class.
     * It creates two common.URLData instances, archives them using the Barrel class,
     * and then checks if the searchInput method correctly returns the archived common.URLData instances.
     */
    @Test
    void searchInputTest() {
        Barrel.index.clear();
        URLData testData1 = new URLData("http://example1.com", "Example Title 1", "Example Description 1");
        URLData testData2 = new URLData("http://example2.com", "Example Title 2", "Example Description 2");
        try {
            Barrel.archiveURL(testData1);
            Barrel.archiveURL(testData2);
        } catch (RemoteException e) {
            fail("RemoteException thrown while archiving common.URLData");
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