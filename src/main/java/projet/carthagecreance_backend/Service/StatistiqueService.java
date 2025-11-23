package projet.carthagecreance_backend.Service;

import java.time.LocalDate;
import java.util.Map;

/**
 * Service pour la gestion des statistiques réelles basées sur les données
 */
public interface StatistiqueService {
    
    /**
     * Récupère toutes les statistiques globales
     * @return Map contenant toutes les statistiques
     */
    Map<String, Object> getStatistiquesGlobales();
    
    /**
     * Récupère les statistiques pour une période donnée
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Map contenant les statistiques de la période
     */
    Map<String, Object> getStatistiquesParPeriode(LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Récupère les statistiques d'un agent
     * @param agentId ID de l'agent
     * @return Map contenant les statistiques de l'agent
     */
    Map<String, Object> getStatistiquesAgent(Long agentId);
    
    /**
     * Récupère les statistiques d'un chef et de ses agents
     * @param chefId ID du chef
     * @return Map contenant les statistiques du chef et de ses agents
     */
    Map<String, Object> getStatistiquesChef(Long chefId);
    
    /**
     * Récupère les statistiques de tous les chefs
     * @return Map contenant les statistiques de tous les chefs
     */
    Map<String, Object> getStatistiquesTousChefs();
    
    /**
     * Récupère les statistiques des dossiers
     * @return Map contenant les statistiques des dossiers
     */
    Map<String, Object> getStatistiquesDossiers();
    
    /**
     * Récupère les statistiques des actions amiables
     * @return Map contenant les statistiques des actions amiables
     */
    Map<String, Object> getStatistiquesActionsAmiables();
    
    /**
     * Récupère les statistiques des audiences
     * @return Map contenant les statistiques des audiences
     */
    Map<String, Object> getStatistiquesAudiences();
    
    /**
     * Récupère les statistiques des tâches
     * @return Map contenant les statistiques des tâches
     */
    Map<String, Object> getStatistiquesTaches();
    
    /**
     * Récupère les statistiques financières
     * @return Map contenant les statistiques financières
     */
    Map<String, Object> getStatistiquesFinancieres();
}

