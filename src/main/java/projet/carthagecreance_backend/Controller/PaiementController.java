package projet.carthagecreance_backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.DTO.PaiementDTO;
import projet.carthagecreance_backend.Entity.Paiement;
import projet.carthagecreance_backend.Entity.StatutPaiement;
import projet.carthagecreance_backend.Service.PaiementService;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/paiements")
@CrossOrigin(origins = "*")
public class PaiementController {

    private static final Logger logger = LoggerFactory.getLogger(PaiementController.class);

    @Autowired
    private PaiementService paiementService;

    @PostMapping
    public ResponseEntity<?> createPaiement(@RequestBody PaiementDTO dto) {
        try {
            Paiement paiement = paiementService.createPaiement(dto);
            return new ResponseEntity<>(paiement, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du paiement: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la création du paiement",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPaiementById(@PathVariable Long id) {
        Optional<Paiement> paiement = paiementService.getPaiementById(id);
        return paiement.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Paiement>> getAllPaiements() {
        List<Paiement> paiements = paiementService.getAllPaiements();
        return new ResponseEntity<>(paiements, HttpStatus.OK);
    }

    @GetMapping("/facture/{factureId}")
    public ResponseEntity<List<Paiement>> getPaiementsByFacture(@PathVariable Long factureId) {
        List<Paiement> paiements = paiementService.getPaiementsByFacture(factureId);
        return new ResponseEntity<>(paiements, HttpStatus.OK);
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Paiement>> getPaiementsByStatut(@PathVariable StatutPaiement statut) {
        List<Paiement> paiements = paiementService.getPaiementsByStatut(statut);
        return new ResponseEntity<>(paiements, HttpStatus.OK);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Paiement>> getPaiementsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Paiement> paiements = paiementService.getPaiementsByDateRange(startDate, endDate);
        return new ResponseEntity<>(paiements, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePaiement(@PathVariable Long id, @RequestBody PaiementDTO dto) {
        try {
            Paiement paiement = paiementService.updatePaiement(id, dto);
            return new ResponseEntity<>(paiement, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la mise à jour: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la mise à jour",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaiement(@PathVariable Long id) {
        try {
            paiementService.deletePaiement(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<?> validerPaiement(@PathVariable Long id) {
        try {
            Paiement paiement = paiementService.validerPaiement(id);
            return new ResponseEntity<>(paiement, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la validation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la validation",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @PutMapping("/{id}/refuser")
    public ResponseEntity<?> refuserPaiement(@PathVariable Long id, @RequestParam String motif) {
        try {
            Paiement paiement = paiementService.refuserPaiement(id, motif);
            return new ResponseEntity<>(paiement, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors du refus: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors du refus",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @GetMapping("/facture/{factureId}/total")
    public ResponseEntity<Double> calculerTotalPaiementsByFacture(@PathVariable Long factureId) {
        Double total = paiementService.calculerTotalPaiementsByFacture(factureId);
        return new ResponseEntity<>(total, HttpStatus.OK);
    }

    @GetMapping("/date-range/total")
    public ResponseEntity<Double> calculerTotalPaiementsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        Double total = paiementService.calculerTotalPaiementsByDateRange(startDate, endDate);
        return new ResponseEntity<>(total, HttpStatus.OK);
    }
}

