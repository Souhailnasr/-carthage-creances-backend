package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.DTO.ActionRequestDTO;
import projet.carthagecreance_backend.Entity.Action;
import projet.carthagecreance_backend.Entity.TypeAction;
import projet.carthagecreance_backend.Entity.ReponseDebiteur;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActionService {
    
    // CRUD Operations
    Action createAction(Action action);
    Action createActionFromDTO(ActionRequestDTO actionDTO); // Nouvelle méthode avec DTO
    Optional<Action> getActionById(Long id);
    List<Action> getAllActions();
    Action updateAction(Long id, Action action);
    Action updateActionFromDTO(Long id, ActionRequestDTO actionDTO); // Nouvelle méthode avec DTO
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
    
    // ==================== MÉTHODES POUR REPONSEDEBITEUR ====================
    
    // Search Operations by ReponseDebiteur
    List<Action> getActionsByReponseDebiteur(ReponseDebiteur reponseDebiteur);
    List<Action> getActionsByTypeAndReponseDebiteur(TypeAction type, ReponseDebiteur reponseDebiteur);
    List<Action> getActionsByDossierAndReponseDebiteur(Long dossierId, ReponseDebiteur reponseDebiteur);
    List<Action> getActionsByTypeAndDossierAndReponseDebiteur(TypeAction type, Long dossierId, ReponseDebiteur reponseDebiteur);
    
    // Statistics Operations for ReponseDebiteur
    List<Object[]> getActionCountByReponseDebiteur();
    List<Object[]> getActionCountByTypeAndReponseDebiteur();
    Double calculateTotalCostByReponseDebiteur(ReponseDebiteur reponseDebiteur);
    
    // Specific ReponseDebiteur Operations
    List<Action> getActionsWithPositiveResponse();
    List<Action> getActionsWithNegativeResponse();
    List<Action> getActionsWithoutResponse();
}
