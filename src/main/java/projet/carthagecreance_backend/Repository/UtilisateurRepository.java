// Fichier : src/main/java/projet/carthagecreance_backend/Repository/UtilisateurRepository.java
package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Entity.RoleUtilisateur; // Ajout de l'import

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // Rechercher par nom
    List<Utilisateur> findByNomContainingIgnoreCase(String nom);

    // Rechercher par prénom
    List<Utilisateur> findByPrenomContainingIgnoreCase(String prenom);

    // Rechercher par nom et prénom
    List<Utilisateur> findByNomAndPrenom(String nom, String prenom);

    // Rechercher par email
    Optional<Utilisateur> findByEmail(String email);

    // Rechercher par nom ou prénom (recherche globale)
    @Query("SELECT u FROM Utilisateur u WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :recherche, '%')) OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :recherche, '%'))")
    List<Utilisateur> findByNomOuPrenomContaining(@Param("recherche") String recherche);

    // Authentification par email et mot de passe
    @Query("SELECT u FROM Utilisateur u WHERE u.email = :email AND u.motDePasse = :motDePasse")
    Optional<Utilisateur> findByEmailAndMotDePasse(@Param("email") String email, @Param("motDePasse") String motDePasse);

    // Vérifier l'existence par email
    boolean existsByEmail(String email);

    // >>>>>>>>> CORRECTION : Rechercher par rôle (utilisation de l'enum) <<<<<<<<<
    List<Utilisateur> findByRoleUtilisateur(RoleUtilisateur roleUtilisateur);
    
    // Rechercher par plusieurs rôles
    List<Utilisateur> findByRoleUtilisateurIn(List<RoleUtilisateur> roles);

    // Rechercher les agents créés par un chef spécifique
    List<Utilisateur> findByChefCreateurId(Long chefId);

    // Rechercher les utilisateurs avec des dossiers
    @Query("SELECT u FROM Utilisateur u WHERE SIZE(u.dossiers) > 0")
    List<Utilisateur> findUtilisateursAvecDossiers();

    // Rechercher les utilisateurs sans dossiers
    @Query("SELECT u FROM Utilisateur u WHERE SIZE(u.dossiers) = 0")
    List<Utilisateur> findUtilisateursSansDossiers();

    // Compter les dossiers par utilisateur
    @Query("SELECT u.nom, u.prenom, COUNT(d) FROM Utilisateur u LEFT JOIN u.dossiers d GROUP BY u.id, u.nom, u.prenom ORDER BY COUNT(d) DESC")
    List<Object[]> compterDossiersParUtilisateur();

    // Rechercher les utilisateurs avec le plus de dossiers
    @Query("SELECT u FROM Utilisateur u LEFT JOIN u.dossiers d GROUP BY u.id ORDER BY COUNT(d) DESC")
    List<Utilisateur> findUtilisateursAvecPlusDeDossiers();

    // Rechercher par email (insensible à la casse)
    @Query("SELECT u FROM Utilisateur u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<Utilisateur> findByEmailIgnoreCase(@Param("email") String email);

    // Vérifier l'existence par email (insensible à la casse)
    @Query("SELECT COUNT(u) > 0 FROM Utilisateur u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);
}