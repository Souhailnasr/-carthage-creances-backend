package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Creancier;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreancierRepository extends JpaRepository<Creancier, Long> {
    
    // Rechercher par code créancier
    Optional<Creancier> findByCodeCreancier(String codeCreancier);
    
    // Rechercher par code créance
    Optional<Creancier> findByCodeCreance(String codeCreance);
    
    // Rechercher par nom
    List<Creancier> findByNomContainingIgnoreCase(String nom);
    
    // Rechercher par prénom
    List<Creancier> findByPrenomContainingIgnoreCase(String prenom);
    
    // Rechercher par nom et prénom
    List<Creancier> findByNomAndPrenom(String nom, String prenom);
    
    // Rechercher par email
    Optional<Creancier> findByEmail(String email);
    
    // Rechercher par téléphone
    Optional<Creancier> findByTelephone(String telephone);
    
    // Rechercher par ville
    List<Creancier> findByVilleContainingIgnoreCase(String ville);
    
    // Rechercher par code postal
    List<Creancier> findByCodePostal(String codePostal);
    
    // Rechercher par nom ou prénom (recherche globale)
    @Query("SELECT c FROM Creancier c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :recherche, '%')) OR LOWER(c.prenom) LIKE LOWER(CONCAT('%', :recherche, '%'))")
    List<Creancier> findByNomOuPrenomContaining(@Param("recherche") String recherche);
    
    // Vérifier l'existence par email
    boolean existsByEmail(String email);
    
    // Vérifier l'existence par téléphone
    boolean existsByTelephone(String telephone);
    
    // Vérifier l'existence par code créancier
    boolean existsByCodeCreancier(String codeCreancier);
    
    // Vérifier l'existence par code créance
    boolean existsByCodeCreance(String codeCreance);
    
    // Rechercher par ville et code postal
    List<Creancier> findByVilleAndCodePostal(String ville, String codePostal);
    
    // Compter les créanciers par ville
    @Query("SELECT c.ville, COUNT(c) FROM Creancier c WHERE c.ville IS NOT NULL GROUP BY c.ville ORDER BY COUNT(c) DESC")
    List<Object[]> compterCreanciersParVille();

}
