package springboot;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

/**
 * The WeatherController class is responsible for handling weather-related requests.
 * It is annotated with @Controller, meaning it is a controller in the Spring framework.
 * It uses the RestTemplate to make HTTP requests to the WeatherAPI.
 */
@Controller
public class WeatherController {

    private static final String API_KEY = "e41ae573abe0413ebc8151613241505";
    private static final String BASE_URL = "http://api.weatherapi.com/v1/current.json";

    private final RestTemplate restTemplate;

    /**
     * Constructs a new WeatherController with the provided RestTemplate.
     *
     * @param restTemplate the RestTemplate to be used for making HTTP requests
     */
    public WeatherController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Handles GET requests to the "/weather" endpoint.
     * It gets the current weather for the provided city (or Coimbra by default), and then returns the weather page.
     *
     * @param city         the city for which to get the weather, provided in the request
     * @param model        the Model to which attributes will be added
     * @param fromHomepage a boolean indicating whether the request came from the home page
     * @return the name of the weather page
     */
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