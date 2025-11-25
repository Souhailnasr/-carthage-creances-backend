package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.DocumentHuissier;
import projet.carthagecreance_backend.Entity.ActionHuissier;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Recommendation;

import java.util.List;

/**
 * Service pour la gestion des recommandations intelligentes
 */
public interface RecommendationService {
    
    /**
     * Crée une recommandation pour un document créé
     * @param document Document créé
     * @return Recommendation créée
     */
    Recommendation createRecommendationForDocument(DocumentHuissier document);
    
    /**
     * Crée une recommandation pour un document expiré
     * @param document Document expiré
     * @return Recommendation créée
     */
    Recommendation createRecommendationForExpiredDocument(DocumentHuissier document);
    
    /**
     * Crée une recommandation après une action
     * @param action Action effectuée
     * @param dossier Dossier concerné
     * @return Recommendation créée (peut être null si aucune recommandation nécessaire)
     */
    Recommendation createRecommendationForAction(ActionHuissier action, Dossier dossier);
    
    /**
     * Évalue et crée des recommandations pour un dossier
     * @param dossier Dossier à évaluer
     * @return Liste des recommandations créées
     */
    List<Recommendation> evaluateAndCreateRecommendations(Dossier dossier);
    
    /**
     * Récupère les recommandations d'un dossier
     * @param dossierId ID du dossier
     * @return Liste des recommandations
     */
    List<Recommendation> getRecommendationsByDossier(Long dossierId);
    
    /**
     * Marque une recommandation comme acquittée
     * @param recommendationId ID de la recommandation
     * @param userId ID de l'utilisateur
     */
    void acknowledgeRecommendation(Long recommendationId, Long userId);
}

