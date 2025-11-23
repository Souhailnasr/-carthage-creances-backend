package projet.carthagecreance_backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.DTO.FactureDTO;
import projet.carthagecreance_backend.Entity.Facture;
import projet.carthagecreance_backend.Entity.FactureStatut;
import projet.carthagecreance_backend.Service.FactureService;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/factures")
@CrossOrigin(origins = "*")
public class FactureController {

    private static final Logger logger = LoggerFactory.getLogger(FactureController.class);

    @Autowired
    private FactureService factureService;

    @PostMapping
    public ResponseEntity<?> createFacture(@RequestBody FactureDTO dto) {
        try {
            Facture facture = factureService.createFacture(dto);
            return new ResponseEntity<>(facture, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la facture: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la création de la facture",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFactureById(@PathVariable Long id) {
        Optional<Facture> facture = factureService.getFactureById(id);
        return facture.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<?> getFactureByNumero(@PathVariable String numero) {
        Optional<Facture> facture = factureService.getFactureByNumero(numero);
        return facture.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Facture>> getAllFactures() {
        List<Facture> factures = factureService.getAllFactures();
        return new ResponseEntity<>(factures, HttpStatus.OK);
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<Facture>> getFacturesByDossier(@PathVariable Long dossierId) {
        List<Facture> factures = factureService.getFacturesByDossier(dossierId);
        return new ResponseEntity<>(factures, HttpStatus.OK);
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Facture>> getFacturesByStatut(@PathVariable FactureStatut statut) {
        List<Facture> factures = factureService.getFacturesByStatut(statut);
        return new ResponseEntity<>(factures, HttpStatus.OK);
    }

    @GetMapping("/en-retard")
    public ResponseEntity<List<Facture>> getFacturesEnRetard() {
        List<Facture> factures = factureService.getFacturesEnRetard();
        return new ResponseEntity<>(factures, HttpStatus.OK);
    }

    @PostMapping("/dossier/{dossierId}/generer")
    public ResponseEntity<?> genererFactureAutomatique(
            @PathVariable Long dossierId,
            @RequestParam(required = false) LocalDate periodeDebut,
            @RequestParam(required = false) LocalDate periodeFin) {
        try {
            Facture facture = factureService.genererFactureAutomatique(
                    dossierId,
                    periodeDebut != null ? periodeDebut : LocalDate.now().minusMonths(1),
                    periodeFin != null ? periodeFin : LocalDate.now()
            );
            return new ResponseEntity<>(facture, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération de la facture: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la génération de la facture",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @PutMapping("/{id}/finaliser")
    public ResponseEntity<?> finaliserFacture(@PathVariable Long id) {
        try {
            Facture facture = factureService.finaliserFacture(id);
            return new ResponseEntity<>(facture, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la finalisation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la finalisation",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @PutMapping("/{id}/envoyer")
    public ResponseEntity<?> envoyerFacture(@PathVariable Long id) {
        try {
            Facture facture = factureService.envoyerFacture(id);
            return new ResponseEntity<>(facture, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de l'envoi: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de l'envoi",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @PutMapping("/{id}/relancer")
    public ResponseEntity<?> relancerFacture(@PathVariable Long id) {
        try {
            Facture facture = factureService.relancerFacture(id);
            return new ResponseEntity<>(facture, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la relance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la relance",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> genererPdfFacture(@PathVariable Long id) {
        try {
            byte[] pdf = factureService.genererPdfFacture(id);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .body(pdf);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération PDF: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFacture(@PathVariable Long id, @RequestBody FactureDTO dto) {
        try {
            Facture facture = factureService.updateFacture(id, dto);
            return new ResponseEntity<>(facture, HttpStatus.OK);
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
    public ResponseEntity<Void> deleteFacture(@PathVariable Long id) {
        try {
            factureService.deleteFacture(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

