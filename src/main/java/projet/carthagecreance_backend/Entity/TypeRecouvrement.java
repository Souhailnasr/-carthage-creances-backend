package projet.carthagecreance_backend.Entity;

/**
 * Enum représentant le type de recouvrement d'un dossier
 */
public enum TypeRecouvrement {
    /**
     * Dossier non encore affecté à un type de recouvrement
     */
    NON_AFFECTE,
    
    /**
     * Dossier affecté au recouvrement amiable
     */
    AMIABLE,
    
    /**
     * Dossier affecté au recouvrement juridique
     */
    JURIDIQUE,
    
    /**
     * Dossier affecté au département finance
     */
    FINANCE
}

