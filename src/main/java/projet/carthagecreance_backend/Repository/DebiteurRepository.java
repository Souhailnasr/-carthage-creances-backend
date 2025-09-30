package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Debiteur;

import java.util.List;
import java.util.Optional;

@Repository
public interface DebiteurRepository extends JpaRepository<Debiteur, Long> {
    
    // Rechercher par code créance
    Optional<Debiteur> findByCodeCreance(String codeCreance);
    
    // Rechercher par nom exact
    Optional<Debiteur> findByNom(String nom);
    
    // Rechercher par nom
    List<Debiteur> findByNomContainingIgnoreCase(String nom);
    
    // Rechercher par prénom
    List<Debiteur> findByPrenomContainingIgnoreCase(String prenom);
    
    // Rechercher par nom et prénom
    List<Debiteur> findByNomAndPrenom(String nom, String prenom);
    
    // Rechercher par email
    Optional<Debiteur> findByEmail(String email);
    
    // Rechercher par téléphone
    Optional<Debiteur> findByTelephone(String telephone);
    
    // Rechercher par ville
    List<Debiteur> findByVilleContainingIgnoreCase(String ville);
    
    // Rechercher par code postal
    List<Debiteur> findByCodePostal(String codePostal);
    
    // Rechercher par nom ou prénom (recherche globale)
    @Query("SELECT d FROM Debiteur d WHERE LOWER(d.nom) LIKE LOWER(CONCAT('%', :recherche, '%')) OR LOWER(d.prenom) LIKE LOWER(CONCAT('%', :recherche, '%'))")
    List<Debiteur> findByNomOuPrenomContaining(@Param("recherche") String recherche);
    
    // Vérifier l'existence par email
    boolean existsByEmail(String email);
    
    // Vérifier l'existence par téléphone
    boolean existsByTelephone(String telephone);
    
    // Vérifier l'existence par code créance
    boolean existsByCodeCreance(String codeCreance);
    
    // Rechercher par ville et code postal
    List<Debiteur> findByVilleAndCodePostal(String ville, String codePostal);
    
    // Compter les débiteurs par ville
    @Query("SELECT d.ville, COUNT(d) FROM Debiteur d WHERE d.ville IS NOT NULL GROUP BY d.ville ORDER BY COUNT(d) DESC")
    List<Object[]> compterDebiteursParVille();
    
    // Rechercher les débiteurs avec adresse élue
    @Query("SELECT d FROM Debiteur d WHERE d.adresseElue IS NOT NULL AND d.adresseElue != ''")
    List<Debiteur> findDebiteursAvecAdresseElue();
}
