package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.Service.AutomaticNotificationService;
import projet.carthagecreance_backend.Service.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifierAffectationDossier(Dossier dossier, Utilisateur agent) {
        if (dossier == null || agent == null) {
            return;
        }
        
        try {
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
        } catch (Exception e) {
            // Logger l'erreur mais ne pas la propager pour ne pas affecter la transaction principale
            org.slf4j.LoggerFactory.getLogger(AutomaticNotificationServiceImpl.class)
                .error("Erreur lors de la notification d'affectation de dossier: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifierCreationActionAmiable(Action action, Dossier dossier) {
        if (action == null || dossier == null) {
            return;
        }
        
        try {
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
        } catch (Exception e) {
            // Logger l'erreur mais ne pas la propager pour ne pas affecter la transaction principale
            org.slf4j.LoggerFactory.getLogger(AutomaticNotificationServiceImpl.class)
                .error("Erreur lors de la notification de création d'action amiable: {}", e.getMessage(), e);
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
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifierCreationUtilisateur(Utilisateur utilisateur, Utilisateur createur) {
        if (utilisateur == null) {
            return;
        }
        
        try {
            // Notifier l'utilisateur créé
            String titre = "Compte créé";
            String message = String.format("Votre compte a été créé avec le rôle: %s. Bienvenue !", 
                    utilisateur.getRoleUtilisateur() != null ? utilisateur.getRoleUtilisateur().name() : "N/A");
            
            notificationService.creerNotificationAutomatique(
                utilisateur.getId(),
                TypeNotification.UTILISATEUR_CREE,
                titre,
                message,
                utilisateur.getId(),
                TypeEntite.UTILISATEUR,
                "/profile"
            );
            
            // Notifier le créateur (SuperAdmin ou Chef)
            if (createur != null) {
                String titreCreateur = "Utilisateur créé";
                String messageCreateur = String.format("L'utilisateur %s %s (%s) a été créé avec succès.", 
                        utilisateur.getNom(),
                        utilisateur.getPrenom(),
                        utilisateur.getEmail());
                
                notificationService.creerNotificationAutomatique(
                    createur.getId(),
                    TypeNotification.UTILISATEUR_CREE,
                    titreCreateur,
                    messageCreateur,
                    utilisateur.getId(),
                    TypeEntite.UTILISATEUR,
                    "/users/" + utilisateur.getId()
                );
            }
            
            // Si c'est un agent, notifier son chef de département
            if (utilisateur.getRoleUtilisateur() != null && 
                utilisateur.getRoleUtilisateur().name().contains("AGENT")) {
                RoleUtilisateur roleChef = getRoleChefPourAgent(utilisateur.getRoleUtilisateur());
                if (roleChef != null) {
                    notifierChefsDepartement(roleChef,
                        TypeNotification.UTILISATEUR_AFFECTE,
                        "Nouvel agent affecté",
                        String.format("Un nouvel agent %s %s a été affecté à votre département.", 
                                utilisateur.getNom(), utilisateur.getPrenom()),
                        utilisateur.getId(),
                        TypeEntite.UTILISATEUR
                    );
                }
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AutomaticNotificationServiceImpl.class)
                .error("Erreur lors de la notification de création d'utilisateur: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifierAffectationUtilisateur(Utilisateur utilisateur, Utilisateur chef) {
        if (utilisateur == null || chef == null) {
            return;
        }
        
        try {
            String titre = "Affectation au département";
            String message = String.format("Vous avez été affecté au département de %s %s.", 
                    chef.getNom(), chef.getPrenom());
            
            notificationService.creerNotificationAutomatique(
                utilisateur.getId(),
                TypeNotification.UTILISATEUR_AFFECTE,
                titre,
                message,
                utilisateur.getId(),
                TypeEntite.UTILISATEUR,
                "/profile"
            );
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AutomaticNotificationServiceImpl.class)
                .error("Erreur lors de la notification d'affectation d'utilisateur: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifierCreationDocumentHuissier(DocumentHuissier document, Dossier dossier) {
        if (document == null || dossier == null) {
            return;
        }
        
        try {
            String titre = "Document huissier créé";
            String message = String.format("Un document huissier (%s) a été créé pour le dossier %s.", 
                    document.getTypeDocument() != null ? document.getTypeDocument().name() : "Document",
                    dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId());
            
            // Notifier l'agent responsable
            if (dossier.getAgentResponsable() != null) {
                notificationService.creerNotificationAutomatique(
                    dossier.getAgentResponsable().getId(),
                    TypeNotification.DOCUMENT_HUISSIER_CREE,
                    titre,
                    message,
                    dossier.getId(),
                    TypeEntite.DOSSIER,
                    "/dossiers/" + dossier.getId() + "/documents-huissier"
                );
            }
            
            // Notifier les chefs du département juridique
            notifierChefsDepartement(RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE,
                TypeNotification.DOCUMENT_HUISSIER_CREE,
                titre,
                message,
                dossier.getId(),
                TypeEntite.DOSSIER
            );
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AutomaticNotificationServiceImpl.class)
                .error("Erreur lors de la notification de création de document huissier: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifierExpirationDocumentHuissier(DocumentHuissier document, Dossier dossier) {
        if (document == null || dossier == null) {
            return;
        }
        
        try {
            String titre = "Document huissier expiré";
            String message = String.format("Le délai légal du document %s du dossier %s a expiré. Action requise.", 
                    document.getTypeDocument() != null ? document.getTypeDocument().name() : "Document",
                    dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId());
            
            // Notifier l'agent responsable
            if (dossier.getAgentResponsable() != null) {
                notificationService.creerNotificationAutomatique(
                    dossier.getAgentResponsable().getId(),
                    TypeNotification.DOCUMENT_HUISSIER_EXPIRE,
                    titre,
                    message,
                    dossier.getId(),
                    TypeEntite.DOSSIER,
                    "/dossiers/" + dossier.getId() + "/documents-huissier"
                );
            }
            
            // Notifier les chefs du département juridique
            notifierChefsDepartement(RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE,
                TypeNotification.DOCUMENT_HUISSIER_EXPIRE,
                titre,
                message,
                dossier.getId(),
                TypeEntite.DOSSIER
            );
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AutomaticNotificationServiceImpl.class)
                .error("Erreur lors de la notification d'expiration de document huissier: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifierActionHuissierEffectuee(ActionHuissier action, Dossier dossier) {
        if (action == null || dossier == null) {
            return;
        }
        
        try {
            String titre = "Action huissier effectuée";
            String message = String.format("Action %s réalisée par %s pour le dossier %s. Montant recouvré: %s TND.", 
                    action.getTypeAction() != null ? action.getTypeAction().name() : "Action",
                    action.getHuissierName() != null ? action.getHuissierName() : "Huissier",
                    dossier.getNumeroDossier() != null ? dossier.getNumeroDossier() : "N°" + dossier.getId(),
                    action.getMontantRecouvre() != null ? action.getMontantRecouvre().toString() : "0.00");
            
            // Notifier l'agent responsable
            if (dossier.getAgentResponsable() != null) {
                notificationService.creerNotificationAutomatique(
                    dossier.getAgentResponsable().getId(),
                    TypeNotification.ACTION_HUISSIER_PERFORMED,
                    titre,
                    message,
                    dossier.getId(),
                    TypeEntite.DOSSIER,
                    "/dossiers/" + dossier.getId() + "/actions-huissier"
                );
            }
            
            // Notifier les chefs du département juridique
            notifierChefsDepartement(RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE,
                TypeNotification.ACTION_HUISSIER_PERFORMED,
                titre,
                message,
                dossier.getId(),
                TypeEntite.DOSSIER
            );
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AutomaticNotificationServiceImpl.class)
                .error("Erreur lors de la notification d'action huissier: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void envoyerNotificationHierarchique(Utilisateur expediteur, List<Long> destinataires, 
                                                TypeNotification type, String titre, String message,
                                                Long entiteId, TypeEntite entiteType) {
        if (expediteur == null || destinataires == null || destinataires.isEmpty()) {
            return;
        }
        
        try {
            // Déterminer le type de notification hiérarchique selon le rôle de l'expéditeur
            TypeNotification typeFinal = type;
            if (expediteur.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                // Vérifier si les destinataires sont des chefs ou des agents
                for (Long destinataireId : destinataires) {
                    Utilisateur destinataire = utilisateurRepository.findById(destinataireId).orElse(null);
                    if (destinataire != null) {
                        if (destinataire.getRoleUtilisateur().name().contains("CHEF")) {
                            typeFinal = TypeNotification.NOTIFICATION_SUPERADMIN_VERS_CHEF;
                        } else if (destinataire.getRoleUtilisateur().name().contains("AGENT")) {
                            typeFinal = TypeNotification.NOTIFICATION_SUPERADMIN_VERS_AGENT;
                        }
                    }
                }
            } else if (expediteur.getRoleUtilisateur().name().contains("CHEF")) {
                typeFinal = TypeNotification.NOTIFICATION_CHEF_VERS_AGENT;
            }
            
            // Envoyer à tous les destinataires
            for (Long destinataireId : destinataires) {
                notificationService.creerNotificationAutomatique(
                    destinataireId,
                    typeFinal,
                    titre,
                    message,
                    entiteId,
                    entiteType,
                    null
                );
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AutomaticNotificationServiceImpl.class)
                .error("Erreur lors de l'envoi de notification hiérarchique: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifierSuperAdminVersChef(Utilisateur chef, TypeNotification type, String titre, 
                                          String message, Long entiteId, TypeEntite entiteType) {
        if (chef == null || !chef.getRoleUtilisateur().name().contains("CHEF")) {
            return;
        }
        
        try {
            notificationService.creerNotificationAutomatique(
                chef.getId(),
                TypeNotification.NOTIFICATION_SUPERADMIN_VERS_CHEF,
                titre,
                message,
                entiteId,
                entiteType,
                null
            );
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AutomaticNotificationServiceImpl.class)
                .error("Erreur lors de la notification SuperAdmin vers Chef: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifierSuperAdminVersAgent(Utilisateur agent, TypeNotification type, String titre, 
                                           String message, Long entiteId, TypeEntite entiteType) {
        if (agent == null || !agent.getRoleUtilisateur().name().contains("AGENT")) {
            return;
        }
        
        try {
            notificationService.creerNotificationAutomatique(
                agent.getId(),
                TypeNotification.NOTIFICATION_SUPERADMIN_VERS_AGENT,
                titre,
                message,
                entiteId,
                entiteType,
                null
            );
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AutomaticNotificationServiceImpl.class)
                .error("Erreur lors de la notification SuperAdmin vers Agent: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifierChefVersAgents(Utilisateur chef, TypeNotification type, String titre, 
                                      String message, Long entiteId, TypeEntite entiteType) {
        if (chef == null || !chef.getRoleUtilisateur().name().contains("CHEF")) {
            return;
        }
        
        try {
            // Récupérer les agents du département du chef
            RoleUtilisateur roleChef = chef.getRoleUtilisateur();
            List<RoleUtilisateur> rolesAgents = getRolesAgentsDuChef(roleChef);
            List<Utilisateur> agents = utilisateurRepository.findByRoleUtilisateurIn(rolesAgents);
            
            // Envoyer la notification à tous les agents
            for (Utilisateur agent : agents) {
                notificationService.creerNotificationAutomatique(
                    agent.getId(),
                    TypeNotification.NOTIFICATION_CHEF_VERS_AGENT,
                    titre,
                    message,
                    entiteId,
                    entiteType,
                    null
                );
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AutomaticNotificationServiceImpl.class)
                .error("Erreur lors de la notification Chef vers Agents: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Obtient le rôle du chef correspondant à un rôle d'agent
     */
    private RoleUtilisateur getRoleChefPourAgent(RoleUtilisateur roleAgent) {
        switch (roleAgent) {
            case AGENT_DOSSIER:
                return RoleUtilisateur.CHEF_DEPARTEMENT_DOSSIER;
            case AGENT_RECOUVREMENT_AMIABLE:
                return RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE;
            case AGENT_RECOUVREMENT_JURIDIQUE:
                return RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE;
            case AGENT_FINANCE:
                return RoleUtilisateur.CHEF_DEPARTEMENT_FINANCE;
            default:
                return null;
        }
    }
    
    /**
     * Obtient les rôles des agents d'un chef
     */
    private List<RoleUtilisateur> getRolesAgentsDuChef(RoleUtilisateur roleChef) {
        List<RoleUtilisateur> roles = new java.util.ArrayList<>();
        switch (roleChef) {
            case CHEF_DEPARTEMENT_DOSSIER:
                roles.add(RoleUtilisateur.AGENT_DOSSIER);
                break;
            case CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE:
                roles.add(RoleUtilisateur.AGENT_RECOUVREMENT_AMIABLE);
                break;
            case CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE:
                roles.add(RoleUtilisateur.AGENT_RECOUVREMENT_JURIDIQUE);
                break;
            case CHEF_DEPARTEMENT_FINANCE:
                roles.add(RoleUtilisateur.AGENT_FINANCE);
                break;
            default:
                break;
        }
        return roles;
    }
}

