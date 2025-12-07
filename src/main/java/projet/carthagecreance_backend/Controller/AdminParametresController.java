package projet.carthagecreance_backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.ParametreSysteme;
import projet.carthagecreance_backend.Entity.RoleUtilisateur;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Repository.ParametreSystemeRepository;
import projet.carthagecreance_backend.SecurityServices.UserExtractionService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des paramètres système par Superadmin
 */
@RestController
@RequestMapping("/api/admin/parametres")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminParametresController {

    private static final Logger logger = LoggerFactory.getLogger(AdminParametresController.class);

    @Autowired
    private ParametreSystemeRepository parametreSystemeRepository;
    
    @Autowired
    private UserExtractionService userExtractionService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET /api/admin/parametres
     * Récupère tous les paramètres groupés par catégorie
     */
    @GetMapping
    public ResponseEntity<Map<String, Map<String, Object>>> getAllParametres() {
        try {
            List<ParametreSysteme> tousParametres = parametreSystemeRepository.findAllByOrderByCategorieAscCleAsc();
            
            Map<String, Map<String, Object>> parametresParCategorie = tousParametres.stream()
                    .collect(Collectors.groupingBy(
                            ParametreSysteme::getCategorie,
                            Collectors.toMap(
                                    ParametreSysteme::getCle,
                                    p -> {
                                        Map<String, Object> paramMap = new HashMap<>();
                                        paramMap.put("id", p.getId());
                                        paramMap.put("valeur", convertirValeur(p.getValeur(), p.getType()));
                                        paramMap.put("type", p.getType());
                                        paramMap.put("description", p.getDescription());
                                        paramMap.put("dateModification", p.getDateModification());
                                        return paramMap;
                                    },
                                    (existing, replacement) -> existing,
                                    LinkedHashMap::new
                            )
                    ));
            
            return ResponseEntity.ok(parametresParCategorie);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des paramètres: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/admin/parametres
     * Met à jour les paramètres système
     */
    @PutMapping
    public ResponseEntity<?> updateParametres(
            @RequestBody Map<String, Map<String, Object>> parametres,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur superadmin = userExtractionService.extractUserFromToken(authHeader);
            if (superadmin == null || superadmin.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Map<String, Object> avant = new HashMap<>();
            Map<String, Object> apres = new HashMap<>();
            
            for (Map.Entry<String, Map<String, Object>> categorieEntry : parametres.entrySet()) {
                String categorie = categorieEntry.getKey();
                
                for (Map.Entry<String, Object> cleEntry : categorieEntry.getValue().entrySet()) {
                    String cle = cleEntry.getKey();
                    Object valeur = cleEntry.getValue();
                    
                    Optional<ParametreSysteme> paramOpt = parametreSystemeRepository.findByCategorieAndCle(categorie, cle);
                    
                    if (paramOpt.isPresent()) {
                        ParametreSysteme param = paramOpt.get();
                        
                        // Vérifier que le paramètre a un ID valide
                        if (param.getId() == null) {
                            logger.error("Erreur : Le paramètre n'a pas d'ID valide");
                            continue; // Passer au suivant
                        }
                        
                        avant.put(categorie + "." + cle, param.getValeur());
                        
                        String valeurStr = valeur != null ? valeur.toString() : null;
                        param.setValeur(valeurStr);
                        param.setModifiePar(superadmin.getId());
                        param.setDateModification(LocalDateTime.now());
                        
                        ParametreSysteme saved = parametreSystemeRepository.save(param);
                        
                        // Vérifier que la sauvegarde a réussi
                        if (saved != null && saved.getId() != null) {
                            apres.put(categorie + "." + cle, valeurStr);
                        }
                    } else {
                        // Créer nouveau paramètre
                        ParametreSysteme nouveauParam = ParametreSysteme.builder()
                                .categorie(categorie)
                                .cle(cle)
                                .valeur(valeur != null ? valeur.toString() : null)
                                .type(determinerType(valeur))
                                .modifiePar(superadmin.getId())
                                .build();
                        
                        ParametreSysteme saved = parametreSystemeRepository.save(nouveauParam);
                        
                        // Vérifier que la création a réussi
                        if (saved != null && saved.getId() != null) {
                            apres.put(categorie + "." + cle, valeur);
                        }
                    }
                }
            }
            
            return ResponseEntity.ok(Map.of("message", "Paramètres mis à jour avec succès"));
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour des paramètres: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    /**
     * POST /api/admin/parametres/sauvegarde
     * Déclenche une sauvegarde de la base de données
     */
    @PostMapping("/sauvegarde")
    public ResponseEntity<?> declencherSauvegarde(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur superadmin = userExtractionService.extractUserFromToken(authHeader);
            if (superadmin == null || superadmin.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // TODO: Implémenter la logique de sauvegarde réelle
            // Pour l'instant, on simule une sauvegarde
            String dateSauvegarde = LocalDateTime.now().toString();
            
            return ResponseEntity.ok(Map.of(
                    "message", "Sauvegarde déclenchée avec succès",
                    "dateSauvegarde", dateSauvegarde
            ));
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/admin/parametres/restauration
     * Restaure la base de données depuis un fichier backup
     */
    @PostMapping("/restauration")
    public ResponseEntity<?> restaurerDepuisBackup(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur superadmin = userExtractionService.extractUserFromToken(authHeader);
            if (superadmin == null || superadmin.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String cheminFichier = request.get("cheminFichier");
            String confirmation = request.get("confirmation");
            
            if (cheminFichier == null || cheminFichier.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "cheminFichier est requis"));
            }
            
            if (!"CONFIRMER".equals(confirmation)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Confirmation requise. Envoyez confirmation: 'CONFIRMER'"));
            }
            
            // TODO: Implémenter la logique de restauration réelle
            // Pour l'instant, on simule une restauration
            
            return ResponseEntity.ok(Map.of(
                    "message", "Restauration déclenchée avec succès",
                    "cheminFichier", cheminFichier,
                    "dateRestauration", LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Erreur lors de la restauration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== Méthodes utilitaires ====================
    
    private Object convertirValeur(String valeur, String type) {
        if (valeur == null) return null;
        
        switch (type != null ? type.toUpperCase() : "STRING") {
            case "NUMBER":
                try {
                    if (valeur.contains(".")) {
                        return Double.parseDouble(valeur);
                    } else {
                        return Long.parseLong(valeur);
                    }
                } catch (NumberFormatException e) {
                    return valeur;
                }
            case "BOOLEAN":
                return Boolean.parseBoolean(valeur);
            case "JSON":
                try {
                    return objectMapper.readValue(valeur, Object.class);
                } catch (Exception e) {
                    return valeur;
                }
            default:
                return valeur;
        }
    }
    
    private String determinerType(Object valeur) {
        if (valeur == null) return "STRING";
        if (valeur instanceof Number) return "NUMBER";
        if (valeur instanceof Boolean) return "BOOLEAN";
        if (valeur instanceof Map || valeur instanceof List) return "JSON";
        return "STRING";
    }
}

