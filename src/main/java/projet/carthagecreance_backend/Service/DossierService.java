// Fichier : src/main/java/projet/carthagecreance_backend/Service/DossierService.java
package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Urgence;
import projet.carthagecreance_backend.DTO.DossierRequest; // Ajout de l'import DTO

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service interface pour la gestion des dossiers avec workflow complet
 * Inclut toutes les opérations CRUD et les fonctionnalités de workflow
 */
public interface DossierService {

    // ==================== CRUD Operations ====================
    
    /**
     * Crée un nouveau dossier avec workflow de validation
     * @param request Les données du dossier à créer
     * @return Le dossier créé
     */
    Dossier createDossier(DossierRequest request);
    
    /**
     * Récupère un dossier par son ID
     * @param id L'ID du dossier
     * @return Le dossier trouvé
     */
    Optional<Dossier> getDossierById(Long id);
    
    /**
     * Récupère tous les dossiers
     * @return Liste de tous les dossiers
     */
    List<Dossier> getAllDossiers();
    
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
     * Récupère les dossiers en attente de validation
     * @return Liste des dossiers en attente
     */
    List<Dossier> getDossiersEnAttente();

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
     * Valide un dossier
     * @param dossierId L'ID du dossier à valider
     * @param chefId L'ID du chef qui valide
     * @return Le dossier validé
     * @throws RuntimeException si l'agent ou le chef n'existe pas, ou si le chef n'a pas les droits
     */
    Dossier validerDossier(Long dossierId, Long chefId);

    /**
     * Rejette un dossier
     * @param dossierId L'ID du dossier à rejeter
     * @param commentaire Le commentaire de rejet
     * @return Le dossier rejeté
     * @throws RuntimeException si le dossier n'existe pas
     */
    Dossier rejeterDossier(Long dossierId, String commentaire);

    // ==================== Méthodes de comptage ====================

    /**
     * Compte le total des dossiers
     * @return Nombre total de dossiers
     */
    long countTotalDossiers();

    /**
     * Compte les dossiers en cours de traitement
     * @return Nombre de dossiers en cours
     */
    long countDossiersEnCours();

    /**
     * Compte les dossiers validés
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
}