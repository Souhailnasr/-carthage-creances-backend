package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.DocumentHuissier;
import projet.carthagecreance_backend.Entity.ActionHuissier;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.NotificationHuissier;

import java.util.List;

/**
 * Service pour la gestion des notifications huissier avec canaux multiples
 */
public interface NotificationHuissierService {
    
    /**
     * Programme les notifications pour un document (rappel et expiration)
     * @param document Document huissier
     */
    void scheduleDocumentNotifications(DocumentHuissier document);
    
    /**
     * Notifie l'expiration d'un document
     * @param document Document expiré
     */
    void notifyDocumentExpired(DocumentHuissier document);
    
    /**
     * Notifie qu'une action a été effectuée
     * @param action Action effectuée
     * @param dossier Dossier concerné
     */
    void notifyActionPerformed(ActionHuissier action, Dossier dossier);
    
    /**
     * Envoie une notification via tous les canaux
     * @param notification Notification à envoyer
     */
    void sendNotification(NotificationHuissier notification);
    
    /**
     * Envoie une notification via un canal spécifique
     * @param notification Notification à envoyer
     * @param channel Canal à utiliser
     */
    void sendNotificationViaChannel(NotificationHuissier notification, projet.carthagecreance_backend.Entity.CanalNotification channel);
    
    /**
     * Récupère les notifications d'un dossier
     * @param dossierId ID du dossier
     * @return Liste des notifications
     */
    List<NotificationHuissier> getNotificationsByDossier(Long dossierId);
    
    /**
     * Marque une notification comme acquittée
     * @param notificationId ID de la notification
     * @param userId ID de l'utilisateur
     */
    void acknowledgeNotification(Long notificationId, Long userId);
}

