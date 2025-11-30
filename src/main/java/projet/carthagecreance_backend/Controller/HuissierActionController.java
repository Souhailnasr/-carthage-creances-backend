package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projet.carthagecreance_backend.DTO.ActionHuissierDTO;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Service.ActionHuissierService;
import projet.carthagecreance_backend.Service.FileStorageService;

import java.math.BigDecimal;
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
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private DossierRepository dossierRepository;
    
    /**
     * Crée une action huissier (saisie conservatoire, exécutive, etc.)
     * POST /api/huissier/action
     * 
     * Compatible avec deux formats :
     * 1. JSON avec ActionHuissierDTO (ancien format)
     * 2. Form-data avec MultipartFile (nouveau format)
     */
    @PostMapping("/action")
    public ResponseEntity<?> createAction(
            @RequestParam(value = "dossierId", required = false) Long dossierId,
            @RequestParam(value = "typeAction", required = false) String typeActionStr,
            @RequestParam(value = "huissierName", required = false) String huissierName,
            @RequestParam(value = "montantRecouvre", required = false) String montantRecouvreStr,
            @RequestParam(value = "pieceJointe", required = false) MultipartFile file,
            @RequestBody(required = false) ActionHuissierDTO dto
    ) {
        try {
            ActionHuissierDTO finalDto;
            
            // Si des paramètres de formulaire sont fournis, utiliser le nouveau format
            if (dossierId != null || typeActionStr != null || huissierName != null) {
                // Validation des paramètres requis
                if (dossierId == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "dossierId est requis"));
                }
                if (typeActionStr == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "typeAction est requis"));
                }
                if (huissierName == null || huissierName.trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "huissierName est requis"));
                }
                
                // Convertir le typeAction
                TypeActionHuissier typeAction;
                try {
                    typeAction = TypeActionHuissier.valueOf(typeActionStr);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Type d'action invalide: " + typeActionStr));
                }
                
                // Créer le DTO
                finalDto = ActionHuissierDTO.builder()
                        .dossierId(dossierId)
                        .typeAction(typeAction)
                        .huissierName(huissierName)
                        .build();
                
                // Gérer le montantRecouvre si fourni
                if (montantRecouvreStr != null && !montantRecouvreStr.trim().isEmpty()) {
                    try {
                        BigDecimal montantRecouvre = new BigDecimal(montantRecouvreStr);
                        if (montantRecouvre.compareTo(BigDecimal.ZERO) < 0) {
                            return ResponseEntity.badRequest()
                                    .body(Map.of("error", "montantRecouvre ne peut pas être négatif"));
                        }
                        finalDto.setMontantRecouvre(montantRecouvre);
                    } catch (NumberFormatException e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "montantRecouvre doit être un nombre valide"));
                    }
                }
                
                // Gérer le fichier si présent
                if (file != null && !file.isEmpty()) {
                    try {
                        fileStorageService.validateFile(file);
                        String fileUrl = fileStorageService.storeFile(file, "huissier/actions");
                        finalDto.setPieceJointeUrl(fileUrl);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", e.getMessage()));
                    }
                }
            } else if (dto != null) {
                // Utiliser l'ancien format (JSON)
                finalDto = dto;
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Données manquantes. Utilisez soit form-data soit JSON"));
            }
            
            // Validation finale
            if (finalDto.getDossierId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "dossierId est requis"));
            }
            if (finalDto.getTypeAction() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "typeAction est requis"));
            }
            if (finalDto.getHuissierName() == null || finalDto.getHuissierName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "huissierName est requis"));
            }
            
            // Valider montantRecouvre si fourni
            if (finalDto.getMontantRecouvre() != null && 
                finalDto.getMontantRecouvre().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "montantRecouvre ne peut pas être négatif"));
            }
            
            ActionHuissier action = actionHuissierService.createAction(finalDto);
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

