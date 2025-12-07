package projet.carthagecreance_backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.RoleUtilisateur;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Service.StatistiqueService;
import projet.carthagecreance_backend.Service.Impl.StatistiqueServiceImpl;
import projet.carthagecreance_backend.SecurityServices.UserExtractionService;
import io.jsonwebtoken.ExpiredJwtException;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

/**
 * Contrôleur REST pour les statistiques
 * Fournit des endpoints différents selon le rôle de l'utilisateur
 */
@RestController
@RequestMapping("/api/statistiques")
@CrossOrigin(origins = "*")
public class StatistiqueController {

    private static final Logger logger = LoggerFactory.getLogger(StatistiqueController.class);

    @Autowired
    private StatistiqueService statistiqueService;

    @Autowired
    private StatistiqueServiceImpl statistiqueServiceImpl;

    @Autowired
    private UserExtractionService userExtractionService;

    /**
     * Endpoint pour SuperAdmin et Chef Juridique : Toutes les statistiques globales
     * Note: Pour les chefs, les statistiques sont filtrées par département si nécessaire
     */
    @GetMapping("/globales")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesGlobales() {
        try {
            logger.info("Récupération des statistiques globales");
            Map<String, Object> stats = statistiqueService.getStatistiquesGlobales();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques globales: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour SuperAdmin : Statistiques par période
     */
    @GetMapping("/periode")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistiquesParPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        try {
            logger.info("Récupération des statistiques pour la période {} - {}", dateDebut, dateFin);
            Map<String, Object> stats = statistiqueService.getStatistiquesParPeriode(dateDebut, dateFin);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques par période: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour SuperAdmin : Statistiques de tous les chefs
     */
    @GetMapping("/chefs")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistiquesTousChefs() {
        try {
            logger.info("Récupération des statistiques de tous les chefs");
            Map<String, Object> stats = statistiqueService.getStatistiquesTousChefs();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques des chefs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour SuperAdmin : Statistiques des dossiers
     */
    @GetMapping("/dossiers")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistiquesDossiers() {
        try {
            logger.info("Récupération des statistiques des dossiers");
            Map<String, Object> stats = statistiqueService.getStatistiquesDossiers();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques des dossiers: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour SuperAdmin et Chef Amiable : Statistiques des actions amiables
     */
    @GetMapping("/actions-amiables")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesActionsAmiables(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            logger.info("Récupération des statistiques des actions amiables");
            Map<String, Object> stats = statistiqueService.getStatistiquesActionsAmiables();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques des actions amiables: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour SuperAdmin et Chef Juridique : Statistiques des audiences
     */
    @GetMapping("/audiences")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesAudiences() {
        try {
            logger.info("Récupération des statistiques des audiences");
            Map<String, Object> stats = statistiqueService.getStatistiquesAudiences();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques des audiences: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour SuperAdmin : Statistiques des tâches
     */
    @GetMapping("/taches")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistiquesTaches() {
        try {
            logger.info("Récupération des statistiques des tâches");
            Map<String, Object> stats = statistiqueService.getStatistiquesTaches();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques des tâches: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour SuperAdmin et Chef Finance : Statistiques financières
     */
    @GetMapping("/financieres")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CHEF_DEPARTEMENT_FINANCE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesFinancieres() {
        try {
            logger.info("Récupération des statistiques financières");
            Map<String, Object> stats = statistiqueService.getStatistiquesFinancieres();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques financières: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour les Chefs : Statistiques de leur département
     */
    @GetMapping("/departement")
    @PreAuthorize("hasAnyRole('CHEF_DEPARTEMENT_DOSSIER', 'CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE', 'CHEF_DEPARTEMENT_FINANCE')")
    public ResponseEntity<?> getStatistiquesDepartement(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur chef;
            try {
                chef = userExtractionService.extractUserFromToken(authHeader);
            } catch (ExpiredJwtException e) {
                logger.error("Token JWT expiré lors de la récupération des statistiques du département: {}", e.getMessage());
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Token expiré",
                    "message", "Votre session a expiré. Veuillez vous reconnecter.",
                    "code", "TOKEN_EXPIRED",
                    "expiredAt", e.getClaims().getExpiration().toString(),
                    "currentTime", new Date().toString()
                ));
            }
            
            if (chef == null) {
                logger.warn("Impossible d'extraire l'utilisateur depuis le token pour les statistiques du département");
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Token invalide",
                    "message", "Impossible d'extraire l'utilisateur depuis le token",
                    "code", "USER_NOT_FOUND"
                ));
            }
            
            Long chefId = chef.getId();
            logger.info("Récupération des statistiques du département pour le chef {}", chefId);
            
            // Récupérer le rôle du chef pour filtrer par département
            RoleUtilisateur roleChef = chef.getRoleUtilisateur();
            Map<String, Object> stats = statistiqueServiceImpl.getStatistiquesParDepartement(roleChef);
            
            // Ajouter aussi les statistiques du chef et de ses agents
            Map<String, Object> statsChef = statistiqueService.getStatistiquesChef(chefId);
            stats.put("chef", statsChef);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques du département: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Erreur interne du serveur",
                "message", "Une erreur est survenue lors de la récupération des statistiques",
                "code", "INTERNAL_SERVER_ERROR"
            ));
        }
    }

    /**
     * Endpoint pour les Chefs : Statistiques de leurs agents
     */
    @GetMapping("/mes-agents")
    @PreAuthorize("hasAnyRole('CHEF_DEPARTEMENT_DOSSIER', 'CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE', 'CHEF_DEPARTEMENT_FINANCE')")
    public ResponseEntity<?> getStatistiquesMesAgents(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur chef;
            try {
                chef = userExtractionService.extractUserFromToken(authHeader);
            } catch (ExpiredJwtException e) {
                logger.error("Token JWT expiré lors de la récupération des statistiques des agents: {}", e.getMessage());
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Token expiré",
                    "message", "Votre session a expiré. Veuillez vous reconnecter.",
                    "code", "TOKEN_EXPIRED",
                    "expiredAt", e.getClaims().getExpiration().toString(),
                    "currentTime", new Date().toString()
                ));
            }
            
            if (chef == null) {
                logger.warn("Impossible d'extraire l'utilisateur depuis le token pour les statistiques des agents");
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Token invalide",
                    "message", "Impossible d'extraire l'utilisateur depuis le token",
                    "code", "USER_NOT_FOUND"
                ));
            }
            
            Long chefId = chef.getId();
            logger.info("Récupération des statistiques des agents du chef {}", chefId);
            Map<String, Object> stats = statistiqueService.getStatistiquesChef(chefId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques des agents: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Erreur interne du serveur",
                "message", "Une erreur est survenue lors de la récupération des statistiques",
                "code", "INTERNAL_SERVER_ERROR"
            ));
        }
    }

    /**
     * Endpoint pour les Agents : Statistiques de leurs dossiers
     */
    @GetMapping("/mes-dossiers")
    @PreAuthorize("hasAnyRole('AGENT_DOSSIER', 'AGENT_RECOUVREMENT_AMIABLE', 'AGENT_RECOUVREMENT_JURIDIQUE', 'AGENT_FINANCE')")
    public ResponseEntity<?> getStatistiquesMesDossiers(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur agent;
            try {
                agent = userExtractionService.extractUserFromToken(authHeader);
            } catch (ExpiredJwtException e) {
                logger.error("Token JWT expiré lors de la récupération des statistiques de l'agent: {}", e.getMessage());
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Token expiré",
                    "message", "Votre session a expiré. Veuillez vous reconnecter.",
                    "code", "TOKEN_EXPIRED",
                    "expiredAt", e.getClaims().getExpiration().toString(),
                    "currentTime", new Date().toString()
                ));
            }
            
            if (agent == null) {
                logger.warn("Impossible d'extraire l'utilisateur depuis le token pour les statistiques de l'agent");
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Token invalide",
                    "message", "Impossible d'extraire l'utilisateur depuis le token",
                    "code", "USER_NOT_FOUND"
                ));
            }
            
            Long agentId = agent.getId();
            logger.info("Récupération des statistiques de l'agent {}", agentId);
            Map<String, Object> stats = statistiqueService.getStatistiquesAgent(agentId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques de l'agent: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Erreur interne du serveur",
                "message", "Une erreur est survenue lors de la récupération des statistiques",
                "code", "INTERNAL_SERVER_ERROR"
            ));
        }
    }

    /**
     * Endpoint pour obtenir les statistiques d'un agent spécifique (pour les chefs)
     */
    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasAnyRole('CHEF_DEPARTEMENT_DOSSIER', 'CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE', 'CHEF_DEPARTEMENT_FINANCE', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistiquesAgent(@PathVariable Long agentId) {
        try {
            logger.info("Récupération des statistiques de l'agent {}", agentId);
            Map<String, Object> stats = statistiqueService.getStatistiquesAgent(agentId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques de l'agent {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour forcer le recalcul des statistiques (SuperAdmin uniquement)
     */
    @PostMapping("/recalculer")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> recalculerStatistiques() {
        try {
            logger.info("Recalcul manuel des statistiques demandé");
            statistiqueServiceImpl.calculerEtStockerStatistiquesGlobales();
            return ResponseEntity.ok(Map.of("message", "Statistiques recalculées avec succès"));
        } catch (Exception e) {
            logger.error("Erreur lors du recalcul des statistiques: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors du recalcul: " + e.getMessage()));
        }
    }

    /**
     * ✅ NOUVEAU : Endpoint pour les statistiques de recouvrement par phase
     * GET /api/statistiques/recouvrement-par-phase
     */
    @GetMapping("/recouvrement-par-phase")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE', 'CHEF_DEPARTEMENT_FINANCE')")
    public ResponseEntity<Map<String, Object>> getStatistiquesRecouvrementParPhase() {
        try {
            logger.info("Récupération des statistiques de recouvrement par phase");
            Map<String, Object> stats = statistiqueService.getStatistiquesRecouvrementParPhase();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques par phase: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ✅ NOUVEAU : Endpoint pour les statistiques de recouvrement par phase pour un département
     * GET /api/statistiques/recouvrement-par-phase/departement
     */
    @GetMapping("/recouvrement-par-phase/departement")
    @PreAuthorize("hasAnyRole('CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE', 'CHEF_DEPARTEMENT_FINANCE', 'SUPER_ADMIN')")
    public ResponseEntity<?> getStatistiquesRecouvrementParPhaseDepartement(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur utilisateur;
            try {
                utilisateur = userExtractionService.extractUserFromToken(authHeader);
            } catch (ExpiredJwtException e) {
                logger.error("Token JWT expiré lors de la récupération des statistiques par phase département: {}", e.getMessage());
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Token expiré",
                    "message", "Votre session a expiré. Veuillez vous reconnecter.",
                    "code", "TOKEN_EXPIRED",
                    "expiredAt", e.getClaims().getExpiration().toString(),
                    "currentTime", new Date().toString()
                ));
            }
            
            if (utilisateur == null) {
                logger.warn("Impossible d'extraire l'utilisateur depuis le token pour les statistiques par phase département");
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Token invalide",
                    "message", "Impossible d'extraire l'utilisateur depuis le token",
                    "code", "USER_NOT_FOUND"
                ));
            }
            
            logger.info("Récupération des statistiques de recouvrement par phase pour le département de {}", utilisateur.getId());
            Map<String, Object> stats = statistiqueService.getStatistiquesRecouvrementParPhaseDepartement(utilisateur.getRoleUtilisateur());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques par phase département: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Erreur interne du serveur",
                "message", "Une erreur est survenue lors de la récupération des statistiques",
                "code", "INTERNAL_SERVER_ERROR"
            ));
        }
    }
}

