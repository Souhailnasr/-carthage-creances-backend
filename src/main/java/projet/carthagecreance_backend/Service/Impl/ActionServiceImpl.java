package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Action;
import projet.carthagecreance_backend.Entity.TypeAction;
import projet.carthagecreance_backend.Repository.ActionRepository;
import projet.carthagecreance_backend.Service.ActionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ActionServiceImpl implements ActionService {

    @Autowired
    private ActionRepository actionRepository;

    @Override
    public Action createAction(Action action) {
        return actionRepository.save(action);
    }

    @Override
    public Optional<Action> getActionById(Long id) {
        return actionRepository.findById(id);
    }

    @Override
    public List<Action> getAllActions() {
        return actionRepository.findAll();
    }

    @Override
    public Action updateAction(Long id, Action action) {
        if (actionRepository.existsById(id)) {
            action.setId(id);
            return actionRepository.save(action);
        }
        throw new RuntimeException("Action not found with id: " + id);
    }

    @Override
    public void deleteAction(Long id) {
        if (actionRepository.existsById(id)) {
            actionRepository.deleteById(id);
        } else {
            throw new RuntimeException("Action not found with id: " + id);
        }
    }

    @Override
    public List<Action> getActionsByType(TypeAction type) {
        return actionRepository.findByType(type);
    }

    @Override
    public List<Action> getActionsByDossier(Long dossierId) {
        return actionRepository.findByDossierId(dossierId);
    }

    @Override
    public List<Action> getActionsByDate(LocalDate date) {
        return actionRepository.findByDateAction(date);
    }

    @Override
    public List<Action> getActionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return actionRepository.findByDateActionBetween(startDate, endDate);
    }

    @Override
    public List<Action> getActionsByTypeAndDossier(TypeAction type, Long dossierId) {
        return actionRepository.findByTypeAndDossierId(type, dossierId);
    }

    @Override
    public Double calculateTotalCostByDossier(Long dossierId) {
        return actionRepository.calculerCoutTotalParDossier(dossierId);
    }

    @Override
    public Double calculateTotalCostByType(TypeAction type) {
        return actionRepository.calculerCoutTotalParType(type);
    }

    @Override
    public List<Action> getActionsWithCostGreaterThan(Double amount) {
        return actionRepository.findByCoutSuperieurA(amount);
    }
}
