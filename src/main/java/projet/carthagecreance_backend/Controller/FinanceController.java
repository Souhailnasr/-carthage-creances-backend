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
            org.springframework.data.domain.Page<Finance> pageResult = financeService.getDossiersAvecCouts(page, size, sort);
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
}
