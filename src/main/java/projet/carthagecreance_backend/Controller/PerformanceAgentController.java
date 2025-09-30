package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.PerformanceAgent;
import projet.carthagecreance_backend.Service.PerformanceAgentService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrôleur REST complet pour la gestion des performances des agents
 * 
 * Ce contrôleur expose toutes les opérations CRUD et les fonctionnalités métier
 * pour la gestion des performances des agents dans le système de gestion de créances.
 * 
 * @author Système de Gestion de Créances
 * @version 1.0
 */
@RestController
@RequestMapping("/api/performance-agents")
@CrossOrigin(origins = "http://localhost:4200")
public class PerformanceAgentController {

    @Autowired
    private PerformanceAgentService performanceAgentService;

    // ==================== ENDPOINTS DE BASE (CRUD) ====================

    /**
     * Crée une nouvelle performance d'agent
     * 
     * @param performance La performance à créer (agent, période, score, objectifs)
     * @return ResponseEntity avec la performance créée (201 CREATED) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/performance-agents
     * {
     *   "agent": {"id": 1},
     *   "periode": "2024-01",
     *   "score": 85.5,
     *   "objectif": 80.0
     * }
     */
    @PostMapping
    public ResponseEntity<PerformanceAgent> createPerformanceAgent(@RequestBody PerformanceAgent performance) {
        try {
            PerformanceAgent createdPerformance = performanceAgentService.createPerformanceAgent(performance);
            return ResponseEntity.ok(createdPerformance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Met à jour une performance d'agent existante
     * 
     * @param id L'ID de la performance à modifier
     * @param performance Les nouvelles données de la performance
     * @return ResponseEntity avec la performance mise à jour (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * PUT /api/performance-agents/1
     * {
     *   "score": 90.0,
     *   "objectif": 85.0
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<PerformanceAgent> updatePerformanceAgent(@PathVariable Long id, @RequestBody PerformanceAgent performance) {
        try {
            PerformanceAgent updatedPerformance = performanceAgentService.updatePerformanceAgent(id, performance);
            return ResponseEntity.ok(updatedPerformance);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprime une performance d'agent
     * 
     * @param id L'ID de la performance à supprimer
     * @return ResponseEntity vide (204 NO_CONTENT) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * DELETE /api/performance-agents/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerformanceAgent(@PathVariable Long id) {
        try {
            performanceAgentService.deletePerformanceAgent(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère une performance d'agent par son ID
     * 
     * @param id L'ID de la performance
     * @return ResponseEntity avec la performance trouvée (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * GET /api/performance-agents/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<PerformanceAgent> getPerformanceAgentById(@PathVariable Long id) {
        try {
            PerformanceAgent performance = performanceAgentService.getPerformanceAgentById(id);
            return ResponseEntity.ok(performance);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère toutes les performances d'agents
     * 
     * @return ResponseEntity avec la liste de toutes les performances (200 OK)
     * 
     * @example
     * GET /api/performance-agents
     */
    @GetMapping
    public ResponseEntity<List<PerformanceAgent>> getAllPerformancesAgent() {
        try {
            List<PerformanceAgent> performances = performanceAgentService.getAllPerformancesAgent();
            return ResponseEntity.ok(performances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS DE FILTRAGE ====================

    /**
     * Récupère les performances par agent
     * 
     * @param agentId L'ID de l'agent
     * @return ResponseEntity avec la liste des performances de l'agent (200 OK)
     * 
     * @example
     * GET /api/performance-agents/agent/1
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<PerformanceAgent>> getPerformancesByAgent(@PathVariable Long agentId) {
        try {
            List<PerformanceAgent> performances = performanceAgentService.getPerformancesByAgent(agentId);
            return ResponseEntity.ok(performances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les performances par période
     * 
     * @param periode La période des performances (ex: "2024-01", "2024-Q1")
     * @return ResponseEntity avec la liste des performances de la période (200 OK)
     * 
     * @example
     * GET /api/performance-agents/periode/2024-01
     */
    @GetMapping("/periode/{periode}")
    public ResponseEntity<List<PerformanceAgent>> getPerformancesByPeriode(@PathVariable String periode) {
        try {
            List<PerformanceAgent> performances = performanceAgentService.getPerformancesByPeriode(periode);
            return ResponseEntity.ok(performances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère la performance par agent et période
     * 
     * @param agentId L'ID de l'agent
     * @param periode La période de la performance
     * @return ResponseEntity avec la performance trouvée (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * GET /api/performance-agents/agent/1/periode/2024-01
     */
    @GetMapping("/agent/{agentId}/periode/{periode}")
    public ResponseEntity<PerformanceAgent> getPerformanceByAgentAndPeriode(@PathVariable Long agentId, @PathVariable String periode) {
        try {
            PerformanceAgent performance = performanceAgentService.getPerformanceByAgentAndPeriode(agentId, periode);
            return ResponseEntity.ok(performance);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les performances récentes
     * 
     * @param dateDebut Date de début pour la recherche (format ISO 8601)
     * @return ResponseEntity avec la liste des performances récentes (200 OK)
     * 
     * @example
     * GET /api/performance-agents/recentes?dateDebut=2024-01-01T00:00:00
     */
    @GetMapping("/recentes")
    public ResponseEntity<List<PerformanceAgent>> getPerformancesRecent(@RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut) {
        try {
            List<PerformanceAgent> performances = performanceAgentService.getPerformancesRecent(dateDebut);
            return ResponseEntity.ok(performances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les meilleures performances
     * 
     * @return ResponseEntity avec la liste des meilleures performances triées par score (200 OK)
     * 
     * @example
     * GET /api/performance-agents/top
     */
    @GetMapping("/top")
    public ResponseEntity<List<PerformanceAgent>> getTopPerformances() {
        try {
            List<PerformanceAgent> performances = performanceAgentService.getTopPerformances();
            return ResponseEntity.ok(performances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS D'ACTIONS ====================

    /**
     * Calcule toutes les performances
     * 
     * @return ResponseEntity avec la liste de toutes les performances calculées (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/performance-agents/calculer
     */
    @PostMapping("/calculer")
    public ResponseEntity<List<PerformanceAgent>> calculerToutesPerformances() {
        try {
            List<PerformanceAgent> performances = performanceAgentService.calculerToutesPerformances();
            return ResponseEntity.ok(performances);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Calcule la performance d'un agent pour une période donnée
     * 
     * @param agentId L'ID de l'agent
     * @param periode La période de calcul (ex: "2024-01", "2024-Q1")
     * @return ResponseEntity avec la performance calculée (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/performance-agents/calculer/agent/1?periode=2024-01
     */
    @PostMapping("/calculer/agent/{agentId}")
    public ResponseEntity<PerformanceAgent> calculerPerformanceAgent(@PathVariable Long agentId, @RequestParam String periode) {
        try {
            PerformanceAgent performance = performanceAgentService.calculerPerformanceAgent(agentId, periode);
            return ResponseEntity.ok(performance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Calcule les performances pour une période donnée
     * 
     * @param periode La période de calcul (ex: "2024-01", "2024-Q1")
     * @return ResponseEntity avec la liste des performances calculées (200 OK) ou erreur (400 BAD_REQUEST)
     * 
     * @example
     * POST /api/performance-agents/calculer/periode/2024-01
     */
    @PostMapping("/calculer/periode/{periode}")
    public ResponseEntity<List<PerformanceAgent>> calculerPerformancesPeriode(@PathVariable String periode) {
        try {
            List<PerformanceAgent> performances = performanceAgentService.calculerPerformancesPeriode(periode);
            return ResponseEntity.ok(performances);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS DE STATISTIQUES ====================

    /**
     * Compte les performances par agent
     * 
     * @param agentId L'ID de l'agent
     * @return ResponseEntity avec le nombre de performances de l'agent (200 OK)
     * 
     * @example
     * GET /api/performance-agents/statistiques/agent/1
     */
    @GetMapping("/statistiques/agent/{agentId}")
    public ResponseEntity<Long> countPerformancesByAgent(@PathVariable Long agentId) {
        try {
            long count = performanceAgentService.countPerformancesByAgent(agentId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les performances par score minimum
     * 
     * @param scoreMin Le score minimum requis
     * @return ResponseEntity avec la liste des performances avec le score minimum (200 OK)
     * 
     * @example
     * GET /api/performance-agents/statistiques/score-min/80.0
     */
    @GetMapping("/statistiques/score-min/{scoreMin}")
    public ResponseEntity<List<PerformanceAgent>> getPerformancesByScoreMin(@PathVariable Double scoreMin) {
        try {
            List<PerformanceAgent> performances = performanceAgentService.getPerformancesByScoreMin(scoreMin);
            return ResponseEntity.ok(performances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les performances par agent et score minimum
     * 
     * @param agentId L'ID de l'agent
     * @param scoreMin Le score minimum requis
     * @return ResponseEntity avec la liste des performances de l'agent avec le score minimum (200 OK)
     * 
     * @example
     * GET /api/performance-agents/statistiques/agent/1/score-min/80.0
     */
    @GetMapping("/statistiques/agent/{agentId}/score-min/{scoreMin}")
    public ResponseEntity<List<PerformanceAgent>> getPerformancesByAgentAndScoreMin(@PathVariable Long agentId, @PathVariable Double scoreMin) {
        try {
            List<PerformanceAgent> performances = performanceAgentService.getPerformancesByAgentAndScoreMin(agentId, scoreMin);
            return ResponseEntity.ok(performances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
