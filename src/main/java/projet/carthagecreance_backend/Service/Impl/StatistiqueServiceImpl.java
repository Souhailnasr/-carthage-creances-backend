package projet.carthagecreance_backend.Service.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.Service.StatistiqueService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation complète du service de statistiques
 * Calcule et stocke toutes les statistiques du système
 */
@Service
@Transactional
public class StatistiqueServiceImpl implements StatistiqueService {

    private static final Logger logger = LoggerFactory.getLogger(StatistiqueServiceImpl.class);

    @Autowired
    private StatistiqueRepository statistiqueRepository;

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private EnquetteRepository enquetteRepository;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private AudienceRepository audienceRepository;

    @Autowired
    private TacheUrgenteRepository tacheUrgenteRepository;

    @Autowired
    private DocumentHuissierRepository documentHuissierRepository;

    @Autowired
    private ActionHuissierRepository actionHuissierRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private TarifDossierRepository tarifDossierRepository;
    
    @Autowired
    private FactureRepository factureRepository;

    /**
     * Calcule et stocke toutes les statistiques globales
     * Appelé automatiquement chaque jour à 2h du matin
     */
    @Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h
    public void calculerEtStockerStatistiquesGlobales() {
        logger.info("Début du calcul automatique des statistiques globales");
        try {
            String periode = YearMonth.now().toString(); // Format: "2024-01"
            
            // Supprimer les anciennes statistiques de la période pour éviter les duplications
            List<Statistique> anciennesStats = statistiqueRepository.findByPeriode(periode);
            if (!anciennesStats.isEmpty()) {
                statistiqueRepository.deleteAll(anciennesStats);
                logger.info("Suppression de {} anciennes statistiques pour la période {}", 
                           anciennesStats.size(), periode);
            }
            
            Map<String, Object> stats = getStatistiquesGlobales();
            
            // Stocker chaque statistique
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                TypeStatistique type = getTypeStatistiqueFromKey(entry.getKey());
                if (type != null) {
                    Statistique statistique = Statistique.builder()
                            .type(type)
                            .valeur(convertToDouble(entry.getValue()))
                            .description(entry.getKey())
                            .periode(periode)
                            .dateCalcul(LocalDateTime.now())
                            .build();
                    if (statistique != null) {
                        statistiqueRepository.save(statistique);
                    }
                }
            }
            logger.info("Statistiques globales calculées et stockées avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors du calcul automatique des statistiques: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesGlobales() {
        logger.info("Calcul des statistiques globales");
        Map<String, Object> stats = new HashMap<>();

        // Statistiques des dossiers
        List<Dossier> tousDossiers = dossierRepository.findAll();
        long totalDossiers = tousDossiers.size();
        long dossiersEnCours = tousDossiers.stream()
                .filter(d -> d.getDossierStatus() == DossierStatus.ENCOURSDETRAITEMENT)
                .count();
        long dossiersValides = tousDossiers.stream()
                .filter(d -> d.getValide() != null && d.getValide())
                .count();
        long dossiersRejetes = tousDossiers.stream()
                .filter(d -> d.getStatut() == Statut.REJETE)
                .count();
        long dossiersClotures = tousDossiers.stream()
                .filter(d -> d.getDossierStatus() == DossierStatus.CLOTURE)
                .count();
        
        // Dossiers créés ce mois
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        long dossiersCreesCeMois = tousDossiers.stream()
                .filter(d -> d.getDateCreation() != null && 
                        d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isAfter(debutMois.minusDays(1)))
                .count();

        // Dossiers par phase
        long dossiersPhaseCreation = tousDossiers.stream()
                .filter(d -> d.getTypeRecouvrement() == null || d.getTypeRecouvrement() == TypeRecouvrement.NON_AFFECTE)
                .count();
        
        long dossiersPhaseEnquete = tousDossiers.stream()
                .filter(d -> {
                    Optional<Enquette> enquete = enquetteRepository.findByDossierId(d.getId());
                    return enquete.isPresent() && enquete.get().getStatut() != Statut.VALIDE;
                })
                .count();
        
        long dossiersPhaseAmiable = tousDossiers.stream()
                .filter(d -> d.getTypeRecouvrement() == TypeRecouvrement.AMIABLE)
                .count();
        
        long dossiersPhaseJuridique = tousDossiers.stream()
                .filter(d -> d.getTypeRecouvrement() == TypeRecouvrement.JURIDIQUE)
                .count();

        stats.put("totalDossiers", totalDossiers);
        stats.put("dossiersEnCours", dossiersEnCours);
        stats.put("dossiersValides", dossiersValides);
        stats.put("dossiersRejetes", dossiersRejetes);
        stats.put("dossiersClotures", dossiersClotures);
        stats.put("dossiersCreesCeMois", dossiersCreesCeMois);
        stats.put("dossiersPhaseCreation", dossiersPhaseCreation);
        stats.put("dossiersPhaseEnquete", dossiersPhaseEnquete);
        stats.put("dossiersPhaseAmiable", dossiersPhaseAmiable);
        stats.put("dossiersPhaseJuridique", dossiersPhaseJuridique);

        // Statistiques des enquêtes
        List<Enquette> toutesEnquetes = enquetteRepository.findAll();
        long totalEnquetes = toutesEnquetes.size();
        long enquetesCompletees = toutesEnquetes.stream()
                .filter(e -> e.getStatut() == Statut.VALIDE)
                .count();
        stats.put("totalEnquetes", totalEnquetes);
        stats.put("enquetesCompletees", enquetesCompletees);
        long enquetesEnCours = toutesEnquetes.stream()
                .filter(e -> e.getStatut() == Statut.EN_COURS || e.getStatut() == Statut.EN_ATTENTE_VALIDATION)
                .count();
        stats.put("enquetesEnCours", enquetesEnCours);

        // Statistiques des actions amiables
        List<Action> toutesActions = actionRepository.findAll();
        long actionsAmiables = toutesActions.size();
        long actionsAmiablesCompletees = toutesActions.stream()
                .filter(a -> a.getReponseDebiteur() != null)
                .count();
        stats.put("actionsAmiables", actionsAmiables);
        stats.put("actionsAmiablesCompletees", actionsAmiablesCompletees);

        // Statistiques des documents huissier
        List<DocumentHuissier> tousDocuments = documentHuissierRepository.findAll();
        long documentsHuissierCrees = tousDocuments.size();
        long documentsHuissierCompletes = tousDocuments.stream()
                .filter(d -> d.getStatus() == StatutDocumentHuissier.COMPLETED)
                .count();
        stats.put("documentsHuissierCrees", documentsHuissierCrees);
        stats.put("documentsHuissierCompletes", documentsHuissierCompletes);

        // Statistiques des actions huissier
        List<ActionHuissier> toutesActionsHuissier = actionHuissierRepository.findAll();
        long actionsHuissierCrees = toutesActionsHuissier.size();
        long actionsHuissierCompletes = toutesActionsHuissier.stream()
                .filter(a -> a.getEtatDossier() != null && 
                        (a.getEtatDossier() == EtatDossier.RECOVERED_TOTAL || 
                         a.getEtatDossier() == EtatDossier.RECOVERED_PARTIAL))
                .count();
        stats.put("actionsHuissierCrees", actionsHuissierCrees);
        stats.put("actionsHuissierCompletes", actionsHuissierCompletes);

        // Statistiques des audiences
        List<Audience> toutesAudiences = audienceRepository.findAll();
        long audiencesTotales = toutesAudiences.size();
        LocalDate aujourdhui = LocalDate.now();
        LocalDate dans7Jours = aujourdhui.plusDays(7);
        long audiencesProchaines = toutesAudiences.stream()
                .filter(a -> a.getDateProchaine() != null && 
                        a.getDateProchaine().isAfter(aujourdhui.minusDays(1)) && 
                        a.getDateProchaine().isBefore(dans7Jours.plusDays(1)))
                .count();
        stats.put("audiencesTotales", audiencesTotales);
        stats.put("audiencesProchaines", audiencesProchaines);

        // Statistiques des tâches
        List<TacheUrgente> toutesTaches = tacheUrgenteRepository.findAll();
        long tachesCompletees = toutesTaches.stream()
                .filter(t -> t.getStatut() == StatutTache.TERMINEE)
                .count();
        long tachesEnCours = toutesTaches.stream()
                .filter(t -> t.getStatut() == StatutTache.EN_COURS)
                .count();
        long tachesEnRetard = tacheUrgenteRepository.findTachesEnRetard(LocalDateTime.now()).size();
        stats.put("tachesCompletees", tachesCompletees);
        stats.put("tachesEnCours", tachesEnCours);
        stats.put("tachesEnRetard", tachesEnRetard);

        // Statistiques financières
        // ✅ Utiliser les montants recouvrés réels au lieu de montant créance des dossiers clôturés
        double montantRecouvre = tousDossiers.stream()
                .filter(d -> d.getMontantRecouvre() != null)
                .mapToDouble(Dossier::getMontantRecouvre)
                .sum();
        double montantEnCours = tousDossiers.stream()
                .filter(d -> d.getDateCloture() == null)
                .mapToDouble(d -> d.getMontantCreance() != null ? d.getMontantCreance() : 0.0)
                .sum();
        
        // ✅ NOUVEAU : Ajouter les montants par phase
        double montantRecouvrePhaseAmiable = tousDossiers.stream()
                .filter(d -> d.getMontantRecouvrePhaseAmiable() != null)
                .mapToDouble(Dossier::getMontantRecouvrePhaseAmiable)
                .sum();
        double montantRecouvrePhaseJuridique = tousDossiers.stream()
                .filter(d -> d.getMontantRecouvrePhaseJuridique() != null)
                .mapToDouble(Dossier::getMontantRecouvrePhaseJuridique)
                .sum();
        
        stats.put("montantRecouvre", montantRecouvre);
        stats.put("montantEnCours", montantEnCours);
        stats.put("montantRecouvrePhaseAmiable", montantRecouvrePhaseAmiable);
        stats.put("montantRecouvrePhaseJuridique", montantRecouvrePhaseJuridique);

        // Taux de réussite global
        double tauxReussite = totalDossiers > 0 ? ((double) dossiersClotures / totalDossiers) * 100 : 0.0;
        stats.put("tauxReussiteGlobal", tauxReussite);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesParPeriode(LocalDate dateDebut, LocalDate dateFin) {
        logger.info("Calcul des statistiques pour la période {} - {}", dateDebut, dateFin);
        Map<String, Object> stats = new HashMap<>();

        // Filtrer les dossiers par période
        List<Dossier> dossiersPeriode = dossierRepository.findAll().stream()
                .filter(d -> {
                    if (d.getDateCreation() == null) return false;
                    LocalDate dateCreation = d.getDateCreation().toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    return !dateCreation.isBefore(dateDebut) && !dateCreation.isAfter(dateFin);
                })
                .collect(Collectors.toList());

        // Calculer les statistiques similaires à getStatistiquesGlobales mais filtrées
        stats.put("totalDossiers", (long) dossiersPeriode.size());
        stats.put("dossiersEnCours", dossiersPeriode.stream()
                .filter(d -> d.getDossierStatus() == DossierStatus.ENCOURSDETRAITEMENT)
                .count());
        stats.put("dossiersClotures", dossiersPeriode.stream()
                .filter(d -> d.getDossierStatus() == DossierStatus.CLOTURE)
                .count());
        
        // Ajouter d'autres statistiques filtrées par période...
        // (similaire à getStatistiquesGlobales mais avec filtrage par date)

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesAgent(Long agentId) {
        logger.info("Calcul des statistiques pour l'agent {}", agentId);
        Map<String, Object> stats = new HashMap<>();

        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + agentId));

        // Récupérer les dossiers de l'agent (créés ou assignés)
        List<Dossier> dossiersAgent = dossierRepository.findAll().stream()
                .filter(d -> (d.getAgentCreateur() != null && d.getAgentCreateur().getId().equals(agentId)) ||
                            (d.getAgentResponsable() != null && d.getAgentResponsable().getId().equals(agentId)))
                .collect(Collectors.toList());

        long dossiersTraites = dossiersAgent.size();
        long dossiersValides = dossiersAgent.stream()
                .filter(d -> d.getValide() != null && d.getValide())
                .count();
        long dossiersClotures = dossiersAgent.stream()
                .filter(d -> d.getDossierStatus() == DossierStatus.CLOTURE)
                .count();

        // Enquêtes complétées
        long enquetesCompletees = dossiersAgent.stream()
                .mapToLong(d -> {
                    Optional<Enquette> enquete = enquetteRepository.findByDossierId(d.getId());
                    return enquete.isPresent() && enquete.get().getStatut() == Statut.VALIDE ? 1 : 0;
                })
                .sum();

        // Actions amiables
        long actionsAmiables = dossiersAgent.stream()
                .mapToLong(d -> actionRepository.findByDossierId(d.getId()).size())
                .sum();

        // Documents huissier
        long documentsHuissier = dossiersAgent.stream()
                .mapToLong(d -> documentHuissierRepository.findByDossierId(d.getId()).size())
                .sum();

        // Actions huissier
        long actionsHuissier = dossiersAgent.stream()
                .mapToLong(d -> actionHuissierRepository.findByDossierId(d.getId()).size())
                .sum();

        // Audiences
        long audiences = dossiersAgent.stream()
                .mapToLong(d -> audienceRepository.findByDossierId(d.getId()).size())
                .sum();

        // Tâches
        long taches = tacheUrgenteRepository.findByAgentAssignéId(agentId).size();
        long tachesCompletees = tacheUrgenteRepository.findByAgentAssignéId(agentId).stream()
                .filter(t -> t.getStatut() == StatutTache.TERMINEE)
                .count();

        // Montants
        double montantRecouvre = dossiersAgent.stream()
                .filter(d -> d.getDateCloture() != null)
                .mapToDouble(d -> d.getMontantCreance() != null ? d.getMontantCreance() : 0.0)
                .sum();
        double montantEnCours = dossiersAgent.stream()
                .filter(d -> d.getDateCloture() == null)
                .mapToDouble(d -> d.getMontantCreance() != null ? d.getMontantCreance() : 0.0)
                .sum();

        // Taux de réussite
        double tauxReussite = dossiersTraites > 0 ? ((double) dossiersValides / dossiersTraites) * 100 : 0.0;

        stats.put("agentId", agentId);
        stats.put("agentNom", agent.getNom() + " " + agent.getPrenom());
        stats.put("dossiersTraites", dossiersTraites);
        stats.put("dossiersValides", dossiersValides);
        stats.put("dossiersClotures", dossiersClotures);
        stats.put("enquetesCompletees", enquetesCompletees);
        stats.put("actionsAmiables", actionsAmiables);
        stats.put("documentsHuissier", documentsHuissier);
        stats.put("actionsHuissier", actionsHuissier);
        stats.put("audiences", audiences);
        stats.put("taches", taches);
        stats.put("tachesCompletees", tachesCompletees);
        stats.put("montantRecouvre", montantRecouvre);
        stats.put("montantEnCours", montantEnCours);
        stats.put("tauxReussite", tauxReussite);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesChef(Long chefId) {
        logger.info("Calcul des statistiques pour le chef {}", chefId);
        Map<String, Object> stats = new HashMap<>();

        Utilisateur chef = utilisateurRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef non trouvé avec l'ID: " + chefId));

        // Déterminer le département du chef
        RoleUtilisateur roleChef = chef.getRoleUtilisateur();
        List<RoleUtilisateur> rolesAgents = getRolesAgentsDuChef(roleChef);

        // Récupérer les agents du département
        List<Utilisateur> agents = utilisateurRepository.findByRoleUtilisateurIn(rolesAgents);

        // Statistiques agrégées du chef et de ses agents
        Map<String, Object> statsChef = getStatistiquesAgent(chefId);
        stats.put("chef", statsChef);

        // Statistiques de chaque agent
        List<Map<String, Object>> statsAgents = new ArrayList<>();
        for (Utilisateur agent : agents) {
            statsAgents.add(getStatistiquesAgent(agent.getId()));
        }
        stats.put("agents", statsAgents);

        // Statistiques agrégées du département
        long totalDossiers = agents.stream()
                .mapToLong(a -> {
                    List<Dossier> dossiers = dossierRepository.findAll().stream()
                            .filter(d -> (d.getAgentCreateur() != null && d.getAgentCreateur().getId().equals(a.getId())) ||
                                        (d.getAgentResponsable() != null && d.getAgentResponsable().getId().equals(a.getId())))
                            .collect(Collectors.toList());
                    return dossiers.size();
                })
                .sum();

        stats.put("totalDossiersDepartement", totalDossiers);
        stats.put("nombreAgents", agents.size());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesTousChefs() {
        logger.info("Calcul des statistiques de tous les chefs");
        Map<String, Object> stats = new HashMap<>();

        List<Utilisateur> chefs = utilisateurRepository.findAll().stream()
                .filter(u -> u.getRoleUtilisateur().name().contains("CHEF"))
                .collect(Collectors.toList());

        List<Map<String, Object>> statsChefs = new ArrayList<>();
        for (Utilisateur chef : chefs) {
            statsChefs.add(getStatistiquesChef(chef.getId()));
        }

        stats.put("chefs", statsChefs);
        stats.put("nombreChefs", chefs.size());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesDossiers() {
        logger.info("Calcul des statistiques des dossiers");
        Map<String, Object> stats = new HashMap<>();

        List<Dossier> tousDossiers = dossierRepository.findAll();

        // Répartition par statut
        Map<DossierStatus, Long> parStatut = tousDossiers.stream()
                .collect(Collectors.groupingBy(Dossier::getDossierStatus, Collectors.counting()));
        stats.put("parStatut", parStatut);

        // Répartition par type de recouvrement
        Map<TypeRecouvrement, Long> parTypeRecouvrement = tousDossiers.stream()
                .filter(d -> d.getTypeRecouvrement() != null)
                .collect(Collectors.groupingBy(Dossier::getTypeRecouvrement, Collectors.counting()));
        stats.put("parTypeRecouvrement", parTypeRecouvrement);

        // Répartition par urgence
        Map<Urgence, Long> parUrgence = tousDossiers.stream()
                .filter(d -> d.getUrgence() != null)
                .collect(Collectors.groupingBy(Dossier::getUrgence, Collectors.counting()));
        stats.put("parUrgence", parUrgence);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesActionsAmiables() {
        logger.info("Calcul des statistiques des actions amiables");
        Map<String, Object> stats = new HashMap<>();

        List<Action> toutesActions = actionRepository.findAll();
        long total = toutesActions.size();
        long completees = toutesActions.stream()
                .filter(a -> a.getReponseDebiteur() != null)
                .count();

        stats.put("total", total);
        stats.put("completees", completees);
        stats.put("enCours", total - completees);
        stats.put("tauxReussite", total > 0 ? ((double) completees / total) * 100 : 0.0);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesAudiences() {
        logger.info("Calcul des statistiques des audiences");
        Map<String, Object> stats = new HashMap<>();

        List<Audience> toutesAudiences = audienceRepository.findAll();
        long total = toutesAudiences.size();
        
        LocalDate aujourdhui = LocalDate.now();
        long prochaines = toutesAudiences.stream()
                .filter(a -> a.getDateProchaine() != null && 
                        a.getDateProchaine().isAfter(aujourdhui.minusDays(1)))
                .count();

        stats.put("total", total);
        stats.put("prochaines", prochaines);
        stats.put("passees", total - prochaines);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesTaches() {
        logger.info("Calcul des statistiques des tâches");
        Map<String, Object> stats = new HashMap<>();

        List<TacheUrgente> toutesTaches = tacheUrgenteRepository.findAll();
        long total = toutesTaches.size();
        long enAttente = toutesTaches.stream()
                .filter(t -> t.getStatut() == StatutTache.EN_ATTENTE)
                .count();
        long enCours = toutesTaches.stream()
                .filter(t -> t.getStatut() == StatutTache.EN_COURS)
                .count();
        long terminees = toutesTaches.stream()
                .filter(t -> t.getStatut() == StatutTache.TERMINEE)
                .count();
        long annulees = toutesTaches.stream()
                .filter(t -> t.getStatut() == StatutTache.ANNULEE)
                .count();
        long enRetard = tacheUrgenteRepository.findTachesEnRetard(LocalDateTime.now()).size();

        stats.put("total", total);
        stats.put("enAttente", enAttente);
        stats.put("enCours", enCours);
        stats.put("terminees", terminees);
        stats.put("annulees", annulees);
        stats.put("enRetard", enRetard);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesFinancieres() {
        logger.info("Calcul des statistiques financières");
        Map<String, Object> stats = new HashMap<>();

        // Utiliser FinanceAnalyticsService si disponible, sinon calculer directement
        List<Dossier> tousDossiers = dossierRepository.findAll();
        double montantRecouvre = tousDossiers.stream()
                .filter(d -> d.getDateCloture() != null)
                .mapToDouble(d -> d.getMontantCreance() != null ? d.getMontantCreance() : 0.0)
                .sum();

        double montantEnCours = tousDossiers.stream()
                .filter(d -> d.getDateCloture() == null)
                .mapToDouble(d -> d.getMontantCreance() != null ? d.getMontantCreance() : 0.0)
                .sum();

        // Total frais engagés (depuis TarifDossier)
        double totalFraisEngages = tarifDossierRepository.findAll().stream()
                .mapToDouble(t -> t.getMontantTotal() != null ? t.getMontantTotal().doubleValue() : 0.0)
                .sum();

        // Frais récupérés (paiements validés)
        double fraisRecuperes = paiementRepository.findAll().stream()
                .filter(p -> p.getStatut() == StatutPaiement.VALIDE)
                .mapToDouble(p -> p.getMontant() != null ? p.getMontant().doubleValue() : 0.0)
                .sum();

        // ✅ NOUVEAU : Statistiques des factures
        long totalFactures = factureRepository.count();
        long facturesPayees = factureRepository.findAll().stream()
                .filter(f -> f.getStatut() == FactureStatut.PAYEE)
                .count();
        long facturesEnAttente = factureRepository.findAll().stream()
                .filter(f -> f.getStatut() == FactureStatut.EMISE || f.getStatut() == FactureStatut.BROUILLON)
                .count();
        
        // ✅ NOUVEAU : Statistiques des paiements
        long totalPaiements = paiementRepository.count();
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        long paiementsCeMois = paiementRepository.findAll().stream()
                .filter(p -> {
                    if (p.getDatePaiement() == null) return false;
                    LocalDate datePaiement = p.getDatePaiement();
                    return !datePaiement.isBefore(debutMois);
                })
                .count();

        // ✅ NOUVEAU : Montants recouvrés par phase
        double montantRecouvrePhaseAmiable = tousDossiers.stream()
                .filter(d -> d.getMontantRecouvrePhaseAmiable() != null)
                .mapToDouble(Dossier::getMontantRecouvrePhaseAmiable)
                .sum();
        
        double montantRecouvrePhaseJuridique = tousDossiers.stream()
                .filter(d -> d.getMontantRecouvrePhaseJuridique() != null)
                .mapToDouble(Dossier::getMontantRecouvrePhaseJuridique)
                .sum();
        
        double montantRecouvreTotal = montantRecouvrePhaseAmiable + montantRecouvrePhaseJuridique;

        stats.put("montantRecouvre", montantRecouvreTotal);  // Utiliser le total par phase au lieu de montantCreance des dossiers clôturés
        stats.put("montantEnCours", montantEnCours);
        stats.put("montantRecouvrePhaseAmiable", montantRecouvrePhaseAmiable);  // ✅ NOUVEAU
        stats.put("montantRecouvrePhaseJuridique", montantRecouvrePhaseJuridique);  // ✅ NOUVEAU
        stats.put("totalFraisEngages", totalFraisEngages);
        stats.put("fraisRecuperes", fraisRecuperes);
        stats.put("netGenere", montantRecouvreTotal - totalFraisEngages);  // Utiliser le total par phase
        stats.put("totalFactures", totalFactures);
        stats.put("facturesPayees", facturesPayees);
        stats.put("facturesEnAttente", facturesEnAttente);
        stats.put("totalPaiements", totalPaiements);
        stats.put("paiementsCeMois", paiementsCeMois);

        return stats;
    }

    /**
     * Recalcule et stocke automatiquement les statistiques globales de manière asynchrone
     * Appelé après chaque action importante (création/modification de dossier, enquête, action, etc.)
     * Cette méthode est asynchrone pour ne pas ralentir les opérations principales
     */
    @Override
    @Async
    @Transactional
    public void recalculerStatistiquesAsync() {
        logger.debug("Début du recalcul asynchrone des statistiques");
        try {
            String periode = YearMonth.now().toString(); // Format: "2024-01"
            
            // Supprimer les anciennes statistiques de la période pour éviter les duplications
            List<Statistique> anciennesStats = statistiqueRepository.findByPeriode(periode);
            if (!anciennesStats.isEmpty()) {
                statistiqueRepository.deleteAll(anciennesStats);
                logger.debug("Suppression de {} anciennes statistiques pour la période {}", 
                            anciennesStats.size(), periode);
            }
            
            Map<String, Object> stats = getStatistiquesGlobales();
            
            // Stocker chaque statistique
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                TypeStatistique type = getTypeStatistiqueFromKey(entry.getKey());
                if (type != null) {
                    Statistique statistique = Statistique.builder()
                            .type(type)
                            .valeur(convertToDouble(entry.getValue()))
                            .description(entry.getKey())
                            .periode(periode)
                            .dateCalcul(LocalDateTime.now())
                            .build();
                    if (statistique != null) {
                        statistiqueRepository.save(statistique);
                    }
                }
            }
            logger.debug("Statistiques recalculées et stockées avec succès (asynchrone)");
        } catch (Exception e) {
            logger.error("Erreur lors du recalcul asynchrone des statistiques: {}", e.getMessage(), e);
            // Ne pas propager l'erreur pour ne pas affecter l'opération principale
        }
    }

    /**
     * ✅ NOUVEAU : Récupère les statistiques de recouvrement par phase
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesRecouvrementParPhase() {
        logger.info("Calcul des statistiques de recouvrement par phase");
        Map<String, Object> stats = new HashMap<>();
        
        List<Dossier> tousDossiers = dossierRepository.findAll();
        
        // Calculer les montants recouvrés par phase
        double montantRecouvrePhaseAmiable = tousDossiers.stream()
                .filter(d -> d.getMontantRecouvrePhaseAmiable() != null)
                .mapToDouble(Dossier::getMontantRecouvrePhaseAmiable)
                .sum();
        
        double montantRecouvrePhaseJuridique = tousDossiers.stream()
                .filter(d -> d.getMontantRecouvrePhaseJuridique() != null)
                .mapToDouble(Dossier::getMontantRecouvrePhaseJuridique)
                .sum();
        
        double montantRecouvreTotal = montantRecouvrePhaseAmiable + montantRecouvrePhaseJuridique;
        
        // Calculer le nombre de dossiers avec recouvrement par phase
        long dossiersAvecRecouvrementAmiable = tousDossiers.stream()
                .filter(d -> d.getMontantRecouvrePhaseAmiable() != null && d.getMontantRecouvrePhaseAmiable() > 0)
                .count();
        
        long dossiersAvecRecouvrementJuridique = tousDossiers.stream()
                .filter(d -> d.getMontantRecouvrePhaseJuridique() != null && d.getMontantRecouvrePhaseJuridique() > 0)
                .count();
        
        // Calculer les taux de recouvrement par phase
        double montantTotalCreances = tousDossiers.stream()
                .filter(d -> d.getMontantCreance() != null)
                .mapToDouble(Dossier::getMontantCreance)
                .sum();
        
        double tauxRecouvrementAmiable = montantTotalCreances > 0 ? 
                (montantRecouvrePhaseAmiable / montantTotalCreances) * 100 : 0.0;
        
        double tauxRecouvrementJuridique = montantTotalCreances > 0 ? 
                (montantRecouvrePhaseJuridique / montantTotalCreances) * 100 : 0.0;
        
        double tauxRecouvrementTotal = montantTotalCreances > 0 ? 
                (montantRecouvreTotal / montantTotalCreances) * 100 : 0.0;
        
        stats.put("montantRecouvrePhaseAmiable", montantRecouvrePhaseAmiable);
        stats.put("montantRecouvrePhaseJuridique", montantRecouvrePhaseJuridique);
        stats.put("montantRecouvreTotal", montantRecouvreTotal);
        stats.put("dossiersAvecRecouvrementAmiable", dossiersAvecRecouvrementAmiable);
        stats.put("dossiersAvecRecouvrementJuridique", dossiersAvecRecouvrementJuridique);
        stats.put("tauxRecouvrementAmiable", tauxRecouvrementAmiable);
        stats.put("tauxRecouvrementJuridique", tauxRecouvrementJuridique);
        stats.put("tauxRecouvrementTotal", tauxRecouvrementTotal);
        stats.put("montantTotalCreances", montantTotalCreances);
        
        return stats;
    }

    /**
     * ✅ NOUVEAU : Récupère les statistiques de recouvrement par phase pour un département
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesRecouvrementParPhaseDepartement(RoleUtilisateur roleChef) {
        logger.info("Calcul des statistiques de recouvrement par phase pour le département {}", roleChef);
        Map<String, Object> stats = new HashMap<>();
        
        List<RoleUtilisateur> rolesAgents = getRolesAgentsDuChef(roleChef);
        
        // Récupérer tous les dossiers des agents du département
        List<Dossier> dossiersDepartement = dossierRepository.findAll().stream()
                .filter(d -> {
                    if (d.getAgentCreateur() != null && rolesAgents.contains(d.getAgentCreateur().getRoleUtilisateur())) {
                        return true;
                    }
                    if (d.getAgentResponsable() != null && rolesAgents.contains(d.getAgentResponsable().getRoleUtilisateur())) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        // Calculer les montants recouvrés par phase pour le département
        double montantRecouvrePhaseAmiable = dossiersDepartement.stream()
                .filter(d -> d.getMontantRecouvrePhaseAmiable() != null)
                .mapToDouble(Dossier::getMontantRecouvrePhaseAmiable)
                .sum();
        
        double montantRecouvrePhaseJuridique = dossiersDepartement.stream()
                .filter(d -> d.getMontantRecouvrePhaseJuridique() != null)
                .mapToDouble(Dossier::getMontantRecouvrePhaseJuridique)
                .sum();
        
        double montantRecouvreTotal = montantRecouvrePhaseAmiable + montantRecouvrePhaseJuridique;
        
        // Calculer le nombre de dossiers avec recouvrement par phase
        long dossiersAvecRecouvrementAmiable = dossiersDepartement.stream()
                .filter(d -> d.getMontantRecouvrePhaseAmiable() != null && d.getMontantRecouvrePhaseAmiable() > 0)
                .count();
        
        long dossiersAvecRecouvrementJuridique = dossiersDepartement.stream()
                .filter(d -> d.getMontantRecouvrePhaseJuridique() != null && d.getMontantRecouvrePhaseJuridique() > 0)
                .count();
        
        // Calculer les taux de recouvrement par phase
        double montantTotalCreances = dossiersDepartement.stream()
                .filter(d -> d.getMontantCreance() != null)
                .mapToDouble(Dossier::getMontantCreance)
                .sum();
        
        double tauxRecouvrementAmiable = montantTotalCreances > 0 ? 
                (montantRecouvrePhaseAmiable / montantTotalCreances) * 100 : 0.0;
        
        double tauxRecouvrementJuridique = montantTotalCreances > 0 ? 
                (montantRecouvrePhaseJuridique / montantTotalCreances) * 100 : 0.0;
        
        double tauxRecouvrementTotal = montantTotalCreances > 0 ? 
                (montantRecouvreTotal / montantTotalCreances) * 100 : 0.0;
        
        stats.put("montantRecouvrePhaseAmiable", montantRecouvrePhaseAmiable);
        stats.put("montantRecouvrePhaseJuridique", montantRecouvrePhaseJuridique);
        stats.put("montantRecouvreTotal", montantRecouvreTotal);
        stats.put("dossiersAvecRecouvrementAmiable", dossiersAvecRecouvrementAmiable);
        stats.put("dossiersAvecRecouvrementJuridique", dossiersAvecRecouvrementJuridique);
        stats.put("tauxRecouvrementAmiable", tauxRecouvrementAmiable);
        stats.put("tauxRecouvrementJuridique", tauxRecouvrementJuridique);
        stats.put("tauxRecouvrementTotal", tauxRecouvrementTotal);
        stats.put("montantTotalCreances", montantTotalCreances);
        stats.put("totalDossiers", (long) dossiersDepartement.size());
        
        return stats;
    }

    /**
     * Récupère les statistiques filtrées par département pour un chef
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesParDepartement(RoleUtilisateur roleChef) {
        logger.info("Calcul des statistiques pour le département {}", roleChef);
        Map<String, Object> stats = new HashMap<>();

        List<RoleUtilisateur> rolesAgents = getRolesAgentsDuChef(roleChef);

        // Récupérer tous les dossiers des agents du département
        List<Dossier> dossiersDepartement = dossierRepository.findAll().stream()
                .filter(d -> {
                    if (d.getAgentCreateur() != null && rolesAgents.contains(d.getAgentCreateur().getRoleUtilisateur())) {
                        return true;
                    }
                    if (d.getAgentResponsable() != null && rolesAgents.contains(d.getAgentResponsable().getRoleUtilisateur())) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());

        // Calculer les statistiques similaires à getStatistiquesGlobales mais filtrées par département
        stats.put("totalDossiers", (long) dossiersDepartement.size());
        stats.put("dossiersEnCours", dossiersDepartement.stream()
                .filter(d -> d.getDossierStatus() == DossierStatus.ENCOURSDETRAITEMENT)
                .count());
        stats.put("dossiersClotures", dossiersDepartement.stream()
                .filter(d -> d.getDossierStatus() == DossierStatus.CLOTURE)
                .count());

        // Ajouter d'autres statistiques spécifiques au département...

        return stats;
    }

    // Méthodes utilitaires

    private List<RoleUtilisateur> getRolesAgentsDuChef(RoleUtilisateur roleChef) {
        List<RoleUtilisateur> roles = new ArrayList<>();
        switch (roleChef) {
            case CHEF_DEPARTEMENT_DOSSIER:
                roles.add(RoleUtilisateur.AGENT_DOSSIER);
                break;
            case CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE:
                roles.add(RoleUtilisateur.AGENT_RECOUVREMENT_AMIABLE);
                break;
            case CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE:
                roles.add(RoleUtilisateur.AGENT_RECOUVREMENT_JURIDIQUE);
                break;
            case CHEF_DEPARTEMENT_FINANCE:
                roles.add(RoleUtilisateur.AGENT_FINANCE);
                break;
            default:
                break;
        }
        return roles;
    }

    private TypeStatistique getTypeStatistiqueFromKey(String key) {
        try {
            return TypeStatistique.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Mapping personnalisé pour les clés qui ne correspondent pas exactement
            Map<String, TypeStatistique> mapping = new HashMap<>();
            mapping.put("totalDossiers", TypeStatistique.TOTAL_DOSSIERS);
            mapping.put("dossiersEnCours", TypeStatistique.DOSSIERS_EN_COURS);
            mapping.put("dossiersValides", TypeStatistique.DOSSIERS_VALIDES);
            mapping.put("dossiersRejetes", TypeStatistique.DOSSIERS_REJETES);
            mapping.put("dossiersClotures", TypeStatistique.DOSSIERS_CLOTURES);
            mapping.put("dossiersCreesCeMois", TypeStatistique.DOSSIERS_CREES_CE_MOIS);
            mapping.put("dossiersPhaseCreation", TypeStatistique.DOSSIERS_PAR_PHASE_CREATION);
            mapping.put("dossiersPhaseEnquete", TypeStatistique.DOSSIERS_PAR_PHASE_ENQUETE);
            mapping.put("dossiersPhaseAmiable", TypeStatistique.DOSSIERS_PAR_PHASE_AMIABLE);
            mapping.put("dossiersPhaseJuridique", TypeStatistique.DOSSIERS_PAR_PHASE_JURIDIQUE);
            mapping.put("totalEnquetes", TypeStatistique.TOTAL_ENQUETES);
            mapping.put("enquetesCompletees", TypeStatistique.ENQUETES_COMPLETEES);
            mapping.put("actionsAmiables", TypeStatistique.ACTIONS_AMIABLES);
            mapping.put("actionsAmiablesCompletees", TypeStatistique.ACTIONS_AMIABLES_COMPLETEES);
            mapping.put("documentsHuissierCrees", TypeStatistique.DOCUMENTS_HUISSIER_CREES);
            mapping.put("documentsHuissierCompletes", TypeStatistique.DOCUMENTS_HUISSIER_COMPLETES);
            mapping.put("actionsHuissierCrees", TypeStatistique.ACTIONS_HUISSIER_CREES);
            mapping.put("actionsHuissierCompletes", TypeStatistique.ACTIONS_HUISSIER_COMPLETES);
            mapping.put("audiencesTotales", TypeStatistique.AUDIENCES_TOTALES);
            mapping.put("audiencesProchaines", TypeStatistique.AUDIENCES_PROCHAINES);
            mapping.put("tachesCompletees", TypeStatistique.TACHES_COMPLETEES);
            mapping.put("tachesEnCours", TypeStatistique.TACHES_EN_COURS);
            mapping.put("tachesEnRetard", TypeStatistique.TACHES_EN_RETARD);
            mapping.put("tauxReussiteGlobal", TypeStatistique.TAUX_REUSSITE_GLOBAL);
            mapping.put("montantRecouvre", TypeStatistique.MONTANT_RECOUVRE);
            mapping.put("montantEnCours", TypeStatistique.MONTANT_EN_COURS);
            mapping.put("totalFactures", TypeStatistique.TOTAL_FACTURES);
            mapping.put("facturesPayees", TypeStatistique.FACTURES_PAYEES);
            mapping.put("facturesEnAttente", TypeStatistique.FACTURES_EN_ATTENTE);
            mapping.put("totalPaiements", TypeStatistique.TOTAL_PAIEMENTS);
            mapping.put("paiementsCeMois", TypeStatistique.PAIEMENTS_CE_MOIS);
            mapping.put("enquetesEnCours", TypeStatistique.ENQUETES_EN_COURS);
            mapping.put("montantEnCours", TypeStatistique.MONTANT_EN_COURS);
            return mapping.get(key);
        }
    }

    private Double convertToDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}

