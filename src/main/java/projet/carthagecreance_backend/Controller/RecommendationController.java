package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Recommendation;
import projet.carthagecreance_backend.Service.RecommendationService;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion des recommandations intelligentes
 */
@RestController
@RequestMapping({"/api/recommendations", "/recommendations"})
@CrossOrigin(origins = "*")
public class RecommendationController {
    
    @Autowired
    private RecommendationService recommendationService;
    
    /**
     * Récupère les recommandations d'un dossier
     * GET /api/recommendations?dossierId={id}
     */
    @GetMapping
    public ResponseEntity<?> getRecommendations(
            @RequestParam(required = false) Long dossierId) {
        try {
            List<Recommendation> recommendations;
            if (dossierId != null) {
                recommendations = recommendationService.getRecommendationsByDossier(dossierId);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "dossierId est requis"));
            }
            return new ResponseEntity<>(recommendations, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération: " + e.getMessage()));
        }
    }
    
    /**
     * Acquitte une recommandation
     * POST /api/recommendations/{id}/ack
     */
    @PostMapping("/{id}/ack")
    public ResponseEntity<?> acknowledgeRecommendation(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Long userId = body.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "userId est requis"));
            }
            
            recommendationService.acknowledgeRecommendation(id, userId);
            return ResponseEntity.ok(Map.of("message", "Recommandation acquittée avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'acquittement: " + e.getMessage()));
        }
    }
}

