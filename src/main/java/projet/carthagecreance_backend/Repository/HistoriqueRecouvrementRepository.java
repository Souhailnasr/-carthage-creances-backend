package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.HistoriqueRecouvrement;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriqueRecouvrementRepository extends JpaRepository<HistoriqueRecouvrement, Long> {
    
    /**
     * Trouver tous les historiques d'un dossier
     */
    List<HistoriqueRecouvrement> findByDossierIdOrderByDateEnregistrementDesc(Long dossierId);
    
    /**
     * Trouver les historiques d'un dossier par phase
     */
    List<HistoriqueRecouvrement> findByDossierIdAndPhaseOrderByDateEnregistrementDesc(
            Long dossierId, 
            HistoriqueRecouvrement.PhaseRecouvrement phase);
    
    /**
     * Trouver les historiques par type d'action
     */
    List<HistoriqueRecouvrement> findByDossierIdAndTypeActionOrderByDateEnregistrementDesc(
            Long dossierId,
            HistoriqueRecouvrement.TypeActionRecouvrement typeAction);
    
    /**
     * Calculer le montant total recouvré par phase pour un dossier
     */
    @Query("SELECT COALESCE(SUM(h.montantRecouvre), 0) FROM HistoriqueRecouvrement h " +
           "WHERE h.dossierId = :dossierId AND h.phase = :phase")
    java.math.BigDecimal calculerMontantTotalParPhase(
            @Param("dossierId") Long dossierId,
            @Param("phase") HistoriqueRecouvrement.PhaseRecouvrement phase);
    
    /**
     * Trouver les historiques dans une période
     */
    @Query("SELECT h FROM HistoriqueRecouvrement h " +
           "WHERE h.dateEnregistrement BETWEEN :dateDebut AND :dateFin " +
           "ORDER BY h.dateEnregistrement DESC")
    List<HistoriqueRecouvrement> findByDateEnregistrementBetween(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);
    
    /**
     * Trouver les historiques d'un utilisateur
     */
    List<HistoriqueRecouvrement> findByUtilisateurIdOrderByDateEnregistrementDesc(Long utilisateurId);
}

