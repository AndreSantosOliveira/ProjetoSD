import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Downloader {

    /*
     * Classe que faz o download de uma página web e indexa as palavras chave
     * para posterior pesquisa.
     */

    static HashMap<String, HashSet<URLData>> index = new HashMap<>();

    public static void main(String[] args) {

        // TODO 1: Fetch url from URL queue
        // TODO 2: URL -> new thread -> Donwloader.crawl(url) -> URLData

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

                        // TODO 3: o título pode não conter o conteúdo essencial da página, encontrar uma maneira mais otimizada de o procurar
                        for (String s : titulo.split(" ")) {
                            s = s.toLowerCase();
                            if (s.length() > 3) {
                                if (index.containsKey(s) && !doesIndexHaveURL(s, link)) {
                                    URLData r = new URLData(link, titulo);
                                    index.get(s).add(r);
                                } else {
                                    HashSet<URLData> newHashSet = new HashSet<>(Collections.singletonList(new URLData(link, titulo)));
                                    index.put(s, newHashSet);
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

        // TODO 4: Send to ISB via multicast
        //...
        
        // Close the scanner
        scanner.close();
    }

    public static Boolean doesIndexHaveURL(String chave, String url) {
        return index.get(chave).stream().anyMatch(urlData -> urlData.getURL().equalsIgnoreCase(url));
    }

    public static void pesquisar(String pesquisa) {
        if (pesquisa.isEmpty()) {
            System.out.println("Pesquisa vazia, o problema localiza-se entre o teclado e a cadeira.");
            return;
        }

        pesquisa = pesquisa.toLowerCase();
        System.out.println("Pesquisa para a query: " + pesquisa);
        List<URLData> intersecao = new ArrayList<>();
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
            intersecao.forEach(urlData -> System.out.println(urlData.toString()));
        }
    }

    public static HashMap<String, HashSet<URLData>> sortIndexByListCountAsc() {
        // Create a stream from the entrySet of the map
        // Sort it by comparing the size of the HashSet<URLData> for each entry
        // Then collect the results into a new LinkedHashMap to preserve the sorted order

        return index.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().size()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // in case of duplicates, keep the first
                        LinkedHashMap::new
                ));
    }
}
