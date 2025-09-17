package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Finance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceRepository extends JpaRepository<Finance, Long> {
    
    // Rechercher par devise
    List<Finance> findByDevise(String devise);
    
    // Rechercher par date d'opération
    List<Finance> findByDateOperation(LocalDate dateOperation);
    
    // Rechercher par date d'opération entre deux dates
    List<Finance> findByDateOperationBetween(LocalDate dateDebut, LocalDate dateFin);
    
    // Rechercher par description
    List<Finance> findByDescriptionContainingIgnoreCase(String description);
    
    // Rechercher par dossier
    @Query("SELECT f FROM Finance f WHERE f.dossier.id = :dossierId")
    Optional<Finance> findByDossierId(@Param("dossierId") Long dossierId);
    
    // Rechercher par frais avocat supérieur à
    List<Finance> findByFraisAvocatGreaterThan(Double fraisAvocat);
    
    // Rechercher par frais huissier supérieur à
    List<Finance> findByFraisHuissierGreaterThan(Double fraisHuissier);
    
    // Rechercher les finances avec frais avocat
    @Query("SELECT f FROM Finance f WHERE f.fraisAvocat IS NOT NULL AND f.fraisAvocat > 0")
    List<Finance> findFinancesAvecFraisAvocat();
    
    // Rechercher les finances avec frais huissier
    @Query("SELECT f FROM Finance f WHERE f.fraisHuissier IS NOT NULL AND f.fraisHuissier > 0")
    List<Finance> findFinancesAvecFraisHuissier();
    
    // Calculer le total des frais avocat
    @Query("SELECT COALESCE(SUM(f.fraisAvocat), 0) FROM Finance f WHERE f.fraisAvocat IS NOT NULL")
    Double calculerTotalFraisAvocat();
    
    // Calculer le total des frais huissier
    @Query("SELECT COALESCE(SUM(f.fraisHuissier), 0) FROM Finance f WHERE f.fraisHuissier IS NOT NULL")
    Double calculerTotalFraisHuissier();
    
    // Calculer le total global des frais
    @Query("SELECT COALESCE(SUM(f.fraisAvocat), 0) + COALESCE(SUM(f.fraisHuissier), 0) FROM Finance f")
    Double calculerTotalGlobalFrais();
    
    // Calculer le total des frais par devise
    @Query("SELECT f.devise, COALESCE(SUM(f.fraisAvocat), 0) + COALESCE(SUM(f.fraisHuissier), 0) FROM Finance f WHERE f.devise IS NOT NULL GROUP BY f.devise")
    List<Object[]> calculerTotalFraisParDevise();
    
    // Rechercher les finances récentes (derniers 30 jours)
    @Query("SELECT f FROM Finance f WHERE f.dateOperation >= :dateLimite ORDER BY f.dateOperation DESC")
    List<Finance> findFinancesRecentes(@Param("dateLimite") LocalDate dateLimite);
    
    // Compter les finances par devise
    @Query("SELECT f.devise, COUNT(f) FROM Finance f WHERE f.devise IS NOT NULL GROUP BY f.devise")
    List<Object[]> compterFinancesParDevise();
    
    // Rechercher les finances avec actions
    @Query("SELECT f FROM Finance f WHERE SIZE(f.actions) > 0")
    List<Finance> findFinancesAvecActions();
    
    // Calculer le coût total des actions pour une finance
    @Query("SELECT COALESCE(SUM(a.nbOccurrences * a.coutUnitaire), 0) FROM Finance f JOIN f.actions a WHERE f.id = :financeId")
    Double calculerCoutTotalActions(@Param("financeId") Long financeId);
}
