package projet.carthagecreance_backend.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;
    
    @Value("${app.name:Carthage Créances}")
    private String appName;
    
    @Override
    public void sendPasswordResetEmail(String email, String nom, String token) {
        log.info("Envoi d'un email de réinitialisation à: {}", email);
        
        // Construire le lien de réinitialisation
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        
        // Construire le sujet
        String subject = "Réinitialisation de votre mot de passe - " + appName;
        
        // Construire le corps de l'email
        String body = buildEmailBody(nom, resetLink);
        
        // Envoyer l'email réellement via SMTP
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML
            
            mailSender.send(message);
            log.info("Email de réinitialisation envoyé avec succès à: {}", email);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email: {}", e.getMessage(), e);
            // En cas d'erreur, log l'email pour debug (développement)
            logEmail(email, subject, body);
            throw new RuntimeException("Erreur lors de l'envoi de l'email: " + e.getMessage(), e);
        }
    }
    
    private String buildEmailBody(String nom, String resetLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 10px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour %s,</p>
                        <p>Vous avez demandé la réinitialisation de votre mot de passe.</p>
                        <p>Cliquez sur le bouton suivant pour réinitialiser votre mot de passe :</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Réinitialiser mon mot de passe</a>
                        </p>
                        <p>Ou copiez et collez ce lien dans votre navigateur :</p>
                        <p style="word-break: break-all; color: #0066cc;">%s</p>
                        <div class="warning">
                            <strong>⚠️ Important :</strong> Ce lien est valide pendant 24 heures uniquement.
                        </div>
                        <p>Si vous n'avez pas fait cette demande, ignorez cet email. Votre mot de passe ne sera pas modifié.</p>
                    </div>
                    <div class="footer">
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                        <p>&copy; %s - Tous droits réservés</p>
                    </div>
                </div>
            </body>
            </html>
            """, appName, nom, resetLink, resetLink, appName);
    }
    
    private void logEmail(String email, String subject, String body) {
        log.info("========================================");
        log.info("[EMAIL SIMULATION]");
        log.info("To: {}", email);
        log.info("Subject: {}", subject);
        log.info("Body (HTML): {}", body);
        log.info("========================================");
    }
}

