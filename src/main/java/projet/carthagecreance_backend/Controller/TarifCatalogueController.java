package projet.carthagecreance_backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.DTO.TarifCatalogueDTO;
import projet.carthagecreance_backend.Entity.PhaseFrais;
import projet.carthagecreance_backend.Entity.TarifCatalogue;
import projet.carthagecreance_backend.Service.TarifCatalogueService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tarifs")
@CrossOrigin(origins = "*")
public class TarifCatalogueController {

    private static final Logger logger = LoggerFactory.getLogger(TarifCatalogueController.class);

    @Autowired
    private TarifCatalogueService tarifCatalogueService;

    @PostMapping
    public ResponseEntity<?> createTarif(@RequestBody TarifCatalogueDTO dto) {
        try {
            TarifCatalogue tarif = tarifCatalogueService.createTarif(dto);
            return new ResponseEntity<>(tarif, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du tarif: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la création du tarif",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTarifById(@PathVariable Long id) {
        Optional<TarifCatalogue> tarif = tarifCatalogueService.getTarifById(id);
        return tarif.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<TarifCatalogue>> getAllTarifs() {
        List<TarifCatalogue> tarifs = tarifCatalogueService.getAllTarifs();
        return new ResponseEntity<>(tarifs, HttpStatus.OK);
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<TarifCatalogue>> getTarifsActifs() {
        List<TarifCatalogue> tarifs = tarifCatalogueService.getTarifsActifs();
        return new ResponseEntity<>(tarifs, HttpStatus.OK);
    }

    @GetMapping("/phase/{phase}")
    public ResponseEntity<List<TarifCatalogue>> getTarifsByPhase(@PathVariable PhaseFrais phase) {
        List<TarifCatalogue> tarifs = tarifCatalogueService.getTarifsByPhase(phase);
        return new ResponseEntity<>(tarifs, HttpStatus.OK);
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<TarifCatalogue>> getTarifsByCategorie(@PathVariable String categorie) {
        List<TarifCatalogue> tarifs = tarifCatalogueService.getTarifsByCategorie(categorie);
        return new ResponseEntity<>(tarifs, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTarif(@PathVariable Long id, @RequestBody TarifCatalogueDTO dto) {
        try {
            TarifCatalogue tarif = tarifCatalogueService.updateTarif(id, dto);
            return new ResponseEntity<>(tarif, HttpStatus.OK);
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
    public ResponseEntity<Void> deleteTarif(@PathVariable Long id) {
        try {
            tarifCatalogueService.deleteTarif(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverTarif(@PathVariable Long id) {
        try {
            tarifCatalogueService.desactiverTarif(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la désactivation",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}/historique")
    public ResponseEntity<List<TarifCatalogue>> getHistoriqueTarif(@PathVariable Long id) {
        List<TarifCatalogue> historique = tarifCatalogueService.getHistoriqueTarif(id);
        return new ResponseEntity<>(historique, HttpStatus.OK);
    }
}

