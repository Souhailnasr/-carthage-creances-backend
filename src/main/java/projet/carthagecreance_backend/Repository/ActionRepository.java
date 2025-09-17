package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Action;
import projet.carthagecreance_backend.Entity.TypeAction;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {
    
    // Rechercher les actions par type
    List<Action> findByType(TypeAction type);
    
    // Rechercher les actions par dossier
    List<Action> findByDossierId(Long dossierId);
    
    // Rechercher les actions par date
    List<Action> findByDateAction(LocalDate dateAction);
    
    // Rechercher les actions entre deux dates
    List<Action> findByDateActionBetween(LocalDate dateDebut, LocalDate dateFin);
    
    // Rechercher les actions par type et dossier
    List<Action> findByTypeAndDossierId(TypeAction type, Long dossierId);
    
    // Calculer le coût total des actions pour un dossier
    @Query("SELECT COALESCE(SUM(a.nbOccurrences * a.coutUnitaire), 0) FROM Action a WHERE a.dossier.id = :dossierId")
    Double calculerCoutTotalParDossier(@Param("dossierId") Long dossierId);
    
    // Calculer le coût total des actions par type
    @Query("SELECT COALESCE(SUM(a.nbOccurrences * a.coutUnitaire), 0) FROM Action a WHERE a.type = :type")
    Double calculerCoutTotalParType(@Param("type") TypeAction type);
    
    // Rechercher les actions avec un coût supérieur à un montant
    @Query("SELECT a FROM Action a WHERE (a.nbOccurrences * a.coutUnitaire) > :montant")
    List<Action> findByCoutSuperieurA(@Param("montant") Double montant);
    
    // Compter le nombre d'actions par type
    @Query("SELECT a.type, COUNT(a) FROM Action a GROUP BY a.type")
    List<Object[]> compterActionsParType();
    
    // Rechercher les actions récentes (derniers 30 jours)
    @Query("SELECT a FROM Action a WHERE a.dateAction >= :dateLimite")
    List<Action> findActionsRecentes(@Param("dateLimite") LocalDate dateLimite);
}
