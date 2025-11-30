package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour gérer le workflow huissier
 */
@RestController
@RequestMapping("/api/dossiers")
@CrossOrigin(origins = "*")
public class HuissierWorkflowController {

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private DocumentHuissierRepository documentHuissierRepository;

    @Autowired
    private ActionHuissierRepository actionHuissierRepository;

    /**
     * Passe un dossier à l'étape actions
     * POST /api/dossiers/{dossierId}/huissier/passer-aux-actions
     */
    @PostMapping("/{dossierId}/huissier/passer-aux-actions")
    @Transactional
    public ResponseEntity<?> passerAuxActions(@PathVariable Long dossierId) {
        try {
            Dossier dossier = dossierRepository.findById(dossierId)
                    .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));

            // Vérifier qu'il y a au moins un document
            List<DocumentHuissier> documents = documentHuissierRepository.findByDossierId(dossierId);
            if (documents.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vous devez créer au moins un document avant de passer aux actions"));
            }

            // Vérifier l'étape actuelle (optionnel mais recommandé)
            if (dossier.getEtapeHuissier() != EtapeHuissier.EN_DOCUMENTS && 
                dossier.getEtapeHuissier() != EtapeHuissier.EN_ATTENTE_DOCUMENTS) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le dossier doit être à l'étape documents pour passer aux actions"));
            }

            dossier.setEtapeHuissier(EtapeHuissier.EN_ACTIONS);
            Dossier updated = dossierRepository.save(dossier);

            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Passe un dossier à l'étape audiences
     * POST /api/dossiers/{dossierId}/huissier/passer-aux-audiences
     */
    @PostMapping("/{dossierId}/huissier/passer-aux-audiences")
    @Transactional
    public ResponseEntity<?> passerAuxAudiences(@PathVariable Long dossierId) {
        try {
            Dossier dossier = dossierRepository.findById(dossierId)
                    .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));

            // Vérifier qu'il y a au moins une action
            List<ActionHuissier> actions = actionHuissierRepository.findByDossierId(dossierId);
            if (actions.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vous devez créer au moins une action avant de passer aux audiences"));
            }

            // Vérifier l'étape actuelle (optionnel mais recommandé)
            if (dossier.getEtapeHuissier() != EtapeHuissier.EN_ACTIONS) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le dossier doit être à l'étape actions pour passer aux audiences"));
            }

            dossier.setEtapeHuissier(EtapeHuissier.EN_AUDIENCES);
            Dossier updated = dossierRepository.save(dossier);

            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Récupère les dossiers à l'étape documents
     * GET /api/dossiers/huissier/documents
     */
    @GetMapping("/huissier/documents")
    public ResponseEntity<?> getDossiersEnDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        try {
            if (size > 100) {
                size = 100; // Limiter à 100
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Dossier> dossiers = dossierRepository.findByEtapeHuissier(
                    EtapeHuissier.EN_DOCUMENTS,
                    pageable
            );

            return ResponseEntity.ok(dossiers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération: " + e.getMessage()));
        }
    }

    /**
     * Récupère les dossiers à l'étape actions
     * GET /api/dossiers/huissier/actions
     */
    @GetMapping("/huissier/actions")
    public ResponseEntity<?> getDossiersEnActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        try {
            if (size > 100) {
                size = 100; // Limiter à 100
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Dossier> dossiers = dossierRepository.findByEtapeHuissier(
                    EtapeHuissier.EN_ACTIONS,
                    pageable
            );

            return ResponseEntity.ok(dossiers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération: " + e.getMessage()));
        }
    }

    /**
     * Récupère tous les documents huissier d'un dossier
     * GET /api/dossiers/{dossierId}/huissier/documents
     */
    @GetMapping("/{dossierId}/huissier/documents")
    public ResponseEntity<?> getDocumentsByDossier(@PathVariable Long dossierId) {
        try {
            List<DocumentHuissier> documents = documentHuissierRepository.findByDossierId(dossierId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération: " + e.getMessage()));
        }
    }

    /**
     * Récupère toutes les actions huissier d'un dossier
     * GET /api/dossiers/{dossierId}/huissier/actions
     */
    @GetMapping("/{dossierId}/huissier/actions")
    public ResponseEntity<?> getActionsByDossier(@PathVariable Long dossierId) {
        try {
            List<ActionHuissier> actions = actionHuissierRepository.findByDossierId(dossierId);
            return ResponseEntity.ok(actions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération: " + e.getMessage()));
        }
    }
}

