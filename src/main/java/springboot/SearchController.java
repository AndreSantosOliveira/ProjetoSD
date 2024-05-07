package springboot;

import common.ConnectionsEnum;
import common.MetodosRMIGateway;
import common.URLData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.rmi.Naming;
import java.util.Collections;
import java.util.List;

@Controller
public class SearchController {

    @GetMapping("/search")
    public String search(@RequestParam(name = "query", required = false, defaultValue = "") String query, Model model) {
        // Implement your search logic here with the 'query' parameter
        // For example, you can pass the 'query' parameter to your search service
        // and retrieve search results
        model.addAttribute("query", query);

        try {
            // Invoke the search method on the remote Gateway service
            MetodosRMIGateway metodosGateway = null;
            metodosGateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");

            if (query.startsWith("index:")) {
                String[] parts = query.split(":");
                if (parts.length == 2) {
                    // check if parts[1] has https
                    if (parts[1].startsWith("https://")) {
                        metodosGateway.indexURLString(parts[1]);
                    } else {
                        metodosGateway.indexURLString("https://" + parts[1]);
                    }
                    model.addAttribute("searchResults", Collections.singleton(new URLData(parts[1], parts[1] + " foi enviado para indexação.", parts[1])));
                }
            } else  {
                List<URLData> searchResults = metodosGateway.search(query);
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
