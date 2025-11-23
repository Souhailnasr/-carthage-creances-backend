package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.*;

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
}

