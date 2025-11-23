package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.FluxFrais;
import projet.carthagecreance_backend.Entity.PhaseFrais;
import projet.carthagecreance_backend.Entity.StatutFrais;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FluxFraisRepository extends JpaRepository<FluxFrais, Long> {

    @Query("SELECT f FROM FluxFrais f WHERE f.dossier.id = :dossierId")
    List<FluxFrais> findByDossierId(@Param("dossierId") Long dossierId);

    List<FluxFrais> findByStatut(StatutFrais statut);

    @Query("SELECT f FROM FluxFrais f WHERE f.facture.id = :factureId")
    List<FluxFrais> findByFactureId(@Param("factureId") Long factureId);

    List<FluxFrais> findByPhase(PhaseFrais phase);

    List<FluxFrais> findByDateActionBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(f.montant) FROM FluxFrais f WHERE f.dossier.id = :dossierId")
    Double calculerTotalFraisByDossier(@Param("dossierId") Long dossierId);

    @Query("SELECT SUM(f.montant) FROM FluxFrais f WHERE f.statut = :statut")
    Double calculerTotalFraisByStatut(@Param("statut") StatutFrais statut);

    @Query("SELECT f FROM FluxFrais f WHERE f.action.id = :actionId")
    List<FluxFrais> findByActionId(@Param("actionId") Long actionId);

    @Query("SELECT f FROM FluxFrais f WHERE f.enquette.id = :enqueteId")
    List<FluxFrais> findByEnqueteId(@Param("enqueteId") Long enqueteId);

    @Query("SELECT f FROM FluxFrais f WHERE f.audience.id = :audienceId")
    List<FluxFrais> findByAudienceId(@Param("audienceId") Long audienceId);
}

