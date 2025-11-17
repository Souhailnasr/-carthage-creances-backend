// Fichier : src/main/java/projet/carthagecreance_backend/Service/Impl/DossierServiceImpl.java
package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.Service.DossierService;
import projet.carthagecreance_backend.Service.FileStorageService;
import projet.carthagecreance_backend.Service.NotificationService;
import projet.carthagecreance_backend.Service.TacheUrgenteService;
import projet.carthagecreance_backend.DTO.DossierRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.criteria.Predicate;

/**
 * Implémentation du service de gestion des dossiers avec workflow complet
 * Inclut toutes les opérations CRUD et les fonctionnalités de workflow
 */
@Service
@Transactional
public class DossierServiceImpl implements DossierService {

    private static final Logger logger = LoggerFactory.getLogger(DossierServiceImpl.class);

    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CreancierRepository creancierRepository;

    @Autowired
    private DebiteurRepository debiteurRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ValidationDossierRepository validationDossierRepository;
    
    @Autowired
    private AvocatRepository avocatRepository;
    
    @Autowired
    private HuissierRepository huissierRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TacheUrgenteService tacheUrgenteService;
    
    @Autowired
    private FinanceRepository financeRepository;

    /**
     * Crée un nouveau dossier avec workflow de validation
     * Règles:
     * - Si un agent crée => statut de validation EN_ATTENTE (dossier non validé)
     * - Si un chef crée => validation automatique (dossier validé)
     */
    @Override
    public Dossier createDossier(DossierRequest request) {
        try {
            // 1. Récupérer ou créer le Créancier à partir du nom fourni
            Creancier creancier = creancierRepository.findByNom(request.getNomCreancier())
                    .orElseGet(() -> {
                        // Créer un nouveau créancier si il n'existe pas
                        Creancier nouveauCreancier = Creancier.builder()
                                .nom(request.getNomCreancier())
                                .build();
                        return creancierRepository.save(nouveauCreancier);
                    });

            // 2. Récupérer ou créer le Débiteur à partir du nom fourni
            Debiteur debiteur = debiteurRepository.findByNom(request.getNomDebiteur())
                    .orElseGet(() -> {
                        // Créer un nouveau débiteur si il n'existe pas
                        Debiteur nouveauDebiteur = Debiteur.builder()
                                .nom(request.getNomDebiteur())
                                .build();
                        return debiteurRepository.save(nouveauDebiteur);
                    });

            // 3. Vérifier que l'agent créateur existe (si fourni)
            Utilisateur agentCreateur = null;
            if (request.getAgentCreateurId() != null) {
                agentCreateur = utilisateurRepository.findById(request.getAgentCreateurId())
                        .orElseThrow(() -> new IllegalArgumentException("Agent créateur avec ID " + request.getAgentCreateurId() + " introuvable."));
            }

            // 4. Gérer les fichiers uploadés
            String contratFilePath = null;
            String pouvoirFilePath = null;
            
            if (request.getContratSigneFile() != null && !request.getContratSigneFile().isEmpty()) {
                contratFilePath = fileStorageService.saveFile(request.getContratSigneFile(), "contrat");
            }
            
            if (request.getPouvoirFile() != null && !request.getPouvoirFile().isEmpty()) {
                pouvoirFilePath = fileStorageService.saveFile(request.getPouvoirFile(), "pouvoir");
            }

            // 5. Construire l'entité Dossier avec workflow
            Dossier dossier = Dossier.builder()
                    .titre(request.getTitre())
                    .description(request.getDescription())
                    .numeroDossier(request.getNumeroDossier())
                    .montantCreance(request.getMontantCreance())
                    .contratSigneFilePath(contratFilePath)
                    .pouvoirFilePath(pouvoirFilePath)
                    .urgence(request.getUrgence())
                    .dossierStatus(DossierStatus.ENCOURSDETRAITEMENT)
                    .statut(request.getStatut() != null ? request.getStatut() : Statut.EN_ATTENTE_VALIDATION)
                    .typeDocumentJustificatif(request.getTypeDocumentJustificatif())
                    .creancier(creancier)
                    .debiteur(debiteur)
                    .agentCreateur(agentCreateur)
                    .build();

            // 5. Sauvegarder le Dossier
            Dossier savedDossier = dossierRepository.save(dossier);

            // 5.5. Créer automatiquement une Finance pour ce dossier
            try {
                Finance finance = Finance.builder()
                        .dossier(savedDossier)
                        .devise("TND")
                        .dateOperation(LocalDate.now())
                        .description("Finance pour dossier " + savedDossier.getNumeroDossier())
                        .fraisCreationDossier(50.0)
                        .fraisGestionDossier(10.0)
                        .dureeGestionMois(0)
                        .coutActionsAmiable(0.0)
                        .coutActionsJuridique(0.0)
                        .nombreActionsAmiable(0)
                        .nombreActionsJuridique(0)
                        .factureFinalisee(false)
                        .build();
                financeRepository.save(finance);
                logger.info("Finance créée automatiquement pour le dossier ID: {}", savedDossier.getId());
            } catch (Exception e) {
                logger.error("Erreur lors de la création automatique de Finance pour le dossier {}: {}", 
                           savedDossier.getId(), e.getMessage());
                // Ne pas faire échouer la création du dossier si Finance échoue
                // La Finance pourra être créée manuellement plus tard
            }

            // 6. Appliquer les règles de validation selon le rôle du créateur
            if (agentCreateur != null) {
                boolean createurEstChef = isChef(agentCreateur);
                if (createurEstChef) {
                    // Validation automatique
                    savedDossier.setValide(true);
                    savedDossier.setDateValidation(LocalDateTime.now());
                    savedDossier.setStatut(Statut.VALIDE);
                    savedDossier.setDossierStatus(DossierStatus.ENCOURSDETRAITEMENT);
                    Dossier updated = dossierRepository.save(savedDossier);

                    ValidationDossier validation = ValidationDossier.builder()
                            .dossier(updated)
                            .agentCreateur(agentCreateur)
                            .chefValidateur(agentCreateur)
                            .statut(StatutValidation.VALIDE)
                            .dateCreation(LocalDateTime.now())
                            .dateValidation(LocalDateTime.now())
                            .build();
                    validationDossierRepository.save(validation);
                } else {
                    // Dossier en attente si créé par un agent
                    savedDossier.setStatut(Statut.EN_ATTENTE_VALIDATION);
                    ValidationDossier validation = ValidationDossier.builder()
                            .dossier(savedDossier)
                            .agentCreateur(agentCreateur)
                            .statut(StatutValidation.EN_ATTENTE)
                            .dateCreation(LocalDateTime.now())
                            .build();
                    validationDossierRepository.save(validation);

                    // Notification au chef de département
                    Notification notification = Notification.builder()
                            .utilisateur(getChefDepartementDossier())
                            .titre("Nouveau dossier en attente de validation: " + savedDossier.getTitre())
                            .message("Un nouveau dossier a été créé par " + agentCreateur.getNom() + " " + agentCreateur.getPrenom())
                            .type(TypeNotification.DOSSIER_EN_ATTENTE)
                            .entiteId(savedDossier.getId())
                            .entiteType(TypeEntite.DOSSIER)
                            .dateCreation(LocalDateTime.now())
                            .build();
                    notificationService.createNotification(notification);
                }
            }

            return savedDossier;
        } catch (IllegalArgumentException e) {
            logger.warn("Validation createDossier échouée: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erreur createDossier: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la création du dossier : " + e.getMessage(), e);
        }
    }

    @Override
    public Dossier createDossierWithFiles(DossierRequest request, MultipartFile pouvoirFile, MultipartFile contratSigneFile) {
        try {
            // 1. Sauvegarder les fichiers avec FileStorageService
            String pouvoirFilePath = null;
            String contratSigneFilePath = null;
            
            if (pouvoirFile != null && !pouvoirFile.isEmpty()) {
                pouvoirFilePath = fileStorageService.saveFile(pouvoirFile, "pouvoir");
            }
            
            if (contratSigneFile != null && !contratSigneFile.isEmpty()) {
                contratSigneFilePath = fileStorageService.saveFile(contratSigneFile, "contrat");
            }
            
            // 2. Créer un nouveau DossierRequest avec les chemins des fichiers
            DossierRequest dossierWithFiles = new DossierRequest();
            dossierWithFiles.setTitre(request.getTitre());
            dossierWithFiles.setDescription(request.getDescription());
            dossierWithFiles.setNumeroDossier(request.getNumeroDossier());
            dossierWithFiles.setMontantCreance(request.getMontantCreance());
            dossierWithFiles.setUrgence(request.getUrgence());
            dossierWithFiles.setDossierStatus(request.getDossierStatus());
            dossierWithFiles.setTypeDocumentJustificatif(request.getTypeDocumentJustificatif());
            dossierWithFiles.setNomCreancier(request.getNomCreancier());
            dossierWithFiles.setNomDebiteur(request.getNomDebiteur());
            dossierWithFiles.setAgentCreateurId(request.getAgentCreateurId());
            
            // Ajouter les chemins des fichiers
            dossierWithFiles.setContratSigneFilePath(contratSigneFilePath);
            dossierWithFiles.setPouvoirFilePath(pouvoirFilePath);
            
            // 3. Créer le dossier avec les chemins des fichiers
            Dossier createdDossier = createDossier(dossierWithFiles);
            
            // 4. Retourner le dossier sauvegardé
            return createdDossier;
            
        } catch (IllegalArgumentException e) {
            // Nettoyer les fichiers partiellement uploadés en cas d'erreur
            try {
                if (pouvoirFile != null && !pouvoirFile.isEmpty()) {
                    fileStorageService.deleteFile(fileStorageService.saveFile(pouvoirFile, "pouvoir"));
                }
                if (contratSigneFile != null && !contratSigneFile.isEmpty()) {
                    fileStorageService.deleteFile(fileStorageService.saveFile(contratSigneFile, "contrat"));
                }
            } catch (Exception cleanupException) {
                // Log l'erreur de nettoyage mais ne pas la propager
            }
            throw e;
        } catch (Exception e) {
            // Nettoyer les fichiers partiellement uploadés en cas d'erreur
            try {
                if (pouvoirFile != null && !pouvoirFile.isEmpty()) {
                    fileStorageService.deleteFile(fileStorageService.saveFile(pouvoirFile, "pouvoir"));
                }
                if (contratSigneFile != null && !contratSigneFile.isEmpty()) {
                    fileStorageService.deleteFile(fileStorageService.saveFile(contratSigneFile, "contrat"));
                }
            } catch (Exception cleanupException) {
                // Log l'erreur de nettoyage mais ne pas la propager
            }
            throw new RuntimeException("Erreur lors de la création du dossier avec fichiers : " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Dossier> getDossierById(Long id) {
        return dossierRepository.findById(id);
    }

    @Override
    public List<Dossier> getAllDossiers() {
        return dossierRepository.findAll();
    }

    /**
     * Met à jour un dossier avec validation
     * Vérifie les droits et envoie des notifications si nécessaire
     */
    @Override
    public Dossier updateDossier(Long id, Dossier dossierDetails) {
        Optional<Dossier> optionalDossier = dossierRepository.findById(id);
        if (optionalDossier.isPresent()) {
            Dossier existingDossier = optionalDossier.get();
            
            // Mettre à jour les champs modifiables
            existingDossier.setTitre(dossierDetails.getTitre());
            existingDossier.setDescription(dossierDetails.getDescription());
            existingDossier.setNumeroDossier(dossierDetails.getNumeroDossier());
            existingDossier.setMontantCreance(dossierDetails.getMontantCreance());
            existingDossier.setContratSigneFilePath(dossierDetails.getContratSigneFilePath());
            existingDossier.setPouvoirFilePath(dossierDetails.getPouvoirFilePath());
            existingDossier.setUrgence(dossierDetails.getUrgence());
            existingDossier.setDossierStatus(dossierDetails.getDossierStatus());
            existingDossier.setTypeDocumentJustificatif(dossierDetails.getTypeDocumentJustificatif());
            existingDossier.setDateCloture(dossierDetails.getDateCloture());
            existingDossier.setAvocat(dossierDetails.getAvocat());
            existingDossier.setHuissier(dossierDetails.getHuissier());

            Dossier updatedDossier = dossierRepository.save(existingDossier);

            // Envoyer une notification si le statut a changé
            if (!existingDossier.getDossierStatus().equals(dossierDetails.getDossierStatus())) {
                Notification notification = Notification.builder()
                        .utilisateur(getChefDepartementDossier())
                        .titre("Dossier modifié: " + updatedDossier.getTitre())
                        .message("Le dossier " + updatedDossier.getNumeroDossier() + " a été modifié. Nouveau statut: " + dossierDetails.getDossierStatus())
                        .type(TypeNotification.INFO)
                        .entiteId(updatedDossier.getId())
                        .entiteType(TypeEntite.DOSSIER)
                        .dateCreation(LocalDateTime.now())
                        .build();
                notificationService.createNotification(notification);
            }

            return updatedDossier;
        } else {
            logger.warn("updateDossier: dossier {} introuvable", id);
            throw new RuntimeException("Dossier not found with id: " + id);
        }
    }

    /**
     * Supprime un dossier avec vérifications
     * Vérifie qu'il n'y a pas de validations en cours
     */
    @Override
    public void deleteDossier(Long id) {
        Optional<Dossier> dossier = dossierRepository.findById(id);
        if (dossier.isPresent()) {
            // Vérifier s'il y a des validations en cours
            List<ValidationDossier> validations = validationDossierRepository.findByDossierId(id);
            if (!validations.isEmpty()) {
                logger.warn("deleteDossier: validations en cours pour dossier {}", id);
                throw new RuntimeException("Impossible de supprimer le dossier: des validations sont en cours");
            }
            
            // Supprimer le dossier
            dossierRepository.deleteById(id);
            
            // Envoyer une notification
            Notification notification = Notification.builder()
                    .utilisateur(getChefDepartementDossier())
                    .titre("Dossier supprimé: " + dossier.get().getTitre())
                    .message("Le dossier " + dossier.get().getNumeroDossier() + " a été supprimé")
                    .type(TypeNotification.INFO)
                    .entiteId(null)
                    .entiteType(TypeEntite.DOSSIER)
                    .dateCreation(LocalDateTime.now())
                    .build();
            notificationService.createNotification(notification);
        } else {
            logger.warn("deleteDossier: dossier {} introuvable", id);
            throw new RuntimeException("Dossier not found with id: " + id);
        }
    }

    @Override
    public Optional<Dossier> getDossierByNumber(String numeroDossier) {
        return dossierRepository.findByNumeroDossier(numeroDossier);
    }

    @Override
    public List<Dossier> getDossiersByTitle(String title) {
        return dossierRepository.findByTitreContainingIgnoreCase(title);
    }

    @Override
    public List<Dossier> getDossiersByDescription(String description) {
        return dossierRepository.findByDescriptionContainingIgnoreCase(description);
    }

    @Override
    public List<Dossier> getDossiersByUrgency(Urgence urgency) {
        return dossierRepository.findByUrgence(urgency);
    }

    @Override
    public List<Dossier> getDossiersByAvocat(Long avocatId) {
        return dossierRepository.findByAvocatId(avocatId);
    }

    @Override
    public List<Dossier> getDossiersByHuissier(Long huissierId) {
        return dossierRepository.findByHuissierId(huissierId);
    }

    @Override
    public List<Dossier> getDossiersByCreancier(Long creancierId) {
        return dossierRepository.findByCreancierId(creancierId);
    }

    @Override
    public List<Dossier> getDossiersByDebiteur(Long debiteurId) {
        return dossierRepository.findByDebiteurId(debiteurId);
    }

    @Override
    public List<Dossier> getDossiersByUser(Long userId) {
        // Implémentation requise si utilisé
        // return dossierRepository.findByUtilisateurId(userId);
        // Si la méthode n'est pas implémentée dans le repository ou le service, lever une exception
        throw new UnsupportedOperationException("Méthode getDossiersByUser non implémentée ou non supportée.");
        // Ou retourner une liste vide si préféré :
        // return Collections.emptyList();
    }

    @Override
    public List<Dossier> getDossiersByCreationDate(Date date) {
        return dossierRepository.findByDateCreation(date);
    }

    @Override
    public List<Dossier> getDossiersByCreationDateRange(Date startDate, Date endDate) {
        return dossierRepository.findByDateCreationBetween(startDate, endDate);
    }

    @Override
    public List<Dossier> getDossiersByClosureDate(Date date) {
        return dossierRepository.findByDateCloture(date);
    }

    @Override
    public List<Dossier> getDossiersByAmount(Double amount) {
        return dossierRepository.findByMontantCreance(amount);
    }

    @Override
    public List<Dossier> getDossiersByAmountRange(Double minAmount, Double maxAmount) {
        return dossierRepository.findByMontantCreanceBetween(minAmount, maxAmount);
    }

    @Override
    public List<Dossier> searchDossiers(String searchTerm) {
        return dossierRepository.findByTitreOuDescriptionContaining(searchTerm);
    }

    @Override
    public List<Dossier> getOpenDossiers() {
        return dossierRepository.findDossiersOuverts();
    }

    @Override
    public List<Dossier> getClosedDossiers() {
        return dossierRepository.findDossiersFermes();
    }

    @Override
    public List<Dossier> getRecentDossiers() {
        // Derniers 30 jours
        long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
        Date thirtyDaysAgo = new Date(System.currentTimeMillis() - thirtyDaysInMillis);
        return dossierRepository.findDossiersRecents(thirtyDaysAgo);
    }

    @Override
    public boolean existsByNumber(String numeroDossier) {
        return dossierRepository.existsByNumeroDossier(numeroDossier);
    }

    // ==================== Nouvelles méthodes de workflow ====================

    @Override
    public List<Dossier> getDossiersEnAttente() {
        // En attente = validations avec statut EN_ATTENTE
        List<ValidationDossier> validations = validationDossierRepository.findDossiersEnAttente();
        return validations.stream()
                .map(ValidationDossier::getDossier)
                .distinct()
                .toList();
    }

    @Override
    public List<Dossier> getDossiersByValidationStatut(StatutValidation statut) {
        List<ValidationDossier> validations;
        switch (statut) {
            case EN_ATTENTE:
                validations = validationDossierRepository.findDossiersEnAttente();
                break;
            case VALIDE:
                validations = validationDossierRepository.findByStatut(StatutValidation.VALIDE);
                break;
            case REJETE:
                validations = validationDossierRepository.findByStatut(StatutValidation.REJETE);
                break;
            default:
                validations = List.of();
        }
        return validations.stream()
                .map(ValidationDossier::getDossier)
                .distinct()
                .toList();
    }

    @Override
    public List<Dossier> getDossiersValides() {
        return dossierRepository.findByStatut(Statut.VALIDE);
    }

    @Override
    public List<Dossier> getDossiersParAgentEtStatut(Long agentId, Statut statut) {
        return dossierRepository.findByStatutAndAgentResponsableId(statut, agentId);
    }

    @Override
    public List<Dossier> getDossiersByAgent(Long agentId) {
        return dossierRepository.findByUtilisateurId(agentId);
    }

    @Override
    public List<Dossier> getDossiersCreesByAgent(Long agentId) {
        return dossierRepository.findByAgentCreateurId(agentId);
    }

    @Override
    public void validerDossier(Long dossierId, Long chefId) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));

        // Vérifier que le chef existe et a les droits
        Utilisateur chef = utilisateurRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef non trouvé avec l'ID: " + chefId));

        if (!isChef(chef)) {
            throw new RuntimeException("L'utilisateur avec l'ID " + chefId + " n'est pas autorisé à valider des dossiers");
        }

        // Valider le dossier (marquer comme validé SANS clôturer)
        dossier.setValide(true);
        dossier.setDateValidation(LocalDateTime.now());
        dossier.setStatut(Statut.VALIDE);
        dossier.setDossierStatus(DossierStatus.ENCOURSDETRAITEMENT);
        // dateCloture reste NULL - sera assignée seulement lors de la clôture explicite
        dossierRepository.save(dossier);

        // Mettre à jour la validation
        List<ValidationDossier> validations = validationDossierRepository.findByDossierId(dossierId);
        for (ValidationDossier validation : validations) {
            if (validation.getStatut() == StatutValidation.EN_ATTENTE) {
                validation.setStatut(StatutValidation.VALIDE);
                validation.setChefValidateur(chef);
                validation.setDateValidation(LocalDateTime.now());
                validationDossierRepository.save(validation);
            }
        }

        // Envoyer une notification à l'agent créateur
        List<ValidationDossier> dossierValidations = validationDossierRepository.findByDossierId(dossierId);
        for (ValidationDossier validation : dossierValidations) {
            if (validation.getAgentCreateur() != null) {
                Notification notification = Notification.builder()
                        .utilisateur(validation.getAgentCreateur())
                        .titre("Dossier validé: " + dossier.getTitre())
                        .message("Votre dossier " + dossier.getNumeroDossier() + " a été validé par " + chef.getNom() + " " + chef.getPrenom())
                        .type(TypeNotification.DOSSIER_VALIDE)
                        .entiteId(dossier.getId())
                        .entiteType(TypeEntite.DOSSIER)
                        .dateCreation(LocalDateTime.now())
                        .build();
                notificationService.createNotification(notification);
            }
        }

    }

    @Override
    public void rejeterDossier(Long dossierId, String commentaire) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));

        // Mettre à jour le dossier (reste non validé pour correction)
        dossier.setValide(false);
        dossier.setCommentaireValidation(commentaire);
        dossier.setStatut(Statut.REJETE);
        dossierRepository.save(dossier);

        // Mettre à jour la validation
        List<ValidationDossier> validations = validationDossierRepository.findByDossierId(dossierId);
        for (ValidationDossier validation : validations) {
            if (validation.getStatut() == StatutValidation.EN_ATTENTE) {
                validation.setStatut(StatutValidation.REJETE);
                validation.setCommentaires(commentaire);
                validation.setDateValidation(LocalDateTime.now());
                validationDossierRepository.save(validation);
            }
        }

        // Envoyer une notification à l'agent créateur
        List<ValidationDossier> dossierValidations = validationDossierRepository.findByDossierId(dossierId);
        for (ValidationDossier validation : dossierValidations) {
            if (validation.getAgentCreateur() != null) {
                Notification notification = Notification.builder()
                        .utilisateur(validation.getAgentCreateur())
                        .titre("Dossier rejeté: " + dossier.getTitre())
                        .message("Votre dossier " + dossier.getNumeroDossier() + " a été rejeté. Commentaires: " + commentaire)
                        .type(TypeNotification.DOSSIER_REJETE)
                        .entiteId(dossier.getId())
                        .entiteType(TypeEntite.DOSSIER)
                        .dateCreation(LocalDateTime.now())
                        .build();
                notificationService.createNotification(notification);
            }
        }

    }

    // ==================== Méthodes de comptage ====================

    @Override
    public long countTotalDossiers() {
        return dossierRepository.count();
    }

    @Override
    public long countDossiersEnCours() {
        // En cours = dossiers en attente de validation
        return validationDossierRepository.countByStatut(StatutValidation.EN_ATTENTE);
    }

    @Override
    public long countDossiersValides() {
        // Valides = validations avec statut VALIDE
        return validationDossierRepository.countByStatut(StatutValidation.VALIDE);
    }

    @Override
    public long countDossiersCreesCeMois() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date debutMois = calendar.getTime();
        
        return dossierRepository.countByDateCreationAfter(debutMois);
    }

    @Override
    public long countDossiersByAgent(Long agentId) {
        return dossierRepository.countByUtilisateurId(agentId);
    }

    @Override
    public long countDossiersCreesByAgent(Long agentId) {
        return dossierRepository.countByAgentCreateurId(agentId);
    }

    @Override
    @Transactional
    public Dossier assignerAgentResponsable(Long dossierId, Long agentId) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Vérifier que l'agent existe
        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + agentId));
        
        // Assigner l'agent comme agent responsable
        dossier.setAgentResponsable(agent);
        
        return dossierRepository.save(dossier);
    }

    @Override
    @Transactional
    public Dossier assignerAvocat(Long dossierId, Long avocatId) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Vérifier que l'avocat existe
        Avocat avocat = avocatRepository.findById(avocatId)
                .orElseThrow(() -> new RuntimeException("Avocat non trouvé avec l'ID: " + avocatId));
        
        // Assigner l'avocat
        dossier.setAvocat(avocat);
        
        return dossierRepository.save(dossier);
    }

    @Override
    @Transactional
    public Dossier assignerHuissier(Long dossierId, Long huissierId) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Vérifier que l'huissier existe
        Huissier huissier = huissierRepository.findById(huissierId)
                .orElseThrow(() -> new RuntimeException("Huissier non trouvé avec l'ID: " + huissierId));
        
        // Assigner l'huissier
        dossier.setHuissier(huissier);
        
        return dossierRepository.save(dossier);
    }
    
    @Override
    @Transactional
    public Dossier affecterAvocatEtHuissier(Long dossierId, Long avocatId, Long huissierId) {
        logger.info("Affectation d'avocat/huissier au dossier {}: avocatId={}, huissierId={}", dossierId, avocatId, huissierId);
        
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Gérer l'affectation de l'avocat
        if (avocatId != null) {
            // Vérifier que l'avocat existe
            Avocat avocat = avocatRepository.findById(avocatId)
                    .orElseThrow(() -> new RuntimeException("Avocat non trouvé avec l'ID: " + avocatId));
            dossier.setAvocat(avocat);
            logger.info("Avocat {} affecté au dossier {}", avocatId, dossierId);
        } else {
            // Retirer l'affectation de l'avocat si null
            dossier.setAvocat(null);
            logger.info("Affectation de l'avocat retirée du dossier {}", dossierId);
        }
        
        // Gérer l'affectation de l'huissier
        if (huissierId != null) {
            // Vérifier que l'huissier existe
            Huissier huissier = huissierRepository.findById(huissierId)
                    .orElseThrow(() -> new RuntimeException("Huissier non trouvé avec l'ID: " + huissierId));
            dossier.setHuissier(huissier);
            logger.info("Huissier {} affecté au dossier {}", huissierId, dossierId);
        } else {
            // Retirer l'affectation de l'huissier si null
            dossier.setHuissier(null);
            logger.info("Affectation de l'huissier retirée du dossier {}", dossierId);
        }
        
        // Vérifier qu'au moins un des deux est affecté
        if (avocatId == null && huissierId == null) {
            logger.warn("Aucun avocat ni huissier n'a été affecté au dossier {}", dossierId);
        }
        
        return dossierRepository.save(dossier);
    }
    
    @Override
    @Transactional
    public Dossier affecterAuRecouvrementAmiable(Long dossierId) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Vérifier que le dossier est validé
        if (dossier.getStatut() != Statut.VALIDE || !Boolean.TRUE.equals(dossier.getValide())) {
            throw new RuntimeException("Seuls les dossiers validés peuvent être affectés au recouvrement amiable");
        }
        
        // Vérifier que le dossier n'est pas déjà clôturé
        if (dossier.getDossierStatus() == DossierStatus.CLOTURE) {
            throw new RuntimeException("Un dossier clôturé ne peut pas être affecté");
        }
        
        // Vérifier qu'un dossier avec avocat ou huissier ne peut pas être affecté à l'amiable
        if (dossier.getAvocat() != null || dossier.getHuissier() != null) {
            throw new RuntimeException("Un dossier avec un avocat ou un huissier ne peut pas être affecté au recouvrement amiable");
        }
        
        // Trouver le chef du département recouvrement amiable
        List<Utilisateur> chefsAmiables = utilisateurRepository.findByRoleUtilisateur(
            RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE
        );
        
        if (chefsAmiables.isEmpty()) {
            throw new RuntimeException("Aucun chef du département recouvrement amiable trouvé");
        }
        
        // Prendre le premier chef amiable disponible
        Utilisateur chefAmiable = chefsAmiables.get(0);
        
        // Assigner le chef comme agent responsable
        dossier.setAgentResponsable(chefAmiable);
        
        // Mettre à jour le type de recouvrement
        dossier.setTypeRecouvrement(TypeRecouvrement.AMIABLE);
        
        // Initialiser la liste utilisateurs si elle est null
        if (dossier.getUtilisateurs() == null) {
            dossier.setUtilisateurs(new ArrayList<>());
        }
        
        // Ajouter le chef à la liste des utilisateurs associés (éviter les doublons)
        if (!dossier.getUtilisateurs().contains(chefAmiable)) {
            dossier.getUtilisateurs().add(chefAmiable);
        }
        
        // Récupérer tous les agents du département recouvrement amiable
        List<Utilisateur> agentsAmiables = utilisateurRepository.findByRoleUtilisateur(
            RoleUtilisateur.AGENT_RECOUVREMENT_AMIABLE
        );
        
        // Ajouter les agents à la liste des utilisateurs associés
        for (Utilisateur agent : agentsAmiables) {
            if (!dossier.getUtilisateurs().contains(agent)) {
                dossier.getUtilisateurs().add(agent);
            }
        }
        
        // Sauvegarder
        return dossierRepository.save(dossier);
    }
    
    @Override
    @Transactional
    public Dossier affecterAuRecouvrementJuridique(Long dossierId) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Vérifier que le dossier est validé
        if (dossier.getStatut() != Statut.VALIDE || !Boolean.TRUE.equals(dossier.getValide())) {
            throw new RuntimeException("Seuls les dossiers validés peuvent être affectés au recouvrement juridique");
        }
        
        // Vérifier que le dossier n'est pas déjà clôturé
        if (dossier.getDossierStatus() == DossierStatus.CLOTURE) {
            throw new RuntimeException("Un dossier clôturé ne peut pas être affecté");
        }
        
        // Trouver le chef du département recouvrement juridique
        List<Utilisateur> chefsJuridiques = utilisateurRepository.findByRoleUtilisateur(
            RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE
        );
        
        if (chefsJuridiques.isEmpty()) {
            throw new RuntimeException("Aucun chef du département recouvrement juridique trouvé");
        }
        
        // Prendre le premier chef juridique disponible
        Utilisateur chefJuridique = chefsJuridiques.get(0);
        
        // Assigner le chef comme agent responsable
        dossier.setAgentResponsable(chefJuridique);
        
        // Mettre à jour le type de recouvrement
        dossier.setTypeRecouvrement(TypeRecouvrement.JURIDIQUE);
        
        // Initialiser la liste utilisateurs si elle est null
        if (dossier.getUtilisateurs() == null) {
            dossier.setUtilisateurs(new ArrayList<>());
        }
        
        // Ajouter le chef à la liste des utilisateurs associés (éviter les doublons)
        if (!dossier.getUtilisateurs().contains(chefJuridique)) {
            dossier.getUtilisateurs().add(chefJuridique);
        }
        
        // Récupérer tous les agents du département recouvrement juridique
        List<Utilisateur> agentsJuridiques = utilisateurRepository.findByRoleUtilisateur(
            RoleUtilisateur.AGENT_RECOUVREMENT_JURIDIQUE
        );
        
        // Ajouter les agents à la liste des utilisateurs associés
        for (Utilisateur agent : agentsJuridiques) {
            if (!dossier.getUtilisateurs().contains(agent)) {
                dossier.getUtilisateurs().add(agent);
            }
        }
        
        // Sauvegarder
        return dossierRepository.save(dossier);
    }
    
    @Override
    @Transactional
    public Dossier cloturerDossier(Long dossierId) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Vérifier que le dossier est validé
        if (dossier.getStatut() != Statut.VALIDE || !Boolean.TRUE.equals(dossier.getValide())) {
            throw new RuntimeException("Seuls les dossiers validés peuvent être clôturés");
        }
        
        // Vérifier que le dossier n'est pas déjà clôturé
        if (dossier.getDossierStatus() == DossierStatus.CLOTURE) {
            throw new RuntimeException("Ce dossier est déjà clôturé");
        }
        
        // Clôturer le dossier
        dossier.setDossierStatus(DossierStatus.CLOTURE);
        dossier.setDateCloture(new java.util.Date());
        Dossier savedDossier = dossierRepository.save(dossier);
        
        // Mettre à jour la Finance avec la durée de gestion
        try {
            Optional<Finance> financeOpt = financeRepository.findByDossierId(dossierId);
            if (financeOpt.isPresent()) {
                Finance finance = financeOpt.get();
                
                // Calculer la durée de gestion en mois
                if (dossier.getDateCreation() != null) {
                    LocalDate dateCreation = dossier.getDateCreation().toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                    LocalDate dateCloture = LocalDate.now();
                    
                    long mois = ChronoUnit.MONTHS.between(dateCreation, dateCloture);
                    // Arrondir au mois supérieur
                    int dureeMois = (int) Math.max(1, mois + 1);
                    
                    finance.setDureeGestionMois(dureeMois);
                    financeRepository.save(finance);
                    logger.info("Finance mise à jour pour dossier {} : durée gestion = {} mois", dossierId, dureeMois);
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de Finance pour le dossier {}: {}", dossierId, e.getMessage());
            // Ne pas faire échouer la clôture si Finance échoue
        }
        
        return savedDossier;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDossiersValidesDisponibles(int page, int size, String sort, String direction, String search) {
        // Créer la pagination
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        // Créer la spécification pour filtrer les dossiers validés et non clôturés
        Specification<Dossier> spec = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            
            // Statut = VALIDE
            predicates.add(cb.equal(root.get("statut"), Statut.VALIDE));
            
            // valide = true
            predicates.add(cb.equal(root.get("valide"), true));
            
            // dossierStatus != CLOTURE
            predicates.add(cb.notEqual(root.get("dossierStatus"), DossierStatus.CLOTURE));
            
            // Recherche optionnelle
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                    cb.like(cb.lower(root.get("numeroDossier")), searchPattern),
                    cb.like(cb.lower(root.get("titre")), searchPattern),
                    cb.like(cb.lower(root.get("description")), searchPattern)
                );
                predicates.add(searchPredicate);
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // Exécuter la requête avec pagination
        Page<Dossier> dossierPage = dossierRepository.findAll(spec, pageable);
        
        // Construire la réponse
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", dossierPage.getContent());
        response.put("totalElements", dossierPage.getTotalElements());
        response.put("totalPages", dossierPage.getTotalPages());
        response.put("currentPage", dossierPage.getNumber());
        response.put("size", dossierPage.getSize());
        response.put("numberOfElements", dossierPage.getNumberOfElements());
        response.put("first", dossierPage.isFirst());
        response.put("last", dossierPage.isLast());
        
        return response;
    }

    // ==================== Méthodes utilitaires ====================

    /**
     * Récupère le chef de département des dossiers
     * @return Le chef de département des dossiers
     */
    private Utilisateur getChefDepartementDossier() {
        return utilisateurRepository.findByRoleUtilisateur(RoleUtilisateur.CHEF_DEPARTEMENT_DOSSIER)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun chef de département des dossiers trouvé"));
    }

    private boolean isChef(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getRoleUtilisateur() == null) return false;
        String roleName = utilisateur.getRoleUtilisateur().name();
        return roleName.startsWith("CHEF_DEPARTEMENT");
    }

    @Override
    public Map<String, Object> getAllDossiersWithPagination(String role, Long userId, int page, int size, String search) {
        logger.info("=== DÉBUT getAllDossiersWithPagination ===");
        logger.info("Paramètres - role: {}, userId: {}, page: {}, size: {}, search: {}", role, userId, page, size, search);

        try {
            // Créer la spécification pour les filtres
            Specification<Dossier> spec = createDossierSpecification(role, userId, search);
            
            // Créer la pagination avec tri par date de création (plus récent en premier)
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateCreation"));
            
            // Exécuter la requête paginée avec gestion d'erreurs robuste
            Page<Dossier> dossierPage;
            try {
                logger.info("Tentative de récupération des dossiers avec pagination...");
                dossierPage = dossierRepository.findAll(spec, pageable);
                logger.info("Récupération réussie: {} dossiers trouvés", dossierPage.getContent().size());
                
            } catch (Exception e) {
                logger.error("Erreur lors de la récupération des dossiers: {}", e.getMessage());
                
                // Gestion spécifique de l'erreur d'enum
                if (e.getMessage().contains("No enum constant") || e.getMessage().contains("DossierStatus")) {
                    logger.error("ERREUR CRITIQUE: Valeurs invalides dans dossier_status détectées!");
                    logger.error("Détails de l'erreur: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                    
                    // Retourner une page vide avec message d'erreur explicite
                    dossierPage = new org.springframework.data.domain.PageImpl<>(
                        new java.util.ArrayList<>(), 
                        pageable, 
                        0
                    );
                    
                    logger.error("SOLUTION REQUISE: Exécutez le script de nettoyage de la base de données");
                    throw new RuntimeException("Erreur de données dans la base de données. Valeurs invalides détectées dans dossier_status. Veuillez exécuter le script de nettoyage.", e);
                    
                } else {
                    logger.error("Erreur inattendue: {}", e.getMessage());
                    throw new RuntimeException("Erreur lors de la récupération des dossiers: " + e.getMessage(), e);
                }
            }
            
            logger.info("Résultat pagination - totalElements: {}, totalPages: {}, currentPage: {}, size: {}", 
                       dossierPage.getTotalElements(), dossierPage.getTotalPages(), 
                       dossierPage.getNumber(), dossierPage.getSize());

            // Construire la réponse
            Map<String, Object> result = Map.of(
                "content", dossierPage.getContent(),
                "totalElements", dossierPage.getTotalElements(),
                "totalPages", dossierPage.getTotalPages(),
                "currentPage", dossierPage.getNumber(),
                "size", dossierPage.getSize(),
                "first", dossierPage.isFirst(),
                "last", dossierPage.isLast(),
                "numberOfElements", dossierPage.getNumberOfElements()
            );

            return result;

        } catch (Exception e) {
            logger.error("Erreur dans getAllDossiersWithPagination: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération des dossiers paginés: " + e.getMessage(), e);
        }
    }

    /**
     * Crée une spécification JPA pour filtrer les dossiers
     */
    private Specification<Dossier> createDossierSpecification(String role, Long userId, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            // Filtre par rôle et utilisateur
            if (role != null && role.toUpperCase().startsWith("AGENT") && userId != null) {
                // Agent ne voit que ses dossiers (créés ou responsables)
                Predicate agentCreateur = criteriaBuilder.equal(root.get("agentCreateur").get("id"), userId);
                Predicate agentResponsable = criteriaBuilder.equal(root.get("agentResponsable").get("id"), userId);
                predicates.add(criteriaBuilder.or(agentCreateur, agentResponsable));
            }
            // Pour les chefs, pas de filtre supplémentaire (ils voient tous les dossiers)

            // Filtre par recherche textuelle
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate titreMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("titre")), searchPattern);
                Predicate numeroMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("numeroDossier")), searchPattern);
                Predicate descriptionMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchPattern);
                
                predicates.add(criteriaBuilder.or(titreMatch, numeroMatch, descriptionMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}