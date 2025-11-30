package projet.carthagecreance_backend.Entity;

/**
 * Enum pour gérer les étapes du workflow huissier
 */
public enum EtapeHuissier {
    /**
     * Dossier en attente de création de documents
     */
    EN_ATTENTE_DOCUMENTS,
    
    /**
     * Dossier à l'étape documents (documents en cours de création)
     */
    EN_DOCUMENTS,
    
    /**
     * Dossier à l'étape actions (actions en cours)
     */
    EN_ACTIONS,
    
    /**
     * Dossier prêt pour les audiences (toutes les actions sont terminées)
     */
    EN_AUDIENCES
}

