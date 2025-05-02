package org.example.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.example.models.Commande;
import org.example.models.CartItem;

import java.util.List;

public class SmsGenerator {
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;

    public SmsGenerator(String accountSid, String authToken, String fromNumber) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        // Initialize Twilio client
        Twilio.init(accountSid, authToken);
    }

    public void sendOrderConfirmation(Commande commande, List<CartItem> cartItems) {
        String toNumber = formatPhoneNumber(String.valueOf(commande.getPhone_number()));

        // Build the message
        StringBuilder message = new StringBuilder("Merci pour votre commande chez ChronoSerena !\n\n");
        message.append("Détails de la commande :\n");
        message.append("Nom du Client: ").append(commande.getNom()).append(" ").append(commande.getPrenom()).append("\n");
        message.append("Prix total: €").append(String.format("%.2f", commande.getTotalAmount())).append("\n\n");

        // Add product details
        message.append("Produits:\n");
        for (CartItem item : cartItems) {
            message.append("- ").append(item.getProduit().getNom())
                    .append(" x").append(item.getQuantite()).append("\n");
        }

        message.append("\nMerci d'avoir choisi ChronoSerena !");

        try {
            // Send the SMS
            Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    message.toString()
            ).create();
            System.out.println("SMS sent successfully to " + toNumber);
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
            // Log the error but don't disrupt the checkout flow
        }
    }

    private String formatPhoneNumber(String number) {
        // Remove spaces, dashes, etc.
        String cleaned = number.replaceAll("[^0-9]", "");

        // If it doesn't start with +, assume it's a local number and add country code
        if (!cleaned.startsWith("+")) {
            return "+216" + cleaned; // Assuming Tunisia (+216) as default country code
        }
        return cleaned;
    }
}