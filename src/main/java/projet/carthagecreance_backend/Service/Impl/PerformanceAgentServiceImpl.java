package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.PerformanceAgent;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Enquette;
import projet.carthagecreance_backend.Entity.Statut;
import projet.carthagecreance_backend.Repository.PerformanceAgentRepository;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Repository.EnquetteRepository;
import projet.carthagecreance_backend.Service.PerformanceAgentService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service de gestion des performances des agents
 * Gère toutes les opérations CRUD et la logique métier pour les performances
 */
@Service
@Transactional
public class PerformanceAgentServiceImpl implements PerformanceAgentService {

    @Autowired
    private PerformanceAgentRepository performanceAgentRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private EnquetteRepository enquetteRepository;

    @Autowired
    private projet.carthagecreance_backend.Repository.TacheUrgenteRepository tacheUrgenteRepository;
    
    @Autowired
    private projet.carthagecreance_backend.Repository.ActionRepository actionRepository;
    
    @Autowired
    private projet.carthagecreance_backend.Repository.AudienceRepository audienceRepository;

    /**
     * Crée une nouvelle performance d'agent
     * @param performance La performance à créer
     * @return La performance créée avec son ID généré
     * @throws RuntimeException si les données de la performance sont invalides
     */
    @Override
    public PerformanceAgent createPerformanceAgent(PerformanceAgent performance) {
        // Validation des données obligatoires
        validatePerformanceData(performance);
        
        // Vérifier que l'agent existe
        if (performance.getAgent() == null || performance.getAgent().getId() == null) {
            throw new RuntimeException("L'agent est obligatoire");
        }
        
        utilisateurRepository.findById(performance.getAgent().getId())
                .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + performance.getAgent().getId()));
        
        // Vérifier qu'il n'y a pas déjà une performance pour cet agent et cette période
        if (performanceAgentRepository.existsByAgentIdAndPeriode(performance.getAgent().getId(), performance.getPeriode())) {
            throw new RuntimeException("Une performance existe déjà pour cet agent et cette période");
        }
        
        // Initialiser les valeurs par défaut
        performance.setDateCalcul(LocalDateTime.now());
        if (performance.getDossiersTraites() == null) performance.setDossiersTraites(0);
        if (performance.getDossiersValides() == null) performance.setDossiersValides(0);
        if (performance.getEnquetesCompletees() == null) performance.setEnquetesCompletees(0);
        if (performance.getScore() == null) performance.setScore(0.0);
        
        // Calculer le taux de réussite
        calculerTauxReussite(performance);
        
        return performanceAgentRepository.save(performance);
    }

    /**
     * Met à jour une performance d'agent existante
     * @param id L'ID de la performance à modifier
     * @param performance Les nouvelles données de la performance
     * @return La performance mise à jour
     * @throws RuntimeException si la performance n'existe pas
     */
    @Override
    public PerformanceAgent updatePerformanceAgent(Long id, PerformanceAgent performance) {
        PerformanceAgent existingPerformance = performanceAgentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Performance non trouvée avec l'ID: " + id));
        
        // Validation des données
        validatePerformanceData(performance);
        
        // Mettre à jour les champs modifiables
        existingPerformance.setDossiersTraites(performance.getDossiersTraites());
        existingPerformance.setDossiersValides(performance.getDossiersValides());
        existingPerformance.setEnquetesCompletees(performance.getEnquetesCompletees());
        existingPerformance.setScore(performance.getScore());
        existingPerformance.setCommentaires(performance.getCommentaires());
        existingPerformance.setObjectif(performance.getObjectif());
        
        // Recalculer le taux de réussite
        calculerTauxReussite(existingPerformance);
        
        return performanceAgentRepository.save(existingPerformance);
    }

    /**
     * Supprime une performance d'agent
     * @param id L'ID de la performance à supprimer
     * @throws RuntimeException si la performance n'existe pas
     */
    @Override
    public void deletePerformanceAgent(Long id) {
        if (!performanceAgentRepository.existsById(id)) {
            throw new RuntimeException("Performance non trouvée avec l'ID: " + id);
        }
        performanceAgentRepository.deleteById(id);
    }

    /**
     * Récupère une performance d'agent par son ID
     * @param id L'ID de la performance
     * @return La performance trouvée
     * @throws RuntimeException si la performance n'existe pas
     */
    @Override
    @Transactional(readOnly = true)
    public PerformanceAgent getPerformanceAgentById(Long id) {
        return performanceAgentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Performance non trouvée avec l'ID: " + id));
    }

    /**
     * Récupère toutes les performances d'agents
     * @return Liste de toutes les performances d'agents
     */
    @Override
    @Transactional(readOnly = true)
    public List<PerformanceAgent> getAllPerformancesAgent() {
        return performanceAgentRepository.findAll();
    }

    /**
     * Récupère les performances par agent
     * @param agentId L'ID de l'agent
     * @return Liste des performances de l'agent
     */
    @Override
    @Transactional(readOnly = true)
    public List<PerformanceAgent> getPerformancesByAgent(Long agentId) {
        return performanceAgentRepository.findByAgentIdOrderByDateCalculDesc(agentId);
    }

    /**
     * Récupère les performances par période
     * @param periode La période des performances
     * @return Liste des performances de la période
     */
    @Override
    @Transactional(readOnly = true)
    public List<PerformanceAgent> getPerformancesByPeriode(String periode) {
        return performanceAgentRepository.findByPeriodeOrderByScoreDesc(periode);
    }

    /**
     * Récupère la performance par agent et période
     * @param agentId L'ID de l'agent
     * @param periode La période de la performance
     * @return La performance trouvée
     * @throws RuntimeException si la performance n'existe pas
     */
    @Override
    @Transactional(readOnly = true)
    public PerformanceAgent getPerformanceByAgentAndPeriode(Long agentId, String periode) {
        PerformanceAgent performance = performanceAgentRepository.findByAgentIdAndPeriode(agentId, periode);
        if (performance == null) {
            throw new RuntimeException("Performance non trouvée pour l'agent " + agentId + " et la période " + periode);
        }
        return performance;
    }

    /**
     * Récupère les performances récentes
     * @param dateDebut Date de début pour la recherche
     * @return Liste des performances récentes
     */
    @Override
    @Transactional(readOnly = true)
    public List<PerformanceAgent> getPerformancesRecent(LocalDateTime dateDebut) {
        return performanceAgentRepository.findPerformancesRecent(dateDebut);
    }

    /**
     * Récupère les meilleures performances
     * @return Liste des meilleures performances triées par score
     */
    @Override
    @Transactional(readOnly = true)
    public List<PerformanceAgent> getTopPerformances() {
        return performanceAgentRepository.findTopPerformances();
    }

    /**
     * Récupère les performances par score minimum
     * @param scoreMin Le score minimum requis
     * @return Liste des performances avec le score minimum
     */
    @Override
    @Transactional(readOnly = true)
    public List<PerformanceAgent> getPerformancesByScoreMin(Double scoreMin) {
        return performanceAgentRepository.findPerformancesByScoreMin(scoreMin);
    }

    /**
     * Récupère les performances par agent et score minimum
     * @param agentId L'ID de l'agent
     * @param scoreMin Le score minimum requis
     * @return Liste des performances de l'agent avec le score minimum
     */
    @Override
    @Transactional(readOnly = true)
    public List<PerformanceAgent> getPerformancesByAgentAndScoreMin(Long agentId, Double scoreMin) {
        return performanceAgentRepository.findPerformancesByAgentAndScoreMin(agentId, scoreMin);
    }

    /**
     * Calcule la performance d'un agent pour une période donnée
     * @param agentId L'ID de l'agent
     * @param periode La période de calcul
     * @return La performance calculée
     * @throws RuntimeException si l'agent n'existe pas
     */
    @Override
    public PerformanceAgent calculerPerformanceAgent(Long agentId, String periode) {
        // Vérifier que l'agent existe
        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + agentId));
        
        // Récupérer ou créer la performance
        PerformanceAgent performance = performanceAgentRepository.findByAgentIdAndPeriode(agentId, periode);
        if (performance == null) {
            performance = PerformanceAgent.builder()
                    .agent(agent)
                    .periode(periode)
                    .dossiersTraites(0)
                    .dossiersValides(0)
                    .enquetesCompletees(0)
                    .score(0.0)
                    .dateCalcul(LocalDateTime.now())
                    .build();
        }
        
        // Calculer les statistiques
        calculerStatistiquesAgent(performance, agentId, periode);
        
        // Calculer le score
        calculerScore(performance);
        
        // Calculer le taux de réussite
        calculerTauxReussite(performance);
        
        return performanceAgentRepository.save(performance);
    }

    /**
     * Calcule les performances pour une période donnée
     * @param periode La période de calcul
     * @return Liste des performances calculées
     */
    @Override
    public List<PerformanceAgent> calculerPerformancesPeriode(String periode) {
        // Récupérer tous les agents
        List<Utilisateur> agents = utilisateurRepository.findAll();
        
        for (Utilisateur agent : agents) {
            calculerPerformanceAgent(agent.getId(), periode);
        }
        
        return performanceAgentRepository.findByPeriode(periode);
    }

    /**
     * Calcule toutes les performances
     * @return Liste de toutes les performances calculées
     */
    @Override
    public List<PerformanceAgent> calculerToutesPerformances() {
        // Récupérer tous les agents
        List<Utilisateur> agents = utilisateurRepository.findAll();
        
        // Périodes à calculer
        String[] periodes = {"2024-01", "2024-02", "2024-03", "2024-04", "2024-05", "2024-06"};
        
        for (Utilisateur agent : agents) {
            for (String periode : periodes) {
                calculerPerformanceAgent(agent.getId(), periode);
            }
        }
        
        return performanceAgentRepository.findAll();
    }

    /**
     * Compte les performances par agent
     * @param agentId L'ID de l'agent
     * @return Nombre de performances de l'agent
     */
    @Override
    @Transactional(readOnly = true)
    public long countPerformancesByAgent(Long agentId) {
        return performanceAgentRepository.countByAgentId(agentId);
    }

    /**
     * Compte les performances par période
     * @param periode La période des performances
     * @return Nombre de performances de la période
     */
    @Override
    @Transactional(readOnly = true)
    public long countPerformancesByPeriode(String periode) {
        return performanceAgentRepository.countByPeriode(periode);
    }

    /**
     * Récupère les performances par taux de réussite minimum
     * @param tauxMin Le taux de réussite minimum
     * @return Liste des performances avec le taux minimum
     */
    @Override
    @Transactional(readOnly = true)
    public List<PerformanceAgent> getPerformancesByTauxReussiteMin(Double tauxMin) {
        return performanceAgentRepository.findByTauxReussiteGreaterThanEqual(tauxMin);
    }

    /**
     * Récupère les performances par agent et taux de réussite minimum
     * @param agentId L'ID de l'agent
     * @param tauxMin Le taux de réussite minimum
     * @return Liste des performances de l'agent avec le taux minimum
     */
    @Override
    @Transactional(readOnly = true)
    public List<PerformanceAgent> getPerformancesByAgentAndTauxReussiteMin(Long agentId, Double tauxMin) {
        return performanceAgentRepository.findByAgentIdAndTauxReussiteGreaterThanEqual(agentId, tauxMin);
    }

    /**
     * Récupère les performances par objectif
     * @return Liste des performances avec objectif
     */
    @Override
    @Transactional(readOnly = true)
    public List<PerformanceAgent> getPerformancesByObjectif() {
        return performanceAgentRepository.findByObjectifNotNull();
    }

    /**
     * Récupère les performances par agent et objectif
     * @param agentId L'ID de l'agent
     * @return Liste des performances de l'agent avec objectif
     */
    @Override
    @Transactional(readOnly = true)
    public List<PerformanceAgent> getPerformancesByAgentAndObjectif(Long agentId) {
        return performanceAgentRepository.findByAgentIdAndObjectifNotNull(agentId);
    }

    /**
     * Récupère les performances par période et objectif
     * @param periode La période des performances
     * @return Liste des performances de la période avec objectif
     */
    @Override
    @Transactional(readOnly = true)
    public List<PerformanceAgent> getPerformancesByPeriodeAndObjectif(String periode) {
        return performanceAgentRepository.findByPeriodeAndObjectifNotNull(periode);
    }

    /**
     * Met à jour le taux de réussite d'une performance
     * @param performanceId L'ID de la performance
     * @return La performance mise à jour
     * @throws RuntimeException si la performance n'existe pas
     */
    @Override
    public PerformanceAgent mettreAJourTauxReussite(Long performanceId) {
        PerformanceAgent performance = getPerformanceAgentById(performanceId);
        calculerTauxReussite(performance);
        return performanceAgentRepository.save(performance);
    }

    /**
     * Met à jour le score d'une performance
     * @param performanceId L'ID de la performance
     * @return La performance mise à jour
     * @throws RuntimeException si la performance n'existe pas
     */
    @Override
    public PerformanceAgent mettreAJourScore(Long performanceId) {
        PerformanceAgent performance = getPerformanceAgentById(performanceId);
        calculerScore(performance);
        return performanceAgentRepository.save(performance);
    }

    /**
     * Vérifie si une performance existe pour un agent et une période
     * @param agentId L'ID de l'agent
     * @param periode La période
     * @return true si la performance existe, false sinon
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existePerformance(Long agentId, String periode) {
        return performanceAgentRepository.existsByAgentIdAndPeriode(agentId, periode);
    }

    /**
     * Valide les données d'une performance
     * @param performance La performance à valider
     * @throws RuntimeException si les données sont invalides
     */
    private void validatePerformanceData(PerformanceAgent performance) {
        if (performance == null) {
            throw new RuntimeException("La performance ne peut pas être nulle");
        }
        
        if (performance.getAgent() == null || performance.getAgent().getId() == null) {
            throw new RuntimeException("L'agent est obligatoire");
        }
        
        if (performance.getPeriode() == null || performance.getPeriode().trim().isEmpty()) {
            throw new RuntimeException("La période est obligatoire");
        }
    }

    /**
     * Calcule les statistiques d'un agent pour une période
     * @param performance La performance à mettre à jour
     * @param agentId L'ID de l'agent
     * @param periode La période
     */
    private void calculerStatistiquesAgent(PerformanceAgent performance, Long agentId, String periode) {
        // Calculer les dossiers traités par l'agent (créés ou assignés)
        List<Dossier> dossiersCrees = dossierRepository.findByAgentCreateurId(agentId);
        List<Dossier> dossiersAssignes = dossierRepository.findByAgentResponsableId(agentId);
        
        // Combiner les dossiers uniques
        java.util.Set<Long> dossierIds = new java.util.HashSet<>();
        dossiersCrees.forEach(d -> dossierIds.add(d.getId()));
        dossiersAssignes.forEach(d -> dossierIds.add(d.getId()));
        
        performance.setDossiersTraites(dossierIds.size());
        
        // Calculer les dossiers validés
        long dossiersValides = dossierIds.stream()
                .mapToLong(id -> {
                    Dossier d = dossierRepository.findById(id).orElse(null);
                    return (d != null && d.getStatut() == Statut.VALIDE) ? 1 : 0;
                })
                .sum();
        performance.setDossiersValides((int) dossiersValides);
        
        // Calculer les enquêtes complétées par l'agent
        List<Enquette> toutesEnquetes = enquetteRepository.findAll();
        java.util.Set<Long> enqueteIds = new java.util.HashSet<>();
        for (Enquette e : toutesEnquetes) {
            if ((e.getAgentCreateur() != null && e.getAgentCreateur().getId().equals(agentId)) ||
                (e.getAgentResponsable() != null && e.getAgentResponsable().getId().equals(agentId))) {
                enqueteIds.add(e.getId());
            }
        }
        performance.setEnquetesCompletees(enqueteIds.size());
        
        // Calculer les tâches urgentes complétées
        List<projet.carthagecreance_backend.Entity.TacheUrgente> tachesUrgentes = tacheUrgenteRepository.findByAgentAssignéId(agentId);
        long tachesCompletees = tachesUrgentes.stream()
                .filter(t -> t.getStatut() == projet.carthagecreance_backend.Entity.StatutTache.TERMINEE)
                .count();
        
        // Mettre à jour la date de calcul
        performance.setDateCalcul(LocalDateTime.now());
    }

    /**
     * Calcule le score d'une performance basé sur toutes les entités
     * @param performance La performance à mettre à jour
     */
    private void calculerScore(PerformanceAgent performance) {
        double score = 0.0;
        Long agentId = performance.getAgent().getId();
        
        // Score basé sur les dossiers validés (30% du score total)
        if (performance.getDossiersTraites() > 0) {
            double tauxDossiersValides = (double) performance.getDossiersValides() / performance.getDossiersTraites();
            score += tauxDossiersValides * 30;
        }
        
        // Score basé sur les enquêtes complétées (20% du score total)
        score += Math.min(performance.getEnquetesCompletees() * 1.5, 20);
        
        // Score basé sur les tâches complétées (20% du score total)
        List<projet.carthagecreance_backend.Entity.TacheUrgente> taches = tacheUrgenteRepository.findByAgentAssignéId(agentId);
        long tachesTotal = taches.size();
        long tachesCompletees = taches.stream()
                .filter(t -> t.getStatut() == projet.carthagecreance_backend.Entity.StatutTache.TERMINEE)
                .count();
        if (tachesTotal > 0) {
            double tauxTachesCompletees = (double) tachesCompletees / tachesTotal;
            score += tauxTachesCompletees * 20;
        }
        
        // Score basé sur les actions amiables (15% du score total)
        List<projet.carthagecreance_backend.Entity.Dossier> dossiersAgent = dossierRepository.findByAgentResponsableId(agentId);
        long actionsTotal = 0;
        for (Dossier d : dossiersAgent) {
            if (d.getActions() != null) {
                actionsTotal += d.getActions().size();
            }
        }
        score += Math.min(actionsTotal * 0.5, 15);
        
        // Score basé sur les audiences gérées (15% du score total)
        long audiencesTotal = 0;
        for (Dossier d : dossiersAgent) {
            if (d.getAudiences() != null) {
                audiencesTotal += d.getAudiences().size();
            }
        }
        score += Math.min(audiencesTotal * 0.5, 15);
        
        performance.setScore(Math.min(score, 100.0)); // Score maximum de 100
    }

    /**
     * Calcule le taux de réussite d'une performance
     * @param performance La performance à mettre à jour
     */
    private void calculerTauxReussite(PerformanceAgent performance) {
        if (performance.getDossiersTraites() > 0) {
            double tauxReussite = (double) performance.getDossiersValides() / performance.getDossiersTraites() * 100;
            performance.setTauxReussite(Math.min(tauxReussite, 100.0));
        } else {
            performance.setTauxReussite(0.0);
        }
    }
}
