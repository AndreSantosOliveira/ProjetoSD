package springboot;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GoogolController {

	@GetMapping("/")
	public String index(@CookieValue(value = "username", defaultValue = "-") String username,
						@CookieValue(value = "accountType", defaultValue = "0") String accountType,
						Model model) {

		model.addAttribute("isLoggedIn", !username.equals("-"));
		model.addAttribute("username", username);

		return "homepage";
	}
}