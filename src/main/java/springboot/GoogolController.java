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