package org.example.util;
import org.example.models.Admin;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.services.AdminService;
import org.example.services.MedecinService;
import org.example.services.PatientService;

import java.util.Date;
import java.util.List;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
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
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("issaouiameni28@gmail.com"));
            message.setSubject("Test Email");
            message.setText("This is a test email from ChronoSerna.");
            Transport.send(message);
            System.out.println("Email sent successfully");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
        }
