package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Service.UtilisateurService;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST complet pour la gestion des utilisateurs avec workflow
 * Inclut toutes les opérations CRUD et les fonctionnalités de gestion des agents
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    // ==================== ENDPOINTS DE BASE (CRUD) ====================

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
    public ResponseEntity<?> createUtilisateur(@RequestBody Utilisateur utilisateur) {
        try {
            Utilisateur createdUtilisateur = utilisateurService.createUtilisateur(utilisateur);
            return new ResponseEntity<>(createdUtilisateur, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création de l'utilisateur: " + e.getMessage());
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
    public ResponseEntity<Utilisateur> getUtilisateurByEmail(@PathVariable String email) {
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
}