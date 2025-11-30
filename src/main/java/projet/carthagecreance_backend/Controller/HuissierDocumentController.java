package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projet.carthagecreance_backend.DTO.DocumentHuissierDTO;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Service.DocumentHuissierService;
import projet.carthagecreance_backend.Service.FileStorageService;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion des documents huissier (Phase 1 & 2)
 */
@RestController
@RequestMapping({"/api/huissier", "/huissier"})
@CrossOrigin(origins = "*")
public class HuissierDocumentController {

    @Autowired
    private DocumentHuissierService documentHuissierService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private DossierRepository dossierRepository;

    /**
     * Crée un document huissier (PV mise en demeure, Ordonnance de paiement, etc.)
     * POST /api/huissier/document
     * 
     * Compatible avec deux formats :
     * 1. JSON avec DocumentHuissierDTO (ancien format)
     * 2. Form-data avec MultipartFile (nouveau format)
     */
    @PostMapping("/document")
    public ResponseEntity<?> createDocument(
            @RequestParam(value = "dossierId", required = false) Long dossierId,
            @RequestParam(value = "typeDocument", required = false) String typeDocumentStr,
            @RequestParam(value = "huissierName", required = false) String huissierName,
            @RequestParam(value = "delaiLegalDays", required = false) Integer delaiLegalDays,
            @RequestParam(value = "pieceJointe", required = false) MultipartFile file,
            @RequestBody(required = false) DocumentHuissierDTO dto
    ) {
        try {
            DocumentHuissierDTO finalDto;
            
            // Si des paramètres de formulaire sont fournis, utiliser le nouveau format
            if (dossierId != null || typeDocumentStr != null || huissierName != null) {
                // Validation des paramètres requis
                if (dossierId == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "dossierId est requis"));
                }
                if (typeDocumentStr == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "typeDocument est requis"));
                }
                if (huissierName == null || huissierName.trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "huissierName est requis"));
                }
                
                // Convertir le typeDocument
                TypeDocumentHuissier typeDocument;
                try {
                    typeDocument = TypeDocumentHuissier.valueOf(typeDocumentStr);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Type de document invalide: " + typeDocumentStr));
                }
                
                // Créer le DTO
                finalDto = DocumentHuissierDTO.builder()
                        .dossierId(dossierId)
                        .typeDocument(typeDocument)
                        .huissierName(huissierName)
                        .delaiLegalDays(delaiLegalDays)
                        .build();
                
                // Gérer le fichier si présent
                if (file != null && !file.isEmpty()) {
                    try {
                        fileStorageService.validateFile(file);
                        String fileUrl = fileStorageService.storeFile(file, "huissier/documents");
                        finalDto.setPieceJointeUrl(fileUrl);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", e.getMessage()));
                    }
                }
            } else if (dto != null) {
                // Utiliser l'ancien format (JSON)
                finalDto = dto;
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Données manquantes. Utilisez soit form-data soit JSON"));
            }
            
            // Validation finale
            if (finalDto.getDossierId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "dossierId est requis"));
            }
            if (finalDto.getTypeDocument() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "typeDocument est requis"));
            }
            if (finalDto.getHuissierName() == null || finalDto.getHuissierName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "huissierName est requis"));
            }

            DocumentHuissier document = documentHuissierService.createDocument(finalDto);
            
            // Mettre à jour l'étape du dossier si c'est le premier document
            Dossier dossier = dossierRepository.findById(finalDto.getDossierId())
                    .orElse(null);
            if (dossier != null && dossier.getEtapeHuissier() == EtapeHuissier.EN_ATTENTE_DOCUMENTS) {
                dossier.setEtapeHuissier(EtapeHuissier.EN_DOCUMENTS);
                dossierRepository.save(dossier);
            }
            
            return new ResponseEntity<>(document, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Récupère un document par son ID
     * GET /api/huissier/document/{id}
     */
    @GetMapping("/document/{id}")
    public ResponseEntity<?> getDocumentById(@PathVariable Long id) {
        try {
            DocumentHuissier document = documentHuissierService.getDocumentById(id);
            return new ResponseEntity<>(document, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère tous les documents d'un dossier
     * GET /api/huissier/documents?dossierId={id}
     */
    @GetMapping("/documents")
    public ResponseEntity<?> getDocumentsByDossier(@RequestParam Long dossierId) {
        try {
            List<DocumentHuissier> documents = documentHuissierService.getDocumentsByDossier(dossierId);
            return new ResponseEntity<>(documents, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération: " + e.getMessage()));
        }
    }

    /**
     * Marque un document comme complété
     * PUT /api/huissier/document/{id}/complete
     * Contraintes :
     * - Seulement si le statut est PENDING
     * - Impossible si le statut est EXPIRED
     * - Impossible si le statut est déjà COMPLETED
     */
    @PutMapping("/document/{id}/complete")
    public ResponseEntity<?> markDocumentAsCompleted(@PathVariable Long id) {
        try {
            System.out.println("=== DÉBUT markDocumentAsCompleted pour document ID: " + id + " ===");
            DocumentHuissier document = documentHuissierService.markAsCompleted(id);
            System.out.println("=== SUCCÈS markDocumentAsCompleted ===");
            return ResponseEntity.ok(document);
        } catch (RuntimeException e) {
            System.err.println("❌ ERREUR Runtime dans markDocumentAsCompleted: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Erreur lors du marquage du document",
                            "message", e.getMessage(),
                            "documentId", id
                    ));
        } catch (Exception e) {
            System.err.println("❌ ERREUR Exception dans markDocumentAsCompleted: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Erreur interne du serveur",
                            "message", e.getMessage() != null ? e.getMessage() : "Une erreur inattendue s'est produite",
                            "documentId", id,
                            "exceptionType", e.getClass().getName()
                    ));
        }
    }

    /**
     * Marque un document comme expiré (utilisé par le scheduler)
     * PUT /api/huissier/document/{id}/expire
     */
    @PutMapping("/document/{id}/expire")
    public ResponseEntity<?> markDocumentAsExpired(@PathVariable Long id) {
        try {
            DocumentHuissier document = documentHuissierService.markAsExpired(id);
            return new ResponseEntity<>(document, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}