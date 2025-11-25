package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.AuditLog;
import projet.carthagecreance_backend.Service.AuditLogService;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la consultation des logs d'audit
 */
@RestController
@RequestMapping({"/api/audit-logs", "/audit-logs"})
@CrossOrigin(origins = "*")
public class AuditLogController {
    
    @Autowired
    private AuditLogService auditLogService;
    
    /**
     * Récupère les logs d'audit d'un dossier
     * GET /api/audit-logs?dossierId={id}
     */
    @GetMapping
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(required = false) Long dossierId,
            @RequestParam(required = false) Long userId) {
        try {
            List<AuditLog> logs;
            if (dossierId != null) {
                logs = auditLogService.getLogsByDossier(dossierId);
            } else if (userId != null) {
                logs = auditLogService.getLogsByUser(userId);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "dossierId ou userId est requis"));
            }
            return new ResponseEntity<>(logs, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération: " + e.getMessage()));
        }
    }
}

