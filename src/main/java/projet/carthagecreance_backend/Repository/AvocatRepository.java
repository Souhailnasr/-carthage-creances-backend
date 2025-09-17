package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Avocat;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvocatRepository extends JpaRepository<Avocat, Long> {
    
    // Rechercher par nom
    List<Avocat> findByNomContainingIgnoreCase(String nom);
    
    // Rechercher par prénom
    List<Avocat> findByPrenomContainingIgnoreCase(String prenom);
    
    // Rechercher par nom et prénom
    List<Avocat> findByNomAndPrenom(String nom, String prenom);
    
    // Rechercher par email
    Optional<Avocat> findByEmail(String email);
    
    // Rechercher par téléphone
    Optional<Avocat> findByTelephone(String telephone);
    
    // Rechercher par spécialité
    List<Avocat> findBySpecialiteContainingIgnoreCase(String specialite);
    
    // Rechercher par nom ou prénom (recherche globale)
    @Query("SELECT a FROM Avocat a WHERE LOWER(a.nom) LIKE LOWER(CONCAT('%', :recherche, '%')) OR LOWER(a.prenom) LIKE LOWER(CONCAT('%', :recherche, '%'))")
    List<Avocat> findByNomOuPrenomContaining(@Param("recherche") String recherche);
    
    // Vérifier l'existence par email
    boolean existsByEmail(String email);
    
    // Vérifier l'existence par téléphone
    boolean existsByTelephone(String telephone);
    
    // Compter les avocats par spécialité
    @Query("SELECT a.specialite, COUNT(a) FROM Avocat a WHERE a.specialite IS NOT NULL GROUP BY a.specialite")
    List<Object[]> compterAvocatsParSpecialite();
    
    // Rechercher les avocats avec le plus de dossiers
    @Query("SELECT a FROM Avocat a LEFT JOIN a.dossiers d GROUP BY a.id ORDER BY COUNT(d) DESC")
    List<Avocat> findAvocatsAvecPlusDeDossiers();
}
