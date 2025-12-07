package projet.carthagecreance_backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Notification;
import projet.carthagecreance_backend.Service.NotificationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST complet pour la gestion des notifications
 * 
 * Ce contrôleur expose toutes les opérations CRUD et les fonctionnalités métier
 * pour la gestion des notifications dans le système de gestion de créances.
 * 
 * @author Système de Gestion de Créances
 * @version 1.0
 */
@RestController
@RequestMapping({"/api/notifications", "/notifications"})
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    // ==================== ENDPOINTS DE BASE (CRUD) ====================

    /**
     * Crée une nouvelle notification
     * 
     * @param notification La notification à créer (utilisateur, titre, message, type)
     * @return ResponseEntity avec la notification créée (201 CREATED) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/notifications
     * {
     *   "utilisateur": {"id": 1},
     *   "titre": "Nouvelle validation",
     *   "message": "Votre dossier a été validé",
     *   "type": "DOSSIER_VALIDE"
     * }
     */
    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        try {
            Notification createdNotification = notificationService.createNotification(notification);
            return ResponseEntity.ok(createdNotification);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Met à jour une notification existante
     * 
     * @param id L'ID de la notification à modifier
     * @param notification Les nouvelles données de la notification
     * @return ResponseEntity avec la notification mise à jour (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * PUT /api/notifications/1
     * {
     *   "titre": "Titre mis à jour",
     *   "message": "Message modifié"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<Notification> updateNotification(@PathVariable Long id, @RequestBody Notification notification) {
        try {
            Notification updatedNotification = notificationService.updateNotification(id, notification);
            return ResponseEntity.ok(updatedNotification);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprime une notification
     * 
     * @param id L'ID de la notification à supprimer
     * @return ResponseEntity vide (204 NO_CONTENT) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * DELETE /api/notifications/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère une notification par son ID
     * 
     * @param id L'ID de la notification
     * @return ResponseEntity avec la notification trouvée (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * GET /api/notifications/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        try {
            Notification notification = notificationService.getNotificationById(id);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère toutes les notifications
     * 
     * @return ResponseEntity avec la liste de toutes les notifications (200 OK)
     * 
     * @example
     * GET /api/notifications
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        try {
            List<Notification> notifications = notificationService.getAllNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS DE FILTRAGE ====================

    /**
     * Récupère les notifications par utilisateur
     * 
     * @param userId L'ID de l'utilisateur
     * @return ResponseEntity avec la liste des notifications de l'utilisateur (200 OK)
     * 
     * @example
     * GET /api/notifications/user/1
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable Long userId) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByUser(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les notifications non lues par utilisateur
     * 
     * @param userId L'ID de l'utilisateur
     * @return ResponseEntity avec la liste des notifications non lues de l'utilisateur (200 OK)
     * 
     * @example
     * GET /api/notifications/user/1/non-lues
     */
    @GetMapping("/user/{userId}/non-lues")
    public ResponseEntity<List<Notification>> getNotificationsNonLuesByUser(@PathVariable Long userId) {
        try {
            List<Notification> notifications = notificationService.getNotificationsNonLuesByUser(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les notifications par type et utilisateur
     * 
     * @param userId L'ID de l'utilisateur
     * @param type Le type de notification (DOSSIER_VALIDE, ENQUETE_VALIDE, etc.)
     * @return ResponseEntity avec la liste des notifications correspondantes (200 OK)
     * 
     * @example
     * GET /api/notifications/user/1/type/DOSSIER_VALIDE
     */
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByType(@PathVariable Long userId, @PathVariable String type) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByType(userId, type);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les notifications récentes par utilisateur
     * 
     * @param userId L'ID de l'utilisateur
     * @param dateDebut Date de début pour la recherche (format ISO 8601)
     * @return ResponseEntity avec la liste des notifications récentes (200 OK)
     * 
     * @example
     * GET /api/notifications/user/1/recentes?dateDebut=2024-01-01T00:00:00
     */
    @GetMapping("/user/{userId}/recentes")
    public ResponseEntity<List<Notification>> getNotificationsRecent(@PathVariable Long userId, 
                                                                   @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut) {
        try {
            List<Notification> notifications = notificationService.getNotificationsRecent(userId, dateDebut);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les notifications par entité
     * 
     * @param entiteId L'ID de l'entité concernée
     * @param entiteType Le type d'entité (DOSSIER, ENQUETE, etc.)
     * @return ResponseEntity avec la liste des notifications de l'entité (200 OK)
     * 
     * @example
     * GET /api/notifications/entite/1/type/DOSSIER
     */
    @GetMapping("/entite/{entiteId}/type/{entiteType}")
    public ResponseEntity<List<Notification>> getNotificationsByEntite(@PathVariable Long entiteId, @PathVariable String entiteType) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByEntite(entiteId, entiteType);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS D'ACTIONS ====================

    /**
     * Marque une notification comme lue
     * 
     * @param id L'ID de la notification à marquer comme lue
     * @return ResponseEntity avec la notification mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/notifications/1/marquer-lue
     */
    @PostMapping("/{id}/marquer-lue")
    public ResponseEntity<Notification> marquerLue(@PathVariable Long id) {
        try {
            Notification notification = notificationService.marquerLue(id);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marque une notification comme non lue
     * 
     * @param id L'ID de la notification à marquer comme non lue
     * @return ResponseEntity avec la notification mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/notifications/1/marquer-non-lue
     */
    @PostMapping("/{id}/marquer-non-lue")
    public ResponseEntity<Notification> marquerNonLue(@PathVariable Long id) {
        try {
            Notification notification = notificationService.marquerNonLue(id);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     * 
     * @param userId L'ID de l'utilisateur
     * @return ResponseEntity avec le nombre de notifications marquées comme lues (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/notifications/user/1/marquer-toutes-lues
     */
    @PostMapping("/user/{userId}/marquer-toutes-lues")
    public ResponseEntity<Long> marquerToutesLues(@PathVariable Long userId) {
        try {
            long count = notificationService.marquerToutesLues(userId);
            return ResponseEntity.ok(count);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS DE STATISTIQUES ====================

    /**
     * Compte les notifications par utilisateur
     * 
     * @param userId L'ID de l'utilisateur
     * @return ResponseEntity avec le nombre de notifications de l'utilisateur (200 OK)
     * 
     * @example
     * GET /api/notifications/statistiques/user/1
     */
    @GetMapping("/statistiques/user/{userId}")
    public ResponseEntity<Long> countNotificationsByUser(@PathVariable Long userId) {
        try {
            long count = notificationService.countNotificationsByUser(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compte les notifications non lues par utilisateur
     * 
     * @param userId L'ID de l'utilisateur
     * @return ResponseEntity avec le nombre de notifications non lues (200 OK)
     * 
     * @example
     * GET /api/notifications/statistiques/non-lues/user/1
     */
    @GetMapping("/statistiques/non-lues/user/{userId}")
    public ResponseEntity<Long> countNotificationsNonLuesByUser(@PathVariable Long userId) {
        try {
            long count = notificationService.countNotificationsNonLuesByUser(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compte les notifications par type
     * 
     * @param type Le type de notification
     * @return ResponseEntity avec le nombre de notifications avec ce type (200 OK)
     * 
     * @example
     * GET /api/notifications/statistiques/type/DOSSIER_VALIDE
     */
    @GetMapping("/statistiques/type/{type}")
    public ResponseEntity<Long> countNotificationsByType(@PathVariable String type) {
        try {
            long count = notificationService.countNotificationsByType(type);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS AVANCÉS ====================

    /**
     * Envoie une notification à plusieurs utilisateurs (pour les chefs)
     * 
     * @param request Map contenant userIds, type, titre, message, entiteId, entiteType
     * @return ResponseEntity avec le nombre de notifications créées (200 OK)
     */
    @PostMapping("/envoyer-multiples")
    public ResponseEntity<Map<String, Object>> envoyerNotificationMultiples(@RequestBody Map<String, Object> request) {
        try {
            // Validation des champs requis
            if (request.get("userIds") == null) {
                logger.error("Erreur: userIds est manquant dans la requête");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le champ 'userIds' est obligatoire"));
            }
            
            if (request.get("type") == null) {
                logger.error("Erreur: type est manquant dans la requête");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le champ 'type' est obligatoire"));
            }
            
            if (request.get("titre") == null || ((String) request.get("titre")).trim().isEmpty()) {
                logger.error("Erreur: titre est manquant ou vide dans la requête");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le champ 'titre' est obligatoire"));
            }
            
            if (request.get("message") == null || ((String) request.get("message")).trim().isEmpty()) {
                logger.error("Erreur: message est manquant ou vide dans la requête");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le champ 'message' est obligatoire"));
            }
            
            // Conversion sécurisée des userIds (gère Integer et Long)
            List<Long> userIds = new ArrayList<>();
            Object userIdsObj = request.get("userIds");
            if (userIdsObj instanceof List) {
                for (Object id : (List<?>) userIdsObj) {
                    if (id instanceof Number) {
                        userIds.add(((Number) id).longValue());
                    } else {
                        logger.error("Erreur: userId invalide dans la liste: {}", id);
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", "Les userIds doivent être des nombres valides"));
                    }
                }
            } else {
                logger.error("Erreur: userIds n'est pas une liste valide: {}", userIdsObj);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le champ 'userIds' doit être une liste de nombres"));
            }
            
            if (userIds.isEmpty()) {
                logger.error("Erreur: la liste userIds est vide");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Au moins un utilisateur doit être sélectionné"));
            }
            
            // Extraction et validation des autres champs
            String typeStr = (String) request.get("type");
            String titre = (String) request.get("titre");
            String message = (String) request.get("message");
            Long entiteId = request.get("entiteId") != null ? Long.valueOf(request.get("entiteId").toString()) : null;
            String entiteTypeStr = (String) request.get("entiteType");
            
            // Validation et conversion du type de notification
            projet.carthagecreance_backend.Entity.TypeNotification type;
            try {
                type = projet.carthagecreance_backend.Entity.TypeNotification.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                logger.error("Erreur: type de notification invalide: {}", typeStr, e);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Type de notification invalide: " + typeStr));
            }
            
            // Validation et conversion du type d'entité (optionnel)
            projet.carthagecreance_backend.Entity.TypeEntite entiteType = null;
            if (entiteTypeStr != null && !entiteTypeStr.trim().isEmpty()) {
                try {
                    entiteType = projet.carthagecreance_backend.Entity.TypeEntite.valueOf(entiteTypeStr);
                } catch (IllegalArgumentException e) {
                    logger.error("Erreur: type d'entité invalide: {}", entiteTypeStr, e);
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Type d'entité invalide: " + entiteTypeStr));
                }
            }
            
            // Envoi des notifications
            int count = notificationService.envoyerNotificationAMultiplesUtilisateurs(
                userIds, type, titre, message, entiteId, entiteType);
            
            logger.info("Notifications envoyées avec succès: {} notification(s) créée(s) pour {} utilisateur(s)", 
                count, userIds.size());
            
            return ResponseEntity.ok(Map.of("count", count, "message", "Notifications envoyées avec succès"));
        } catch (ClassCastException e) {
            logger.error("Erreur de conversion de type dans la requête", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Format de données invalide: " + e.getMessage()));
        } catch (NumberFormatException e) {
            logger.error("Erreur de format numérique dans la requête", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Format numérique invalide: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'envoi de notifications", e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de l'envoi des notifications: " + e.getMessage()));
        }
    }

    /**
     * Envoie une notification à tous les agents d'un chef
     * 
     * @param chefId L'ID du chef
     * @param request Map contenant type, titre, message, entiteId, entiteType
     * @return ResponseEntity avec le nombre de notifications créées (200 OK)
     */
    @PostMapping("/chef/{chefId}/agents")
    public ResponseEntity<java.util.Map<String, Object>> envoyerNotificationAAgentsChef(
            @PathVariable Long chefId, @RequestBody java.util.Map<String, Object> request) {
        try {
            String typeStr = (String) request.get("type");
            String titre = (String) request.get("titre");
            String message = (String) request.get("message");
            Long entiteId = request.get("entiteId") != null ? Long.valueOf(request.get("entiteId").toString()) : null;
            String entiteTypeStr = (String) request.get("entiteType");
            
            projet.carthagecreance_backend.Entity.TypeNotification type = 
                projet.carthagecreance_backend.Entity.TypeNotification.valueOf(typeStr);
            projet.carthagecreance_backend.Entity.TypeEntite entiteType = 
                entiteTypeStr != null ? projet.carthagecreance_backend.Entity.TypeEntite.valueOf(entiteTypeStr) : null;
            
            int count = notificationService.envoyerNotificationAAgentsChef(
                chefId, type, titre, message, entiteId, entiteType);
            
            return ResponseEntity.ok(java.util.Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Envoie une notification à tous les utilisateurs (pour le super admin)
     * 
     * @param request Map contenant type, titre, message, entiteId, entiteType
     * @return ResponseEntity avec le nombre de notifications créées (200 OK)
     */
    @PostMapping("/envoyer-tous")
    public ResponseEntity<java.util.Map<String, Object>> envoyerNotificationATous(@RequestBody java.util.Map<String, Object> request) {
        try {
            String typeStr = (String) request.get("type");
            String titre = (String) request.get("titre");
            String message = (String) request.get("message");
            Long entiteId = request.get("entiteId") != null ? Long.valueOf(request.get("entiteId").toString()) : null;
            String entiteTypeStr = (String) request.get("entiteType");
            
            projet.carthagecreance_backend.Entity.TypeNotification type = 
                projet.carthagecreance_backend.Entity.TypeNotification.valueOf(typeStr);
            projet.carthagecreance_backend.Entity.TypeEntite entiteType = 
                entiteTypeStr != null ? projet.carthagecreance_backend.Entity.TypeEntite.valueOf(entiteTypeStr) : null;
            
            int count = notificationService.envoyerNotificationATousUtilisateurs(
                type, titre, message, entiteId, entiteType);
            
            return ResponseEntity.ok(java.util.Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Compte les notifications non lues par utilisateur (endpoint simplifié)
     * 
     * @param userId L'ID de l'utilisateur
     * @return ResponseEntity avec le nombre de notifications non lues (200 OK)
     */
    @GetMapping("/user/{userId}/count/non-lues")
    public ResponseEntity<Long> getNombreNotificationsNonLues(@PathVariable Long userId) {
        try {
            long count = notificationService.countNotificationsNonLuesByUser(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
