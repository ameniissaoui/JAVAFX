package org.example.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import java.util.Properties;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.example.models.Event;
import org.example.models.Reservation;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class EmailService {

    // Méthode pour envoyer l'email avec lien vers PDF
    public void sendEmail(String toEmail, String subject, String body) {
        // Configuration des propriétés SMTP
        String host = "smtp.gmail.com"; // Serveur SMTP de Gmail
        final String user = "chronoserena@gmail.com"; // Ton adresse email
        final String password = "kshgdipkemhwqkon"; // Ton mot de passe email

        // Propriétés de la session
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Création d'une session avec authentification
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // Création du message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);

            // Création d'un contenu multipart pour l'email HTML
            MimeMultipart multipart = new MimeMultipart("related");

            // Partie HTML de l'email
            MimeBodyPart htmlPart = new MimeBodyPart();
            String htmlContent =
                    "<html>" +
                            "<head>" +
                            "<style>" +
                            "body { font-family: Arial, sans-serif; line-height: 1.6; }" +
                            ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                            ".header { background-color: #3fbbc0; color: white; padding: 10px; text-align: center; }" +
                            ".content { padding: 20px; background-color: #f9f9f9; }" +
                            ".button { display: inline-block; background-color: #3fbbc0; color: white; padding: 10px 20px; " +
                            "text-decoration: none; border-radius: 5px; margin-top: 20px; }" +
                            ".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }" +
                            "</style>" +
                            "</head>" +
                            "<body>" +
                            "<div class='container'>" +
                            "<div class='header'><h2>Confirmation de Réservation</h2></div>" +
                            "<div class='content'>" +
                            "<p>" + body.replace("\n", "<br/>") + "</p>" +
                            "<div style='text-align: center;'>" +
                            "<a href='GENERATE_PDF_URL' class='button'>Télécharger votre PDF</a>" +
                            "</div>" +
                            "</div>" +
                            "<div class='footer'>Merci d'avoir choisi nos services. À bientôt!</div>" +
                            "</div>" +
                            "</body>" +
                            "</html>";

            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);

            // Envoi de l'email
            Transport.send(message);
            System.out.println("Email envoyé avec succès!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour envoyer l'email avec PDF en pièce jointe (modifiée pour plusieurs PDFs)
    public void sendEmailWithPDF(String toEmail, String subject, String messageBody, Reservation reservation) {
        // Configuration des propriétés SMTP
        String host = "smtp.gmail.com";
        final String user = "chronoserena@gmail.com";
        final String password = "kshgdipkemhwqkon";

        // Propriétés de la session
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Création d'une session avec authentification
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // Création du message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);

            // Création d'un contenu multipart
            Multipart multipart = new MimeMultipart();

            // Partie texte de l'email
            MimeBodyPart textPart = new MimeBodyPart();

            // Nombre de personnes
            int nombrePersonnes = Integer.parseInt(reservation.getNbrpersonne());

            String htmlContent =
                    "<html>" +
                            "<head>" +
                            "<style>" +
                            "body { font-family: Arial, sans-serif; line-height: 1.6; }" +
                            ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                            ".header { background-color: #3fbbc0; color: white; padding: 10px; text-align: center; }" +
                            ".content { padding: 20px; background-color: #f9f9f9; }" +
                            ".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }" +
                            "</style>" +
                            "</head>" +
                            "<body>" +
                            "<div class='container'>" +
                            "<div class='header'><h2>Confirmation de Réservation</h2></div>" +
                            "<div class='content'>" +
                            "<p>" + messageBody.replace("\n", "<br/>") + "</p>" +
                            "<p>Vous trouverez ci-joint " + nombrePersonnes + " billets en PDF pour votre réservation.</p>" +
                            "</div>" +
                            "<div class='footer'>Merci d'avoir choisi nos services. À bientôt!</div>" +
                            "</div>" +
                            "</body>" +
                            "</html>";

            textPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(textPart);

            // Générer un PDF pour chaque personne
            List<File> pdfFiles = new ArrayList<>();

            for (int i = 1; i <= nombrePersonnes; i++) {
                // Génération du PDF individuel
                File pdfFile = generateIndividualPdfFile(reservation, i);
                pdfFiles.add(pdfFile);

                // Ajouter le PDF comme pièce jointe
                MimeBodyPart pdfPart = new MimeBodyPart();
                pdfPart.attachFile(pdfFile);
                pdfPart.setFileName("Billet_" + reservation.getNomreserv() + "_" + i + ".pdf");
                multipart.addBodyPart(pdfPart);
            }

            // Association du contenu multipart avec le message
            message.setContent(multipart);

            // Envoi de l'email
            Transport.send(message);
            System.out.println("Email avec " + nombrePersonnes + " PDFs envoyé avec succès!");

            // Nettoyage - supprime les fichiers PDF temporaires
            for (File file : pdfFiles) {
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Nouvelle méthode pour envoyer l'email avec PDF comprenant les détails de l'événement
    public void sendEmailWithPDF(String toEmail, String subject, String messageBody, Reservation reservation, Event event) {
        // Configuration des propriétés SMTP
        String host = "smtp.gmail.com";
        final String user = "chronoserena@gmail.com";
        final String password = "kshgdipkemhwqkon";

        // Propriétés de la session
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Création d'une session avec authentification
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // Création du message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);

            // Création d'un contenu multipart
            Multipart multipart = new MimeMultipart();

            // Partie texte de l'email
            MimeBodyPart textPart = new MimeBodyPart();

            // Nombre de personnes
            int nombrePersonnes = Integer.parseInt(reservation.getNbrpersonne());

            String htmlContent =
                    "<html>" +
                            "<head>" +
                            "<style>" +
                            "body { font-family: Arial, sans-serif; line-height: 1.6; }" +
                            ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                            ".header { background-color: #3fbbc0; color: white; padding: 10px; text-align: center; }" +
                            ".content { padding: 20px; background-color: #f9f9f9; }" +
                            ".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }" +
                            "</style>" +
                            "</head>" +
                            "<body>" +
                            "<div class='container'>" +
                            "<div class='header'><h2>Confirmation de Réservation</h2></div>" +
                            "<div class='content'>" +
                            "<p>" + messageBody.replace("\n", "<br/>") + "</p>" +
                            "<p>Vous trouverez ci-joint " + nombrePersonnes + " billets en PDF pour votre réservation.</p>" +
                            "</div>" +
                            "<div class='footer'>Merci d'avoir choisi nos services. À bientôt!</div>" +
                            "</div>" +
                            "</body>" +
                            "</html>";

            textPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(textPart);

            // Générer un PDF pour chaque personne
            List<File> pdfFiles = new ArrayList<>();

            for (int i = 1; i <= nombrePersonnes; i++) {
                // Génération du PDF individuel avec les informations de l'événement
                File pdfFile = generateIndividualPdfFile(reservation, event, i);
                pdfFiles.add(pdfFile);

                // Ajouter le PDF comme pièce jointe
                MimeBodyPart pdfPart = new MimeBodyPart();
                pdfPart.attachFile(pdfFile);
                pdfPart.setFileName("Billet_" + reservation.getNomreserv() + "_" + i + ".pdf");
                multipart.addBodyPart(pdfPart);
            }

            // Association du contenu multipart avec le message
            message.setContent(multipart);

            // Envoi de l'email
            Transport.send(message);
            System.out.println("Email avec " + nombrePersonnes + " PDFs envoyé avec succès!");

            // Nettoyage - supprime les fichiers PDF temporaires
            for (File file : pdfFiles) {
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour générer un fichier PDF individuel (version originale sans Event)
    private File generateIndividualPdfFile(Reservation reservation, int ticketNumber) throws Exception {
        // Nom du fichier PDF
        String filename = "Billet_" + reservation.getNomreserv() + "_" + ticketNumber + ".pdf";
        File pdfFile = new File(filename);

        // Création du document PDF avec marges personnalisées
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        // Styles de base
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLUE);
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.DARK_GRAY);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);

        // Titre
        Paragraph title = new Paragraph("Billet d'entrée n°" + ticketNumber + "/" + reservation.getNbrpersonne(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Bloc de contenu stylisé
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setPadding(20);
        cell.setBackgroundColor(new BaseColor(244, 247, 252)); // couleur #f4f7fc
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph content = new Paragraph();
        content.add(new Phrase("Nom du réservant : ", boldFont));
        content.add(new Phrase(reservation.getNomreserv() + "\n", sectionFont));

        content.add(new Phrase("Email : ", boldFont));
        content.add(new Phrase(reservation.getMail() + "\n", sectionFont));

        content.add(new Phrase("Billet : ", boldFont));
        content.add(new Phrase(ticketNumber + " sur " + reservation.getNbrpersonne() + "\n", sectionFont));

        cell.addElement(content);
        table.addCell(cell);
        document.add(table);

        // Espace avant QR
        document.add(new Paragraph("\n"));

        // QR Code avec un identifiant unique pour chaque billet
        String qrContent = "Réservation: " + reservation.getNomreserv()
                + " | Email: " + reservation.getMail()
                + " | Billet: " + ticketNumber + "/" + reservation.getNbrpersonne()
                + " | ID: " + reservation.getId() + "-" + ticketNumber;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 150, 150);
        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOut);
        Image qrImage = Image.getInstance(pngOut.toByteArray());
        qrImage.setAlignment(Element.ALIGN_CENTER);
        document.add(qrImage);

        Paragraph qrCaption = new Paragraph("Scannez ce code pour vérifier votre billet", sectionFont);
        qrCaption.setAlignment(Element.ALIGN_CENTER);
        document.add(qrCaption);

        // Message de remerciement
        Paragraph thankYou = new Paragraph("\nCe billet est nominatif et personnel", boldFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        thankYou.setSpacingBefore(30);

        Paragraph message = new Paragraph("Merci de vous présenter avec une pièce d'identité. Ce billet doit être présenté à l'entrée de l'événement.", sectionFont);
        message.setAlignment(Element.ALIGN_CENTER);
        message.setSpacingAfter(20);

        document.add(thankYou);
        document.add(message);

        // Clôture
        document.close();

        return pdfFile;
    }

    // Méthode pour générer un fichier PDF individuel avec infos de l'événement
    private File generateIndividualPdfFile(Reservation reservation, Event event, int ticketNumber) throws Exception {
        // Nom du fichier PDF
        String filename = "Billet_" + reservation.getNomreserv() + "_" + ticketNumber + ".pdf";
        File pdfFile = new File(filename);

        // Création du document PDF avec marges personnalisées
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        // Styles de base
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLUE);
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.DARK_GRAY);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);

        // Titre
        Paragraph title = new Paragraph("Billet d'entrée n°" + ticketNumber + "/" + reservation.getNbrpersonne(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Bloc de contenu stylisé
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setPadding(20);
        cell.setBackgroundColor(new BaseColor(244, 247, 252)); // couleur #f4f7fc
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph content = new Paragraph();
        content.add(new Phrase("Nom du réservant : ", boldFont));
        content.add(new Phrase(reservation.getNomreserv() + "\n", sectionFont));

        content.add(new Phrase("Email : ", boldFont));
        content.add(new Phrase(reservation.getMail() + "\n", sectionFont));

        content.add(new Phrase("Billet : ", boldFont));
        content.add(new Phrase(ticketNumber + " sur " + reservation.getNbrpersonne() + "\n", sectionFont));

        cell.addElement(content);
        table.addCell(cell);
        document.add(table);

        // Espace avant QR
        document.add(new Paragraph("\n"));

        // QR Code avec un identifiant unique pour chaque billet et les informations de l'événement
        String qrContent = "Réservation: " + reservation.getNomreserv()
                + " | Email: " + reservation.getMail()
                + " | Billet: " + ticketNumber + "/" + reservation.getNbrpersonne()
                + " | ID: " + reservation.getId() + "-" + ticketNumber
                + " | Événement: " + event.getTitre()
                + " | Date: " + event.getDateevent()
                + " | Lieu: " + event.getLieu()
                + " | Nombre total de personnes: " + reservation.getNbrpersonne();

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 150, 150);
        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOut);
        Image qrImage = Image.getInstance(pngOut.toByteArray());
        qrImage.setAlignment(Element.ALIGN_CENTER);
        document.add(qrImage);

        Paragraph qrCaption = new Paragraph("Scannez ce code pour vérifier votre billet", sectionFont);
        qrCaption.setAlignment(Element.ALIGN_CENTER);
        document.add(qrCaption);

        // Message de remerciement
        Paragraph thankYou = new Paragraph("\nCe billet est nominatif et personnel", boldFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        thankYou.setSpacingBefore(30);

        Paragraph message = new Paragraph("Merci de vous présenter avec une pièce d'identité. Ce billet doit être présenté à l'entrée de l'événement.", sectionFont);
        message.setAlignment(Element.ALIGN_CENTER);
        message.setSpacingAfter(20);

        document.add(thankYou);
        document.add(message);

        // Clôture
        document.close();

        return pdfFile;
    }

    // Conservez la méthode d'origine pour des raisons de compatibilité
    private File generatePdfFile(Reservation reservation) throws Exception {
        // Nom du fichier PDF
        String filename = "Reservation_" + reservation.getNomreserv() + ".pdf";
        File pdfFile = new File(filename);

        // Création du document PDF avec marges personnalisées
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        // Styles de base
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLUE);
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.DARK_GRAY);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);

        // Titre
        Paragraph title = new Paragraph("Confirmation de Réservation", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Bloc de contenu stylisé
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setPadding(20);
        cell.setBackgroundColor(new BaseColor(244, 247, 252)); // couleur #f4f7fc
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph content = new Paragraph();
        content.add(new Phrase("Nom : ", boldFont));
        content.add(new Phrase(reservation.getNomreserv() + "\n", sectionFont));

        content.add(new Phrase("Email : ", boldFont));
        content.add(new Phrase(reservation.getMail() + "\n", sectionFont));

        content.add(new Phrase("Nombre de personnes : ", boldFont));
        content.add(new Phrase(String.valueOf(reservation.getNbrpersonne()) + "\n", sectionFont));

        cell.addElement(content);
        table.addCell(cell);
        document.add(table);

        // Espace avant QR
        document.add(new Paragraph("\n"));

        // QR Code
        String qrContent = "Réservation: " + reservation.getNomreserv()
                + " | Email: " + reservation.getMail()
                + " | Nombre: " + reservation.getNbrpersonne();

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 150, 150);
        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOut);
        Image qrImage = Image.getInstance(pngOut.toByteArray());
        qrImage.setAlignment(Element.ALIGN_CENTER);
        document.add(qrImage);

        Paragraph qrCaption = new Paragraph("Scannez ce code pour vérifier votre réservation", sectionFont);
        qrCaption.setAlignment(Element.ALIGN_CENTER);
        document.add(qrCaption);

        // Message de remerciement
        Paragraph thankYou = new Paragraph("\nMerci pour votre confiance !", boldFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        thankYou.setSpacingBefore(30);

        Paragraph message = new Paragraph("Nous sommes ravis de vous avoir parmi nos clients. À bientôt pour votre prochaine réservation !", sectionFont);
        message.setAlignment(Element.ALIGN_CENTER);
        message.setSpacingAfter(20);

        document.add(thankYou);
        document.add(message);

        // Clôture
        document.close();

        return pdfFile;
    }
}