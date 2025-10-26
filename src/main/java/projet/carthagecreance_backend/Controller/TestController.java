package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Repository.DossierRepository;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur de test pour diagnostiquer les problèmes de base de données
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:4200")
public class TestController {

    @Autowired
    private DossierRepository dossierRepository;

    /**
     * Test simple pour récupérer tous les dossiers sans pagination
     */
    @GetMapping("/dossiers")
    public ResponseEntity<?> testGetAllDossiers() {
        try {
            System.out.println("=== TEST: Récupération de tous les dossiers ===");
            List<Dossier> dossiers = dossierRepository.findAll();
            System.out.println("Nombre de dossiers trouvés: " + dossiers.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", dossiers.size(),
                "dossiers", dossiers
            ));
        } catch (Exception e) {
            System.err.println("ERREUR lors du test: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "cause", e.getCause() != null ? e.getCause().getMessage() : "Unknown"
            ));
        }
    }

    /**
     * Test pour vérifier les valeurs d'enum dans la base de données
     */
    @GetMapping("/enum-check")
    public ResponseEntity<?> testEnumValues() {
        try {
            System.out.println("=== TEST: Vérification des valeurs d'enum ===");
            
            // Test simple de récupération d'un dossier
            List<Dossier> dossiers = dossierRepository.findAll();
            
            if (dossiers.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Aucun dossier trouvé",
                    "count", 0
                ));
            }
            
            // Vérifier le premier dossier
            Dossier premierDossier = dossiers.get(0);
            System.out.println("Premier dossier - ID: " + premierDossier.getId());
            System.out.println("Premier dossier - Status: " + premierDossier.getDossierStatus());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", dossiers.size(),
                "firstDossier", Map.of(
                    "id", premierDossier.getId(),
                    "titre", premierDossier.getTitre(),
                    "status", premierDossier.getDossierStatus()
                )
            ));
            
        } catch (Exception e) {
            System.err.println("ERREUR lors de la vérification des enums: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "cause", e.getCause() != null ? e.getCause().getMessage() : "Unknown"
            ));
        }
    }

    /**
     * Test de pagination simple
     */
    @GetMapping("/dossiers-paginated")
    public ResponseEntity<?> testDossiersPaginated() {
        try {
            System.out.println("=== TEST: Pagination simple ===");
            
            // Test de pagination simple sans spécification
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(0, 5, 
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "dateCreation"));
            
            org.springframework.data.domain.Page<Dossier> dossierPage = dossierRepository.findAll(pageable);
            
            System.out.println("Page récupérée - totalElements: " + dossierPage.getTotalElements());
            System.out.println("Page récupérée - totalPages: " + dossierPage.getTotalPages());
            System.out.println("Page récupérée - content size: " + dossierPage.getContent().size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "totalElements", dossierPage.getTotalElements(),
                "totalPages", dossierPage.getTotalPages(),
                "currentPage", dossierPage.getNumber(),
                "size", dossierPage.getSize(),
                "content", dossierPage.getContent()
            ));
            
        } catch (Exception e) {
            System.err.println("ERREUR lors du test de pagination: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "cause", e.getCause() != null ? e.getCause().getMessage() : "Unknown"
            ));
        }
    }

    /**
     * Test qui reproduit exactement l'endpoint principal
     */
    @GetMapping("/dossiers-main")
    public ResponseEntity<?> testDossiersMain() {
        try {
            System.out.println("=== TEST: Reproduction endpoint principal ===");
            
            // Paramètres par défaut comme dans l'endpoint principal
            String role = null;
            Long userId = null;
            int page = 0;
            int size = 10;
            String search = null;
            
            System.out.println("Paramètres - role: " + role + ", userId: " + userId + ", page: " + page + ", size: " + size + ", search: " + search);
            
            // Créer la pagination avec tri par date de création (plus récent en premier)
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size, 
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "dateCreation"));
            
            // Exécuter la requête paginée simple
            org.springframework.data.domain.Page<Dossier> dossierPage = dossierRepository.findAll(pageable);
            
            System.out.println("Résultat pagination - totalElements: " + dossierPage.getTotalElements());
            System.out.println("Résultat pagination - totalPages: " + dossierPage.getTotalPages());
            System.out.println("Résultat pagination - currentPage: " + dossierPage.getNumber());
            System.out.println("Résultat pagination - size: " + dossierPage.getSize());
            
            // Construire la réponse exactement comme dans le service
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
            System.err.println("ERREUR lors du test de l'endpoint principal: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "cause", e.getCause() != null ? e.getCause().getMessage() : "Unknown"
            ));
        }
    }

    @Autowired
    private projet.carthagecreance_backend.Service.DossierService dossierService;

    /**
     * Test avec le service DossierService directement
     */
    @GetMapping("/dossiers-service")
    public ResponseEntity<?> testDossiersService() {
        try {
            System.out.println("=== TEST: Service DossierService directement ===");
            
            // Appel direct du service
            Map<String, Object> result = dossierService.getAllDossiersWithPagination(null, null, 0, 10, null);
            
            System.out.println("Service appelé avec succès");
            System.out.println("Résultat - totalElements: " + result.get("totalElements"));
            System.out.println("Résultat - totalPages: " + result.get("totalPages"));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("ERREUR lors du test du service: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "cause", e.getCause() != null ? e.getCause().getMessage() : "Unknown"
            ));
        }
    }
}
