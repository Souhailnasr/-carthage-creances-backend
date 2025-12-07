package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.DTO.AudienceRequestDTO;
import projet.carthagecreance_backend.Service.AudienceService;
import projet.carthagecreance_backend.Service.Impl.AudienceServiceImpl;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/audiences")
@CrossOrigin(origins = "*")
public class AudienceController {

    private static final Logger logger = LoggerFactory.getLogger(AudienceController.class);

    @Autowired
    private AudienceService audienceService;
    
    @Autowired
    private AudienceServiceImpl audienceServiceImpl;

    // CRUD Operations
    /**
     * Crée une nouvelle audience
     * Accepte AudienceRequestDTO avec soit dossierId soit dossier: {id}
     * 
     * @param dto DTO contenant les données de l'audience
     * @return L'audience créée avec toutes ses relations chargées
     * 
     * @example
     * POST /api/audiences
     * Body: {
     *   "dateAudience": "2025-11-17",
     *   "dossierId": 38,
     *   "avocatId": 3
     * }
     * 
     * OU
     * 
     * Body: {
     *   "dateAudience": "2025-11-17",
     *   "dossier": { "id": 38 },
     *   "avocat": { "id": 3 }
     * }
     */
    @PostMapping
    public ResponseEntity<?> createAudience(@RequestBody AudienceRequestDTO dto) {
        try {
            logger.info("📥 Requête de création d'audience reçue: {}", dto);
            logger.info("📥 Dossier ID: {}, Avocat ID: {}, Huissier ID: {}", 
                    dto.getDossierIdValue(), dto.getAvocatIdValue(), dto.getHuissierIdValue());
            
            Audience createdAudience = audienceServiceImpl.createAudienceFromDTO(dto);
            
            logger.info("✅ Audience créée avec succès, ID: {}, dossier_id: {}", 
                    createdAudience.getId(), 
                    createdAudience.getDossier() != null ? createdAudience.getDossier().getId() : "NULL");
            
            return new ResponseEntity<>(createdAudience, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la création de l'audience: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la création de l'audience",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Erreur interne lors de la création de l'audience: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Erreur interne du serveur",
                    "message", "Erreur lors de la création de l'audience: " + e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Audience> getAudienceById(@PathVariable Long id) {
        Optional<Audience> audience = audienceService.getAudienceById(id);
        return audience.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Audience>> getAllAudiences() {
        List<Audience> audiences = audienceService.getAllAudiences();
        return new ResponseEntity<>(audiences, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAudience(@PathVariable Long id, @RequestBody AudienceRequestDTO dto) {
        try {
            logger.info("📥 Mise à jour de l'audience {} avec DTO: {}", id, dto);
            Audience updatedAudience = audienceServiceImpl.updateAudienceFromDTO(id, dto);
            logger.info("✅ Audience mise à jour avec succès, dossier_id: {}", 
                    updatedAudience.getDossier() != null ? updatedAudience.getDossier().getId() : "NULL");
            return new ResponseEntity<>(updatedAudience, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la mise à jour: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la mise à jour de l'audience",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Erreur interne lors de la mise à jour: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Erreur interne du serveur",
                    "message", "Erreur lors de la mise à jour de l'audience: " + e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAudience(@PathVariable Long id) {
        try {
            audienceService.deleteAudience(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Search Operations
    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<Audience>> getAudiencesByDossier(@PathVariable Long dossierId) {
        List<Audience> audiences = audienceService.getAudiencesByDossier(dossierId);
        return new ResponseEntity<>(audiences, HttpStatus.OK);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Audience>> getAudiencesByDate(@PathVariable LocalDate date) {
        List<Audience> audiences = audienceService.getAudiencesByDate(date);
        return new ResponseEntity<>(audiences, HttpStatus.OK);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Audience>> getAudiencesByDateRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        List<Audience> audiences = audienceService.getAudiencesByDateRange(startDate, endDate);
        return new ResponseEntity<>(audiences, HttpStatus.OK);
    }

    @GetMapping("/tribunal-type/{tribunalType}")
    public ResponseEntity<List<Audience>> getAudiencesByTribunalType(@PathVariable TribunalType tribunalType) {
        List<Audience> audiences = audienceService.getAudiencesByTribunalType(tribunalType);
        return new ResponseEntity<>(audiences, HttpStatus.OK);
    }

    @GetMapping("/result/{result}")
    public ResponseEntity<List<Audience>> getAudiencesByResult(@PathVariable DecisionResult result) {
        List<Audience> audiences = audienceService.getAudiencesByResult(result);
        return new ResponseEntity<>(audiences, HttpStatus.OK);
    }

    @GetMapping("/avocat/{avocatId}")
    public ResponseEntity<List<Audience>> getAudiencesByAvocat(@PathVariable Long avocatId) {
        List<Audience> audiences = audienceService.getAudiencesByAvocat(avocatId);
        return new ResponseEntity<>(audiences, HttpStatus.OK);
    }

    @GetMapping("/huissier/{huissierId}")
    public ResponseEntity<List<Audience>> getAudiencesByHuissier(@PathVariable Long huissierId) {
        List<Audience> audiences = audienceService.getAudiencesByHuissier(huissierId);
        return new ResponseEntity<>(audiences, HttpStatus.OK);
    }

    @GetMapping("/location")
    public ResponseEntity<List<Audience>> getAudiencesByLocation(@RequestParam String location) {
        List<Audience> audiences = audienceService.getAudiencesByLocation(location);
        return new ResponseEntity<>(audiences, HttpStatus.OK);
    }

    // Special Operations
    @GetMapping("/upcoming")
    public ResponseEntity<List<Audience>> getUpcomingAudiences() {
        List<Audience> audiences = audienceService.getUpcomingAudiences();
        return new ResponseEntity<>(audiences, HttpStatus.OK);
    }

    @GetMapping("/past")
    public ResponseEntity<List<Audience>> getPastAudiences() {
        List<Audience> audiences = audienceService.getPastAudiences();
        return new ResponseEntity<>(audiences, HttpStatus.OK);
    }

    @GetMapping("/dossier/{dossierId}/next")
    public ResponseEntity<List<Audience>> getNextAudienceByDossier(@PathVariable Long dossierId) {
        List<Audience> audiences = audienceService.getNextAudienceByDossier(dossierId);
        return new ResponseEntity<>(audiences, HttpStatus.OK);
    }
}
