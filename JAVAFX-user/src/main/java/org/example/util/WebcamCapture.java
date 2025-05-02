package org.example.util;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class WebcamCapture {
    private static WebcamCapture instance;
    private VideoCapture capture;
    private final ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
    private boolean running = false;
    private Thread captureThread;
    private boolean opencvAvailable = false;

    private WebcamCapture() {
        try {
            // Try to load OpenCV library
            loadOpenCV();

            // If we get here, OpenCV is available
            opencvAvailable = true;
        } catch (UnsatisfiedLinkError | Exception e) {
            System.err.println("Failed to load OpenCV native library: " + e.getMessage());
            System.err.println("Make sure the OpenCV library is in your java.library.path");
            System.err.println("Current java.library.path: " + System.getProperty("java.library.path"));

            // OpenCV is not available, but we won't throw an exception here
            // We'll handle this gracefully in the methods that need OpenCV
            opencvAvailable = false;
        }
    }

    public static WebcamCapture getInstance() {
        if (instance == null) {
            instance = new WebcamCapture();
        }
        return instance;
    }

    public ObjectProperty<Image> imageProperty() {
        return imageProperty;
    }

    public boolean startCamera(int deviceId) {
        if (!opencvAvailable) {
            showOpenCVMissingAlert();
            return false;
        }

        if (running) {
            return true; // Camera is already running
        }

        try {
            // Initialize the camera
            capture = new VideoCapture();
            boolean opened = capture.open(deviceId);

            if (!opened) {
                System.err.println("Failed to open camera device " + deviceId);
                return false;
            }

            // Start the camera thread
            running = true;
            captureThread = new Thread(this::captureLoop);
            captureThread.setDaemon(true);
            captureThread.start();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stopCamera() {
        running = false;

        if (captureThread != null) {
            try {
                captureThread.join(1000); // Wait for the thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (capture != null) {
            capture.release();
            capture = null;
        }
    }

    public Image captureImage() {
        if (!opencvAvailable || capture == null || !running) {
            return null;
        }

        Mat frame = new Mat();
        boolean success = capture.read(frame);

        if (!success || frame.empty()) {
            return null;
        }

        return matToImage(frame);
    }

    private void captureLoop() {
        Mat frame = new Mat();

        while (running) {
            try {
                boolean success = capture.read(frame);

                if (success && !frame.empty()) {
                    Image image = matToImage(frame);

                    Platform.runLater(() -> {
                        imageProperty.set(image);
                    });
                }

                // Sleep a bit to reduce CPU usage
                Thread.sleep(30);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private Image matToImage(Mat frame) {
        try {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", frame, buffer);
            byte[] imageData = buffer.toArray();

            ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
            return new Image(bis);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadOpenCV() {
        try {
            // First try the standard way
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            // If that fails, try to extract and load the library from resources
            try {
                extractAndLoadOpenCVFromResources();
            } catch (Exception ex) {
                throw new UnsatisfiedLinkError("Could not load OpenCV library: " + ex.getMessage());
            }
        }
    }

    private void extractAndLoadOpenCVFromResources() throws IOException {
        // Determine the correct library name based on OS
        String osName = System.getProperty("os.name").toLowerCase();
        String libName;
        String resourcePath;

        if (osName.contains("win")) {
            libName = "opencv_java460.dll";
            resourcePath = "/opencv/windows/" + libName;
        } else if (osName.contains("mac")) {
            libName = "libopencv_java460.dylib";
            resourcePath = "/opencv/macos/" + libName;
        } else {
            // Assume Linux
            libName = "libopencv_java460.so";
            resourcePath = "/opencv/linux/" + libName;
        }

        // Create a temporary directory if it doesn't exist
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "opencv_lib");
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        // Extract the library to the temporary directory
        Path libPath = tempDir.resolve(libName);

        // Only extract if the file doesn't exist
        if (!Files.exists(libPath)) {
            try (var inputStream = WebcamCapture.class.getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    throw new IOException("Could not find OpenCV library in resources: " + resourcePath);
                }
                Files.copy(inputStream, libPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // Make the library executable on Unix-like systems
        if (!osName.contains("win")) {
            File libFile = libPath.toFile();
            libFile.setExecutable(true);
        }

        // Load the library
        System.load(libPath.toString());
    }

    private void showOpenCVMissingAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("OpenCV Not Available");
            alert.setHeaderText("Face Recognition Unavailable");
            alert.setContentText("The OpenCV library required for face recognition is not available.\n\n" +
                    "To use face recognition, please install OpenCV:\n\n" +
                    "1. Download OpenCV from https://opencv.org/releases/\n" +
                    "2. Extract the files\n" +
                    "3. Add the 'bin' directory to your PATH environment variable\n" +
                    "4. Restart the application\n\n" +
                    "Alternatively, you can use password authentication.");
            alert.showAndWait();
        });
    }
}