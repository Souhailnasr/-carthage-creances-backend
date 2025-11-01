package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * Récupère un utilisateur par son email et retourne un JSON avec l'ID, nom, prénom, email et rôle
     * Endpoint legacy pour compatibilité frontend
     *
     * @param email L'email de l'utilisateur
     * @return ResponseEntity avec un JSON contenant l'ID, nom, prénom, email et rôle (200 OK) ou 404 si non trouvé
     *
     * @example
     * GET /api/utilisateurs/by-email/{email}
     * Response: {"id": 1, "nom": "Dupont", "prenom": "Jean", "email": "jean.dupont@example.com", "role": "AGENT_DOSSIER"}
     */
    @GetMapping("/by-email/{email}")
    @PreAuthorize("hasAnyRole('AGENT_DOSSIER', 'CHEF_DEPARTEMENT_DOSSIER', 'SUPER_ADMIN')")
    public ResponseEntity<?> getUtilisateurByEmail(@PathVariable String email) {
        try {
            logger.info("=== DÉBUT getUtilisateurByEmail (Legacy) ===");
            logger.info("Email reçu: {}", email);
            
            // Validation de l'email
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Email vide ou null reçu");
                return ResponseEntity.badRequest().body("L'email ne peut pas être vide");
            }
            
            // Recherche de l'utilisateur
            Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurByEmail(email);
            
            if (utilisateur.isPresent()) {
                Utilisateur user = utilisateur.get();
                logger.info("Utilisateur trouvé: ID={}, Nom={}, Email={}, Role={}", 
                           user.getId(), user.getNom(), user.getEmail(), user.getRoleUtilisateur());
                
                // Retourner un JSON avec les champs spécifiés
                java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("nom", user.getNom());
                userInfo.put("prenom", user.getPrenom());
                userInfo.put("email", user.getEmail());
                userInfo.put("role", user.getRoleUtilisateur() != null ? user.getRoleUtilisateur().name() : null);
                
                logger.info("=== FIN getUtilisateurByEmail (Legacy) - Utilisateur trouvé ===");
                return ResponseEntity.ok(userInfo);
            } else {
                logger.warn("Utilisateur non trouvé pour l'email: {}", email);
                logger.info("=== FIN getUtilisateurByEmail (Legacy) - Utilisateur non trouvé ===");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans getUtilisateurByEmail (Legacy): {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur interne du serveur: " + e.getMessage());
        }
    }

    /**
     * Récupère un utilisateur par son ID et retourne un JSON avec l'ID, nom, prénom, email et rôle
     * Endpoint legacy pour compatibilité frontend
     *
     * @param id L'ID de l'utilisateur
     * @return ResponseEntity avec un JSON contenant l'ID, nom, prénom, email et rôle (200 OK) ou 404 si non trouvé
     *
     * @example
     * GET /api/utilisateurs/{id}
     * Response: {"id": 1, "nom": "Dupont", "prenom": "Jean", "email": "jean.dupont@example.com", "role": "AGENT_DOSSIER"}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT_DOSSIER', 'CHEF_DEPARTEMENT_DOSSIER', 'SUPER_ADMIN')")
    public ResponseEntity<?> getUtilisateurById(@PathVariable Long id) {
        try {
            logger.info("=== DÉBUT getUtilisateurById (Legacy) ===");
            logger.info("ID reçu: {}", id);
            
            // Validation de l'ID
            if (id == null || id <= 0) {
                logger.warn("ID invalide reçu: {}", id);
                return ResponseEntity.badRequest().body("L'ID doit être un nombre positif");
            }
            
            // Recherche de l'utilisateur
            Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurById(id);
            
            if (utilisateur.isPresent()) {
                Utilisateur user = utilisateur.get();
                logger.info("Utilisateur trouvé: ID={}, Nom={}, Email={}, Role={}", 
                           user.getId(), user.getNom(), user.getEmail(), user.getRoleUtilisateur());
                
                // Retourner un JSON avec les champs spécifiés
                java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("nom", user.getNom());
                userInfo.put("prenom", user.getPrenom());
                userInfo.put("email", user.getEmail());
                userInfo.put("role", user.getRoleUtilisateur() != null ? user.getRoleUtilisateur().name() : null);
                
                logger.info("=== FIN getUtilisateurById (Legacy) - Utilisateur trouvé ===");
                return ResponseEntity.ok(userInfo);
            } else {
                logger.warn("Utilisateur non trouvé pour l'ID: {}", id);
                logger.info("=== FIN getUtilisateurById (Legacy) - Utilisateur non trouvé ===");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans getUtilisateurById (Legacy): {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur interne du serveur: " + e.getMessage());
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
    @PreAuthorize("hasAnyRole('AGENT_DOSSIER', 'CHEF_DEPARTEMENT_DOSSIER', 'SUPER_ADMIN')")
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




