package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.EnquetteRepository;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Repository.ValidationEnqueteRepository;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Repository.ActionRepository;
import projet.carthagecreance_backend.Repository.AudienceRepository;
import projet.carthagecreance_backend.Repository.ActionHuissierRepository;
import projet.carthagecreance_backend.Repository.DocumentHuissierRepository;
import projet.carthagecreance_backend.Service.EnquetteService;
import projet.carthagecreance_backend.Service.NotificationService;
import projet.carthagecreance_backend.Service.IaPredictionService;
import projet.carthagecreance_backend.Service.Impl.IaFeatureBuilderService;
import projet.carthagecreance_backend.DTO.IaPredictionResult;
import projet.carthagecreance_backend.Repository.ActionRepository;
import projet.carthagecreance_backend.Repository.AudienceRepository;
import projet.carthagecreance_backend.Repository.ActionHuissierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnquetteServiceImpl implements EnquetteService {

    @Autowired
    private EnquetteRepository enquetteRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ValidationEnqueteRepository validationEnqueteRepository;

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private IaPredictionService iaPredictionService;

    @Autowired
    private IaFeatureBuilderService iaFeatureBuilderService;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private AudienceRepository audienceRepository;

    @Autowired
    private ActionHuissierRepository actionHuissierRepository;
    
    @Autowired
    private DocumentHuissierRepository documentHuissierRepository;
    
    @Autowired
    private projet.carthagecreance_backend.Service.StatistiqueService statistiqueService;
    
    @Autowired
    private projet.carthagecreance_backend.Service.TarifDossierService tarifDossierService;

    private static final Logger logger = LoggerFactory.getLogger(EnquetteServiceImpl.class);

    @Override
    @Transactional
    public Enquette createEnquette(Enquette enquette) {
        // VALIDATION OBLIGATOIRE : Le dossierId doit être fourni
        Long dossierId = enquette.getDossierId();
        if (dossierId == null) {
            throw new RuntimeException("Le dossierId est obligatoire pour créer une enquête");
        }
        
        // Charger et valider le dossier
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier avec l'ID " + dossierId + " non trouvé"));
        
        // Vérifier que le dossier n'a pas déjà une enquête associée
        Optional<Enquette> existingEnquette = enquetteRepository.findByDossierId(dossierId);
        if (existingEnquette.isPresent()) {
            throw new RuntimeException("Le dossier avec l'ID " + dossierId + " a déjà une enquête associée (ID: " + existingEnquette.get().getId() + ")");
        }
        
        // Associer le dossier à l'enquête
        enquette.setDossier(dossier);
        
        // Charger les Utilisateurs à partir des IDs si fournis
        Utilisateur agentCreateur = null;
        if (enquette.getAgentCreateurId() != null) {
            Optional<Utilisateur> agentCreateurOpt = utilisateurRepository.findById(enquette.getAgentCreateurId());
            if (agentCreateurOpt.isPresent()) {
                agentCreateur = agentCreateurOpt.get();
                enquette.setAgentCreateur(agentCreateur);
            } else {
                throw new RuntimeException("Utilisateur avec l'ID " + enquette.getAgentCreateurId() + " non trouvé pour agentCreateur");
            }
        }

        if (enquette.getAgentResponsableId() != null) {
            Optional<Utilisateur> agentResponsable = utilisateurRepository.findById(enquette.getAgentResponsableId());
            if (agentResponsable.isPresent()) {
                enquette.setAgentResponsable(agentResponsable.get());
            } else {
                throw new RuntimeException("Utilisateur avec l'ID " + enquette.getAgentResponsableId() + " non trouvé pour agentResponsable");
            }
        }

        // Appliquer les règles de validation selon le rôle du créateur (même logique que Dossier)
        if (agentCreateur != null) {
            boolean createurEstChef = isChef(agentCreateur);
            if (createurEstChef) {
                // Validation automatique si créé par un chef
                enquette.setValide(true);
                enquette.setDateValidation(LocalDateTime.now());
                enquette.setStatut(Statut.VALIDE);
                Enquette savedEnquette = enquetteRepository.save(enquette);

                // Créer une validation automatique
                ValidationEnquete validation = ValidationEnquete.builder()
                        .enquete(savedEnquette)
                        .agentCreateur(agentCreateur)
                        .chefValidateur(agentCreateur)
                        .statut(StatutValidation.VALIDE)
                        .dateCreation(LocalDateTime.now())
                        .dateValidation(LocalDateTime.now())
                        .build();
                validationEnqueteRepository.save(validation);
                
                // ✅ NOUVEAU : Déclencher la prédiction IA après validation automatique
                triggerIaPrediction(savedEnquette.getDossier().getId(), savedEnquette);
                
                // Recalcul automatique des statistiques (asynchrone)
                try {
                    statistiqueService.recalculerStatistiquesAsync();
                } catch (Exception e) {
                    logger.warn("Erreur lors du recalcul automatique des statistiques après création d'enquête: {}", e.getMessage());
                }
                
                return savedEnquette;
            } else {
                // Enquête en attente si créée par un agent
                if (enquette.getStatut() == null) {
                    enquette.setStatut(Statut.EN_ATTENTE_VALIDATION);
                }
            }
        } else {
            // Par défaut, en attente de validation
            if (enquette.getStatut() == null) {
                enquette.setStatut(Statut.EN_ATTENTE_VALIDATION);
            }
        }

        Enquette savedEnquette = enquetteRepository.save(enquette);

        // Créer une validation en attente si créée par un agent
        if (agentCreateur != null && !isChef(agentCreateur)) {
            ValidationEnquete validation = ValidationEnquete.builder()
                    .enquete(savedEnquette)
                    .agentCreateur(agentCreateur)
                    .statut(StatutValidation.EN_ATTENTE)
                    .dateCreation(LocalDateTime.now())
                    .build();
            validationEnqueteRepository.save(validation);
        }

        // Recalcul automatique des statistiques (asynchrone)
        try {
            statistiqueService.recalculerStatistiquesAsync();
        } catch (Exception e) {
            logger.warn("Erreur lors du recalcul automatique des statistiques après création d'enquête: {}", e.getMessage());
        }

        return savedEnquette;
    }

    @Override
    public Optional<Enquette> getEnquetteById(Long id) {
        // Utiliser findByIdNative() pour éviter les problèmes avec dossier_id = NULL
        // Si cela échoue, essayer findById() en gérant l'exception
        try {
            Optional<Enquette> enquette = enquetteRepository.findByIdNative(id);
            if (enquette.isPresent()) {
                return enquette;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de findByIdNative() pour l'enquête " + id + ": " + e.getMessage());
        }
        
        // Fallback vers findById() standard si findByIdNative() ne fonctionne pas
        try {
        return enquetteRepository.findById(id);
        } catch (Exception e) {
            System.err.println("Erreur lors de findById() pour l'enquête " + id + ": " + e.getMessage());
            // Si l'enquête existe mais dossier_id est NULL, retourner empty
            // L'existence peut être vérifiée avec existsByIdNative()
            return Optional.empty();
        }
    }

    @Override
    public List<Enquette> getAllEnquettes() {
        return enquetteRepository.findAll();
    }
    
    /**
     * Méthode helper pour convertir le résultat Integer de existsByIdNative en boolean
     */
    private boolean checkExistsById(Long id) {
        Integer result = enquetteRepository.existsByIdNative(id);
        return result != null && result > 0;
    }
    
    @Override
    public boolean existsById(Long id) {
        // Utiliser existsByIdNative() pour éviter les problèmes avec dossier_id = NULL
        // La requête native retourne un Integer (0 ou 1), on le convertit en boolean
        return checkExistsById(id);
    }

    @Override
    public Enquette updateEnquette(Long id, Enquette enquette) {
        // Utiliser existsByIdNative() pour éviter les problèmes avec dossier_id = NULL
        if (!checkExistsById(id)) {
            throw new RuntimeException("Enquette not found with id: " + id);
        }
        
            enquette.setId(id);
            
            // Charger les Utilisateurs à partir des IDs si fournis
            if (enquette.getAgentCreateurId() != null) {
                Optional<Utilisateur> agentCreateur = utilisateurRepository.findById(enquette.getAgentCreateurId());
                if (agentCreateur.isPresent()) {
                    enquette.setAgentCreateur(agentCreateur.get());
                } else {
                    throw new RuntimeException("Utilisateur avec l'ID " + enquette.getAgentCreateurId() + " non trouvé pour agentCreateur");
                }
            }

            if (enquette.getAgentResponsableId() != null) {
                Optional<Utilisateur> agentResponsable = utilisateurRepository.findById(enquette.getAgentResponsableId());
                if (agentResponsable.isPresent()) {
                    enquette.setAgentResponsable(agentResponsable.get());
                } else {
                    throw new RuntimeException("Utilisateur avec l'ID " + enquette.getAgentResponsableId() + " non trouvé pour agentResponsable");
                }
            }

        // Essayer de sauvegarder, mais gérer le cas où dossier_id est NULL
        try {
            return enquetteRepository.save(enquette);
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde de l'enquête " + id + ": " + e.getMessage());
            // Si l'erreur est due à dossier_id NULL, on peut quand même mettre à jour les autres champs
            // en utilisant une requête native UPDATE (à implémenter si nécessaire)
            throw new RuntimeException("Erreur lors de la mise à jour de l'enquête: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteEnquette(Long id) {
        // Vérifier d'abord que l'enquête existe avec une requête native
        // Cela évite les problèmes de lazy loading avec la relation Dossier
        boolean exists = checkExistsById(id);
        
        if (!exists) {
            System.err.println("Enquête avec ID " + id + " n'existe pas dans la base de données");
            throw new RuntimeException("Enquette not found with id: " + id);
        }
        
        System.out.println("Enquête ID " + id + " trouvée dans la base de données");
        
        // Supprimer toutes les validations associées à cette enquête
        // Cela permet de supprimer l'enquête même si la validation n'est pas effectuée
        List<ValidationEnquete> validations = validationEnqueteRepository.findByEnqueteId(id);
        System.out.println("Nombre de validations à supprimer: " + validations.size());
        if (!validations.isEmpty()) {
            validationEnqueteRepository.deleteAll(validations);
            System.out.println("Validations supprimées avec succès");
        }
        
        // Supprimer l'enquête directement avec une requête native DELETE
        // Cela évite complètement le chargement de l'entité et les problèmes de relations
        // Même si dossier_id est NULL ou si le Dossier n'existe plus, la suppression fonctionnera
        try {
            enquetteRepository.deleteByIdNative(id);
            System.out.println("deleteByIdNative() appelé pour l'enquête " + id);
        } catch (Exception e) {
            System.err.println("Erreur lors de deleteByIdNative() pour l'enquête " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression de l'enquête: " + e.getMessage(), e);
        }
        
        // Vérifier que la suppression a bien eu lieu avec une requête native
        boolean stillExists = checkExistsById(id);
        if (stillExists) {
            System.err.println("ERREUR: L'enquête " + id + " existe toujours après la suppression !");
            throw new RuntimeException("L'enquête n'a pas pu être supprimée. Une contrainte de base de données empêche probablement la suppression. " +
                    "Vérifiez les relations avec Dossier et autres entités.");
        }
        
        System.out.println("Vérification: L'enquête " + id + " a bien été supprimée de la base de données");
    }

    @Override
    public Optional<Enquette> getEnquetteByDossier(Long dossierId) {
        return enquetteRepository.findByDossierId(dossierId);
    }

    @Override
    public List<Enquette> getEnquettesByCreationDate(LocalDate date) {
        return enquetteRepository.findByDateCreation(date);
    }

    @Override
    public List<Enquette> getEnquettesByCreationDateRange(LocalDate startDate, LocalDate endDate) {
        return enquetteRepository.findByDateCreationBetween(startDate, endDate);
    }

    @Override
    public List<Enquette> getEnquettesBySector(String sector) {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getSecteurActivite() != null && 
                           e.getSecteurActivite().toLowerCase().contains(sector.toLowerCase()))
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesByLegalForm(String legalForm) {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getFormeJuridique() != null && 
                           e.getFormeJuridique().toLowerCase().contains(legalForm.toLowerCase()))
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesByPDG(String pdg) {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getPdg() != null && 
                           e.getPdg().toLowerCase().contains(pdg.toLowerCase()))
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesByCapitalRange(Double minCapital, Double maxCapital) {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getCapital() != null && 
                           e.getCapital() >= minCapital && e.getCapital() <= maxCapital)
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesByRevenueRange(Double minRevenue, Double maxRevenue) {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getChiffreAffaire() != null && 
                           e.getChiffreAffaire() >= minRevenue && e.getChiffreAffaire() <= maxRevenue)
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesByStaffRange(Integer minStaff, Integer maxStaff) {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getEffectif() != null && 
                           e.getEffectif() >= minStaff && e.getEffectif() <= maxStaff)
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesWithRealEstate() {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getBienImmobilier() != null && !e.getBienImmobilier().isEmpty())
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesWithMovableProperty() {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getBienMobilier() != null && !e.getBienMobilier().isEmpty())
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesWithObservations() {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getObservations() != null && !e.getObservations().isEmpty())
                .toList();
    }

    @Override
    @Transactional
    public void validerEnquette(Long enquetteId, Long chefId) {
        // Vérifier que l'enquête existe avec une requête native
        if (!checkExistsById(enquetteId)) {
            throw new RuntimeException("Enquête non trouvée avec l'ID: " + enquetteId);
        }

        // Vérifier que le chef existe et a les droits
        Utilisateur chef = utilisateurRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef non trouvé avec l'ID: " + chefId));

        if (!isChef(chef)) {
            throw new RuntimeException("L'utilisateur avec l'ID " + chefId + " n'est pas autorisé à valider des enquêtes");
        }

        // Mettre à jour l'enquête avec une requête native pour éviter les problèmes avec dossier_id = NULL
        LocalDateTime dateValidation = LocalDateTime.now();
        try {
            enquetteRepository.updateStatutNative(enquetteId, Statut.VALIDE.name(), true, dateValidation, null);
        } catch (Exception e) {
            System.err.println("Erreur lors de updateStatutNative() pour l'enquête " + enquetteId + ": " + e.getMessage());
            // Fallback : essayer de charger et sauvegarder normalement
            try {
                Enquette enquette = enquetteRepository.findByIdNative(enquetteId)
                        .orElseThrow(() -> new RuntimeException("Enquête non trouvée avec l'ID: " + enquetteId));
        enquette.setValide(true);
                enquette.setDateValidation(dateValidation);
        enquette.setStatut(Statut.VALIDE);
        enquetteRepository.save(enquette);
            } catch (Exception e2) {
                throw new RuntimeException("Erreur lors de la validation de l'enquête: " + e2.getMessage(), e2);
            }
        }

        // Mettre à jour la validation
        List<ValidationEnquete> validations = validationEnqueteRepository.findByEnqueteId(enquetteId);
        for (ValidationEnquete validation : validations) {
            if (validation.getStatut() == StatutValidation.EN_ATTENTE) {
                validation.setStatut(StatutValidation.VALIDE);
                validation.setChefValidateur(chef);
                validation.setDateValidation(LocalDateTime.now());
                validationEnqueteRepository.save(validation);
            }
        }

        // Envoyer une notification à l'agent créateur
        // Essayer de charger l'enquête pour obtenir le rapportCode
        String rapportCode = "ID " + enquetteId;
        Enquette enquetteValidated = null;
        try {
            Optional<Enquette> enquetteOpt = enquetteRepository.findByIdNative(enquetteId);
            if (enquetteOpt.isPresent()) {
                enquetteValidated = enquetteOpt.get();
                if (enquetteValidated.getRapportCode() != null) {
                    rapportCode = enquetteValidated.getRapportCode();
                }
            }
        } catch (Exception e) {
            // Utiliser l'ID par défaut si on ne peut pas charger l'enquête
        }
        
        List<ValidationEnquete> enqueteValidations = validationEnqueteRepository.findByEnqueteId(enquetteId);
        for (ValidationEnquete validation : enqueteValidations) {
            if (validation.getAgentCreateur() != null) {
                Notification notification = Notification.builder()
                        .utilisateur(validation.getAgentCreateur())
                        .titre("Enquête validée: " + rapportCode)
                        .message("Votre enquête " + rapportCode + " a été validée par " + chef.getNom() + " " + chef.getPrenom())
                        .type(TypeNotification.DOSSIER_VALIDE) // Utiliser le même type pour l'instant
                        .entiteId(enquetteId)
                        .entiteType(TypeEntite.DOSSIER) // Utiliser le même type pour l'instant
                        .dateCreation(LocalDateTime.now())
                        .build();
                notificationService.createNotification(notification);
            }
        }
        
        // ✅ NOUVEAU : Créer automatiquement le tarif d'enquête (300 TND)
        try {
            if (enquetteValidated != null && enquetteValidated.getDossier() != null) {
                tarifDossierService.createTarifEnqueteAutomatique(enquetteValidated.getDossier(), enquetteValidated);
                logger.info("Tarif d'enquête créé automatiquement pour l'enquête {}", enquetteId);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la création automatique du tarif d'enquête: {}", e.getMessage());
            // Ne pas bloquer la validation si la création du tarif échoue
        }
        
        // ✅ NOUVEAU : Déclencher la prédiction IA après validation
        if (enquetteValidated != null && enquetteValidated.getDossier() != null) {
            triggerIaPrediction(enquetteValidated.getDossier().getId(), enquetteValidated);
        }
        
        // Recalcul automatique des statistiques (asynchrone)
        try {
            statistiqueService.recalculerStatistiquesAsync();
        } catch (Exception e) {
            logger.warn("Erreur lors du recalcul automatique des statistiques après validation d'enquête: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void rejeterEnquette(Long enquetteId, String commentaire) {
        // Vérifier que l'enquête existe avec une requête native
        if (!checkExistsById(enquetteId)) {
            throw new RuntimeException("Enquête non trouvée avec l'ID: " + enquetteId);
        }

        // Mettre à jour l'enquête avec une requête native pour éviter les problèmes avec dossier_id = NULL
        try {
            enquetteRepository.updateStatutNative(enquetteId, Statut.REJETE.name(), false, null, commentaire);
        } catch (Exception e) {
            System.err.println("Erreur lors de updateStatutNative() pour l'enquête " + enquetteId + ": " + e.getMessage());
            // Fallback : essayer de charger et sauvegarder normalement
            try {
                Enquette enquette = enquetteRepository.findByIdNative(enquetteId)
                .orElseThrow(() -> new RuntimeException("Enquête non trouvée avec l'ID: " + enquetteId));
        enquette.setValide(false);
        enquette.setCommentaireValidation(commentaire);
        enquette.setStatut(Statut.REJETE);
        enquetteRepository.save(enquette);
            } catch (Exception e2) {
                throw new RuntimeException("Erreur lors du rejet de l'enquête: " + e2.getMessage(), e2);
            }
        }

        // Mettre à jour la validation
        List<ValidationEnquete> validations = validationEnqueteRepository.findByEnqueteId(enquetteId);
        for (ValidationEnquete validation : validations) {
            if (validation.getStatut() == StatutValidation.EN_ATTENTE) {
                validation.setStatut(StatutValidation.REJETE);
                validation.setCommentaires(commentaire);
                validation.setDateValidation(LocalDateTime.now());
                validationEnqueteRepository.save(validation);
            }
        }

        // Envoyer une notification à l'agent créateur
        // Essayer de charger l'enquête pour obtenir le rapportCode
        String rapportCode = "ID " + enquetteId;
        try {
            Optional<Enquette> enquetteOpt = enquetteRepository.findByIdNative(enquetteId);
            if (enquetteOpt.isPresent() && enquetteOpt.get().getRapportCode() != null) {
                rapportCode = enquetteOpt.get().getRapportCode();
            }
        } catch (Exception e) {
            // Utiliser l'ID par défaut si on ne peut pas charger l'enquête
        }
        
        List<ValidationEnquete> enqueteValidations = validationEnqueteRepository.findByEnqueteId(enquetteId);
        for (ValidationEnquete validation : enqueteValidations) {
            if (validation.getAgentCreateur() != null) {
                Notification notification = Notification.builder()
                        .utilisateur(validation.getAgentCreateur())
                        .titre("Enquête rejetée: " + rapportCode)
                        .message("Votre enquête " + rapportCode + " a été rejetée. Commentaires: " + commentaire)
                        .type(TypeNotification.DOSSIER_REJETE) // Utiliser le même type pour l'instant
                        .entiteId(enquetteId)
                        .entiteType(TypeEntite.DOSSIER) // Utiliser le même type pour l'instant
                        .dateCreation(LocalDateTime.now())
                        .build();
                notificationService.createNotification(notification);
            }
        }
    }

    /**
     * Déclenche la prédiction IA pour un dossier après validation de l'enquête
     * 
     * @param dossierId ID du dossier
     * @param enquette L'enquête validée
     */
    private void triggerIaPrediction(Long dossierId, Enquette enquette) {
        try {
            logger.info("Déclenchement de la prédiction IA pour le dossier {} après validation de l'enquête {}", dossierId, enquette.getId());
            
            // Récupérer le dossier
            Dossier dossier = dossierRepository.findById(dossierId)
                    .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
            
            // Récupérer les données associées
            List<Action> actions = actionRepository.findByDossierId(dossierId);
            List<Audience> audiences = audienceRepository.findByDossierId(dossierId);
            List<ActionHuissier> actionsHuissier = actionHuissierRepository.findByDossierId(dossierId);
            List<DocumentHuissier> documentsHuissier = documentHuissierRepository.findByDossierId(dossierId);
            
            // Construire les features à partir des données réelles
            Map<String, Object> features = iaFeatureBuilderService.buildFeaturesFromRealData(
                dossier,
                enquette,  // Utiliser l'enquête validée
                actions,
                audiences,
                actionsHuissier,
                documentsHuissier
            );
            
            // Prédire avec l'IA
            IaPredictionResult prediction = iaPredictionService.predictRisk(features);
            
            // Mettre à jour le dossier avec les résultats de la prédiction
            dossier.setEtatPrediction(EtatDossier.valueOf(prediction.getEtatFinal()));
            dossier.setRiskScore(prediction.getRiskScore());
            dossier.setRiskLevel(prediction.getRiskLevel());
            dossier.setDatePrediction(LocalDateTime.now());
            
            // Sauvegarder le dossier mis à jour
            dossierRepository.save(dossier);
            
            logger.info("Prédiction IA appliquée au dossier {} après validation de l'enquête: etatPrediction={}, riskScore={}, riskLevel={}", 
                dossierId, prediction.getEtatFinal(), prediction.getRiskScore(), prediction.getRiskLevel());
            
        } catch (Exception e) {
            // En cas d'erreur avec l'IA, on continue quand même (pas bloquant)
            logger.warn("Erreur lors de la prédiction IA pour le dossier {} après validation de l'enquête {}: {}. Le dossier sera sauvegardé sans prédiction.", 
                dossierId, enquette != null ? enquette.getId() : "N/A", e.getMessage());
        }
    }

    /**
     * Vérifie si un utilisateur est un chef
     */
    private boolean isChef(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getRoleUtilisateur() == null) return false;
        String roleName = utilisateur.getRoleUtilisateur().name();
        return roleName.startsWith("CHEF_DEPARTEMENT") || roleName.equals("SUPER_ADMIN");
    }
    
    // ==================== STATISTIQUES ====================
    
    @Override
    public long countTotalEnquettes() {
        return enquetteRepository.count();
    }
    
    @Override
    public long countEnquettesByStatut(Statut statut) {
        return enquetteRepository.countByStatut(statut);
    }
    
    @Override
    public long countEnquettesValides() {
        return enquetteRepository.countByValideTrue();
    }
    
    @Override
    public long countEnquettesNonValides() {
        return enquetteRepository.countByValideFalse();
    }
    
    @Override
    public long countEnquettesCreesCeMois() {
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        return enquetteRepository.countByDateCreationAfter(debutMois);
    }
    
    @Override
    public long countEnquettesByAgentCreateur(Long agentId) {
        return enquetteRepository.countByAgentCreateurId(agentId);
    }
    
    @Override
    public long countEnquettesByAgentResponsable(Long agentId) {
        return enquetteRepository.countByAgentResponsableId(agentId);
    }
}
