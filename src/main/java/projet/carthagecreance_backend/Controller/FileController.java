package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Service.FileStorageService;

import java.util.Map;

/**
 * Contrôleur pour gérer le téléchargement des fichiers
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Télécharge un document huissier
     * GET /api/files/huissier/documents/{fileName}
     */
    @GetMapping("/huissier/documents/{fileName:.+}")
    public ResponseEntity<?> downloadDocument(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFile(fileName, "huissier/documents");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du téléchargement: " + e.getMessage()));
        }
    }

    /**
     * Télécharge une action huissier
     * GET /api/files/huissier/actions/{fileName}
     */
    @GetMapping("/huissier/actions/{fileName:.+}")
    public ResponseEntity<?> downloadAction(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFile(fileName, "huissier/actions");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du téléchargement: " + e.getMessage()));
        }
    }
}

