package projet.carthagecreance_backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.DTO.FactureDTO;
import projet.carthagecreance_backend.DTO.SoldeFactureDTO;
import projet.carthagecreance_backend.Entity.Facture;
import projet.carthagecreance_backend.Entity.FactureStatut;
import projet.carthagecreance_backend.Mapper.FactureMapper;
import projet.carthagecreance_backend.Service.FactureService;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/factures")
@CrossOrigin(origins = "*")
public class FactureController {

    private static final Logger logger = LoggerFactory.getLogger(FactureController.class);

    @Autowired
    private FactureService factureService;
    
    @Autowired
    private FactureMapper factureMapper;

    @PostMapping
    public ResponseEntity<?> createFacture(@RequestBody FactureDTO dto) {
        try {
            Facture facture = factureService.createFacture(dto);
            FactureDTO factureDTO = factureMapper.toDTO(facture);
            return new ResponseEntity<>(factureDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la facture: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la création de la facture",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFactureById(@PathVariable Long id) {
        Optional<Facture> facture = factureService.getFactureById(id);
        return facture.map(value -> {
            FactureDTO factureDTO = factureMapper.toDTO(value);
            return new ResponseEntity<>(factureDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<?> getFactureByNumero(@PathVariable String numero) {
        Optional<Facture> facture = factureService.getFactureByNumero(numero);
        return facture.map(value -> {
            FactureDTO factureDTO = factureMapper.toDTO(value);
            return new ResponseEntity<>(factureDTO, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<FactureDTO>> getAllFactures() {
        List<Facture> factures = factureService.getAllFactures();
        List<FactureDTO> factureDTOs = factureMapper.toDTOList(factures);
        return new ResponseEntity<>(factureDTOs, HttpStatus.OK);
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<FactureDTO>> getFacturesByDossier(@PathVariable Long dossierId) {
        List<Facture> factures = factureService.getFacturesByDossier(dossierId);
        List<FactureDTO> factureDTOs = factureMapper.toDTOList(factures);
        return new ResponseEntity<>(factureDTOs, HttpStatus.OK);
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<FactureDTO>> getFacturesByStatut(@PathVariable FactureStatut statut) {
        List<Facture> factures = factureService.getFacturesByStatut(statut);
        List<FactureDTO> factureDTOs = factureMapper.toDTOList(factures);
        return new ResponseEntity<>(factureDTOs, HttpStatus.OK);
    }

    @GetMapping("/en-retard")
    public ResponseEntity<List<FactureDTO>> getFacturesEnRetard() {
        List<Facture> factures = factureService.getFacturesEnRetard();
        List<FactureDTO> factureDTOs = factureMapper.toDTOList(factures);
        return new ResponseEntity<>(factureDTOs, HttpStatus.OK);
    }

    @PostMapping("/dossier/{dossierId}/generer")
    public ResponseEntity<?> genererFactureAutomatique(
            @PathVariable Long dossierId,
            @RequestParam(required = false) LocalDate periodeDebut,
            @RequestParam(required = false) LocalDate periodeFin) {
        try {
            Facture facture = factureService.genererFactureAutomatique(
                    dossierId,
                    periodeDebut != null ? periodeDebut : LocalDate.now().minusMonths(1),
                    periodeFin != null ? periodeFin : LocalDate.now()
            );
            FactureDTO factureDTO = factureMapper.toDTO(facture);
            return new ResponseEntity<>(factureDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération de la facture: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la génération de la facture",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @PutMapping("/{id}/finaliser")
    public ResponseEntity<?> finaliserFacture(@PathVariable Long id) {
        try {
            Facture facture = factureService.finaliserFacture(id);
            FactureDTO factureDTO = factureMapper.toDTO(facture);
            return new ResponseEntity<>(factureDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la finalisation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la finalisation",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @PutMapping("/{id}/envoyer")
    public ResponseEntity<?> envoyerFacture(@PathVariable Long id) {
        try {
            Facture facture = factureService.envoyerFacture(id);
            FactureDTO factureDTO = factureMapper.toDTO(facture);
            return new ResponseEntity<>(factureDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de l'envoi: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de l'envoi",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @PutMapping("/{id}/relancer")
    public ResponseEntity<?> relancerFacture(@PathVariable Long id) {
        try {
            Facture facture = factureService.relancerFacture(id);
            FactureDTO factureDTO = factureMapper.toDTO(facture);
            return new ResponseEntity<>(factureDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la relance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la relance",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> genererPdfFacture(@PathVariable Long id) {
        try {
            byte[] pdf = factureService.genererPdfFacture(id);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .body(pdf);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération PDF: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFacture(@PathVariable Long id, @RequestBody FactureDTO dto) {
        try {
            Facture facture = factureService.updateFacture(id, dto);
            FactureDTO factureDTO = factureMapper.toDTO(facture);
            return new ResponseEntity<>(factureDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Erreur lors de la mise à jour: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la mise à jour",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacture(@PathVariable Long id) {
        try {
            factureService.deleteFacture(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // ✅ NOUVEAU : Endpoint pour calculer le solde restant
    @GetMapping("/{factureId}/solde")
    public ResponseEntity<?> calculerSoldeRestant(@PathVariable Long factureId) {
        try {
            SoldeFactureDTO solde = factureService.calculerSoldeRestant(factureId);
            return ResponseEntity.ok(solde);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul du solde: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors du calcul du solde",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }
    
    // ✅ NOUVEAU : Endpoint pour vérifier et mettre à jour le statut de la facture
    @PutMapping("/{factureId}/verifier-statut")
    public ResponseEntity<?> verifierStatutFacture(@PathVariable Long factureId) {
        try {
            Facture facture = factureService.verifierEtMettreAJourStatutFacture(factureId);
            FactureDTO factureDTO = factureMapper.toDTO(facture);
            return ResponseEntity.ok(factureDTO);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du statut: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la vérification du statut",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }
}

