package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.DTO.ActionHuissierDTO;
import projet.carthagecreance_backend.Entity.ActionHuissier;
import projet.carthagecreance_backend.Service.ActionHuissierService;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion des actions huissier (Phase 3 - Exécution)
 */
@RestController
@RequestMapping({"/api/huissier", "/huissier"})
@CrossOrigin(origins = "*")
public class HuissierActionController {
    
    @Autowired
    private ActionHuissierService actionHuissierService;
    
    /**
     * Crée une action huissier (saisie conservatoire, exécutive, etc.)
     * POST /api/huissier/action
     */
    @PostMapping("/action")
    public ResponseEntity<?> createAction(@RequestBody ActionHuissierDTO dto) {
        try {
            // Validation
            if (dto.getDossierId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "dossierId est requis"));
            }
            if (dto.getTypeAction() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "typeAction est requis"));
            }
            if (dto.getHuissierName() == null || dto.getHuissierName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "huissierName est requis"));
            }
            
            // Valider montantRecouvre si fourni
            if (dto.getMontantRecouvre() != null && 
                dto.getMontantRecouvre().compareTo(java.math.BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "montantRecouvre ne peut pas être négatif"));
            }
            
            ActionHuissier action = actionHuissierService.createAction(dto);
            return new ResponseEntity<>(action, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }
    
    /**
     * Récupère une action par son ID
     * GET /api/huissier/action/{id}
     */
    @GetMapping("/action/{id}")
    public ResponseEntity<?> getActionById(@PathVariable Long id) {
        try {
            ActionHuissier action = actionHuissierService.getActionById(id);
            return new ResponseEntity<>(action, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Récupère toutes les actions d'un dossier
     * GET /api/huissier/actions?dossierId={id}
     */
    @GetMapping("/actions")
    public ResponseEntity<?> getActionsByDossier(@RequestParam Long dossierId) {
        try {
            List<ActionHuissier> actions = actionHuissierService.getActionsByDossier(dossierId);
            return new ResponseEntity<>(actions, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération: " + e.getMessage()));
        }
    }
}

