package projet.carthagecreance_backend.Entity;

public enum TypeNotification {
    // Notifications Dossiers
    DOSSIER_CREE,
    DOSSIER_VALIDE,
    DOSSIER_REJETE,
    DOSSIER_EN_ATTENTE,
    DOSSIER_AFFECTE,
    DOSSIER_CLOTURE,
    
    // Notifications Enquêtes
    ENQUETE_CREE,
    ENQUETE_VALIDE,
    ENQUETE_REJETE,
    ENQUETE_EN_ATTENTE,
    
    // Notifications Actions
    ACTION_AMIABLE_CREE,
    ACTION_AMIABLE_COMPLETEE,
    
    // Notifications Audiences
    AUDIENCE_PROCHAINE,
    AUDIENCE_CREE,
    AUDIENCE_REPORTEE,
    
    // Notifications Tâches
    TACHE_URGENTE,
    TACHE_AFFECTEE,
    TACHE_COMPLETEE,
    
    // Notifications Utilisateurs
    UTILISATEUR_CREE,
    UTILISATEUR_AFFECTE,
    UTILISATEUR_MODIFIE,
    
    // Notifications Huissier (fusionnées)
    DOCUMENT_HUISSIER_CREE,
    DOCUMENT_HUISSIER_EXPIRE,
    DELAY_WARNING,
    DELAY_EXPIRED,
    ACTION_HUISSIER_PERFORMED,
    AMIABLE_RESPONSE_POSITIVE,
    AMIABLE_RESPONSE_NEGATIVE,
    AMOUNT_UPDATED,
    STATUS_CHANGED,
    
    // Notifications Générales
    TRAITEMENT_DOSSIER,
    RAPPEL,
    INFO,
    NOTIFICATION_MANUELLE,
    
    // Notifications Hiérarchiques
    NOTIFICATION_CHEF_VERS_AGENT,
    NOTIFICATION_SUPERADMIN_VERS_CHEF,
    NOTIFICATION_SUPERADMIN_VERS_AGENT
}
