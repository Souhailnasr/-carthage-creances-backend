package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.DTO.IaPredictionResult;

import java.util.Map;

/**
 * Service pour la prédiction IA
 */
public interface IaPredictionService {
    /**
     * Prédit le risque et l'état final d'un dossier à partir des données réelles
     * 
     * @param donneesReelles Map contenant les features pour la prédiction
     * @return Résultat de la prédiction (état final, score de risque, niveau de risque)
     */
    IaPredictionResult predictRisk(Map<String, Object> donneesReelles);
}

