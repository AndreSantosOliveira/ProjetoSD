package springboot;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Controller
public class WeatherController {

    // Replace "YOUR_API_KEY" with your actual WeatherAPI key
    private static final String API_KEY = "e41ae573abe0413ebc8151613241505";
    private static final String BASE_URL = "http://api.weatherapi.com/v1/current.json";

    private final RestTemplate restTemplate;

    public WeatherController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Get current weather, default is Coimbra, but asks browser to get the current location
    @GetMapping("/weather")
    public String getWeather(@RequestParam(name = "city", defaultValue = "Coimbra", required = false) String city, Model model, @RequestParam(name = "fromHomepage", defaultValue = "false") boolean fromHomepage) {
        String url = BASE_URL + "?key=" + API_KEY + "&q=" + city;


        // Get the weather from weather api
        String weatherResponse = restTemplate.getForObject(url, String.class);

        // Parse the weather response
        JSONObject weatherJson = new JSONObject(weatherResponse);

        // Extract location information
        JSONObject location = weatherJson.getJSONObject("location");
        String region = location.getString("region");
        String country = location.getString("country");

        // Extract current weather information
        JSONObject current = weatherJson.getJSONObject("current");
        double temperature = current.getDouble("temp_c");
        int humidity = current.getInt("humidity");
        String condition = current.getJSONObject("condition").getString("text");

        // Add weather data to the model
        model.addAttribute("fromHomepage", fromHomepage);
        model.addAttribute("city", city);
        model.addAttribute("region", region);
        model.addAttribute("country", country);
        model.addAttribute("temperature", temperature);
        model.addAttribute("humidity", humidity);
        model.addAttribute("condition", condition);

        // Return the Thymeleaf template name
        return "weather";
    }
}
