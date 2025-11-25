package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.NotificationHuissier;
import projet.carthagecreance_backend.Service.NotificationHuissierService;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion des notifications huissier
 * Note: Utilise /api/huissier/notifications pour éviter le conflit avec NotificationController
 * qui utilise /api/notifications pour les notifications générales
 */
@RestController
@RequestMapping({"/api/huissier/notifications", "/huissier/notifications"})
@CrossOrigin(origins = "*")
public class NotificationHuissierController {
    
    @Autowired
    private NotificationHuissierService notificationHuissierService;
    
    /**
     * Récupère les notifications d'un dossier
     * GET /api/huissier/notifications?dossierId={id}
     * Note: Pour éviter le conflit avec NotificationController, on utilise /api/huissier/notifications
     * au lieu de /api/notifications
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestParam(required = false) Long dossierId) {
        try {
            List<NotificationHuissier> notifications;
            if (dossierId != null) {
                notifications = notificationHuissierService.getNotificationsByDossier(dossierId);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "dossierId est requis"));
            }
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération: " + e.getMessage()));
        }
    }
    
    /**
     * Acquitte une notification
     * POST /api/notifications/{id}/ack
     */
    @PostMapping("/{id}/ack")
    public ResponseEntity<?> acknowledgeNotification(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Long userId = body.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "userId est requis"));
            }
            
            notificationHuissierService.acknowledgeNotification(id, userId);
            return ResponseEntity.ok(Map.of("message", "Notification acquittée avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'acquittement: " + e.getMessage()));
        }
    }
}

