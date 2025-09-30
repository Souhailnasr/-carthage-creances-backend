package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.PerformanceAgent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface pour la gestion des performances des agents
 * Fournit toutes les opérations CRUD et les fonctionnalités métier
 * pour la gestion des performances des agents dans le système
 */
public interface PerformanceAgentService {

    /**
     * Crée une nouvelle performance d'agent
     * @param performance La performance à créer
     * @return La performance créée avec son ID généré
     * @throws RuntimeException si les données de la performance sont invalides
     */
    PerformanceAgent createPerformanceAgent(PerformanceAgent performance);

    /**
     * Met à jour une performance d'agent existante
     * @param id L'ID de la performance à modifier
     * @param performance Les nouvelles données de la performance
     * @return La performance mise à jour
     * @throws RuntimeException si la performance n'existe pas
     */
    PerformanceAgent updatePerformanceAgent(Long id, PerformanceAgent performance);

    /**
     * Supprime une performance d'agent
     * @param id L'ID de la performance à supprimer
     * @throws RuntimeException si la performance n'existe pas
     */
    void deletePerformanceAgent(Long id);

    /**
     * Récupère une performance d'agent par son ID
     * @param id L'ID de la performance
     * @return La performance trouvée
     * @throws RuntimeException si la performance n'existe pas
     */
    PerformanceAgent getPerformanceAgentById(Long id);

    /**
     * Récupère toutes les performances d'agents
     * @return Liste de toutes les performances d'agents
     */
    List<PerformanceAgent> getAllPerformancesAgent();

    /**
     * Récupère les performances par agent
     * @param agentId L'ID de l'agent
     * @return Liste des performances de l'agent
     */
    List<PerformanceAgent> getPerformancesByAgent(Long agentId);

    /**
     * Récupère les performances par période
     * @param periode La période des performances
     * @return Liste des performances de la période
     */
    List<PerformanceAgent> getPerformancesByPeriode(String periode);

    /**
     * Récupère la performance par agent et période
     * @param agentId L'ID de l'agent
     * @param periode La période de la performance
     * @return La performance trouvée
     * @throws RuntimeException si la performance n'existe pas
     */
    PerformanceAgent getPerformanceByAgentAndPeriode(Long agentId, String periode);

    /**
     * Récupère les performances récentes
     * @param dateDebut Date de début pour la recherche
     * @return Liste des performances récentes
     */
    List<PerformanceAgent> getPerformancesRecent(LocalDateTime dateDebut);

    /**
     * Récupère les meilleures performances
     * @return Liste des meilleures performances triées par score
     */
    List<PerformanceAgent> getTopPerformances();

    /**
     * Récupère les performances par score minimum
     * @param scoreMin Le score minimum requis
     * @return Liste des performances avec le score minimum
     */
    List<PerformanceAgent> getPerformancesByScoreMin(Double scoreMin);

    /**
     * Récupère les performances par agent et score minimum
     * @param agentId L'ID de l'agent
     * @param scoreMin Le score minimum requis
     * @return Liste des performances de l'agent avec le score minimum
     */
    List<PerformanceAgent> getPerformancesByAgentAndScoreMin(Long agentId, Double scoreMin);

    /**
     * Calcule la performance d'un agent pour une période donnée
     * @param agentId L'ID de l'agent
     * @param periode La période de calcul
     * @return La performance calculée
     * @throws RuntimeException si l'agent n'existe pas
     */
    PerformanceAgent calculerPerformanceAgent(Long agentId, String periode);

    /**
     * Calcule les performances pour une période donnée
     * @param periode La période de calcul
     * @return Liste des performances calculées
     */
    List<PerformanceAgent> calculerPerformancesPeriode(String periode);

    /**
     * Calcule toutes les performances
     * @return Liste de toutes les performances calculées
     */
    List<PerformanceAgent> calculerToutesPerformances();

    /**
     * Compte les performances par agent
     * @param agentId L'ID de l'agent
     * @return Nombre de performances de l'agent
     */
    long countPerformancesByAgent(Long agentId);

    /**
     * Compte les performances par période
     * @param periode La période des performances
     * @return Nombre de performances de la période
     */
    long countPerformancesByPeriode(String periode);

    /**
     * Récupère les performances par taux de réussite minimum
     * @param tauxMin Le taux de réussite minimum
     * @return Liste des performances avec le taux minimum
     */
    List<PerformanceAgent> getPerformancesByTauxReussiteMin(Double tauxMin);

    /**
     * Récupère les performances par agent et taux de réussite minimum
     * @param agentId L'ID de l'agent
     * @param tauxMin Le taux de réussite minimum
     * @return Liste des performances de l'agent avec le taux minimum
     */
    List<PerformanceAgent> getPerformancesByAgentAndTauxReussiteMin(Long agentId, Double tauxMin);

    /**
     * Récupère les performances par objectif
     * @return Liste des performances avec objectif
     */
    List<PerformanceAgent> getPerformancesByObjectif();

    /**
     * Récupère les performances par agent et objectif
     * @param agentId L'ID de l'agent
     * @return Liste des performances de l'agent avec objectif
     */
    List<PerformanceAgent> getPerformancesByAgentAndObjectif(Long agentId);

    /**
     * Récupère les performances par période et objectif
     * @param periode La période des performances
     * @return Liste des performances de la période avec objectif
     */
    List<PerformanceAgent> getPerformancesByPeriodeAndObjectif(String periode);

    /**
     * Met à jour le taux de réussite d'une performance
     * @param performanceId L'ID de la performance
     * @return La performance mise à jour
     * @throws RuntimeException si la performance n'existe pas
     */
    PerformanceAgent mettreAJourTauxReussite(Long performanceId);

    /**
     * Met à jour le score d'une performance
     * @param performanceId L'ID de la performance
     * @return La performance mise à jour
     * @throws RuntimeException si la performance n'existe pas
     */
    PerformanceAgent mettreAJourScore(Long performanceId);

    /**
     * Vérifie si une performance existe pour un agent et une période
     * @param agentId L'ID de l'agent
     * @param periode La période
     * @return true si la performance existe, false sinon
     */
    boolean existePerformance(Long agentId, String periode);
}
