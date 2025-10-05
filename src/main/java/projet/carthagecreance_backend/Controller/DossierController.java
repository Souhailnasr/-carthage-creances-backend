// Fichier : src/main/java/projet/carthagecreance_backend/Controller/DossierController.java
package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Urgence;
import projet.carthagecreance_backend.Service.DossierService;
import projet.carthagecreance_backend.DTO.DossierRequest; // Ajout de l'import DTO
import projet.carthagecreance_backend.Service.FileStorageService;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contrôleur REST complet pour la gestion des dossiers avec workflow
 * 
 * Ce contrôleur expose toutes les opérations CRUD et les fonctionnalités de workflow
 * pour la gestion des dossiers dans le système de gestion de créances.
 * 
 * @author Système de Gestion de Créances
 * @version 1.0
 */
@RestController
@RequestMapping("/api/dossiers")
@CrossOrigin(origins = "http://localhost:4200")
public class DossierController {

    private static final Logger logger = LoggerFactory.getLogger(DossierController.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    @Autowired
    private DossierService dossierService;
    
    @Autowired
    private FileStorageService fileStorageService;

    // ==================== MÉTHODES DE VALIDATION ====================
    
    /**
     * Valide un fichier PDF
     * @param file Le fichier à valider
     * @param fieldName Le nom du champ pour les messages d'erreur
     * @throws IllegalArgumentException Si la validation échoue
     */
    private void validatePdfFile(MultipartFile file, String fieldName) {
        if (file == null || file.isEmpty()) {
            return; // Fichier optionnel
        }
        
        // 1. Vérifier que le fichier est un PDF (content-type application/pdf)
        String contentType = file.getContentType();
        if (contentType == null || !PDF_CONTENT_TYPE.equals(contentType)) {
            logger.warn("Validation échouée pour {}: type MIME invalide {}", fieldName, contentType);
            throw new IllegalArgumentException("Le fichier " + fieldName + " doit être un PDF (type: " + contentType + ")");
        }
        
        // 2. Vérifier que la taille ne dépasse pas 10MB
        if (file.getSize() > MAX_FILE_SIZE) {
            logger.warn("Validation échouée pour {}: taille {} MB dépasse la limite de 10MB", 
                       fieldName, file.getSize() / (1024 * 1024));
            throw new IllegalArgumentException("Le fichier " + fieldName + " ne peut pas dépasser 10MB (taille actuelle: " + 
                                             (file.getSize() / (1024 * 1024)) + "MB)");
        }
        
        // 3. Vérifier que le nom du fichier est valide
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            logger.warn("Validation échouée pour {}: nom de fichier manquant", fieldName);
            throw new IllegalArgumentException("Le nom du fichier " + fieldName + " ne peut pas être vide");
        }
        
        // Vérifier l'extension
        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            logger.warn("Validation échouée pour {}: extension invalide {}", fieldName, originalFilename);
            throw new IllegalArgumentException("Le fichier " + fieldName + " doit avoir l'extension .pdf");
        }
        
        // Vérifier les caractères interdits dans le nom
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            logger.warn("Validation échouée pour {}: nom de fichier contient des caractères interdits {}", fieldName, originalFilename);
            throw new IllegalArgumentException("Le nom du fichier " + fieldName + " contient des caractères interdits");
        }
        
        logger.debug("Validation réussie pour {}: {} ({} bytes)", fieldName, originalFilename, file.getSize());
    }

    // ==================== ENDPOINTS DE BASE (CRUD) ====================

    /**
     * Crée un nouveau dossier avec workflow de validation
     * 
     * @param request Les données du dossier à créer
     * @param contratSigne Fichier de contrat signé (optionnel)
     * @param pouvoir Fichier de pouvoir (optionnel)
     * @return ResponseEntity avec le dossier créé (201 CREATED) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/dossiers/addDossier
     * Content-Type: multipart/form-data
     * dossier: {"titre": "Dossier test", "agentCreateurId": 1, ...}
     */
    @PostMapping(value = "/addDossierWithFiles", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createDossier(
            @RequestPart("dossier") DossierRequest request,
            @RequestPart(value = "contratSigne", required = false) MultipartFile contratSigne,
            @RequestPart(value = "pouvoir", required = false) MultipartFile pouvoir) {

        try {
            // Validation des fichiers avant traitement
            validatePdfFile(contratSigne, "contratSigne");
            validatePdfFile(pouvoir, "pouvoir");
            
            // Handle file saving if provided
            if (contratSigne != null && !contratSigne.isEmpty()) {
                request.setContratSigneFile(contratSigne);
            }
            if (pouvoir != null && !pouvoir.isEmpty()) {
                request.setPouvoirFile(pouvoir);
            }

            Dossier createdDossier = dossierService.createDossier(request);
            logger.info("Dossier créé avec succès: ID={}, Titre={}", createdDossier.getId(), createdDossier.getTitre());
            return new ResponseEntity<>(createdDossier, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation lors de la création du dossier: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur de validation",
                "message", e.getMessage(),
                "timestamp", new Date().toString()
            ));
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la création du dossier: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de la création du dossier: " + e.getMessage(),
                        "timestamp", new Date().toString()
                    ));
        }
    }

    /**
     * Crée un nouveau dossier avec workflow (version simplifiée)
     * 
     * @param request Les données du dossier à créer
     * @return ResponseEntity avec le dossier créé (201 CREATED) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/dossiers
     * {
     *   "titre": "Dossier test",
     *   "agentCreateurId": 1,
     *   "nomCreancier": "Entreprise ABC",
     *   "nomDebiteur": "Client XYZ"
     * }
     */
    @PostMapping
    public ResponseEntity<?> createDossierSimple(@RequestBody DossierRequest request) {
        try {
            Dossier createdDossier = dossierService.createDossier(request);
            return new ResponseEntity<>(createdDossier, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création du dossier: " + e.getMessage());
        }
    }

    /**
     * Crée un nouveau dossier avec fichiers uploadés
     * 
     * @param dossierJson JSON du dossier à créer
     * @param pouvoirFile Fichier pouvoir (optionnel)
     * @param contratSigneFile Fichier contrat signé (optionnel)
     * @return ResponseEntity avec le dossier créé (201 CREATED) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/dossiers/addDossier
     * Content-Type: multipart/form-data
     * dossier: {"titre": "Dossier test", "agentCreateurId": 1, ...}
     * pouvoir: [PDF file]
     * contratSigne: [PDF file]
     */


    /**
     * Récupère un dossier par son ID
     * 
     * @param id L'ID du dossier
     * @return ResponseEntity avec le dossier trouvé (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * GET /api/dossiers/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Dossier> getDossierById(@PathVariable Long id) {
        Optional<Dossier> dossier = dossierService.getDossierById(id);
        return dossier.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Récupère tous les dossiers
     * 
     * @return ResponseEntity avec la liste de tous les dossiers (200 OK)
     * 
     * @example
     * GET /api/dossiers
     */
    @GetMapping
    public ResponseEntity<List<Dossier>> getAllDossiers() {
        List<Dossier> dossiers = dossierService.getAllDossiers();
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    /**
     * Met à jour un dossier avec validation
     * 
     * @param id L'ID du dossier à modifier
     * @param dossierDetails Les nouvelles données du dossier
     * @return ResponseEntity avec le dossier mis à jour (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * PUT /api/dossiers/1
     * {
     *   "titre": "Titre modifié",
     *   "description": "Description modifiée"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<Dossier> updateDossier(@PathVariable Long id, @RequestBody Dossier dossierDetails) {
        try {
            Dossier updatedDossier = dossierService.updateDossier(id, dossierDetails);
            return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Supprime un dossier avec vérifications
     * 
     * @param id L'ID du dossier à supprimer
     * @return ResponseEntity vide (204 NO_CONTENT) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * DELETE /api/dossiers/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDossier(@PathVariable Long id) {
        try {
            dossierService.deleteDossier(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Search Operations (restent inchangées)
    @GetMapping("/number/{numeroDossier}")
    public ResponseEntity<Dossier> getDossierByNumber(@PathVariable String numeroDossier) {
        Optional<Dossier> dossier = dossierService.getDossierByNumber(numeroDossier);
        return dossier.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<Dossier>> getDossiersByTitle(@RequestParam String title) {
        List<Dossier> dossiers = dossierService.getDossiersByTitle(title);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/search/description")
    public ResponseEntity<List<Dossier>> getDossiersByDescription(@RequestParam String description) {
        List<Dossier> dossiers = dossierService.getDossiersByDescription(description);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/urgency/{urgency}")
    public ResponseEntity<List<Dossier>> getDossiersByUrgency(@PathVariable Urgence urgency) {
        List<Dossier> dossiers = dossierService.getDossiersByUrgency(urgency);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/avocat/{avocatId}")
    public ResponseEntity<List<Dossier>> getDossiersByAvocat(@PathVariable Long avocatId) {
        List<Dossier> dossiers = dossierService.getDossiersByAvocat(avocatId);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/huissier/{huissierId}")
    public ResponseEntity<List<Dossier>> getDossiersByHuissier(@PathVariable Long huissierId) {
        List<Dossier> dossiers = dossierService.getDossiersByHuissier(huissierId);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/creancier/{creancierId}")
    public ResponseEntity<List<Dossier>> getDossiersByCreancier(@PathVariable Long creancierId) {
        List<Dossier> dossiers = dossierService.getDossiersByCreancier(creancierId);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/debiteur/{debiteurId}")
    public ResponseEntity<List<Dossier>> getDossiersByDebiteur(@PathVariable Long debiteurId) {
        List<Dossier> dossiers = dossierService.getDossiersByDebiteur(debiteurId);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Dossier>> getDossiersByUser(@PathVariable Long userId) {
        List<Dossier> dossiers = dossierService.getDossiersByUser(userId);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/creation-date/{date}")
    public ResponseEntity<List<Dossier>> getDossiersByCreationDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        List<Dossier> dossiers = dossierService.getDossiersByCreationDate(date);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/creation-date-range")
    public ResponseEntity<List<Dossier>> getDossiersByCreationDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<Dossier> dossiers = dossierService.getDossiersByCreationDateRange(startDate, endDate);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/closure-date/{date}")
    public ResponseEntity<List<Dossier>> getDossiersByClosureDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        List<Dossier> dossiers = dossierService.getDossiersByClosureDate(date);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/amount/{amount}")
    public ResponseEntity<List<Dossier>> getDossiersByAmount(@PathVariable Double amount) {
        List<Dossier> dossiers = dossierService.getDossiersByAmount(amount);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/amount-range")
    public ResponseEntity<List<Dossier>> getDossiersByAmountRange(
            @RequestParam Double minAmount,
            @RequestParam Double maxAmount) {
        List<Dossier> dossiers = dossierService.getDossiersByAmountRange(minAmount, maxAmount);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Dossier>> searchDossiers(@RequestParam String searchTerm) {
        List<Dossier> dossiers = dossierService.searchDossiers(searchTerm);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    // Special Operations
    @GetMapping("/open")
    public ResponseEntity<List<Dossier>> getOpenDossiers() {
        List<Dossier> dossiers = dossierService.getOpenDossiers();
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/closed")
    public ResponseEntity<List<Dossier>> getClosedDossiers() {
        List<Dossier> dossiers = dossierService.getClosedDossiers();
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Dossier>> getRecentDossiers() {
        List<Dossier> dossiers = dossierService.getRecentDossiers();
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    // Validation Operations
    @GetMapping("/exists/number/{numeroDossier}")
    public ResponseEntity<Boolean> existsByNumber(@PathVariable String numeroDossier) {
        boolean exists = dossierService.existsByNumber(numeroDossier);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    // ==================== NOUVEAUX ENDPOINTS DE WORKFLOW ====================

    /**
     * Récupère les dossiers en attente de validation
     * 
     * @return ResponseEntity avec la liste des dossiers en attente (200 OK)
     * 
     * @example
     * GET /api/dossiers/en-attente
     */
    @GetMapping("/en-attente")
    public ResponseEntity<List<Dossier>> getDossiersEnAttente() {
        try {
            List<Dossier> dossiers = dossierService.getDossiersEnAttente();
            return new ResponseEntity<>(dossiers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les dossiers assignés à un agent
     * 
     * @param agentId L'ID de l'agent
     * @return ResponseEntity avec la liste des dossiers de l'agent (200 OK)
     * 
     * @example
     * GET /api/dossiers/agent/1
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<Dossier>> getDossiersByAgent(@PathVariable Long agentId) {
        try {
            List<Dossier> dossiers = dossierService.getDossiersByAgent(agentId);
            return new ResponseEntity<>(dossiers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les dossiers créés par un agent
     * 
     * @param agentId L'ID de l'agent créateur
     * @return ResponseEntity avec la liste des dossiers créés par l'agent (200 OK)
     * 
     * @example
     * GET /api/dossiers/agent/1/crees
     */
    @GetMapping("/agent/{agentId}/crees")
    public ResponseEntity<List<Dossier>> getDossiersCreesByAgent(@PathVariable Long agentId) {
        try {
            List<Dossier> dossiers = dossierService.getDossiersCreesByAgent(agentId);
            return new ResponseEntity<>(dossiers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Valide un dossier
     * 
     * @param id L'ID du dossier à valider
     * @param chefId L'ID du chef qui valide
     * @return ResponseEntity avec le dossier validé (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/dossiers/1/valider?chefId=5
     */
    @PostMapping("/{id}/valider")
    public ResponseEntity<Dossier> validerDossier(@PathVariable Long id, @RequestParam Long chefId) {
        try {
            Dossier dossier = dossierService.validerDossier(id, chefId);
            return new ResponseEntity<>(dossier, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Rejette un dossier
     * 
     * @param id L'ID du dossier à rejeter
     * @param commentaire Le commentaire de rejet
     * @return ResponseEntity avec le dossier rejeté (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/dossiers/1/rejeter?commentaire=Informations manquantes
     */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<Dossier> rejeterDossier(@PathVariable Long id, @RequestParam String commentaire) {
        try {
            Dossier dossier = dossierService.rejeterDossier(id, commentaire);
            return new ResponseEntity<>(dossier, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ENDPOINTS D'UPLOAD DE FICHIERS ====================

    /**
     * Upload un fichier contrat signé pour un dossier
     * 
     * @param dossierId L'ID du dossier
     * @param file Le fichier PDF à uploader
     * @return ResponseEntity avec le chemin du fichier sauvegardé (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/dossiers/1/upload/contrat
     * Content-Type: multipart/form-data
     * file: [PDF file]
     */
    @PostMapping("/{dossierId}/upload/contrat")
    public ResponseEntity<?> uploadContratSigne(@PathVariable Long dossierId, 
                                               @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Le fichier ne peut pas être vide");
            }
            
            if (!fileStorageService.isValidPdfFile(file)) {
                return ResponseEntity.badRequest().body("Seuls les fichiers PDF sont autorisés");
            }
            
            String filePath = fileStorageService.saveFile(file, "contrat");
            
            // Mettre à jour le dossier avec le chemin du fichier
            Optional<Dossier> dossierOpt = dossierService.getDossierById(dossierId);
            if (dossierOpt.isPresent()) {
                Dossier dossier = dossierOpt.get();
                dossier.setContratSigneFilePath(filePath);
                dossierService.updateDossier(dossierId, dossier);
                
                return ResponseEntity.ok(Map.of(
                    "message", "Fichier contrat signé uploadé avec succès",
                    "filePath", filePath
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'upload: " + e.getMessage());
        }
    }

    /**
     * Upload un fichier pouvoir pour un dossier
     * 
     * @param dossierId L'ID du dossier
     * @param file Le fichier PDF à uploader
     * @return ResponseEntity avec le chemin du fichier sauvegardé (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/dossiers/1/upload/pouvoir
     * Content-Type: multipart/form-data
     * file: [PDF file]
     */
    @PostMapping("/{dossierId}/upload/pouvoir")
    public ResponseEntity<?> uploadPouvoir(@PathVariable Long dossierId, 
                                          @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Le fichier ne peut pas être vide");
            }
            
            if (!fileStorageService.isValidPdfFile(file)) {
                return ResponseEntity.badRequest().body("Seuls les fichiers PDF sont autorisés");
            }
            
            String filePath = fileStorageService.saveFile(file, "pouvoir");
            
            // Mettre à jour le dossier avec le chemin du fichier
            Optional<Dossier> dossierOpt = dossierService.getDossierById(dossierId);
            if (dossierOpt.isPresent()) {
                Dossier dossier = dossierOpt.get();
                dossier.setPouvoirFilePath(filePath);
                dossierService.updateDossier(dossierId, dossier);
                
                return ResponseEntity.ok(Map.of(
                    "message", "Fichier pouvoir uploadé avec succès",
                    "filePath", filePath
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'upload: " + e.getMessage());
        }
    }

    /**
     * Supprime un fichier contrat signé d'un dossier
     * 
     * @param dossierId L'ID du dossier
     * @return ResponseEntity avec message de succès (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * DELETE /api/dossiers/1/upload/contrat
     */
    @DeleteMapping("/{dossierId}/upload/contrat")
    public ResponseEntity<?> deleteContratSigne(@PathVariable Long dossierId) {
        try {
            Optional<Dossier> dossierOpt = dossierService.getDossierById(dossierId);
            if (dossierOpt.isPresent()) {
                Dossier dossier = dossierOpt.get();
                String filePath = dossier.getContratSigneFilePath();
                
                if (filePath != null && !filePath.isEmpty()) {
                    fileStorageService.deleteFile(filePath);
                    dossier.setContratSigneFilePath(null);
                    dossierService.updateDossier(dossierId, dossier);
                }
                
                return ResponseEntity.ok(Map.of("message", "Fichier contrat signé supprimé avec succès"));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    /**
     * Supprime un fichier pouvoir d'un dossier
     * 
     * @param dossierId L'ID du dossier
     * @return ResponseEntity avec message de succès (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * DELETE /api/dossiers/1/upload/pouvoir
     */
    @DeleteMapping("/{dossierId}/upload/pouvoir")
    public ResponseEntity<?> deletePouvoir(@PathVariable Long dossierId) {
        try {
            Optional<Dossier> dossierOpt = dossierService.getDossierById(dossierId);
            if (dossierOpt.isPresent()) {
                Dossier dossier = dossierOpt.get();
                String filePath = dossier.getPouvoirFilePath();
                
                if (filePath != null && !filePath.isEmpty()) {
                    fileStorageService.deleteFile(filePath);
                    dossier.setPouvoirFilePath(null);
                    dossierService.updateDossier(dossierId, dossier);
                }
                
                return ResponseEntity.ok(Map.of("message", "Fichier pouvoir supprimé avec succès"));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    // ==================== ENDPOINTS DE STATISTIQUES ====================

    /**
     * Compte le total des dossiers
     * 
     * @return ResponseEntity avec le nombre total de dossiers (200 OK)
     * 
     * @example
     * GET /api/dossiers/statistiques/total
     */
    @GetMapping("/statistiques/total")
    public ResponseEntity<Long> countTotalDossiers() {
        try {
            long count = dossierService.countTotalDossiers();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Compte les dossiers en cours de traitement
     * 
     * @return ResponseEntity avec le nombre de dossiers en cours (200 OK)
     * 
     * @example
     * GET /api/dossiers/statistiques/en-cours
     */
    @GetMapping("/statistiques/en-cours")
    public ResponseEntity<Long> countDossiersEnCours() {
        try {
            long count = dossierService.countDossiersEnCours();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Compte les dossiers validés
     * 
     * @return ResponseEntity avec le nombre de dossiers validés (200 OK)
     * 
     * @example
     * GET /api/dossiers/statistiques/valides
     */
    @GetMapping("/statistiques/valides")
    public ResponseEntity<Long> countDossiersValides() {
        try {
            long count = dossierService.countDossiersValides();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Compte les dossiers créés ce mois
     * 
     * @return ResponseEntity avec le nombre de dossiers créés ce mois (200 OK)
     * 
     * @example
     * GET /api/dossiers/statistiques/ce-mois
     */
    @GetMapping("/statistiques/ce-mois")
    public ResponseEntity<Long> countDossiersCreesCeMois() {
        try {
            long count = dossierService.countDossiersCreesCeMois();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Compte les dossiers par agent
     * 
     * @param agentId L'ID de l'agent
     * @return ResponseEntity avec le nombre de dossiers de l'agent (200 OK)
     * 
     * @example
     * GET /api/dossiers/statistiques/agent/1
     */
    @GetMapping("/statistiques/agent/{agentId}")
    public ResponseEntity<Long> countDossiersByAgent(@PathVariable Long agentId) {
        try {
            long count = dossierService.countDossiersByAgent(agentId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Compte les dossiers créés par un agent
     * 
     * @param agentId L'ID de l'agent créateur
     * @return ResponseEntity avec le nombre de dossiers créés par l'agent (200 OK)
     * 
     * @example
     * GET /api/dossiers/statistiques/agent/1/crees
     */
    @GetMapping("/statistiques/agent/{agentId}/crees")
    public ResponseEntity<Long> countDossiersCreesByAgent(@PathVariable Long agentId) {
        try {
            long count = dossierService.countDossiersCreesByAgent(agentId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}