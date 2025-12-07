package projet.carthagecreance_backend.Service;

/**
 * Service pour le recalcul automatique de la prédiction IA
 */
public interface IaPredictionRecalculationService {
    /**
     * Recalcule la prédiction IA pour un dossier donné
     * 
     * @param dossierId ID du dossier
     * @return true si le recalcul a réussi, false sinon
     */
    boolean recalculatePrediction(Long dossierId);
}

