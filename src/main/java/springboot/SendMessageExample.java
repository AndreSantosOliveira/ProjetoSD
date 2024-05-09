package springboot;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendMessageExample {

    public static void main(String[] args) {
        try {
            // Endpoint URL
            URL url = new URL("http://localhost:8080/api/send-message");

            // Open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method
            connection.setRequestMethod("POST");

            // Set headers
            connection.setRequestProperty("Content-Type", "application/json");

            // Enable output and set content length
            connection.setDoOutput(true);

            // JSON data to send
            String jsonData = "teste";

            // Write JSON data to output stream
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Close connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
