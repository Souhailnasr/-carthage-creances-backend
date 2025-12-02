// Fichier : src/main/java/projet/carthagecreance_backend/Controller/DossierController.java
package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Urgence;
import projet.carthagecreance_backend.Entity.StatutValidation;
import projet.carthagecreance_backend.Entity.Statut;
import io.jsonwebtoken.ExpiredJwtException;
import projet.carthagecreance_backend.Service.DossierService;
import projet.carthagecreance_backend.Service.DossierMontantService;
import projet.carthagecreance_backend.DTO.DossierRequest; // Ajout de l'import DTO
import projet.carthagecreance_backend.DTO.AffectationDossierDTO;
import projet.carthagecreance_backend.DTO.MontantDossierDTO;
import projet.carthagecreance_backend.DTO.ActionAmiableDTO;
import projet.carthagecreance_backend.Service.FileStorageService;
import projet.carthagecreance_backend.SecurityServices.UserExtractionService;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Entity.RoleUtilisateur;
import projet.carthagecreance_backend.Entity.TypeRecouvrement;
import projet.carthagecreance_backend.Entity.DossierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import projet.carthagecreance_backend.Repository.DossierRepository;

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
    private DossierRepository dossierRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private UserExtractionService userExtractionService;
    
    @Autowired
    private DossierMontantService dossierMontantService;
    
    @Autowired
    private projet.carthagecreance_backend.Service.IaPredictionService iaPredictionService;
    
    @Autowired
    private projet.carthagecreance_backend.Service.Impl.IaFeatureBuilderService iaFeatureBuilderService;
    
    @Autowired
    private projet.carthagecreance_backend.Repository.ActionRepository actionRepository;
    
    @Autowired
    private projet.carthagecreance_backend.Repository.ActionHuissierRepository actionHuissierRepository;
    
    @Autowired
    private projet.carthagecreance_backend.Repository.EnquetteRepository enquetteRepository;
    
    @Autowired
    private projet.carthagecreance_backend.Repository.AudienceRepository audienceRepository;

    // Méthode utilitaire pour déterminer si un rôle est chef
    private boolean isChef(RoleUtilisateur role) {
        return role == RoleUtilisateur.CHEF_DEPARTEMENT_DOSSIER || 
               role == RoleUtilisateur.SUPER_ADMIN;
    }


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
    // Nouvelle route: POST /api/dossiers/create (multipart) — compatible Angular
    @PostMapping(value = "/create/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createDossierMultipart(
            @PathVariable("id") Long userId,
            @RequestPart("dossier") DossierRequest request,
            @RequestPart(value = "contratSigne", required = false) MultipartFile contratSigne,
            @RequestPart(value = "pouvoir", required = false) MultipartFile pouvoir,
            @RequestParam(value = "isChef", required = false, defaultValue = "false") boolean isChef) {

        try {
            request.setAgentCreateurId(userId);

            // Validation PDF
            validatePdfFile(contratSigne, "contratSigne");
            validatePdfFile(pouvoir, "pouvoir");

            if (contratSigne != null && !contratSigne.isEmpty()) {
                request.setContratSigneFile(contratSigne);
            }
            if (pouvoir != null && !pouvoir.isEmpty()) {
                request.setPouvoirFile(pouvoir);
            }

            // Statut selon rôle
            request.setStatut(isChef ? Statut.VALIDE : Statut.EN_ATTENTE_VALIDATION);

            Dossier createdDossier = dossierService.createDossier(request);

            if (isChef) {
                try {
                    dossierService.validerDossier(createdDossier.getId(), userId);
                    createdDossier = dossierService.getDossierById(createdDossier.getId()).orElse(createdDossier);
                } catch (RuntimeException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                            "error", "Validation chef",
                            "message", e.getMessage(),
                            "timestamp", new Date().toString()
                    ));
                }
            }

            return new ResponseEntity<>(createdDossier, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur de validation",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Erreur interne du serveur",
                    "message", "Erreur lors de la création du dossier: " + e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }


    /**
     * Crée un nouveau dossier avec fichiers uploadés (multipart/form-data)
     * 
     * @param request Les données du dossier à créer (en JSON dans le FormData)
     * @param contratSigne Fichier contrat signé (optionnel)
     * @param pouvoir Fichier pouvoir (optionnel)
     * @param isChef Indique si l'utilisateur crée en tant que chef
     * @param authHeader Token JWT pour extraire l'utilisateur connecté
     * @return ResponseEntity avec le dossier créé (201 CREATED) ou erreur
     * 
     * @example
     * POST /api/dossiers/create?isChef=true
     * Content-Type: multipart/form-data
     * dossier: {"titre": "Dossier test", "nomCreancier": "Entreprise ABC", ...}
     * contratSigne: [PDF file]
     * pouvoir: [PDF file]
     */
    @PostMapping(path = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createDossierWithFiles(
            @RequestPart("dossier") DossierRequest request,
            @RequestPart(value = "contratSigne", required = false) MultipartFile contratSigne,
            @RequestPart(value = "pouvoir", required = false) MultipartFile pouvoir,
            @RequestParam(value = "isChef", required = false, defaultValue = "false") boolean isChef,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        
        try {
            // Extraire l'utilisateur connecté depuis le token (obligatoire)
            if (authHeader == null || authHeader.isBlank()) {
                logger.warn("/api/dossiers/create (multipart) - Token manquant");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Non autorisé",
                    "message", "Token d'authentification requis pour créer un dossier",
                    "code", "TOKEN_MISSING",
                    "timestamp", new Date().toString()
                ));
            }

            Utilisateur utilisateurConnecte;
            try {
                utilisateurConnecte = userExtractionService.extractUserFromToken(authHeader);
                if (utilisateurConnecte == null) {
                    logger.warn("/api/dossiers/create (multipart) - Utilisateur non trouvé");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "Token invalide",
                        "message", "Impossible d'extraire l'utilisateur depuis le token",
                        "code", "USER_NOT_FOUND",
                        "timestamp", new Date().toString()
                    ));
                }
            } catch (ExpiredJwtException e) {
                logger.error("/api/dossiers/create (multipart) - Token JWT expiré: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Token expiré",
                    "message", "Votre session a expiré. Veuillez vous reconnecter pour créer un dossier.",
                    "code", "TOKEN_EXPIRED",
                    "expiredAt", e.getClaims().getExpiration().toString(),
                    "currentTime", new Date().toString(),
                    "timestamp", new Date().toString()
                ));
            }

            // TOUJOURS définir agentCreateurId à partir de l'utilisateur connecté
            request.setAgentCreateurId(utilisateurConnecte.getId());
            logger.info("agentCreateurId défini automatiquement à partir de l'utilisateur connecté: ID={}", utilisateurConnecte.getId());

            // Validation PDF
            validatePdfFile(contratSigne, "contratSigne");
            validatePdfFile(pouvoir, "pouvoir");

            // Ajouter les fichiers au request
            if (contratSigne != null && !contratSigne.isEmpty()) {
                request.setContratSigneFile(contratSigne);
            }
            if (pouvoir != null && !pouvoir.isEmpty()) {
                request.setPouvoirFile(pouvoir);
            }

            // Déterminer le statut selon le rôle de l'utilisateur connecté
            boolean createurEstChef = isChef(utilisateurConnecte.getRoleUtilisateur());
            if (createurEstChef || isChef) {
                request.setStatut(Statut.VALIDE);
            } else {
                request.setStatut(Statut.EN_ATTENTE_VALIDATION);
            }

            Dossier createdDossier = dossierService.createDossier(request);

            // Si l'utilisateur est chef, valider immédiatement le dossier
            if (createurEstChef || isChef) {
                try {
                    dossierService.validerDossier(createdDossier.getId(), request.getAgentCreateurId());
                    createdDossier = dossierService.getDossierById(createdDossier.getId()).orElse(createdDossier);
                } catch (RuntimeException e) {
                    logger.error("Erreur lors de la validation automatique par chef: {}", e.getMessage());
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Validation chef",
                        "message", e.getMessage(),
                        "timestamp", new Date().toString()
                    ));
                }
            }

            return new ResponseEntity<>(createdDossier, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur de validation",
                "message", e.getMessage(),
                "timestamp", new Date().toString()
            ));
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la création du dossier avec fichiers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Erreur interne du serveur",
                "message", "Erreur lors de la création du dossier: " + e.getMessage(),
                "timestamp", new Date().toString()
            ));
        }
    }

    /**
     * Crée un nouveau dossier avec workflow (version simplifiée - JSON uniquement)
     * 
     * @param request Les données du dossier à créer
     * @return ResponseEntity avec le dossier créé (201 CREATED) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/dossiers/create
     * Content-Type: application/json
     * {
     *   "titre": "Dossier test",
     *   "nomCreancier": "Entreprise ABC",
     *   "nomDebiteur": "Client XYZ"
     * }
     */
    // Nouvelle route: POST /api/dossiers/create (JSON)
    @PostMapping(path = "/create", consumes = {"application/json"})
    public ResponseEntity<?> createDossierSimple(@RequestBody DossierRequest request,
                                                 @RequestParam(value = "isChef", required = false, defaultValue = "false") boolean isChef,
                                                 @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            // Extraire l'utilisateur connecté depuis le token (obligatoire)
            if (authHeader == null || authHeader.isBlank()) {
                logger.warn("/api/dossiers/create (JSON) - Token manquant");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Non autorisé",
                    "message", "Token d'authentification requis pour créer un dossier",
                    "code", "TOKEN_MISSING",
                    "timestamp", new Date().toString()
                ));
            }

            Utilisateur utilisateurConnecte;
            try {
                utilisateurConnecte = userExtractionService.extractUserFromToken(authHeader);
                if (utilisateurConnecte == null) {
                    logger.warn("/api/dossiers/create (JSON) - Utilisateur non trouvé");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "Token invalide",
                        "message", "Impossible d'extraire l'utilisateur depuis le token",
                        "code", "USER_NOT_FOUND",
                        "timestamp", new Date().toString()
                    ));
                }
            } catch (ExpiredJwtException e) {
                logger.error("/api/dossiers/create (JSON) - Token JWT expiré: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Token expiré",
                    "message", "Votre session a expiré. Veuillez vous reconnecter pour créer un dossier.",
                    "code", "TOKEN_EXPIRED",
                    "expiredAt", e.getClaims().getExpiration().toString(),
                    "currentTime", new Date().toString(),
                    "timestamp", new Date().toString()
                ));
            }

            // TOUJOURS définir agentCreateurId à partir de l'utilisateur connecté (écrase toute valeur du DTO)
            request.setAgentCreateurId(utilisateurConnecte.getId());
            logger.info("agentCreateurId défini automatiquement à partir de l'utilisateur connecté: ID={}", utilisateurConnecte.getId());

            // Déterminer le statut selon le rôle de l'utilisateur connecté
            boolean createurEstChef = isChef(utilisateurConnecte.getRoleUtilisateur());
            if (createurEstChef) {
                request.setStatut(Statut.VALIDE);
            } else {
                request.setStatut(Statut.EN_ATTENTE_VALIDATION);
            }

            Dossier createdDossier = dossierService.createDossier(request);

            // Si l'utilisateur est chef, valider immédiatement le dossier
            if (createurEstChef || isChef) {
                try {
                    dossierService.validerDossier(createdDossier.getId(), request.getAgentCreateurId());
                    createdDossier = dossierService.getDossierById(createdDossier.getId()).orElse(createdDossier);
                } catch (RuntimeException e) {
                    logger.error("Erreur lors de la validation automatique par chef: {}", e.getMessage());
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Validation chef",
                        "message", e.getMessage(),
                        "timestamp", new Date().toString()
                    ));
                }
            }

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
     * Récupère les dossiers affectés au recouvrement amiable avec pagination
     * 
     * @param page Numéro de page (défaut: 0)
     * @param size Taille de la page (défaut: 10)
     * @param sort Champ de tri (défaut: "dateCreation")
     * @return ResponseEntity avec la page des dossiers amiable (200 OK) ou erreur (500)
     * 
     * @example
     * GET /api/dossiers/recouvrement-amiable?page=0&size=10&sort=dateCreation
     */
    @GetMapping("/recouvrement-amiable")
    public ResponseEntity<?> getDossiersRecouvrementAmiable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateCreation") String sort) {
        try {
            // Validation des paramètres
            if (page < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le numéro de page doit être >= 0"));
            }
            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest().body(Map.of("error", "La taille de page doit être entre 1 et 100"));
            }
            
            // Créer la pagination avec tri
            Sort.Direction sortDirection = Sort.Direction.DESC;
            Sort sortObj = Sort.by(sortDirection, sort);
            Pageable pageable = PageRequest.of(page, size, sortObj);
            
            // Récupérer les dossiers avec pagination
            Page<Dossier> dossierPage = dossierRepository.findByTypeRecouvrementAndValideAndDossierStatus(
                TypeRecouvrement.AMIABLE,
                DossierStatus.ENCOURSDETRAITEMENT,
                pageable
            );
            
            // Construire la réponse
            Map<String, Object> result = Map.of(
                "content", dossierPage.getContent(),
                "totalElements", dossierPage.getTotalElements(),
                "totalPages", dossierPage.getTotalPages(),
                "currentPage", dossierPage.getNumber(),
                "size", dossierPage.getSize(),
                "first", dossierPage.isFirst(),
                "last", dossierPage.isLast(),
                "numberOfElements", dossierPage.getNumberOfElements()
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des dossiers recouvrement amiable: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de la récupération: " + e.getMessage()
                    ));
        }
    }

    /**
     * Récupère les dossiers affectés au recouvrement juridique avec pagination
     * 
     * @param page Numéro de page (défaut: 0)
     * @param size Taille de la page (défaut: 10)
     * @param sort Champ de tri (défaut: "dateCreation")
     * @return ResponseEntity avec la page des dossiers juridique (200 OK) ou erreur (500)
     * 
     * @example
     * GET /api/dossiers/recouvrement-juridique?page=0&size=10&sort=dateCreation
     */
    @GetMapping("/recouvrement-juridique")
    public ResponseEntity<?> getDossiersRecouvrementJuridique(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateCreation") String sort) {
        try {
            // Validation des paramètres
            if (page < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le numéro de page doit être >= 0"));
            }
            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest().body(Map.of("error", "La taille de page doit être entre 1 et 100"));
            }
            
            // Créer la pagination avec tri
            Sort.Direction sortDirection = Sort.Direction.DESC;
            Sort sortObj = Sort.by(sortDirection, sort);
            Pageable pageable = PageRequest.of(page, size, sortObj);
            
            // Récupérer les dossiers avec pagination
            Page<Dossier> dossierPage = dossierRepository.findByTypeRecouvrementAndValideAndDossierStatus(
                TypeRecouvrement.JURIDIQUE,
                DossierStatus.ENCOURSDETRAITEMENT,
                pageable
            );
            
            // Construire la réponse
            Map<String, Object> result = Map.of(
                "content", dossierPage.getContent(),
                "totalElements", dossierPage.getTotalElements(),
                "totalPages", dossierPage.getTotalPages(),
                "currentPage", dossierPage.getNumber(),
                "size", dossierPage.getSize(),
                "first", dossierPage.isFirst(),
                "last", dossierPage.isLast(),
                "numberOfElements", dossierPage.getNumberOfElements()
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des dossiers recouvrement juridique: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de la récupération: " + e.getMessage()
                    ));
        }
    }

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
     * Récupère tous les dossiers avec filtres optionnels et pagination
     * 
     * @param role Le rôle de l'utilisateur pour filtrer les dossiers
     * @param userId L'ID de l'utilisateur pour filtrer les dossiers
     * @param page Numéro de page (défaut: 0)
     * @param size Taille de la page (défaut: 10)
     * @param search Terme de recherche dans titre, numeroDossier, description
     * @param authHeader Le token d'authentification
     * @return ResponseEntity avec la page des dossiers (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * GET /api/dossiers?role=AGENT&userId=123&page=0&size=10&search=client
     * GET /api/dossiers?role=CHEF&page=0&size=5
     * GET /api/dossiers?search=urgent
     * GET /api/dossiers
     */
    @GetMapping
    public ResponseEntity<?> getAllDossiers(
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        
        logger.info("=== DÉBUT getAllDossiers ===");
        
        try {
            // Validation des paramètres
            if (page < 0) {
                logger.warn("Numéro de page invalide: {}", page);
                return ResponseEntity.badRequest().body(Map.of("error", "Le numéro de page doit être >= 0"));
            }
            if (size <= 0 || size > 100) {
                logger.warn("Taille de page invalide: {}", size);
                return ResponseEntity.badRequest().body(Map.of("error", "La taille de page doit être entre 1 et 100"));
            }



            // Appel du service avec pagination
            Map<String, Object> result = dossierService.getAllDossiersWithPagination(role, userId, page, size, search);
            
            logger.info("Résultat - totalElements: {}, totalPages: {}, currentPage: {}, size: {}", 
                       result.get("totalElements"), result.get("totalPages"), 
                       result.get("currentPage"), result.get("size"));

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation dans getAllDossiers: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur getAllDossiers avec filtres: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne du serveur: " + e.getMessage()));
        }
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

    // GET /api/dossiers/search?term=
    @GetMapping("/search")
    public ResponseEntity<List<Dossier>> searchDossiers(@RequestParam(name = "term") String term) {
        List<Dossier> dossiers = dossierService.searchDossiers(term);
        return new ResponseEntity<>(dossiers, HttpStatus.OK);
    }

    // GET /api/dossiers/status/{statut}
    @GetMapping("/status/{statut}")
    public ResponseEntity<List<Dossier>> getDossiersByValidationStatut(@PathVariable String statut) {
        try {
            StatutValidation s = StatutValidation.valueOf(statut.toUpperCase());
            List<Dossier> dossiers = dossierService.getDossiersByValidationStatut(s);
            return ResponseEntity.ok(dossiers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
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
            logger.error("Erreur lors de la récupération des dossiers de l'agent {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les dossiers assignés à un agent avec pagination
     * 
     * @param agentId L'ID de l'agent
     * @param page Numéro de page (défaut: 0)
     * @param size Taille de la page (défaut: 10, max: 100)
     * @param sort Champ de tri (défaut: "dateCreation")
     * @return ResponseEntity avec la page des dossiers de l'agent (200 OK) ou erreur (400/500)
     * 
     * @example
     * GET /api/dossiers/agent/1/paginated?page=0&size=10&sort=dateCreation
     */
    @GetMapping("/agent/{agentId}/paginated")
    public ResponseEntity<?> getDossiersByAgentPaginated(
            @PathVariable Long agentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateCreation") String sort) {
        try {
            // Validation des paramètres
            if (page < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le numéro de page doit être >= 0"));
            }
            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest().body(Map.of("error", "La taille de page doit être entre 1 et 100"));
            }
            
            // Corriger la taille si elle dépasse 100
            if (size > 100) {
                size = 100;
            }
            
            // Créer la pagination avec tri
            Sort.Direction sortDirection = Sort.Direction.DESC;
            Sort sortObj = Sort.by(sortDirection, sort);
            Pageable pageable = PageRequest.of(page, size, sortObj);
            
            // Récupérer tous les dossiers de l'agent
            List<Dossier> allDossiers = dossierService.getDossiersByAgent(agentId);
            
            // Paginer manuellement
            int start = page * size;
            int end = Math.min(start + size, allDossiers.size());
            List<Dossier> pagedDossiers = start < allDossiers.size() 
                ? allDossiers.subList(start, end) 
                : List.of();
            
            // Calculer le nombre total de pages
            int totalPages = (int) Math.ceil((double) allDossiers.size() / size);
            
            // Construire la réponse
            Map<String, Object> result = Map.of(
                "content", pagedDossiers,
                "totalElements", allDossiers.size(),
                "totalPages", totalPages,
                "currentPage", page,
                "size", size,
                "first", page == 0,
                "last", page >= totalPages - 1,
                "numberOfElements", pagedDossiers.size()
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération paginée des dossiers de l'agent {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de la récupération: " + e.getMessage()
                    ));
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
    // PUT /api/dossiers/{id}/valider
    @PutMapping("/{id}/valider")
    public ResponseEntity<Dossier> validerDossier(@PathVariable Long id, @RequestParam Long chefId) {
        try {
            dossierService.validerDossier(id, chefId);
            Optional<Dossier> updated = dossierService.getDossierById(id);
            return updated.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
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
    // PUT /api/dossiers/{id}/rejeter
    @PutMapping("/{id}/rejeter")
    public ResponseEntity<Dossier> rejeterDossier(@PathVariable Long id, @RequestParam String commentaire) {
        try {
            dossierService.rejeterDossier(id, commentaire);
            Optional<Dossier> updated = dossierService.getDossierById(id);
            return updated.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/dossiers/stats?agentId=
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestParam(name = "agentId", required = false) Long agentId,
                                      @RequestParam(name = "role", required = false) String role) {
        try {
            if (role != null && role.toUpperCase().startsWith("AGENT")) {
                if (agentId == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "agentId requis pour role=AGENT"));
                }
                // Stats restreintes à l'agent (créés + responsables)
                ResponseEntity<?> response = getAllDossiers("AGENT", agentId, 0, 1000, null, null);
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.getBody();
                @SuppressWarnings("unchecked")
                List<Dossier> visibles = result != null ? (List<Dossier>) result.get("content") : java.util.List.of();
                long total = visibles.size();
                long valides = visibles.stream().filter(d -> d.getStatut() == Statut.VALIDE).count();
                long enCours = visibles.stream().filter(d -> d.getStatut() == Statut.EN_ATTENTE_VALIDATION).count();
                long ceMois = visibles.stream().filter(d -> {
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.set(java.util.Calendar.DAY_OF_MONTH, 1);
                    c.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    c.set(java.util.Calendar.MINUTE, 0);
                    c.set(java.util.Calendar.SECOND, 0);
                    c.set(java.util.Calendar.MILLISECOND, 0);
                    return d.getDateCreation() != null && d.getDateCreation().after(c.getTime());
                }).count();
                return ResponseEntity.ok(Map.of(
                    "total", total,
                    "enCours", enCours,
                    "valides", valides,
                    "ceMois", ceMois,
                    "agentId", agentId
                ));
            } else {
                // Chef (ou rôle non fourni): stats globales
                long total = dossierService.countTotalDossiers();
                long enCours = dossierService.countDossiersEnCours();
                long valides = dossierService.countDossiersValides();
                long ceMois = dossierService.countDossiersCreesCeMois();
                if (agentId != null) {
                    long nbAgent = dossierService.countDossiersByAgent(agentId);
                    long nbAgentCrees = dossierService.countDossiersCreesByAgent(agentId);
                    return ResponseEntity.ok(Map.of(
                        "total", total,
                        "enCours", enCours,
                        "valides", valides,
                        "ceMois", ceMois,
                        "agentId", agentId,
                        "parAgent", nbAgent,
                        "creesParAgent", nbAgentCrees
                    ));
                }
                return ResponseEntity.ok(Map.of(
                    "total", total,
                    "enCours", enCours,
                    "valides", valides,
                    "ceMois", ceMois
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Stats",
                "message", e.getMessage()
            ));
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

    // ==================== ENDPOINTS D'AFFECTATION ====================

    /**
     * Assigne un agent responsable à un dossier
     * 
     * @param id L'ID du dossier
     * @param agentId L'ID de l'agent à assigner
     * @return ResponseEntity avec le dossier mis à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * PUT /api/dossiers/1/assign/agent?agentId=5
     */
    @PutMapping("/{id}/assign/agent")
    public ResponseEntity<?> assignerAgentResponsable(@PathVariable Long id, @RequestParam Long agentId) {
        try {
            Dossier updatedDossier = dossierService.assignerAgentResponsable(id, agentId);
            return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de l'assignation de l'agent: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur d'assignation",
                "message", e.getMessage(),
                "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            logger.error("Erreur interne lors de l'assignation de l'agent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de l'assignation de l'agent: " + e.getMessage(),
                        "timestamp", new Date().toString()
                    ));
        }
    }

    /**
     * Assigne un avocat à un dossier
     * 
     * @param id L'ID du dossier
     * @param avocatId L'ID de l'avocat à assigner
     * @return ResponseEntity avec le dossier mis à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * PUT /api/dossiers/1/assign/avocat?avocatId=3
     */
    @PutMapping("/{id}/assign/avocat")
    public ResponseEntity<?> assignerAvocat(@PathVariable Long id, @RequestParam Long avocatId) {
        try {
            Dossier updatedDossier = dossierService.assignerAvocat(id, avocatId);
            return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de l'assignation de l'avocat: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur d'assignation",
                "message", e.getMessage(),
                "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            logger.error("Erreur interne lors de l'assignation de l'avocat: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de l'assignation de l'avocat: " + e.getMessage(),
                        "timestamp", new Date().toString()
                    ));
        }
    }

    /**
     * Assigne un huissier à un dossier
     * 
     * @param id L'ID du dossier
     * @param huissierId L'ID de l'huissier à assigner
     * @return ResponseEntity avec le dossier mis à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * PUT /api/dossiers/1/assign/huissier?huissierId=2
     */
    @PutMapping("/{id}/assign/huissier")
    public ResponseEntity<?> assignerHuissier(@PathVariable Long id, @RequestParam Long huissierId) {
        try {
            Dossier updatedDossier = dossierService.assignerHuissier(id, huissierId);
            return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de l'assignation de l'huissier: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur d'assignation",
                "message", e.getMessage(),
                "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            logger.error("Erreur interne lors de l'assignation de l'huissier: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de l'assignation de l'huissier: " + e.getMessage(),
                        "timestamp", new Date().toString()
                    ));
        }
    }
    
    /**
     * Affecte un dossier à un avocat et/ou un huissier de manière flexible
     * Permet d'affecter soit un avocat, soit un huissier, soit les deux
     * Si un ID est null dans le body, l'affectation correspondante sera retirée
     * 
     * @param id L'ID du dossier
     * @param affectationDTO DTO contenant les IDs de l'avocat et/ou de l'huissier (optionnels)
     * @return ResponseEntity avec le dossier mis à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * PUT /api/dossiers/1/assign/avocat-huissier
     * Body: {"avocatId": 3, "huissierId": 2}  // Affecter les deux
     * Body: {"avocatId": 3}                    // Affecter uniquement l'avocat
     * Body: {"huissierId": 2}                  // Affecter uniquement l'huissier
     * Body: {"avocatId": null, "huissierId": 2} // Retirer l'avocat, affecter l'huissier
     */
    @PutMapping("/{id}/assign/avocat-huissier")
    public ResponseEntity<?> affecterAvocatEtHuissier(
            @PathVariable Long id,
            @RequestBody AffectationDossierDTO affectationDTO) {
        try {
            Dossier updatedDossier = dossierService.affecterAvocatEtHuissier(
                    id, 
                    affectationDTO.getAvocatId(), 
                    affectationDTO.getHuissierId()
            );
            return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de l'affectation avocat/huissier: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur d'affectation",
                "message", e.getMessage(),
                "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            logger.error("Erreur interne lors de l'affectation avocat/huissier: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de l'affectation avocat/huissier: " + e.getMessage(),
                        "timestamp", new Date().toString()
                    ));
        }
    }
    
    /**
     * Affecte un dossier validé au recouvrement amiable
     * 
     * @param id L'ID du dossier à affecter
     * @return ResponseEntity avec le dossier mis à jour (200 OK) ou erreur
     * 
     * @example
     * PUT /api/dossiers/1/affecter/recouvrement-amiable
     */
    @PutMapping("/{id}/affecter/recouvrement-amiable")
    public ResponseEntity<?> affecterAuRecouvrementAmiable(@PathVariable Long id) {
        try {
            Dossier updatedDossier = dossierService.affecterAuRecouvrementAmiable(id);
            return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de l'affectation au recouvrement amiable: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur d'affectation",
                "message", e.getMessage(),
                "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            logger.error("Erreur interne lors de l'affectation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de l'affectation: " + e.getMessage(),
                        "timestamp", new Date().toString()
                    ));
        }
    }

    /**
     * Affecte un dossier validé au recouvrement juridique
     * 
     * @param id L'ID du dossier à affecter
     * @return ResponseEntity avec le dossier mis à jour (200 OK) ou erreur
     * 
     * @example
     * PUT /api/dossiers/1/affecter/recouvrement-juridique
     */
    @PutMapping("/{id}/affecter/recouvrement-juridique")
    public ResponseEntity<?> affecterAuRecouvrementJuridique(@PathVariable Long id) {
        try {
            Dossier updatedDossier = dossierService.affecterAuRecouvrementJuridique(id);
            return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de l'affectation au recouvrement juridique: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur d'affectation",
                "message", e.getMessage(),
                "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            logger.error("Erreur interne lors de l'affectation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de l'affectation: " + e.getMessage(),
                        "timestamp", new Date().toString()
                    ));
        }
    }

    /**
     * Affecte un dossier validé au chef du département finance
     * PUT /api/dossiers/1/affecter/finance
     */
    @PutMapping("/{id}/affecter/finance")
    public ResponseEntity<?> affecterAuFinance(@PathVariable Long id) {
        try {
            Dossier updatedDossier = dossierService.affecterAuFinance(id);
            return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de l'affectation au finance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur d'affectation",
                "message", e.getMessage(),
                "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            logger.error("Erreur interne lors de l'affectation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de l'affectation: " + e.getMessage(),
                        "timestamp", new Date().toString()
                    ));
        }
    }

    /**
     * Clôture un dossier validé
     * 
     * @param id L'ID du dossier à clôturer
     * @return ResponseEntity avec le dossier mis à jour (200 OK) ou erreur
     * 
     * @example
     * PUT /api/dossiers/1/cloturer
     */
    @PutMapping("/{id}/cloturer")
    public ResponseEntity<?> cloturerDossier(@PathVariable Long id) {
        try {
            Dossier updatedDossier = dossierService.cloturerDossier(id);
            return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la clôture du dossier: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur de clôture",
                "message", e.getMessage(),
                "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            logger.error("Erreur interne lors de la clôture: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de la clôture: " + e.getMessage(),
                        "timestamp", new Date().toString()
                    ));
        }
    }
    
    /**
     * Récupère les dossiers validés disponibles pour l'affectation
     * 
     * @param page Numéro de page (optionnel, défaut: 0)
     * @param size Taille de la page (optionnel, défaut: 10)
     * @param sort Champ de tri (optionnel, défaut: "dateCreation")
     * @param direction Direction du tri (optionnel, défaut: "DESC")
     * @param search Terme de recherche (optionnel)
     * @return ResponseEntity avec la liste paginée des dossiers validés
     * 
     * @example
     * GET /api/dossiers/valides-disponibles?page=0&size=10&sort=dateCreation&direction=DESC&search=Dossier61
     */
    @GetMapping("/valides-disponibles")
    public ResponseEntity<?> getDossiersValidesDisponibles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateCreation") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String search) {
        try {
            Map<String, Object> result = dossierService.getDossiersValidesDisponibles(page, size, sort, direction, search);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des dossiers validés: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Erreur interne du serveur",
                        "message", "Erreur lors de la récupération: " + e.getMessage()
                    ));
        }
    }

    // ==================== NOUVEAUX ENDPOINTS POUR MONTANTS ====================
    
    /**
     * Met à jour les montants d'un dossier
     * @param id ID du dossier
     * @param dto DTO contenant les montants et le mode de mise à jour
     * @return Dossier mis à jour
     */
    @PutMapping("/{id}/montant")
    public ResponseEntity<?> updateMontants(
            @PathVariable Long id,
            @RequestBody MontantDossierDTO dto) {
        try {
            Dossier updatedDossier = dossierMontantService.updateMontants(id, dto);
            return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation lors de la mise à jour des montants: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la mise à jour des montants: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur interne lors de la mise à jour des montants: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }
    
    /**
     * Enregistre une réponse amiable avec montant recouvré et prédiction IA
     * @param id ID du dossier
     * @param dto DTO contenant le montant recouvré
     * @return Dossier mis à jour avec prédiction IA
     */
    @PostMapping("/{id}/amiable")
    public ResponseEntity<?> enregistrerActionAmiable(
            @PathVariable Long id,
            @RequestBody ActionAmiableDTO dto) {
        try {
            // Validation
            if (dto.getMontantRecouvre() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "montantRecouvre est requis"));
            }
            if (dto.getMontantRecouvre().compareTo(java.math.BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "montantRecouvre ne peut pas être négatif"));
            }
            
            // Mettre à jour le montant recouvré (mode ADD par défaut)
            Dossier dossier = dossierMontantService.updateMontantRecouvreAmiable(
                id, 
                dto.getMontantRecouvre(), 
                projet.carthagecreance_backend.Entity.ModeMiseAJour.ADD
            );
            
            // ✅ NOUVEAU : Prédiction IA
            try {
                // Récupérer les données associées
                Optional<projet.carthagecreance_backend.Entity.Enquette> enqueteOpt = 
                    enquetteRepository.findByDossierId(id);
                List<projet.carthagecreance_backend.Entity.Action> actions = 
                    actionRepository.findByDossierId(id);
                List<projet.carthagecreance_backend.Entity.Audience> audiences = 
                    audienceRepository.findByDossierId(id);
                List<projet.carthagecreance_backend.Entity.ActionHuissier> actionsHuissier = 
                    actionHuissierRepository.findByDossierId(id);
                
                // Construire les features à partir des données réelles
                Map<String, Object> features = iaFeatureBuilderService.buildFeaturesFromRealData(
                    dossier,
                    enqueteOpt.orElse(null),
                    actions,
                    audiences,
                    actionsHuissier
                );
                
                // Prédire avec l'IA
                projet.carthagecreance_backend.DTO.IaPredictionResult prediction = 
                    iaPredictionService.predictRisk(features);
                
                // Mettre à jour le dossier avec les résultats de la prédiction
                dossier.setEtatPrediction(
                    projet.carthagecreance_backend.Entity.EtatDossier.valueOf(prediction.getEtatFinal())
                );
                dossier.setRiskScore(prediction.getRiskScore());
                dossier.setRiskLevel(prediction.getRiskLevel());
                
                // Sauvegarder le dossier mis à jour
                dossier = dossierRepository.save(dossier);
                
                logger.info("Prédiction IA appliquée au dossier {}: etatPrediction={}, riskScore={}, riskLevel={}", 
                    id, prediction.getEtatFinal(), prediction.getRiskScore(), prediction.getRiskLevel());
                
            } catch (Exception e) {
                // En cas d'erreur avec l'IA, on continue quand même (pas bloquant)
                logger.warn("Erreur lors de la prédiction IA pour le dossier {}: {}. Le dossier sera sauvegardé sans prédiction.", 
                    id, e.getMessage());
            }
            
            return new ResponseEntity<>(dossier, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation lors de l'enregistrement de l'action amiable: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Erreur lors de l'enregistrement de l'action amiable: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur interne lors de l'enregistrement de l'action amiable: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }
}