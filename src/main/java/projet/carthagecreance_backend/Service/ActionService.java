package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Action;
import projet.carthagecreance_backend.Entity.TypeAction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActionService {
    
    // CRUD Operations
    Action createAction(Action action);
    Optional<Action> getActionById(Long id);
    List<Action> getAllActions();
    Action updateAction(Long id, Action action);
    void deleteAction(Long id);
    
    // Search Operations
    List<Action> getActionsByType(TypeAction type);
    List<Action> getActionsByDossier(Long dossierId);
    List<Action> getActionsByDate(LocalDate date);
    List<Action> getActionsByDateRange(LocalDate startDate, LocalDate endDate);
    List<Action> getActionsByTypeAndDossier(TypeAction type, Long dossierId);
    
    // Calculated Operations
    Double calculateTotalCostByDossier(Long dossierId);
    Double calculateTotalCostByType(TypeAction type);
    List<Action> getActionsWithCostGreaterThan(Double amount);
}
