import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.StringTokenizer;

public class Downloader {

    public static void main (String[] args) {
        String url = "https://www.sapo.pt";
        try {
            Document doc = Jsoup.connect(url).get();
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int countTokens = 0;
            while (tokens.hasMoreElements() && countTokens++ < 100) {
                System.out.println(tokens.nextToken().toLowerCase());
                Elements links = doc.select("a[href]");
                for (Element elementoLink : links) {
                    String titulo = elementoLink.text();
                    String link = elementoLink.attr("abs:href");

                    if (titulo.length() > 3 && link.startsWith("http")) {
                        System.out.println(titulo + "\n" + link + "\n");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
