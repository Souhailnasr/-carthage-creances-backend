package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.ValidationDossier;
import projet.carthagecreance_backend.Entity.StatutValidation;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Entity.RoleUtilisateur;
import projet.carthagecreance_backend.Repository.ValidationDossierRepository;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Service.ValidationDossierService;
import projet.carthagecreance_backend.Service.DossierService;
import projet.carthagecreance_backend.Service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service de gestion des validations de dossiers
 * Gère toutes les opérations CRUD et la logique métier pour les validations de dossiers
 */
@Service
@Transactional
public class ValidationDossierServiceImpl implements ValidationDossierService {

    @Autowired
    private ValidationDossierRepository validationDossierRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DossierService dossierService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Crée une nouvelle validation de dossier
     * @param validation La validation à créer
     * @return La validation créée avec son ID généré
     * @throws RuntimeException si les données de la validation sont invalides
     */
    @Override
    public ValidationDossier createValidationDossier(ValidationDossier validation) {
        // Validation des données obligatoires
        validateValidationData(validation);
        
        // Vérifier que l'agent créateur existe
        if (validation.getAgentCreateur() == null || validation.getAgentCreateur().getId() == null) {
            throw new RuntimeException("L'agent créateur est obligatoire");
        }
        
        utilisateurRepository.findById(validation.getAgentCreateur().getId())
                .orElseThrow(() -> new RuntimeException("Agent créateur non trouvé avec l'ID: " + validation.getAgentCreateur().getId()));
        
        // Vérifier que le dossier existe
        if (validation.getDossier() == null || validation.getDossier().getId() == null) {
            throw new RuntimeException("Le dossier est obligatoire");
        }
        
        Dossier dossier = dossierService.getDossierById(validation.getDossier().getId())
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + validation.getDossier().getId()));
        
        // Vérifier qu'il n'y a pas déjà une validation en attente pour ce dossier
        if (validationDossierRepository.existsByDossierIdAndStatut(dossier.getId(), StatutValidation.EN_ATTENTE)) {
            throw new RuntimeException("Une validation est déjà en attente pour ce dossier");
        }
        
        // Initialiser les valeurs par défaut
        validation.setStatut(StatutValidation.EN_ATTENTE);
        validation.setDateCreation(LocalDateTime.now());
        
        return validationDossierRepository.save(validation);
    }

    /**
     * Met à jour une validation de dossier existante
     * @param id L'ID de la validation à modifier
     * @param validation Les nouvelles données de la validation
     * @return La validation mise à jour
     * @throws RuntimeException si la validation n'existe pas
     */
    @Override
    public ValidationDossier updateValidationDossier(Long id, ValidationDossier validation) {
        ValidationDossier existingValidation = validationDossierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Validation non trouvée avec l'ID: " + id));
        
        // Validation des données
        validateValidationData(validation);
        
        // Vérifier que la validation peut être modifiée
        if (existingValidation.getStatut() != StatutValidation.EN_ATTENTE) {
            throw new RuntimeException("Seules les validations en attente peuvent être modifiées");
        }
        
        // Mettre à jour les champs modifiables
        existingValidation.setCommentaires(validation.getCommentaires());
        
        return validationDossierRepository.save(existingValidation);
    }

    /**
     * Supprime une validation de dossier
     * @param id L'ID de la validation à supprimer
     * @throws RuntimeException si la validation n'existe pas
     */
    @Override
    public void deleteValidationDossier(Long id) {
        if (!validationDossierRepository.existsById(id)) {
            throw new RuntimeException("Validation non trouvée avec l'ID: " + id);
        }
        validationDossierRepository.deleteById(id);
    }

    /**
     * Récupère une validation de dossier par son ID
     * @param id L'ID de la validation
     * @return La validation trouvée
     * @throws RuntimeException si la validation n'existe pas
     */
    @Override
    @Transactional(readOnly = true)
    public ValidationDossier getValidationDossierById(Long id) {
        return validationDossierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Validation non trouvée avec l'ID: " + id));
    }

    /**
     * Récupère toutes les validations de dossiers
     * @return Liste de toutes les validations de dossiers
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValidationDossier> getAllValidationsDossier() {
        return validationDossierRepository.findAll();
    }

    /**
     * Récupère les dossiers en attente de validation
     * @return Liste des validations en attente
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValidationDossier> getDossiersEnAttente() {
        return validationDossierRepository.findDossiersEnAttente();
    }

    /**
     * Récupère les validations par agent créateur
     * @param agentId L'ID de l'agent créateur
     * @return Liste des validations de l'agent
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValidationDossier> getValidationsByAgent(Long agentId) {
        return validationDossierRepository.findByAgentCreateurId(agentId);
    }

    /**
     * Récupère les validations par chef validateur
     * @param chefId L'ID du chef validateur
     * @return Liste des validations du chef
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValidationDossier> getValidationsByChef(Long chefId) {
        return validationDossierRepository.findByChefValidateurId(chefId);
    }

    /**
     * Récupère les validations par dossier
     * @param dossierId L'ID du dossier
     * @return Liste des validations du dossier
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValidationDossier> getValidationsByDossier(Long dossierId) {
        return validationDossierRepository.findByDossierId(dossierId);
    }

    /**
     * Récupère les validations par statut
     * @param statut Le statut des validations
     * @return Liste des validations avec le statut spécifié
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValidationDossier> getValidationsByStatut(StatutValidation statut) {
        return validationDossierRepository.findByStatut(statut);
    }

    /**
     * Valide un dossier
     * @param dossierId L'ID du dossier à valider
     * @param chefId L'ID du chef qui valide
     * @param commentaire Le commentaire de validation
     * @return La validation mise à jour
     * @throws RuntimeException si le dossier n'existe pas ou si le chef n'a pas les droits
     */
    @Override
    public ValidationDossier validerDossier(Long dossierId, Long chefId, String commentaire) {
        // Vérifier que le chef existe et a les droits
        Utilisateur chef = utilisateurRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef non trouvé avec l'ID: " + chefId));
        
        if (!isChefValidateur(chef)) {
            throw new RuntimeException("L'utilisateur n'a pas les droits pour valider des dossiers");
        }
        
        // Récupérer la validation en attente pour ce dossier
        List<ValidationDossier> validations = validationDossierRepository.findByDossierIdAndStatut(dossierId, StatutValidation.EN_ATTENTE);
        if (validations.isEmpty()) {
            throw new RuntimeException("Aucune validation en attente trouvée pour ce dossier");
        }
        
        ValidationDossier validation = validations.get(0);
        
        // Vérifier que le chef ne valide pas ses propres dossiers
        if (validation.getAgentCreateur().getId().equals(chefId)) {
            throw new RuntimeException("Un agent ne peut pas valider ses propres dossiers");
        }
        
        // Mettre à jour la validation
        validation.setStatut(StatutValidation.VALIDE);
        validation.setChefValidateur(chef);
        validation.setDateValidation(LocalDateTime.now());
        validation.setCommentaires(commentaire);
        
        // Mettre à jour le statut du dossier
        Dossier dossier = dossierService.getDossierById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Ici, on pourrait mettre à jour le statut du dossier selon la logique métier
        // Par exemple, passer le dossier en "VALIDÉ" ou "APPROUVÉ"
        
        // Envoyer une notification à l'agent créateur
        notificationService.envoyerNotificationValidation(
            validation.getAgentCreateur(),
            dossier.getNumeroDossier(),
            "VALIDÉ",
            commentaire
        );
        
        return validationDossierRepository.save(validation);
    }

    /**
     * Rejette un dossier
     * @param dossierId L'ID du dossier à rejeter
     * @param chefId L'ID du chef qui rejette
     * @param commentaire Le commentaire de rejet
     * @return La validation mise à jour
     * @throws RuntimeException si le dossier n'existe pas ou si le chef n'a pas les droits
     */
    @Override
    public ValidationDossier rejeterDossier(Long dossierId, Long chefId, String commentaire) {
        // Vérifier que le chef existe et a les droits
        Utilisateur chef = utilisateurRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef non trouvé avec l'ID: " + chefId));
        
        if (!isChefValidateur(chef)) {
            throw new RuntimeException("L'utilisateur n'a pas les droits pour rejeter des dossiers");
        }
        
        // Récupérer la validation en attente pour ce dossier
        List<ValidationDossier> validations = validationDossierRepository.findByDossierIdAndStatut(dossierId, StatutValidation.EN_ATTENTE);
        if (validations.isEmpty()) {
            throw new RuntimeException("Aucune validation en attente trouvée pour ce dossier");
        }
        
        ValidationDossier validation = validations.get(0);
        
        // Vérifier que le chef ne rejette pas ses propres dossiers
        if (validation.getAgentCreateur().getId().equals(chefId)) {
            throw new RuntimeException("Un agent ne peut pas rejeter ses propres dossiers");
        }
        
        // Mettre à jour la validation
        validation.setStatut(StatutValidation.REJETE);
        validation.setChefValidateur(chef);
        validation.setDateValidation(LocalDateTime.now());
        validation.setCommentaires(commentaire);
        
        // Envoyer une notification à l'agent créateur
        Dossier dossier = dossierService.getDossierById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        notificationService.envoyerNotificationValidation(
            validation.getAgentCreateur(),
            dossier.getNumeroDossier(),
            "REJETÉ",
            commentaire
        );
        
        return validationDossierRepository.save(validation);
    }

    /**
     * Remet une validation en attente
     * @param validationId L'ID de la validation
     * @param commentaire Le commentaire de remise en attente
     * @return La validation mise à jour
     * @throws RuntimeException si la validation n'existe pas
     */
    @Override
    public ValidationDossier remettreEnAttente(Long validationId, String commentaire) {
        ValidationDossier validation = validationDossierRepository.findById(validationId)
                .orElseThrow(() -> new RuntimeException("Validation non trouvée avec l'ID: " + validationId));
        
        // Vérifier que la validation peut être remise en attente
        if (validation.getStatut() == StatutValidation.EN_ATTENTE) {
            throw new RuntimeException("La validation est déjà en attente");
        }
        
        // Remettre en attente
        validation.setStatut(StatutValidation.EN_ATTENTE);
        validation.setChefValidateur(null);
        validation.setDateValidation(null);
        validation.setCommentaires(commentaire);
        
        return validationDossierRepository.save(validation);
    }

    /**
     * Compte les validations par statut
     * @param statut Le statut des validations
     * @return Nombre de validations avec ce statut
     */
    @Override
    @Transactional(readOnly = true)
    public long countValidationsByStatut(StatutValidation statut) {
        return validationDossierRepository.countByStatut(statut);
    }

    /**
     * Compte les validations par agent créateur
     * @param agentId L'ID de l'agent créateur
     * @return Nombre de validations de l'agent
     */
    @Override
    @Transactional(readOnly = true)
    public long countValidationsByAgent(Long agentId) {
        return validationDossierRepository.countByAgentCreateurId(agentId);
    }

    /**
     * Compte les validations par chef validateur
     * @param chefId L'ID du chef validateur
     * @return Nombre de validations du chef
     */
    @Override
    @Transactional(readOnly = true)
    public long countValidationsByChef(Long chefId) {
        return validationDossierRepository.countByChefValidateurId(chefId);
    }

    /**
     * Valide les données d'une validation
     * @param validation La validation à valider
     * @throws RuntimeException si les données sont invalides
     */
    private void validateValidationData(ValidationDossier validation) {
        if (validation == null) {
            throw new RuntimeException("La validation ne peut pas être nulle");
        }
        
        if (validation.getDossier() == null || validation.getDossier().getId() == null) {
            throw new RuntimeException("Le dossier est obligatoire");
        }
        
        if (validation.getAgentCreateur() == null || validation.getAgentCreateur().getId() == null) {
            throw new RuntimeException("L'agent créateur est obligatoire");
        }
    }

    /**
     * Vérifie si un utilisateur a les droits de chef validateur
     * @param utilisateur L'utilisateur à vérifier
     * @return true si l'utilisateur peut valider des dossiers
     */
    private boolean isChefValidateur(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getRoleUtilisateur() == null) {
            return false;
        }
        
        RoleUtilisateur role = utilisateur.getRoleUtilisateur();
        return role == RoleUtilisateur.SUPER_ADMIN ||
               role == RoleUtilisateur.CHEF_DEPARTEMENT_DOSSIER ||
               role == RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE ||
               role == RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE ||
               role == RoleUtilisateur.CHEF_DEPARTEMENT_FINANCE;
    }
}
