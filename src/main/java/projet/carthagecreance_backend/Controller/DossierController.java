package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Urgence;
import projet.carthagecreance_backend.Service.DossierService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dossiers")
@CrossOrigin(origins = "*")
public class DossierController {

    @Autowired
    private DossierService dossierService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<Dossier> createDossier(@RequestBody Dossier dossier) {
        try {
            Dossier createdDossier = dossierService.createDossier(dossier);
            return new ResponseEntity<>(createdDossier, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dossier> getDossierById(@PathVariable Long id) {
        Optional<Dossier> dossier = dossierService.getDossierById(id);
        return dossier.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Dossier>> getAllDossiers() {
        List<Dossier> dossiers = dossierService.getAllDossiers();
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dossier> updateDossier(@PathVariable Long id, @RequestBody Dossier dossier) {
        try {
            Dossier updatedDossier = dossierService.updateDossier(id, dossier);
            return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDossier(@PathVariable Long id) {
        try {
            dossierService.deleteDossier(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Search Operations
    @GetMapping("/number/{numeroDossier}")
    public ResponseEntity<Dossier> getDossierByNumber(@PathVariable String numeroDossier) {
        Optional<Dossier> dossier = dossierService.getDossierByNumber(numeroDossier);
        return dossier.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<Dossier>> getDossiersByTitle(@RequestParam String title) {
        List<Dossier> dossiers = dossierService.getDossiersByTitle(title);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/search/description")
    public ResponseEntity<List<Dossier>> getDossiersByDescription(@RequestParam String description) {
        List<Dossier> dossiers = dossierService.getDossiersByDescription(description);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/urgency/{urgency}")
    public ResponseEntity<List<Dossier>> getDossiersByUrgency(@PathVariable Urgence urgency) {
        List<Dossier> dossiers = dossierService.getDossiersByUrgency(urgency);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/avocat/{avocatId}")
    public ResponseEntity<List<Dossier>> getDossiersByAvocat(@PathVariable Long avocatId) {
        List<Dossier> dossiers = dossierService.getDossiersByAvocat(avocatId);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/huissier/{huissierId}")
    public ResponseEntity<List<Dossier>> getDossiersByHuissier(@PathVariable Long huissierId) {
        List<Dossier> dossiers = dossierService.getDossiersByHuissier(huissierId);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/creancier/{creancierId}")
    public ResponseEntity<List<Dossier>> getDossiersByCreancier(@PathVariable Long creancierId) {
        List<Dossier> dossiers = dossierService.getDossiersByCreancier(creancierId);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/debiteur/{debiteurId}")
    public ResponseEntity<List<Dossier>> getDossiersByDebiteur(@PathVariable Long debiteurId) {
        List<Dossier> dossiers = dossierService.getDossiersByDebiteur(debiteurId);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Dossier>> getDossiersByUser(@PathVariable Long userId) {
        List<Dossier> dossiers = dossierService.getDossiersByUser(userId);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/creation-date/{date}")
    public ResponseEntity<List<Dossier>> getDossiersByCreationDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        List<Dossier> dossiers = dossierService.getDossiersByCreationDate(date);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/creation-date-range")
    public ResponseEntity<List<Dossier>> getDossiersByCreationDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<Dossier> dossiers = dossierService.getDossiersByCreationDateRange(startDate, endDate);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/closure-date/{date}")
    public ResponseEntity<List<Dossier>> getDossiersByClosureDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        List<Dossier> dossiers = dossierService.getDossiersByClosureDate(date);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/amount/{amount}")
    public ResponseEntity<List<Dossier>> getDossiersByAmount(@PathVariable Double amount) {
        List<Dossier> dossiers = dossierService.getDossiersByAmount(amount);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/amount-range")
    public ResponseEntity<List<Dossier>> getDossiersByAmountRange(
            @RequestParam Double minAmount, 
            @RequestParam Double maxAmount) {
        List<Dossier> dossiers = dossierService.getDossiersByAmountRange(minAmount, maxAmount);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Dossier>> searchDossiers(@RequestParam String searchTerm) {
        List<Dossier> dossiers = dossierService.searchDossiers(searchTerm);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    // Special Operations
    @GetMapping("/open")
    public ResponseEntity<List<Dossier>> getOpenDossiers() {
        List<Dossier> dossiers = dossierService.getOpenDossiers();
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/closed")
    public ResponseEntity<List<Dossier>> getClosedDossiers() {
        List<Dossier> dossiers = dossierService.getClosedDossiers();
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Dossier>> getRecentDossiers() {
        List<Dossier> dossiers = dossierService.getRecentDossiers();
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    // Validation Operations
    @GetMapping("/exists/number/{numeroDossier}")
    public ResponseEntity<Boolean> existsByNumber(@PathVariable String numeroDossier) {
        boolean exists = dossierService.existsByNumber(numeroDossier);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
}
