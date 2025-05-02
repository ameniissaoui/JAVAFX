package org.example.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PurgoMalumService {

    private static final String BASE_URL = "https://www.purgomalum.com/service/";

    /**
     * Checks if the text contains profanity using PurgoMalum API
     * @param text Text to check
     * @return true if profanity is detected, false otherwise
     */
    public boolean containsProfanity(String text) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            URL url = new URL(BASE_URL + "containsprofanity?text=" + encodedText);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();

            return "true".equals(response);
        } catch (Exception e) {
            System.err.println("Error checking profanity: " + e.getMessage());
            e.printStackTrace();
            return false; // In case of API failure, allow the report to proceed
        }
    }
}