package projet.carthagecreance_backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur pour l'export de données par Superadmin
 */
@RestController
@RequestMapping("/api/admin/export")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminExportController {

    private static final Logger logger = LoggerFactory.getLogger(AdminExportController.class);

    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * GET /api/admin/export/rapport
     * Génère un rapport selon le type demandé (PDF, CSV, Excel)
     */
    @GetMapping("/rapport")
    public ResponseEntity<?> genererRapport(
            @RequestParam String type, // GLOBAL, DEPARTEMENT, AGENT, PERIODE
            @RequestParam(required = false) String format, // pdf, csv, excel
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            @RequestParam(required = false) String departement) {
        try {
            String formatExport = format != null ? format.toLowerCase() : "csv";
            
            switch (type.toUpperCase()) {
                case "GLOBAL":
                    return genererRapportGlobal(formatExport);
                case "DEPARTEMENT":
                    return genererRapportDepartement(formatExport, departement);
                case "AGENT":
                    return genererRapportAgent(formatExport);
                case "PERIODE":
                    LocalDate debut = dateDebut != null ? LocalDate.parse(dateDebut) : null;
                    LocalDate fin = dateFin != null ? LocalDate.parse(dateFin) : null;
                    return genererRapportPeriode(formatExport, debut, fin);
                default:
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Type de rapport invalide. Types valides: GLOBAL, DEPARTEMENT, AGENT, PERIODE"));
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du rapport: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la génération: " + e.getMessage()));
        }
    }

    private ResponseEntity<?> genererRapportGlobal(String format) {
        List<Dossier> tousDossiers = dossierRepository.findAll();
        
        // Statistiques globales
        long totalDossiers = tousDossiers.size();
        long dossiersClotures = tousDossiers.stream()
                .filter(d -> d.getDossierStatus() == DossierStatus.CLOTURE)
                .count();
        
        double montantTotalCreances = tousDossiers.stream()
                .filter(d -> d.getMontantCreance() != null)
                .mapToDouble(Dossier::getMontantCreance)
                .sum();
        
        double montantTotalRecouvre = tousDossiers.stream()
                .filter(d -> d.getMontantRecouvre() != null)
                .mapToDouble(Dossier::getMontantRecouvre)
                .sum();
        
        double tauxRecouvrement = montantTotalCreances > 0 ? 
                (montantTotalRecouvre / montantTotalCreances) * 100 : 0.0;
        
        if ("csv".equals(format)) {
            StringBuilder csv = new StringBuilder();
            csv.append("Rapport Global\n");
            csv.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
            csv.append("Total Dossiers,").append(totalDossiers).append("\n");
            csv.append("Dossiers Clôturés,").append(dossiersClotures).append("\n");
            csv.append("Montant Total Créances,").append(montantTotalCreances).append("\n");
            csv.append("Montant Total Recouvré,").append(montantTotalRecouvre).append("\n");
            csv.append("Taux Recouvrement,").append(String.format("%.2f", tauxRecouvrement)).append("%\n");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", 
                    "rapport_global_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csv.toString());
        }
        
        // Pour PDF et Excel, retourner les données JSON (à implémenter avec une bibliothèque)
        Map<String, Object> rapport = Map.of(
                "type", "GLOBAL",
                "dateGeneration", LocalDateTime.now().toString(),
                "statistiques", Map.of(
                        "totalDossiers", totalDossiers,
                        "dossiersClotures", dossiersClotures,
                        "montantTotalCreances", montantTotalCreances,
                        "montantTotalRecouvre", montantTotalRecouvre,
                        "tauxRecouvrement", Math.round(tauxRecouvrement * 100.0) / 100.0
                )
        );
        
        return ResponseEntity.ok(rapport);
    }

    private ResponseEntity<?> genererRapportDepartement(String format, String departement) {
        List<Dossier> dossiers = dossierRepository.findAll();
        
        if (departement != null && !departement.isEmpty()) {
            TypeRecouvrement type = TypeRecouvrement.valueOf(departement.toUpperCase());
            dossiers = dossiers.stream()
                    .filter(d -> d.getTypeRecouvrement() == type)
                    .collect(Collectors.toList());
        }
        
        // Statistiques par département
        Map<String, Object> stats = calculerStatsDepartement(dossiers);
        
        if ("csv".equals(format)) {
            StringBuilder csv = new StringBuilder();
            csv.append("Rapport Département: ").append(departement != null ? departement : "TOUS").append("\n");
            csv.append("Date: ").append(LocalDateTime.now().toString()).append("\n\n");
            csv.append("Statistiques\n");
            stats.forEach((k, v) -> csv.append(k).append(",").append(v).append("\n"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", 
                    "rapport_departement_" + (departement != null ? departement : "all") + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csv.toString());
        }
        
        return ResponseEntity.ok(Map.of("type", "DEPARTEMENT", "departement", departement, "statistiques", stats));
    }

    private ResponseEntity<?> genererRapportAgent(String format) {
        List<Utilisateur> agents = utilisateurRepository.findAll().stream()
                .filter(u -> u.getRoleUtilisateur() != null && 
                        u.getRoleUtilisateur().name().contains("AGENT"))
                .collect(Collectors.toList());
        
        List<Map<String, Object>> performances = agents.stream()
                .map(agent -> {
                    List<Dossier> dossiers = dossierRepository.findByAgentResponsableId(agent.getId());
                    dossiers.addAll(dossierRepository.findByAgentCreateurId(agent.getId()));
                    
                    long dossiersTraites = dossiers.stream().distinct().count();
                    long dossiersClotures = dossiers.stream()
                            .filter(d -> d.getDossierStatus() == DossierStatus.CLOTURE)
                            .count();
                    
                    return Map.<String, Object>of(
                            "agent", agent.getNom() + " " + agent.getPrenom(),
                            "email", agent.getEmail(),
                            "dossiersTraites", dossiersTraites,
                            "dossiersClotures", dossiersClotures
                    );
                })
                .collect(Collectors.toList());
        
        if ("csv".equals(format)) {
            StringBuilder csv = new StringBuilder();
            csv.append("Rapport Agents\n");
            csv.append("Date: ").append(LocalDateTime.now().toString()).append("\n\n");
            csv.append("Agent,Email,Dossiers Traités,Dossiers Clôturés\n");
            performances.forEach(p -> csv.append(p.get("agent")).append(",")
                    .append(p.get("email")).append(",")
                    .append(p.get("dossiersTraites")).append(",")
                    .append(p.get("dossiersClotures")).append("\n"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", 
                    "rapport_agents_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csv.toString());
        }
        
        return ResponseEntity.ok(Map.of("type", "AGENT", "performances", performances));
    }

    private ResponseEntity<?> genererRapportPeriode(String format, LocalDate dateDebut, LocalDate dateFin) {
        List<Dossier> dossiers = dossierRepository.findAll();
        
        if (dateDebut != null) {
            java.util.Date debutDate = java.sql.Date.valueOf(dateDebut);
            dossiers = dossiers.stream()
                    .filter(d -> d.getDateCreation() != null && d.getDateCreation().after(debutDate))
                    .collect(Collectors.toList());
        }
        
        if (dateFin != null) {
            java.util.Date finDate = java.sql.Date.valueOf(dateFin);
            dossiers = dossiers.stream()
                    .filter(d -> d.getDateCreation() != null && d.getDateCreation().before(finDate))
                    .collect(Collectors.toList());
        }
        
        Map<String, Object> stats = calculerStatsDepartement(dossiers);
        
        if ("csv".equals(format)) {
            StringBuilder csv = new StringBuilder();
            csv.append("Rapport Période\n");
            csv.append("Du: ").append(dateDebut != null ? dateDebut.toString() : "Début").append("\n");
            csv.append("Au: ").append(dateFin != null ? dateFin.toString() : "Fin").append("\n\n");
            csv.append("Statistiques\n");
            stats.forEach((k, v) -> csv.append(k).append(",").append(v).append("\n"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", 
                    "rapport_periode_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csv.toString());
        }
        
        return ResponseEntity.ok(Map.of("type", "PERIODE", "dateDebut", dateDebut, "dateFin", dateFin, "statistiques", stats));
    }

    private Map<String, Object> calculerStatsDepartement(List<Dossier> dossiers) {
        long total = dossiers.size();
        long clotures = dossiers.stream()
                .filter(d -> d.getDossierStatus() == DossierStatus.CLOTURE)
                .count();
        
        double montantCreances = dossiers.stream()
                .filter(d -> d.getMontantCreance() != null)
                .mapToDouble(Dossier::getMontantCreance)
                .sum();
        
        double montantRecouvre = dossiers.stream()
                .filter(d -> d.getMontantRecouvre() != null)
                .mapToDouble(Dossier::getMontantRecouvre)
                .sum();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDossiers", total);
        stats.put("dossiersClotures", clotures);
        stats.put("montantCreances", montantCreances);
        stats.put("montantRecouvre", montantRecouvre);
        stats.put("tauxRecouvrement", montantCreances > 0 ? (montantRecouvre / montantCreances * 100) : 0.0);
        
        return stats;
    }
}

