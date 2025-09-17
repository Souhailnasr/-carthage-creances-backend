package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Audience;
import projet.carthagecreance_backend.Entity.DecisionResult;
import projet.carthagecreance_backend.Entity.TribunalType;
import projet.carthagecreance_backend.Service.AudienceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/audiences")
@CrossOrigin(origins = "*")
public class AudienceController {

    @Autowired
    private AudienceService audienceService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<Audience> createAudience(@RequestBody Audience audience) {
        try {
            Audience createdAudience = audienceService.createAudience(audience);
            return new ResponseEntity<>(createdAudience, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<Audience> updateAudience(@PathVariable Long id, @RequestBody Audience audience) {
        try {
            Audience updatedAudience = audienceService.updateAudience(id, audience);
            return new ResponseEntity<>(updatedAudience, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
