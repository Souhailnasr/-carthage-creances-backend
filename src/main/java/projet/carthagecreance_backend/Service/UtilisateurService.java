package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Entity.RoleUtilisateur;
import projet.carthagecreance_backend.Entity.PerformanceAgent;
import projet.carthagecreance_backend.PayloadResponse.AuthenticationResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface pour la gestion des utilisateurs avec workflow complet
 * Inclut toutes les opérations CRUD et les fonctionnalités de gestion des agents
 */
public interface UtilisateurService {

    // ==================== CRUD Operations ====================
    
    /**
     * Crée un nouvel utilisateur avec validation des rôles
     * @param utilisateur L'utilisateur à créer
     * @return L'utilisateur créé
     */
    AuthenticationResponse createUtilisateur(Utilisateur utilisateur);
    
    /**
     * Récupère un utilisateur par son ID
     * @param id L'ID de l'utilisateur
     * @return L'utilisateur trouvé
     */
    Optional<Utilisateur> getUtilisateurById(Long id);
    
    /**
     * Récupère tous les utilisateurs
     * @return Liste de tous les utilisateurs
     */
    List<Utilisateur> getAllUtilisateurs();
    
    /**
     * Met à jour un utilisateur avec validation des rôles
     * @param id L'ID de l'utilisateur à modifier
     * @param utilisateur Les nouvelles données de l'utilisateur
     * @return L'utilisateur mis à jour
     */
    Utilisateur updateUtilisateur(Long id, Utilisateur utilisateur);
    
    /**
     * Supprime un utilisateur avec vérifications
     * @param id L'ID de l'utilisateur à supprimer
     */
    void deleteUtilisateur(Long id);

    // ==================== Search Operations ====================
    
    List<Utilisateur> getUtilisateursByName(String name);
    List<Utilisateur> getUtilisateursByFirstName(String firstName);
    List<Utilisateur> getUtilisateursByFullName(String name, String firstName);
    Optional<Utilisateur> getUtilisateurByEmail(String email);
    List<Utilisateur> searchUtilisateurs(String searchTerm);
    List<Utilisateur> getUtilisateursWithDossiers();
    List<Utilisateur> getUtilisateursWithoutDossiers();
    
    // ==================== Authentication Operations ====================
    
    Optional<Utilisateur> authenticate(String email, String password);

    // ==================== Validation Operations ====================
    
    boolean existsByEmail(String email);

    // ==================== Nouvelles méthodes de gestion des agents ====================

    /**
     * Récupère tous les agents
     * @return Liste de tous les agents
     */
    List<Utilisateur> getAgents();

    /**
     * Récupère les utilisateurs par rôle
     * @param role Le rôle des utilisateurs
     * @return Liste des utilisateurs avec ce rôle
     */
    List<Utilisateur> getAgentsByRole(String role);

    /**
     * Récupère tous les chefs
     * @return Liste de tous les chefs
     */
    List<Utilisateur> getChefs();

    /**
     * Récupère les agents actifs
     * @return Liste des agents actifs
     */
    List<Utilisateur> getAgentsActifs();

    /**
     * Récupère les performances des agents
     * @return Liste des performances des agents
     */
    List<PerformanceAgent> getPerformanceAgents();

    /**
     * Compte le nombre d'agents
     * @return Nombre d'agents
     */
    long countAgents();

    /**
     * Compte le nombre de chefs
     * @return Nombre de chefs
     */
    long countChefs();

    /**
     * Récupère les agents triés par performance
     * @return Liste des agents triés par performance
     */
    List<Utilisateur> getAgentsByPerformance();

    // ==================== Méthodes de validation des rôles ====================

    /**
     * Vérifie si un utilisateur peut créer un autre utilisateur avec un rôle donné
     * @param createurId L'ID de l'utilisateur créateur
     * @param roleCree Le rôle de l'utilisateur à créer
     * @return true si l'utilisateur peut créer, false sinon
     */
    boolean peutCreerUtilisateur(Long createurId, RoleUtilisateur roleCree);

    /**
     * Récupère les utilisateurs par rôle (version enum)
     * @param roleUtilisateur Le rôle des utilisateurs
     * @return Liste des utilisateurs avec ce rôle
     */
    List<Utilisateur> getUtilisateursByRoleUtilisateur(RoleUtilisateur roleUtilisateur);
}