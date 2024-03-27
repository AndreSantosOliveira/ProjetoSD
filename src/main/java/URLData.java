import java.io.Serializable;

/**
 * URLData is a class that represents a URL and its associated page title.
 * This class implements Serializable, allowing its instances to be written to an OutputStream.
 */
public class URLData implements Serializable {
    private String url;
    private String pageTitle;

    /**
     * Constructor for URLData.
     *
     * @param url       the URL
     * @param pageTitle the page title associated with the URL
     */
    public URLData(String url, String pageTitle) {
        this.url = url;
        this.pageTitle = pageTitle;
    }

    /**
     * Returns a string representation of the URLData object.
     *
     * @return a string representation of the URLData object
     */
    @Override
    public String toString() {
        return "URLData{" +
                "url='" + url + '\'' +
                ", pageTitle='" + pageTitle + '\'' +
                '}';
    }

    /**
     * Returns a string representation of the URLData object in the format of a data packet.
     *
     * @return a string representation of the URLData object in the format of a data packet
     */
    public String toStringDataPacket() {
        return url + "|" + pageTitle;
    }

    /**
     * Returns the URL of the URLData object.
     *
     * @return the URL of the URLData object
     */
    public String getURL() {
        return this.url;
    }

    /**
     * Returns the page title of the URLData object.
     *
     * @return the page title of the URLData object
     */
    public String getPageTitle() {
        return pageTitle;
    }
}