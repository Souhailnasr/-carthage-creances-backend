package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.ValidationDossier;
import projet.carthagecreance_backend.Entity.StatutValidation;

import java.util.List;

@Repository
public interface ValidationDossierRepository extends JpaRepository<ValidationDossier, Long> {

    // Trouver les validations par statut
    List<ValidationDossier> findByStatut(StatutValidation statut);

    // Trouver les validations en attente
    @Query("SELECT v FROM ValidationDossier v WHERE v.statut = 'EN_ATTENTE'")
    List<ValidationDossier> findDossiersEnAttente();

    // Trouver les validations par agent créateur
    List<ValidationDossier> findByAgentCreateurId(Long agentId);

    // Trouver les validations par chef validateur
    List<ValidationDossier> findByChefValidateurId(Long chefId);

    // Trouver les validations par dossier
    List<ValidationDossier> findByDossierId(Long dossierId);
    // Trouver les validations récentes
    @Query("SELECT v FROM ValidationDossier v WHERE v.dateCreation >= :dateDebut ORDER BY v.dateCreation DESC")
    List<ValidationDossier> findValidationsRecent(@Param("dateDebut") java.time.LocalDateTime dateDebut);

    // Compter les validations par statut
    long countByStatut(StatutValidation statut);

    // Compter les validations par agent créateur
    long countByAgentCreateurId(Long agentId);

    // Compter les validations par chef validateur
    long countByChefValidateurId(Long chefId);

    // Trouver les validations par agent et statut
    List<ValidationDossier> findByAgentCreateurIdAndStatut(Long agentId, StatutValidation statut);

    // Trouver les validations par chef et statut
    List<ValidationDossier> findByChefValidateurIdAndStatut(Long chefId, StatutValidation statut);

    // Trouver les validations par dossier et statut
    List<ValidationDossier> findByDossierIdAndStatut(Long dossierId, StatutValidation statut);

    // Trouver les validations par date de validation
    @Query("SELECT v FROM ValidationDossier v WHERE v.dateValidation BETWEEN :dateDebut AND :dateFin ORDER BY v.dateValidation DESC")
    List<ValidationDossier> findByDateValidationBetween(@Param("dateDebut") java.time.LocalDateTime dateDebut, @Param("dateFin") java.time.LocalDateTime dateFin);

    // Trouver les validations par date de création
    @Query("SELECT v FROM ValidationDossier v WHERE v.dateCreation BETWEEN :dateDebut AND :dateFin ORDER BY v.dateCreation DESC")
    List<ValidationDossier> findByDateCreationBetween(@Param("dateDebut") java.time.LocalDateTime dateDebut, @Param("dateFin") java.time.LocalDateTime dateFin);

    // Trouver les validations validées récemment
    @Query("SELECT v FROM ValidationDossier v WHERE v.statut = 'VALIDE' AND v.dateValidation >= :dateDebut ORDER BY v.dateValidation DESC")
    List<ValidationDossier> findValidationsValideesRecent(@Param("dateDebut") java.time.LocalDateTime dateDebut);

    // Trouver les validations rejetées récemment
    @Query("SELECT v FROM ValidationDossier v WHERE v.statut = 'REJETE' AND v.dateValidation >= :dateDebut ORDER BY v.dateValidation DESC")
    List<ValidationDossier> findValidationsRejeteesRecent(@Param("dateDebut") java.time.LocalDateTime dateDebut);

    // Trouver les validations en attente depuis plus de X jours
    @Query("SELECT v FROM ValidationDossier v WHERE v.statut = 'EN_ATTENTE' AND v.dateCreation <= :dateLimite ORDER BY v.dateCreation ASC")
    List<ValidationDossier> findValidationsEnAttenteDepuis(@Param("dateLimite") java.time.LocalDateTime dateLimite);

    // Trouver les validations par agent et dossier
    List<ValidationDossier> findByAgentCreateurIdAndDossierId(Long agentId, Long dossierId);

    // Trouver les validations par chef et dossier
    List<ValidationDossier> findByChefValidateurIdAndDossierId(Long chefId, Long dossierId);

    // Vérifier l'existence d'une validation pour un dossier
    boolean existsByDossierId(Long dossierId);

    // Vérifier l'existence d'une validation en attente pour un dossier
    boolean existsByDossierIdAndStatut(Long dossierId, StatutValidation statut);
}
