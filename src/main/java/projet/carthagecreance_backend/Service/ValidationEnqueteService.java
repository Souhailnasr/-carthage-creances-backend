package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.ValidationEnquete;
import projet.carthagecreance_backend.Entity.StatutValidation;

import java.util.List;

/**
 * Service interface pour la gestion des validations d'enquêtes
 * Fournit toutes les opérations CRUD et les fonctionnalités métier
 * pour la gestion des validations d'enquêtes dans le système
 */
public interface ValidationEnqueteService {

    /**
     * Crée une nouvelle validation d'enquête
     * @param validation La validation à créer
     * @return La validation créée avec son ID généré
     * @throws RuntimeException si les données de la validation sont invalides
     */
    ValidationEnquete createValidationEnquete(ValidationEnquete validation);

    /**
     * Met à jour une validation d'enquête existante
     * @param id L'ID de la validation à modifier
     * @param validation Les nouvelles données de la validation
     * @return La validation mise à jour
     * @throws RuntimeException si la validation n'existe pas
     */
    ValidationEnquete updateValidationEnquete(Long id, ValidationEnquete validation);

    /**
     * Supprime une validation d'enquête
     * @param id L'ID de la validation à supprimer
     * @throws RuntimeException si la validation n'existe pas
     */
    void deleteValidationEnquete(Long id);

    /**
     * Récupère une validation d'enquête par son ID
     * @param id L'ID de la validation
     * @return La validation trouvée
     * @throws RuntimeException si la validation n'existe pas
     */
    ValidationEnquete getValidationEnqueteById(Long id);

    /**
     * Récupère toutes les validations d'enquêtes
     * @return Liste de toutes les validations d'enquêtes
     */
    List<ValidationEnquete> getAllValidationsEnquete();

    /**
     * Récupère les enquêtes en attente de validation
     * @return Liste des validations en attente
     */
    List<ValidationEnquete> getEnquetesEnAttente();

    /**
     * Récupère les validations par agent créateur
     * @param agentId L'ID de l'agent créateur
     * @return Liste des validations de l'agent
     */
    List<ValidationEnquete> getValidationsByAgent(Long agentId);

    /**
     * Récupère les validations par chef validateur
     * @param chefId L'ID du chef validateur
     * @return Liste des validations du chef
     */
    List<ValidationEnquete> getValidationsByChef(Long chefId);

    /**
     * Récupère les validations par enquête
     * @param enqueteId L'ID de l'enquête
     * @return Liste des validations de l'enquête
     */
    List<ValidationEnquete> getValidationsByEnquete(Long enqueteId);

    /**
     * Récupère les validations par statut
     * @param statut Le statut des validations
     * @return Liste des validations avec le statut spécifié
     */
    List<ValidationEnquete> getValidationsByStatut(StatutValidation statut);

    /**
     * Valide une enquête
     * @param enqueteId L'ID de l'enquête à valider
     * @param chefId L'ID du chef qui valide
     * @param commentaire Le commentaire de validation
     * @return La validation mise à jour
     * @throws RuntimeException si l'enquête n'existe pas ou si le chef n'a pas les droits
     */
    ValidationEnquete validerEnquete(Long enqueteId, Long chefId, String commentaire);

    /**
     * Rejette une enquête
     * @param enqueteId L'ID de l'enquête à rejeter
     * @param chefId L'ID du chef qui rejette
     * @param commentaire Le commentaire de rejet
     * @return La validation mise à jour
     * @throws RuntimeException si l'enquête n'existe pas ou si le chef n'a pas les droits
     */
    ValidationEnquete rejeterEnquete(Long enqueteId, Long chefId, String commentaire);

    /**
     * Remet une validation en attente
     * @param validationId L'ID de la validation
     * @param commentaire Le commentaire de remise en attente
     * @return La validation mise à jour
     * @throws RuntimeException si la validation n'existe pas
     */
    ValidationEnquete remettreEnAttente(Long validationId, String commentaire);

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
