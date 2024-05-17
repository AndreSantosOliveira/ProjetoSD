package common;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import java.io.Serializable;

/**
 * The URLData class represents a URL and its associated page title.
 * This class implements Serializable, allowing its instances to be written to an OutputStream.
 */
public class URLData implements Serializable {
    // The URL, page title, and URL where it was found
    public String url, pageTitle, urlOndeFoiEncontrado;

    // The page number
    private int pageNumber = -1;
    // The relevance of the URL
    private int relevance = 0;

    /**
     * Constructs a new URLData with the provided URL, page title, and the URL where it was found.
     *
     * @param url                  the URL
     * @param pageTitle            the page title associated with the URL
     * @param urlOndeFoiEncontrado the URL where this URL was found
     */
    public URLData(String url, String pageTitle, String urlOndeFoiEncontrado) {
        this.url = url;
        this.pageTitle = pageTitle;
        this.urlOndeFoiEncontrado = urlOndeFoiEncontrado;
    }

    /**
     * Constructs a new URLData with the provided URL, page title, and relevance.
     *
     * @param url       the URL
     * @param pageTitle the page title associated with the URL
     * @param relevance the relevance of the URL
     */
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
     * If a page number is set, it is prepended to the page title.
     *
     * @return the page title of the URLData object
     */
    public String getPageTitle() {
        return pageNumber == -1 ? pageTitle : (pageNumber + ". " + pageTitle);
    }

    /**
     * Adds a page number to the URLData object.
     *
     * @param i the page number to add
     */
    public void addPageNumber(int i) {
        this.pageNumber = i;
    }

    /**
     * Sets the relevance of the URLData object.
     *
     * @param relevance the relevance to set
     */
    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

    /**
     * Returns the relevance of the URLData object.
     *
     * @return the relevance of the URLData object
     */
    public int getRelevance() {
        return this.relevance;
    }
}