import java.net.URL;
import java.util.List;

public class URLRecord {

    private String url;
    private String pageTitle, textCitation;
    private List<String> words;

    public URLRecord(String url, String pageTitle, String textCitation, List<String> words) {
        this.url = url;
        this.pageTitle = pageTitle;
        this.textCitation = textCitation;
        this.words = words;
    }
}
