package projet.carthagecreance_backend.Service.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.FinanceAlertDTO;
import projet.carthagecreance_backend.DTO.FinanceStatsDTO;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.Service.FinanceAnalyticsService;
import projet.carthagecreance_backend.Service.FluxFraisService;
import projet.carthagecreance_backend.Service.PaiementService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
@Transactional
public class FinanceAnalyticsServiceImpl implements FinanceAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(FinanceAnalyticsServiceImpl.class);
    private static final Double SEUIL_FRAIS_ELEVES = 0.40; // 40% du montant dû
    private static final int JOURS_INACTIF = 90; // 3 mois

    @Autowired
    private FluxFraisRepository fluxFraisRepository;

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private FactureRepository factureRepository;

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private FluxFraisService fluxFraisService;

    @Autowired
    private PaiementService paiementService;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    public FinanceStatsDTO getDashboardStats() {
        logger.info("Calcul des statistiques du dashboard");

        // Calculer les totaux
        Double totalFraisEngages = fluxFraisRepository.findAll().stream()
                .mapToDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                .sum();

        // Montant recouvré = somme des montants des dossiers clôturés
        Double montantRecouvre = dossierRepository.findAll().stream()
                .filter(d -> d.getDateCloture() != null)
                .mapToDouble(d -> d.getMontantCreance() != null ? d.getMontantCreance() : 0.0)
                .sum();

        // Frais récupérés = somme des paiements validés
        Double fraisRecuperes = paiementRepository.findAll().stream()
                .filter(p -> p.getStatut() == StatutPaiement.VALIDE)
                .mapToDouble(p -> p.getMontant() != null ? p.getMontant() : 0.0)
                .sum();

        Double netGenere = montantRecouvre - totalFraisEngages;

        // Répartition des frais par catégorie
        Map<String, Double> repartitionMap = fluxFraisRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        FluxFrais::getCategorie,
                        Collectors.summingDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                ));

        List<FinanceStatsDTO.RepartitionFraisDTO> repartitionFrais = repartitionMap.entrySet().stream()
                .map(e -> FinanceStatsDTO.RepartitionFraisDTO.builder()
                        .categorie(e.getKey())
                        .montant(e.getValue())
                        .pourcentage(totalFraisEngages > 0 ? (e.getValue() / totalFraisEngages) * 100 : 0.0)
                        .build())
                .collect(Collectors.toList());

        // Évolution mensuelle (derniers 12 mois)
        LocalDate startDate = LocalDate.now().minusMonths(12);
        List<FinanceStatsDTO.EvolutionMensuelleDTO> evolutionMensuelle = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            LocalDate monthStart = startDate.plusMonths(i);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            
            Double fraisMois = fluxFraisRepository.findByDateActionBetween(monthStart, monthEnd).stream()
                    .mapToDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                    .sum();
            
            Double recouvreMois = dossierRepository.findAll().stream()
                    .filter(d -> d.getDateCloture() != null && 
                            d.getDateCloture().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                    .isAfter(monthStart.minusDays(1)) &&
                            d.getDateCloture().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                    .isBefore(monthEnd.plusDays(1)))
                    .mapToDouble(d -> d.getMontantCreance() != null ? d.getMontantCreance() : 0.0)
                    .sum();

            evolutionMensuelle.add(FinanceStatsDTO.EvolutionMensuelleDTO.builder()
                    .mois(monthStart.format(DateTimeFormatter.ofPattern("MM/yyyy")))
                    .frais(fraisMois)
                    .recouvre(recouvreMois)
                    .build());
        }

        // ROI par agent - calcul réel
        List<FinanceStatsDTO.AgentRoiDTO> agentRoi = calculerRoiParAgent();

        return FinanceStatsDTO.builder()
                .totalFraisEngages(totalFraisEngages)
                .montantRecouvre(montantRecouvre)
                .fraisRecuperes(fraisRecuperes)
                .netGenere(netGenere)
                .repartitionFrais(repartitionFrais)
                .evolutionMensuelle(evolutionMensuelle)
                .agentRoi(agentRoi)
                .build();
    }

    @Override
    public FinanceStatsDTO getStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        // Similaire à getDashboardStats mais filtré par date
        return getDashboardStats(); // TODO: Implémenter le filtrage par date
    }

    @Override
    public List<FinanceAlertDTO> getAlerts(String niveau, String phase) {
        logger.info("Génération des alertes financières");

        List<FinanceAlertDTO> alerts = new ArrayList<>();

        // Alerte 1: Frais élevés (> 40% du montant dû)
        dossierRepository.findAll().forEach(dossier -> {
            Double totalFrais = fluxFraisService.calculerTotalFraisByDossier(dossier.getId());
            Double montantCreance = dossier.getMontantCreance() != null ? dossier.getMontantCreance() : 0.0;
            
            if (montantCreance > 0 && totalFrais / montantCreance > SEUIL_FRAIS_ELEVES) {
                alerts.add(FinanceAlertDTO.builder()
                        .type("FRAIS_ELEVES")
                        .message(String.format("Les frais du dossier %s dépassent %.0f%% du montant dû", 
                                dossier.getNumeroDossier(), SEUIL_FRAIS_ELEVES * 100))
                        .dossierId(dossier.getId())
                        .dossierNumero(dossier.getNumeroDossier())
                        .niveau("DANGER")
                        .dateDeclenchement(LocalDate.now())
                        .build());
            }
        });

        // Alerte 2: Dossier inactif (> 3 mois sans recouvrement)
        dossierRepository.findAll().forEach(dossier -> {
            if (dossier.getDateCloture() == null) {
                LocalDate lastAction = fluxFraisRepository.findByDossierId(dossier.getId()).stream()
                        .map(FluxFrais::getDateAction)
                        .max(LocalDate::compareTo)
                        .orElse(dossier.getDateCreation().toInstant()
                                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                
                if (lastAction.isBefore(LocalDate.now().minusDays(JOURS_INACTIF))) {
                    alerts.add(FinanceAlertDTO.builder()
                            .type("DOSSIER_INACTIF")
                            .message(String.format("Le dossier %s n'a pas eu d'activité depuis plus de %d jours", 
                                    dossier.getNumeroDossier(), JOURS_INACTIF))
                            .dossierId(dossier.getId())
                            .dossierNumero(dossier.getNumeroDossier())
                            .niveau("WARNING")
                            .dateDeclenchement(LocalDate.now())
                            .build());
                }
            }
        });

        // Filtrer par niveau et phase si demandé
        List<FinanceAlertDTO> filteredAlerts = alerts;
        if (niveau != null) {
            filteredAlerts = alerts.stream()
                    .filter(a -> a.getNiveau().equals(niveau))
                    .collect(Collectors.toList());
        }

        return filteredAlerts;
    }

    @Override
    public List<FinanceAlertDTO> getAlertsByDossier(Long dossierId) {
        return getAlerts(null, null).stream()
                .filter(a -> a.getDossierId().equals(dossierId))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getRepartitionFrais() {
        Map<String, Double> repartition = fluxFraisRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        FluxFrais::getCategorie,
                        Collectors.summingDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                ));

        Map<String, Object> result = new HashMap<>();
        result.put("repartition", repartition);
        result.put("total", repartition.values().stream().mapToDouble(Double::doubleValue).sum());
        return result;
    }

    @Override
    public Map<String, Object> getEvolutionMensuelle(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> evolution = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());
            if (monthEnd.isAfter(endDate)) {
                monthEnd = endDate;
            }

            Double frais = fluxFraisRepository.findByDateActionBetween(current, monthEnd).stream()
                    .mapToDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                    .sum();

            Map<String, Object> mois = new HashMap<>();
            mois.put("mois", current.format(DateTimeFormatter.ofPattern("MM/yyyy")));
            mois.put("frais", frais);
            evolution.add(mois);

            current = current.plusMonths(1);
        }

        result.put("evolution", evolution);
        return result;
    }

    @Override
    public List<Map<String, Object>> getAgentRoiClassement() {
        List<FinanceStatsDTO.AgentRoiDTO> agentRoi = calculerRoiParAgent();
        return agentRoi.stream()
                .map(roi -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("agentId", roi.getAgentId());
                    map.put("agentNom", roi.getAgentNom());
                    map.put("montantRecouvre", roi.getMontantRecouvre());
                    map.put("fraisEngages", roi.getFraisEngages());
                    map.put("roiPourcentage", roi.getRoiPourcentage());
                    return map;
                })
                .sorted((a, b) -> Double.compare(
                        (Double) b.get("roiPourcentage"),
                        (Double) a.get("roiPourcentage")))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStatistiquesDossier(Long dossierId) {
        Map<String, Object> stats = new HashMap<>();
        
        Double totalFrais = fluxFraisService.calculerTotalFraisByDossier(dossierId);
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));
        
        stats.put("totalFrais", totalFrais);
        stats.put("montantCreance", dossier.getMontantCreance());
        stats.put("ratioFrais", dossier.getMontantCreance() != null && dossier.getMontantCreance() > 0 
                ? totalFrais / dossier.getMontantCreance() : 0.0);
        
        return stats;
    }

    @Override
    public Double calculerRoiAgent(Long agentId) {
        // Récupérer tous les dossiers de l'agent (créés ou assignés)
        List<Dossier> dossiersAgent = dossierRepository.findAll().stream()
                .filter(d -> (d.getAgentCreateur() != null && d.getAgentCreateur().getId().equals(agentId)) ||
                            (d.getAgentResponsable() != null && d.getAgentResponsable().getId().equals(agentId)))
                .collect(Collectors.toList());
        
        // Calculer le montant recouvré (dossiers clôturés)
        Double montantRecouvre = dossiersAgent.stream()
                .filter(d -> d.getDateCloture() != null)
                .mapToDouble(d -> d.getMontantCreance() != null ? d.getMontantCreance() : 0.0)
                .sum();
        
        // Calculer les frais engagés pour ces dossiers
        Double fraisEngages = dossiersAgent.stream()
                .mapToDouble(d -> fluxFraisService.calculerTotalFraisByDossier(d.getId()))
                .sum();
        
        // Calculer le ROI
        if (fraisEngages == null || fraisEngages == 0.0) {
            return montantRecouvre > 0 ? 100.0 : 0.0;
        }
        
        return ((montantRecouvre - fraisEngages) / fraisEngages) * 100;
    }
    
    private List<FinanceStatsDTO.AgentRoiDTO> calculerRoiParAgent() {
        // Récupérer tous les agents (utilisateurs avec rôle AGENT)
        List<Utilisateur> agents = utilisateurRepository.findAll().stream()
                .filter(u -> u.getRoleUtilisateur() != null && 
                        (u.getRoleUtilisateur().name().contains("AGENT") || 
                         u.getRoleUtilisateur().name().contains("CHEF")))
                .collect(Collectors.toList());
        
        List<FinanceStatsDTO.AgentRoiDTO> agentRoiList = new ArrayList<>();
        
        for (Utilisateur agent : agents) {
            Long agentId = agent.getId();
            
            // Récupérer tous les dossiers de l'agent
            List<Dossier> dossiersAgent = dossierRepository.findAll().stream()
                    .filter(d -> (d.getAgentCreateur() != null && d.getAgentCreateur().getId().equals(agentId)) ||
                                (d.getAgentResponsable() != null && d.getAgentResponsable().getId().equals(agentId)))
                    .collect(Collectors.toList());
            
            // Montant recouvré
            Double montantRecouvre = dossiersAgent.stream()
                    .filter(d -> d.getDateCloture() != null)
                    .mapToDouble(d -> d.getMontantCreance() != null ? d.getMontantCreance() : 0.0)
                    .sum();
            
            // Frais engagés
            Double fraisEngages = dossiersAgent.stream()
                    .mapToDouble(d -> fluxFraisService.calculerTotalFraisByDossier(d.getId()))
                    .sum();
            
            // Calculer ROI
            Double roiPourcentage = 0.0;
            if (fraisEngages != null && fraisEngages > 0) {
                roiPourcentage = ((montantRecouvre - fraisEngages) / fraisEngages) * 100;
            } else if (montantRecouvre > 0) {
                roiPourcentage = 100.0;
            }
            
            agentRoiList.add(FinanceStatsDTO.AgentRoiDTO.builder()
                    .agentId(agentId)
                    .agentNom(agent.getNom() + " " + agent.getPrenom())
                    .montantRecouvre(montantRecouvre)
                    .fraisEngages(fraisEngages)
                    .roiPourcentage(roiPourcentage)
                    .build());
        }
        
        return agentRoiList;
    }

    @Override
    public List<Map<String, Object>> getInsights() {
        List<Map<String, Object>> insights = new ArrayList<>();

        // Insight 1: Éviter huissier pour dossiers < 5000
        Map<String, Object> insight1 = new HashMap<>();
        insight1.put("categorie", "Optimisation coûts");
        insight1.put("message", "Pour les dossiers de moins de 5000 TND, éviter l'intervention d'un huissier");
        insight1.put("action", "Réviser la stratégie de recouvrement pour les petits montants");
        insights.add(insight1);

        // Insight 2: Audit créancier
        Map<String, Object> insight2 = new HashMap<>();
        insight2.put("categorie", "Risques dossier");
        insight2.put("message", "Certains créanciers ont un taux élevé de dossiers avec frais > 30%");
        insight2.put("action", "Proposer un audit pour optimiser les processus");
        insights.add(insight2);

        return insights;
    }

    @Override
    public void marquerInsightTraite(Long insightId) {
        logger.info("Marquage de l'insight ID: {} comme traité", insightId);
        // TODO: Implémenter la persistance des insights traités
    }
    
    @Override
    public byte[] exporterRapportExcel(String typeRapport, LocalDate startDate, LocalDate endDate, Map<String, Object> filtres) {
        logger.info("Export Excel - Type: {}, Période: {} - {}", typeRapport, startDate, endDate);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Rapport " + typeRapport);
            int rowNum = 0;
            
            // Style pour l'en-tête
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Style pour les cellules numériques
            CellStyle numberStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            numberStyle.setDataFormat(format.getFormat("#,##0.00"));
            
            // Créer l'en-tête selon le type de rapport
            Row headerRow = sheet.createRow(rowNum++);
            List<String> headers = new ArrayList<>();
            
            switch (typeRapport.toUpperCase()) {
                case "MENSUEL":
                    headers = Arrays.asList("Mois", "Frais Engagés", "Montant Recouvré", "Net Généré");
                    break;
                case "CLIENT":
                    headers = Arrays.asList("Créancier", "Total Dû", "Total Récupéré", "Frais Engagés", "Ratio");
                    break;
                case "AGENT":
                    headers = Arrays.asList("Agent", "Montant Recouvré", "Frais Engagés", "ROI (%)");
                    break;
                case "SECTEUR":
                    headers = Arrays.asList("Secteur", "Nombre Dossiers", "Frais Total", "Montant Total");
                    break;
                default:
                    headers = Arrays.asList("Colonne 1", "Colonne 2", "Colonne 3");
            }
            
            // Remplir l'en-tête
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            // Remplir les données selon le type
            switch (typeRapport.toUpperCase()) {
                case "MENSUEL":
                    remplirDonneesMensuelles(sheet, startDate, endDate, numberStyle, rowNum);
                    break;
                case "AGENT":
                    remplirDonneesAgents(sheet, numberStyle, rowNum);
                    break;
                default:
                    // Données par défaut
                    Row dataRow = sheet.createRow(rowNum++);
                    dataRow.createCell(0).setCellValue("Données non disponibles");
            }
            
            // Ajuster la largeur des colonnes
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            byte[] excelBytes = out.toByteArray();
            logger.info("Export Excel généré avec succès, taille: {} bytes", excelBytes.length);
            return excelBytes;
            
        } catch (IOException e) {
            logger.error("Erreur lors de l'export Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'export Excel: " + e.getMessage(), e);
        }
    }
    
    private void remplirDonneesMensuelles(Sheet sheet, LocalDate startDate, LocalDate endDate, 
                                          CellStyle numberStyle, int startRow) {
        int rowNum = startRow;
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            final LocalDate monthEndFinal;
            LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());
            if (monthEnd.isAfter(endDate)) {
                monthEndFinal = endDate;
            } else {
                monthEndFinal = monthEnd;
            }
            
            final LocalDate currentFinal = current;
            
            Double frais = fluxFraisRepository.findByDateActionBetween(currentFinal, monthEndFinal).stream()
                    .mapToDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                    .sum();
            
            Double recouvre = dossierRepository.findAll().stream()
                    .filter(d -> d.getDateCloture() != null)
                    .filter(d -> {
                        LocalDate dateCloture = d.getDateCloture().toInstant()
                                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        return !dateCloture.isBefore(currentFinal) && !dateCloture.isAfter(monthEndFinal);
                    })
                    .mapToDouble(d -> d.getMontantCreance() != null ? d.getMontantCreance() : 0.0)
                    .sum();
            
            Double net = recouvre - frais;
            
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(currentFinal.format(DateTimeFormatter.ofPattern("MM/yyyy")));
            
            Cell cellFrais = row.createCell(1);
            cellFrais.setCellValue(frais);
            cellFrais.setCellStyle(numberStyle);
            
            Cell cellRecouvre = row.createCell(2);
            cellRecouvre.setCellValue(recouvre);
            cellRecouvre.setCellStyle(numberStyle);
            
            Cell cellNet = row.createCell(3);
            cellNet.setCellValue(net);
            cellNet.setCellStyle(numberStyle);
            
            current = current.plusMonths(1);
        }
    }
    
    private void remplirDonneesAgents(Sheet sheet, CellStyle numberStyle, int startRow) {
        int rowNum = startRow;
        List<FinanceStatsDTO.AgentRoiDTO> agentRoi = calculerRoiParAgent();
        
        for (FinanceStatsDTO.AgentRoiDTO roi : agentRoi) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(roi.getAgentNom());
            
            Cell cellRecouvre = row.createCell(1);
            cellRecouvre.setCellValue(roi.getMontantRecouvre() != null ? roi.getMontantRecouvre() : 0.0);
            cellRecouvre.setCellStyle(numberStyle);
            
            Cell cellFrais = row.createCell(2);
            cellFrais.setCellValue(roi.getFraisEngages() != null ? roi.getFraisEngages() : 0.0);
            cellFrais.setCellStyle(numberStyle);
            
            Cell cellRoi = row.createCell(3);
            cellRoi.setCellValue(roi.getRoiPourcentage() != null ? roi.getRoiPourcentage() : 0.0);
            cellRoi.setCellStyle(numberStyle);
        }
    }
}

