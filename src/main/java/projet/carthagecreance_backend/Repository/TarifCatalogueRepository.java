package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.PhaseFrais;
import projet.carthagecreance_backend.Entity.TarifCatalogue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TarifCatalogueRepository extends JpaRepository<TarifCatalogue, Long> {

    List<TarifCatalogue> findByActifTrue();

    List<TarifCatalogue> findByPhaseAndActifTrue(PhaseFrais phase);

    Optional<TarifCatalogue> findFirstByPhaseAndCategorieAndActifTrueOrderByDateDebutDesc(PhaseFrais phase, String categorie);

    List<TarifCatalogue> findByCategorie(String categorie);

    @Query("SELECT t FROM TarifCatalogue t WHERE t.phase = :phase AND t.categorie = :categorie " +
           "AND t.actif = true AND (t.dateDebut <= :date AND (t.dateFin IS NULL OR t.dateFin >= :date)) " +
           "ORDER BY t.dateDebut DESC")
    Optional<TarifCatalogue> findTarifActifByPhaseAndCategorie(@Param("phase") PhaseFrais phase, 
                                                                      @Param("categorie") String categorie, 
                                                                      @Param("date") LocalDate date);

    @Query("SELECT t FROM TarifCatalogue t WHERE t.id = :id OR " +
           "(t.phase = (SELECT t2.phase FROM TarifCatalogue t2 WHERE t2.id = :id) AND " +
           "t.categorie = (SELECT t2.categorie FROM TarifCatalogue t2 WHERE t2.id = :id)) " +
           "ORDER BY t.dateDebut DESC")
    List<TarifCatalogue> findHistoriqueByTarifId(@Param("id") Long id);
}

