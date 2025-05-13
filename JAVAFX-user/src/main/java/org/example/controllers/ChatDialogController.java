package org.example.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.util.GeminiChatbot;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the chat dialog window
 */
public class ChatDialogController implements Initializable {

    @FXML
    private VBox chatMessagesContainer;
    
    @FXML
    private TextField messageField;
    
    @FXML
    private Button sendButton;
    
    @FXML
    private Label typingIndicator;
    
    private GeminiChatbot chatbot;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private boolean initialized = false;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("ChatDialogController: initialize called");
        
        // Disable send button when text field is empty
        sendButton.disableProperty().bind(
                Bindings.createBooleanBinding(() -> 
                        messageField.getText().trim().isEmpty(),
                        messageField.textProperty()
                )
        );
        
        // Add event listener for enter key
        messageField.setOnAction(event -> sendMessage());
        
        // Hide typing indicator initially
        typingIndicator.setVisible(false);
    }
    
    /**
     * Initialize the chat after the dialog is shown
     * This is called from ChatBubbleController to prevent initialization errors
     */
    public void initializeChat() {
        if (initialized) {
            return;
        }
        
        System.out.println("ChatDialogController: initializeChat called");
        try {
            chatbot = new GeminiChatbot();
            // Add welcome message
            addWelcomeMessage();
            initialized = true;
        } catch (Exception e) {
            System.err.println("Error initializing chatbot: " + e.getMessage());
            e.printStackTrace();
            
            // Add error message to chat instead of failing
            Platform.runLater(() -> {
                addBotMessage("Je suis désolé, mais je ne peux pas me connecter au service de chat en ce moment. " +
                        "Veuillez vérifier votre connexion internet et réessayer plus tard.");
            });
        }
    }
    
    /**
     * Adds the welcome message from the chatbot
     */
    private void addWelcomeMessage() {
        String welcomeMessage = "Bonjour ! Je suis votre assistant ChronoSerena, propulsé par Google Gemini. " +
                "Comment puis-je vous aider aujourd'hui ?";
        addBotMessage(welcomeMessage);
    }
    
    /**
     * Handles the send button click
     */
    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // Add user message to chat
        addUserMessage(message);
        
        // Clear input field
        messageField.clear();
        
        // Show typing indicator
        typingIndicator.setVisible(true);
        
        // Handle the case where API might not be available
        if (chatbot == null) {
            // Try to initialize chatbot again
            try {
                chatbot = new GeminiChatbot();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    // Hide typing indicator
                    typingIndicator.setVisible(false);
                    
                    // Add error message
                    addBotMessage("Je suis désolé, mais je ne peux pas me connecter au service de chat en ce moment. " +
                            "Veuillez vérifier votre connexion internet et réessayer plus tard.");
                });
                return;
            }
        }
        
        try {
            // Simulate a response if API is unavailable
            CompletableFuture<String> future = chatbot.sendMessage(message)
                .exceptionally(ex -> {
                    System.err.println("Error getting chatbot response: " + ex.getMessage());
                    ex.printStackTrace();
                    // Return a fallback response
                    return "Je suis désolé, mais je ne peux pas accéder au service de réponse en ce moment. " +
                           "Voici quelques conseils généraux sur la santé: maintenez une bonne hydratation, " +
                           "pratiquez une activité physique régulière, et suivez les recommandations de votre médecin.";
                });
            
            // Handle response
            future.thenAccept(response -> {
                Platform.runLater(() -> {
                    // Hide typing indicator
                    typingIndicator.setVisible(false);
                    
                    // Add bot response to chat
                    addBotMessage(response);
                });
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                // Hide typing indicator
                typingIndicator.setVisible(false);
                
                // Add error message
                addBotMessage("Je suis désolé, mais j'ai rencontré une erreur. Veuillez réessayer.");
            });
            e.printStackTrace();
        }
    }
    
    /**
     * Adds a user message to the chat
     * 
     * @param message The message to add
     */
    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));
        
        VBox messageContainer = new VBox(5);
        messageContainer.setAlignment(Pos.CENTER_RIGHT);
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().addAll("message-bubble", "user-message");
        messageLabel.setWrapText(true);
        
        Label timestampLabel = new Label(LocalTime.now().format(timeFormatter));
        timestampLabel.getStyleClass().add("timestamp");
        
        messageContainer.getChildren().addAll(messageLabel, timestampLabel);
        messageBox.getChildren().add(messageContainer);
        
        chatMessagesContainer.getChildren().add(messageBox);
        
        // Scroll to bottom
        Platform.runLater(() -> chatMessagesContainer.layout());
    }
    
    /**
     * Adds a bot message to the chat
     * 
     * @param message The message to add
     */
    private void addBotMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));
        
        VBox messageContainer = new VBox(5);
        messageContainer.setAlignment(Pos.CENTER_LEFT);
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().addAll("message-bubble", "bot-message");
        messageLabel.setWrapText(true);
        
        Label timestampLabel = new Label(LocalTime.now().format(timeFormatter));
        timestampLabel.getStyleClass().add("timestamp");
        
        messageContainer.getChildren().addAll(messageLabel, timestampLabel);
        messageBox.getChildren().add(messageContainer);
        
        chatMessagesContainer.getChildren().add(messageBox);
        
        // Scroll to bottom
        Platform.runLater(() -> {
            chatMessagesContainer.layout();
            messageLabel.requestFocus();
        });
    }
    
    /**
     * Clears the chat history
     */
    @FXML
    private void clearChat() {
        // Clear UI
        chatMessagesContainer.getChildren().clear();
        
        // Clear chatbot conversation history
        chatbot.clearConversation();
        
        // Add welcome message again
        addWelcomeMessage();
    }
    
    /**
     * Closes the chat dialog
     */
    @FXML
    private void closeDialog() {
        Stage stage = (Stage) messageField.getScene().getWindow();
        stage.close();
    }
} 