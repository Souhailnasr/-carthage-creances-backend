package projet.carthagecreance_backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.DTO.FinanceAlertDTO;
import projet.carthagecreance_backend.DTO.FinanceStatsDTO;
import projet.carthagecreance_backend.Service.FinanceAnalyticsService;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finances/analytics")
@CrossOrigin(origins = "*")
public class FinanceAnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(FinanceAnalyticsController.class);

    @Autowired
    private FinanceAnalyticsService financeAnalyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<FinanceStatsDTO> getDashboardStats() {
        try {
            FinanceStatsDTO stats = financeAnalyticsService.getDashboardStats();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des statistiques: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<FinanceStatsDTO> getStatsByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            FinanceStatsDTO stats = financeAnalyticsService.getStatsByDateRange(
                    startDate != null ? startDate : LocalDate.now().minusMonths(12),
                    endDate != null ? endDate : LocalDate.now()
            );
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des statistiques: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<FinanceAlertDTO>> getAlerts(
            @RequestParam(required = false) String niveau,
            @RequestParam(required = false) String phase) {
        try {
            List<FinanceAlertDTO> alerts = financeAnalyticsService.getAlerts(niveau, phase);
            return new ResponseEntity<>(alerts, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des alertes: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/alerts/dossier/{dossierId}")
    public ResponseEntity<List<FinanceAlertDTO>> getAlertsByDossier(@PathVariable Long dossierId) {
        try {
            List<FinanceAlertDTO> alerts = financeAnalyticsService.getAlertsByDossier(dossierId);
            return new ResponseEntity<>(alerts, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des alertes: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/repartition")
    public ResponseEntity<Map<String, Object>> getRepartitionFrais() {
        try {
            Map<String, Object> repartition = financeAnalyticsService.getRepartitionFrais();
            return new ResponseEntity<>(repartition, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul de la répartition: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/evolution")
    public ResponseEntity<Map<String, Object>> getEvolutionMensuelle(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Map<String, Object> evolution = financeAnalyticsService.getEvolutionMensuelle(startDate, endDate);
            return new ResponseEntity<>(evolution, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul de l'évolution: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/roi-agents")
    public ResponseEntity<List<Map<String, Object>>> getAgentRoiClassement() {
        try {
            List<Map<String, Object>> classement = financeAnalyticsService.getAgentRoiClassement();
            return new ResponseEntity<>(classement, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul du ROI: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/dossier/{dossierId}/stats")
    public ResponseEntity<Map<String, Object>> getStatistiquesDossier(@PathVariable Long dossierId) {
        try {
            Map<String, Object> stats = financeAnalyticsService.getStatistiquesDossier(dossierId);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des statistiques: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors du calcul des statistiques",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }

    @GetMapping("/roi/agent/{agentId}")
    public ResponseEntity<Double> calculerRoiAgent(@PathVariable Long agentId) {
        try {
            Double roi = financeAnalyticsService.calculerRoiAgent(agentId);
            return new ResponseEntity<>(roi, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul du ROI: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/insights")
    public ResponseEntity<List<Map<String, Object>>> getInsights() {
        try {
            List<Map<String, Object>> insights = financeAnalyticsService.getInsights();
            return new ResponseEntity<>(insights, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des insights: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/insights/{insightId}/traite")
    public ResponseEntity<?> marquerInsightTraite(@PathVariable Long insightId) {
        try {
            financeAnalyticsService.marquerInsightTraite(insightId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors du marquage: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors du marquage",
                    "message", e.getMessage(),
                    "timestamp", new Date().toString()
            ));
        }
    }
    
    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exporterRapportExcel(
            @RequestParam String typeRapport,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Map<String, Object> filtres) {
        try {
            byte[] excelBytes = financeAnalyticsService.exporterRapportExcel(
                    typeRapport, startDate, endDate, filtres != null ? filtres : new java.util.HashMap<>());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    "rapport_" + typeRapport + "_" + startDate + "_" + endDate + ".xlsx");
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de l'export Excel: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

