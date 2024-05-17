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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.rmi.Naming;
import java.util.Objects;

/**
 * The AccountController class is responsible for handling account-related requests.
 * It is annotated with @Controller, meaning it is a controller in the Spring framework.
 */
@Controller
public class AccountController {

    /**
     * Handles GET requests to the "/login" endpoint.
     * It returns the login page.
     *
     * @return the name of the login page
     */
    @GetMapping("/login")
    public String login() {
        // Your login logic here
        return "login"; // Redirect to success page
    }


    /**
     * Handles POST requests to the "/loginForm" endpoint.
     * It authenticates the user and sets cookies if the authentication is successful.
     *
     * @param username the username provided in the request
     * @param password the password provided in the request
     * @param response the HttpServletResponse to which cookies will be added
     * @param model    the Model to which attributes will be added
     * @return the name of the page to which the user will be redirected
     */
    @PostMapping("/loginForm")
    public String loginForm(@RequestParam("username") String username,
                            @RequestParam("password") String password, HttpServletResponse response, Model model) {

        try {
            // Invoke the search method on the remote Gateway service
            MetodosRMIGateway metodosGateway = (MetodosRMIGateway) Naming.lookup("rmi://" + ConnectionsEnum.GATEWAY.getIP() + ":" + ConnectionsEnum.GATEWAY.getPort() + "/gateway");

            int resultado = metodosGateway.autenticarCliente(username, password);

            if (resultado != -1) {
                response.addCookie(new Cookie("username", username));
                response.addCookie(new Cookie("password", password));
                response.addCookie(new Cookie("accountType", resultado + ""));
                return "redirect:/";
            } else {
                model.addAttribute("resultadoOperacao", "Invalid username or password!");
                return "login";
            }

        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace();
            model.addAttribute("errorMessage", "An error occurred during search.");
        }

        return "login";
    }

    /**
     * Handles GET requests to the "/account" endpoint.
     * It returns the account page if the user is logged in, or redirects to the login page if the user is not logged in.
     *
     * @param username    the username cookie value
     * @param accountType the account type cookie value
     * @param model       the Model to which attributes will be added
     * @return the name of the page to which the user will be redirected
     */
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

    /**
     * Handles POST requests to the "/logout" endpoint.
     * It logs out the user and redirects to the home page.
     *
     * @param response the HttpServletResponse to which cookies will be added
     * @return the name of the page to which the user will be redirected
     */
    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        response.addCookie(new Cookie("accountType", "0"));
        return "redirect:/"; // Redirect to googol page
    }

    /**
     * Handles GET requests to the "/admin" endpoint.
     * It returns the admin page.
     *
     * @param accountType the account type cookie value
     * @param model       the Model to which attributes will be added
     * @return the name of the admin page
     */
    @GetMapping("/admin")
    public String adminPage(@CookieValue(value = "accountType", defaultValue = "0") String accountType, Model model) {
        model.addAttribute("tipoConta", accountType);
        return "admin";
    }

    /**
     * Handles GET requests to the "/gato" endpoint.
     * It returns the gato page with a random cat image.
     *
     * @param model the Model to which attributes will be added
     * @return the name of the gato page
     */
    @GetMapping("/gato")
    public String gato(Model model) {
        model.addAttribute("imageUrl", "https://cataas.com/cat");
        return "gato";
    }
}