package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.Notification;
import projet.carthagecreance_backend.Entity.StatutNotification;
import projet.carthagecreance_backend.Entity.TypeNotification;
import projet.carthagecreance_backend.Entity.TypeEntite;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Repository.NotificationRepository;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service de gestion des notifications
 * Gère toutes les opérations CRUD et la logique métier pour les notifications
 */
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * Crée une nouvelle notification
     * @param notification La notification à créer
     * @return La notification créée avec son ID généré
     * @throws RuntimeException si les données de la notification sont invalides
     */
    @Override
    public Notification createNotification(Notification notification) {
        // Validation des données obligatoires
        validateNotificationData(notification);
        
        // Vérifier que l'utilisateur existe
        if (notification.getUtilisateur() == null || notification.getUtilisateur().getId() == null) {
            throw new RuntimeException("L'utilisateur destinataire est obligatoire");
        }
        
        utilisateurRepository.findById(notification.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur destinataire non trouvé avec l'ID: " + notification.getUtilisateur().getId()));
        
        // Initialiser les valeurs par défaut
        notification.setStatut(StatutNotification.NON_LUE);
        notification.setDateCreation(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }

    /**
     * Met à jour une notification existante
     * @param id L'ID de la notification à modifier
     * @param notification Les nouvelles données de la notification
     * @return La notification mise à jour
     * @throws RuntimeException si la notification n'existe pas
     */
    @Override
    public Notification updateNotification(Long id, Notification notification) {
        Notification existingNotification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'ID: " + id));
        
        // Validation des données
        validateNotificationData(notification);
        
        // Mettre à jour les champs modifiables
        existingNotification.setTitre(notification.getTitre());
        existingNotification.setMessage(notification.getMessage());
        existingNotification.setType(notification.getType());
        existingNotification.setEntiteId(notification.getEntiteId());
        existingNotification.setEntiteType(notification.getEntiteType());
        existingNotification.setLienAction(notification.getLienAction());
        
        return notificationRepository.save(existingNotification);
    }

    /**
     * Supprime une notification
     * @param id L'ID de la notification à supprimer
     * @throws RuntimeException si la notification n'existe pas
     */
    @Override
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException("Notification non trouvée avec l'ID: " + id);
        }
        notificationRepository.deleteById(id);
    }

    /**
     * Récupère une notification par son ID
     * @param id L'ID de la notification
     * @return La notification trouvée
     * @throws RuntimeException si la notification n'existe pas
     */
    @Override
    @Transactional(readOnly = true)
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'ID: " + id));
    }

    /**
     * Récupère toutes les notifications
     * @return Liste de toutes les notifications
     */
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    /**
     * Récupère les notifications par utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Liste des notifications de l'utilisateur
     */
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUtilisateurIdOrderByDateCreationDesc(userId);
    }

    /**
     * Récupère les notifications non lues par utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Liste des notifications non lues de l'utilisateur
     */
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsNonLuesByUser(Long userId) {
        return notificationRepository.findNotificationsNonLuesByUserOrderByDateCreationDesc(userId);
    }

    /**
     * Récupère les notifications par type et utilisateur
     * @param userId L'ID de l'utilisateur
     * @param type Le type de notification
     * @return Liste des notifications correspondantes
     */
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByType(Long userId, String type) {
        try {
            TypeNotification typeNotification = TypeNotification.valueOf(type.toUpperCase());
            return notificationRepository.findByUtilisateurIdAndType(userId, typeNotification);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Type de notification invalide: " + type);
        }
    }

    /**
     * Récupère les notifications récentes par utilisateur
     * @param userId L'ID de l'utilisateur
     * @param dateDebut Date de début pour la recherche
     * @return Liste des notifications récentes
     */
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsRecent(Long userId, LocalDateTime dateDebut) {
        return notificationRepository.findNotificationsRecent(userId, dateDebut);
    }

    /**
     * Récupère les notifications par entité
     * @param entiteId L'ID de l'entité
     * @param entiteType Le type d'entité
     * @return Liste des notifications de l'entité
     */
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByEntite(Long entiteId, String entiteType) {
        try {
            TypeEntite typeEntite = TypeEntite.valueOf(entiteType.toUpperCase());
            return notificationRepository.findByEntiteIdAndEntiteType(entiteId, typeEntite);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Type d'entité invalide: " + entiteType);
        }
    }

    /**
     * Marque une notification comme lue
     * @param notificationId L'ID de la notification
     * @return La notification mise à jour
     * @throws RuntimeException si la notification n'existe pas
     */
    @Override
    public Notification marquerLue(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'ID: " + notificationId));
        
        if (notification.getStatut() == StatutNotification.LUE) {
            throw new RuntimeException("La notification est déjà marquée comme lue");
        }
        
        notification.setStatut(StatutNotification.LUE);
        notification.setDateLecture(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }

    /**
     * Marque une notification comme non lue
     * @param notificationId L'ID de la notification
     * @return La notification mise à jour
     * @throws RuntimeException si la notification n'existe pas
     */
    @Override
    public Notification marquerNonLue(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'ID: " + notificationId));
        
        if (notification.getStatut() == StatutNotification.NON_LUE) {
            throw new RuntimeException("La notification est déjà marquée comme non lue");
        }
        
        notification.setStatut(StatutNotification.NON_LUE);
        notification.setDateLecture(null);
        
        return notificationRepository.save(notification);
    }

    /**
     * Marque toutes les notifications comme lues pour un utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Nombre de notifications marquées comme lues
     */
    @Override
    public int marquerToutesLues(Long userId) {
        List<Notification> notificationsNonLues = notificationRepository.findNotificationsNonLuesByUserOrderByDateCreationDesc(userId);
        
        LocalDateTime maintenant = LocalDateTime.now();
        for (Notification notification : notificationsNonLues) {
            notification.setStatut(StatutNotification.LUE);
            notification.setDateLecture(maintenant);
        }
        
        notificationRepository.saveAll(notificationsNonLues);
        return notificationsNonLues.size();
    }

    /**
     * Compte les notifications par utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Nombre de notifications de l'utilisateur
     */
    @Override
    @Transactional(readOnly = true)
    public long countNotificationsByUser(Long userId) {
        return notificationRepository.countByUtilisateurId(userId);
    }

    /**
     * Compte les notifications non lues par utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Nombre de notifications non lues de l'utilisateur
     */
    @Override
    @Transactional(readOnly = true)
    public long countNotificationsNonLuesByUser(Long userId) {
        return notificationRepository.countNotificationsNonLues(userId);
    }

    /**
     * Compte les notifications par type
     * @param type Le type de notification
     * @return Nombre de notifications avec ce type
     */
    @Override
    @Transactional(readOnly = true)
    public long countNotificationsByType(String type) {
        try {
            TypeNotification typeNotification = TypeNotification.valueOf(type.toUpperCase());
            return notificationRepository.countByType(typeNotification);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Type de notification invalide: " + type);
        }
    }

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
    @Override
    public Notification creerNotificationAutomatique(Long userId, TypeNotification type, String titre, String message, 
                                                     Long entiteId, TypeEntite entiteType, String lienAction) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));
        
        Notification notification = Notification.builder()
                .utilisateur(utilisateur)
                .type(type)
                .titre(titre)
                .message(message)
                .entiteId(entiteId)
                .entiteType(entiteType)
                .lienAction(lienAction)
                .statut(StatutNotification.NON_LUE)
                .dateCreation(LocalDateTime.now())
                .build();
        
        return notificationRepository.save(notification);
    }

    /**
     * Envoie une notification à un utilisateur (méthode de compatibilité)
     * @param utilisateur L'utilisateur destinataire
     * @param titre Le titre de la notification
     * @param message Le message de la notification
     * @param type Le type de notification
     */
    @Override
    public void envoyerNotification(Utilisateur utilisateur, String titre, String message, String type) {
        try {
            TypeNotification typeNotification = TypeNotification.valueOf(type.toUpperCase());
            creerNotificationAutomatique(utilisateur.getId(), typeNotification, titre, message, null, null, null);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Type de notification invalide: " + type);
        }
    }

    /**
     * Envoie une notification par ID utilisateur (méthode de compatibilité)
     * @param utilisateurId L'ID de l'utilisateur destinataire
     * @param titre Le titre de la notification
     * @param message Le message de la notification
     * @param type Le type de notification
     */
    @Override
    public void envoyerNotification(Long utilisateurId, String titre, String message, String type) {
        try {
            TypeNotification typeNotification = TypeNotification.valueOf(type.toUpperCase());
            creerNotificationAutomatique(utilisateurId, typeNotification, titre, message, null, null, null);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Type de notification invalide: " + type);
        }
    }

    /**
     * Envoie une notification de validation de dossier (méthode de compatibilité)
     * @param utilisateur L'utilisateur destinataire
     * @param numeroDossier Le numéro du dossier
     * @param statut Le statut de validation
     * @param commentaire Le commentaire de validation
     */
    @Override
    public void envoyerNotificationValidation(Utilisateur utilisateur, String numeroDossier, String statut, String commentaire) {
        String titre = "Dossier " + numeroDossier + " " + statut;
        String message = "Votre dossier " + numeroDossier + " a été " + statut.toLowerCase();
        if (commentaire != null && !commentaire.isEmpty()) {
            message += ". Commentaire: " + commentaire;
        }
        
        TypeNotification type = "VALIDÉ".equalsIgnoreCase(statut) ? TypeNotification.DOSSIER_VALIDE : TypeNotification.DOSSIER_REJETE;
        creerNotificationAutomatique(utilisateur.getId(), type, titre, message, null, TypeEntite.DOSSIER, null);
    }

    /**
     * Envoie une notification de validation de dossier par ID (méthode de compatibilité)
     * @param utilisateurId L'ID de l'utilisateur destinataire
     * @param numeroDossier Le numéro du dossier
     * @param statut Le statut de validation
     * @param commentaire Le commentaire de validation
     */
    @Override
    public void envoyerNotificationValidation(Long utilisateurId, String numeroDossier, String statut, String commentaire) {
        String titre = "Dossier " + numeroDossier + " " + statut;
        String message = "Votre dossier " + numeroDossier + " a été " + statut.toLowerCase();
        if (commentaire != null && !commentaire.isEmpty()) {
            message += ". Commentaire: " + commentaire;
        }
        
        TypeNotification type = "VALIDÉ".equalsIgnoreCase(statut) ? TypeNotification.DOSSIER_VALIDE : TypeNotification.DOSSIER_REJETE;
        creerNotificationAutomatique(utilisateurId, type, titre, message, null, TypeEntite.DOSSIER, null);
    }

    /**
     * Valide les données d'une notification
     * @param notification La notification à valider
     * @throws RuntimeException si les données sont invalides
     */
    private void validateNotificationData(Notification notification) {
        if (notification == null) {
            throw new RuntimeException("La notification ne peut pas être nulle");
        }
        
        if (notification.getTitre() == null || notification.getTitre().trim().isEmpty()) {
            throw new RuntimeException("Le titre de la notification est obligatoire");
        }
        
        if (notification.getType() == null) {
            throw new RuntimeException("Le type de notification est obligatoire");
        }
        
        if (notification.getUtilisateur() == null || notification.getUtilisateur().getId() == null) {
            throw new RuntimeException("L'utilisateur destinataire est obligatoire");
        }
    }
}