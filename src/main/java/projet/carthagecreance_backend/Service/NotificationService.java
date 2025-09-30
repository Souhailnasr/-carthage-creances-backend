package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Notification;
import projet.carthagecreance_backend.Entity.TypeNotification;
import projet.carthagecreance_backend.Entity.TypeEntite;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface pour la gestion des notifications
 * Fournit toutes les opérations CRUD et les fonctionnalités métier
 * pour la gestion des notifications dans le système
 */
public interface NotificationService {

    /**
     * Crée une nouvelle notification
     * @param notification La notification à créer
     * @return La notification créée avec son ID généré
     * @throws RuntimeException si les données de la notification sont invalides
     */
    Notification createNotification(Notification notification);

    /**
     * Met à jour une notification existante
     * @param id L'ID de la notification à modifier
     * @param notification Les nouvelles données de la notification
     * @return La notification mise à jour
     * @throws RuntimeException si la notification n'existe pas
     */
    Notification updateNotification(Long id, Notification notification);

    /**
     * Supprime une notification
     * @param id L'ID de la notification à supprimer
     * @throws RuntimeException si la notification n'existe pas
     */
    void deleteNotification(Long id);

    /**
     * Récupère une notification par son ID
     * @param id L'ID de la notification
     * @return La notification trouvée
     * @throws RuntimeException si la notification n'existe pas
     */
    Notification getNotificationById(Long id);

    /**
     * Récupère toutes les notifications
     * @return Liste de toutes les notifications
     */
    List<Notification> getAllNotifications();

    /**
     * Récupère les notifications par utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Liste des notifications de l'utilisateur
     */
    List<Notification> getNotificationsByUser(Long userId);

    /**
     * Récupère les notifications non lues par utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Liste des notifications non lues de l'utilisateur
     */
    List<Notification> getNotificationsNonLuesByUser(Long userId);

    /**
     * Récupère les notifications par type et utilisateur
     * @param userId L'ID de l'utilisateur
     * @param type Le type de notification
     * @return Liste des notifications correspondantes
     */
    List<Notification> getNotificationsByType(Long userId, String type);

    /**
     * Récupère les notifications récentes par utilisateur
     * @param userId L'ID de l'utilisateur
     * @param dateDebut Date de début pour la recherche
     * @return Liste des notifications récentes
     */
    List<Notification> getNotificationsRecent(Long userId, LocalDateTime dateDebut);

    /**
     * Récupère les notifications par entité
     * @param entiteId L'ID de l'entité
     * @param entiteType Le type d'entité
     * @return Liste des notifications de l'entité
     */
    List<Notification> getNotificationsByEntite(Long entiteId, String entiteType);

    /**
     * Marque une notification comme lue
     * @param notificationId L'ID de la notification
     * @return La notification mise à jour
     * @throws RuntimeException si la notification n'existe pas
     */
    Notification marquerLue(Long notificationId);

    /**
     * Marque une notification comme non lue
     * @param notificationId L'ID de la notification
     * @return La notification mise à jour
     * @throws RuntimeException si la notification n'existe pas
     */
    Notification marquerNonLue(Long notificationId);

    /**
     * Marque toutes les notifications comme lues pour un utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Nombre de notifications marquées comme lues
     */
    int marquerToutesLues(Long userId);

    /**
     * Compte les notifications par utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Nombre de notifications de l'utilisateur
     */
    long countNotificationsByUser(Long userId);

    /**
     * Compte les notifications non lues par utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Nombre de notifications non lues de l'utilisateur
     */
    long countNotificationsNonLuesByUser(Long userId);

    /**
     * Compte les notifications par type
     * @param type Le type de notification
     * @return Nombre de notifications avec ce type
     */
    long countNotificationsByType(String type);

    /**
     * Crée une notification automatique pour un événement
     * @param userId L'ID de l'utilisateur destinataire
     * @param type Le type de notification
     * @param titre Le titre de la notification
     * @param message Le message de la notification
     * @param entiteId L'ID de l'entité concernée
     * @param entiteType Le type d'entité
     * @param lienAction Le lien d'action (optionnel)
     * @return La notification créée
     */
    Notification creerNotificationAutomatique(Long userId, TypeNotification type, String titre, String message, 
                                             Long entiteId, TypeEntite entiteType, String lienAction);

    /**
     * Envoie une notification à un utilisateur (méthode de compatibilité)
     * @param utilisateur L'utilisateur destinataire
     * @param titre Le titre de la notification
     * @param message Le message de la notification
     * @param type Le type de notification
     */
    void envoyerNotification(projet.carthagecreance_backend.Entity.Utilisateur utilisateur, String titre, String message, String type);

    /**
     * Envoie une notification par ID utilisateur (méthode de compatibilité)
     * @param utilisateurId L'ID de l'utilisateur destinataire
     * @param titre Le titre de la notification
     * @param message Le message de la notification
     * @param type Le type de notification
     */
    void envoyerNotification(Long utilisateurId, String titre, String message, String type);

    /**
     * Envoie une notification de validation de dossier (méthode de compatibilité)
     * @param utilisateur L'utilisateur destinataire
     * @param numeroDossier Le numéro du dossier
     * @param statut Le statut de validation
     * @param commentaire Le commentaire de validation
     */
    void envoyerNotificationValidation(projet.carthagecreance_backend.Entity.Utilisateur utilisateur, String numeroDossier, String statut, String commentaire);

    /**
     * Envoie une notification de validation de dossier par ID (méthode de compatibilité)
     * @param utilisateurId L'ID de l'utilisateur destinataire
     * @param numeroDossier Le numéro du dossier
     * @param statut Le statut de validation
     * @param commentaire Le commentaire de validation
     */
    void envoyerNotificationValidation(Long utilisateurId, String numeroDossier, String statut, String commentaire);
}