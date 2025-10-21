package projet.carthagecreance_backend.Entity;

public enum DossierStatus {
    ENCOURSDETRAITEMENT,
    CLOTURE,
    INCONNU; // Valeur par défaut pour les données invalides
    
    /**
     * Méthode pour gérer les valeurs invalides
     */
    public static DossierStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ENCOURSDETRAITEMENT;
        }
        
        try {
            return DossierStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return ENCOURSDETRAITEMENT; // Valeur par défaut
        }
    }
}
