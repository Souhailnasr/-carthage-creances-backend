package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.TacheUrgente;
import projet.carthagecreance_backend.Entity.StatutTache;
import projet.carthagecreance_backend.Entity.PrioriteTache;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TacheUrgenteRepository extends JpaRepository<TacheUrgente, Long> {

    // Trouver les tâches par agent assigné
    List<TacheUrgente> findByAgentAssignéId(Long agentId);

    // Trouver les tâches par statut
    List<TacheUrgente> findByStatut(StatutTache statut);

    // Trouver les tâches par priorité
    List<TacheUrgente> findByPriorite(PrioriteTache priorite);

    // Trouver les tâches urgentes (échéance proche)
    @Query("SELECT t FROM TacheUrgente t WHERE t.dateEcheance <= :dateLimite AND t.statut != 'TERMINEE'")
    List<TacheUrgente> findTachesUrgentes(@Param("dateLimite") LocalDateTime dateLimite);

    // Trouver les tâches en retard
    @Query("SELECT t FROM TacheUrgente t WHERE t.dateEcheance < :maintenant AND t.statut != 'TERMINEE'")
    List<TacheUrgente> findTachesEnRetard(@Param("maintenant") LocalDateTime maintenant);

    // Trouver les tâches par chef créateur
    List<TacheUrgente> findByChefCreateurId(Long chefId);
    // Trouver les tâches par dossier
    List<TacheUrgente> findByDossierId(Long dossierId);

    // Trouver les tâches par enquête
    List<TacheUrgente> findByEnqueteId(Long enqueteId);

    // Trouver les tâches par agent et priorité
    List<TacheUrgente> findByAgentAssignéIdAndPriorite(Long agentId, PrioriteTache priorite);

    // Trouver les tâches récentes
    @Query("SELECT t FROM TacheUrgente t WHERE t.dateCreation >= :dateDebut ORDER BY t.dateCreation DESC")
    List<TacheUrgente> findTachesRecent(@Param("dateDebut") LocalDateTime dateDebut);

    // Compter les tâches par agent
    long countByAgentAssignéId(Long agentId);

    // Compter les tâches par statut
    long countByStatut(StatutTache statut);

    // Compter les tâches urgentes
    @Query("SELECT COUNT(t) FROM TacheUrgente t WHERE t.dateEcheance <= :dateLimite AND t.statut != 'TERMINEE'")
    long countTachesUrgentes(@Param("dateLimite") LocalDateTime dateLimite);

    // Compter les tâches par agent et statut
    long countByAgentAssignéIdAndStatut(Long agentId, StatutTache statut);

    // Trouver les tâches par agent et statut
    List<TacheUrgente> findByAgentAssignéIdAndStatut(Long agentId, StatutTache statut);

    // Trouver les tâches par chef créateur et statut
    List<TacheUrgente> findByChefCreateurIdAndStatut(Long chefId, StatutTache statut);

    // Trouver les tâches par priorité et statut
    List<TacheUrgente> findByPrioriteAndStatut(PrioriteTache priorite, StatutTache statut);

    // Trouver les tâches par type
    List<TacheUrgente> findByType(projet.carthagecreance_backend.Entity.TypeTache type);

    // Trouver les tâches par type et statut
    List<TacheUrgente> findByTypeAndStatut(projet.carthagecreance_backend.Entity.TypeTache type, StatutTache statut);

    // Trouver les tâches par agent et type
    List<TacheUrgente> findByAgentAssignéIdAndType(Long agentId, projet.carthagecreance_backend.Entity.TypeTache type);

    // Trouver les tâches par date d'échéance
    @Query("SELECT t FROM TacheUrgente t WHERE t.dateEcheance BETWEEN :dateDebut AND :dateFin ORDER BY t.dateEcheance ASC")
    List<TacheUrgente> findByDateEcheanceBetween(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    // Trouver les tâches par date de création
    @Query("SELECT t FROM TacheUrgente t WHERE t.dateCreation BETWEEN :dateDebut AND :dateFin ORDER BY t.dateCreation DESC")
    List<TacheUrgente> findByDateCreationBetween(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    // Trouver les tâches terminées récemment
    @Query("SELECT t FROM TacheUrgente t WHERE t.statut = 'TERMINEE' AND t.dateCompletion >= :dateDebut ORDER BY t.dateCompletion DESC")
    List<TacheUrgente> findTachesTermineesRecent(@Param("dateDebut") LocalDateTime dateDebut);

    // Trouver les tâches par agent et date d'échéance
    @Query("SELECT t FROM TacheUrgente t WHERE t.agentAssigné.id = :agentId AND t.dateEcheance BETWEEN :dateDebut AND :dateFin ORDER BY t.dateEcheance ASC")
    List<TacheUrgente> findByAgentAndDateEcheanceBetween(@Param("agentId") Long agentId, @Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);
}

