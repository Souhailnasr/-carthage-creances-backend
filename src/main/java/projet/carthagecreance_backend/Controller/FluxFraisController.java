package projet.carthagecreance_backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projet.carthagecreance_backend.DTO.FluxFraisDTO;
import projet.carthagecreance_backend.DTO.ValidationFraisDTO;
import projet.carthagecreance_backend.Entity.FluxFrais;
import projet.carthagecreance_backend.Entity.PhaseFrais;
import projet.carthagecreance_backend.Entity.StatutFrais;
import projet.carthagecreance_backend.Service.FluxFraisService;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/frais")
@CrossOrigin(origins = "*")
public class FluxFraisController {

    private static final Logger logger = LoggerFactory.getLogger(FluxFraisController.class);

    @Autowired
    private FluxFraisService fluxFraisService;

    @PostMapping
    public ResponseEntity<?> createFluxFrais(@RequestBody FluxFraisDTO dto) {
        try {
            FluxFrais fluxFrais = fluxFraisService.createFluxFrais(dto);
            return new ResponseEntity<>(fluxFrais, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du flux de frais: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la création du flux de frais",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFluxFraisById(@PathVariable Long id) {
        Optional<FluxFrais> fluxFrais = fluxFraisService.getFluxFraisById(id);
        return fluxFrais.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<FluxFrais>> getAllFluxFrais() {
        List<FluxFrais> fluxFrais = fluxFraisService.getAllFluxFrais();
        return new ResponseEntity<>(fluxFrais, HttpStatus.OK);
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<FluxFrais>> getFluxFraisByDossier(@PathVariable Long dossierId) {
        List<FluxFrais> fluxFrais = fluxFraisService.getFluxFraisByDossier(dossierId);
        return new ResponseEntity<>(fluxFrais, HttpStatus.OK);
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<FluxFrais>> getFluxFraisByStatut(@PathVariable StatutFrais statut) {
        List<FluxFrais> fluxFrais = fluxFraisService.getFluxFraisByStatut(statut);
        return new ResponseEntity<>(fluxFrais, HttpStatus.OK);
    }

    @GetMapping("/en-attente")
    public ResponseEntity<List<FluxFrais>> getFluxFraisEnAttente() {
        List<FluxFrais> fluxFrais = fluxFraisService.getFluxFraisEnAttente();
        return new ResponseEntity<>(fluxFrais, HttpStatus.OK);
    }

    @GetMapping("/phase/{phase}")
    public ResponseEntity<List<FluxFrais>> getFluxFraisByPhase(@PathVariable PhaseFrais phase) {
        List<FluxFrais> fluxFrais = fluxFraisService.getFluxFraisByPhase(phase);
        return new ResponseEntity<>(fluxFrais, HttpStatus.OK);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<FluxFrais>> getFluxFraisByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<FluxFrais> fluxFrais = fluxFraisService.getFluxFraisByDateRange(startDate, endDate);
        return new ResponseEntity<>(fluxFrais, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFluxFrais(@PathVariable Long id, @RequestBody FluxFraisDTO dto) {
        try {
            FluxFrais fluxFrais = fluxFraisService.updateFluxFrais(id, dto);
            return new ResponseEntity<>(fluxFrais, HttpStatus.OK);
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
    public ResponseEntity<Void> deleteFluxFrais(@PathVariable Long id) {
        try {
            fluxFraisService.deleteFluxFrais(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<?> validerFrais(@PathVariable Long id, @RequestBody ValidationFraisDTO dto) {
        try {
            FluxFrais fluxFrais = fluxFraisService.validerFrais(id, dto);
            return new ResponseEntity<>(fluxFrais, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la validation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la validation",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @PutMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterFrais(@PathVariable Long id, @RequestBody ValidationFraisDTO dto) {
        try {
            FluxFrais fluxFrais = fluxFraisService.rejeterFrais(id, dto);
            return new ResponseEntity<>(fluxFrais, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors du rejet: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors du rejet",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @PostMapping("/action/{actionId}")
    public ResponseEntity<?> creerFraisDepuisAction(@PathVariable Long actionId) {
        try {
            FluxFrais fluxFrais = fluxFraisService.creerFraisDepuisAction(actionId);
            return new ResponseEntity<>(fluxFrais, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Erreur lors de la création depuis action: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la création depuis action",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @PostMapping("/enquete/{enqueteId}")
    public ResponseEntity<?> creerFraisDepuisEnquete(@PathVariable Long enqueteId) {
        try {
            FluxFrais fluxFrais = fluxFraisService.creerFraisDepuisEnquete(enqueteId);
            return new ResponseEntity<>(fluxFrais, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Erreur lors de la création depuis enquête: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la création depuis enquête",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @PostMapping("/audience/{audienceId}")
    public ResponseEntity<?> creerFraisDepuisAudience(@PathVariable Long audienceId) {
        try {
            FluxFrais fluxFrais = fluxFraisService.creerFraisDepuisAudience(audienceId);
            return new ResponseEntity<>(fluxFrais, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Erreur lors de la création depuis audience: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la création depuis audience",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @GetMapping("/dossier/{dossierId}/total")
    public ResponseEntity<Double> calculerTotalFraisByDossier(@PathVariable Long dossierId) {
        Double total = fluxFraisService.calculerTotalFraisByDossier(dossierId);
        return new ResponseEntity<>(total, HttpStatus.OK);
    }

    @GetMapping("/statut/{statut}/total")
    public ResponseEntity<Double> calculerTotalFraisByStatut(@PathVariable StatutFrais statut) {
        Double total = fluxFraisService.calculerTotalFraisByStatut(statut);
        return new ResponseEntity<>(total, HttpStatus.OK);
    }
    
    @PostMapping("/import-csv")
    public ResponseEntity<?> importerFraisDepuisCSV(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Fichier vide",
                        "message", "Le fichier CSV est vide",
                        "timestamp", new Date().toString()
                ));
            }
            
            byte[] csvContent = file.getBytes();
            Map<String, Object> result = fluxFraisService.importerFraisDepuisCSV(csvContent);
            
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de l'import CSV: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de l'import CSV",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }
}

