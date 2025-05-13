package org.example.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GeminiAIService {

    // API Constants
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    // You can replace this with your actual API key directly in the code
    // For production, consider using environment variables or a secure vault
    private static final String API_KEY = "AIzaSyCEmbz1R3S6zcI9VyZdNcUGHq9lUvANIuc";

    // Client configuration
    private final OkHttpClient client;
    private final Gson gson;

    // Singleton instance
    private static GeminiAIService instance;

    private GeminiAIService() {
        // Configure OkHttp client with reasonable timeouts
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Get singleton instance of the service
     */
    public static synchronized GeminiAIService getInstance() {
        if (instance == null) {
            instance = new GeminiAIService();
        }
        return instance;
    }

    /**
     * Extract text from a PDF file
     */
    public String extractTextFromPDF(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            System.err.println("Error extracting text from PDF: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Generate a medical comment based on the extracted text from a PDF
     */
    public CompletableFuture<String> generateMedicalComment(String pdfText, String patientName, String patientDiagnosis) {
        CompletableFuture<String> future = new CompletableFuture<>();

        try {
            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();

            // Create a detailed prompt with medical context
            String prompt = "Tu es un médecin professionnel qui examine un rapport médical. " +
                    "Nom du patient: " + patientName + "\n" +
                    "Diagnostic actuel: " + patientDiagnosis + "\n\n" +
                    "Voici le texte extrait du rapport médical (bilan) du patient. " +
                    "Analyse-le et fournis un commentaire de suivi médical professionnel. " +
                    "Le commentaire doit faire entre 200-400 caractères, inclure les principales conclusions du rapport, " +
                    "toute valeur ou résultat préoccupant, et une brève recommandation pour les prochaines étapes. " +
                    "Utilise un ton médical formel et professionnel, adapté à un dossier médical.\n\n" +
                    "Texte du rapport:\n" + pdfText;

            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            // Add generation config
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.2);
            generationConfig.addProperty("topK", 40);
            generationConfig.addProperty("topP", 0.95);
            generationConfig.addProperty("maxOutputTokens", 800);
            requestBody.add("generationConfig", generationConfig);

            // Create HTTP request
            Request request = new Request.Builder()
                    .url(API_URL + "?key=" + API_KEY)
                    .post(RequestBody.create(
                            requestBody.toString(),
                            MediaType.parse("application/json")
                    ))
                    .build();

            // Execute asynchronously
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    future.completeExceptionally(new IOException("Échec de la communication avec l'API Gemini: " + e.getMessage(), e));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful() || responseBody == null) {
                            future.completeExceptionally(new IOException("Réponse inattendue de l'API Gemini: " + response));
                            return;
                        }

                        String responseJson = responseBody.string();
                        JsonObject jsonResponse = gson.fromJson(responseJson, JsonObject.class);

                        try {
                            // Parse response to get the generated text
                            String generatedText = jsonResponse
                                    .getAsJsonArray("candidates")
                                    .get(0)
                                    .getAsJsonObject()
                                    .getAsJsonObject("content")
                                    .getAsJsonArray("parts")
                                    .get(0)
                                    .getAsJsonObject()
                                    .get("text")
                                    .getAsString();

                            future.complete(generatedText);
                        } catch (Exception e) {
                            future.completeExceptionally(new IOException("Erreur d'analyse de la réponse: " + responseJson, e));
                        }
                    }
                }
            });

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Helper method to check if a file exists and is readable
     */
    public boolean isFileReadable(File file) {
        return file != null && file.exists() && file.isFile() && file.canRead();
    }

    /**
     * Helper method to get file extension
     */
    public String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    /**
     * Check if file is a PDF
     */
    public boolean isPdfFile(File file) {
        String extension = getFileExtension(file);
        return extension.equalsIgnoreCase("pdf");
    }

    /**
     * Handles binary files like images by converting them to base64
     * For future use if handling image-based reports
     */
    public String fileToBase64(File file) throws IOException {
        byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileBytes);
    }
}