package org.example.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for interacting with the Google Gemini AI API for chatbot functionality
 */
public class GeminiChatbot {
    
    // Google Gemini API key and endpoint
    private static final String API_KEY = "AIzaSyDFHoRC5VyXGDZltsrNzYwBwe0sAJ-Is-I";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    
    // Store conversation messages for context
    private List<JSONObject> conversationHistory = new ArrayList<>();
    private final boolean isOfflineMode;
    
    /**
     * Initializes the chatbot with a system message
     */
    public GeminiChatbot() {
        // Add initial system message
        JSONObject systemMessage = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        part.put("text", "Vous êtes un assistant de santé pour l'application ChronoSerena. " +
                "Vous aidez les utilisateurs avec des conseils de santé généraux, des informations sur l'application, " +
                "et vous répondez aux questions sur la nutrition, l'exercice, et le bien-être. " +
                "Gardez vos réponses concises, précises et professionnelles. " +
                "Rappelez aux utilisateurs de consulter un professionnel de santé pour des conseils médicaux spécifiques.");
        parts.put(part);
        systemMessage.put("role", "system");
        systemMessage.put("parts", parts);
        conversationHistory.add(systemMessage);
        
        // Check if we can connect to the API on startup
        boolean canConnect = testConnection();
        this.isOfflineMode = !canConnect;
        if (isOfflineMode) {
            System.out.println("GeminiChatbot: Running in offline mode - API not available");
        } else {
            System.out.println("GeminiChatbot: API connection successful");
        }
    }
    
    /**
     * Tests the connection to the Gemini API
     * 
     * @return true if the connection is successful, false otherwise
     */
    private boolean testConnection() {
        try {
            // Create a simple request with tight timeout
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)  // 5 seconds
                    .setSocketTimeout(5000)   // 5 seconds
                    .build();
            
            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .build()) {
                
                // Add API key as query parameter
                String url = API_URL + "?key=" + API_KEY;
                HttpPost request = new HttpPost(url);
                
                // Set headers
                request.setHeader("Content-Type", "application/json");
                
                // Create a simple test request that follows the Gemini API format
                JSONObject requestBody = new JSONObject();
                JSONArray contents = new JSONArray();
                
                // Create a simple user message - only include text part without role for simplest test
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", "Hello");
                parts.put(part);
                content.put("parts", parts);
                
                contents.put(content);
                requestBody.put("contents", contents);
                
                // Set request body - keep it minimal for test
                String jsonRequestBody = requestBody.toString();
                System.out.println("Test request: " + jsonRequestBody);
                request.setEntity(new StringEntity(jsonRequestBody, "UTF-8"));
                
                // Execute request
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    System.out.println("Test connection status code: " + statusCode);
                    
                    if (statusCode == 200) {
                        return true;
                    } else {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            String result = EntityUtils.toString(entity);
                            System.out.println("Test connection response: " + result);
                        }
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("GeminiChatbot: Connection test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Sends a message to the Gemini API and returns the response asynchronously
     * 
     * @param userMessage The message from the user
     * @return A CompletableFuture that will contain the AI's response
     */
    public CompletableFuture<String> sendMessage(String userMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create a user message object
                JSONObject message = new JSONObject();
                message.put("role", "user");
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", userMessage);
                parts.put(part);
                message.put("parts", parts);
                
                // Add to conversation history
                conversationHistory.add(message);
                
                String response;
                
                // If we're in offline mode, provide a predefined response
                if (isOfflineMode) {
                    response = getOfflineResponse(userMessage);
                } else {
                    // Send request to Gemini API
                    try {
                        response = callGeminiAPI();
                    } catch (Exception e) {
                        // If API call fails, fallback to offline response
                        System.err.println("GeminiChatbot: API call failed, using offline response: " + e.getMessage());
                        e.printStackTrace();
                        response = getOfflineResponse(userMessage);
                    }
                }
                
                // Create and add model response to history
                JSONObject assistantMessage = new JSONObject();
                assistantMessage.put("role", "model");
                JSONArray responseParts = new JSONArray();
                JSONObject responsePart = new JSONObject();
                responsePart.put("text", response);
                responseParts.put(responsePart);
                assistantMessage.put("parts", responseParts);
                conversationHistory.add(assistantMessage);
                
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return "Désolé, je n'ai pas pu obtenir une réponse. Veuillez réessayer.";
            }
        });
    }
    
    /**
     * Provides an offline response when the API is not available
     */
    private String getOfflineResponse(String userMessage) {
        userMessage = userMessage.toLowerCase();
        
        if (userMessage.contains("bonjour") || userMessage.contains("salut") || userMessage.contains("hello")) {
            return "Bonjour ! Je suis votre assistant ChronoSerena. Je fonctionne actuellement en mode hors ligne, mais je peux quand même vous aider avec des informations générales.";
        }
        
        if (userMessage.contains("eau") || userMessage.contains("boire") || userMessage.contains("hydrat")) {
            return "Rester bien hydraté est essentiel pour votre santé. Il est recommandé de boire environ 1,5 à 2 litres d'eau par jour, mais cela peut varier selon votre activité physique et le climat.";
        }
        
        if (userMessage.contains("exercice") || userMessage.contains("sport") || userMessage.contains("activité")) {
            return "L'exercice régulier est crucial pour maintenir une bonne santé. Les recommandations générales suggèrent au moins 150 minutes d'activité modérée par semaine, comme la marche rapide, ou 75 minutes d'activité intense.";
        }
        
        if (userMessage.contains("dormir") || userMessage.contains("sommeil") || userMessage.contains("nuit")) {
            return "Un bon sommeil est essentiel pour votre santé. Les adultes devraient viser 7-9 heures de sommeil par nuit. Essayez de maintenir un horaire de sommeil régulier et créez un environnement propice au repos.";
        }
        
        if (userMessage.contains("nutrition") || userMessage.contains("alimentation") || userMessage.contains("manger")) {
            return "Une alimentation équilibrée devrait inclure des fruits, des légumes, des protéines maigres, des grains entiers et des graisses saines. Limitez la consommation de sucres ajoutés, de sel et de graisses saturées.";
        }
        
        if (userMessage.contains("stress") || userMessage.contains("anxiété") || userMessage.contains("tension")) {
            return "La gestion du stress est importante pour votre bien-être général. Des techniques comme la méditation, la respiration profonde, l'exercice régulier et maintenir des relations sociales positives peuvent aider à réduire le stress.";
        }
        
        // Default response
        return "Je suis désolé, je fonctionne actuellement en mode hors ligne avec des réponses limitées. Pour des conseils de santé personnalisés, veuillez consulter un professionnel de la santé.";
    }
    
    /**
     * Makes the API call to Gemini
     * 
     * @return The assistant's response text
     * @throws IOException If there is an error communicating with the API
     */
    private String callGeminiAPI() throws IOException {
        // Set timeout for API calls
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000)  // 10 seconds
                .setSocketTimeout(30000)   // 30 seconds
                .build();
        
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            
            String url = API_URL + "?key=" + API_KEY;
            HttpPost request = new HttpPost(url);
            
            // Set headers
            request.setHeader("Content-Type", "application/json");
            
            // Create request body according to Gemini API format
            JSONObject requestBody = new JSONObject();
            
            // Simplify the request to match the quickstart example
            JSONArray contents = new JSONArray();
            
            // Only send the current user message to avoid context limitations
            JSONObject lastUserMessage = null;
            for (int i = conversationHistory.size() - 1; i >= 0; i--) {
                JSONObject message = conversationHistory.get(i);
                if ("user".equals(message.getString("role"))) {
                    lastUserMessage = message;
                    break;
                }
            }
            
            if (lastUserMessage != null) {
                // Convert user message to simpler format
                JSONObject content = new JSONObject();
                JSONArray parts = lastUserMessage.getJSONArray("parts");
                content.put("parts", parts);
                contents.put(content);
            } else {
                // Fallback if no user message found
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", "Hello");
                parts.put(part);
                content.put("parts", parts);
                contents.put(content);
            }
            
            requestBody.put("contents", contents);
            
            // Set request body
            String jsonRequestBody = requestBody.toString();
            System.out.println("GeminiChatbot: Sending request to API: " + jsonRequestBody);
            request.setEntity(new StringEntity(jsonRequestBody, "UTF-8"));
            
            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("GeminiChatbot: Received response with status code: " + statusCode);
                
                HttpEntity entity = response.getEntity();
                
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    System.out.println("GeminiChatbot: Response body: " + result);
                    
                    try {
                        JSONObject jsonResponse = new JSONObject(result);
                        
                        // Extract and return the content from Gemini response
                        if (jsonResponse.has("candidates") && !jsonResponse.isNull("candidates")) {
                            JSONArray candidates = jsonResponse.getJSONArray("candidates");
                            if (candidates.length() > 0) {
                                JSONObject candidate = candidates.getJSONObject(0);
                                JSONObject content = candidate.getJSONObject("content");
                                JSONArray parts = content.getJSONArray("parts");
                                if (parts.length() > 0) {
                                    JSONObject part = parts.getJSONObject(0);
                                    return part.getString("text");
                                }
                            }
                        } else if (jsonResponse.has("error")) {
                            // Handle API error
                            JSONObject error = jsonResponse.getJSONObject("error");
                            String errorMessage = error.optString("message", "Unknown API error");
                            throw new IOException("Gemini API error: " + errorMessage);
                        }
                        
                        throw new IOException("Unexpected API response format");
                    } catch (JSONException e) {
                        System.err.println("GeminiChatbot: JSON parsing error: " + e.getMessage());
                        throw new IOException("Error parsing API response: " + e.getMessage(), e);
                    }
                }
                
                throw new IOException("Empty response from API");
            }
        } catch (Exception e) {
            System.err.println("GeminiChatbot: API call exception: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Erreur lors de l'appel à l'API Gemini: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clears the conversation history except for the initial system message
     */
    public void clearConversation() {
        JSONObject systemMessage = conversationHistory.get(0);
        conversationHistory.clear();
        conversationHistory.add(systemMessage);
    }
} 