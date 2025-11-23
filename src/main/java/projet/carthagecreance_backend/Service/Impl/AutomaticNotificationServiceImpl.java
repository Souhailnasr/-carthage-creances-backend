package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.Service.AutomaticNotificationService;
import projet.carthagecreance_backend.Service.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service de notifications automatiques
 */
@Service
@Transactional
public class AutomaticNotificationServiceImpl implements AutomaticNotificationService {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private AudienceRepository audienceRepository;
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Override
    public void notifierCreationDossier(Dossier dossier) {
        if (dossier == null || dossier.getAgentCreateur() == null) {
            return;
        }
        
        // Notifier le créateur
        String titre = "Dossier créé";
        String message = String.format("Le dossier %s a été créé avec succès.", 
                dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId());
        
        notificationService.creerNotificationAutomatique(
            dossier.getAgentCreateur().getId(),
            TypeNotification.DOSSIER_CREE,
            titre,
            message,
            dossier.getId(),
            TypeEntite.DOSSIER,
            "/dossiers/" + dossier.getId()
        );
        
        // Notifier les chefs du département dossier
        notifierChefsDepartement(RoleUtilisateur.CHEF_DEPARTEMENT_DOSSIER, 
            TypeNotification.DOSSIER_CREE,
            "Nouveau dossier créé",
            message,
            dossier.getId(),
            TypeEntite.DOSSIER
        );
    }
    
    @Override
    public void notifierAffectationDossier(Dossier dossier, Utilisateur agent) {
        if (dossier == null || agent == null) {
            return;
        }
        
        String titre = "Dossier affecté";
        String message = String.format("Le dossier %s vous a été affecté.", 
                dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId());
        
        notificationService.creerNotificationAutomatique(
            agent.getId(),
            TypeNotification.DOSSIER_AFFECTE,
            titre,
            message,
            dossier.getId(),
            TypeEntite.DOSSIER,
            "/dossiers/" + dossier.getId()
        );
    }
    
    @Override
    public void notifierCreationActionAmiable(Action action, Dossier dossier) {
        if (action == null || dossier == null) {
            return;
        }
        
        // Notifier l'agent responsable du dossier
        if (dossier.getAgentResponsable() != null) {
            String titre = "Action amiable créée";
            String message = String.format("Une nouvelle action amiable a été créée pour le dossier %s.", 
                    dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId());
            
            notificationService.creerNotificationAutomatique(
                dossier.getAgentResponsable().getId(),
                TypeNotification.ACTION_AMIABLE_CREE,
                titre,
                message,
                dossier.getId(),
                TypeEntite.DOSSIER,
                "/dossiers/" + dossier.getId() + "/actions"
            );
        }
    }
    
    @Override
    public void notifierAudienceProchaine(Audience audience, Dossier dossier) {
        if (audience == null || dossier == null || audience.getDateProchaine() == null) {
            return;
        }
        
        LocalDate aujourdhui = LocalDate.now();
        LocalDate dateProchaine = audience.getDateProchaine();
        
        // Notifier seulement si l'audience est dans les 7 prochains jours
        if (dateProchaine.isAfter(aujourdhui) && dateProchaine.isBefore(aujourdhui.plusDays(8))) {
            String titre = "Audience prochaine";
            String message = String.format("Une audience est prévue le %s pour le dossier %s.", 
                    dateProchaine.toString(),
                    dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId());
            
            // Notifier l'agent responsable
            if (dossier.getAgentResponsable() != null) {
                notificationService.creerNotificationAutomatique(
                    dossier.getAgentResponsable().getId(),
                    TypeNotification.AUDIENCE_PROCHAINE,
                    titre,
                    message,
                    dossier.getId(),
                    TypeEntite.AUDIENCE,
                    "/dossiers/" + dossier.getId() + "/audiences"
                );
            }
            
            // Notifier les chefs du département juridique
            notifierChefsDepartement(RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE,
                TypeNotification.AUDIENCE_PROCHAINE,
                titre,
                message,
                dossier.getId(),
                TypeEntite.AUDIENCE
            );
        }
    }
    
    @Override
    public void notifierCreationAudience(Audience audience, Dossier dossier) {
        if (audience == null || dossier == null) {
            return;
        }
        
        String titre = "Audience créée";
        String message = String.format("Une nouvelle audience a été créée pour le dossier %s.", 
                dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId());
        
        // Notifier l'agent responsable
        if (dossier.getAgentResponsable() != null) {
            notificationService.creerNotificationAutomatique(
                dossier.getAgentResponsable().getId(),
                TypeNotification.AUDIENCE_CREE,
                titre,
                message,
                dossier.getId(),
                TypeEntite.AUDIENCE,
                "/dossiers/" + dossier.getId() + "/audiences"
            );
        }
    }
    
    @Override
    public void notifierTraitementDossier(Dossier dossier, Utilisateur agent) {
        if (dossier == null || agent == null) {
            return;
        }
        
        String titre = "Dossier en traitement";
        String message = String.format("Le dossier %s est en cours de traitement.", 
                dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId());
        
        // Notifier les chefs
        notifierChefsDepartement(RoleUtilisateur.CHEF_DEPARTEMENT_DOSSIER,
            TypeNotification.TRAITEMENT_DOSSIER,
            titre,
            message,
            dossier.getId(),
            TypeEntite.DOSSIER
        );
    }
    
    @Override
    public void notifierValidationDossier(Dossier dossier, Utilisateur valideur) {
        if (dossier == null) {
            return;
        }
        
        String titre = "Dossier validé";
        String message = String.format("Le dossier %s a été validé.", 
                dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId());
        
        // Notifier le créateur du dossier
        if (dossier.getAgentCreateur() != null) {
            notificationService.creerNotificationAutomatique(
                dossier.getAgentCreateur().getId(),
                TypeNotification.DOSSIER_VALIDE,
                titre,
                message,
                dossier.getId(),
                TypeEntite.DOSSIER,
                "/dossiers/" + dossier.getId()
            );
        }
        
        // Notifier l'agent responsable s'il est différent du créateur
        if (dossier.getAgentResponsable() != null && 
            !dossier.getAgentResponsable().getId().equals(dossier.getAgentCreateur() != null ? dossier.getAgentCreateur().getId() : null)) {
            notificationService.creerNotificationAutomatique(
                dossier.getAgentResponsable().getId(),
                TypeNotification.DOSSIER_VALIDE,
                titre,
                message,
                dossier.getId(),
                TypeEntite.DOSSIER,
                "/dossiers/" + dossier.getId()
            );
        }
    }
    
    @Override
    public void notifierCreationTache(TacheUrgente tache) {
        if (tache == null || tache.getAgentAssigné() == null) {
            return;
        }
        
        String titre = "Tâche assignée";
        String message = String.format("Une nouvelle tâche vous a été assignée: %s", 
                tache.getTitre() != null ? tache.getTitre() : "Tâche #" + tache.getId());
        
        notificationService.creerNotificationAutomatique(
            tache.getAgentAssigné().getId(),
            TypeNotification.TACHE_AFFECTEE,
            titre,
            message,
            tache.getId(),
            TypeEntite.TACHE_URGENTE,
            "/taches/" + tache.getId()
        );
    }
    
    @Override
    public void notifierCompletionTache(TacheUrgente tache) {
        if (tache == null || tache.getChefCreateur() == null) {
            return;
        }
        
        String titre = "Tâche complétée";
        String message = String.format("La tâche '%s' a été complétée.", 
                tache.getTitre() != null ? tache.getTitre() : "Tâche #" + tache.getId());
        
        notificationService.creerNotificationAutomatique(
            tache.getChefCreateur().getId(),
            TypeNotification.TACHE_COMPLETEE,
            titre,
            message,
            tache.getId(),
            TypeEntite.TACHE_URGENTE,
            "/taches/" + tache.getId()
        );
    }
    
    @Override
    @Scheduled(cron = "0 0 8 * * ?") // Tous les jours à 8h
    public void verifierEtNotifierAudiencesProchaines() {
        LocalDate aujourdhui = LocalDate.now();
        LocalDate dans7Jours = aujourdhui.plusDays(7);
        
        List<Audience> audiences = audienceRepository.findAll();
        
        for (Audience audience : audiences) {
            if (audience.getDateProchaine() != null) {
                LocalDate dateProchaine = audience.getDateProchaine();
                
                // Vérifier si l'audience est dans les 7 prochains jours
                if (dateProchaine.isAfter(aujourdhui) && dateProchaine.isBefore(dans7Jours.plusDays(1))) {
                    Dossier dossier = audience.getDossier();
                    if (dossier != null) {
                        notifierAudienceProchaine(audience, dossier);
                    }
                }
            }
        }
    }
    
    /**
     * Notifie tous les chefs d'un département
     */
    private void notifierChefsDepartement(RoleUtilisateur roleChef, TypeNotification type, 
                                         String titre, String message, Long entiteId, TypeEntite entiteType) {
        List<Utilisateur> chefs = utilisateurRepository.findByRoleUtilisateur(roleChef);
        for (Utilisateur chef : chefs) {
            notificationService.creerNotificationAutomatique(
                chef.getId(),
                type,
                titre,
                message,
                entiteId,
                entiteType,
                null
            );
        }
    }
}

