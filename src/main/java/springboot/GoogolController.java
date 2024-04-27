package springboot;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GoogolController {

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/search")
	public String search(@RequestParam(name = "query", required = false, defaultValue = "") String query, Model model) {
		// Implement your search logic here with the 'query' parameter
		// For example, you can pass the 'query' parameter to your search service
		// and retrieve search results
		model.addAttribute("query", query);
		return "search";
	}

	@GetMapping("/statistics")
	public String statistics() {
		// Implement your statistics logic here
		return "statistics";
	}


}