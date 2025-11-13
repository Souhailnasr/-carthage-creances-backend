package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.ValidationEnquete;
import projet.carthagecreance_backend.Entity.StatutValidation;
import projet.carthagecreance_backend.Entity.Statut;
import projet.carthagecreance_backend.Entity.Enquette;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Entity.RoleUtilisateur;
import projet.carthagecreance_backend.Repository.ValidationEnqueteRepository;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Service.ValidationEnqueteService;
import projet.carthagecreance_backend.Service.EnquetteService;
import projet.carthagecreance_backend.Service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service de gestion des validations d'enquêtes
 * Gère toutes les opérations CRUD et la logique métier pour les validations d'enquêtes
 */
@Service
@Transactional
public class ValidationEnqueteServiceImpl implements ValidationEnqueteService {

    @Autowired
    private ValidationEnqueteRepository validationEnqueteRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private EnquetteService enquetteService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Crée une nouvelle validation d'enquête
     * @param validation La validation à créer
     * @return La validation créée avec son ID généré
     * @throws RuntimeException si les données de la validation sont invalides
     */
    @Override
    public ValidationEnquete createValidationEnquete(ValidationEnquete validation) {
        // Validation des données obligatoires
        validateValidationData(validation);
        
        // Vérifier que l'agent créateur existe
        if (validation.getAgentCreateur() == null || validation.getAgentCreateur().getId() == null) {
            throw new RuntimeException("L'agent créateur est obligatoire");
        }
        
        utilisateurRepository.findById(validation.getAgentCreateur().getId())
                .orElseThrow(() -> new RuntimeException("Agent créateur non trouvé avec l'ID: " + validation.getAgentCreateur().getId()));
        
        // Vérifier que l'enquête existe
        if (validation.getEnquete() == null || validation.getEnquete().getId() == null) {
            throw new RuntimeException("L'enquête est obligatoire");
        }
        
        // Vérifier que l'enquête existe avec une méthode qui évite les problèmes avec dossier_id = NULL
        if (!enquetteService.existsById(validation.getEnquete().getId())) {
            throw new RuntimeException("Enquête non trouvée avec l'ID: " + validation.getEnquete().getId());
        }
        
        // Essayer de charger l'enquête, mais gérer le cas où dossier_id est NULL
        Enquette enquete;
        try {
            enquete = enquetteService.getEnquetteById(validation.getEnquete().getId())
                .orElseThrow(() -> new RuntimeException("Enquête non trouvée avec l'ID: " + validation.getEnquete().getId()));
        } catch (Exception e) {
            // Si l'enquête existe mais ne peut pas être chargée (dossier_id NULL), on peut quand même créer la validation
            // car la validation ne nécessite pas que l'enquête soit complètement chargée
            System.err.println("Avertissement: L'enquête " + validation.getEnquete().getId() + " existe mais ne peut pas être chargée (dossier_id NULL). La validation sera créée quand même.");
            // Créer une enquête minimale pour la validation
            enquete = Enquette.builder()
                    .id(validation.getEnquete().getId())
                    .build();
        }
        
        // Vérifier qu'il n'y a pas déjà une validation en attente pour cette enquête
        if (validationEnqueteRepository.existsByEnqueteIdAndStatut(enquete.getId(), StatutValidation.EN_ATTENTE)) {
            throw new RuntimeException("Une validation est déjà en attente pour cette enquête");
        }
        
        // Initialiser les valeurs par défaut
        validation.setStatut(StatutValidation.EN_ATTENTE);
        validation.setDateCreation(LocalDateTime.now());
        
        return validationEnqueteRepository.save(validation);
    }

    /**
     * Met à jour une validation d'enquête existante
     * @param id L'ID de la validation à modifier
     * @param validation Les nouvelles données de la validation
     * @return La validation mise à jour
     * @throws RuntimeException si la validation n'existe pas
     */
    @Override
    public ValidationEnquete updateValidationEnquete(Long id, ValidationEnquete validation) {
        ValidationEnquete existingValidation = validationEnqueteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Validation non trouvée avec l'ID: " + id));
        
        // Validation des données
        validateValidationData(validation);
        
        // Vérifier que la validation peut être modifiée
        if (existingValidation.getStatut() != StatutValidation.EN_ATTENTE) {
            throw new RuntimeException("Seules les validations en attente peuvent être modifiées");
        }
        
        // Mettre à jour les champs modifiables
        existingValidation.setCommentaires(validation.getCommentaires());
        
        return validationEnqueteRepository.save(existingValidation);
    }

    /**
     * Supprime une validation d'enquête
     * @param id L'ID de la validation à supprimer
     * @throws RuntimeException si la validation n'existe pas
     */
    @Override
    public void deleteValidationEnquete(Long id) {
        if (!validationEnqueteRepository.existsById(id)) {
            throw new RuntimeException("Validation non trouvée avec l'ID: " + id);
        }
        validationEnqueteRepository.deleteById(id);
    }

    /**
     * Récupère une validation d'enquête par son ID
     * @param id L'ID de la validation
     * @return La validation trouvée
     * @throws RuntimeException si la validation n'existe pas
     */
    @Override
    @Transactional(readOnly = true)
    public ValidationEnquete getValidationEnqueteById(Long id) {
        return validationEnqueteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Validation non trouvée avec l'ID: " + id));
    }

    /**
     * Récupère toutes les validations d'enquêtes
     * @return Liste de toutes les validations d'enquêtes
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValidationEnquete> getAllValidationsEnquete() {
        return validationEnqueteRepository.findAll();
    }

    /**
     * Récupère les enquêtes en attente de validation
     * Filtre les validations orphelines (dont l'enquête n'existe plus)
     * @return Liste des validations en attente avec enquête existante
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValidationEnquete> getEnquetesEnAttente() {
        // Récupérer toutes les validations en attente
        List<ValidationEnquete> validations = validationEnqueteRepository.findEnquetesEnAttente();
        
        // Filtrer pour ne garder que celles dont l'enquête existe encore
        return validations.stream()
                .filter(validation -> {
                    try {
                        // Vérifier que l'enquête existe
                        if (validation.getEnquete() == null || validation.getEnquete().getId() == null) {
                            return false;
                        }
                        return enquetteService.existsById(validation.getEnquete().getId());
                    } catch (Exception e) {
                        // Si erreur lors de la vérification, considérer comme orpheline
                        return false;
                    }
                })
                .toList();
    }

    /**
     * Récupère les validations par agent créateur
     * @param agentId L'ID de l'agent créateur
     * @return Liste des validations de l'agent
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValidationEnquete> getValidationsByAgent(Long agentId) {
        return validationEnqueteRepository.findByAgentCreateurId(agentId);
    }

    /**
     * Récupère les validations par chef validateur
     * @param chefId L'ID du chef validateur
     * @return Liste des validations du chef
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValidationEnquete> getValidationsByChef(Long chefId) {
        return validationEnqueteRepository.findByChefValidateurId(chefId);
    }

    /**
     * Récupère les validations par enquête
     * @param enqueteId L'ID de l'enquête
     * @return Liste des validations de l'enquête
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValidationEnquete> getValidationsByEnquete(Long enqueteId) {
        return validationEnqueteRepository.findByEnqueteId(enqueteId);
    }

    /**
     * Récupère les validations par statut
     * @param statut Le statut des validations
     * @return Liste des validations avec le statut spécifié
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValidationEnquete> getValidationsByStatut(StatutValidation statut) {
        return validationEnqueteRepository.findByStatut(statut);
    }

    /**
     * Valide une enquête
     * @param enqueteId L'ID de l'enquête à valider
     * @param chefId L'ID du chef qui valide
     * @param commentaire Le commentaire de validation
     * @return La validation mise à jour
     * @throws RuntimeException si l'enquête n'existe pas ou si le chef n'a pas les droits
     */
    @Override
    public ValidationEnquete validerEnquete(Long enqueteId, Long chefId, String commentaire) {
        // Vérifier que le chef existe et a les droits
        Utilisateur chef = utilisateurRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef non trouvé avec l'ID: " + chefId));
        
        if (!isChefValidateur(chef)) {
            throw new RuntimeException("L'utilisateur n'a pas les droits pour valider des enquêtes");
        }
        
        // Récupérer la validation en attente pour cette enquête
        List<ValidationEnquete> validations = validationEnqueteRepository.findByEnqueteIdAndStatut(enqueteId, StatutValidation.EN_ATTENTE);
        if (validations.isEmpty()) {
            throw new RuntimeException("Aucune validation en attente trouvée pour cette enquête");
        }
        
        ValidationEnquete validation = validations.get(0);
        
        // Vérifier que le chef ne valide pas ses propres enquêtes
        if (validation.getAgentCreateur().getId().equals(chefId)) {
            throw new RuntimeException("Un agent ne peut pas valider ses propres enquêtes");
        }
        
        // Mettre à jour la validation
        validation.setStatut(StatutValidation.VALIDE);
        validation.setChefValidateur(chef);
        validation.setDateValidation(LocalDateTime.now());
        validation.setCommentaires(commentaire);
        
        // Vérifier que l'enquête existe
        if (!enquetteService.existsById(enqueteId)) {
            throw new RuntimeException("Enquête non trouvée avec l'ID: " + enqueteId);
        }
        
        // Mettre à jour le statut de l'enquête en utilisant la méthode de validation du service
        // qui gère les cas où dossier_id est NULL et préserve agent_createur_id
        try {
            // Utiliser directement la méthode validerEnquette du service qui utilise updateStatutNative()
            // Cette méthode ne modifie que statut, valide, date_validation et commentaire_validation
            // Elle préserve agent_createur_id et tous les autres champs
            enquetteService.validerEnquette(enqueteId, chefId);
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de l'enquête " + enqueteId + ": " + e.getMessage());
            // Si la mise à jour échoue à cause de dossier_id NULL, on peut quand même continuer
            // car la validation a été mise à jour
        }
        
        // Envoyer une notification à l'agent créateur
        // Essayer de récupérer le rapportCode, sinon utiliser l'ID
        String rapportCode = "ID " + enqueteId;
        try {
            Optional<Enquette> enqueteOpt = enquetteService.getEnquetteById(enqueteId);
            if (enqueteOpt.isPresent() && enqueteOpt.get().getRapportCode() != null) {
                rapportCode = enqueteOpt.get().getRapportCode();
            }
        } catch (Exception e) {
            // Utiliser l'ID par défaut si on ne peut pas charger l'enquête
        }
        
        notificationService.envoyerNotificationValidation(
            validation.getAgentCreateur(),
            "Enquête " + rapportCode,
            "VALIDÉE",
            commentaire
        );
        
        return validationEnqueteRepository.save(validation);
    }

    /**
     * Rejette une enquête
     * @param enqueteId L'ID de l'enquête à rejeter
     * @param chefId L'ID du chef qui rejette
     * @param commentaire Le commentaire de rejet
     * @return La validation mise à jour
     * @throws RuntimeException si l'enquête n'existe pas ou si le chef n'a pas les droits
     */
    @Override
    public ValidationEnquete rejeterEnquete(Long enqueteId, Long chefId, String commentaire) {
        // Vérifier que le chef existe et a les droits
        Utilisateur chef = utilisateurRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef non trouvé avec l'ID: " + chefId));
        
        if (!isChefValidateur(chef)) {
            throw new RuntimeException("L'utilisateur n'a pas les droits pour rejeter des enquêtes");
        }
        
        // Récupérer la validation en attente pour cette enquête
        List<ValidationEnquete> validations = validationEnqueteRepository.findByEnqueteIdAndStatut(enqueteId, StatutValidation.EN_ATTENTE);
        if (validations.isEmpty()) {
            throw new RuntimeException("Aucune validation en attente trouvée pour cette enquête");
        }
        
        ValidationEnquete validation = validations.get(0);
        
        // Vérifier que le chef ne rejette pas ses propres enquêtes
        if (validation.getAgentCreateur().getId().equals(chefId)) {
            throw new RuntimeException("Un agent ne peut pas rejeter ses propres enquêtes");
        }
        
        // Mettre à jour la validation
        validation.setStatut(StatutValidation.REJETE);
        validation.setChefValidateur(chef);
        validation.setDateValidation(LocalDateTime.now());
        validation.setCommentaires(commentaire);
        
        // Vérifier que l'enquête existe
        if (!enquetteService.existsById(enqueteId)) {
            throw new RuntimeException("Enquête non trouvée avec l'ID: " + enqueteId);
        }
        
        // Mettre à jour le statut de l'enquête en utilisant la méthode de rejet du service
        // qui gère les cas où dossier_id est NULL et préserve agent_createur_id
        try {
            // Utiliser directement la méthode rejeterEnquette du service qui utilise updateStatutNative()
            // Cette méthode ne modifie que statut, valide, date_validation et commentaire_validation
            // Elle préserve agent_createur_id et tous les autres champs
            enquetteService.rejeterEnquette(enqueteId, commentaire);
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de l'enquête " + enqueteId + ": " + e.getMessage());
            // Si la mise à jour échoue à cause de dossier_id NULL, on peut quand même continuer
            // car la validation a été mise à jour
        }
        
        // Envoyer une notification à l'agent créateur
        // Essayer de récupérer le rapportCode, sinon utiliser l'ID
        String rapportCode = "ID " + enqueteId;
        try {
            Optional<Enquette> enqueteOpt = enquetteService.getEnquetteById(enqueteId);
            if (enqueteOpt.isPresent() && enqueteOpt.get().getRapportCode() != null) {
                rapportCode = enqueteOpt.get().getRapportCode();
            }
        } catch (Exception e) {
            // Utiliser l'ID par défaut si on ne peut pas charger l'enquête
        }
        
        notificationService.envoyerNotificationValidation(
            validation.getAgentCreateur(),
            "Enquête " + rapportCode,
            "REJETÉE",
            commentaire
        );
        
        return validationEnqueteRepository.save(validation);
    }

    /**
     * Remet une validation en attente
     * @param validationId L'ID de la validation
     * @param commentaire Le commentaire de remise en attente
     * @return La validation mise à jour
     * @throws RuntimeException si la validation n'existe pas
     */
    @Override
    public ValidationEnquete remettreEnAttente(Long validationId, String commentaire) {
        ValidationEnquete validation = validationEnqueteRepository.findById(validationId)
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
        
        return validationEnqueteRepository.save(validation);
    }

    /**
     * Compte les validations par statut
     * @param statut Le statut des validations
     * @return Nombre de validations avec ce statut
     */
    @Override
    @Transactional(readOnly = true)
    public long countValidationsByStatut(StatutValidation statut) {
        return validationEnqueteRepository.countByStatut(statut);
    }

    /**
     * Compte les validations par agent créateur
     * @param agentId L'ID de l'agent créateur
     * @return Nombre de validations de l'agent
     */
    @Override
    @Transactional(readOnly = true)
    public long countValidationsByAgent(Long agentId) {
        return validationEnqueteRepository.countByAgentCreateurId(agentId);
    }

    /**
     * Compte les validations par chef validateur
     * @param chefId L'ID du chef validateur
     * @return Nombre de validations du chef
     */
    @Override
    @Transactional(readOnly = true)
    public long countValidationsByChef(Long chefId) {
        return validationEnqueteRepository.countByChefValidateurId(chefId);
    }

    /**
     * Valide les données d'une validation
     * @param validation La validation à valider
     * @throws RuntimeException si les données sont invalides
     */
    private void validateValidationData(ValidationEnquete validation) {
        if (validation == null) {
            throw new RuntimeException("La validation ne peut pas être nulle");
        }
        
        if (validation.getEnquete() == null || validation.getEnquete().getId() == null) {
            throw new RuntimeException("L'enquête est obligatoire");
        }
        
        if (validation.getAgentCreateur() == null || validation.getAgentCreateur().getId() == null) {
            throw new RuntimeException("L'agent créateur est obligatoire");
        }
    }

    /**
     * Nettoie les validations orphelines (dont l'enquête n'existe plus)
     * @return Nombre de validations orphelines supprimées
     */
    @Override
    @Transactional
    public int nettoyerValidationsOrphelines() {
        // Récupérer toutes les validations
        List<ValidationEnquete> allValidations = validationEnqueteRepository.findAll();
        
        // Identifier les validations orphelines
        List<ValidationEnquete> orphelines = allValidations.stream()
                .filter(validation -> {
                    try {
                        if (validation.getEnquete() == null || validation.getEnquete().getId() == null) {
                            return true; // Orpheline si pas d'enquête
                        }
                        // Vérifier si l'enquête existe
                        return !enquetteService.existsById(validation.getEnquete().getId());
                    } catch (Exception e) {
                        // En cas d'erreur, considérer comme orpheline
                        return true;
                    }
                })
                .toList();
        
        // Supprimer les validations orphelines
        if (!orphelines.isEmpty()) {
            validationEnqueteRepository.deleteAll(orphelines);
        }
        
        return orphelines.size();
    }

    /**
     * Vérifie si un utilisateur a les droits de chef validateur
     * @param utilisateur L'utilisateur à vérifier
     * @return true si l'utilisateur peut valider des enquêtes
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
