package projet.carthagecreance_backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.SecurityServices.UserExtractionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur pour le dashboard Superadmin
 * Fournit les KPIs, graphiques et alertes
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);

    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private UserExtractionService userExtractionService;

    /**
     * GET /api/admin/dashboard/kpis
     * Retourne les KPIs principaux du dashboard
     */
    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKPIs() {
        try {
            logger.info("Récupération des KPIs du dashboard Superadmin");
            
            List<Dossier> tousDossiers = dossierRepository.findAll();
            List<Utilisateur> tousUtilisateurs = utilisateurRepository.findAll();
            
            // 1. Total créances en cours
            double montantCreancesEnCours = tousDossiers.stream()
                    .filter(d -> d.getDossierStatus() != null &&
                                d.getDossierStatus() != DossierStatus.CLOTURE &&
                                d.getArchive() != null && !d.getArchive() &&
                                d.getMontantCreance() != null)
                    .mapToDouble(Dossier::getMontantCreance)
                    .sum();
            
            // Calcul variation mois précédent (simplifié)
            LocalDate maintenant = LocalDate.now();
            LocalDate moisPrecedent = maintenant.minusMonths(1);
            double montantMoisPrecedent = tousDossiers.stream()
                    .filter(d -> d.getDateCreation() != null &&
                                d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isBefore(moisPrecedent.atStartOfDay().toLocalDate()) &&
                                d.getDossierStatus() != DossierStatus.CLOTURE &&
                                d.getMontantCreance() != null)
                    .mapToDouble(Dossier::getMontantCreance)
                    .sum();
            
            double variation = montantMoisPrecedent > 0 ? 
                    ((montantCreancesEnCours - montantMoisPrecedent) / montantMoisPrecedent) * 100 : 0.0;
            
            Map<String, Object> totalCreancesEnCours = Map.of(
                    "montant", montantCreancesEnCours,
                    "variationMoisPrecedent", Math.round(variation * 100.0) / 100.0,
                    "tendance", variation >= 0 ? "up" : "down"
            );
            
            // 2. Taux recouvrement global
            double montantTotalCreances = tousDossiers.stream()
                    .filter(d -> d.getMontantCreance() != null)
                    .mapToDouble(Dossier::getMontantCreance)
                    .sum();
            
            double montantRecouvre = tousDossiers.stream()
                    .filter(d -> d.getMontantRecouvre() != null)
                    .mapToDouble(Dossier::getMontantRecouvre)
                    .sum();
            
            double tauxRecouvrement = montantTotalCreances > 0 ? 
                    (montantRecouvre / montantTotalCreances) * 100 : 0.0;
            
            Map<String, Object> tauxRecouvrementGlobal = Map.of(
                    "pourcentage", Math.round(tauxRecouvrement * 100.0) / 100.0,
                    "objectif", 75.0,
                    "performance", tauxRecouvrement >= 75 ? "bon" : tauxRecouvrement >= 50 ? "moyen" : "faible"
            );
            
            // 3. Dossiers en retard
            LocalDateTime maintenantDateTime = LocalDateTime.now();
            long plus30j = tousDossiers.stream()
                    .filter(d -> d.getDossierStatus() != DossierStatus.CLOTURE &&
                                d.getDateCreation() != null &&
                                ChronoUnit.DAYS.between(
                                        d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                                        maintenantDateTime) > 30)
                    .count();
            
            long plus60j = tousDossiers.stream()
                    .filter(d -> d.getDossierStatus() != DossierStatus.CLOTURE &&
                                d.getDateCreation() != null &&
                                ChronoUnit.DAYS.between(
                                        d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                                        maintenantDateTime) > 60)
                    .count();
            
            long plus90j = tousDossiers.stream()
                    .filter(d -> d.getDossierStatus() != DossierStatus.CLOTURE &&
                                d.getDateCreation() != null &&
                                ChronoUnit.DAYS.between(
                                        d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                                        maintenantDateTime) > 90)
                    .count();
            
            Map<String, Object> dossiersEnRetard = Map.of(
                    "plus30j", plus30j,
                    "plus60j", plus60j,
                    "plus90j", plus90j
            );
            
            // 4. Agents actifs
            long agentsActifs = tousUtilisateurs.stream()
                    .filter(u -> u.getDerniereConnexion() != null &&
                                u.getDerniereConnexion().isAfter(LocalDateTime.now().minusDays(30)))
                    .count();
            
            long totalAgents = tousUtilisateurs.stream()
                    .filter(u -> u.getRoleUtilisateur() != null &&
                                u.getRoleUtilisateur().name().contains("AGENT"))
                    .count();
            
            double tauxActivite = totalAgents > 0 ? (agentsActifs * 100.0 / totalAgents) : 0.0;
            
            Map<String, Object> agentsActifsMap = Map.of(
                    "actifs", agentsActifs,
                    "total", totalAgents,
                    "tauxActivite", Math.round(tauxActivite * 100.0) / 100.0
            );
            
            // 5. Nouveaux dossiers
            LocalDate date7j = LocalDate.now().minusDays(7);
            LocalDate date30j = LocalDate.now().minusDays(30);
            
            long nouveaux7j = tousDossiers.stream()
                    .filter(d -> d.getDateCreation() != null &&
                                d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isAfter(date7j))
                    .count();
            
            long nouveaux30j = tousDossiers.stream()
                    .filter(d -> d.getDateCreation() != null &&
                                d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isAfter(date30j))
                    .count();
            
            Map<String, Object> nouveauxDossiers = Map.of(
                    "7j", nouveaux7j,
                    "30j", nouveaux30j,
                    "tendance", nouveaux7j > (nouveaux30j / 4) ? "up" : "down"
            );
            
            // 6. Performance moyenne par département
            Map<String, Double> performanceParDepartement = new HashMap<>();
            for (TypeRecouvrement type : TypeRecouvrement.values()) {
                List<Dossier> dossiersType = tousDossiers.stream()
                        .filter(d -> d.getTypeRecouvrement() == type)
                        .collect(Collectors.toList());
                
                if (!dossiersType.isEmpty()) {
                    double tauxRecouvrementType = dossiersType.stream()
                            .filter(d -> d.getMontantCreance() != null && d.getMontantRecouvre() != null)
                            .mapToDouble(d -> d.getMontantRecouvre() / d.getMontantCreance() * 100)
                            .average()
                            .orElse(0.0);
                    
                    performanceParDepartement.put(type.name(), Math.round(tauxRecouvrementType * 100.0) / 100.0);
                }
            }
            
            double scoreGlobal = performanceParDepartement.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            
            Map<String, Object> performanceMoyenne = Map.of(
                    "scoreGlobal", Math.round(scoreGlobal * 100.0) / 100.0,
                    "parDepartement", performanceParDepartement
            );
            
            Map<String, Object> response = Map.of(
                    "totalCreancesEnCours", totalCreancesEnCours,
                    "tauxRecouvrementGlobal", tauxRecouvrementGlobal,
                    "dossiersEnRetard", dossiersEnRetard,
                    "agentsActifs", agentsActifsMap,
                    "nouveauxDossiers", nouveauxDossiers,
                    "performanceMoyenne", performanceMoyenne
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des KPIs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des KPIs: " + e.getMessage()));
        }
    }

    /**
     * GET /api/admin/dashboard/graphiques/evolution-mensuelle
     * Retourne l'évolution mensuelle des montants recouvrés
     */
    @GetMapping("/graphiques/evolution-mensuelle")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getEvolutionMensuelle(
            @RequestParam(required = false) Integer annee) {
        try {
            int anneeCourante = annee != null ? annee : LocalDate.now().getYear();
            int anneePrecedente = anneeCourante - 1;
            
            List<Dossier> tousDossiers = dossierRepository.findAll();
            
            // Grouper par mois pour année courante
            Map<String, Double> evolutionCourante = new TreeMap<>();
            Map<String, Double> evolutionPrecedente = new TreeMap<>();
            
            for (Dossier d : tousDossiers) {
                if (d.getDateCloture() != null && d.getMontantRecouvre() != null) {
                    LocalDate dateCloture = d.getDateCloture().toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    
                    String mois = dateCloture.getYear() + "-" + 
                            String.format("%02d", dateCloture.getMonthValue());
                    
                    if (dateCloture.getYear() == anneeCourante) {
                        evolutionCourante.put(mois, 
                                evolutionCourante.getOrDefault(mois, 0.0) + d.getMontantRecouvre());
                    } else if (dateCloture.getYear() == anneePrecedente) {
                        evolutionPrecedente.put(mois,
                                evolutionPrecedente.getOrDefault(mois, 0.0) + d.getMontantRecouvre());
                    }
                }
            }
            
            List<Map<String, Object>> anneeCouranteList = evolutionCourante.entrySet().stream()
                    .map(e -> Map.<String, Object>of("mois", e.getKey(), "montantRecouvre", e.getValue()))
                    .collect(Collectors.toList());
            
            List<Map<String, Object>> anneePrecedenteList = evolutionPrecedente.entrySet().stream()
                    .map(e -> Map.<String, Object>of("mois", e.getKey(), "montantRecouvre", e.getValue()))
                    .collect(Collectors.toList());
            
            Map<String, List<Map<String, Object>>> response = Map.of(
                    "anneeCourante", anneeCouranteList,
                    "anneePrecedente", anneePrecedenteList
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'évolution mensuelle: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/admin/dashboard/graphiques/repartition-statut
     * Retourne la répartition des dossiers par statut
     */
    @GetMapping("/graphiques/repartition-statut")
    public ResponseEntity<Map<String, Long>> getRepartitionStatut() {
        try {
            List<Dossier> tousDossiers = dossierRepository.findAll();
            
            Map<String, Long> repartition = tousDossiers.stream()
                    .collect(Collectors.groupingBy(
                            d -> d.getDossierStatus() != null ? d.getDossierStatus().name() : "INCONNU",
                            Collectors.counting()
                    ));
            
            return ResponseEntity.ok(repartition);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la répartition: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/admin/dashboard/graphiques/performance-departements
     * Retourne la performance par département
     */
    @GetMapping("/graphiques/performance-departements")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getPerformanceDepartements() {
        try {
            List<Dossier> tousDossiers = dossierRepository.findAll();
            
            List<Map<String, Object>> departements = new ArrayList<>();
            
            for (TypeRecouvrement type : TypeRecouvrement.values()) {
                List<Dossier> dossiersType = tousDossiers.stream()
                        .filter(d -> d.getTypeRecouvrement() == type)
                        .collect(Collectors.toList());
                
                if (!dossiersType.isEmpty()) {
                    double tauxRecouvrement = dossiersType.stream()
                            .filter(d -> d.getMontantCreance() != null && d.getMontantRecouvre() != null)
                            .mapToDouble(d -> d.getMontantRecouvre() / d.getMontantCreance() * 100)
                            .average()
                            .orElse(0.0);
                    
                    long dossiersTraites = dossiersType.stream()
                            .filter(d -> d.getDossierStatus() == DossierStatus.CLOTURE)
                            .count();
                    
                    double tempsMoyen = dossiersType.stream()
                            .filter(d -> d.getDateCloture() != null && d.getDateCreation() != null)
                            .mapToDouble(d -> ChronoUnit.DAYS.between(
                                    d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                                    d.getDateCloture().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                            .average()
                            .orElse(0.0);
                    
                    departements.add(Map.of(
                            "nom", type.name(),
                            "tauxRecouvrement", Math.round(tauxRecouvrement * 100.0) / 100.0,
                            "dossiersTraites", dossiersTraites,
                            "tempsMoyenTraitement", Math.round(tempsMoyen * 100.0) / 100.0
                    ));
                }
            }
            
            return ResponseEntity.ok(Map.of("departements", departements));
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la performance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/admin/dashboard/graphiques/repartition-montant
     * Retourne la répartition des dossiers par tranches de montant
     */
    @GetMapping("/graphiques/repartition-montant")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getRepartitionMontant() {
        try {
            List<Dossier> tousDossiers = dossierRepository.findAll();
            
            long moins1000 = tousDossiers.stream()
                    .filter(d -> d.getMontantCreance() != null && d.getMontantCreance() < 1000)
                    .count();
            
            long entre1000et5000 = tousDossiers.stream()
                    .filter(d -> d.getMontantCreance() != null && 
                                d.getMontantCreance() >= 1000 && d.getMontantCreance() < 5000)
                    .count();
            
            long entre5000et10000 = tousDossiers.stream()
                    .filter(d -> d.getMontantCreance() != null && 
                                d.getMontantCreance() >= 5000 && d.getMontantCreance() < 10000)
                    .count();
            
            long plus10000 = tousDossiers.stream()
                    .filter(d -> d.getMontantCreance() != null && d.getMontantCreance() >= 10000)
                    .count();
            
            List<Map<String, Object>> tranches = List.of(
                    Map.of("tranche", "<1000", "nombre", moins1000),
                    Map.of("tranche", "1000-5000", "nombre", entre1000et5000),
                    Map.of("tranche", "5000-10000", "nombre", entre5000et10000),
                    Map.of("tranche", ">10000", "nombre", plus10000)
            );
            
            return ResponseEntity.ok(Map.of("tranches", tranches));
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la répartition: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/admin/dashboard/activite-recente
     * Retourne l'activité récente avec pagination
     */
    @GetMapping("/activite-recente")
    public ResponseEntity<Map<String, Object>> getActiviteRecente(
            @RequestParam(defaultValue = "24h") String periode,
            @RequestParam(required = false) String typeAction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // Endpoint désactivé - AuditLog supprimé
            // Retourner une liste vide pour compatibilité
            Map<String, Object> response = Map.of(
                    "content", new ArrayList<>(),
                    "totalElements", 0,
                    "totalPages", 0
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'activité récente: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/admin/dashboard/alertes
     * Retourne les alertes (dossiers attention, agents sous-performance, départements rééquilibrage)
     */
    @GetMapping("/alertes")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getAlertes() {
        try {
            List<Dossier> tousDossiers = dossierRepository.findAll();
            List<Utilisateur> tousUtilisateurs = utilisateurRepository.findAll();
            
            // Dossiers nécessitant attention
            List<Map<String, Object>> dossiersAttention = new ArrayList<>();
            
            // Score IA faible
            tousDossiers.stream()
                    .filter(d -> d.getRiskScore() != null && d.getRiskScore() < 30)
                    .forEach(d -> dossiersAttention.add(Map.of(
                            "id", d.getId(),
                            "reference", d.getNumeroDossier() != null ? d.getNumeroDossier() : "N/A",
                            "raison", "Score IA faible",
                            "scoreIA", d.getRiskScore()
                    )));
            
            // Retard > 90j
            LocalDateTime maintenant = LocalDateTime.now();
            tousDossiers.stream()
                    .filter(d -> d.getDossierStatus() != DossierStatus.CLOTURE &&
                                d.getDateCreation() != null &&
                                ChronoUnit.DAYS.between(
                                        d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                                        maintenant) > 90)
                    .forEach(d -> dossiersAttention.add(Map.of(
                            "id", d.getId(),
                            "reference", d.getNumeroDossier() != null ? d.getNumeroDossier() : "N/A",
                            "raison", "Retard >90j",
                            "joursRetard", ChronoUnit.DAYS.between(
                                    d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                                    maintenant)
                    )));
            
            // Agents sous-performance
            List<Map<String, Object>> agentsSousPerformance = new ArrayList<>();
            double objectifTauxRecouvrement = 70.0;
            
            for (Utilisateur user : tousUtilisateurs) {
                if (user.getRoleUtilisateur() != null && 
                    user.getRoleUtilisateur().name().contains("AGENT")) {
                    
                    List<Dossier> dossiersAgent = tousDossiers.stream()
                            .filter(d -> d.getUtilisateurs() != null &&
                                        d.getUtilisateurs().contains(user))
                            .collect(Collectors.toList());
                    
                    if (!dossiersAgent.isEmpty()) {
                        double tauxRecouvrement = dossiersAgent.stream()
                                .filter(d -> d.getMontantCreance() != null && d.getMontantRecouvre() != null)
                                .mapToDouble(d -> d.getMontantRecouvre() / d.getMontantCreance() * 100)
                                .average()
                                .orElse(0.0);
                        
                        if (tauxRecouvrement < objectifTauxRecouvrement) {
                            agentsSousPerformance.add(Map.of(
                                    "id", user.getId(),
                                    "nom", user.getNom() != null ? user.getNom() : "",
                                    "tauxRecouvrement", Math.round(tauxRecouvrement * 100.0) / 100.0,
                                    "objectif", objectifTauxRecouvrement
                            ));
                        }
                    }
                }
            }
            
            // Départements rééquilibrage
            List<Map<String, Object>> departementsReequilibrage = new ArrayList<>();
            double seuilCharge = 80.0;
            
            for (TypeRecouvrement type : TypeRecouvrement.values()) {
                List<Dossier> dossiersType = tousDossiers.stream()
                        .filter(d -> d.getTypeRecouvrement() == type &&
                                    d.getDossierStatus() != DossierStatus.CLOTURE)
                        .collect(Collectors.toList());
                
                if (!dossiersType.isEmpty()) {
                    long totalDossiers = tousDossiers.size();
                    double charge = (dossiersType.size() * 100.0) / totalDossiers;
                    
                    if (charge > seuilCharge) {
                        long dossiersAReaffecter = Math.round((charge - seuilCharge) * totalDossiers / 100);
                        departementsReequilibrage.add(Map.of(
                                "nom", type.name(),
                                "charge", Math.round(charge * 100.0) / 100.0,
                                "recommandation", "Réaffecter " + dossiersAReaffecter + " dossiers"
                        ));
                    }
                }
            }
            
            Map<String, List<Map<String, Object>>> response = Map.of(
                    "dossiersAttention", dossiersAttention,
                    "agentsSousPerformance", agentsSousPerformance,
                    "departementsReequilibrage", departementsReequilibrage
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des alertes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

