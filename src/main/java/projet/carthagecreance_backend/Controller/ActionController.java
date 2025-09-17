package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Action;
import projet.carthagecreance_backend.Entity.TypeAction;
import projet.carthagecreance_backend.Service.ActionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/actions")
@CrossOrigin(origins = "*")
public class ActionController {

    @Autowired
    private ActionService actionService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<Action> createAction(@RequestBody Action action) {
        try {
            Action createdAction = actionService.createAction(action);
            return new ResponseEntity<>(createdAction, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Action> getActionById(@PathVariable Long id) {
        Optional<Action> action = actionService.getActionById(id);
        return action.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Action>> getAllActions() {
        List<Action> actions = actionService.getAllActions();
        return new ResponseEntity<>(actions, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Action> updateAction(@PathVariable Long id, @RequestBody Action action) {
        try {
            Action updatedAction = actionService.updateAction(id, action);
            return new ResponseEntity<>(updatedAction, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAction(@PathVariable Long id) {
        try {
            actionService.deleteAction(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Search Operations
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Action>> getActionsByType(@PathVariable TypeAction type) {
        List<Action> actions = actionService.getActionsByType(type);
        return new ResponseEntity<>(actions, HttpStatus.OK);
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<Action>> getActionsByDossier(@PathVariable Long dossierId) {
        List<Action> actions = actionService.getActionsByDossier(dossierId);
        return new ResponseEntity<>(actions, HttpStatus.OK);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Action>> getActionsByDate(@PathVariable LocalDate date) {
        List<Action> actions = actionService.getActionsByDate(date);
        return new ResponseEntity<>(actions, HttpStatus.OK);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Action>> getActionsByDateRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        List<Action> actions = actionService.getActionsByDateRange(startDate, endDate);
        return new ResponseEntity<>(actions, HttpStatus.OK);
    }

    @GetMapping("/type/{type}/dossier/{dossierId}")
    public ResponseEntity<List<Action>> getActionsByTypeAndDossier(
            @PathVariable TypeAction type, 
            @PathVariable Long dossierId) {
        List<Action> actions = actionService.getActionsByTypeAndDossier(type, dossierId);
        return new ResponseEntity<>(actions, HttpStatus.OK);
    }

    // Calculated Operations
    @GetMapping("/dossier/{dossierId}/total-cost")
    public ResponseEntity<Double> getTotalCostByDossier(@PathVariable Long dossierId) {
        Double totalCost = actionService.calculateTotalCostByDossier(dossierId);
        return new ResponseEntity<>(totalCost, HttpStatus.OK);
    }

    @GetMapping("/type/{type}/total-cost")
    public ResponseEntity<Double> getTotalCostByType(@PathVariable TypeAction type) {
        Double totalCost = actionService.calculateTotalCostByType(type);
        return new ResponseEntity<>(totalCost, HttpStatus.OK);
    }

    @GetMapping("/cost-greater-than")
    public ResponseEntity<List<Action>> getActionsWithCostGreaterThan(@RequestParam Double amount) {
        List<Action> actions = actionService.getActionsWithCostGreaterThan(amount);
        return new ResponseEntity<>(actions, HttpStatus.OK);
    }
}
