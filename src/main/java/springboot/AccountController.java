package springboot;

import common.ConnectionsEnum;
import common.MetodosRMIGateway;
import common.URLData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.rmi.Naming;
import java.util.List;
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
    public String loginForm(@RequestParam("username") String username,
                        @RequestParam("password") String password, HttpServletResponse response, Model model) {

        try {
            // Invoke the search method on the remote Gateway service
            MetodosRMIGateway metodosGateway = null;
            metodosGateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");

            int resultado = metodosGateway.autenticarCliente(username, password);

            if (resultado != -1) {
                response.addCookie(new Cookie("username", resultado+""));
                response.addCookie(new Cookie("username", username));
                response.addCookie(new Cookie("password", password));
                return "redirect:/";
            } else {
                model.addAttribute("resultadoOperacao", "Invalid username or password.");
                return "login";
            }

        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace();
            model.addAttribute("errorMessage", "An error occurred during search.");
        }

        return "login";
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
