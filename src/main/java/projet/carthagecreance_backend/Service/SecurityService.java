package projet.carthagecreance_backend.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.RoleUtilisateur;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de sécurité pour la gestion des autorisations
 * 
 * Ce service fournit des méthodes utilitaires pour vérifier
 * les autorisations et les rôles des utilisateurs authentifiés.
 * 
 * @author Système de Gestion de Créances
 * @version 1.0
 */
@Service
public class SecurityService {

    /**
     * Récupère l'utilisateur actuellement authentifié
     * 
     * @return L'utilisateur authentifié ou null
     */
    public UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !(authentication.getPrincipal() instanceof String)) {
            return (UserDetails) authentication.getPrincipal();
        }
        
        return null;
    }

    /**
     * Récupère le nom d'utilisateur actuellement authentifié
     * 
     * @return Le nom d'utilisateur ou null
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        
        return null;
    }

    /**
     * Récupère les rôles de l'utilisateur actuellement authentifié
     * 
     * @return Liste des rôles ou liste vide
     */
    public List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toList());
        }
        
        return List.of();
    }

    /**
     * Vérifie si l'utilisateur actuel a un rôle spécifique
     * 
     * @param role Le rôle à vérifier
     * @return true si l'utilisateur a le rôle, false sinon
     */
    public boolean hasRole(String role) {
        List<String> userRoles = getCurrentUserRoles();
        return userRoles.contains(role) || userRoles.contains("ROLE_" + role);
    }

    /**
     * Vérifie si l'utilisateur actuel a un des rôles spécifiés
     * 
     * @param roles Les rôles à vérifier
     * @return true si l'utilisateur a au moins un des rôles, false sinon
     */
    public boolean hasAnyRole(String... roles) {
        List<String> userRoles = getCurrentUserRoles();
        
        for (String role : roles) {
            if (userRoles.contains(role) || userRoles.contains("ROLE_" + role)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Vérifie si l'utilisateur actuel a tous les rôles spécifiés
     * 
     * @param roles Les rôles à vérifier
     * @return true si l'utilisateur a tous les rôles, false sinon
     */
    public boolean hasAllRoles(String... roles) {
        List<String> userRoles = getCurrentUserRoles();
        
        for (String role : roles) {
            if (!userRoles.contains(role) && !userRoles.contains("ROLE_" + role)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Vérifie si l'utilisateur actuel est un super administrateur
     * 
     * @return true si l'utilisateur est super admin, false sinon
     */
    public boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }

    /**
     * Vérifie si l'utilisateur actuel est un chef
     * 
     * @return true si l'utilisateur est un chef, false sinon
     */
    public boolean isChef() {
        return hasAnyRole("CHEF_DEPARTEMENT_DOSSIER", "CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE", 
                         "CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE", "CHEF_DEPARTEMENT_FINANCE");
    }

    /**
     * Vérifie si l'utilisateur actuel est un agent
     * 
     * @return true si l'utilisateur est un agent, false sinon
     */
    public boolean isAgent() {
        return hasAnyRole("AGENT_DOSSIER", "AGENT_RECOUVREMENT_AMIABLE", 
                         "AGENT_RECOUVREMENT_JURIDIQUE", "AGENT_FINANCE");
    }

    /**
     * Vérifie si l'utilisateur actuel peut gérer les utilisateurs
     * 
     * @return true si l'utilisateur peut gérer les utilisateurs, false sinon
     */
    public boolean canManageUsers() {
        return isSuperAdmin() || isChef();
    }

    /**
     * Vérifie si l'utilisateur actuel peut valider des dossiers
     * 
     * @return true si l'utilisateur peut valider des dossiers, false sinon
     */
    public boolean canValidateDossiers() {
        return isSuperAdmin() || hasRole("CHEF_DEPARTEMENT_DOSSIER");
    }

    /**
     * Vérifie si l'utilisateur actuel peut gérer les performances
     * 
     * @return true si l'utilisateur peut gérer les performances, false sinon
     */
    public boolean canManagePerformance() {
        return isSuperAdmin() || hasRole("CHEF_DEPARTEMENT_DOSSIER");
    }

    /**
     * Vérifie si l'utilisateur actuel peut accéder aux statistiques
     * 
     * @return true si l'utilisateur peut accéder aux statistiques, false sinon
     */
    public boolean canAccessStatistics() {
        return isSuperAdmin() || isChef();
    }

    /**
     * Vérifie si l'utilisateur actuel est authentifié
     * 
     * @return true si l'utilisateur est authentifié, false sinon
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !(authentication.getPrincipal() instanceof String);
    }

    /**
     * Vérifie si l'utilisateur actuel peut accéder à une ressource spécifique
     * 
     * @param resourceType Le type de ressource
     * @param action L'action à effectuer
     * @return true si l'utilisateur peut accéder à la ressource, false sinon
     */
    public boolean canAccessResource(String resourceType, String action) {
        if (!isAuthenticated()) {
            return false;
        }

        switch (resourceType.toLowerCase()) {
            case "users":
                return canManageUsers();
            case "dossiers":
                return isSuperAdmin() || hasRole("CHEF_DEPARTEMENT_DOSSIER") || hasRole("AGENT_DOSSIER");
            case "enquetes":
                return isSuperAdmin() || hasRole("CHEF_DEPARTEMENT_DOSSIER") || hasRole("AGENT_DOSSIER");
            case "validations":
                return canValidateDossiers();
            case "performance":
                return canManagePerformance();
            case "statistics":
                return canAccessStatistics();
            default:
                return false;
        }
    }

    /**
     * Récupère le niveau d'autorisation de l'utilisateur actuel
     * 
     * @return Le niveau d'autorisation (0 = non authentifié, 1 = agent, 2 = chef, 3 = super admin)
     */
    public int getAuthorizationLevel() {
        if (!isAuthenticated()) {
            return 0;
        }

        if (isSuperAdmin()) {
            return 3;
        } else if (isChef()) {
            return 2;
        } else if (isAgent()) {
            return 1;
        }

        return 0;
    }

    /**
     * Vérifie si l'utilisateur actuel a un niveau d'autorisation suffisant
     * 
     * @param requiredLevel Le niveau d'autorisation requis
     * @return true si l'utilisateur a le niveau requis, false sinon
     */
    public boolean hasRequiredAuthorizationLevel(int requiredLevel) {
        return getAuthorizationLevel() >= requiredLevel;
    }
}
