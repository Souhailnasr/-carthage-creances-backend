package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Huissier;

import java.util.List;
import java.util.Optional;

@Repository
public interface HuissierRepository extends JpaRepository<Huissier, Long> {
    
    // Rechercher par nom
    List<Huissier> findByNomContainingIgnoreCase(String nom);
    
    // Rechercher par prénom
    List<Huissier> findByPrenomContainingIgnoreCase(String prenom);
    
    // Rechercher par nom et prénom
    List<Huissier> findByNomAndPrenom(String nom, String prenom);
    
    // Rechercher par email
    Optional<Huissier> findByEmail(String email);
    
    // Rechercher par téléphone
    Optional<Huissier> findByTelephone(String telephone);
    
    // Rechercher par spécialité
    List<Huissier> findBySpecialiteContainingIgnoreCase(String specialite);
    
    // Rechercher par nom ou prénom (recherche globale)
    @Query("SELECT h FROM Huissier h WHERE LOWER(h.nom) LIKE LOWER(CONCAT('%', :recherche, '%')) OR LOWER(h.prenom) LIKE LOWER(CONCAT('%', :recherche, '%'))")
    List<Huissier> findByNomOuPrenomContaining(@Param("recherche") String recherche);
    
    // Vérifier l'existence par email
    boolean existsByEmail(String email);
    
    // Vérifier l'existence par téléphone
    boolean existsByTelephone(String telephone);
    
    // Compter les huissiers par spécialité
    @Query("SELECT h.specialite, COUNT(h) FROM Huissier h WHERE h.specialite IS NOT NULL GROUP BY h.specialite")
    List<Object[]> compterHuissiersParSpecialite();
    
    // Rechercher les huissiers avec le plus de dossiers
    @Query("SELECT h FROM Huissier h LEFT JOIN h.dossiers d GROUP BY h.id ORDER BY COUNT(d) DESC")
    List<Huissier> findHuissiersAvecPlusDeDossiers();
    
    // Rechercher les huissiers sans dossiers
    @Query("SELECT h FROM Huissier h WHERE SIZE(h.dossiers) = 0")
    List<Huissier> findHuissiersSansDossiers();
}
