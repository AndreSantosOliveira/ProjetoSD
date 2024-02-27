import java.util.List;

public class URLRecord {
    private String url;
    private String pageTitle;

    public URLRecord(String url, String pageTitle) {
        this.url = url;
        this.pageTitle = pageTitle;
    }

    @Override
    public String toString() {
        return pageTitle + "\n> " + url;
    }

    public String getURL() {
        return this.url;
    }

    public String getPageTitle() {
        return pageTitle;
    }
}
