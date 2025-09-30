package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.ValidationDossier;
import projet.carthagecreance_backend.Entity.StatutValidation;

import java.util.List;

/**
 * Service interface pour la gestion des validations de dossiers
 * Fournit toutes les opérations CRUD et les fonctionnalités métier
 * pour la gestion des validations de dossiers dans le système
 */
public interface ValidationDossierService {

    /**
     * Crée une nouvelle validation de dossier
     * @param validation La validation à créer
     * @return La validation créée avec son ID généré
     * @throws RuntimeException si les données de la validation sont invalides
     */
    ValidationDossier createValidationDossier(ValidationDossier validation);

    /**
     * Met à jour une validation de dossier existante
     * @param id L'ID de la validation à modifier
     * @param validation Les nouvelles données de la validation
     * @return La validation mise à jour
     * @throws RuntimeException si la validation n'existe pas
     */
    ValidationDossier updateValidationDossier(Long id, ValidationDossier validation);

    /**
     * Supprime une validation de dossier
     * @param id L'ID de la validation à supprimer
     * @throws RuntimeException si la validation n'existe pas
     */
    void deleteValidationDossier(Long id);

    /**
     * Récupère une validation de dossier par son ID
     * @param id L'ID de la validation
     * @return La validation trouvée
     * @throws RuntimeException si la validation n'existe pas
     */
    ValidationDossier getValidationDossierById(Long id);

    /**
     * Récupère toutes les validations de dossiers
     * @return Liste de toutes les validations de dossiers
     */
    List<ValidationDossier> getAllValidationsDossier();

    /**
     * Récupère les dossiers en attente de validation
     * @return Liste des validations en attente
     */
    List<ValidationDossier> getDossiersEnAttente();

    /**
     * Récupère les validations par agent créateur
     * @param agentId L'ID de l'agent créateur
     * @return Liste des validations de l'agent
     */
    List<ValidationDossier> getValidationsByAgent(Long agentId);

    /**
     * Récupère les validations par chef validateur
     * @param chefId L'ID du chef validateur
     * @return Liste des validations du chef
     */
    List<ValidationDossier> getValidationsByChef(Long chefId);

    /**
     * Récupère les validations par dossier
     * @param dossierId L'ID du dossier
     * @return Liste des validations du dossier
     */
    List<ValidationDossier> getValidationsByDossier(Long dossierId);

    /**
     * Récupère les validations par statut
     * @param statut Le statut des validations
     * @return Liste des validations avec le statut spécifié
     */
    List<ValidationDossier> getValidationsByStatut(StatutValidation statut);

    /**
     * Valide un dossier
     * @param dossierId L'ID du dossier à valider
     * @param chefId L'ID du chef qui valide
     * @param commentaire Le commentaire de validation
     * @return La validation mise à jour
     * @throws RuntimeException si le dossier n'existe pas ou si le chef n'a pas les droits
     */
    ValidationDossier validerDossier(Long dossierId, Long chefId, String commentaire);

    /**
     * Rejette un dossier
     * @param dossierId L'ID du dossier à rejeter
     * @param chefId L'ID du chef qui rejette
     * @param commentaire Le commentaire de rejet
     * @return La validation mise à jour
     * @throws RuntimeException si le dossier n'existe pas ou si le chef n'a pas les droits
     */
    ValidationDossier rejeterDossier(Long dossierId, Long chefId, String commentaire);

    /**
     * Remet une validation en attente
     * @param validationId L'ID de la validation
     * @param commentaire Le commentaire de remise en attente
     * @return La validation mise à jour
     * @throws RuntimeException si la validation n'existe pas
     */
    ValidationDossier remettreEnAttente(Long validationId, String commentaire);

    /**
     * Compte les validations par statut
     * @param statut Le statut des validations
     * @return Nombre de validations avec ce statut
     */
    long countValidationsByStatut(StatutValidation statut);

    /**
     * Compte les validations par agent créateur
     * @param agentId L'ID de l'agent créateur
     * @return Nombre de validations de l'agent
     */
    long countValidationsByAgent(Long agentId);

    /**
     * Compte les validations par chef validateur
     * @param chefId L'ID du chef validateur
     * @return Nombre de validations du chef
     */
    long countValidationsByChef(Long chefId);
}
