package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.TacheUrgente;
import projet.carthagecreance_backend.Entity.StatutTache;
import projet.carthagecreance_backend.Entity.PrioriteTache;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface pour la gestion des tâches urgentes
 * Fournit toutes les opérations CRUD et les fonctionnalités métier
 * pour la gestion des tâches urgentes dans le système
 */
public interface TacheUrgenteService {

    /**
     * Crée une nouvelle tâche urgente
     * @param tache La tâche à créer
     * @return La tâche créée avec son ID généré
     * @throws RuntimeException si les données de la tâche sont invalides
     */
    TacheUrgente createTacheUrgente(TacheUrgente tache);

    /**
     * Met à jour une tâche urgente existante
     * @param id L'ID de la tâche à modifier
     * @param tache Les nouvelles données de la tâche
     * @return La tâche mise à jour
     * @throws RuntimeException si la tâche n'existe pas
     */
    TacheUrgente updateTacheUrgente(Long id, TacheUrgente tache);

    /**
     * Supprime une tâche urgente
     * @param id L'ID de la tâche à supprimer
     * @throws RuntimeException si la tâche n'existe pas
     */
    void deleteTacheUrgente(Long id);

    /**
     * Récupère une tâche urgente par son ID
     * @param id L'ID de la tâche
     * @return La tâche trouvée
     * @throws RuntimeException si la tâche n'existe pas
     */
    TacheUrgente getTacheUrgenteById(Long id);

    /**
     * Récupère toutes les tâches urgentes
     * @return Liste de toutes les tâches urgentes
     */
    List<TacheUrgente> getAllTachesUrgentes();

    /**
     * Récupère les tâches assignées à un agent spécifique
     * @param agentId L'ID de l'agent
     * @return Liste des tâches de l'agent
     */
    List<TacheUrgente> getTachesByAgent(Long agentId);

    /**
     * Récupère les tâches par statut
     * @param statut Le statut des tâches à rechercher
     * @return Liste des tâches avec le statut spécifié
     */
    List<TacheUrgente> getTachesByStatut(StatutTache statut);

    /**
     * Récupère les tâches par priorité
     * @param priorite La priorité des tâches à rechercher
     * @return Liste des tâches avec la priorité spécifiée
     */
    List<TacheUrgente> getTachesByPriorite(PrioriteTache priorite);

    /**
     * Récupère les tâches urgentes (échéance proche)
     * @param dateLimite Date limite pour considérer une tâche comme urgente
     * @return Liste des tâches urgentes
     */
    List<TacheUrgente> getTachesUrgentes(LocalDateTime dateLimite);

    /**
     * Récupère les tâches en retard
     * @return Liste des tâches en retard
     */
    List<TacheUrgente> getTachesEnRetard();

    /**
     * Récupère les tâches par agent et priorité
     * @param agentId L'ID de l'agent
     * @param priorite La priorité des tâches
     * @return Liste des tâches correspondantes
     */
    List<TacheUrgente> getTachesByAgentAndPriorite(Long agentId, PrioriteTache priorite);

    /**
     * Récupère les tâches récentes
     * @param dateDebut Date de début pour la recherche
     * @return Liste des tâches créées après cette date
     */
    List<TacheUrgente> getTachesRecent(LocalDateTime dateDebut);

    /**
     * Marque une tâche comme terminée
     * @param tacheId L'ID de la tâche
     * @param commentaire Commentaire de completion
     * @return La tâche mise à jour
     * @throws RuntimeException si la tâche n'existe pas ou si l'agent n'a pas les droits
     */
    TacheUrgente marquerComplete(Long tacheId, String commentaire);

    /**
     * Marque une tâche comme en cours
     * @param tacheId L'ID de la tâche
     * @return La tâche mise à jour
     * @throws RuntimeException si la tâche n'existe pas ou si l'agent n'a pas les droits
     */
    TacheUrgente marquerEnCours(Long tacheId);

    /**
     * Annule une tâche
     * @param tacheId L'ID de la tâche
     * @param raison Raison de l'annulation
     * @return La tâche mise à jour
     * @throws RuntimeException si la tâche n'existe pas
     */
    TacheUrgente annulerTache(Long tacheId, String raison);

    /**
     * Compte les tâches par agent
     * @param agentId L'ID de l'agent
     * @return Nombre de tâches de l'agent
     */
    long countTachesByAgent(Long agentId);

    /**
     * Compte les tâches par statut
     * @param statut Le statut des tâches
     * @return Nombre de tâches avec ce statut
     */
    long countTachesByStatut(StatutTache statut);

    /**
     * Compte les tâches urgentes
     * @return Nombre de tâches urgentes
     */
    long countTachesUrgentes();

    /**
     * Compte les tâches par agent et statut
     * @param agentId L'ID de l'agent
     * @param statut Le statut des tâches
     * @return Nombre de tâches correspondantes
     */
    long countTachesByAgentAndStatut(Long agentId, StatutTache statut);
    
    /**
     * Récupère les tâches créées par un chef
     * @param chefId L'ID du chef
     * @return Liste des tâches créées par le chef
     */
    List<TacheUrgente> getTachesByChef(Long chefId);
    
    /**
     * Récupère les tâches par agent et statut
     * @param agentId L'ID de l'agent
     * @param statut Le statut des tâches
     * @return Liste des tâches correspondantes
     */
    List<TacheUrgente> getTachesByAgentAndStatut(Long agentId, StatutTache statut);
    
    /**
     * Récupère les tâches par type
     * @param type Le type de tâche
     * @return Liste des tâches avec ce type
     */
    List<TacheUrgente> getTachesByType(projet.carthagecreance_backend.Entity.TypeTache type);
}