package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.*;
import java.util.List;

/**
 * Service pour la gestion des notifications automatiques
 * Crée automatiquement des notifications pour les événements importants du système
 */
public interface AutomaticNotificationService {
    
    /**
     * Crée une notification lors de la création d'un dossier
     * @param dossier Le dossier créé
     */
    void notifierCreationDossier(Dossier dossier);
    
    /**
     * Crée une notification lors de l'affectation d'un dossier à un agent
     * @param dossier Le dossier affecté
     * @param agent L'agent à qui le dossier est affecté
     */
    void notifierAffectationDossier(Dossier dossier, Utilisateur agent);
    
    /**
     * Crée une notification lors de la création d'une action amiable
     * @param action L'action créée
     * @param dossier Le dossier concerné
     */
    void notifierCreationActionAmiable(Action action, Dossier dossier);
    
    /**
     * Crée une notification pour une audience prochaine
     * @param audience L'audience prochaine
     * @param dossier Le dossier concerné
     */
    void notifierAudienceProchaine(Audience audience, Dossier dossier);
    
    /**
     * Crée une notification lors de la création d'une audience
     * @param audience L'audience créée
     * @param dossier Le dossier concerné
     */
    void notifierCreationAudience(Audience audience, Dossier dossier);
    
    /**
     * Crée une notification lors du traitement d'un dossier
     * @param dossier Le dossier traité
     * @param agent L'agent qui traite le dossier
     */
    void notifierTraitementDossier(Dossier dossier, Utilisateur agent);
    
    /**
     * Crée une notification lors de la validation d'un dossier
     * @param dossier Le dossier validé
     * @param valideur Le validateur
     */
    void notifierValidationDossier(Dossier dossier, Utilisateur valideur);
    
    /**
     * Crée une notification lors de la création d'une tâche
     * @param tache La tâche créée
     */
    void notifierCreationTache(TacheUrgente tache);
    
    /**
     * Crée une notification lors de la complétion d'une tâche
     * @param tache La tâche complétée
     */
    void notifierCompletionTache(TacheUrgente tache);
    
    /**
     * Vérifie et crée des notifications pour les audiences prochaines
     * Cette méthode doit être appelée périodiquement (par exemple via un scheduler)
     */
    void verifierEtNotifierAudiencesProchaines();
    
    /**
     * Crée une notification lors de la création d'un utilisateur
     * @param utilisateur L'utilisateur créé
     * @param createur L'utilisateur qui a créé le compte (SuperAdmin ou Chef)
     */
    void notifierCreationUtilisateur(Utilisateur utilisateur, Utilisateur createur);
    
    /**
     * Crée une notification lors de l'affectation d'un utilisateur à un département
     * @param utilisateur L'utilisateur affecté
     * @param chef Le chef du département
     */
    void notifierAffectationUtilisateur(Utilisateur utilisateur, Utilisateur chef);
    
    /**
     * Notifie la création d'un document huissier (fusionné depuis NotificationHuissier)
     * @param document Le document créé
     * @param dossier Le dossier concerné
     */
    void notifierCreationDocumentHuissier(DocumentHuissier document, Dossier dossier);
    
    /**
     * Notifie l'expiration d'un document huissier (fusionné depuis NotificationHuissier)
     * @param document Le document expiré
     * @param dossier Le dossier concerné
     */
    void notifierExpirationDocumentHuissier(DocumentHuissier document, Dossier dossier);
    
    /**
     * Notifie qu'une action huissier a été effectuée (fusionné depuis NotificationHuissier)
     * @param action L'action effectuée
     * @param dossier Le dossier concerné
     */
    void notifierActionHuissierEffectuee(ActionHuissier action, Dossier dossier);
    
    /**
     * Envoie une notification hiérarchique (SuperAdmin → Chef → Agent)
     * @param expediteur L'utilisateur qui envoie la notification
     * @param destinataires Liste des IDs des destinataires
     * @param type Type de notification
     * @param titre Titre de la notification
     * @param message Message de la notification
     * @param entiteId ID de l'entité concernée
     * @param entiteType Type d'entité
     */
    void envoyerNotificationHierarchique(Utilisateur expediteur, List<Long> destinataires, 
                                         TypeNotification type, String titre, String message,
                                         Long entiteId, TypeEntite entiteType);
    
    /**
     * Envoie une notification d'un SuperAdmin vers un Chef
     * @param chef Le chef destinataire
     * @param type Type de notification
     * @param titre Titre
     * @param message Message
     * @param entiteId ID entité
     * @param entiteType Type entité
     */
    void notifierSuperAdminVersChef(Utilisateur chef, TypeNotification type, String titre, 
                                    String message, Long entiteId, TypeEntite entiteType);
    
    /**
     * Envoie une notification d'un SuperAdmin vers un Agent
     * @param agent L'agent destinataire
     * @param type Type de notification
     * @param titre Titre
     * @param message Message
     * @param entiteId ID entité
     * @param entiteType Type entité
     */
    void notifierSuperAdminVersAgent(Utilisateur agent, TypeNotification type, String titre, 
                                    String message, Long entiteId, TypeEntite entiteType);
    
    /**
     * Envoie une notification d'un Chef vers ses Agents
     * @param chef Le chef expéditeur
     * @param type Type de notification
     * @param titre Titre
     * @param message Message
     * @param entiteId ID entité
     * @param entiteType Type entité
     */
    void notifierChefVersAgents(Utilisateur chef, TypeNotification type, String titre, 
                               String message, Long entiteId, TypeEntite entiteType);
}

