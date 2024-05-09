package springboot;

import common.ConnectionsEnum;
import common.MetodosRMIGateway;
import common.URLData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            inputList.get(i).addPageNumber(i + 1);
        }

        for (int i = 0; i < inputList.size(); i += sublistSize) {
            int end = Math.min(inputList.size(), i + sublistSize);
            result.add(new ArrayList<>(inputList.subList(i, end)));
        }

        return result;
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "query", defaultValue = "") String query,
                         @RequestParam(name = "page", defaultValue = "0") int page,
                         Model model) {

        try {
            // Invoke the search method on the remote Gateway service
            MetodosRMIGateway metodosGateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");

            // If request is to index
            if (query.startsWith("index:")) {
                String[] parts = query.split(":");
                if (parts.length == 2) {
                    // check if parts[1] has https
                    if (!parts[1].startsWith("https://")) {
                        // Add "https://" to parts[1]
                        parts[1] = "https://" + parts[1];
                    }
                    metodosGateway.indexURLString(parts[1]);

                    model.addAttribute("searchResults", Collections.singleton(new URLData(parts[1], "New url (" + parts[1] + ") indexed.", parts[1])));
                }
            } else {

                // Extract results
                List<URLData> aux = metodosGateway.search(query);
                // Divide by 10 results each page
                List<List<URLData>> aux2 = separateList(aux, 10);

                List<URLData> searchResults = new ArrayList<>();
                if (!aux2.isEmpty()) {
                    // Get the results corresponding to the requested page
                    searchResults = aux2.get(page);
                }

                model.addAttribute("query", query);
                model.addAttribute("page", page);
                model.addAttribute("searchResults", searchResults);
            }

        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace();
            model.addAttribute("errorMessage", "An error occurred during search.");
        }

        return "search";
    }

}
