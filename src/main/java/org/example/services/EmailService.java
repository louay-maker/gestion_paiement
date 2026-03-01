package org.example.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;

import java.io.IOException;
import org.example.utils.Config;

public class EmailService {
    // Removed hardcoded keys for security - Loaded from config.properties
    private static final String SENDGRID_API_KEY = Config.get("sendgrid.api.key"); 
    private static final String FROM_EMAIL = Config.get("sendgrid.from.email");

    public static void sendPaymentConfirmation(String toEmail, String userName, double amount, String currency) {
        if (toEmail == null || toEmail.isEmpty()) {
            System.err.println("❌ Cannot send email: No email address provided.");
            return;
        }

        Email from = new Email(FROM_EMAIL);
        String subject = "Confirmation de votre paiement - Gestion Voyage";
        Email to = new Email(toEmail);
        
        Content content = new Content("text/html", 
            "<html><body>" +
            "<h1>Bonjour " + userName + "!</h1>" +
            "<p>Votre paiement de <strong>" + String.format("%.2f", amount) + " " + currency + "</strong> a été confirmé avec succès.</p>" +
            "<p>Votre billet est maintenant disponible dans votre espace client.</p>" +
            "<br><p>Merci d'avoir choisi notre plateforme de voyage !</p>" +
            "</body></html>");
            
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            
            System.out.println("📧 Simulation d'envoi d'email à " + toEmail);
            System.out.println("Status Code: " + response.getStatusCode());
        } catch (IOException ex) {
            System.err.println("❌ Error sending email via SendGrid: " + ex.getMessage());
        }
    }
}
