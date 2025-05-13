package org.example.services;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class FaceRecognitionService {
    private static final String SERVER_URL = "http://localhost:8000";
    private static FaceRecognitionService instance;
    private Process pythonServerProcess;
    private boolean serverRunning = false;
    private boolean opencvAvailable = true;

    private FaceRecognitionService() {
        try {
            startPythonServer();
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            opencvAvailable = false;
            System.err.println("OpenCV not available: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            serverRunning = false;
        }
    }

    public static FaceRecognitionService getInstance() {
        if (instance == null) {
            instance = new FaceRecognitionService();
        }
        return instance;
    }

    private void startPythonServer() {
        try {
            // Check if Python is installed
            Process checkPython = Runtime.getRuntime().exec("python --version");
            int exitCode = checkPython.waitFor();

            if (exitCode != 0) {
                // Try with python3
                checkPython = Runtime.getRuntime().exec("python3 --version");
                exitCode = checkPython.waitFor();

                if (exitCode != 0) {
                    showAlert("Python not found", "Please install Python 3.6+ to use face recognition.");
                    serverRunning = false;
                    return;
                }
            }

            // Get the path to the Python script
            String scriptPath = new File("face_recognition_server.py").getAbsolutePath();

            // Check if the script exists
            if (!new File(scriptPath).exists()) {
                showAlert("Script not found", "The face recognition server script was not found at: " + scriptPath);
                serverRunning = false;
                return;
            }

            // Start the Python server
            ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath);
            processBuilder.redirectErrorStream(true);
            pythonServerProcess = processBuilder.start();

            // Read the output in a separate thread
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonServerProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Python server: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Wait for the server to start
            Thread.sleep(2000);

            // Check if server is running
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                int responseCode = connection.getResponseCode();
                serverRunning = (responseCode == 200);
            } catch (Exception e) {
                e.printStackTrace();
                serverRunning = false;
            }

            if (!serverRunning) {
                showAlert("Server Error", "Failed to start face recognition server. Check console for details.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to start Python server: " + e.getMessage());
            serverRunning = false;
        }
    }

    public void shutdown() {
        if (pythonServerProcess != null) {
            pythonServerProcess.destroy();
        }
    }

    public CompletableFuture<JSONObject> recognizeFace(Image image) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!opencvAvailable) {
                    JSONObject errorResult = new JSONObject();
                    errorResult.put("success", false);
                    errorResult.put("message", "OpenCV is not available. Face recognition is disabled.");
                    return errorResult;
                }

                if (!serverRunning) {
                    JSONObject errorResult = new JSONObject();
                    errorResult.put("success", false);
                    errorResult.put("message", "Face recognition server is not running");
                    return errorResult;
                }

                String base64Image = imageToBase64(image);

                URL url = new URL(SERVER_URL + "/recognize");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);

                JSONObject requestBody = new JSONObject();
                requestBody.put("image", base64Image);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        return new JSONObject(response.toString());
                    }
                } else {
                    JSONObject errorResult = new JSONObject();
                    errorResult.put("success", false);
                    errorResult.put("message", "HTTP error: " + responseCode);
                    return errorResult;
                }

            } catch (Exception e) {
                e.printStackTrace();
                JSONObject errorResult = new JSONObject();
                errorResult.put("success", false);
                errorResult.put("message", "Error: " + e.getMessage());
                return errorResult;
            }
        });
    }

    public CompletableFuture<JSONObject> registerFace(int userId, Image image) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!opencvAvailable) {
                    JSONObject errorResult = new JSONObject();
                    errorResult.put("success", false);
                    errorResult.put("message", "OpenCV is not available. Face registration is disabled.");
                    return errorResult;
                }

                if (!serverRunning) {
                    JSONObject errorResult = new JSONObject();
                    errorResult.put("success", false);
                    errorResult.put("message", "Face recognition server is not running");
                    return errorResult;
                }

                String base64Image = imageToBase64(image);

                URL url = new URL(SERVER_URL + "/register");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);

                JSONObject requestBody = new JSONObject();
                requestBody.put("user_id", userId);
                requestBody.put("image", base64Image);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        return new JSONObject(response.toString());
                    }
                } else {
                    JSONObject errorResult = new JSONObject();
                    errorResult.put("success", false);
                    errorResult.put("message", "HTTP error: " + responseCode);
                    return errorResult;
                }

            } catch (Exception e) {
                e.printStackTrace();
                JSONObject errorResult = new JSONObject();
                errorResult.put("success", false);
                errorResult.put("message", "Error: " + e.getMessage());
                return errorResult;
            }
        });
    }

    private String imageToBase64(Image image) throws IOException {
        try {
            // Convert JavaFX Image to byte array
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = bufferedImage.createGraphics();

            // Create a SwingFXUtils.fromFXImage equivalent since we can't use that directly
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    javafx.scene.paint.Color color = image.getPixelReader().getColor(x, y);
                    java.awt.Color awtColor = new java.awt.Color(
                            (float) color.getRed(),
                            (float) color.getGreen(),
                            (float) color.getBlue(),
                            (float) color.getOpacity()
                    );
                    bufferedImage.setRGB(x, y, awtColor.getRGB());
                }
            }

            g.dispose();

            // Convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            // Convert to Base64
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new IOException("Failed to convert image to Base64: " + e.getMessage(), e);
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public boolean isServerRunning() {
        return serverRunning;
    }

    public boolean isOpencvAvailable() {
        return opencvAvailable;
    }
}