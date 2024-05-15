/*
    ____  ____
   / ___||  _ \     Projeto de Sistemas Distribuídos
   \___ \| | | |    Meta 2 - LEI FCTUC 2024
    ___) | |_| |    José Rodrigues - 2021235353
   |____/|____/     André Oliveira - 2021226714

*/

import common.ConnectionsEnum;
import common.URLData;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class HackerNewsThread extends Thread {

    private Gateway gateway;

    public HackerNewsThread(Gateway gateway) {
        this.gateway = gateway;
    }

    private Set<String> topStoryIds = new HashSet<>();

    @Override
    public void run() {
        while (true) {
            try {
                fetchTopStories();
                Thread.sleep(300000); // esperar 5m
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchTopStories() {
        try {
            URL url = new URL("https://hacker-news.firebaseio.com/v0/topstories.json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();

            String[] currentTopStoryIds = response.toString().replaceAll("[\\[\\]\"]", "").split(",");

            for (int i = 0; i < Math.min(30, currentTopStoryIds.length); i++) {
                String storyId = currentTopStoryIds[i];
                if (!topStoryIds.contains(storyId)) {
                    topStoryIds.add(storyId);
                    fetchStoryDetails(Integer.parseInt(storyId));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchStoryDetails(int storyId) throws IOException {
        URL url = new URL("https://hacker-news.firebaseio.com/v0/item/" + storyId + ".json");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        connection.disconnect();

        // load json
        JSONObject obj = new JSONObject(response.toString());
        System.out.println(obj.get("title") + " - " + obj.get("url") + " enviado para os barrels.");

        sendResultToISBviaMulticast(Collections.singletonList(new URLData(obj.getString("url"), obj.getString("title"), "https://news.ycombinator.com")));
    }

    /**
     * Sends the result to ISB via multicast.
     * It creates a multicast socket and sends a datagram packet for each common.URLData object in the result.
     *
     * @param resultado the result to send
     */
    public static void sendResultToISBviaMulticast(List<URLData> resultado) {
        try {
            // Create a multicast socket
            MulticastSocket multicastSocket = new MulticastSocket();

            // Convert the message to bytes
            for (URLData data : resultado) {
                byte[] buffer = data.toStringDataPacket().getBytes();

                // Get the multicast address
                InetAddress group = InetAddress.getByName(ConnectionsEnum.MULTICAST.getIP());

                // Create a datagram packet to send
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, ConnectionsEnum.MULTICAST.getPort());
                // Send the packet
                multicastSocket.send(packet);
            }

            // Close the socket
            multicastSocket.close();
        } catch (IOException e) {
            System.out.println("Error sending multicast message: " + e.getMessage());
        }
    }
}