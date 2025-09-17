package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Audience;
import projet.carthagecreance_backend.Entity.DecisionResult;
import projet.carthagecreance_backend.Entity.TribunalType;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AudienceRepository extends JpaRepository<Audience, Long> {
    
    // Rechercher les audiences par dossier
    List<Audience> findByDossierId(Long dossierId);
    
    // Rechercher les audiences par date
    List<Audience> findByDateAudience(LocalDate dateAudience);
    
    // Rechercher les audiences entre deux dates
    List<Audience> findByDateAudienceBetween(LocalDate dateDebut, LocalDate dateFin);
    
    // Rechercher les audiences par type de tribunal
    List<Audience> findByTribunalType(TribunalType tribunalType);
    
    // Rechercher les audiences par résultat
    List<Audience> findByResultat(DecisionResult resultat);
    
    // Rechercher les audiences par avocat
    List<Audience> findByAvocatId(Long avocatId);
    
    // Rechercher les audiences par huissier
    List<Audience> findByHuissierId(Long huissierId);
    
    // Rechercher les audiences à venir (date >= aujourd'hui)
    @Query("SELECT a FROM Audience a WHERE a.dateAudience >= :aujourdhui ORDER BY a.dateAudience ASC")
    List<Audience> findAudiencesAVenir(@Param("aujourdhui") LocalDate aujourdhui);
    
    // Rechercher les audiences passées (date < aujourd'hui)
    @Query("SELECT a FROM Audience a WHERE a.dateAudience < :aujourdhui ORDER BY a.dateAudience DESC")
    List<Audience> findAudiencesPassees(@Param("aujourdhui") LocalDate aujourdhui);
    
    // Rechercher les audiences par lieu de tribunal
    List<Audience> findByLieuTribunalContainingIgnoreCase(String lieuTribunal);
    
    // Compter les audiences par résultat
    @Query("SELECT a.resultat, COUNT(a) FROM Audience a GROUP BY a.resultat")
    List<Object[]> compterAudiencesParResultat();
    
    // Rechercher les audiences avec commentaires
    @Query("SELECT a FROM Audience a WHERE a.commentaireDecision IS NOT NULL AND a.commentaireDecision != ''")
    List<Audience> findAudiencesAvecCommentaires();
    
    // Rechercher la prochaine audience pour un dossier
    @Query("SELECT a FROM Audience a WHERE a.dossier.id = :dossierId AND a.dateAudience >= :aujourdhui ORDER BY a.dateAudience ASC")
    List<Audience> findProchaineAudienceParDossier(@Param("dossierId") Long dossierId, @Param("aujourdhui") LocalDate aujourdhui);
}
