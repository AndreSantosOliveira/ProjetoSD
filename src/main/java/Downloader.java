import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class Downloader {

    static Map<String, List<URLRecord>> index = new HashMap<>();

    public static void main (String[] args) {
        String url = "https://www.sapo.pt";
        try {
            Document doc = Jsoup.connect(url).get();
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int countTokens = 0;
            while (tokens.hasMoreElements() && countTokens++ < 100) {
                //System.out.println(tokens.nextToken().toLowerCase());
                Elements links = doc.select("a[href]");
                for (Element elementoLink : links) {
                    String titulo = elementoLink.text();
                    String link = elementoLink.attr("abs:href");

                    if (titulo.length() > 3 && link.startsWith("http")) {
                        //System.out.println(titulo + "\n" + link + "\n");

                        for (String s : titulo.split(" ")) {

                            s = s.toLowerCase();
                            if (s.length() > 3) {
                                if (index.containsKey(s) && !doesIndexHaveURL(s, link)) {
                                    URLRecord r = new URLRecord(link, titulo);
                                    index.get(s).add(r);
                                } else {
                                    List<URLRecord> a = new ArrayList<>();
                                    a.add(new URLRecord(link, titulo));
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

        /*
        index = sortIndexByListCountAsc();

        System.out.println("Top 10 palavras:");
        int ctr = 0;
        for (String s : index.keySet()) {
            System.out.println(s + " - " + index.get(s).size());
            ++ctr;
            if (ctr == 10) break;
        }

        System.out.println();

         */

        //pesquisar("SAPO");

        //pesquisa por consola
        Scanner scanner = new Scanner(System.in);

        // Prompt the user for input
        System.out.println("Pesquisar:");

        // Continuously read from console until "exit" is entered
        while (scanner.hasNext()) {
            String input = scanner.nextLine();

            // Check if the input is "exit"
            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("Exiting...");
                break; // Exit the loop
            }

            // Print the input received
            pesquisar(input);

            // Prompt again for continuity
            System.out.println("\nPesquisar:");
        }

        // Close the scanner
        scanner.close();
    }

    public static Boolean doesIndexHaveURL(String chave, String url) {
        return index.get(chave).stream().anyMatch(urlRecord -> urlRecord.getURL().equalsIgnoreCase(url));
    }

    public static void pesquisar(String pesquisa) {
        if (pesquisa.isEmpty()) {
            System.out.println("Pesquisa vazia, o problema localiza-se entre o teclado e a cadeira.");
            return;
        }

        pesquisa = pesquisa.toLowerCase();
        System.out.println("Pesquisa para a query: " + pesquisa);
        List<URLRecord> intersecao = new ArrayList<>();
        boolean isFirstWord = true;

        String[] palavras = pesquisa.split(" ");

        for (String palavra : palavras) {
            for (String key : index.keySet()) {
                if (key.contains(palavra)) {
                    intersecao.addAll(index.get(key));
                }
            }
        }

        intersecao = intersecao.stream().distinct().collect(Collectors.toList());

        if (intersecao.isEmpty()) {
            System.out.println("Sem resultados para essa query.");
        } else {
            System.out.println(intersecao.size() + " resultados:");
            intersecao.forEach(urlRecord -> System.out.println(urlRecord.toString()));
        }
    }

    public static Map<String, List<URLRecord>> sortIndexByListCountAsc() {
        // Create a stream from the entrySet of the map
        // Sort it by comparing the size of the List<URLRecord> for each entry
        // Then collect the results into a new LinkedHashMap to preserve the sorted order
        return index.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, List<URLRecord>> entry) -> entry.getValue().size()).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // in case of duplicates, keep the first
                        LinkedHashMap::new
                ));
    }
}
