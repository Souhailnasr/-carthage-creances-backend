package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.DTO.DocumentHuissierDTO;
import projet.carthagecreance_backend.Entity.DocumentHuissier;
import projet.carthagecreance_backend.Service.DocumentHuissierService;

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

    /**
     * Crée un document huissier (PV mise en demeure, Ordonnance de paiement, etc.)
     * POST /api/huissier/document
     */
    @PostMapping("/document")
    public ResponseEntity<?> createDocument(@RequestBody DocumentHuissierDTO dto) {
        try {
            // Validation
            if (dto.getDossierId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "dossierId est requis"));
            }
            if (dto.getTypeDocument() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "typeDocument est requis"));
            }
            if (dto.getHuissierName() == null || dto.getHuissierName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "huissierName est requis"));
            }

            DocumentHuissier document = documentHuissierService.createDocument(dto);
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