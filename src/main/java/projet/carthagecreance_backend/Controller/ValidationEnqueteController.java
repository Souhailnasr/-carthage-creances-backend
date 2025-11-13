package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.ValidationEnquete;
import projet.carthagecreance_backend.Entity.StatutValidation;
import projet.carthagecreance_backend.Service.ValidationEnqueteService;

import java.util.List;

/**
 * Contrôleur REST complet pour la gestion des validations d'enquêtes
 * 
 * Ce contrôleur expose toutes les opérations CRUD et les fonctionnalités métier
 * pour la gestion des validations d'enquêtes dans le système de gestion de créances.
 * 
 * @author Système de Gestion de Créances
 * @version 1.0
 */
@RestController
@RequestMapping("/api/validation/enquetes")
@CrossOrigin(origins = "http://localhost:4200")
public class ValidationEnqueteController {

    @Autowired
    private ValidationEnqueteService validationEnqueteService;

    // ==================== ENDPOINTS DE BASE (CRUD) ====================

    /**
     * Crée une nouvelle validation d'enquête
     * 
     * @param validation La validation à créer (enquête, agent créateur, commentaires)
     * @return ResponseEntity avec la validation créée (201 CREATED) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/validation/enquetes
     * {
     *   "enquete": {"id": 1},
     *   "agentCreateur": {"id": 2},
     *   "commentaires": "Demande de validation pour l'enquête ENQ-2024-001"
     * }
     */
    @PostMapping
    public ResponseEntity<ValidationEnquete> createValidationEnquete(@RequestBody ValidationEnquete validation) {
        try {
            ValidationEnquete createdValidation = validationEnqueteService.createValidationEnquete(validation);
            return ResponseEntity.ok(createdValidation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Met à jour une validation d'enquête existante
     * 
     * @param id L'ID de la validation à modifier
     * @param validation Les nouvelles données de la validation
     * @return ResponseEntity avec la validation mise à jour (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * PUT /api/validation/enquetes/1
     * {
     *   "commentaires": "Mise à jour des commentaires de validation"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ValidationEnquete> updateValidationEnquete(@PathVariable Long id, @RequestBody ValidationEnquete validation) {
        try {
            ValidationEnquete updatedValidation = validationEnqueteService.updateValidationEnquete(id, validation);
            return ResponseEntity.ok(updatedValidation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprime une validation d'enquête
     * 
     * @param id L'ID de la validation à supprimer
     * @return ResponseEntity vide (204 NO_CONTENT) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * DELETE /api/validation/enquetes/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteValidationEnquete(@PathVariable Long id) {
        try {
            validationEnqueteService.deleteValidationEnquete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère une validation d'enquête par son ID
     * 
     * @param id L'ID de la validation
     * @return ResponseEntity avec la validation trouvée (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * GET /api/validation/enquetes/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<ValidationEnquete> getValidationEnqueteById(@PathVariable Long id) {
        try {
            ValidationEnquete validation = validationEnqueteService.getValidationEnqueteById(id);
            return ResponseEntity.ok(validation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère toutes les validations d'enquêtes
     * 
     * @return ResponseEntity avec la liste de toutes les validations (200 OK)
     * 
     * @example
     * GET /api/validation/enquetes
     */
    @GetMapping
    public ResponseEntity<List<ValidationEnquete>> getAllValidationsEnquete() {
        try {
            List<ValidationEnquete> validations = validationEnqueteService.getAllValidationsEnquete();
            return ResponseEntity.ok(validations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS DE FILTRAGE ====================

    /**
     * Récupère les enquêtes en attente de validation
     * 
     * @return ResponseEntity avec la liste des validations en attente (200 OK)
     * 
     * @example
     * GET /api/validation/enquetes/en-attente
     */
    @GetMapping("/en-attente")
    public ResponseEntity<List<ValidationEnquete>> getEnquetesEnAttente() {
        try {
            List<ValidationEnquete> validations = validationEnqueteService.getEnquetesEnAttente();
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
     * GET /api/validation/enquetes/agent/1
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ValidationEnquete>> getValidationsByAgent(@PathVariable Long agentId) {
        try {
            List<ValidationEnquete> validations = validationEnqueteService.getValidationsByAgent(agentId);
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
     * GET /api/validation/enquetes/chef/1
     */
    @GetMapping("/chef/{chefId}")
    public ResponseEntity<List<ValidationEnquete>> getValidationsByChef(@PathVariable Long chefId) {
        try {
            List<ValidationEnquete> validations = validationEnqueteService.getValidationsByChef(chefId);
            return ResponseEntity.ok(validations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les validations par enquête
     * 
     * @param enqueteId L'ID de l'enquête
     * @return ResponseEntity avec la liste des validations de l'enquête (200 OK)
     * 
     * @example
     * GET /api/validation/enquetes/enquete/1
     */
    @GetMapping("/enquete/{enqueteId}")
    public ResponseEntity<List<ValidationEnquete>> getValidationsByEnquete(@PathVariable Long enqueteId) {
        try {
            List<ValidationEnquete> validations = validationEnqueteService.getValidationsByEnquete(enqueteId);
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
     * GET /api/validation/enquetes/statut/EN_ATTENTE
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<ValidationEnquete>> getValidationsByStatut(@PathVariable StatutValidation statut) {
        try {
            List<ValidationEnquete> validations = validationEnqueteService.getValidationsByStatut(statut);
            return ResponseEntity.ok(validations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS D'ACTIONS ====================

    /**
     * Valide une enquête
     * 
     * @param id L'ID de la validation à valider
     * @param chefId L'ID du chef qui valide
     * @param commentaire Le commentaire de validation (optionnel)
     * @return ResponseEntity avec la validation mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/validation/enquetes/1/valider?chefId=2&commentaire=Enquête validée avec succès
     */
    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validerEnquete(
            @PathVariable Long id,
            @RequestParam Long chefId,
            @RequestParam(required = false) String commentaire) {
        try {
            // Récupérer la validation par son ID pour obtenir l'ID de l'enquête
            ValidationEnquete validation;
            try {
                validation = validationEnqueteService.getValidationEnqueteById(id);
            } catch (RuntimeException e) {
                // La validation n'existe pas
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Erreur : " + e.getMessage());
            }
            
            // Vérifier que la validation est en attente
            if (validation.getStatut() != StatutValidation.EN_ATTENTE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Erreur : Cette validation n'est pas en attente. Statut actuel : " + validation.getStatut());
            }
            
            // Extraire l'ID de l'enquête depuis la validation
            if (validation.getEnquete() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Erreur : Aucune enquête associée à cette validation");
            }
            
            Long enqueteId = validation.getEnquete().getId();
            if (enqueteId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Erreur : L'enquête associée à cette validation n'a pas d'ID");
            }
            
            // Valider l'enquête en utilisant l'ID de l'enquête
            ValidationEnquete validationMiseAJour = validationEnqueteService.validerEnquete(enqueteId, chefId, commentaire);
            return ResponseEntity.ok(validationMiseAJour);
        } catch (RuntimeException e) {
            // Logger l'erreur pour le débogage
            System.err.println("Erreur lors de la validation de l'enquête (validation ID: " + id + ") par le chef " + chefId + ": " + e.getMessage());
            e.printStackTrace();
            
            // Retourner un message d'erreur détaillé au frontend
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Erreur lors de la validation de l'enquête";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur : " + errorMessage);
        } catch (Exception e) {
            // Logger l'erreur pour le débogage
            System.err.println("Erreur inattendue lors de la validation de l'enquête (validation ID: " + id + "): " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur lors de la validation de l'enquête : " + e.getMessage());
        }
    }

    /**
     * Rejette une enquête
     * 
     * @param id L'ID de la validation à rejeter
     * @param chefId L'ID du chef qui rejette
     * @param commentaire Le commentaire de rejet (optionnel)
     * @return ResponseEntity avec la validation mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/validation/enquetes/1/rejeter?chefId=2&commentaire=Informations manquantes
     */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterEnquete(
            @PathVariable Long id,
            @RequestParam Long chefId,
            @RequestParam(required = false) String commentaire) {
        try {
            // Récupérer la validation par son ID pour obtenir l'ID de l'enquête
            ValidationEnquete validation;
            try {
                validation = validationEnqueteService.getValidationEnqueteById(id);
            } catch (RuntimeException e) {
                // La validation n'existe pas
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Erreur : " + e.getMessage());
            }
            
            // Vérifier que la validation est en attente
            if (validation.getStatut() != StatutValidation.EN_ATTENTE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Erreur : Cette validation n'est pas en attente. Statut actuel : " + validation.getStatut());
            }
            
            // Extraire l'ID de l'enquête depuis la validation
            if (validation.getEnquete() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Erreur : Aucune enquête associée à cette validation");
            }
            
            Long enqueteId = validation.getEnquete().getId();
            if (enqueteId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Erreur : L'enquête associée à cette validation n'a pas d'ID");
            }
            
            // Rejeter l'enquête en utilisant l'ID de l'enquête
            ValidationEnquete validationMiseAJour = validationEnqueteService.rejeterEnquete(enqueteId, chefId, commentaire);
            return ResponseEntity.ok(validationMiseAJour);
        } catch (RuntimeException e) {
            // Logger l'erreur pour le débogage
            System.err.println("Erreur lors du rejet de l'enquête (validation ID: " + id + ") par le chef " + chefId + ": " + e.getMessage());
            e.printStackTrace();
            
            // Retourner un message d'erreur détaillé au frontend
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Erreur lors du rejet de l'enquête";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur : " + errorMessage);
        } catch (Exception e) {
            // Logger l'erreur pour le débogage
            System.err.println("Erreur inattendue lors du rejet de l'enquête (validation ID: " + id + "): " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur lors du rejet de l'enquête : " + e.getMessage());
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
     * POST /api/validation/enquetes/1/en-attente?commentaire=Informations complémentaires requises
     */
    @PostMapping("/{id}/en-attente")
    public ResponseEntity<ValidationEnquete> remettreEnAttente(
            @PathVariable Long id,
            @RequestParam(required = false) String commentaire) {
        try {
            ValidationEnquete validation = validationEnqueteService.remettreEnAttente(id, commentaire);
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
     * GET /api/validation/enquetes/statistiques/statut/EN_ATTENTE
     */
    @GetMapping("/statistiques/statut/{statut}")
    public ResponseEntity<Long> countValidationsByStatut(@PathVariable StatutValidation statut) {
        try {
            long count = validationEnqueteService.countValidationsByStatut(statut);
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
     * GET /api/validation/enquetes/statistiques/agent/1
     */
    @GetMapping("/statistiques/agent/{agentId}")
    public ResponseEntity<Long> countValidationsByAgent(@PathVariable Long agentId) {
        try {
            long count = validationEnqueteService.countValidationsByAgent(agentId);
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
     * GET /api/validation/enquetes/statistiques/chef/1
     */
    @GetMapping("/statistiques/chef/{chefId}")
    public ResponseEntity<Long> countValidationsByChef(@PathVariable Long chefId) {
        try {
            long count = validationEnqueteService.countValidationsByChef(chefId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Nettoie les validations orphelines (dont l'enquête n'existe plus)
     * 
     * @return ResponseEntity avec le nombre de validations supprimées (200 OK)
     * 
     * @example
     * POST /api/validation/enquetes/nettoyer-orphelines
     */
    @PostMapping("/nettoyer-orphelines")
    public ResponseEntity<Integer> nettoyerValidationsOrphelines() {
        try {
            int count = validationEnqueteService.nettoyerValidationsOrphelines();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
