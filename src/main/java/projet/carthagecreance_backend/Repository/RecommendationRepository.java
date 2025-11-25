package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Recommendation;
import projet.carthagecreance_backend.Entity.PrioriteRecommendation;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    
    List<Recommendation> findByDossierId(Long dossierId);
    
    List<Recommendation> findByDossierIdAndAcknowledged(Long dossierId, Boolean acknowledged);
    
    List<Recommendation> findByPriority(PrioriteRecommendation priority);
    
    List<Recommendation> findByRuleCode(String ruleCode);
    
    @Query("SELECT r FROM Recommendation r WHERE r.dossierId = :dossierId AND r.acknowledged = false ORDER BY r.priority DESC, r.createdAt DESC")
    List<Recommendation> findUnacknowledgedByDossierId(@Param("dossierId") Long dossierId);
}

