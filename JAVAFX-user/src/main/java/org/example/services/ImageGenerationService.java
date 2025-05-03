package org.example.services;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

public class ImageGenerationService {

    // Replace with your actual API key
    private static final String API_KEY = "sk-QFPkjhkHuE7XAwiXqphALVjJmaKklerdD76I42rIGjq5Ehu1";
    private static final String API_URL = "https://api.stability.ai/v1/generation/stable-diffusion-xl-1024-v1-0/text-to-image";

    // Define the image directory path
    private final String IMAGE_DIR;

    public ImageGenerationService(String imageDir) {
        this.IMAGE_DIR = imageDir;

        // Make sure the image directory exists
        File directory = new File(IMAGE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public String generateImage(String productName, String productDescription) throws IOException {
        // Create a prompt based on product name and description
        String prompt = "High quality product image of " + productName;
        if (productDescription != null && !productDescription.isEmpty()) {
            prompt += ", " + productDescription;
        }

        // Create JSON payload
        JSONObject payload = new JSONObject();
        payload.put("text_prompts", new JSONObject[]{
                new JSONObject().put("text", prompt).put("weight", 1)
        });
        payload.put("cfg_scale", 7);
        payload.put("height", 1024);
        payload.put("width", 1024);
        payload.put("samples", 1);
        payload.put("steps", 30);

        // Create HTTP client and request
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API_URL);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader("Authorization", "Bearer " + API_KEY);

            request.setEntity(new StringEntity(payload.toString()));

            // Execute request
            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);

                // Parse response
                JSONObject jsonResponse = new JSONObject(result);

                // Get image data
                String base64Image = jsonResponse.getJSONArray("artifacts")
                        .getJSONObject(0)
                        .getString("base64");

                // Save image to file
                return saveBase64Image(base64Image);
            }
        }
    }

    private String saveBase64Image(String base64Image) throws IOException {
        // Generate unique filename
        String uniqueFileName = UUID.randomUUID().toString() + ".png";
        Path targetPath = Paths.get(IMAGE_DIR + uniqueFileName);

        // Decode and save image
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        Files.write(targetPath, imageBytes);

        return uniqueFileName;
    }

    // Alternative method using a free placeholder image service if you don't have an API key
    public String generatePlaceholderImage(String productName) throws IOException {
        // Generate a unique filename
        String uniqueFileName = UUID.randomUUID().toString() + ".png";
        Path targetPath = Paths.get(IMAGE_DIR + uniqueFileName);

        // Create a URL-safe product name
        String encodedName = productName.replace(" ", "+");

        // Use a placeholder image service
        String imageUrl = "https://via.placeholder.com/800x600.png?text=" + encodedName;

        // Download the image
        try (BufferedInputStream in = new BufferedInputStream(new URL(imageUrl).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(targetPath.toFile())) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }

        return uniqueFileName;
    }
}