package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.PerformanceAgent;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface PerformanceAgentRepository extends JpaRepository<PerformanceAgent, Long> {

    // Trouver les performances par agent
    List<PerformanceAgent> findByAgentId(Long agentId);

    // Trouver les performances par période
    List<PerformanceAgent> findByPeriode(String periode);

    // Trouver les performances récentes
    @Query("SELECT p FROM PerformanceAgent p WHERE p.dateCalcul >= :dateDebut ORDER BY p.score DESC")
    List<PerformanceAgent> findPerformancesRecent(@Param("dateDebut") LocalDateTime dateDebut);

    // Trouver les meilleures performances
    @Query("SELECT p FROM PerformanceAgent p ORDER BY p.score DESC")
    List<PerformanceAgent> findTopPerformances();

    // Trouver les performances par agent et période
    PerformanceAgent findByAgentIdAndPeriode(Long agentId, String periode);

    // Trouver les performances par score minimum
    @Query("SELECT p FROM PerformanceAgent p WHERE p.score >= :scoreMin ORDER BY p.score DESC")
    List<PerformanceAgent> findPerformancesByScoreMin(@Param("scoreMin") Double scoreMin);

    // Trouver les performances par agent et score minimum
    @Query("SELECT p FROM PerformanceAgent p WHERE p.agent.id = :agentId AND p.score >= :scoreMin ORDER BY p.score DESC")
    List<PerformanceAgent> findPerformancesByAgentAndScoreMin(@Param("agentId") Long agentId, @Param("scoreMin") Double scoreMin);

    // Compter les performances par agent
    Long countByAgentId(Long agentId);

    // Trouver les performances par période et score
    @Query("SELECT p FROM PerformanceAgent p WHERE p.periode = :periode AND p.score >= :scoreMin ORDER BY p.score DESC")
    List<PerformanceAgent> findPerformancesByPeriodeAndScore(@Param("periode") String periode, @Param("scoreMin") Double scoreMin);

    // Compter les performances par période
    long countByPeriode(String periode);

    // Trouver les performances par date de calcul
    @Query("SELECT p FROM PerformanceAgent p WHERE p.dateCalcul BETWEEN :dateDebut AND :dateFin ORDER BY p.dateCalcul DESC")
    List<PerformanceAgent> findByDateCalculBetween(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    // Trouver les performances par agent et date de calcul
    @Query("SELECT p FROM PerformanceAgent p WHERE p.agent.id = :agentId AND p.dateCalcul BETWEEN :dateDebut AND :dateFin ORDER BY p.dateCalcul DESC")
    List<PerformanceAgent> findByAgentIdAndDateCalculBetween(@Param("agentId") Long agentId, @Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    // Trouver les performances par période et agent
    List<PerformanceAgent> findByPeriodeAndAgentId(String periode, Long agentId);

    // Trouver les performances par score et période
    @Query("SELECT p FROM PerformanceAgent p WHERE p.periode = :periode AND p.score >= :scoreMin ORDER BY p.score DESC")
    List<PerformanceAgent> findByPeriodeAndScoreGreaterThanEqual(@Param("periode") String periode, @Param("scoreMin") Double scoreMin);

    // Trouver les performances par taux de réussite minimum
    @Query("SELECT p FROM PerformanceAgent p WHERE p.tauxReussite >= :tauxMin ORDER BY p.tauxReussite DESC")
    List<PerformanceAgent> findByTauxReussiteGreaterThanEqual(@Param("tauxMin") Double tauxMin);

    // Trouver les performances par agent et taux de réussite
    @Query("SELECT p FROM PerformanceAgent p WHERE p.agent.id = :agentId AND p.tauxReussite >= :tauxMin ORDER BY p.tauxReussite DESC")
    List<PerformanceAgent> findByAgentIdAndTauxReussiteGreaterThanEqual(@Param("agentId") Long agentId, @Param("tauxMin") Double tauxMin);

    // Trouver les performances par objectif
    @Query("SELECT p FROM PerformanceAgent p WHERE p.objectif IS NOT NULL ORDER BY p.objectif DESC")
    List<PerformanceAgent> findByObjectifNotNull();

    // Trouver les performances par agent et objectif
    @Query("SELECT p FROM PerformanceAgent p WHERE p.agent.id = :agentId AND p.objectif IS NOT NULL ORDER BY p.objectif DESC")
    List<PerformanceAgent> findByAgentIdAndObjectifNotNull(@Param("agentId") Long agentId);

    // Trouver les performances par période et objectif
    @Query("SELECT p FROM PerformanceAgent p WHERE p.periode = :periode AND p.objectif IS NOT NULL ORDER BY p.objectif DESC")
    List<PerformanceAgent> findByPeriodeAndObjectifNotNull(@Param("periode") String periode);

    // Vérifier l'existence d'une performance pour un agent et une période
    boolean existsByAgentIdAndPeriode(Long agentId, String periode);

    // Trouver les performances par agent et période avec pagination
    @Query("SELECT p FROM PerformanceAgent p WHERE p.agent.id = :agentId ORDER BY p.dateCalcul DESC")
    List<PerformanceAgent> findByAgentIdOrderByDateCalculDesc(@Param("agentId") Long agentId);

    // Trouver les performances par période avec pagination
    @Query("SELECT p FROM PerformanceAgent p WHERE p.periode = :periode ORDER BY p.score DESC")
    List<PerformanceAgent> findByPeriodeOrderByScoreDesc(@Param("periode") String periode);

    // Trouver les performances par score avec pagination
    @Query("SELECT p FROM PerformanceAgent p ORDER BY p.score DESC")
    List<PerformanceAgent> findAllOrderByScoreDesc();

    // Trouver les performances par taux de réussite avec pagination
    @Query("SELECT p FROM PerformanceAgent p WHERE p.tauxReussite IS NOT NULL ORDER BY p.tauxReussite DESC")
    List<PerformanceAgent> findByTauxReussiteNotNullOrderByTauxReussiteDesc();
}
