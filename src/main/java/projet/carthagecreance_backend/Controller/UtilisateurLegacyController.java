package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Service.UtilisateurService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Contrôleur legacy pour la compatibilité avec l'ancienne URL /api/utilisateurs
 * Fournit les endpoints manquants pour le frontend
 */
@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "http://localhost:4200")
public class UtilisateurLegacyController {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurLegacyController.class);

    @Autowired
    private UtilisateurService utilisateurService;

    /**
     * Récupère un utilisateur complet par son email
     * Endpoint legacy pour compatibilité frontend
     *
     * @param email L'email de l'utilisateur
     * @return ResponseEntity avec l'utilisateur complet (200 OK) ou 404 si non trouvé
     *
     * @example
     * GET /api/utilisateurs/by-email/{email}
     */
    @GetMapping("/by-email/{email}")
    public ResponseEntity<Utilisateur> getUtilisateurByEmail(@PathVariable String email) {
        try {
            logger.info("=== DÉBUT getUtilisateurByEmail (Legacy) ===");
            logger.info("Email reçu: {}", email);
            
            // Validation de l'email
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Email vide ou null reçu");
                return ResponseEntity.badRequest().build();
            }
            
            // Recherche de l'utilisateur
            Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurByEmail(email);
            
            if (utilisateur.isPresent()) {
                Utilisateur user = utilisateur.get();
                logger.info("Utilisateur trouvé: ID={}, Nom={}, Email={}, Role={}", 
                           user.getId(), user.getNom(), user.getEmail(), user.getRoleUtilisateur());
                
                logger.info("=== FIN getUtilisateurByEmail (Legacy) - Utilisateur trouvé ===");
                return ResponseEntity.ok(user);
            } else {
                logger.warn("Utilisateur non trouvé pour l'email: {}", email);
                logger.info("=== FIN getUtilisateurByEmail (Legacy) - Utilisateur non trouvé ===");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans getUtilisateurByEmail (Legacy): {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère l'ID d'un utilisateur par son email
     * Endpoint legacy pour compatibilité frontend
     *
     * @param email L'email de l'utilisateur
     * @return ResponseEntity avec l'ID de l'utilisateur (200 OK) ou 404 si non trouvé
     *
     * @example
     * GET /api/utilisateurs/by-email/{email}/id
     */
    @GetMapping("/by-email/{email}/id")
    public ResponseEntity<?> getUtilisateurIdByEmail(@PathVariable String email) {
        try {
            logger.info("=== DÉBUT getUtilisateurIdByEmail (Legacy) ===");
            logger.info("Email reçu: {}", email);
            
            // Validation de l'email
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Email vide ou null reçu");
                return ResponseEntity.badRequest()
                        .body("L'email ne peut pas être vide");
            }
            
            // Recherche de l'utilisateur
            Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurByEmail(email);
            
            if (utilisateur.isPresent()) {
                Utilisateur user = utilisateur.get();
                logger.info("Utilisateur trouvé: ID={}, Email={}", user.getId(), user.getEmail());
                
                // Retourner seulement l'ID et l'email
                return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
                    put("id", user.getId());
                    put("email", user.getEmail());
                }});
                
            } else {
                logger.warn("Utilisateur non trouvé pour l'email: {}", email);
                logger.info("=== FIN getUtilisateurIdByEmail (Legacy) - Utilisateur non trouvé ===");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans getUtilisateurIdByEmail (Legacy): {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur interne du serveur: " + e.getMessage());
        }
    }
}
