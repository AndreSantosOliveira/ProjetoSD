package springboot;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GoogolController {

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/statistics")
	public String statistics() {
		// Implement your statistics logic here
		return "statistics";
	}


}