package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.models.User;
import org.example.services.AdminService;
import org.example.services.MedecinService;
import org.example.services.PatientService;
import org.example.services.UserService;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

public class ResetPasswordController {

    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    @FXML private VBox alertBox;
    @FXML private Label alertIcon;
    @FXML private Button submitButton;
    @FXML private VBox emailFieldBox;
    @FXML private VBox codeFieldBox;
    @FXML private VBox passwordFieldBox;

    private User user;
    private String verificationCode;
    private UserService<?> userService;

    @FXML
    public void initialize() {
        // Initial state: show email field, hide code and password fields
        emailFieldBox.setManaged(true);
        emailFieldBox.setVisible(true);
        codeFieldBox.setManaged(false);
        codeFieldBox.setVisible(false);
        passwordFieldBox.setManaged(false);
        passwordFieldBox.setVisible(false);
        submitButton.setText("Envoyer le code");
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        if (emailFieldBox.isVisible()) {
            // Step 1: Send verification code
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                showAlert("warning", "Veuillez entrer votre email");
                return;
            }

            // Check if user exists
            user = findUserByEmail(email);
            if (user == null) {
                showAlert("danger", "Aucun compte associé à cet email");
                return;
            }

            // Generate a 6-digit verification code
            verificationCode = generateVerificationCode();

            // Send email with the code
            if (sendVerificationEmail(email, verificationCode)) {
                showAlert("success", "Un code de vérification a été envoyé à votre email.");
                emailFieldBox.setManaged(false);
                emailFieldBox.setVisible(false);
                codeFieldBox.setManaged(true);
                codeFieldBox.setVisible(true);
                submitButton.setDisable(true);
            } else {
                showAlert("danger", "Erreur lors de l'envoi de l'email. Veuillez réessayer.");
            }
        } else if (passwordFieldBox.isVisible()) {
            // Step 3: Reset password
            String newPassword = newPasswordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showAlert("warning", "Veuillez entrer et confirmer votre nouveau mot de passe");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                showAlert("warning", "Les mots de passe ne correspondent pas");
                return;
            }

            try {
                userService.updatePassword(user.getId(), newPassword);
                showAlert("success", "Mot de passe réinitialisé avec succès");
                backToLogin(null);
            } catch (Exception e) {
                showAlert("danger", "Erreur lors de la réinitialisation du mot de passe");
            }
        }
    }

    @FXML
    private void validateCode(ActionEvent event) {
        // Step 2: Validate the verification code
        String enteredCode = codeField.getText().trim();
        if (enteredCode.isEmpty()) {
            showAlert("warning", "Veuillez entrer le code de vérification");
            return;
        }

        if (enteredCode.equals(verificationCode)) {
            showAlert("success", "Code vérifié avec succès");
            codeFieldBox.setManaged(false);
            codeFieldBox.setVisible(false);
            passwordFieldBox.setManaged(true);
            passwordFieldBox.setVisible(true);
            submitButton.setText("Réinitialiser le mot de passe");
            submitButton.setDisable(false);
            initializeUserService(user.getRole());
        } else {
            showAlert("danger", "Code incorrect. Veuillez réessayer.");
            codeField.clear();
        }
    }

    private void initializeUserService(String role) {
        switch (role.toLowerCase()) {
            case "admin":
                userService = new AdminService();
                break;
            case "medecin":
                userService = new MedecinService();
                break;
            case "patient":
                userService = new PatientService();
                break;
            default:
                throw new IllegalArgumentException("Rôle inconnu: " + role);
        }
    }

    private User findUserByEmail(String email) {
        User user = new AdminService().findByEmail(email);
        if (user != null) return user;
        user = new MedecinService().findByEmail(email);
        if (user != null) return user;
        user = new PatientService().findByEmail(email);
        return user;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Generate a 6-digit code
        return String.valueOf(code);
    }

    private boolean sendVerificationEmail(String toEmail, String code) {
        final String username = "chronoserena@gmail.com";
        final String password = "kshgdipkemhwqkon"; // Replace with your 16-character app-specific password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Code de vérification pour réinitialisation de mot de passe");
            message.setText("Bonjour,\n\nVotre code de vérification pour réinitialiser votre mot de passe est : " + code +
                    "\n\nCe code expire dans 1 heure.\n\nCordialement,\nChronoSerna Team");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            showAlert("danger", "Erreur lors de l'envoi de l'email: " + e.getMessage());
            return false;
        }
    }

    @FXML
    private void backToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("danger", "Erreur lors du retour à la connexion");
        }
    }

    @FXML
    private void closeAlert() {
        alertBox.setManaged(false);
        alertBox.setVisible(false);
    }

    private void showAlert(String type, String message) {
        alertBox.setManaged(true);
        alertBox.setVisible(true);
        messageLabel.setText(message);

        switch (type) {
            case "danger":
                alertBox.setStyle("-fx-background-color: #fee8e7; -fx-border-color: #fdd6d3;");
                messageLabel.setStyle("-fx-text-fill: #d63031;");
                alertIcon.setText("❌");
                break;
            case "warning":
                alertBox.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffeeba;");
                messageLabel.setStyle("-fx-text-fill: #856404;");
                alertIcon.setText("⚠️");
                break;
            case "success":
                alertBox.setStyle("-fx-background-color: #d4edda; -fx-border-color: #c3e6cb;");
                messageLabel.setStyle("-fx-text-fill: #155724;");
                alertIcon.setText("✅");
                break;
        }
    }
}