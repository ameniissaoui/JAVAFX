package org.example.util;

import org.example.models.Patient;
import org.example.models.Recommandation;
import org.example.services.PatientService;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {

    // SMTP server configuration
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "yourapplication@gmail.com"; // Replace with your actual sender email
    private static final String SENDER_PASSWORD = "your-app-password"; // Replace with your app password

    /**
     * Send an email notification to a patient about a new recommendation
     * @param patientId The ID of the patient to notify
     * @param recommandation The new recommendation
     * @return true if the email was sent successfully, false otherwise
     */
    public static boolean sendRecommandationNotification(int patientId, Recommandation recommandation) {
        PatientService patientService = new PatientService();
        Patient patient = patientService.getOne(patientId);
        
        if (patient == null) {
            System.err.println("Cannot send email: Patient with ID " + patientId + " not found");
            return false;
        }
        
        String patientEmail = patient.getEmail();
        if (patientEmail == null || patientEmail.isEmpty()) {
            System.err.println("Cannot send email: Patient has no email address");
            return false;
        }
        
        return sendEmail(
                patientEmail,
                "ChronoSerna - Nouvelle recommandation médicale",
                buildRecommandationEmailContent(patient, recommandation)
        );
    }
    
    /**
     * Send an email
     * @param recipient The email address of the recipient
     * @param subject The email subject
     * @param content The email content (HTML)
     * @return true if the email was sent successfully, false otherwise
     */
    private static boolean sendEmail(String recipient, String subject, String content) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=utf-8");
            
            Transport.send(message);
            
            System.out.println("Email sent successfully to " + recipient);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Build the email content for a new recommendation
     * @param patient The patient who received the recommendation
     * @param recommandation The recommendation
     * @return The email content as HTML
     */
    private static String buildRecommandationEmailContent(Patient patient, Recommandation recommandation) {
        StringBuilder content = new StringBuilder();
        
        content.append("<html><body style='font-family: Arial, sans-serif; color: #333;'>");
        content.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>");
        
        // Header
        content.append("<div style='background-color: #3498db; color: white; padding: 15px; text-align: center; border-radius: 5px 5px 0 0;'>");
        content.append("<h1 style='margin: 0;'>Nouvelle Recommandation Médicale</h1>");
        content.append("</div>");
        
        // Greeting
        content.append("<div style='padding: 20px;'>");
        content.append("<p>Bonjour ").append(patient.getPrenom()).append(" ").append(patient.getNom()).append(",</p>");
        content.append("<p>Votre médecin a créé une nouvelle recommandation basée sur votre dernière demande. Voici les détails :</p>");
        
        // Content
        content.append("<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 15px 0;'>");
        
        // Meals
        content.append("<h2 style='color: #2c3e50; border-bottom: 1px solid #ddd; padding-bottom: 10px;'>Plan Alimentaire</h2>");
        
        content.append("<h3 style='color: #3498db; margin-bottom: 5px;'>Petit Déjeuner</h3>");
        content.append("<p>").append(recommandation.getPetit_dejeuner()).append("</p>");
        
        content.append("<h3 style='color: #3498db; margin-bottom: 5px;'>Déjeuner</h3>");
        content.append("<p>").append(recommandation.getDejeuner()).append("</p>");
        
        content.append("<h3 style='color: #3498db; margin-bottom: 5px;'>Dîner</h3>");
        content.append("<p>").append(recommandation.getDiner()).append("</p>");
        
        // Physical Activity
        if (recommandation.getActivity() != null && !recommandation.getActivity().isEmpty() 
                && !recommandation.getActivity().equalsIgnoreCase("Aucune")) {
            content.append("<h2 style='color: #2c3e50; border-bottom: 1px solid #ddd; padding-bottom: 10px; margin-top: 20px;'>Activité Physique</h2>");
            content.append("<p><strong>Type d'activité:</strong> ").append(recommandation.getActivity()).append("</p>");
            
            if (recommandation.getDuree() != null && recommandation.getDuree() > 0) {
                content.append("<p><strong>Durée recommandée:</strong> ").append(recommandation.getDuree()).append(" minutes</p>");
            }
        }
        
        // Supplements
        if (recommandation.getSupplements() != null && !recommandation.getSupplements().isEmpty()) {
            content.append("<h2 style='color: #2c3e50; border-bottom: 1px solid #ddd; padding-bottom: 10px; margin-top: 20px;'>Suppléments</h2>");
            content.append("<p>").append(recommandation.getSupplements()).append("</p>");
        }
        
        // Calories
        if (recommandation.getCalories() != null) {
            content.append("<h2 style='color: #2c3e50; border-bottom: 1px solid #ddd; padding-bottom: 10px; margin-top: 20px;'>Apport Calorique</h2>");
            content.append("<p>Apport calorique quotidien recommandé: <strong>").append(recommandation.getCalories()).append(" kcal</strong></p>");
        }
        
        content.append("</div>");
        
        // Instructions
        content.append("<p>Vous pouvez consulter votre recommandation complète et télécharger votre plan alimentaire hebdomadaire en vous connectant à l'application ChronoSerna.</p>");
        
        // Footer
        content.append("<div style='margin-top: 30px; padding-top: 15px; border-top: 1px solid #ddd; color: #666; font-size: 12px;'>");
        content.append("<p>Cet email a été envoyé automatiquement. Merci de ne pas y répondre.</p>");
        content.append("<p>© ").append(java.time.Year.now().getValue()).append(" ChronoSerna. Tous droits réservés.</p>");
        content.append("</div>");
        
        content.append("</div>");
        content.append("</div>");
        content.append("</body></html>");
        
        return content.toString();
    }
} 