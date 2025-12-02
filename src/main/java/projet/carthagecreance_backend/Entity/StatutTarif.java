package projet.carthagecreance_backend.Entity;

/**
 * Enum pour le statut de validation d'un tarif de dossier
 */
public enum StatutTarif {
    /**
     * Tarif créé mais pas encore validé par le chef financier
     */
    EN_ATTENTE_VALIDATION,
    
    /**
     * Tarif validé par le chef financier
     */
    VALIDE,
    
    /**
     * Tarif rejeté par le chef financier
     */
    REJETE,
    
    /**
     * Tarif inclus dans une facture
     */
    FACTURE,
    
    /**
     * Tarif payé (facture entièrement payée)
     */
    PAYE
}

