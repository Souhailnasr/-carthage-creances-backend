package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Enquette;
import projet.carthagecreance_backend.Service.EnquetteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/enquettes")
@CrossOrigin(origins = "*")
public class EnquetteController {

    @Autowired
    private EnquetteService enquetteService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<?> createEnquette(@RequestBody Enquette enquette) {
        try {
            Enquette createdEnquette = enquetteService.createEnquette(enquette);
            return new ResponseEntity<>(createdEnquette, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Retourner un message d'erreur détaillé pour le frontend
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("dossierId est obligatoire")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Erreur : Le dossierId est obligatoire pour créer une enquête");
                } else if (errorMessage.contains("Dossier") && errorMessage.contains("non trouvé")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Erreur : " + errorMessage);
                } else if (errorMessage.contains("déjà une enquête associée")) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Erreur : " + errorMessage);
                } else if (errorMessage.contains("Utilisateur") && errorMessage.contains("non trouvé")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Erreur : " + errorMessage);
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Erreur lors de la création de l'enquête : " + (errorMessage != null ? errorMessage : "Erreur inconnue"));
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de la création de l'enquête : " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur serveur lors de la création de l'enquête : " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Enquette> getEnquetteById(@PathVariable Long id) {
        Optional<Enquette> enquette = enquetteService.getEnquetteById(id);
        return enquette.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Enquette>> getAllEnquettes() {
        List<Enquette> enquettes = enquetteService.getAllEnquettes();
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Enquette> updateEnquette(@PathVariable Long id, @RequestBody Enquette enquette) {
        try {
            Enquette updatedEnquette = enquetteService.updateEnquette(id, enquette);
            return new ResponseEntity<>(updatedEnquette, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEnquette(@PathVariable Long id) {
        try {
            enquetteService.deleteEnquette(id);
            // Vérifier une dernière fois que l'enquête a bien été supprimée avec existsById()
            if (enquetteService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur : L'enquête n'a pas pu être supprimée. Contrainte de base de données.");
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (DataIntegrityViolationException e) {
            // Erreur de contrainte de base de données
            System.err.println("Contrainte de base de données empêche la suppression de l'enquête " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Impossible de supprimer l'enquête : une contrainte de base de données empêche la suppression. " +
                      "L'enquête est probablement liée à un dossier ou à d'autres entités.");
        } catch (RuntimeException e) {
            // Logger l'erreur pour le débogage
            System.err.println("Erreur lors de la suppression de l'enquête " + id + ": " + e.getMessage());
            e.printStackTrace();
            
            // Retourner un message d'erreur détaillé
            if (e.getMessage() != null && e.getMessage().contains("contrainte")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Impossible de supprimer l'enquête : " + e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Enquête non trouvée ou erreur lors de la suppression : " + e.getMessage());
        } catch (Exception e) {
            // Logger l'erreur pour le débogage
            System.err.println("Erreur inattendue lors de la suppression de l'enquête " + id + ": " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur serveur lors de la suppression de l'enquête : " + e.getMessage());
        }
    }

    // Search Operations
    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<Enquette> getEnquetteByDossier(@PathVariable Long dossierId) {
        Optional<Enquette> enquette = enquetteService.getEnquetteByDossier(dossierId);
        return enquette.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/creation-date/{date}")
    public ResponseEntity<List<Enquette>> getEnquettesByCreationDate(@PathVariable LocalDate date) {
        List<Enquette> enquettes = enquetteService.getEnquettesByCreationDate(date);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/creation-date-range")
    public ResponseEntity<List<Enquette>> getEnquettesByCreationDateRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        List<Enquette> enquettes = enquetteService.getEnquettesByCreationDateRange(startDate, endDate);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/sector/{sector}")
    public ResponseEntity<List<Enquette>> getEnquettesBySector(@PathVariable String sector) {
        List<Enquette> enquettes = enquetteService.getEnquettesBySector(sector);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/legal-form/{legalForm}")
    public ResponseEntity<List<Enquette>> getEnquettesByLegalForm(@PathVariable String legalForm) {
        List<Enquette> enquettes = enquetteService.getEnquettesByLegalForm(legalForm);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/pdg/{pdg}")
    public ResponseEntity<List<Enquette>> getEnquettesByPDG(@PathVariable String pdg) {
        List<Enquette> enquettes = enquetteService.getEnquettesByPDG(pdg);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/capital-range")
    public ResponseEntity<List<Enquette>> getEnquettesByCapitalRange(
            @RequestParam Double minCapital, 
            @RequestParam Double maxCapital) {
        List<Enquette> enquettes = enquetteService.getEnquettesByCapitalRange(minCapital, maxCapital);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/revenue-range")
    public ResponseEntity<List<Enquette>> getEnquettesByRevenueRange(
            @RequestParam Double minRevenue, 
            @RequestParam Double maxRevenue) {
        List<Enquette> enquettes = enquetteService.getEnquettesByRevenueRange(minRevenue, maxRevenue);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/staff-range")
    public ResponseEntity<List<Enquette>> getEnquettesByStaffRange(
            @RequestParam Integer minStaff, 
            @RequestParam Integer maxStaff) {
        List<Enquette> enquettes = enquetteService.getEnquettesByStaffRange(minStaff, maxStaff);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    // Special Operations
    @GetMapping("/with-real-estate")
    public ResponseEntity<List<Enquette>> getEnquettesWithRealEstate() {
        List<Enquette> enquettes = enquetteService.getEnquettesWithRealEstate();
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/with-movable-property")
    public ResponseEntity<List<Enquette>> getEnquettesWithMovableProperty() {
        List<Enquette> enquettes = enquetteService.getEnquettesWithMovableProperty();
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/with-observations")
    public ResponseEntity<List<Enquette>> getEnquettesWithObservations() {
        List<Enquette> enquettes = enquetteService.getEnquettesWithObservations();
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    // Validation Operations
    /**
     * Valide une enquête
     * 
     * @param id L'ID de l'enquête à valider
     * @param chefId L'ID du chef qui valide
     * @return ResponseEntity avec l'enquête validée (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * PUT /api/enquettes/1/valider?chefId=5
     */
    @PutMapping("/{id}/valider")
    public ResponseEntity<Enquette> validerEnquette(@PathVariable Long id, @RequestParam Long chefId) {
        try {
            // Vérifier que l'enquête existe avant de valider
            if (!enquetteService.existsById(id)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            enquetteService.validerEnquette(id, chefId);
            
            // Essayer de récupérer l'enquête mise à jour, mais gérer le cas où elle ne peut pas être chargée
            Optional<Enquette> updated = enquetteService.getEnquetteById(id);
            return updated.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> {
                        // Si l'enquête existe mais ne peut pas être chargée (dossier_id NULL), retourner OK quand même
                        // car la validation a réussi
                        return new ResponseEntity<>(HttpStatus.OK);
                    });
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Rejette une enquête
     * 
     * @param id L'ID de l'enquête à rejeter
     * @param commentaire Le commentaire de rejet
     * @return ResponseEntity avec l'enquête rejetée (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * PUT /api/enquettes/1/rejeter?commentaire=Informations manquantes
     */
    @PutMapping("/{id}/rejeter")
    public ResponseEntity<Enquette> rejeterEnquette(@PathVariable Long id, @RequestParam String commentaire) {
        try {
            // Vérifier que l'enquête existe avant de rejeter
            if (!enquetteService.existsById(id)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            enquetteService.rejeterEnquette(id, commentaire);
            
            // Essayer de récupérer l'enquête mise à jour, mais gérer le cas où elle ne peut pas être chargée
            Optional<Enquette> updated = enquetteService.getEnquetteById(id);
            return updated.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> {
                        // Si l'enquête existe mais ne peut pas être chargée (dossier_id NULL), retourner OK quand même
                        // car le rejet a réussi
                        return new ResponseEntity<>(HttpStatus.OK);
                    });
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ==================== ENDPOINTS DE STATISTIQUES ====================
    
    /**
     * Compte le total des enquêtes
     * 
     * @return ResponseEntity avec le nombre total d'enquêtes (200 OK)
     * 
     * @example
     * GET /api/enquettes/statistiques/total
     */
    @GetMapping("/statistiques/total")
    public ResponseEntity<Long> countTotalEnquettes() {
        try {
            long count = enquetteService.countTotalEnquettes();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Compte les enquêtes par statut
     * 
     * @param statut Le statut des enquêtes (EN_ATTENTE_VALIDATION, VALIDE, REJETE)
     * @return ResponseEntity avec le nombre d'enquêtes avec ce statut (200 OK)
     * 
     * @example
     * GET /api/enquettes/statistiques/statut/VALIDE
     */
    @GetMapping("/statistiques/statut/{statut}")
    public ResponseEntity<Long> countEnquettesByStatut(@PathVariable projet.carthagecreance_backend.Entity.Statut statut) {
        try {
            long count = enquetteService.countEnquettesByStatut(statut);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Compte les enquêtes validées
     * 
     * @return ResponseEntity avec le nombre d'enquêtes validées (200 OK)
     * 
     * @example
     * GET /api/enquettes/statistiques/valides
     */
    @GetMapping("/statistiques/valides")
    public ResponseEntity<Long> countEnquettesValides() {
        try {
            long count = enquetteService.countEnquettesValides();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Compte les enquêtes non validées
     * 
     * @return ResponseEntity avec le nombre d'enquêtes non validées (200 OK)
     * 
     * @example
     * GET /api/enquettes/statistiques/non-valides
     */
    @GetMapping("/statistiques/non-valides")
    public ResponseEntity<Long> countEnquettesNonValides() {
        try {
            long count = enquetteService.countEnquettesNonValides();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Compte les enquêtes créées ce mois
     * 
     * @return ResponseEntity avec le nombre d'enquêtes créées ce mois (200 OK)
     * 
     * @example
     * GET /api/enquettes/statistiques/ce-mois
     */
    @GetMapping("/statistiques/ce-mois")
    public ResponseEntity<Long> countEnquettesCreesCeMois() {
        try {
            long count = enquetteService.countEnquettesCreesCeMois();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Compte les enquêtes créées par un agent
     * 
     * @param agentId L'ID de l'agent créateur
     * @return ResponseEntity avec le nombre d'enquêtes créées par l'agent (200 OK)
     * 
     * @example
     * GET /api/enquettes/statistiques/agent/1/crees
     */
    @GetMapping("/statistiques/agent/{agentId}/crees")
    public ResponseEntity<Long> countEnquettesByAgentCreateur(@PathVariable Long agentId) {
        try {
            long count = enquetteService.countEnquettesByAgentCreateur(agentId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Compte les enquêtes dont un agent est responsable
     * 
     * @param agentId L'ID de l'agent responsable
     * @return ResponseEntity avec le nombre d'enquêtes de l'agent (200 OK)
     * 
     * @example
     * GET /api/enquettes/statistiques/agent/1/responsable
     */
    @GetMapping("/statistiques/agent/{agentId}/responsable")
    public ResponseEntity<Long> countEnquettesByAgentResponsable(@PathVariable Long agentId) {
        try {
            long count = enquetteService.countEnquettesByAgentResponsable(agentId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
