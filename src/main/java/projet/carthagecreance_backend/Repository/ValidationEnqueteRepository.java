package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.ValidationEnquete;
import projet.carthagecreance_backend.Entity.StatutValidation;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ValidationEnqueteRepository extends JpaRepository<ValidationEnquete, Long> {

    // Trouver les validations par statut
    List<ValidationEnquete> findByStatut(StatutValidation statut);

    // Trouver les validations en attente
    @Query("SELECT v FROM ValidationEnquete v WHERE v.statut = 'EN_ATTENTE'")
    List<ValidationEnquete> findEnquetesEnAttente();

    // Trouver les validations par agent créateur
    List<ValidationEnquete> findByAgentCreateurId(Long agentId);

    // Trouver les validations par chef validateur
    List<ValidationEnquete> findByChefValidateurId(Long chefId);

    // Trouver les validations par enquête
    List<ValidationEnquete> findByEnqueteId(Long enqueteId);

    // Compter les validations par statut
    Long countByStatut(StatutValidation statut);

    // Trouver les validations récentes
    @Query("SELECT v FROM ValidationEnquete v WHERE v.dateCreation >= :dateDebut ORDER BY v.dateCreation DESC")
    List<ValidationEnquete> findValidationsRecent(@Param("dateDebut") LocalDateTime dateDebut);
    // Trouver les validations par agent et statut
    List<ValidationEnquete> findByAgentCreateurIdAndStatut(Long agentId, StatutValidation statut);

    // Trouver les validations par chef et statut
    List<ValidationEnquete> findByChefValidateurIdAndStatut(Long chefId, StatutValidation statut);

    // Compter les validations par agent créateur
    long countByAgentCreateurId(Long agentId);

    // Compter les validations par chef validateur
    long countByChefValidateurId(Long chefId);

    // Trouver les validations par enquête et statut
    List<ValidationEnquete> findByEnqueteIdAndStatut(Long enqueteId, StatutValidation statut);

    // Trouver les validations par date de validation
    @Query("SELECT v FROM ValidationEnquete v WHERE v.dateValidation BETWEEN :dateDebut AND :dateFin ORDER BY v.dateValidation DESC")
    List<ValidationEnquete> findByDateValidationBetween(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    // Trouver les validations par date de création
    @Query("SELECT v FROM ValidationEnquete v WHERE v.dateCreation BETWEEN :dateDebut AND :dateFin ORDER BY v.dateCreation DESC")
    List<ValidationEnquete> findByDateCreationBetween(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    // Trouver les validations validées récemment
    @Query("SELECT v FROM ValidationEnquete v WHERE v.statut = 'VALIDE' AND v.dateValidation >= :dateDebut ORDER BY v.dateValidation DESC")
    List<ValidationEnquete> findValidationsValideesRecent(@Param("dateDebut") LocalDateTime dateDebut);

    // Trouver les validations rejetées récemment
    @Query("SELECT v FROM ValidationEnquete v WHERE v.statut = 'REJETE' AND v.dateValidation >= :dateDebut ORDER BY v.dateValidation DESC")
    List<ValidationEnquete> findValidationsRejeteesRecent(@Param("dateDebut") LocalDateTime dateDebut);

    // Trouver les validations en attente depuis plus de X jours
    @Query("SELECT v FROM ValidationEnquete v WHERE v.statut = 'EN_ATTENTE' AND v.dateCreation <= :dateLimite ORDER BY v.dateCreation ASC")
    List<ValidationEnquete> findValidationsEnAttenteDepuis(@Param("dateLimite") LocalDateTime dateLimite);

    // Trouver les validations par agent et enquête
    List<ValidationEnquete> findByAgentCreateurIdAndEnqueteId(Long agentId, Long enqueteId);

    // Trouver les validations par chef et enquête
    List<ValidationEnquete> findByChefValidateurIdAndEnqueteId(Long chefId, Long enqueteId);

    // Vérifier l'existence d'une validation pour une enquête
    boolean existsByEnqueteId(Long enqueteId);

    // Vérifier l'existence d'une validation en attente pour une enquête
    boolean existsByEnqueteIdAndStatut(Long enqueteId, StatutValidation statut);
    
    // Trouver les validations en attente avec enquête existante (JOIN pour éviter les orphelines)
    @Query("SELECT v FROM ValidationEnquete v WHERE v.statut = 'EN_ATTENTE' AND v.enquete IS NOT NULL")
    List<ValidationEnquete> findEnquetesEnAttenteWithExistingEnquete();
}
