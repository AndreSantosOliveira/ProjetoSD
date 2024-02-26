import java.util.List;

public class URLRecord {
    private String url;
    private String pageTitle;
    private List<String> words;

    public URLRecord(String url, String pageTitle,  List<String> words) {
        this.url = url;
        this.pageTitle = pageTitle;
        this.words = words;
    }

    @Override
    public String toString() {
        return "URLRecord{" +
                "url='" + url + '\'' +
                ", pageTitle='" + pageTitle + '\'' +
                ", words=" + words +
                '}';
    }
}
