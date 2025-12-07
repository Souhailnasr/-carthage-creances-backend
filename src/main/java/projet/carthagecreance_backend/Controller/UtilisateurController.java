package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.PayloadResponse.AuthenticationResponse;
import projet.carthagecreance_backend.PayloadResponse.UserProfileResponse;
import projet.carthagecreance_backend.SecurityServices.UserExtractionService;
import projet.carthagecreance_backend.Service.UtilisateurService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.ExpiredJwtException;

/**
 * Contrôleur REST complet pour la gestion des utilisateurs avec workflow
 * Inclut toutes les opérations CRUD et les fonctionnalités de gestion des agents
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UtilisateurController {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurController.class);

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private UserExtractionService userExtractionService;

    // ==================== ENDPOINTS DE BASE (CRUD) ====================

    /**
     * Retourne le profil de l'utilisateur authentifié en lisant le token JWT.
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                logger.warn("/api/users/me - Token manquant");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Non autorisé",
                    "message", "Token d'authentification manquant",
                    "code", "TOKEN_MISSING"
                ));
            }

            Utilisateur user = userExtractionService.extractUserFromToken(authorizationHeader);
            if (user == null) {
                logger.warn("/api/users/me - Utilisateur non trouvé");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Non autorisé",
                    "message", "Impossible d'extraire l'utilisateur depuis le token",
                    "code", "USER_NOT_FOUND"
                ));
            }

            UserProfileResponse body = UserProfileResponse.builder()
                    .userId(user.getId())
                    .nom(user.getNom())
                    .prenom(user.getPrenom())
                    .email(user.getEmail())
                    .role(user.getRoleUtilisateur() != null ? user.getRoleUtilisateur().name() : null)
                    .build();

            return ResponseEntity.ok(body);
        } catch (ExpiredJwtException e) {
            logger.error("/api/users/me - Token JWT expiré: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Token expiré",
                "message", "Votre session a expiré. Veuillez vous reconnecter.",
                "code", "TOKEN_EXPIRED",
                "expiredAt", e.getClaims().getExpiration().toString(),
                "currentTime", new java.util.Date().toString()
            ));
        } catch (Exception e) {
            logger.error("Erreur dans /api/users/me: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Erreur interne",
                "message", "Une erreur est survenue lors de la récupération de votre profil"
            ));
        }
    }

    /**
     * Crée un nouvel utilisateur avec validation des rôles
     *
     * @param utilisateur L'utilisateur à créer
     * @return ResponseEntity avec l'utilisateur créé (201 CREATED) ou erreur (400 BAD_REQUEST)
     *
     * @example
     * POST /api/users
     * {
     *   "nom": "Dupont",
     *   "prenom": "Jean",
     *   "email": "jean.dupont@example.com",
     *   "motDePasse": "motdepasse",
     *   "roleUtilisateur": "AGENT_DOSSIER"
     * }
     */
    @PostMapping
    public ResponseEntity<AuthenticationResponse> createUtilisateur(
            @RequestBody Utilisateur utilisateur,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            BindingResult result) {
        try {
            System.out.println("===== DÉBUT CONTROLLER createUtilisateur =====");
            System.out.println("Utilisateur reçu: " + utilisateur.getEmail());
            
            if (result.hasErrors()) {
                List<String> errors = result.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.toList());
                return ResponseEntity.badRequest().body(AuthenticationResponse.builder().errors(errors).build());
            }
            
            // ✅ Si un token JWT est fourni, extraire le créateur (pour création par chef/admin)
            // Sinon, créer sans créateur (inscription publique)
            Utilisateur createur = null;
            if (authHeader != null && !authHeader.isBlank()) {
                try {
                    createur = userExtractionService.extractUserFromToken(authHeader);
                    if (createur != null) {
                        System.out.println("Créateur extrait du token: " + createur.getEmail() + " (ID: " + createur.getId() + ")");
                    }
                } catch (Exception e) {
                    System.out.println("Impossible d'extraire le créateur du token (inscription publique): " + e.getMessage());
                    // Continue avec createur = null pour inscription publique
                }
            }
            
            System.out.println("Appel du service...");
            AuthenticationResponse response = utilisateurService.createUtilisateur(utilisateur, createur);
            System.out.println("Service appelé avec succès, token: " + (response.getToken() != null ? "PRÉSENT" : "ABSENT"));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur IllegalArgumentException dans le contrôleur: " + e.getMessage());
            return ResponseEntity.badRequest().body(AuthenticationResponse.builder().errors(List.of(e.getMessage())).build());
        } catch (Exception e) {
            System.out.println("Erreur Exception dans le contrôleur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthenticationResponse.builder().errors(List.of("Erreur interne du serveur: " + e.getMessage())).build());
        }
    }

    /**
     * Récupère un utilisateur par son ID
     * 
     * @param id L'ID de l'utilisateur
     * @return ResponseEntity avec l'utilisateur trouvé (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * GET /api/users/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getUtilisateurById(@PathVariable Long id) {
        Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurById(id);
        return utilisateur.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     * Récupère tous les utilisateurs
     *
     * @return ResponseEntity avec la liste de tous les utilisateurs (200 OK)
     *
     * @example
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<Utilisateur>> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
    }

    /**
     * Met à jour un utilisateur avec validation des rôles
     *
     * @param id L'ID de l'utilisateur à modifier
     * @param utilisateur Les nouvelles données de l'utilisateur
     * @return ResponseEntity avec l'utilisateur mis à jour (200 OK) ou erreur (404 NOT_FOUND)
     *
     * @example
     * PUT /api/users/1
     * {
     *   "nom": "Dupont Modifié",
     *   "prenom": "Jean Modifié",
     *   "roleUtilisateur": "CHEF_DEPARTEMENT_DOSSIER"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUtilisateur(@PathVariable Long id, @RequestBody Utilisateur utilisateur) {
        try {
            Utilisateur updatedUtilisateur = utilisateurService.updateUtilisateur(id, utilisateur);
            return new ResponseEntity<>(updatedUtilisateur, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
        }
    }

    /**
     * Supprime un utilisateur avec vérifications
     *
     * @param id L'ID de l'utilisateur à supprimer
     * @return ResponseEntity vide (204 NO_CONTENT) ou erreur (404 NOT_FOUND)
     *
     * @example
     * DELETE /api/users/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable Long id) {
        try {
            utilisateurService.deleteUtilisateur(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Search Operations
    @GetMapping("/search/name")
    public ResponseEntity<List<Utilisateur>> getUtilisateursByName(@RequestParam String name) {
        List<Utilisateur> utilisateurs = utilisateurService.getUtilisateursByName(name);
        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
    }

    @GetMapping("/search/firstname")
    public ResponseEntity<List<Utilisateur>> getUtilisateursByFirstName(@RequestParam String firstName) {
        List<Utilisateur> utilisateurs = utilisateurService.getUtilisateursByFirstName(firstName);
        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
    }

    @GetMapping("/search/fullname")
    public ResponseEntity<List<Utilisateur>> getUtilisateursByFullName(
            @RequestParam String name,
            @RequestParam String firstName) {
        List<Utilisateur> utilisateurs = utilisateurService.getUtilisateursByFullName(name, firstName);
        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Utilisateur> getUtilisateurByEmailSimple(@PathVariable String email) {
        Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurByEmail(email);
        return utilisateur.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Utilisateur>> searchUtilisateurs(@RequestParam String searchTerm) {
        List<Utilisateur> utilisateurs = utilisateurService.searchUtilisateurs(searchTerm);
        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
    }

    @GetMapping("/with-dossiers")
    public ResponseEntity<List<Utilisateur>> getUtilisateursWithDossiers() {
        List<Utilisateur> utilisateurs = utilisateurService.getUtilisateursWithDossiers();
        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
    }

    @GetMapping("/without-dossiers")
    public ResponseEntity<List<Utilisateur>> getUtilisateursWithoutDossiers() {
        List<Utilisateur> utilisateurs = utilisateurService.getUtilisateursWithoutDossiers();
        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
    }


    // Authentication Operations
    @PostMapping("/authenticate")
    public ResponseEntity<Utilisateur> authenticate(@RequestParam String email, @RequestParam String password) {
        Optional<Utilisateur> utilisateur = utilisateurService.authenticate(email, password);
        return utilisateur.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    // Validation Operations
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = utilisateurService.existsByEmail(email);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    // ==================== NOUVEAUX ENDPOINTS DE GESTION DES AGENTS ====================

    /**
     * Récupère tous les agents
     *
     * @return ResponseEntity avec la liste de tous les agents (200 OK)
     *
     * @example
     * GET /api/users/agents
     */
    @GetMapping("/agents")
    public ResponseEntity<List<Utilisateur>> getAgents() {
        try {
            List<Utilisateur> agents = utilisateurService.getAgents();
            return new ResponseEntity<>(agents, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère tous les chefs
     *
     * @return ResponseEntity avec la liste de tous les chefs (200 OK)
     *
     * @example
     * GET /api/users/chefs
     */
    @GetMapping("/chefs")
    public ResponseEntity<List<Utilisateur>> getChefs() {
        try {
            List<Utilisateur> chefs = utilisateurService.getChefs();
            return new ResponseEntity<>(chefs, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les utilisateurs par rôle
     *
     * @param role Le rôle des utilisateurs
     * @return ResponseEntity avec la liste des utilisateurs avec ce rôle (200 OK)
     *
     * @example
     * GET /api/users/role/AGENT_DOSSIER
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<Utilisateur>> getUsersByRole(@PathVariable String role) {
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getAgentsByRole(role);
            return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les agents actifs
     *
     * @return ResponseEntity avec la liste des agents actifs (200 OK)
     *
     * @example
     * GET /api/users/agents/actifs
     */
    @GetMapping("/agents/actifs")
    public ResponseEntity<List<Utilisateur>> getAgentsActifs() {
        try {
            List<Utilisateur> agents = utilisateurService.getAgentsActifs();
            return new ResponseEntity<>(agents, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met à jour le statut actif d'un utilisateur en fonction de ses dates de connexion/déconnexion
     *
     * @param userId L'ID de l'utilisateur
     * @return ResponseEntity avec l'utilisateur mis à jour (200 OK)
     *
     * @example
     * PUT /api/users/{userId}/statut-actif
     */
    @PutMapping("/{userId}/statut-actif")
    public ResponseEntity<?> mettreAJourStatutActif(@PathVariable Long userId) {
        try {
            Utilisateur user = utilisateurService.mettreAJourStatutActif(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Statut actif mis à jour",
                    "userId", userId,
                    "email", user.getEmail(),
                    "actif", user.getActif(),
                    "derniere_connexion", user.getDerniereConnexion() != null ? user.getDerniereConnexion().toString() : "NULL",
                    "derniere_deconnexion", user.getDerniereDeconnexion() != null ? user.getDerniereDeconnexion().toString() : "NULL"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Utilisateur non trouvé", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la mise à jour", "message", e.getMessage()));
        }
    }

    /**
     * Met à jour le statut actif de tous les utilisateurs
     *
     * @return ResponseEntity avec le nombre d'utilisateurs mis à jour (200 OK)
     *
     * @example
     * PUT /api/users/statut-actif/tous
     */
    @PutMapping("/statut-actif/tous")
    public ResponseEntity<?> mettreAJourStatutActifTous() {
        try {
            int count = utilisateurService.mettreAJourStatutActifTous();
            return ResponseEntity.ok(Map.of(
                    "message", "Statut actif mis à jour pour tous les utilisateurs",
                    "nombreUtilisateursMisAJour", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la mise à jour", "message", e.getMessage()));
        }
    }

    /**
     * Récupère les performances des agents
     *
     * @return ResponseEntity avec la liste des performances des agents (200 OK)
     *
     * @example
     * GET /api/users/performance
     */
    @GetMapping("/performance")
    public ResponseEntity<List<PerformanceAgent>> getPerformanceAgents() {
        try {
            List<PerformanceAgent> performances = utilisateurService.getPerformanceAgents();
            return new ResponseEntity<>(performances, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ENDPOINTS DE STATISTIQUES ====================

    /**
     * Compte le nombre d'agents
     *
     * @return ResponseEntity avec le nombre d'agents (200 OK)
     *
     * @example
     * GET /api/users/statistiques/agents
     */
    @GetMapping("/statistiques/agents")
    public ResponseEntity<Long> countAgents() {
        try {
            long count = utilisateurService.countAgents();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Compte le nombre de chefs
     *
     * @return ResponseEntity avec le nombre de chefs (200 OK)
     *
     * @example
     * GET /api/users/statistiques/chefs
     */
    @GetMapping("/statistiques/chefs")
    public ResponseEntity<Long> countChefs() {
        try {
            long count = utilisateurService.countChefs();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les agents triés par performance
     *
     * @return ResponseEntity avec la liste des agents triés par performance (200 OK)
     *
     * @example
     * GET /api/users/statistiques/performance
     */
    @GetMapping("/statistiques/performance")
    public ResponseEntity<List<Utilisateur>> getAgentsByPerformance() {
        try {
            List<Utilisateur> agents = utilisateurService.getAgentsByPerformance();
            return new ResponseEntity<>(agents, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ENDPOINTS DE VALIDATION DES RÔLES ====================

    /**
     * Récupère un utilisateur complet par son email
     *
     * @param email L'email de l'utilisateur
     * @return ResponseEntity avec l'utilisateur complet (200 OK) ou 404 si non trouvé
     *
     * @example
     * GET /api/users/by-email/{email}
     */
    @GetMapping("/by-email/{email}")
    public ResponseEntity<Utilisateur> getUtilisateurByEmail(@PathVariable String email) {
        try {
            logger.info("=== DÉBUT getUtilisateurByEmail ===");
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
                
                logger.info("=== FIN getUtilisateurByEmail - Utilisateur trouvé ===");
                return ResponseEntity.ok(user);
            } else {
                logger.warn("Utilisateur non trouvé pour l'email: {}", email);
                logger.info("=== FIN getUtilisateurByEmail - Utilisateur non trouvé ===");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans getUtilisateurByEmail: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère l'ID d'un utilisateur par son email
     *
     * @param email L'email de l'utilisateur
     * @return ResponseEntity avec l'ID de l'utilisateur (200 OK) ou 404 si non trouvé
     *
     * @example
     * GET /api/users/by-email/{email}/id
     */
    @GetMapping("/by-email/{email}/id")
    public ResponseEntity<?> getUtilisateurIdByEmail(@PathVariable String email) {
        try {
            logger.info("=== DÉBUT getUtilisateurIdByEmail ===");
            logger.info("Email reçu: {}", email);
            
            // Validation de l'email
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Email vide ou null reçu");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'email ne peut pas être vide"));
            }
            
            // Recherche de l'utilisateur
            Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurByEmail(email);
            
            if (utilisateur.isPresent()) {
                Utilisateur user = utilisateur.get();
                logger.info("Utilisateur trouvé: ID={}, Email={}", user.getId(), user.getEmail());
                
                Map<String, Object> response = Map.of(
                    "id", user.getId(),
                    "email", user.getEmail()
                );
                
                logger.info("=== FIN getUtilisateurIdByEmail - ID retourné: {} ===", user.getId());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Utilisateur non trouvé pour l'email: {}", email);
                logger.info("=== FIN getUtilisateurIdByEmail - Utilisateur non trouvé ===");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans getUtilisateurIdByEmail: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne du serveur: " + e.getMessage()));
        }
    }

    /**
     * Vérifie si un utilisateur peut créer un autre utilisateur avec un rôle donné
     *
     * @param createurId L'ID de l'utilisateur créateur
     * @param roleCree Le rôle de l'utilisateur à créer
     * @return ResponseEntity avec true si l'utilisateur peut créer, false sinon (200 OK)
     *
     * @example
     * GET /api/users/validation/peut-creer?createurId=1&roleCree=AGENT_DOSSIER
     */
    @GetMapping("/validation/peut-creer")
    public ResponseEntity<Boolean> peutCreerUtilisateur(
            @RequestParam Long createurId,
            @RequestParam RoleUtilisateur roleCree) {
        try {
            boolean peutCreer = utilisateurService.peutCreerUtilisateur(createurId, roleCree);
            return new ResponseEntity<>(peutCreer, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les utilisateurs par rôle (version enum)
     *
     * @param roleUtilisateur Le rôle des utilisateurs
     * @return ResponseEntity avec la liste des utilisateurs avec ce rôle (200 OK)
     *
     * @example
     * GET /api/users/role-enum/AGENT_DOSSIER
     */
    @GetMapping("/role-enum/{roleUtilisateur}")
    public ResponseEntity<List<Utilisateur>> getUtilisateursByRoleUtilisateur(@PathVariable RoleUtilisateur roleUtilisateur) {
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getUtilisateursByRoleUtilisateur(roleUtilisateur);
            return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtenir les agents d'un chef
     * GET /api/users/chef/{chefId}
     *
     * @param chefId L'ID du chef
     * @return ResponseEntity avec la liste des agents du chef (200 OK) ou erreur (500 INTERNAL_SERVER_ERROR)
     *
     * @example
     * GET /api/users/chef/46
     */
    @GetMapping("/chef/{chefId}")
    public ResponseEntity<List<Utilisateur>> getAgentsByChef(@PathVariable Long chefId) {
        try {
            logger.info("Récupération des agents pour le chef ID: {}", chefId);
            List<Utilisateur> agents = utilisateurService.getAgentsByChef(chefId);
            logger.info("Nombre d'agents trouvés: {}", agents.size());
            return ResponseEntity.ok(agents);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la récupération des agents du chef " + chefId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de la récupération des agents du chef " + chefId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}