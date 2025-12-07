package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Statistique;
import projet.carthagecreance_backend.Entity.TypeStatistique;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatistiqueRepository extends JpaRepository<Statistique, Long> {
    
    // Trouver par type
    List<Statistique> findByType(TypeStatistique type);
    
    // Trouver par utilisateur
    List<Statistique> findByUtilisateurId(Long utilisateurId);
    
    // Trouver par type et utilisateur
    List<Statistique> findByTypeAndUtilisateurId(TypeStatistique type, Long utilisateurId);
    
    // Trouver par période
    List<Statistique> findByPeriode(String periode);
    
    // Trouver par type et période
    List<Statistique> findByTypeAndPeriode(TypeStatistique type, String periode);
    
    // Trouver les statistiques récentes
    @Query("SELECT s FROM Statistique s WHERE s.dateCalcul >= :dateDebut ORDER BY s.dateCalcul DESC")
    List<Statistique> findStatistiquesRecent(@Param("dateDebut") LocalDateTime dateDebut);
    
    // Trouver la dernière statistique d'un type
    @Query("SELECT s FROM Statistique s WHERE s.type = :type ORDER BY s.dateCalcul DESC")
    Optional<Statistique> findLatestByType(@Param("type") TypeStatistique type);
    
    // Trouver la dernière statistique d'un type et utilisateur
    @Query("SELECT s FROM Statistique s WHERE s.type = :type AND s.utilisateur.id = :utilisateurId ORDER BY s.dateCalcul DESC")
    Optional<Statistique> findLatestByTypeAndUtilisateur(@Param("type") TypeStatistique type, @Param("utilisateurId") Long utilisateurId);
    
    // Supprimer les anciennes statistiques (avant une date)
    @Query("DELETE FROM Statistique s WHERE s.dateCalcul < :dateLimite")
    void deleteOldStatistiques(@Param("dateLimite") LocalDateTime dateLimite);
}

