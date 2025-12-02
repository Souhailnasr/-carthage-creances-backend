package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.PhaseFrais;
import projet.carthagecreance_backend.Entity.StatutTarif;
import projet.carthagecreance_backend.Entity.TarifDossier;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarifDossierRepository extends JpaRepository<TarifDossier, Long> {
    
    List<TarifDossier> findByDossierId(Long dossierId);
    
    List<TarifDossier> findByDossierIdAndPhase(Long dossierId, PhaseFrais phase);
    
    List<TarifDossier> findByDossierIdAndStatut(Long dossierId, StatutTarif statut);
    
    long countByDossierIdAndPhaseAndStatut(Long dossierId, PhaseFrais phase, StatutTarif statut);
    
    Optional<TarifDossier> findByDossierIdAndPhaseAndCategorie(Long dossierId, PhaseFrais phase, String categorie);
    
    @Query("SELECT t FROM TarifDossier t WHERE t.dossier.id = :dossierId AND t.action.id = :actionId")
    Optional<TarifDossier> findByDossierIdAndActionId(@Param("dossierId") Long dossierId, @Param("actionId") Long actionId);
    
    @Query("SELECT t FROM TarifDossier t WHERE t.dossier.id = :dossierId AND t.documentHuissier.id = :documentId")
    Optional<TarifDossier> findByDossierIdAndDocumentHuissierId(@Param("dossierId") Long dossierId, @Param("documentId") Long documentId);
    
    @Query("SELECT t FROM TarifDossier t WHERE t.dossier.id = :dossierId AND t.actionHuissier.id = :actionHuissierId")
    Optional<TarifDossier> findByDossierIdAndActionHuissierId(@Param("dossierId") Long dossierId, @Param("actionHuissierId") Long actionHuissierId);
    
    @Query("SELECT t FROM TarifDossier t WHERE t.dossier.id = :dossierId AND t.audience.id = :audienceId")
    Optional<TarifDossier> findByDossierIdAndAudienceId(@Param("dossierId") Long dossierId, @Param("audienceId") Long audienceId);
    
    @Query("SELECT t FROM TarifDossier t WHERE t.dossier.id = :dossierId AND t.enquete.id = :enqueteId")
    Optional<TarifDossier> findByDossierIdAndEnqueteId(@Param("dossierId") Long dossierId, @Param("enqueteId") Long enqueteId);
}

