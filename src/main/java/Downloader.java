import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class Downloader {

    static Map<String, HashSet<URLRecord>> index = new HashMap<>();

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
                        //System.out.println(titulo + "\n" + link + "\n");

                        for (String s : titulo.split(" ")) {
                            if (s.length() > 3) {
                                if (index.containsKey(s)) {
                                    URLRecord r = new URLRecord(link, titulo, Arrays.stream(titulo.split(" ")).collect(Collectors.toList()));
                                    index.get(s).add(r);
                                } else {
                                    HashSet<URLRecord> a = new HashSet<>();
                                    a.add(new URLRecord(link, titulo, Arrays.stream(titulo.split(" ")).collect(Collectors.toList())));
                                    index.put(s, a);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Indexadas: " + index.size());
        System.out.println(index.toString());
    }

}
