// Fichier : src/main/java/projet/carthagecreance_backend/Service/DossierService.java
package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Urgence;
import projet.carthagecreance_backend.Entity.StatutValidation;
import projet.carthagecreance_backend.Entity.Statut;
import projet.carthagecreance_backend.DTO.DossierRequest; // Ajout de l'import DTO
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface pour la gestion des dossiers avec workflow complet
 * Inclut toutes les opérations CRUD et les fonctionnalités de workflow
 */
public interface DossierService {

    // ==================== CRUD Operations ====================
    
    /**
     * Crée un nouveau dossier avec workflow de validation.
     * Règles métier:
     * - Un agent peut créer un dossier => une ValidationDossier est créée avec statut EN_ATTENTE
     * - Un chef (rôle commençant par CHEF_DEPARTEMENT) crée un dossier => validation automatique (VALIDE)
     * Les fichiers PDF (contrat, pouvoir) sont référencés via des chemins stockés sur l'entité Dossier.
     * @param request Les données du dossier à créer
     * @return Le dossier créé
     */
    Dossier createDossier(DossierRequest request);
    
    /**
     * Crée un nouveau dossier avec fichiers uploadés
     * @param request Les données du dossier à créer
     * @param pouvoirFile Fichier pouvoir (optionnel)
     * @param contratSigneFile Fichier contrat signé (optionnel)
     * @return Le dossier créé avec les chemins des fichiers
     */
    Dossier createDossierWithFiles(DossierRequest request, MultipartFile pouvoirFile, MultipartFile contratSigneFile);
    
    /**
     * Récupère un dossier par son ID
     * @param id L'ID du dossier
     * @return Un Optional contenant le dossier si présent
     */
    Optional<Dossier> getDossierById(Long id);
    
    /**
     * Récupère tous les dossiers
     * @return Liste de tous les dossiers
     */
    List<Dossier> getAllDossiers();

    /**
     * Récupère tous les dossiers avec pagination et filtres
     * @param role Le rôle de l'utilisateur pour filtrer les dossiers
     * @param userId L'ID de l'utilisateur pour filtrer les dossiers
     * @param page Numéro de page
     * @param size Taille de la page
     * @param search Terme de recherche
     * @return Map contenant les dossiers paginés et les métadonnées
     */
    Map<String, Object> getAllDossiersWithPagination(String role, Long userId, int page, int size, String search);
    
    /**
     * Met à jour un dossier avec validation
     * @param id L'ID du dossier à modifier
     * @param dossier Les nouvelles données du dossier
     * @return Le dossier mis à jour
     */
    Dossier updateDossier(Long id, Dossier dossier);
    
    /**
     * Supprime un dossier avec vérifications
     * @param id L'ID du dossier à supprimer
     */
    void deleteDossier(Long id);

    // ==================== Search Operations ====================
    
    Optional<Dossier> getDossierByNumber(String numeroDossier);
    List<Dossier> getDossiersByTitle(String title);
    List<Dossier> getDossiersByDescription(String description);
    List<Dossier> getDossiersByUrgency(Urgence urgency);
    List<Dossier> getDossiersByAvocat(Long avocatId);
    List<Dossier> getDossiersByHuissier(Long huissierId);
    List<Dossier> getDossiersByCreancier(Long creancierId);
    List<Dossier> getDossiersByDebiteur(Long debiteurId);
    List<Dossier> getDossiersByUser(Long userId);
    List<Dossier> getDossiersByCreationDate(Date date);
    List<Dossier> getDossiersByCreationDateRange(Date startDate, Date endDate);
    List<Dossier> getDossiersByClosureDate(Date date);
    List<Dossier> getDossiersByAmount(Double amount);
    List<Dossier> getDossiersByAmountRange(Double minAmount, Double maxAmount);
    List<Dossier> searchDossiers(String searchTerm);

    // ==================== Special Operations ====================
    
    List<Dossier> getOpenDossiers();
    List<Dossier> getClosedDossiers();
    List<Dossier> getRecentDossiers();

    // ==================== Validation Operations ====================
    
    boolean existsByNumber(String numeroDossier);

    // ==================== Nouvelles méthodes de workflow ====================

    /**
     * Récupère les dossiers en attente de validation.
     * Basé sur les entrées de ValidationDossier avec statut EN_ATTENTE.
     * @return Liste des dossiers en attente
     */
    List<Dossier> getDossiersEnAttente();

    /**
     * Récupère les dossiers par statut de validation (EN_ATTENTE, VALIDE, REJETE).
     * @param statut Statut de validation recherché
     * @return Liste des dossiers
     */
    List<Dossier> getDossiersByValidationStatut(StatutValidation statut);

    /**
     * Récupère les dossiers validés (statut VALIDE).
     */
    List<Dossier> getDossiersValides();

    /**
     * Récupère les dossiers par agent et statut (Statut workflow).
     */
    List<Dossier> getDossiersParAgentEtStatut(Long agentId, Statut statut);

    /**
     * Récupère les dossiers assignés à un agent
     * @param agentId L'ID de l'agent
     * @return Liste des dossiers assignés à l'agent
     */
    List<Dossier> getDossiersByAgent(Long agentId);

    /**
     * Récupère les dossiers créés par un agent
     * @param agentId L'ID de l'agent créateur
     * @return Liste des dossiers créés par l'agent
     */
    List<Dossier> getDossiersCreesByAgent(Long agentId);

    /**
     * Valide un dossier.
     * Nécessite un utilisateur chef (rôle CHEF_DEPARTEMENT_*) comme validateur.
     * @param dossierId L'ID du dossier à valider
     * @param chefId L'ID du chef qui valide
     * @return Le dossier validé
     * @throws RuntimeException si le dossier/chef n'existe pas ou si l'utilisateur n'a pas les droits
     */
    void validerDossier(Long dossierId, Long chefId);

    /**
     * Rejette un dossier.
     * Le dossier reste non validé et en cours de traitement pour correction.
     * @param dossierId L'ID du dossier à rejeter
     * @param commentaire Le commentaire de rejet
     * @return Le dossier mis à jour
     * @throws RuntimeException si le dossier n'existe pas
     */
    void rejeterDossier(Long dossierId, String commentaire);

    // ==================== Méthodes de comptage ====================

    /**
     * Compte le total des dossiers
     * @return Nombre total de dossiers
     */
    long countTotalDossiers();

    /**
     * Compte les dossiers en cours de traitement.
     * Correspond au nombre de validations en statut EN_ATTENTE.
     * @return Nombre de dossiers en cours
     */
    long countDossiersEnCours();

    /**
     * Compte les dossiers validés.
     * Correspond au nombre de validations en statut VALIDE.
     * @return Nombre de dossiers validés
     */
    long countDossiersValides();

    /**
     * Compte les dossiers créés ce mois
     * @return Nombre de dossiers créés ce mois
     */
    long countDossiersCreesCeMois();

    /**
     * Compte les dossiers assignés à un agent
     * @param agentId L'ID de l'agent
     * @return Nombre de dossiers de l'agent
     */
    long countDossiersByAgent(Long agentId);

    /**
     * Compte les dossiers créés par un agent
     * @param agentId L'ID de l'agent créateur
     * @return Nombre de dossiers créés par l'agent
     */
    long countDossiersCreesByAgent(Long agentId);

    // ==================== Affectations ====================
    Dossier assignerAgentResponsable(Long dossierId, Long agentId);
    Dossier assignerAvocat(Long dossierId, Long avocatId);
    Dossier assignerHuissier(Long dossierId, Long huissierId);
    
    /**
     * Affecte un dossier à un avocat et/ou un huissier de manière flexible
     * Permet d'affecter soit un avocat, soit un huissier, soit les deux
     * Si un ID est null, l'affectation correspondante sera retirée
     * @param dossierId L'ID du dossier à affecter
     * @param avocatId L'ID de l'avocat (optionnel, null pour retirer l'affectation)
     * @param huissierId L'ID de l'huissier (optionnel, null pour retirer l'affectation)
     * @return Le dossier mis à jour
     * @throws RuntimeException si le dossier, l'avocat ou l'huissier n'existe pas
     */
    Dossier affecterAvocatEtHuissier(Long dossierId, Long avocatId, Long huissierId);
    
    /**
     * Affecte un dossier validé au chef du département recouvrement amiable
     * @param dossierId L'ID du dossier à affecter
     * @return Le dossier mis à jour avec le chef amiable assigné comme agentResponsable
     * @throws RuntimeException si le dossier n'existe pas, n'est pas validé, ou si aucun chef amiable n'est trouvé
     */
    Dossier affecterAuRecouvrementAmiable(Long dossierId);
    
    /**
     * Affecte un dossier validé au chef du département recouvrement juridique
     * @param dossierId L'ID du dossier à affecter
     * @return Le dossier mis à jour avec le chef juridique assigné comme agentResponsable
     * @throws RuntimeException si le dossier n'existe pas, n'est pas validé, ou si aucun chef juridique n'est trouvé
     */
    Dossier affecterAuRecouvrementJuridique(Long dossierId);
    
    /**
     * Affecte un dossier validé au chef du département finance
     * @param dossierId L'ID du dossier à affecter
     * @return Le dossier mis à jour avec le chef financier assigné comme agentResponsable
     * @throws RuntimeException si le dossier n'existe pas, n'est pas validé, ou si aucun chef financier n'est trouvé
     */
    Dossier affecterAuFinance(Long dossierId);
    
    /**
     * Clôture un dossier validé
     * @param dossierId L'ID du dossier à clôturer
     * @return Le dossier mis à jour avec dossierStatus = CLOTURE et dateCloture remplie
     * @throws RuntimeException si le dossier n'existe pas ou n'est pas validé
     */
    Dossier cloturerDossier(Long dossierId);
    
    // ✅ NOUVEAU : Méthodes pour la clôture et archivage après paiement complet
    projet.carthagecreance_backend.DTO.PeutEtreClotureDTO peutEtreCloture(Long dossierId);
    projet.carthagecreance_backend.DTO.ClotureDossierDTO cloturerEtArchiver(Long dossierId);
    
    /**
     * Récupère les dossiers validés disponibles pour l'affectation
     * @param page Numéro de page (0-indexed)
     * @param size Taille de la page
     * @param sort Champ de tri
     * @param direction Direction du tri (ASC ou DESC)
     * @param search Terme de recherche (optionnel)
     * @return Map contenant la liste paginée des dossiers et les métadonnées de pagination
     */
    Map<String, Object> getDossiersValidesDisponibles(int page, int size, String sort, String direction, String search);
}