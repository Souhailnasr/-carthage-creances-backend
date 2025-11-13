package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Enquette;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnquetteRepository extends JpaRepository<Enquette, Long> {

    // Rechercher par date de création
    List<Enquette> findByDateCreation(LocalDate dateCreation);

    // Rechercher par date de création entre deux dates
    List<Enquette> findByDateCreationBetween(LocalDate dateDebut, LocalDate dateFin);


    // Rechercher par dossier
    @Query("SELECT e FROM Enquette e WHERE e.dossier.id = :dossierId")
    Optional<Enquette> findByDossierId(@Param("dossierId") Long dossierId);
    
    // Vérifier l'existence d'une enquête par ID (sans charger les relations)
    // Utiliser CAST pour convertir le résultat en BOOLEAN (MySQL/MariaDB retourne INTEGER pour les comparaisons)
    @Query(value = "SELECT CAST(COUNT(*) > 0 AS UNSIGNED) FROM enquette WHERE id = :id", nativeQuery = true)
    Integer existsByIdNative(@Param("id") Long id);
    
    // Trouver une enquête par ID sans charger la relation Dossier (pour la suppression)
    @Query(value = "SELECT * FROM enquette WHERE id = :id", nativeQuery = true)
    Optional<Enquette> findByIdNative(@Param("id") Long id);
    
    // Supprimer une enquête directement par ID (requête native pour éviter les problèmes de relations)
    @Query(value = "DELETE FROM enquette WHERE id = :id", nativeQuery = true)
    @Modifying
    void deleteByIdNative(@Param("id") Long id);
    
    // Mettre à jour le statut d'une enquête sans charger la relation Dossier
    @Query(value = "UPDATE enquette SET statut = :statut, valide = :valide, date_validation = :dateValidation, commentaire_validation = :commentaire WHERE id = :id", nativeQuery = true)
    @Modifying
    void updateStatutNative(@Param("id") Long id, @Param("statut") String statut, @Param("valide") Boolean valide, @Param("dateValidation") java.time.LocalDateTime dateValidation, @Param("commentaire") String commentaire);
    
    // ==================== STATISTIQUES ====================
    
    // Compter les enquêtes par statut
    @Query("SELECT COUNT(e) FROM Enquette e WHERE e.statut = :statut")
    long countByStatut(@Param("statut") projet.carthagecreance_backend.Entity.Statut statut);
    
    // Compter les enquêtes validées
    @Query("SELECT COUNT(e) FROM Enquette e WHERE e.valide = true")
    long countByValideTrue();
    
    // Compter les enquêtes non validées
    @Query("SELECT COUNT(e) FROM Enquette e WHERE e.valide = false OR e.valide IS NULL")
    long countByValideFalse();
    
    // Compter les enquêtes créées après une date
    @Query("SELECT COUNT(e) FROM Enquette e WHERE e.dateCreation >= :date")
    long countByDateCreationAfter(@Param("date") LocalDate date);
    
    // Compter les enquêtes par agent créateur
    @Query("SELECT COUNT(e) FROM Enquette e WHERE e.agentCreateur.id = :agentId")
    long countByAgentCreateurId(@Param("agentId") Long agentId);
    
    // Compter les enquêtes par agent responsable
    @Query("SELECT COUNT(e) FROM Enquette e WHERE e.agentResponsable.id = :agentId")
    long countByAgentResponsableId(@Param("agentId") Long agentId);
}
