package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.ValidationDossier;
import projet.carthagecreance_backend.Entity.StatutValidation;
import projet.carthagecreance_backend.Service.ValidationDossierService;

import java.util.List;

/**
 * Contrôleur REST complet pour la gestion des validations de dossiers
 * 
 * Ce contrôleur expose toutes les opérations CRUD et les fonctionnalités métier
 * pour la gestion des validations de dossiers dans le système de gestion de créances.
 * 
 * @author Système de Gestion de Créances
 * @version 1.0
 */
@RestController
@RequestMapping("/api/validation/dossiers")
@CrossOrigin(origins = "http://localhost:4200")
public class ValidationDossierController {

    @Autowired
    private ValidationDossierService validationDossierService;

    // ==================== ENDPOINTS DE BASE (CRUD) ====================

    /**
     * Crée une nouvelle validation de dossier
     * 
     * @param validation La validation à créer (dossier, agent créateur, commentaires)
     * @return ResponseEntity avec la validation créée (201 CREATED) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/validation/dossiers
     * {
     *   "dossier": {"id": 1},
     *   "agentCreateur": {"id": 2},
     *   "commentaires": "Demande de validation pour le dossier 2024-001"
     * }
     */
    @PostMapping
    public ResponseEntity<ValidationDossier> createValidationDossier(@RequestBody ValidationDossier validation) {
        try {
            ValidationDossier createdValidation = validationDossierService.createValidationDossier(validation);
            return ResponseEntity.ok(createdValidation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Met à jour une validation de dossier existante
     * 
     * @param id L'ID de la validation à modifier
     * @param validation Les nouvelles données de la validation
     * @return ResponseEntity avec la validation mise à jour (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * PUT /api/validation/dossiers/1
     * {
     *   "commentaires": "Mise à jour des commentaires de validation"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ValidationDossier> updateValidationDossier(@PathVariable Long id, @RequestBody ValidationDossier validation) {
        try {
            ValidationDossier updatedValidation = validationDossierService.updateValidationDossier(id, validation);
            return ResponseEntity.ok(updatedValidation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprime une validation de dossier
     * 
     * @param id L'ID de la validation à supprimer
     * @return ResponseEntity vide (204 NO_CONTENT) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * DELETE /api/validation/dossiers/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteValidationDossier(@PathVariable Long id) {
        try {
            validationDossierService.deleteValidationDossier(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère une validation de dossier par son ID
     * 
     * @param id L'ID de la validation
     * @return ResponseEntity avec la validation trouvée (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * GET /api/validation/dossiers/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<ValidationDossier> getValidationDossierById(@PathVariable Long id) {
        try {
            ValidationDossier validation = validationDossierService.getValidationDossierById(id);
            return ResponseEntity.ok(validation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère toutes les validations de dossiers
     * 
     * @return ResponseEntity avec la liste de toutes les validations (200 OK)
     * 
     * @example
     * GET /api/validation/dossiers
     */
    @GetMapping
    public ResponseEntity<List<ValidationDossier>> getAllValidationsDossier() {
        try {
            List<ValidationDossier> validations = validationDossierService.getAllValidationsDossier();
            return ResponseEntity.ok(validations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS DE FILTRAGE ====================

    /**
     * Récupère les dossiers en attente de validation
     * 
     * @return ResponseEntity avec la liste des validations en attente (200 OK)
     * 
     * @example
     * GET /api/validation/dossiers/en-attente
     */
    @GetMapping("/en-attente")
    public ResponseEntity<List<ValidationDossier>> getDossiersEnAttente() {
        try {
            List<ValidationDossier> validations = validationDossierService.getDossiersEnAttente();
            return ResponseEntity.ok(validations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les validations par agent créateur
     * 
     * @param agentId L'ID de l'agent créateur
     * @return ResponseEntity avec la liste des validations de l'agent (200 OK)
     * 
     * @example
     * GET /api/validation/dossiers/agent/1
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ValidationDossier>> getValidationsByAgent(@PathVariable Long agentId) {
        try {
            List<ValidationDossier> validations = validationDossierService.getValidationsByAgent(agentId);
            return ResponseEntity.ok(validations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les validations par chef validateur
     * 
     * @param chefId L'ID du chef validateur
     * @return ResponseEntity avec la liste des validations du chef (200 OK)
     * 
     * @example
     * GET /api/validation/dossiers/chef/1
     */
    @GetMapping("/chef/{chefId}")
    public ResponseEntity<List<ValidationDossier>> getValidationsByChef(@PathVariable Long chefId) {
        try {
            List<ValidationDossier> validations = validationDossierService.getValidationsByChef(chefId);
            return ResponseEntity.ok(validations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les validations par dossier
     * 
     * @param dossierId L'ID du dossier
     * @return ResponseEntity avec la liste des validations du dossier (200 OK)
     * 
     * @example
     * GET /api/validation/dossiers/dossier/1
     */
    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<ValidationDossier>> getValidationsByDossier(@PathVariable Long dossierId) {
        try {
            List<ValidationDossier> validations = validationDossierService.getValidationsByDossier(dossierId);
            return ResponseEntity.ok(validations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les validations par statut
     * 
     * @param statut Le statut des validations (EN_ATTENTE, VALIDE, REJETE)
     * @return ResponseEntity avec la liste des validations avec le statut spécifié (200 OK)
     * 
     * @example
     * GET /api/validation/dossiers/statut/EN_ATTENTE
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<ValidationDossier>> getValidationsByStatut(@PathVariable StatutValidation statut) {
        try {
            List<ValidationDossier> validations = validationDossierService.getValidationsByStatut(statut);
            return ResponseEntity.ok(validations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS D'ACTIONS ====================

    /**
     * Valide un dossier
     * 
     * @param id L'ID de la validation à valider
     * @param chefId L'ID du chef qui valide
     * @param commentaire Le commentaire de validation (optionnel)
     * @return ResponseEntity avec la validation mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/validation/dossiers/1/valider?chefId=2&commentaire=Dossier validé avec succès
     */
    @PostMapping("/{id}/valider")
    public ResponseEntity<ValidationDossier> validerDossier(
            @PathVariable Long id,
            @RequestParam Long chefId,
            @RequestParam(required = false) String commentaire) {
        try {
            ValidationDossier validation = validationDossierService.validerDossier(id, chefId, commentaire);
            return ResponseEntity.ok(validation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Rejette un dossier
     * 
     * @param id L'ID de la validation à rejeter
     * @param chefId L'ID du chef qui rejette
     * @param commentaire Le commentaire de rejet (optionnel)
     * @return ResponseEntity avec la validation mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/validation/dossiers/1/rejeter?chefId=2&commentaire=Documents manquants
     */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<ValidationDossier> rejeterDossier(
            @PathVariable Long id,
            @RequestParam Long chefId,
            @RequestParam(required = false) String commentaire) {
        try {
            ValidationDossier validation = validationDossierService.rejeterDossier(id, chefId, commentaire);
            return ResponseEntity.ok(validation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Remet une validation en attente
     * 
     * @param id L'ID de la validation à remettre en attente
     * @param commentaire Le commentaire de remise en attente (optionnel)
     * @return ResponseEntity avec la validation mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/validation/dossiers/1/en-attente?commentaire=Informations complémentaires requises
     */
    @PostMapping("/{id}/en-attente")
    public ResponseEntity<ValidationDossier> remettreEnAttente(
            @PathVariable Long id,
            @RequestParam(required = false) String commentaire) {
        try {
            ValidationDossier validation = validationDossierService.remettreEnAttente(id, commentaire);
            return ResponseEntity.ok(validation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS DE STATISTIQUES ====================

    /**
     * Compte les validations par statut
     * 
     * @param statut Le statut des validations
     * @return ResponseEntity avec le nombre de validations avec ce statut (200 OK)
     * 
     * @example
     * GET /api/validation/dossiers/statistiques/statut/EN_ATTENTE
     */
    @GetMapping("/statistiques/statut/{statut}")
    public ResponseEntity<Long> countValidationsByStatut(@PathVariable StatutValidation statut) {
        try {
            long count = validationDossierService.countValidationsByStatut(statut);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compte les validations par agent créateur
     * 
     * @param agentId L'ID de l'agent créateur
     * @return ResponseEntity avec le nombre de validations de l'agent (200 OK)
     * 
     * @example
     * GET /api/validation/dossiers/statistiques/agent/1
     */
    @GetMapping("/statistiques/agent/{agentId}")
    public ResponseEntity<Long> countValidationsByAgent(@PathVariable Long agentId) {
        try {
            long count = validationDossierService.countValidationsByAgent(agentId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compte les validations par chef validateur
     * 
     * @param chefId L'ID du chef validateur
     * @return ResponseEntity avec le nombre de validations du chef (200 OK)
     * 
     * @example
     * GET /api/validation/dossiers/statistiques/chef/1
     */
    @GetMapping("/statistiques/chef/{chefId}")
    public ResponseEntity<Long> countValidationsByChef(@PathVariable Long chefId) {
        try {
            long count = validationDossierService.countValidationsByChef(chefId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
