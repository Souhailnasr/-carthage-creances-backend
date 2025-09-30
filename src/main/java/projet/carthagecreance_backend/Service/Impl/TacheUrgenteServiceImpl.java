package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.TacheUrgente;
import projet.carthagecreance_backend.Entity.StatutTache;
import projet.carthagecreance_backend.Entity.PrioriteTache;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Repository.TacheUrgenteRepository;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Service.TacheUrgenteService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service de gestion des tâches urgentes
 * Gère toutes les opérations CRUD et la logique métier pour les tâches urgentes
 */
@Service
@Transactional
public class TacheUrgenteServiceImpl implements TacheUrgenteService {

    @Autowired
    private TacheUrgenteRepository tacheUrgenteRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * Crée une nouvelle tâche urgente
     * @param tache La tâche à créer
     * @return La tâche créée avec son ID généré
     * @throws RuntimeException si les données de la tâche sont invalides
     */
    @Override
    public TacheUrgente createTacheUrgente(TacheUrgente tache) {
        // Validation des données obligatoires
        validateTacheData(tache);
        
        // Vérifier que l'agent assigné existe
        if (tache.getAgentAssigné() == null || tache.getAgentAssigné().getId() == null) {
            throw new RuntimeException("L'agent assigné est obligatoire");
        }
        
        utilisateurRepository.findById(tache.getAgentAssigné().getId())
                .orElseThrow(() -> new RuntimeException("Agent assigné non trouvé avec l'ID: " + tache.getAgentAssigné().getId()));
        
        // Vérifier que le chef créateur existe
        if (tache.getChefCreateur() == null || tache.getChefCreateur().getId() == null) {
            throw new RuntimeException("Le chef créateur est obligatoire");
        }
        
        utilisateurRepository.findById(tache.getChefCreateur().getId())
                .orElseThrow(() -> new RuntimeException("Chef créateur non trouvé avec l'ID: " + tache.getChefCreateur().getId()));
        
        // Initialiser les valeurs par défaut
        tache.setStatut(StatutTache.EN_ATTENTE);
        tache.setDateCreation(LocalDateTime.now());
        
        // Vérifier que la date d'échéance est dans le futur
        if (tache.getDateEcheance() != null && tache.getDateEcheance().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La date d'échéance doit être dans le futur");
        }
        
        return tacheUrgenteRepository.save(tache);
    }

    /**
     * Met à jour une tâche urgente existante
     * @param id L'ID de la tâche à modifier
     * @param tache Les nouvelles données de la tâche
     * @return La tâche mise à jour
     * @throws RuntimeException si la tâche n'existe pas
     */
    @Override
    public TacheUrgente updateTacheUrgente(Long id, TacheUrgente tache) {
        TacheUrgente existingTache = tacheUrgenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'ID: " + id));
        
        // Validation des données
        validateTacheData(tache);
        
        // Vérifier que la date d'échéance est dans le futur
        if (tache.getDateEcheance() != null && tache.getDateEcheance().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La date d'échéance doit être dans le futur");
        }
        
        // Mettre à jour les champs modifiables
        existingTache.setTitre(tache.getTitre());
        existingTache.setDescription(tache.getDescription());
        existingTache.setType(tache.getType());
        existingTache.setPriorite(tache.getPriorite());
        existingTache.setDateEcheance(tache.getDateEcheance());
        existingTache.setDossier(tache.getDossier());
        existingTache.setEnquete(tache.getEnquete());
        existingTache.setCommentaires(tache.getCommentaires());
        
        // Mettre à jour l'agent assigné si fourni
        if (tache.getAgentAssigné() != null && tache.getAgentAssigné().getId() != null) {
            Utilisateur agent = utilisateurRepository.findById(tache.getAgentAssigné().getId())
                    .orElseThrow(() -> new RuntimeException("Agent assigné non trouvé avec l'ID: " + tache.getAgentAssigné().getId()));
            existingTache.setAgentAssigné(agent);
        }
        
        return tacheUrgenteRepository.save(existingTache);
    }

    /**
     * Supprime une tâche urgente
     * @param id L'ID de la tâche à supprimer
     * @throws RuntimeException si la tâche n'existe pas
     */
    @Override
    public void deleteTacheUrgente(Long id) {
        if (!tacheUrgenteRepository.existsById(id)) {
            throw new RuntimeException("Tâche non trouvée avec l'ID: " + id);
        }
        tacheUrgenteRepository.deleteById(id);
    }

    /**
     * Récupère une tâche urgente par son ID
     * @param id L'ID de la tâche
     * @return La tâche trouvée
     * @throws RuntimeException si la tâche n'existe pas
     */
    @Override
    @Transactional(readOnly = true)
    public TacheUrgente getTacheUrgenteById(Long id) {
        return tacheUrgenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'ID: " + id));
    }

    /**
     * Récupère toutes les tâches urgentes
     * @return Liste de toutes les tâches urgentes
     */
    @Override
    @Transactional(readOnly = true)
    public List<TacheUrgente> getAllTachesUrgentes() {
        return tacheUrgenteRepository.findAll();
    }

    /**
     * Récupère les tâches assignées à un agent spécifique
     * @param agentId L'ID de l'agent
     * @return Liste des tâches de l'agent
     */
    @Override
    @Transactional(readOnly = true)
    public List<TacheUrgente> getTachesByAgent(Long agentId) {
        return tacheUrgenteRepository.findByAgentAssignéId(agentId);
    }

    /**
     * Récupère les tâches par statut
     * @param statut Le statut des tâches à rechercher
     * @return Liste des tâches avec le statut spécifié
     */
    @Override
    @Transactional(readOnly = true)
    public List<TacheUrgente> getTachesByStatut(StatutTache statut) {
        return tacheUrgenteRepository.findByStatut(statut);
    }

    /**
     * Récupère les tâches par priorité
     * @param priorite La priorité des tâches à rechercher
     * @return Liste des tâches avec la priorité spécifiée
     */
    @Override
    @Transactional(readOnly = true)
    public List<TacheUrgente> getTachesByPriorite(PrioriteTache priorite) {
        return tacheUrgenteRepository.findByPriorite(priorite);
    }

    /**
     * Récupère les tâches urgentes (échéance proche)
     * @param dateLimite Date limite pour considérer une tâche comme urgente
     * @return Liste des tâches urgentes
     */
    @Override
    @Transactional(readOnly = true)
    public List<TacheUrgente> getTachesUrgentes(LocalDateTime dateLimite) {
        return tacheUrgenteRepository.findTachesUrgentes(dateLimite);
    }

    /**
     * Récupère les tâches en retard
     * @return Liste des tâches en retard
     */
    @Override
    @Transactional(readOnly = true)
    public List<TacheUrgente> getTachesEnRetard() {
        return tacheUrgenteRepository.findTachesEnRetard(LocalDateTime.now());
    }

    /**
     * Récupère les tâches par agent et priorité
     * @param agentId L'ID de l'agent
     * @param priorite La priorité des tâches
     * @return Liste des tâches correspondantes
     */
    @Override
    @Transactional(readOnly = true)
    public List<TacheUrgente> getTachesByAgentAndPriorite(Long agentId, PrioriteTache priorite) {
        return tacheUrgenteRepository.findByAgentAssignéIdAndPriorite(agentId, priorite);
    }

    /**
     * Récupère les tâches récentes
     * @param dateDebut Date de début pour la recherche
     * @return Liste des tâches créées après cette date
     */
    @Override
    @Transactional(readOnly = true)
    public List<TacheUrgente> getTachesRecent(LocalDateTime dateDebut) {
        return tacheUrgenteRepository.findTachesRecent(dateDebut);
    }

    /**
     * Marque une tâche comme terminée
     * @param tacheId L'ID de la tâche
     * @param commentaire Commentaire de completion
     * @return La tâche mise à jour
     * @throws RuntimeException si la tâche n'existe pas ou si l'agent n'a pas les droits
     */
    @Override
    public TacheUrgente marquerComplete(Long tacheId, String commentaire) {
        TacheUrgente tache = tacheUrgenteRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'ID: " + tacheId));
        
        // Vérifier que la tâche n'est pas déjà terminée
        if (tache.getStatut() == StatutTache.TERMINEE) {
            throw new RuntimeException("La tâche est déjà terminée");
        }
        
        // Mettre à jour le statut et la date de completion
        tache.setStatut(StatutTache.TERMINEE);
        tache.setDateCompletion(LocalDateTime.now());
        tache.setCommentaires(commentaire);
        
        return tacheUrgenteRepository.save(tache);
    }

    /**
     * Marque une tâche comme en cours
     * @param tacheId L'ID de la tâche
     * @return La tâche mise à jour
     * @throws RuntimeException si la tâche n'existe pas ou si l'agent n'a pas les droits
     */
    @Override
    public TacheUrgente marquerEnCours(Long tacheId) {
        TacheUrgente tache = tacheUrgenteRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'ID: " + tacheId));
        
        // Vérifier que la tâche peut être mise en cours
        if (tache.getStatut() == StatutTache.TERMINEE) {
            throw new RuntimeException("Impossible de modifier une tâche terminée");
        }
        
        if (tache.getStatut() == StatutTache.ANNULEE) {
            throw new RuntimeException("Impossible de modifier une tâche annulée");
        }
        
        // Mettre à jour le statut
        tache.setStatut(StatutTache.EN_COURS);
        
        return tacheUrgenteRepository.save(tache);
    }

    /**
     * Annule une tâche
     * @param tacheId L'ID de la tâche
     * @param raison Raison de l'annulation
     * @return La tâche mise à jour
     * @throws RuntimeException si la tâche n'existe pas
     */
    @Override
    public TacheUrgente annulerTache(Long tacheId, String raison) {
        TacheUrgente tache = tacheUrgenteRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'ID: " + tacheId));
        
        // Vérifier que la tâche peut être annulée
        if (tache.getStatut() == StatutTache.TERMINEE) {
            throw new RuntimeException("Impossible d'annuler une tâche terminée");
        }
        
        // Mettre à jour le statut et ajouter la raison
        tache.setStatut(StatutTache.ANNULEE);
        tache.setCommentaires(raison);
        
        return tacheUrgenteRepository.save(tache);
    }

    /**
     * Compte les tâches par agent
     * @param agentId L'ID de l'agent
     * @return Nombre de tâches de l'agent
     */
    @Override
    @Transactional(readOnly = true)
    public long countTachesByAgent(Long agentId) {
        return tacheUrgenteRepository.countByAgentAssignéId(agentId);
    }

    /**
     * Compte les tâches par statut
     * @param statut Le statut des tâches
     * @return Nombre de tâches avec ce statut
     */
    @Override
    @Transactional(readOnly = true)
    public long countTachesByStatut(StatutTache statut) {
        return tacheUrgenteRepository.countByStatut(statut);
    }

    /**
     * Compte les tâches urgentes
     * @return Nombre de tâches urgentes
     */
    @Override
    @Transactional(readOnly = true)
    public long countTachesUrgentes() {
        // Considérer comme urgentes les tâches avec échéance dans les 24h
        LocalDateTime dateLimite = LocalDateTime.now().plusHours(24);
        return tacheUrgenteRepository.countTachesUrgentes(dateLimite);
    }

    /**
     * Compte les tâches par agent et statut
     * @param agentId L'ID de l'agent
     * @param statut Le statut des tâches
     * @return Nombre de tâches correspondantes
     */
    @Override
    @Transactional(readOnly = true)
    public long countTachesByAgentAndStatut(Long agentId, StatutTache statut) {
        return tacheUrgenteRepository.countByAgentAssignéIdAndStatut(agentId, statut);
    }

    /**
     * Valide les données d'une tâche
     * @param tache La tâche à valider
     * @throws RuntimeException si les données sont invalides
     */
    private void validateTacheData(TacheUrgente tache) {
        if (tache == null) {
            throw new RuntimeException("La tâche ne peut pas être nulle");
        }
        
        if (tache.getTitre() == null || tache.getTitre().trim().isEmpty()) {
            throw new RuntimeException("Le titre de la tâche est obligatoire");
        }
        
        if (tache.getType() == null) {
            throw new RuntimeException("Le type de la tâche est obligatoire");
        }
        
        if (tache.getPriorite() == null) {
            throw new RuntimeException("La priorité de la tâche est obligatoire");
        }
        
        if (tache.getDateEcheance() == null) {
            throw new RuntimeException("La date d'échéance est obligatoire");
        }
        
        // Vérifier que la date d'échéance est dans le futur
        if (tache.getDateEcheance().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La date d'échéance doit être dans le futur");
        }
    }
}