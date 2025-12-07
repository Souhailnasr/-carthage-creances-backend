package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projet.carthagecreance_backend.DTO.ActionHuissierDTO;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Service.ActionHuissierService;
import projet.carthagecreance_backend.SecurityServices.UserExtractionService;
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
    private UserExtractionService userExtractionService;
    
    /**
     * Crée une action huissier (saisie conservatoire, exécutive, etc.)
     * POST /api/huissier/action
     * 
     * Compatible avec deux formats :
     * 1. JSON avec ActionHuissierDTO (sans fichier)
     * 2. multipart/form-data avec @RequestPart (avec fichier)
     */
    @PostMapping(value = "/action", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createAction(
            @RequestPart(value = "dto", required = false) ActionHuissierDTO dto,
            @RequestPart(value = "dossierId", required = false) String dossierIdStr,
            @RequestPart(value = "typeAction", required = false) String typeActionStr,
            @RequestPart(value = "huissierName", required = false) String huissierName,
            @RequestPart(value = "montantRecouvre", required = false) String montantRecouvreStr,
            @RequestPart(value = "updateMode", required = false) String updateModeStr,
            @RequestPart(value = "pieceJointe", required = false) MultipartFile pieceJointe,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            ActionHuissierDTO finalDto;
            
            // Si dto est fourni (JSON), l'utiliser directement
            if (dto != null) {
                finalDto = dto;
            } else if (dossierIdStr != null || typeActionStr != null || huissierName != null) {
                // Sinon, construire depuis les paramètres multipart
                // Validation des paramètres requis
                if (dossierIdStr == null || dossierIdStr.trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "dossierId est requis"));
                }
                if (typeActionStr == null || typeActionStr.trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "typeAction est requis"));
                }
                if (huissierName == null || huissierName.trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "huissierName est requis"));
                }
                
                // Convertir dossierId
                Long dossierId;
                try {
                    dossierId = Long.parseLong(dossierIdStr);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "dossierId doit être un nombre valide"));
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
                
                // Gérer updateMode si fourni
                if (updateModeStr != null && !updateModeStr.trim().isEmpty()) {
                    try {
                        ModeMiseAJour updateMode = ModeMiseAJour.valueOf(updateModeStr);
                        finalDto.setUpdateMode(updateMode);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "updateMode invalide: " + updateModeStr));
                    }
                }
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Données manquantes. Utilisez soit JSON (dto) soit multipart/form-data"));
            }
            
            // Traiter le fichier si présent (pour les deux formats)
            if (pieceJointe != null && !pieceJointe.isEmpty()) {
                try {
                    fileStorageService.validateFile(pieceJointe);
                    String fileUrl = fileStorageService.storeFile(pieceJointe, "huissier/actions");
                    finalDto.setPieceJointeUrl(fileUrl);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", e.getMessage()));
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Erreur lors de la sauvegarde du fichier: " + e.getMessage()));
                }
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
            
            // ✅ Extraire l'utilisateur pour la traçabilité
            Utilisateur utilisateur = userExtractionService.extractUserFromToken(authHeader);
            Long utilisateurId = utilisateur != null ? utilisateur.getId() : null;
            
            // ✅ Définir l'utilisateur dans le DTO pour la traçabilité
            if (finalDto != null && utilisateurId != null) {
                finalDto.setUtilisateurId(utilisateurId);
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

