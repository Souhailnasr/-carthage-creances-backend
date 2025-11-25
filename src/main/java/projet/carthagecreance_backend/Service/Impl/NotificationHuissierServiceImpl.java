package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Repository.NotificationHuissierRepository;
import projet.carthagecreance_backend.Service.NotificationHuissierService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NotificationHuissierServiceImpl implements NotificationHuissierService {
    
    @Autowired
    private NotificationHuissierRepository notificationHuissierRepository;
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Value("${app.webhook.url:}")
    private String webhookUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public void scheduleDocumentNotifications(DocumentHuissier document) {
        Dossier dossier = dossierRepository.findById(document.getDossierId())
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + document.getDossierId()));
        
        Instant now = Instant.now();
        Instant reminderDate = document.getDateCreation().plus(document.getDelaiLegalDays() - 2, ChronoUnit.DAYS);
        Instant expirationDate = document.getDateCreation().plus(document.getDelaiLegalDays(), ChronoUnit.DAYS);
        
        // Créer notification de rappel (2 jours avant expiration)
        if (reminderDate.isAfter(now)) {
            String message = String.format(
                "Rappel: %s du dossier %s expire le %s. Montant restant: %.2f TND.",
                document.getTypeDocument(),
                dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId(),
                expirationDate.toString(),
                dossier.getMontantRestant() != null ? dossier.getMontantRestant() : 0.0
            );
            
            createAndSendNotification(
                document.getDossierId(),
                TypeNotificationHuissier.DELAY_WARNING,
                message,
                null
            );
        }
    }
    
    @Override
    public void notifyDocumentExpired(DocumentHuissier document) {
        Dossier dossier = dossierRepository.findById(document.getDossierId())
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + document.getDossierId()));
        
        String message = String.format(
            "Expiration: délai légal terminé pour %s du dossier %s. Recommandation: %s",
            document.getTypeDocument(),
            dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId(),
            getRecommendationForExpiredDocument(document)
        );
        
        NotificationHuissier notification = createAndSendNotification(
            document.getDossierId(),
            TypeNotificationHuissier.DELAY_EXPIRED,
            message,
            null
        );
        
        // Envoyer via tous les canaux
        sendNotification(notification);
    }
    
    @Override
    public void notifyActionPerformed(ActionHuissier action, Dossier dossier) {
        String message = String.format(
            "Action %s réalisée par %s pour dossier %s. Montant recouvré: %s TND. Montant restant: %s TND.",
            action.getTypeAction(),
            action.getHuissierName() != null ? action.getHuissierName() : "Huissier",
            dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId(),
            action.getMontantRecouvre() != null ? action.getMontantRecouvre() : "0.00",
            action.getMontantRestant() != null ? action.getMontantRestant() : "0.00"
        );
        
        NotificationHuissier notification = createAndSendNotification(
            action.getDossierId(),
            TypeNotificationHuissier.ACTION_PERFORMED,
            message,
            null
        );
        
        sendNotification(notification);
    }
    
    @Override
    public void sendNotification(NotificationHuissier notification) {
        // Envoyer via tous les canaux configurés
        sendNotificationViaChannel(notification, CanalNotification.IN_APP);
        sendNotificationViaChannel(notification, CanalNotification.EMAIL);
        sendNotificationViaChannel(notification, CanalNotification.SMS);
        
        // WEBHOOK si configuré
        if (webhookUrl != null && !webhookUrl.isEmpty()) {
            sendNotificationViaChannel(notification, CanalNotification.WEBHOOK);
        }
    }
    
    @Override
    public void sendNotificationViaChannel(NotificationHuissier notification, CanalNotification channel) {
        // Créer une copie de la notification pour chaque canal
        NotificationHuissier channelNotification = NotificationHuissier.builder()
                .dossierId(notification.getDossierId())
                .type(notification.getType())
                .channel(channel)
                .message(notification.getMessage())
                .payload(notification.getPayload())
                .createdAt(notification.getCreatedAt())
                .acked(false)
                .recommendationId(notification.getRecommendationId())
                .build();
        
        channelNotification.setSentAt(Instant.now());
        notificationHuissierRepository.save(channelNotification);
        
        // Envoyer selon le canal
        switch (channel) {
            case IN_APP:
                // Déjà sauvegardé en DB, rien à faire de plus
                System.out.println("[IN_APP] Notification créée: " + notification.getMessage());
                break;
                
            case EMAIL:
                sendEmail(notification);
                break;
                
            case SMS:
                sendSms(notification);
                break;
                
            case WEBHOOK:
                sendWebhook(notification);
                break;
        }
    }
    
    private void sendEmail(NotificationHuissier notification) {
        // Simulation de l'envoi d'email (à remplacer par un vrai service email)
        String subject = "Notification - " + notification.getType();
        String body = notification.getMessage();
        
        System.out.println("========================================");
        System.out.println("[EMAIL SIMULATION]");
        System.out.println("To: dossier-" + notification.getDossierId() + "@example.com");
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("========================================");
        
        // TODO: Intégrer un vrai service email (JavaMailSender, SendGrid, etc.)
    }
    
    private void sendSms(NotificationHuissier notification) {
        // Simulation de l'envoi de SMS (à remplacer par un vrai service SMS)
        String phoneNumber = "+216XXXXXXXX"; // À récupérer depuis le dossier/utilisateur
        
        System.out.println("========================================");
        System.out.println("[SMS SIMULATION]");
        System.out.println("To: " + phoneNumber);
        System.out.println("Message: " + notification.getMessage());
        System.out.println("========================================");
        
        // TODO: Intégrer un vrai service SMS (Twilio, etc.)
    }
    
    private void sendWebhook(NotificationHuissier notification) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("dossierId", notification.getDossierId());
            payload.put("type", notification.getType().name());
            payload.put("message", notification.getMessage());
            payload.put("channel", notification.getChannel().name());
            payload.put("timestamp", Instant.now().toString());
            
            restTemplate.postForObject(webhookUrl, payload, String.class);
            
            System.out.println("[WEBHOOK] Notification envoyée à: " + webhookUrl);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du webhook: " + e.getMessage());
        }
    }
    
    private NotificationHuissier createAndSendNotification(Long dossierId, TypeNotificationHuissier type, 
                                                         String message, Long recommendationId) {
        NotificationHuissier notification = NotificationHuissier.builder()
                .dossierId(dossierId)
                .type(type)
                .channel(CanalNotification.IN_APP) // Canal par défaut
                .message(message)
                .createdAt(Instant.now())
                .acked(false)
                .recommendationId(recommendationId)
                .build();
        
        return notificationHuissierRepository.save(notification);
    }
    
    private String getRecommendationForExpiredDocument(DocumentHuissier document) {
        if (document.getTypeDocument() == TypeDocumentHuissier.PV_MISE_EN_DEMEURE) {
            return "Déposer demande ordonnance de paiement";
        } else {
            return "Initier action d'exécution (saisie)";
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationHuissier> getNotificationsByDossier(Long dossierId) {
        return notificationHuissierRepository.findByDossierId(dossierId);
    }
    
    @Override
    public void acknowledgeNotification(Long notificationId, Long userId) {
        NotificationHuissier notification = notificationHuissierRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'ID: " + notificationId));
        
        notification.setAcked(true);
        notificationHuissierRepository.save(notification);
    }
}

