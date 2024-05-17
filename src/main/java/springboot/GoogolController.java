package springboot;

/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The GoogolController class is responsible for handling requests to the home page.
 * It is annotated with @Controller, meaning it is a controller in the Spring framework.
 */
@Controller
public class GoogolController {

    /**
     * Handles GET requests to the "/" endpoint.
     * It adds attributes to the model based on the user's login status and username, and then returns the home page.
     *
     * @param username the username cookie value
     * @param accountType the account type cookie value
     * @param model the Model to which attributes will be added
     * @return the name of the home page
     */
    @GetMapping("/")
    public String index(@CookieValue(value = "username", defaultValue = "-") String username,
                        @CookieValue(value = "accountType", defaultValue = "0") String accountType,
                        Model model) {

        model.addAttribute("isLoggedIn", !username.equals("-"));
        model.addAttribute("username", username);

        return "homepage";
    }
}