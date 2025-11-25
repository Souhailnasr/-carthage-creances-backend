package projet.carthagecreance_backend.Scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import projet.carthagecreance_backend.Entity.DocumentHuissier;
import projet.carthagecreance_backend.Entity.StatutDocumentHuissier;
import projet.carthagecreance_backend.Repository.DocumentHuissierRepository;
import projet.carthagecreance_backend.Service.DocumentHuissierService;
import projet.carthagecreance_backend.Service.NotificationHuissierService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduler pour vérifier les délais légaux des documents huissier
 * Exécuté toutes les 10 minutes
 */
@Component
public class LegalDelayScheduler {
    
    @Autowired
    private DocumentHuissierRepository documentHuissierRepository;
    
    @Autowired
    private DocumentHuissierService documentHuissierService;
    
    @Autowired
    private NotificationHuissierService notificationHuissierService;
    
    /**
     * Vérifie les documents expirés et ceux nécessitant un rappel
     * Exécuté toutes les 10 minutes
     */
    @Scheduled(cron = "0 */10 * * * *") // Toutes les 10 minutes
    public void checkExpiredDocuments() {
        Instant now = Instant.now();
        
        try {
            // Récupérer tous les documents en attente
            List<DocumentHuissier> pendingDocuments = documentHuissierRepository.findByStatus(StatutDocumentHuissier.PENDING);
            
            for (DocumentHuissier document : pendingDocuments) {
                Instant creation = document.getDateCreation();
                int delai = document.getDelaiLegalDays() != null ? document.getDelaiLegalDays() : 0;
                Instant deadline = creation.plus(delai, ChronoUnit.DAYS);
                Instant reminderDate = deadline.minus(2, ChronoUnit.DAYS);
                
                // 1. Vérifier si le document est expiré
                if (!deadline.isAfter(now)) {
                    documentHuissierService.markAsExpired(document.getId());
                    continue;
                }
                
                // 2. Vérifier si un rappel doit être envoyé (2 jours avant l'expiration)
                if (!document.getNotified() && !reminderDate.isAfter(now) && deadline.isAfter(now)) {
                    notificationHuissierService.scheduleDocumentNotifications(document);
                    document.setNotified(true);
                    documentHuissierRepository.save(document);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des délais légaux: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

