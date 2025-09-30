package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.TacheUrgente;
import projet.carthagecreance_backend.Entity.StatutTache;
import projet.carthagecreance_backend.Entity.PrioriteTache;
import projet.carthagecreance_backend.Service.TacheUrgenteService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrôleur REST complet pour la gestion des tâches urgentes
 * 
 * Ce contrôleur expose toutes les opérations CRUD et les fonctionnalités métier
 * pour la gestion des tâches urgentes dans le système de gestion de créances.
 * 
 * @author Système de Gestion de Créances
 * @version 1.0
 */
@RestController
@RequestMapping("/api/taches-urgentes")
@CrossOrigin(origins = "http://localhost:4200")
public class TacheUrgenteController {

    @Autowired
    private TacheUrgenteService tacheUrgenteService;

    /**
     * Crée une nouvelle tâche urgente
     * 
     * @param tache La tâche à créer (titre, description, priorité, date d'échéance, agent assigné)
     * @return ResponseEntity avec la tâche créée (201 CREATED) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/taches-urgentes
     * {
     *   "titre": "Relance client urgent",
     *   "description": "Relancer le client pour le paiement de la facture 2024-001",
     *   "priorite": "HAUTE",
     *   "dateEcheance": "2024-01-15T10:00:00",
     *   "agentAssigné": {"id": 1}
     * }
     */
    @PostMapping
    public ResponseEntity<TacheUrgente> createTacheUrgente(@RequestBody TacheUrgente tache) {
        try {
            TacheUrgente createdTache = tacheUrgenteService.createTacheUrgente(tache);
            return ResponseEntity.ok(createdTache);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Met à jour une tâche urgente existante
     * 
     * @param id L'ID de la tâche à modifier
     * @param tache Les nouvelles données de la tâche
     * @return ResponseEntity avec la tâche mise à jour (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * PUT /api/taches-urgentes/1
     * {
     *   "titre": "Relance client urgent - Mise à jour",
     *   "description": "Relancer le client pour le paiement de la facture 2024-001 - Urgent",
     *   "priorite": "TRES_HAUTE"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<TacheUrgente> updateTacheUrgente(@PathVariable Long id, @RequestBody TacheUrgente tache) {
        try {
            TacheUrgente updatedTache = tacheUrgenteService.updateTacheUrgente(id, tache);
            return ResponseEntity.ok(updatedTache);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprime une tâche urgente
     * 
     * @param id L'ID de la tâche à supprimer
     * @return ResponseEntity vide (204 NO_CONTENT) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * DELETE /api/taches-urgentes/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTacheUrgente(@PathVariable Long id) {
        try {
            tacheUrgenteService.deleteTacheUrgente(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère une tâche urgente par son ID
     * 
     * @param id L'ID de la tâche
     * @return ResponseEntity avec la tâche trouvée (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * GET /api/taches-urgentes/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<TacheUrgente> getTacheUrgenteById(@PathVariable Long id) {
        try {
            TacheUrgente tache = tacheUrgenteService.getTacheUrgenteById(id);
            return ResponseEntity.ok(tache);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère toutes les tâches urgentes
     * 
     * @return ResponseEntity avec la liste de toutes les tâches urgentes (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes
     */
    @GetMapping
    public ResponseEntity<List<TacheUrgente>> getAllTachesUrgentes() {
        try {
            List<TacheUrgente> taches = tacheUrgenteService.getAllTachesUrgentes();
            return ResponseEntity.ok(taches);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS DE FILTRAGE ====================

    /**
     * Récupère les tâches par agent
     * 
     * @param agentId L'ID de l'agent
     * @return ResponseEntity avec la liste des tâches de l'agent (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/agent/1
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<TacheUrgente>> getTachesByAgent(@PathVariable Long agentId) {
        try {
            List<TacheUrgente> taches = tacheUrgenteService.getTachesByAgent(agentId);
            return ResponseEntity.ok(taches);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les tâches par statut
     * 
     * @param statut Le statut des tâches (EN_ATTENTE, EN_COURS, TERMINEE, ANNULEE)
     * @return ResponseEntity avec la liste des tâches avec le statut spécifié (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/statut/EN_COURS
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<TacheUrgente>> getTachesByStatut(@PathVariable StatutTache statut) {
        try {
            List<TacheUrgente> taches = tacheUrgenteService.getTachesByStatut(statut);
            return ResponseEntity.ok(taches);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les tâches par priorité
     * 
     * @param priorite La priorité des tâches (FAIBLE, MOYENNE, HAUTE, TRES_HAUTE)
     * @return ResponseEntity avec la liste des tâches avec la priorité spécifiée (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/priorite/HAUTE
     */
    @GetMapping("/priorite/{priorite}")
    public ResponseEntity<List<TacheUrgente>> getTachesByPriorite(@PathVariable PrioriteTache priorite) {
        try {
            List<TacheUrgente> taches = tacheUrgenteService.getTachesByPriorite(priorite);
            return ResponseEntity.ok(taches);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les tâches urgentes (échéance proche)
     * 
     * @param dateLimite Date limite pour considérer une tâche comme urgente (optionnel, défaut: 7 jours)
     * @return ResponseEntity avec la liste des tâches urgentes (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/urgentes?dateLimite=2024-01-15T10:00:00
     */
    @GetMapping("/urgentes")
    public ResponseEntity<List<TacheUrgente>> getTachesUrgentes(
            @RequestParam(required = false) LocalDateTime dateLimite) {
        try {
            if (dateLimite == null) {
                dateLimite = LocalDateTime.now().plusDays(7);
            }
            List<TacheUrgente> taches = tacheUrgenteService.getTachesUrgentes(dateLimite);
            return ResponseEntity.ok(taches);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les tâches en retard
     * 
     * @return ResponseEntity avec la liste des tâches en retard (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/en-retard
     */
    @GetMapping("/en-retard")
    public ResponseEntity<List<TacheUrgente>> getTachesEnRetard() {
        try {
            List<TacheUrgente> taches = tacheUrgenteService.getTachesEnRetard();
            return ResponseEntity.ok(taches);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les tâches par agent et priorité
     * 
     * @param agentId L'ID de l'agent
     * @param priorite La priorité des tâches
     * @return ResponseEntity avec la liste des tâches correspondantes (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/agent/1/priorite/HAUTE
     */
    @GetMapping("/agent/{agentId}/priorite/{priorite}")
    public ResponseEntity<List<TacheUrgente>> getTachesByAgentAndPriorite(
            @PathVariable Long agentId, 
            @PathVariable PrioriteTache priorite) {
        try {
            List<TacheUrgente> taches = tacheUrgenteService.getTachesByAgentAndPriorite(agentId, priorite);
            return ResponseEntity.ok(taches);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS D'ACTIONS ====================

    /**
     * Marque une tâche comme terminée
     * 
     * @param tacheId L'ID de la tâche
     * @param commentaire Commentaire de completion (optionnel)
     * @return ResponseEntity avec la tâche mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/taches-urgentes/1/complete?commentaire=Tâche terminée avec succès
     */
    @PostMapping("/{tacheId}/complete")
    public ResponseEntity<TacheUrgente> marquerComplete(
            @PathVariable Long tacheId, 
            @RequestParam(required = false) String commentaire) {
        try {
            TacheUrgente tache = tacheUrgenteService.marquerComplete(tacheId, commentaire);
            return ResponseEntity.ok(tache);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marque une tâche comme en cours
     * 
     * @param tacheId L'ID de la tâche
     * @return ResponseEntity avec la tâche mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/taches-urgentes/1/en-cours
     */
    @PostMapping("/{tacheId}/en-cours")
    public ResponseEntity<TacheUrgente> marquerEnCours(@PathVariable Long tacheId) {
        try {
            TacheUrgente tache = tacheUrgenteService.marquerEnCours(tacheId);
            return ResponseEntity.ok(tache);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Annule une tâche
     * 
     * @param tacheId L'ID de la tâche
     * @param raison Raison de l'annulation
     * @return ResponseEntity avec la tâche mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/taches-urgentes/1/annuler?raison=Client a payé
     */
    @PostMapping("/{tacheId}/annuler")
    public ResponseEntity<TacheUrgente> annulerTache(
            @PathVariable Long tacheId, 
            @RequestParam String raison) {
        try {
            TacheUrgente tache = tacheUrgenteService.annulerTache(tacheId, raison);
            return ResponseEntity.ok(tache);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS DE STATISTIQUES ====================

    /**
     * Compte les tâches par agent
     * 
     * @param agentId L'ID de l'agent
     * @return ResponseEntity avec le nombre de tâches de l'agent (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/statistiques/agent/1
     */
    @GetMapping("/statistiques/agent/{agentId}")
    public ResponseEntity<Long> countTachesByAgent(@PathVariable Long agentId) {
        try {
            long count = tacheUrgenteService.countTachesByAgent(agentId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compte les tâches par statut
     * 
     * @param statut Le statut des tâches
     * @return ResponseEntity avec le nombre de tâches avec ce statut (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/statistiques/statut/EN_COURS
     */
    @GetMapping("/statistiques/statut/{statut}")
    public ResponseEntity<Long> countTachesByStatut(@PathVariable StatutTache statut) {
        try {
            long count = tacheUrgenteService.countTachesByStatut(statut);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compte les tâches par agent et statut
     * 
     * @param agentId L'ID de l'agent
     * @param statut Le statut des tâches
     * @return ResponseEntity avec le nombre de tâches correspondantes (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/statistiques/agent/1/statut/EN_COURS
     */
    @GetMapping("/statistiques/agent/{agentId}/statut/{statut}")
    public ResponseEntity<Long> countTachesByAgentAndStatut(
            @PathVariable Long agentId, 
            @PathVariable StatutTache statut) {
        try {
            long count = tacheUrgenteService.countTachesByAgentAndStatut(agentId, statut);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compte les tâches urgentes
     * 
     * @return ResponseEntity avec le nombre de tâches urgentes (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/statistiques/urgentes
     */
    @GetMapping("/statistiques/urgentes")
    public ResponseEntity<Long> countTachesUrgentes() {
        try {
            long count = tacheUrgenteService.countTachesUrgentes();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
