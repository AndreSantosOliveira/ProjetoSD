package springboot;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Controller
public class AccountController {

    @GetMapping("/login")
    public String login() {
        // Your login logic here
        return "login"; // Redirect to success page
    }


    // Login Action
    @PostMapping("/loginForm")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password, HttpServletResponse response) {

        response.addCookie(new Cookie("username", username));
        response.addCookie(new Cookie("password", password));

        return "redirect:/";
    }

    //account types: 0 para normal, 1 para admin
    @GetMapping("/account")
    public String account(@CookieValue(value = "username", defaultValue = "-") String username,
                          @CookieValue(value = "accountType", defaultValue = "0") String accountType,
                          Model model) {
        if (username.equalsIgnoreCase("-")) {
            return "redirect:/login";
        } else {
            model.addAttribute("username", username);
            model.addAttribute("accountType", Objects.equals(accountType, "1") ? "Admin" : "Normal");
            return "account";
        }
    }

    @PostMapping("/logout")
    public String logout() {
        return "redirect:/"; // Redirect to googol page
    }
}
