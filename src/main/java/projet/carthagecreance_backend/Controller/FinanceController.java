package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Finance;
import projet.carthagecreance_backend.Service.FinanceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/finances")
@CrossOrigin(origins = "*")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<Finance> createFinance(@RequestBody Finance finance) {
        try {
            Finance createdFinance = financeService.createFinance(finance);
            return new ResponseEntity<>(createdFinance, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Finance> getFinanceById(@PathVariable Long id) {
        Optional<Finance> finance = financeService.getFinanceById(id);
        return finance.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Finance>> getAllFinances() {
        List<Finance> finances = financeService.getAllFinances();
        return new ResponseEntity<>(finances, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Finance> updateFinance(@PathVariable Long id, @RequestBody Finance finance) {
        try {
            Finance updatedFinance = financeService.updateFinance(id, finance);
            return new ResponseEntity<>(updatedFinance, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFinance(@PathVariable Long id) {
        try {
            financeService.deleteFinance(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Search Operations
    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<Finance> getFinanceByDossier(@PathVariable Long dossierId) {
        Optional<Finance> finance = financeService.getFinanceByDossier(dossierId);
        return finance.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<Finance>> getFinancesByCurrency(@PathVariable String currency) {
        List<Finance> finances = financeService.getFinancesByCurrency(currency);
        return new ResponseEntity<>(finances, HttpStatus.OK);
    }

    @GetMapping("/operation-date/{date}")
    public ResponseEntity<List<Finance>> getFinancesByOperationDate(@PathVariable LocalDate date) {
        List<Finance> finances = financeService.getFinancesByOperationDate(date);
        return new ResponseEntity<>(finances, HttpStatus.OK);
    }

    @GetMapping("/operation-date-range")
    public ResponseEntity<List<Finance>> getFinancesByOperationDateRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        List<Finance> finances = financeService.getFinancesByOperationDateRange(startDate, endDate);
        return new ResponseEntity<>(finances, HttpStatus.OK);
    }

    @GetMapping("/description")
    public ResponseEntity<List<Finance>> getFinancesByDescription(@RequestParam String description) {
        List<Finance> finances = financeService.getFinancesByDescription(description);
        return new ResponseEntity<>(finances, HttpStatus.OK);
    }

    @GetMapping("/with-avocat-fees")
    public ResponseEntity<List<Finance>> getFinancesWithAvocatFees() {
        List<Finance> finances = financeService.getFinancesWithAvocatFees();
        return new ResponseEntity<>(finances, HttpStatus.OK);
    }

    @GetMapping("/with-huissier-fees")
    public ResponseEntity<List<Finance>> getFinancesWithHuissierFees() {
        List<Finance> finances = financeService.getFinancesWithHuissierFees();
        return new ResponseEntity<>(finances, HttpStatus.OK);
    }

    @GetMapping("/with-actions")
    public ResponseEntity<List<Finance>> getFinancesWithActions() {
        List<Finance> finances = financeService.getFinancesWithActions();
        return new ResponseEntity<>(finances, HttpStatus.OK);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Finance>> getRecentFinances() {
        List<Finance> finances = financeService.getRecentFinances();
        return new ResponseEntity<>(finances, HttpStatus.OK);
    }

    // Calculated Operations
    @GetMapping("/total-avocat-fees")
    public ResponseEntity<Double> getTotalAvocatFees() {
        Double total = financeService.calculateTotalAvocatFees();
        return new ResponseEntity<>(total, HttpStatus.OK);
    }

    @GetMapping("/total-huissier-fees")
    public ResponseEntity<Double> getTotalHuissierFees() {
        Double total = financeService.calculateTotalHuissierFees();
        return new ResponseEntity<>(total, HttpStatus.OK);
    }

    @GetMapping("/total-global-fees")
    public ResponseEntity<Double> getTotalGlobalFees() {
        Double total = financeService.calculateTotalGlobalFees();
        return new ResponseEntity<>(total, HttpStatus.OK);
    }

    @GetMapping("/total-fees-by-currency/{currency}")
    public ResponseEntity<Double> getTotalFeesByCurrency(@PathVariable String currency) {
        Double total = financeService.calculateTotalFeesByCurrency(currency);
        return new ResponseEntity<>(total, HttpStatus.OK);
    }

    @GetMapping("/{financeId}/total-action-costs")
    public ResponseEntity<Double> getTotalActionCosts(@PathVariable Long financeId) {
        Double total = financeService.calculateTotalActionCosts(financeId);
        return new ResponseEntity<>(total, HttpStatus.OK);
    }
    
    // ✅ Nouveaux endpoints pour la facturation complète
    
    @GetMapping("/dossier/{dossierId}/facture")
    public ResponseEntity<?> getDetailFacture(@PathVariable Long dossierId) {
        try {
            java.util.Map<String, Object> detail = financeService.getDetailFacture(dossierId);
            return new ResponseEntity<>(detail, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/dossier/{dossierId}/detail")
    public ResponseEntity<?> getCoutsParDossier(@PathVariable Long dossierId) {
        try {
            java.util.Map<String, Object> couts = financeService.getCoutsParDossier(dossierId);
            return new ResponseEntity<>(couts, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/dossier/{dossierId}/recalculer")
    public ResponseEntity<?> recalculerCouts(@PathVariable Long dossierId) {
        try {
            Finance finance = financeService.recalculerCoutsDossier(dossierId);
            return new ResponseEntity<>(finance, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/statistiques")
    public ResponseEntity<?> getStatistiquesCouts() {
        try {
            java.util.Map<String, Object> stats = financeService.getStatistiquesCouts();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/dossiers-avec-couts")
    public ResponseEntity<?> getDossiersAvecCouts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateOperation") String sort) {
        try {
            // ✅ Retourner FinanceDTO avec dossierId au lieu de Finance
            org.springframework.data.domain.Page<projet.carthagecreance_backend.DTO.FinanceDTO> pageResult = 
                financeService.getDossiersAvecCoutsDTO(page, size, sort);
            
            // ✅ Vérification de debug (à retirer en production si nécessaire)
            pageResult.getContent().forEach(dto -> {
                if (dto.getDossierId() == null) {
                    System.out.println("⚠️ Finance " + dto.getId() + " n'a pas de dossierId");
                }
            });
            
            return new ResponseEntity<>(pageResult, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/factures-en-attente")
    public ResponseEntity<?> getFacturesEnAttente() {
        try {
            List<Finance> factures = financeService.getFacturesEnAttente();
            return new ResponseEntity<>(factures, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/dossier/{dossierId}/finaliser-facture")
    public ResponseEntity<?> finaliserFacture(@PathVariable Long dossierId) {
        try {
            Finance finance = financeService.finaliserFacture(dossierId);
            return new ResponseEntity<>(finance, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    
    // ========== NOUVEAUX ENDPOINTS POUR GESTION DES TARIFS PAR DOSSIER ==========
    
    @Autowired
    private projet.carthagecreance_backend.Service.TarifDossierService tarifDossierService;
    
    /**
     * GET /api/finances/dossier/{dossierId}/traitements
     * Récupère tous les traitements d'un dossier organisés par phase
     */
    @GetMapping("/dossier/{dossierId}/traitements")
    public ResponseEntity<?> getTraitementsDossier(@PathVariable Long dossierId) {
        try {
            projet.carthagecreance_backend.DTO.TraitementsDossierDTO dto = tarifDossierService.getTraitementsDossier(dossierId);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * POST /api/finances/dossier/{dossierId}/tarifs
     * Crée un nouveau tarif pour un traitement spécifique
     */
    @PostMapping("/dossier/{dossierId}/tarifs")
    public ResponseEntity<?> createTarif(@PathVariable Long dossierId, 
                                         @RequestBody projet.carthagecreance_backend.DTO.TarifDossierRequest request) {
        try {
            projet.carthagecreance_backend.DTO.TarifDossierDTO dto = tarifDossierService.createTarif(dossierId, request);
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * POST /api/finances/tarifs/{tarifId}/valider
     * Valide un tarif
     */
    @PostMapping("/tarifs/{tarifId}/valider")
    public ResponseEntity<?> validerTarif(@PathVariable Long tarifId,
                                         @RequestBody(required = false) java.util.Map<String, String> body) {
        try {
            String commentaire = body != null ? body.get("commentaire") : null;
            projet.carthagecreance_backend.DTO.TarifDossierDTO dto = tarifDossierService.validerTarif(tarifId, commentaire);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * POST /api/finances/tarifs/{tarifId}/rejeter
     * Rejette un tarif
     */
    @PostMapping("/tarifs/{tarifId}/rejeter")
    public ResponseEntity<?> rejeterTarif(@PathVariable Long tarifId,
                                          @RequestBody java.util.Map<String, String> body) {
        try {
            String commentaire = body.get("commentaire");
            if (commentaire == null || commentaire.trim().isEmpty()) {
                return new ResponseEntity<>(java.util.Map.of("error", "Un commentaire est obligatoire pour rejeter un tarif"), 
                    HttpStatus.BAD_REQUEST);
            }
            projet.carthagecreance_backend.DTO.TarifDossierDTO dto = tarifDossierService.rejeterTarif(tarifId, commentaire);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * GET /api/finances/dossier/{dossierId}/validation-etat
     * Récupère l'état global de validation des tarifs
     */
    @GetMapping("/dossier/{dossierId}/validation-etat")
    public ResponseEntity<?> getValidationEtat(@PathVariable Long dossierId) {
        try {
            projet.carthagecreance_backend.DTO.ValidationEtatDTO dto = tarifDossierService.getValidationEtat(dossierId);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * GET /api/finances/dossier/{dossierId}/detail-facture
     * Récupère le détail de la facture avec les frais d'enquête inclus
     */
    @GetMapping("/dossier/{dossierId}/detail-facture")
    public ResponseEntity<?> getDetailFactureAmeliore(@PathVariable Long dossierId) {
        try {
            projet.carthagecreance_backend.DTO.DetailFactureDTO dto = tarifDossierService.getDetailFacture(dossierId);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * POST /api/finances/dossier/{dossierId}/generer-facture
     * Génère la facture une fois tous les tarifs validés
     */
    @PostMapping("/dossier/{dossierId}/generer-facture")
    public ResponseEntity<?> genererFacture(@PathVariable Long dossierId) {
        try {
            projet.carthagecreance_backend.DTO.FactureDTO facture = tarifDossierService.genererFacture(dossierId);
            return new ResponseEntity<>(facture, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * GET /api/finances/dossier/{dossierId}/tarifs
     * Récupère tous les tarifs d'un dossier
     */
    @GetMapping("/dossier/{dossierId}/tarifs")
    public ResponseEntity<?> getTarifsByDossier(@PathVariable Long dossierId) {
        try {
            java.util.List<projet.carthagecreance_backend.DTO.TarifDossierDTO> tarifs = tarifDossierService.getTarifsByDossier(dossierId);
            return new ResponseEntity<>(tarifs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
