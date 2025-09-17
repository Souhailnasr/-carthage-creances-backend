// Fichier : src/main/java/projet/carthagecreance_backend/Controller/UtilisateurController.java
package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.RoleUtilisateur; // Ajout de l'import
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Service.UtilisateurService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "*")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<?> createUtilisateur(@RequestBody Utilisateur utilisateur) { // Changé pour ResponseEntity<?>
        try {
            Utilisateur createdUtilisateur = utilisateurService.createUtilisateur(utilisateur);
            return new ResponseEntity<>(createdUtilisateur, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Ou un message plus générique
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getUtilisateurById(@PathVariable Long id) {
        Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurById(id);
        return utilisateur.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Utilisateur>> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUtilisateur(@PathVariable Long id, @RequestBody Utilisateur utilisateur) { // Changé pour ResponseEntity<?>
        try {
            Utilisateur updatedUtilisateur = utilisateurService.updateUtilisateur(id, utilisateur);
            return new ResponseEntity<>(updatedUtilisateur, HttpStatus.OK);
        } catch (IllegalArgumentException e) { // Attraper IllegalArgumentException
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable Long id) {
        try {
            utilisateurService.deleteUtilisateur(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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

    // >>>>>>>>> NOUVELLE MÉTHODE : Recherche par rôle <<<<<<<<<
    @GetMapping("/role/{roleUtilisateur}")
    public ResponseEntity<List<Utilisateur>> getUtilisateursByRoleUtilisateur(@PathVariable RoleUtilisateur roleUtilisateur) {
        try {
            // Le framework Spring convertira automatiquement la chaîne de l'URL en enum RoleUtilisateur
            // Ex: /api/utilisateurs/role/ADMIN -> roleUtilisateur = RoleUtilisateur.ADMIN
            List<Utilisateur> utilisateurs = utilisateurService.getUtilisateursByRoleUtilisateur(roleUtilisateur);
            return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
        } catch (Exception e) {
            // Gestion d'erreur générique, pourrait être plus spécifique
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
}