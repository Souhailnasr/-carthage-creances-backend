package projet.carthagecreance_backend.Entity;

/**
 * Enum pour le statut global de validation des tarifs d'un dossier
 */
public enum StatutValidationTarifs {
    /**
     * Validation en cours
     */
    EN_COURS,
    
    /**
     * Tarifs de création validés
     */
    TARIFS_CREATION_VALIDES,
    
    /**
     * Tarifs d'enquête validés
     */
    TARIFS_ENQUETE_VALIDES,
    
    /**
     * Tarifs amiable validés
     */
    TARIFS_AMIABLE_VALIDES,
    
    /**
     * Tarifs juridique validés
     */
    TARIFS_JURIDIQUE_VALIDES,
    
    /**
     * Tous les tarifs validés, prêt pour facturation
     */
    TOUS_TARIFS_VALIDES,
    
    /**
     * Facture générée
     */
    FACTURE_GENEREE
}

