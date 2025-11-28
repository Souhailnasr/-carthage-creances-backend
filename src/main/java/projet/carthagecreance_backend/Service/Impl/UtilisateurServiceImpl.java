// Fichier : src/main/java/projet/carthagecreance_backend/Service/Impl/UtilisateurServiceImpl.java
package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.PayloadResponse.AuthenticationResponse;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.SecurityConfig.JwtService;
import projet.carthagecreance_backend.Service.UtilisateurService;
import projet.carthagecreance_backend.Service.NotificationService;
import projet.carthagecreance_backend.Service.TacheUrgenteService;
import projet.carthagecreance_backend.Service.PerformanceAgentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des utilisateurs avec workflow complet
 * Inclut toutes les opérations CRUD et les fonctionnalités de gestion des agents
 */
@Service
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PerformanceAgentRepository performanceAgentRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private TacheUrgenteService tacheUrgenteService;

    @Autowired
    private PerformanceAgentService performanceAgentService;

    /**
     * Crée un nouvel utilisateur avec validation des rôles
     * Vérifie les droits de création et envoie des notifications
     */
    @Override
    public AuthenticationResponse createUtilisateur(Utilisateur utilisateur) {
        try {
            System.out.println("===== DÉBUT createUtilisateur =====");
            System.out.println("Email: " + utilisateur.getEmail());
            System.out.println("Mot de passe reçu: " + utilisateur.getMotDePasse());
            
            // 1. Vérifier l'unicité de l'email
            if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
            }

            // 2. Valider le rôle
            if (utilisateur.getRoleUtilisateur() == null) {
                throw new IllegalArgumentException("Le rôle utilisateur est obligatoire.");
            }

            // 2bis. Associer le chef créateur si fourni (obligatoire pour les agents)
            Long chefCreateurId = null;
            if (utilisateur.getChefCreateur() != null && utilisateur.getChefCreateur().getId() != null) {
                chefCreateurId = utilisateur.getChefCreateur().getId();
            } else if (utilisateur.getChefId() != null) {
                chefCreateurId = utilisateur.getChefId();
            }

            Long finalChefCreateurId = chefCreateurId;
            if (finalChefCreateurId != null) {
                Utilisateur chefCreateur = utilisateurRepository.findById(finalChefCreateurId)
                        .orElseThrow(() -> new IllegalArgumentException("Chef créateur introuvable avec l'id: " + finalChefCreateurId));

                if (!(estChef(chefCreateur.getRoleUtilisateur()) || chefCreateur.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN)) {
                    throw new IllegalArgumentException("L'utilisateur " + finalChefCreateurId + " n'est pas autorisé à créer des agents.");
                }

                utilisateur.setChefCreateur(chefCreateur);
            } else if (estAgent(utilisateur.getRoleUtilisateur())) {
                throw new IllegalArgumentException("Un agent doit être rattaché à un chef créateur.");
            }

            // 3. Encoder le mot de passe
            String encodedPassword = passwordEncoder.encode(utilisateur.getMotDePasse());
            System.out.println("Mot de passe crypté: " + encodedPassword);
            utilisateur.setMotDePasse(encodedPassword);

            // 4. Sauvegarder l'utilisateur
            Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
            System.out.println("Utilisateur sauvegardé avec ID: " + savedUtilisateur.getId());

            // 5. Générer le token JWT
            var jwtToken = jwtService.generateToken(savedUtilisateur);
            System.out.println("Token généré: " + jwtToken.substring(0, 20) + "...");
            saveUserToken(savedUtilisateur, jwtToken);
            System.out.println("Token sauvegardé");

            // 6. Envoyer une notification de création (optionnel, peut causer des erreurs)
            try {
                Notification notification = Notification.builder()
                        .utilisateur(savedUtilisateur)
                        .titre("Compte créé avec succès")
                        .message("Votre compte a été créé avec le rôle: " + savedUtilisateur.getRoleUtilisateur())
                        .type(TypeNotification.INFO)
                        .entiteId(savedUtilisateur.getId())
                        .entiteType(TypeEntite.UTILISATEUR)
                        .dateCreation(LocalDateTime.now())
                        .build();
                notificationService.createNotification(notification);
            } catch (Exception e) {
                System.out.println("Erreur lors de la création de la notification (non bloquant): " + e.getMessage());
            }

            // 7. Si c'est un agent, créer une performance initiale (optionnel, peut causer des erreurs)
            try {
                if (estAgent(savedUtilisateur.getRoleUtilisateur())) {
                    PerformanceAgent performance = PerformanceAgent.builder()
                            .agent(savedUtilisateur)
                            .periode(getPeriodeActuelle())
                            .score(0.0)
                            .objectif(80)
                            .tauxReussite(0.0)
                            .dateCalcul(LocalDateTime.now())
                            .build();
                    performanceAgentRepository.save(performance);
                }
            } catch (Exception e) {
                System.out.println("Erreur lors de la création de la performance (non bloquant): " + e.getMessage());
            }

            AuthenticationResponse response = AuthenticationResponse.builder()
                    .token(jwtToken)
                    .userId(savedUtilisateur.getId())
                    .email(savedUtilisateur.getEmail())
                    .nom(savedUtilisateur.getNom())
                    .prenom(savedUtilisateur.getPrenom())
                    .role(savedUtilisateur.getRoleUtilisateur().name())
                    .build();
            
            System.out.println("===== FIN createUtilisateur - Token retourné =====");
            return response;

        } catch (IllegalArgumentException e) {
            System.out.println("Erreur IllegalArgumentException: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Erreur Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création de l'utilisateur : " + e.getMessage(), e);
        }
    }

    private void saveUserToken(Utilisateur user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .expireAt(LocalDateTime.now().plusHours(1)) // Token expire dans 1 heure
                .build();
        tokenRepository.save(token);
    }

    @Override
    public Optional<Utilisateur> getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id);
    }

    @Override
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    /**
     * Met à jour un utilisateur avec validation des rôles
     * Vérifie les droits de modification et envoie des notifications
     */
    @Override
    public Utilisateur updateUtilisateur(Long id, Utilisateur utilisateurDetails) {
        Optional<Utilisateur> optionalUtilisateur = utilisateurRepository.findById(id);
        if (optionalUtilisateur.isPresent()) {
            Utilisateur existingUtilisateur = optionalUtilisateur.get();
            RoleUtilisateur ancienRole = existingUtilisateur.getRoleUtilisateur();
            
            // Mettre à jour les champs nécessaires
            existingUtilisateur.setNom(utilisateurDetails.getNom());
            existingUtilisateur.setPrenom(utilisateurDetails.getPrenom());
            
            // Ne mettez à jour le mot de passe que si un nouveau est fourni
            // IMPORTANT : Encoder le mot de passe avant de le sauvegarder
            if (utilisateurDetails.getMotDePasse() != null && !utilisateurDetails.getMotDePasse().isEmpty()) {
                String encodedPassword = passwordEncoder.encode(utilisateurDetails.getMotDePasse());
                existingUtilisateur.setMotDePasse(encodedPassword);
            }
            
            // Mettre à jour le rôle si fourni
            if (utilisateurDetails.getRoleUtilisateur() != null) {
                existingUtilisateur.setRoleUtilisateur(utilisateurDetails.getRoleUtilisateur());
            }
            
            // Mettre à jour l'email avec vérification d'unicité si nécessaire
            if (utilisateurDetails.getEmail() != null && !utilisateurDetails.getEmail().equals(existingUtilisateur.getEmail())) {
                if (utilisateurRepository.existsByEmail(utilisateurDetails.getEmail())) {
                    throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
                }
                existingUtilisateur.setEmail(utilisateurDetails.getEmail());
            }

            Utilisateur updatedUtilisateur = utilisateurRepository.save(existingUtilisateur);

            // Envoyer une notification si le rôle a changé
            if (ancienRole != null && !ancienRole.equals(utilisateurDetails.getRoleUtilisateur())) {
                Notification notification = Notification.builder()
                        .utilisateur(updatedUtilisateur)
                        .titre("Rôle modifié")
                        .message("Votre rôle a été modifié de " + ancienRole + " vers " + utilisateurDetails.getRoleUtilisateur())
                        .type(TypeNotification.INFO)
                        .entiteId(updatedUtilisateur.getId())
                        .entiteType(TypeEntite.UTILISATEUR)
                        .dateCreation(LocalDateTime.now())
                        .build();
                notificationService.createNotification(notification);
            }

            return updatedUtilisateur;
        } else {
            throw new RuntimeException("Utilisateur non trouvé avec id: " + id);
        }
    }

    // >>>>>>>>> CORRECTION : Implémentation de la méthode avec l'enum <<<<<<<<<
    @Override
    public List<Utilisateur> getUtilisateursByRoleUtilisateur(RoleUtilisateur roleUtilisateur) { // Changement ici
        // Validation si le rôle est null
        if (roleUtilisateur == null) {
            throw new IllegalArgumentException("Le rôle ne peut pas être null.");
        }
        // Appel au repository avec l'enum
        return utilisateurRepository.findByRoleUtilisateur(roleUtilisateur); // Changement ici
    }

    /**
     * Supprime un utilisateur avec vérifications
     * Vérifie les dépendances et envoie des notifications
     */
    @Override
    public void deleteUtilisateur(Long id) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findById(id);
        if (utilisateur.isPresent()) {
            // Vérifier s'il y a des performances associées
            List<PerformanceAgent> performances = performanceAgentRepository.findByAgentId(id);
            if (!performances.isEmpty()) {
                throw new RuntimeException("Impossible de supprimer l'utilisateur: des performances sont associées");
            }

            // Vérifier s'il y a des tâches urgentes associées
            // (à implémenter selon votre logique métier)

            // Supprimer l'utilisateur
            utilisateurRepository.deleteById(id);

            // Envoyer une notification de suppression
            Notification notification = Notification.builder()
                    .utilisateur(getSuperAdmin())
                    .titre("Utilisateur supprimé: " + utilisateur.get().getNom() + " " + utilisateur.get().getPrenom())
                    .message("L'utilisateur " + utilisateur.get().getEmail() + " a été supprimé")
                    .type(TypeNotification.INFO)
                    .entiteId(null)
                    .entiteType(TypeEntite.UTILISATEUR)
                    .dateCreation(LocalDateTime.now())
                    .build();
            notificationService.createNotification(notification);
        } else {
            throw new RuntimeException("Utilisateur non trouvé avec id: " + id);
        }
    }

    // Méthodes de recherche
    @Override
    public List<Utilisateur> getUtilisateursByName(String name) {
        return utilisateurRepository.findByNomContainingIgnoreCase(name);
    }

    @Override
    public List<Utilisateur> getUtilisateursByFirstName(String firstName) {
        return utilisateurRepository.findByPrenomContainingIgnoreCase(firstName);
    }

    @Override
    public List<Utilisateur> getUtilisateursByFullName(String name, String firstName) {
        return utilisateurRepository.findByNomAndPrenom(name, firstName);
    }

    @Override
    public Optional<Utilisateur> getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    @Override
    public List<Utilisateur> searchUtilisateurs(String searchTerm) {
        return utilisateurRepository.findByNomOuPrenomContaining(searchTerm);
    }

    @Override
    public List<Utilisateur> getUtilisateursWithDossiers() {
        return utilisateurRepository.findUtilisateursAvecDossiers();
    }

    @Override
    public List<Utilisateur> getUtilisateursWithoutDossiers() {
        return utilisateurRepository.findUtilisateursSansDossiers();
    }

    // >>>>>>>>> SUPPRESSION : Cette méthode est redondante et incorrecte <<<<<<<<<
    // @Override
    // public List<Utilisateur> getUtilisateursByRoleUtilisateur(String roleutilisateur) {
    //     return List.of(); // Implémentation vide et incorrecte
    // }

    @Override
    public Optional<Utilisateur> authenticate(String email, String password) {
        return utilisateurRepository.findByEmailAndMotDePasse(email, password);
    }

    @Override
    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    // ==================== Nouvelles méthodes de gestion des agents ====================

    @Override
    public List<Utilisateur> getAgents() {
        return utilisateurRepository.findAll().stream()
                .filter(u -> estAgent(u.getRoleUtilisateur()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Utilisateur> getAgentsByRole(String role) {
        try {
            RoleUtilisateur roleEnum = RoleUtilisateur.valueOf(role.toUpperCase());
            return utilisateurRepository.findByRoleUtilisateur(roleEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rôle invalide: " + role);
        }
    }

    @Override
    public List<Utilisateur> getChefs() {
        return utilisateurRepository.findAll().stream()
                .filter(u -> estChef(u.getRoleUtilisateur()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Utilisateur> getAgentsActifs() {
        return utilisateurRepository.findAll().stream()
                .filter(u -> estAgent(u.getRoleUtilisateur()))
                .filter(u -> u.getEmail() != null && !u.getEmail().isEmpty())
                .filter(u -> {
                    // Mettre à jour le statut actif avant de filtrer
                    u.mettreAJourStatutActif();
                    return u.getActif() != null && u.getActif();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PerformanceAgent> getPerformanceAgents() {
        return performanceAgentRepository.findAll();
    }

    @Override
    public long countAgents() {
        return utilisateurRepository.findAll().stream()
                .filter(u -> estAgent(u.getRoleUtilisateur()))
                .count();
    }

    @Override
    public long countChefs() {
        return utilisateurRepository.findAll().stream()
                .filter(u -> estChef(u.getRoleUtilisateur()))
                .count();
    }

    @Override
    public List<Utilisateur> getAgentsByPerformance() {
        List<Utilisateur> agents = getAgents();
        return agents.stream()
                .sorted((a1, a2) -> {
                    // Récupérer les performances récentes de chaque agent
                    List<PerformanceAgent> perf1 = performanceAgentRepository.findByAgentId(a1.getId());
                    List<PerformanceAgent> perf2 = performanceAgentRepository.findByAgentId(a2.getId());
                    
                    double score1 = perf1.isEmpty() ? 0.0 : perf1.get(0).getScore();
                    double score2 = perf2.isEmpty() ? 0.0 : perf2.get(0).getScore();
                    
                    return Double.compare(score2, score1); // Tri décroissant
                })
                .collect(Collectors.toList());
    }

    // ==================== Méthodes de validation des rôles ====================

    @Override
    public boolean peutCreerUtilisateur(Long createurId, RoleUtilisateur roleCree) {
        Optional<Utilisateur> createur = utilisateurRepository.findById(createurId);
        if (!createur.isPresent()) {
            return false;
        }

        RoleUtilisateur roleCreateur = createur.get().getRoleUtilisateur();
        
        // Un super admin peut créer n'importe qui
        if (roleCreateur == RoleUtilisateur.SUPER_ADMIN) {
            return true;
        }
        
        // Un chef peut créer des agents
        if (estChef(roleCreateur) && estAgent(roleCree)) {
            return true;
        }
        
        // Un agent ne peut pas créer d'autres utilisateurs
        return false;
    }

    // ==================== Méthodes utilitaires ====================

    /**
     * Vérifie si un rôle correspond à un agent
     */
    private boolean estAgent(RoleUtilisateur role) {
        return role != null && role.name().startsWith("AGENT_");
    }

    /**
     * Vérifie si un rôle correspond à un chef
     */
    private boolean estChef(RoleUtilisateur role) {
        return role != null && role.name().startsWith("CHEF_");
    }

    /**
     * Récupère la période actuelle (format: YYYY-MM)
     */
    private String getPeriodeActuelle() {
        LocalDateTime now = LocalDateTime.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }

    /**
     * Récupère le super admin
     */
    private Utilisateur getSuperAdmin() {
        return utilisateurRepository.findByRoleUtilisateur(RoleUtilisateur.SUPER_ADMIN)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun super admin trouvé"));
    }

    /**
     * Récupère les agents d'un chef
     * Pour le chef dossier, retourner uniquement les agents dossier
     */
    @Override
    public List<Utilisateur> getAgentsByChef(Long chefId) {
        // Récupérer le chef
        Utilisateur chef = utilisateurRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef non trouvé avec l'ID: " + chefId));

        // Déterminer le rôle du chef pour filtrer les agents appropriés
        RoleUtilisateur chefRole = chef.getRoleUtilisateur();

        if (!(estChef(chefRole) || chefRole == RoleUtilisateur.SUPER_ADMIN)) {
            throw new RuntimeException("Le rôle " + chefRole + " n'est pas autorisé à consulter les agents.");
        }

        List<RoleUtilisateur> rolesAutorises = new java.util.ArrayList<>();
        if (chefRole == RoleUtilisateur.CHEF_DEPARTEMENT_DOSSIER) {
            rolesAutorises.add(RoleUtilisateur.AGENT_DOSSIER);
        } else if (chefRole == RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE) {
            rolesAutorises.add(RoleUtilisateur.AGENT_RECOUVREMENT_AMIABLE);
        } else if (chefRole == RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE) {
            rolesAutorises.add(RoleUtilisateur.AGENT_RECOUVREMENT_JURIDIQUE);
        } else if (chefRole == RoleUtilisateur.CHEF_DEPARTEMENT_FINANCE) {
            rolesAutorises.add(RoleUtilisateur.AGENT_FINANCE);
        } else if (chefRole == RoleUtilisateur.SUPER_ADMIN) {
            rolesAutorises.addAll(java.util.Arrays.asList(
                    RoleUtilisateur.AGENT_DOSSIER,
                    RoleUtilisateur.AGENT_RECOUVREMENT_AMIABLE,
                    RoleUtilisateur.AGENT_RECOUVREMENT_JURIDIQUE,
                    RoleUtilisateur.AGENT_FINANCE
            ));
        }

        // Préférence : récupérer les agents explicitement liés au chef
        List<Utilisateur> agentsAssocies = utilisateurRepository.findByChefCreateurId(chefId);

        if (agentsAssocies.isEmpty()) {
            // Rétrocompatibilité : retourner les agents par rôle si la relation n'est pas encore renseignée
            if (rolesAutorises.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            return utilisateurRepository.findByRoleUtilisateurIn(rolesAutorises);
        }

        if (rolesAutorises.isEmpty()) {
            return agentsAssocies;
        }

        return agentsAssocies.stream()
                .filter(agent -> agent.getRoleUtilisateur() != null && rolesAutorises.contains(agent.getRoleUtilisateur()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Utilisateur mettreAJourStatutActif(Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));
        
        // Calculer et mettre à jour le statut actif
        user.mettreAJourStatutActif();
        
        return utilisateurRepository.save(user);
    }

    @Override
    @Transactional
    public int mettreAJourStatutActifTous() {
        List<Utilisateur> tousLesUtilisateurs = utilisateurRepository.findAll();
        int count = 0;
        
        for (Utilisateur user : tousLesUtilisateurs) {
            boolean ancienStatut = user.getActif() != null ? user.getActif() : false;
            user.mettreAJourStatutActif();
            boolean nouveauStatut = user.getActif() != null ? user.getActif() : false;
            
            // Sauvegarder seulement si le statut a changé
            if (ancienStatut != nouveauStatut) {
                utilisateurRepository.save(user);
                count++;
            }
        }
        
        return count;
    }

    @Override
    @Transactional
    public Utilisateur reinitialiserMotDePasse(Long userId, String nouveauMotDePasse) {
        if (nouveauMotDePasse == null || nouveauMotDePasse.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nouveau mot de passe ne peut pas être vide");
        }

        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        // Encoder le nouveau mot de passe
        String encodedPassword = passwordEncoder.encode(nouveauMotDePasse);
        user.setMotDePasse(encodedPassword);

        return utilisateurRepository.save(user);
    }
}