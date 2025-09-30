// Fichier : src/main/java/projet/carthagecreance_backend/Service/Impl/DossierServiceImpl.java
package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.Service.DossierService;
import projet.carthagecreance_backend.Service.NotificationService;
import projet.carthagecreance_backend.Service.TacheUrgenteService;
import projet.carthagecreance_backend.DTO.DossierRequest;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service de gestion des dossiers avec workflow complet
 * Inclut toutes les opérations CRUD et les fonctionnalités de workflow
 */
@Service
@Transactional
public class DossierServiceImpl implements DossierService {

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private CreancierRepository creancierRepository;

    @Autowired
    private DebiteurRepository debiteurRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ValidationDossierRepository validationDossierRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TacheUrgenteService tacheUrgenteService;

    /**
     * Crée un nouveau dossier avec workflow de validation
     * Le dossier est automatiquement mis en attente de validation
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

            // 4. Construire l'entité Dossier avec workflow
            Dossier dossier = Dossier.builder()
                    .titre(request.getTitre())
                    .description(request.getDescription())
                    .numeroDossier(request.getNumeroDossier())
                    .montantCreance(request.getMontantCreance())
                    .contratSigne(request.getContratSigne())
                    .pouvoir(request.getPouvoir())
                    .urgence(request.getUrgence())
                    .dossierStatus(DossierStatus.ENCOURSDETRAITEMENT) // En cours de traitement par défaut
                    .typeDocumentJustificatif(request.getTypeDocumentJustificatif())
                    .creancier(creancier)
                    .debiteur(debiteur)
                    .build();

            // 5. Sauvegarder le Dossier
            Dossier savedDossier = dossierRepository.save(dossier);

            // 6. Créer une validation de dossier si un agent créateur est fourni
            if (agentCreateur != null) {
                ValidationDossier validation = ValidationDossier.builder()
                        .dossier(savedDossier)
                        .agentCreateur(agentCreateur)
                        .statut(StatutValidation.EN_ATTENTE)
                        .dateCreation(LocalDateTime.now())
                        .build();
                validationDossierRepository.save(validation);

                // 7. Envoyer une notification au chef de département
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

            return savedDossier;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création du dossier : " + e.getMessage(), e);
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
            existingDossier.setContratSigne(dossierDetails.getContratSigne());
            existingDossier.setPouvoir(dossierDetails.getPouvoir());
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
        return dossierRepository.findByDossierStatus(DossierStatus.ENCOURSDETRAITEMENT);
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
    public Dossier validerDossier(Long dossierId, Long chefId) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));

        // Vérifier que le chef existe et a les droits
        Utilisateur chef = utilisateurRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef non trouvé avec l'ID: " + chefId));

        if (!chef.getRoleUtilisateur().name().startsWith("CHEF_DEPARTEMENT")) {
            throw new RuntimeException("L'utilisateur avec l'ID " + chefId + " n'est pas autorisé à valider des dossiers");
        }

        // Mettre à jour le statut du dossier
        dossier.setDossierStatus(DossierStatus.CLOTURE);
        dossier.setDateCloture(new Date());
        Dossier updatedDossier = dossierRepository.save(dossier);

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
                        .titre("Dossier validé: " + updatedDossier.getTitre())
                        .message("Votre dossier " + updatedDossier.getNumeroDossier() + " a été validé par " + chef.getNom() + " " + chef.getPrenom())
                        .type(TypeNotification.DOSSIER_VALIDE)
                        .entiteId(updatedDossier.getId())
                        .entiteType(TypeEntite.DOSSIER)
                        .dateCreation(LocalDateTime.now())
                        .build();
                notificationService.createNotification(notification);
            }
        }

        return updatedDossier;
    }

    @Override
    public Dossier rejeterDossier(Long dossierId, String commentaire) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));

        // Mettre à jour le statut du dossier
        dossier.setDossierStatus(DossierStatus.ENCOURSDETRAITEMENT); // Reste en cours pour correction
        Dossier updatedDossier = dossierRepository.save(dossier);

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
                        .titre("Dossier rejeté: " + updatedDossier.getTitre())
                        .message("Votre dossier " + updatedDossier.getNumeroDossier() + " a été rejeté. Commentaires: " + commentaire)
                        .type(TypeNotification.DOSSIER_REJETE)
                        .entiteId(updatedDossier.getId())
                        .entiteType(TypeEntite.DOSSIER)
                        .dateCreation(LocalDateTime.now())
                        .build();
                notificationService.createNotification(notification);
            }
        }

        return updatedDossier;
    }

    // ==================== Méthodes de comptage ====================

    @Override
    public long countTotalDossiers() {
        return dossierRepository.count();
    }

    @Override
    public long countDossiersEnCours() {
        return dossierRepository.countByDossierStatus(DossierStatus.ENCOURSDETRAITEMENT);
    }

    @Override
    public long countDossiersValides() {
        return dossierRepository.countByDossierStatus(DossierStatus.CLOTURE);
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
}