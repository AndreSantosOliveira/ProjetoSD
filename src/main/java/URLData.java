import java.io.Serializable;

public class URLData implements Serializable {
    private String url;
    private String pageTitle;

    public URLData(String url, String pageTitle) {
        this.url = url;
        this.pageTitle = pageTitle;
    }

    @Override
    public String toString() {
        return "URLData{" +
                "url='" + url + '\'' +
                ", pageTitle='" + pageTitle + '\'' +
                '}';
    }

    public String getURL() {
        return this.url;
    }

    public String getPageTitle() {
        return pageTitle;
    }
}
