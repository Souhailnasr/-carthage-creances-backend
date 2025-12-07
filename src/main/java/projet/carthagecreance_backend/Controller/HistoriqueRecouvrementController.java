package projet.carthagecreance_backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.HistoriqueRecouvrement;
import projet.carthagecreance_backend.Repository.HistoriqueRecouvrementRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour l'historique des recouvrements
 * Permet de consulter l'historique détaillé des montants recouvrés par phase
 */
@RestController
@RequestMapping("/api/historique-recouvrement")
@CrossOrigin(origins = "*")
public class HistoriqueRecouvrementController {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoriqueRecouvrementController.class);
    
    @Autowired
    private HistoriqueRecouvrementRepository historiqueRecouvrementRepository;
    
    /**
     * GET /api/historique-recouvrement/dossier/{dossierId}
     * Récupère l'historique complet des recouvrements d'un dossier
     */
    @GetMapping("/dossier/{dossierId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CHEF_DEPARTEMENT_DOSSIER', 'CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE', 'CHEF_DEPARTEMENT_FINANCE')")
    public ResponseEntity<List<HistoriqueRecouvrement>> getHistoriqueByDossier(@PathVariable Long dossierId) {
        try {
            List<HistoriqueRecouvrement> historique = historiqueRecouvrementRepository
                    .findByDossierIdOrderByDateEnregistrementDesc(dossierId);
            return ResponseEntity.ok(historique);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'historique pour le dossier {}: {}", 
                    dossierId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/historique-recouvrement/dossier/{dossierId}/phase/{phase}
     * Récupère l'historique des recouvrements d'un dossier pour une phase spécifique
     */
    @GetMapping("/dossier/{dossierId}/phase/{phase}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CHEF_DEPARTEMENT_DOSSIER', 'CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE', 'CHEF_DEPARTEMENT_FINANCE')")
    public ResponseEntity<List<HistoriqueRecouvrement>> getHistoriqueByDossierAndPhase(
            @PathVariable Long dossierId,
            @PathVariable String phase) {
        try {
            HistoriqueRecouvrement.PhaseRecouvrement phaseEnum;
            try {
                phaseEnum = HistoriqueRecouvrement.PhaseRecouvrement.valueOf(phase.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(null);
            }
            
            List<HistoriqueRecouvrement> historique = historiqueRecouvrementRepository
                    .findByDossierIdAndPhaseOrderByDateEnregistrementDesc(dossierId, phaseEnum);
            return ResponseEntity.ok(historique);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'historique pour le dossier {} et phase {}: {}", 
                    dossierId, phase, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/historique-recouvrement/dossier/{dossierId}/resume
     * Récupère un résumé des montants recouvrés par phase pour un dossier
     */
    @GetMapping("/dossier/{dossierId}/resume")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CHEF_DEPARTEMENT_DOSSIER', 'CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE', 'CHEF_DEPARTEMENT_FINANCE')")
    public ResponseEntity<Map<String, Object>> getResumeByDossier(@PathVariable Long dossierId) {
        try {
            List<HistoriqueRecouvrement> historique = historiqueRecouvrementRepository
                    .findByDossierIdOrderByDateEnregistrementDesc(dossierId);
            
            // Calculer les totaux par phase
            double totalAmiable = historique.stream()
                    .filter(h -> h.getPhase() == HistoriqueRecouvrement.PhaseRecouvrement.AMIABLE)
                    .mapToDouble(h -> h.getMontantRecouvre().doubleValue())
                    .sum();
            
            double totalJuridique = historique.stream()
                    .filter(h -> h.getPhase() == HistoriqueRecouvrement.PhaseRecouvrement.JURIDIQUE)
                    .mapToDouble(h -> h.getMontantRecouvre().doubleValue())
                    .sum();
            
            double totalGeneral = totalAmiable + totalJuridique;
            
            // Dernier enregistrement
            HistoriqueRecouvrement dernier = historique.isEmpty() ? null : historique.get(0);
            
            Map<String, Object> resume = Map.of(
                    "dossierId", dossierId,
                    "montantRecouvrePhaseAmiable", totalAmiable,
                    "montantRecouvrePhaseJuridique", totalJuridique,
                    "montantRecouvreTotal", totalGeneral,
                    "nombreOperationsAmiable", historique.stream()
                            .filter(h -> h.getPhase() == HistoriqueRecouvrement.PhaseRecouvrement.AMIABLE)
                            .count(),
                    "nombreOperationsJuridique", historique.stream()
                            .filter(h -> h.getPhase() == HistoriqueRecouvrement.PhaseRecouvrement.JURIDIQUE)
                            .count(),
                    "dernierEnregistrement", dernier != null ? Map.of(
                            "date", dernier.getDateEnregistrement(),
                            "montant", dernier.getMontantRecouvre(),
                            "phase", dernier.getPhase(),
                            "typeAction", dernier.getTypeAction()
                    ) : null
            );
            
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du résumé pour le dossier {}: {}", 
                    dossierId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

