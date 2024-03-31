import java.io.Serializable;

/**
 * URLData is a class that represents a URL and its associated page title.
 * This class implements Serializable, allowing its instances to be written to an OutputStream.
 */
public class URLData implements Serializable {
    private String url, pageTitle, urlOndeFoiEncontrado;
    private int pageNumber = -1;
    private int relevance = 0;

    /**
     * Constructor for URLData.
     *
     * @param url       the URL
     * @param pageTitle the page title associated with the URL
     */
    public URLData(String url, String pageTitle, String urlOndeFoiEncontrado) {
        this.url = url;
        this.pageTitle = pageTitle;
        this.urlOndeFoiEncontrado = urlOndeFoiEncontrado;
    }

    public URLData(String url, String pageTitle, int relevance) {
        this.url = url;
        this.pageTitle = pageTitle;
        this.relevance = relevance;
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
        return url + "§±" + pageTitle + "§±" + urlOndeFoiEncontrado;
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
     * Returns the URL where the URLData object was found.
     *
     * @return the URL where the URLData object was found
     */
    public String getURLWhereItWasFound() {
        return this.urlOndeFoiEncontrado;
    }

    /**
     * Returns the page title of the URLData object.
     *
     * @return the page title of the URLData object
     */
    public String getPageTitle() {
        return pageNumber == -1 ? pageTitle : (pageNumber + ". " + pageTitle);
    }

    public void addPageNumber(int i) {
        this.pageNumber = i;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

    public int getRelevance() {
        return this.relevance;
    }
}