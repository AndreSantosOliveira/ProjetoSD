package springboot;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import common.ConnectionsEnum;
import common.MetodosRMIGateway;
import common.URLData;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.rmi.Naming;
import java.util.*;

/**
 * The SearchController class is responsible for handling search-related requests.
 * It is annotated with @Controller, meaning it is a controller in the Spring framework.
 */
@Controller
public class SearchController {


    /**
     * Splits a larger list into smaller lists.
     *
     * @param inputList   list to be split
     * @param sublistSize size of the smaller lists
     * @return a list of lists
     */
    private static List<List<URLData>> separateList(List<URLData> inputList, int sublistSize) {
        List<List<URLData>> result = new ArrayList<>();

        for (int i = 0; i < inputList.size(); ++i) {
            if (!inputList.get(i).pageTitle.equals("Trying to reconnect to the barrels..."))
                inputList.get(i).addPageNumber(i + 1);
        }

        for (int i = 0; i < inputList.size(); i += sublistSize) {
            int end = Math.min(inputList.size(), i + sublistSize);
            result.add(new ArrayList<>(inputList.subList(i, end)));
        }

        return result;
    }

    /**
     * Handles GET requests to the "/search" endpoint.
     * It performs a search based on the provided query and page number, and then returns the search page.
     *
     * @param query the search query provided in the request
     * @param page  the page number provided in the request
     * @param model the Model to which attributes will be added
     * @return the name of the search page
     */
    @GetMapping("/search")
    public String search(@RequestParam(name = "query", defaultValue = "") String query, @RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        try {
            int totalPages = 0;

            // Invoke the search method on the remote Gateway service
            MetodosRMIGateway metodosGateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");

            // If request is to index
            if (query.startsWith("index>")) {
                String[] parts = query.split(">");
                if (parts.length == 2) {
                    // check if parts[1] has https
                    if (!parts[1].startsWith("https://")) {
                        // Add "https://" to parts[1]
                        parts[1] = "https://" + parts[1];
                    }
                    metodosGateway.indexURLString(parts[1]);
                    model.addAttribute("descriptions", Collections.emptyList());
                    model.addAttribute("totalPages", totalPages);
                    model.addAttribute("query", query);
                    model.addAttribute("page", page);
                    model.addAttribute("searchResults", Collections.singleton(new URLData(parts[1], "New url (" + parts[1] + ") indexed.", parts[1])));
                } else {
                    model.addAttribute("descriptions", Collections.emptyList());
                    model.addAttribute("totalPages", totalPages);
                    model.addAttribute("query", query);
                    model.addAttribute("page", page);
                    model.addAttribute("searchResults", Collections.singleton(new URLData("Invalid URL", "Invalid URL, please insert a valid url to index.", "Invalid URL")));
                }
            } else if (query.startsWith("list>")) {
                String[] splitOption = query.split(">");


                if (splitOption.length == 2) {
                    String pesquisaLista = String.join(" ", Arrays.copyOfRange(splitOption, 1, splitOption.length));

                    if (pesquisaLista.isEmpty()) {
                        model.addAttribute("descriptions", Collections.emptyList());
                        model.addAttribute("totalPages", totalPages);
                        model.addAttribute("query", query);
                        model.addAttribute("page", page);
                        model.addAttribute("searchResults", Collections.singleton(new URLData("Invalid URL", "Invalid URL. Please enter a valid URL to list.", "Invalid URL")));
                    }

                    if (!pesquisaLista.startsWith("https://")) {
                        pesquisaLista = "https://" + pesquisaLista;
                    }

                    List<String> links = metodosGateway.linksListForURL(pesquisaLista);
                    List<URLData> linkDataList = new ArrayList<>();

                    if (links.isEmpty()) {
                        model.addAttribute("descriptions", Collections.emptyList());
                        model.addAttribute("totalPages", totalPages);
                        model.addAttribute("query", query);
                        model.addAttribute("page", page);
                        model.addAttribute("searchResults", Collections.singleton(new URLData("Invalid URL", "No links found for this URL.", "Invalid URL")));
                    } else {
                        links.sort(String::compareTo);
                        linkDataList.add(new URLData("P", "Links that reference: " + pesquisaLista, "P"));
                        int i = 0;
                        for (String link : links) {
                            linkDataList.add(new URLData(link, i + 1 + ". " + link + " ignore", link));
                            System.out.println("links " + link);
                            i++;
                        }
                        model.addAttribute("descriptions", Collections.emptyList());
                        model.addAttribute("totalPages", totalPages);
                        model.addAttribute("query", query);
                        model.addAttribute("page", page);
                        model.addAttribute("searchResults", linkDataList);
                    }
                } else {
                    model.addAttribute("descriptions", Collections.emptyList());
                    model.addAttribute("totalPages", totalPages);
                    model.addAttribute("query", query);
                    model.addAttribute("page", page);
                    model.addAttribute("searchResults", Collections.singleton(new URLData("Invalid URL", "Invalid URL, please insert a valid url to list.", "Invalid URL")));
                }

            } else {
                List<URLData> searchResults = new ArrayList<>();
                List<String> descriptions = new ArrayList<>();
                if (!query.isEmpty()) {
                    // Extract results
                    List<URLData> aux = metodosGateway.search(query);
                    // Divide by 10 results each page
                    List<List<URLData>> aux2 = separateList(aux, 10);

                    if (!aux2.isEmpty()) {
                        // Get the total number of pages
                        totalPages = aux2.size();


                        // Get the results corresponding to the requested page
                        searchResults = aux2.get(page);

                        for (URLData result : searchResults) {
                            if (descriptions.isEmpty()) {
                                try {
                                    // Fetch descriptions for each URL
                                    RestTemplate restTemplate = new RestTemplate();
                                    String apiKey = "82bad2ccb57f6a0b725638efe88c51c7";

                                    String apiUrl = "https://api.linkpreview.net?key=" + apiKey + "&q=" + result.getURL();
                                    ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
                                    JSONObject json = new JSONObject(response.getBody());
                                    String description = json.optString("description", "No description available.");
                                    if (description.length() > 100) {
                                        description = description.substring(0, 100) + "...";
                                    }
                                    descriptions.add(description);
                                } catch (HttpClientErrorException e) {
                                    if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                                        descriptions.add("Too many requests / rate limit exceeded.");
                                    } else {
                                        // Rethrow the exception if it's not a Too Many Requests error
                                        descriptions.add("");
                                        throw e;
                                    }
                                }
                            } else {
                                descriptions.add("");
                            }
                        }
                    }
                }

                model.addAttribute("descriptions", descriptions);
                model.addAttribute("totalPages", totalPages);
                model.addAttribute("query", query);
                model.addAttribute("page", page);
                model.addAttribute("searchResults", searchResults);
            }

        } catch (
                Exception e) {
            // Handle any exceptions
            e.printStackTrace();
            model.addAttribute("errorMessage", "An error occurred during search.");
        }


        return "search";
    }

}
