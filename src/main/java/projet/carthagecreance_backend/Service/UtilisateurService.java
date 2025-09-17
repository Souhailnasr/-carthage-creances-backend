package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Entity.RoleUtilisateur; // Ajout de l'import

import java.util.List;
import java.util.Optional;

public interface UtilisateurService {

    // CRUD Operations
    Utilisateur createUtilisateur(Utilisateur utilisateur);
    Optional<Utilisateur> getUtilisateurById(Long id);
    List<Utilisateur> getAllUtilisateurs();
    Utilisateur updateUtilisateur(Long id, Utilisateur utilisateur);
    // >>>>>>>>> CORRECTION : Signature de la méthode <<<<<<<<<
    List<Utilisateur> getUtilisateursByRoleUtilisateur(RoleUtilisateur roleUtilisateur); // Changement ici
    void deleteUtilisateur(Long id);

    // Search Operations
    List<Utilisateur> getUtilisateursByName(String name);
    List<Utilisateur> getUtilisateursByFirstName(String firstName);
    List<Utilisateur> getUtilisateursByFullName(String name, String firstName);
    Optional<Utilisateur> getUtilisateurByEmail(String email);
    List<Utilisateur> searchUtilisateurs(String searchTerm);
    List<Utilisateur> getUtilisateursWithDossiers();
    List<Utilisateur> getUtilisateursWithoutDossiers();
    // Authentication Operations
    Optional<Utilisateur> authenticate(String email, String password);

    // Validation Operations
    boolean existsByEmail(String email);
}