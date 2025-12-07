package projet.carthagecreance_backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.Service.UtilisateurService;
import projet.carthagecreance_backend.SecurityServices.UserExtractionService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des utilisateurs par Superadmin
 * Améliore UtilisateurController avec fonctionnalités admin avancées
 */
@RestController
@RequestMapping("/api/admin/utilisateurs")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminUtilisateurController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUtilisateurController.class);

    @Autowired
    private UtilisateurService utilisateurService;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private UserExtractionService userExtractionService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET /api/admin/utilisateurs
     * Récupère tous les utilisateurs avec filtres, pagination et performance
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUtilisateurs(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String departement,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String recherche,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // ✅ Extraire l'utilisateur connecté pour appliquer le filtre par créateur
            Utilisateur utilisateurConnecte = userExtractionService.extractUserFromToken(authHeader);
            if (utilisateurConnecte == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Non autorisé"));
            }
            
            List<Utilisateur> tousUtilisateurs;
            
            // ✅ NOUVEAU : Filtrer selon le rôle de l'utilisateur connecté
            if (utilisateurConnecte.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                // SUPER_ADMIN voit tous les utilisateurs
                tousUtilisateurs = utilisateurRepository.findAll();
            } else if (estChef(utilisateurConnecte.getRoleUtilisateur())) {
                // Chef ne voit que les utilisateurs qu'il a créés
                tousUtilisateurs = utilisateurRepository.findByCreateurId(utilisateurConnecte.getId());
            } else {
                // Autres rôles : liste vide (pas d'accès)
                tousUtilisateurs = new ArrayList<>();
            }
            
            // Filtrer par les autres critères
            List<Utilisateur> filtres = tousUtilisateurs.stream()
                    .filter(u -> role == null || u.getRoleUtilisateur() != null && 
                            u.getRoleUtilisateur().name().contains(role.toUpperCase()))
                    .filter(u -> actif == null || (u.getActif() != null && u.getActif().equals(actif)))
                    .filter(u -> recherche == null || recherche.isEmpty() ||
                            (u.getNom() != null && u.getNom().toLowerCase().contains(recherche.toLowerCase())) ||
                            (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(recherche.toLowerCase())) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase().contains(recherche.toLowerCase())))
                    .collect(Collectors.toList());
            
            // Pagination manuelle
            int start = page * size;
            int end = Math.min(start + size, filtres.size());
            List<Utilisateur> pageContent = start < filtres.size() ? 
                    filtres.subList(start, end) : new ArrayList<>();
            
            // Calculer performance pour chaque utilisateur
            List<Map<String, Object>> utilisateursAvecPerformance = pageContent.stream()
                    .map(u -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", u.getId());
                        userMap.put("nom", u.getNom());
                        userMap.put("prenom", u.getPrenom());
                        userMap.put("email", u.getEmail());
                        userMap.put("role", u.getRoleUtilisateur() != null ? u.getRoleUtilisateur().name() : null);
                        userMap.put("departement", u.getRoleUtilisateur() != null ? 
                                extraireDepartement(u.getRoleUtilisateur()) : null);
                        userMap.put("actif", u.getActif() != null ? u.getActif() : false);
                        userMap.put("derniereConnexion", u.getDerniereConnexion());
                        
                        // Calculer performance
                        Map<String, Object> performance = calculerPerformance(u.getId());
                        userMap.put("performance", performance);
                        
                        return userMap;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = Map.of(
                    "content", utilisateursAvecPerformance,
                    "totalElements", filtres.size(),
                    "totalPages", (int) Math.ceil((double) filtres.size() / size),
                    "currentPage", page,
                    "size", size
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des utilisateurs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/admin/utilisateurs
     * Crée un nouvel utilisateur avec logging audit
     */
    @PostMapping
    public ResponseEntity<?> createUtilisateur(
            @RequestBody Utilisateur utilisateur,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur createur = userExtractionService.extractUserFromToken(authHeader);
            if (createur == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Non autorisé - Token invalide"));
            }
            
            // Vérifier les droits : SUPER_ADMIN ou CHEF peut créer
            if (createur.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN && 
                !estChef(createur.getRoleUtilisateur())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Vous n'avez pas les droits pour créer un utilisateur"));
            }
            
            // Vérifier email unique
            if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Un utilisateur avec cet email existe déjà"));
            }
            
            // Hasher le mot de passe
            if (utilisateur.getMotDePasse() != null) {
                utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
            }
            
            // Forcer actif = false pour les nouveaux utilisateurs (indépendamment de ce que le frontend envoie)
            // Le statut actif sera calculé automatiquement par calculerStatutActif() basé sur les dates de connexion/déconnexion
            utilisateur.setActif(false);
            
            // ✅ Créer l'utilisateur avec le créateur
            utilisateurService.createUtilisateur(utilisateur, createur);
            
            Utilisateur created = utilisateurRepository.findByEmail(utilisateur.getEmail())
                    .orElse(null);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'utilisateur: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création: " + e.getMessage()));
        }
    }
    
    /**
     * Vérifie si un rôle est un rôle de chef
     */
    private boolean estChef(RoleUtilisateur role) {
        return role == RoleUtilisateur.CHEF_DEPARTEMENT_DOSSIER ||
               role == RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE ||
               role == RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE ||
               role == RoleUtilisateur.CHEF_DEPARTEMENT_FINANCE;
    }

    /**
     * PUT /api/admin/utilisateurs/{id}
     * Met à jour un utilisateur avec logging audit
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUtilisateur(
            @PathVariable Long id,
            @RequestBody Utilisateur utilisateur,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur superadmin = userExtractionService.extractUserFromToken(authHeader);
            if (superadmin == null || superadmin.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Vérifier que l'ID n'est pas null
            if (id == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'ID utilisateur est requis"));
            }
            
            // Vérifier que l'utilisateur existe
            Optional<Utilisateur> existingOpt = utilisateurRepository.findById(id);
            if (existingOpt.isEmpty()) {
                logger.warn("Tentative de modification d'un utilisateur inexistant: ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Utilisateur non trouvé avec l'ID: " + id));
            }
            
            Utilisateur existing = existingOpt.get();
            
            // Vérifier que l'utilisateur a un ID valide
            if (existing.getId() == null) {
                logger.error("Erreur : L'utilisateur existant n'a pas d'ID");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur interne : utilisateur invalide"));
            }
            
            // Mettre à jour
            utilisateur.setId(id);
            if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isEmpty()) {
                utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
            } else {
                utilisateur.setMotDePasse(existing.getMotDePasse());
            }
            
            Utilisateur updated = utilisateurService.updateUtilisateur(id, utilisateur);
            
            // Vérifier que la mise à jour a réussi
            if (updated == null || updated.getId() == null) {
                logger.error("Erreur : La mise à jour a retourné un utilisateur null ou sans ID");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur lors de la mise à jour de l'utilisateur"));
            }
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/admin/utilisateurs/{id}/reinitialiser-mot-de-passe
     * Réinitialise le mot de passe d'un utilisateur
     */
    @PutMapping("/{id}/reinitialiser-mot-de-passe")
    public ResponseEntity<?> reinitialiserMotDePasse(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Vérifier que l'ID n'est pas null
            if (id == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'ID utilisateur est requis"));
            }
            
            Utilisateur superadmin = userExtractionService.extractUserFromToken(authHeader);
            if (superadmin == null || superadmin.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Vérifier que le Superadmin a un ID valide
            if (superadmin.getId() == null) {
                logger.error("Erreur : Le Superadmin n'a pas d'ID valide");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur interne : utilisateur invalide"));
            }
            
            Optional<Utilisateur> userOpt = utilisateurRepository.findById(id);
            if (userOpt.isEmpty()) {
                logger.warn("Tentative de réinitialisation du mot de passe pour un utilisateur inexistant: ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Utilisateur non trouvé avec l'ID: " + id));
            }
            
            Utilisateur user = userOpt.get();
            
            // Vérifier que l'utilisateur a un ID valide
            if (user.getId() == null) {
                logger.error("Erreur : L'utilisateur n'a pas d'ID valide");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur interne : utilisateur invalide"));
            }
            String nouveauMotDePasse;
            
            Boolean genererAutomatiquement = (Boolean) request.get("genererAutomatiquement");
            if (genererAutomatiquement != null && genererAutomatiquement) {
                // Générer mot de passe aléatoire
                nouveauMotDePasse = genererMotDePasseAleatoire();
            } else {
                nouveauMotDePasse = (String) request.get("nouveauMotDePasse");
                if (nouveauMotDePasse == null || nouveauMotDePasse.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "nouveauMotDePasse est requis"));
                }
            }
            
            user.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
            utilisateurRepository.save(user);
            
            Map<String, Object> response = Map.of(
                    "message", "Mot de passe réinitialisé avec succès",
                    "nouveauMotDePasse", genererAutomatiquement != null && genererAutomatiquement ? 
                            nouveauMotDePasse : "***"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la réinitialisation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/admin/utilisateurs/{id}/activer
     * Active un utilisateur
     */
    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerUtilisateur(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Vérifier que l'ID n'est pas null
            if (id == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'ID utilisateur est requis"));
            }
            
            Utilisateur superadmin = userExtractionService.extractUserFromToken(authHeader);
            if (superadmin == null || superadmin.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Vérifier que le Superadmin a un ID valide
            if (superadmin.getId() == null) {
                logger.error("Erreur : Le Superadmin n'a pas d'ID valide");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur interne : utilisateur invalide"));
            }
            
            Optional<Utilisateur> userOpt = utilisateurRepository.findById(id);
            if (userOpt.isEmpty()) {
                logger.warn("Tentative d'activation d'un utilisateur inexistant: ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Utilisateur non trouvé avec l'ID: " + id));
            }
            
            Utilisateur user = userOpt.get();
            
            // Vérifier que l'utilisateur a un ID valide
            if (user.getId() == null) {
                logger.error("Erreur : L'utilisateur n'a pas d'ID valide");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur interne : utilisateur invalide"));
            }
            user.setActif(true);
            Utilisateur saved = utilisateurRepository.save(user);
            
            // Vérifier que la sauvegarde a réussi
            if (saved == null || saved.getId() == null) {
                logger.error("Erreur : La sauvegarde a retourné un utilisateur null ou sans ID");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur lors de l'activation de l'utilisateur"));
            }
            
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Erreur lors de l'activation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/admin/utilisateurs/{id}/desactiver
     * Désactive un utilisateur
     */
    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverUtilisateur(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Vérifier que l'ID n'est pas null
            if (id == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'ID utilisateur est requis"));
            }
            
            Utilisateur superadmin = userExtractionService.extractUserFromToken(authHeader);
            if (superadmin == null || superadmin.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Vérifier que le Superadmin a un ID valide
            if (superadmin.getId() == null) {
                logger.error("Erreur : Le Superadmin n'a pas d'ID valide");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur interne : utilisateur invalide"));
            }
            
            Optional<Utilisateur> userOpt = utilisateurRepository.findById(id);
            if (userOpt.isEmpty()) {
                logger.warn("Tentative de désactivation d'un utilisateur inexistant: ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Utilisateur non trouvé avec l'ID: " + id));
            }
            
            Utilisateur user = userOpt.get();
            
            // Vérifier que l'utilisateur a un ID valide
            if (user.getId() == null) {
                logger.error("Erreur : L'utilisateur n'a pas d'ID valide");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur interne : utilisateur invalide"));
            }
            
            // Ne pas désactiver le Superadmin
            if (user.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Impossible de désactiver un Superadmin"));
            }
            
            user.setActif(false);
            Utilisateur saved = utilisateurRepository.save(user);
            
            // Vérifier que la sauvegarde a réussi
            if (saved == null || saved.getId() == null) {
                logger.error("Erreur : La sauvegarde a retourné un utilisateur null ou sans ID");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur lors de la désactivation de l'utilisateur"));
            }
            
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Erreur lors de la désactivation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /api/admin/utilisateurs/{id}
     * Supprime un utilisateur avec vérifications
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUtilisateur(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Vérifier que l'ID n'est pas null
            if (id == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'ID utilisateur est requis"));
            }
            
            Utilisateur superadmin = userExtractionService.extractUserFromToken(authHeader);
            if (superadmin == null || superadmin.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Vérifier que le Superadmin a un ID valide
            if (superadmin.getId() == null) {
                logger.error("Erreur : Le Superadmin n'a pas d'ID valide");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur interne : utilisateur invalide"));
            }
            
            Optional<Utilisateur> userOpt = utilisateurRepository.findById(id);
            if (userOpt.isEmpty()) {
                logger.warn("Tentative de suppression d'un utilisateur inexistant: ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Utilisateur non trouvé avec l'ID: " + id));
            }
            
            Utilisateur user = userOpt.get();
            
            // Vérifier que l'utilisateur a un ID valide
            if (user.getId() == null) {
                logger.error("Erreur : L'utilisateur n'a pas d'ID valide");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur interne : utilisateur invalide"));
            }
            
            // Ne pas supprimer le Superadmin
            if (user.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Impossible de supprimer un Superadmin"));
            }
            
            // Vérifier qu'il n'a pas de dossiers assignés
            List<Dossier> dossiersResponsable = dossierRepository.findByAgentResponsableId(id);
            List<Dossier> dossiersCreateur = dossierRepository.findByAgentCreateurId(id);
            long nbDossiers = dossiersResponsable.size() + dossiersCreateur.size();
            if (nbDossiers > 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'utilisateur a " + nbDossiers + " dossiers assignés. Réaffectez-les avant de supprimer."));
            }
            
            utilisateurService.deleteUtilisateur(id);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/admin/utilisateurs/{id}/performance
     * Récupère les métriques de performance d'un utilisateur
     */
    @GetMapping("/{id}/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceUtilisateur(@PathVariable Long id) {
        try {
            // Vérifier que l'ID n'est pas null
            if (id == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'ID utilisateur est requis"));
            }
            
            Optional<Utilisateur> userOpt = utilisateurRepository.findById(id);
            if (userOpt.isEmpty()) {
                logger.warn("Tentative de récupération de performance pour un utilisateur inexistant: ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Utilisateur non trouvé avec l'ID: " + id));
            }
            
            Utilisateur user = userOpt.get();
            
            // Vérifier que l'utilisateur a un ID valide
            if (user.getId() == null) {
                logger.error("Erreur : L'utilisateur n'a pas d'ID valide");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erreur interne : utilisateur invalide"));
            }
            Map<String, Object> performance = calculerPerformance(id);
            
            // Évolution mensuelle
            List<Map<String, Object>> evolutionPerformance = new ArrayList<>();
            for (int i = 11; i >= 0; i--) {
                LocalDate mois = LocalDate.now().minusMonths(i);
                String periode = mois.getYear() + "-" + String.format("%02d", mois.getMonthValue());
                
                // Calcul simplifié pour chaque mois
                double scoreMois = calculerScoreMois(id, mois);
                evolutionPerformance.add(Map.of("mois", periode, "score", Math.round(scoreMois * 100.0) / 100.0));
            }
            
            // Répartition par statut
            List<Dossier> dossiersAgent = dossierRepository.findByAgentResponsableId(id);
            dossiersAgent.addAll(dossierRepository.findByAgentCreateurId(id));
            Map<String, Long> repartitionParStatut = dossiersAgent.stream()
                    .distinct()
                    .collect(Collectors.groupingBy(
                            d -> d.getDossierStatus() != null ? d.getDossierStatus().name() : "INCONNU",
                            Collectors.counting()
                    ));
            
            // Dossiers récents
            List<Map<String, Object>> dossiersRecents = dossiersAgent.stream()
                    .distinct()
                    .sorted((d1, d2) -> {
                        if (d1.getDateCreation() == null) return 1;
                        if (d2.getDateCreation() == null) return -1;
                        return d2.getDateCreation().compareTo(d1.getDateCreation());
                    })
                    .limit(10)
                    .map(d -> {
                        double perfDossier = d.getMontantCreance() != null && d.getMontantRecouvre() != null ?
                                (d.getMontantRecouvre() / d.getMontantCreance() * 100) : 0.0;
                        return Map.<String, Object>of(
                                "id", d.getId(),
                                "reference", d.getNumeroDossier() != null ? d.getNumeroDossier() : "N/A",
                                "performance", Math.round(perfDossier * 100.0) / 100.0
                        );
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = Map.of(
                    "utilisateur", Map.of(
                            "id", user.getId(),
                            "nom", user.getNom() != null ? user.getNom() : "",
                            "prenom", user.getPrenom() != null ? user.getPrenom() : ""
                    ),
                    "metriques", performance,
                    "evolutionPerformance", evolutionPerformance,
                    "repartitionParStatut", repartitionParStatut,
                    "dossiersRecents", dossiersRecents
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la performance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== Méthodes utilitaires ====================
    
    private String extraireDepartement(RoleUtilisateur role) {
        if (role == null) return null;
        String roleStr = role.name();
        if (roleStr.contains("DOSSIER")) return "DOSSIER";
        if (roleStr.contains("AMIABLE")) return "AMIABLE";
        if (roleStr.contains("JURIDIQUE")) return "JURIDIQUE";
        if (roleStr.contains("FINANCE")) return "FINANCE";
        return null;
    }
    
    private Map<String, Object> calculerPerformance(Long userId) {
        List<Dossier> dossiersAgent = dossierRepository.findByAgentResponsableId(userId);
        dossiersAgent.addAll(dossierRepository.findByAgentCreateurId(userId));
        List<Dossier> dossiersUniques = dossiersAgent.stream()
                .distinct()
                .collect(Collectors.toList());
        
        long dossiersTraitesTotal = dossiersUniques.size();
        long dossiersClotures = dossiersUniques.stream()
                .filter(d -> d.getDossierStatus() == DossierStatus.CLOTURE)
                .count();
        
        double tauxRecouvrementPersonnel = dossiersUniques.stream()
                .filter(d -> d.getMontantCreance() != null && d.getMontantRecouvre() != null)
                .mapToDouble(d -> d.getMontantRecouvre() / d.getMontantCreance() * 100)
                .average()
                .orElse(0.0);
        
        double tempsMoyenTraitement = dossiersUniques.stream()
                .filter(d -> d.getDateCloture() != null && d.getDateCreation() != null)
                .mapToDouble(d -> ChronoUnit.DAYS.between(
                        d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                        d.getDateCloture().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                .average()
                .orElse(0.0);
        
        long dossiersEnCours = dossiersUniques.stream()
                .filter(d -> d.getDossierStatus() != DossierStatus.CLOTURE)
                .count();
        
        double scorePerformance = (dossiersClotures * 0.4) + 
                (tauxRecouvrementPersonnel * 0.4) + 
                (Math.max(0, 100 - tempsMoyenTraitement) * 0.2);
        
        return Map.of(
                "dossiersTraitesTotal", dossiersTraitesTotal,
                "tauxRecouvrementPersonnel", Math.round(tauxRecouvrementPersonnel * 100.0) / 100.0,
                "tempsMoyenTraitement", Math.round(tempsMoyenTraitement * 100.0) / 100.0,
                "dossiersEnCours", dossiersEnCours,
                "scorePerformance", Math.round(scorePerformance * 100.0) / 100.0
        );
    }
    
    private double calculerScoreMois(Long userId, LocalDate mois) {
        // Calcul simplifié pour un mois donné
        List<Dossier> dossiersAgent = dossierRepository.findByAgentResponsableId(userId);
        dossiersAgent.addAll(dossierRepository.findByAgentCreateurId(userId));
        
        long dossiersMois = dossiersAgent.stream()
                .filter(d -> d.getDateCreation() != null &&
                        d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                .getYear() == mois.getYear() &&
                        d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                .getMonthValue() == mois.getMonthValue())
                .count();
        
        return dossiersMois * 10.0; // Score simplifié
    }
    
    private String genererMotDePasseAleatoire() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}

