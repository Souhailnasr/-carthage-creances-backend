package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.DTO.*;
import projet.carthagecreance_backend.Entity.TacheUrgente;
import projet.carthagecreance_backend.Entity.StatutTache;
import projet.carthagecreance_backend.Entity.PrioriteTache;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Entity.RoleUtilisateur;
import projet.carthagecreance_backend.Mapper.TacheUrgenteMapper;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Repository.EnquetteRepository;
import projet.carthagecreance_backend.SecurityServices.UserExtractionService;
import projet.carthagecreance_backend.Service.TacheUrgenteService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST complet pour la gestion des tâches urgentes
 * 
 * Ce contrôleur expose toutes les opérations CRUD et les fonctionnalités métier
 * pour la gestion des tâches urgentes dans le système de gestion de créances.
 * 
 * @author Système de Gestion de Créances
 * @version 1.0
 */
@RestController
@RequestMapping("/api/taches-urgentes")
@CrossOrigin(origins = "*")
public class TacheUrgenteController {

    @Autowired
    private TacheUrgenteService tacheUrgenteService;
    
    @Autowired
    private TacheUrgenteMapper tacheUrgenteMapper;
    
    @Autowired
    private UserExtractionService userExtractionService;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private EnquetteRepository enquetteRepository;

    /**
     * Crée une nouvelle tâche urgente (Chefs uniquement)
     * 
     * @param request DTO de création de tâche
     * @param authHeader Token JWT pour extraire le chef créateur
     * @return ResponseEntity avec la tâche créée (201 CREATED) ou erreur (400 BAD_REQUEST)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CHEF_DEPARTEMENT_DOSSIER', 'CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE', 'CHEF_DEPARTEMENT_FINANCE', 'SUPER_ADMIN')")
    public ResponseEntity<TacheUrgenteDTO> createTacheUrgente(
            @RequestBody CreateTacheUrgenteRequest request,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            // Extraire le chef créateur depuis le token
            Utilisateur chef = userExtractionService.extractUserFromToken(authHeader);
            if (chef == null) {
                return ResponseEntity.status(401).build();
            }
            
            // Vérifier que c'est un chef ou super admin
            if (!chef.getRoleUtilisateur().name().contains("CHEF") && 
                chef.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN) {
                return ResponseEntity.status(403).build();
            }
            
            // Créer l'entité TacheUrgente
            TacheUrgente tache = TacheUrgente.builder()
                    .titre(request.getTitre())
                    .description(request.getDescription())
                    .type(request.getType())
                    .priorite(request.getPriorite())
                    .dateEcheance(request.getDateEcheance())
                    .chefCreateur(chef)
                    .build();
            
            // Assigner l'agent
            Long agentId = request.getAgentAssignéId();
            if (agentId == null) {
                return ResponseEntity.badRequest().build();
            }
            Utilisateur agent = utilisateurRepository.findById(agentId)
                    .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + agentId));
            tache.setAgentAssigné(agent);
            
            // Lier le dossier si fourni
            if (request.getDossierId() != null) {
                Long dossierId = request.getDossierId();
                tache.setDossier(dossierRepository.findById(dossierId)
                        .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId)));
            }
            
            // Lier l'enquête si fournie
            if (request.getEnqueteId() != null) {
                Long enqueteId = request.getEnqueteId();
                tache.setEnquete(enquetteRepository.findById(enqueteId)
                        .orElseThrow(() -> new RuntimeException("Enquête non trouvée avec l'ID: " + enqueteId)));
            }
            
            TacheUrgente createdTache = tacheUrgenteService.createTacheUrgente(tache);
            return ResponseEntity.ok(tacheUrgenteMapper.toDTO(createdTache));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Met à jour une tâche urgente existante
     * 
     * @param id L'ID de la tâche à modifier
     * @param tache Les nouvelles données de la tâche
     * @return ResponseEntity avec la tâche mise à jour (200 OK) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * PUT /api/taches-urgentes/1
     * {
     *   "titre": "Relance client urgent - Mise à jour",
     *   "description": "Relancer le client pour le paiement de la facture 2024-001 - Urgent",
     *   "priorite": "TRES_HAUTE"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<TacheUrgente> updateTacheUrgente(@PathVariable Long id, @RequestBody TacheUrgente tache) {
        try {
            TacheUrgente updatedTache = tacheUrgenteService.updateTacheUrgente(id, tache);
            return ResponseEntity.ok(updatedTache);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprime une tâche urgente
     * 
     * @param id L'ID de la tâche à supprimer
     * @return ResponseEntity vide (204 NO_CONTENT) ou erreur (404 NOT_FOUND)
     * 
     * @example
     * DELETE /api/taches-urgentes/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTacheUrgente(@PathVariable Long id) {
        try {
            tacheUrgenteService.deleteTacheUrgente(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère une tâche urgente par son ID
     * 
     * @param id L'ID de la tâche
     * @param authHeader Token JWT pour vérifier les permissions
     * @return ResponseEntity avec la tâche trouvée (200 OK) ou erreur (404 NOT_FOUND)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TacheUrgenteDTO> getTacheUrgenteById(
            @PathVariable Long id,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            TacheUrgente tache = tacheUrgenteService.getTacheUrgenteById(id);
            
            // Vérifier les permissions
            Utilisateur user = userExtractionService.extractUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            // Vérifier que l'utilisateur a le droit de voir cette tâche
            boolean canView = false;
            if (user.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                canView = true;
            } else if (user.getRoleUtilisateur().name().contains("CHEF")) {
                // Chef peut voir ses tâches créées
                canView = tache.getChefCreateur() != null && 
                         tache.getChefCreateur().getId().equals(user.getId());
            } else {
                // Agent peut voir ses tâches assignées
                canView = tache.getAgentAssigné() != null && 
                         tache.getAgentAssigné().getId().equals(user.getId());
            }
            
            if (!canView) {
                return ResponseEntity.status(403).build();
            }
            
            return ResponseEntity.ok(tacheUrgenteMapper.toDTO(tache));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère toutes les tâches urgentes (filtrées selon le rôle)
     * 
     * @param authHeader Token JWT pour extraire l'utilisateur
     * @return ResponseEntity avec la liste des tâches (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<TacheUrgenteDTO>> getAllTachesUrgentes(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur user = userExtractionService.extractUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<TacheUrgente> taches;
            
            // Filtrer selon le rôle
            if (user.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                // SuperAdmin voit toutes les tâches
                taches = tacheUrgenteService.getAllTachesUrgentes();
            } else if (user.getRoleUtilisateur().name().contains("CHEF")) {
                // Chef voit les tâches qu'il a créées + les tâches de ses agents
                taches = tacheUrgenteService.getTachesByChef(user.getId());
            } else {
                // Agent voit uniquement ses tâches
                taches = tacheUrgenteService.getTachesByAgent(user.getId());
            }
            
            List<TacheUrgenteDTO> dtos = taches.stream()
                    .map(tacheUrgenteMapper::toDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS DE FILTRAGE ====================

    /**
     * Récupère les tâches par agent (Agent peut voir ses tâches, Chef peut voir les tâches de ses agents)
     * 
     * @param agentId L'ID de l'agent
     * @param authHeader Token JWT pour vérifier les permissions
     * @return ResponseEntity avec la liste des tâches de l'agent (200 OK)
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<TacheUrgenteDTO>> getTachesByAgent(
            @PathVariable Long agentId,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur user = userExtractionService.extractUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            // Vérifier les permissions
            boolean canView = false;
            if (user.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                canView = true;
            } else if (user.getRoleUtilisateur().name().contains("CHEF")) {
                // Chef peut voir les tâches de ses agents
                Utilisateur agent = utilisateurRepository.findById(agentId).orElse(null);
                if (agent != null) {
                    // Vérifier que l'agent appartient au département du chef
                    canView = isAgentInChefDepartment(user, agent);
                }
            } else {
                // Agent peut voir uniquement ses propres tâches
                canView = user.getId().equals(agentId);
            }
            
            if (!canView) {
                return ResponseEntity.status(403).build();
            }
            
            List<TacheUrgente> taches = tacheUrgenteService.getTachesByAgent(agentId);
            List<TacheUrgenteDTO> dtos = taches.stream()
                    .map(tacheUrgenteMapper::toDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Récupère les tâches de l'utilisateur connecté
     */
    @GetMapping("/mes-taches")
    public ResponseEntity<List<TacheUrgenteDTO>> getMesTaches(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur user = userExtractionService.extractUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<TacheUrgente> taches;
            if (user.getRoleUtilisateur().name().contains("CHEF")) {
                // Chef voit les tâches qu'il a créées
                taches = tacheUrgenteService.getTachesByChef(user.getId());
            } else {
                // Agent voit ses tâches assignées
                taches = tacheUrgenteService.getTachesByAgent(user.getId());
            }
            
            List<TacheUrgenteDTO> dtos = taches.stream()
                    .map(tacheUrgenteMapper::toDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les tâches par statut (filtrées selon le rôle)
     * 
     * @param statut Le statut des tâches (EN_ATTENTE, EN_COURS, TERMINEE, ANNULEE)
     * @param authHeader Token JWT pour filtrer selon l'utilisateur
     * @return ResponseEntity avec la liste des tâches avec le statut spécifié (200 OK)
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<TacheUrgenteDTO>> getTachesByStatut(
            @PathVariable StatutTache statut,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur user = userExtractionService.extractUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<TacheUrgente> taches;
            if (user.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                taches = tacheUrgenteService.getTachesByStatut(statut);
            } else if (user.getRoleUtilisateur().name().contains("CHEF")) {
                // Chef voit les tâches de son département avec ce statut
                List<TacheUrgente> allTaches = tacheUrgenteService.getTachesByStatut(statut);
                taches = allTaches.stream()
                        .filter(t -> t.getChefCreateur() != null && t.getChefCreateur().getId().equals(user.getId()) ||
                                    (t.getAgentAssigné() != null && isAgentInChefDepartment(user, t.getAgentAssigné())))
                        .collect(Collectors.toList());
            } else {
                // Agent voit ses tâches avec ce statut
                taches = tacheUrgenteService.getTachesByAgentAndStatut(user.getId(), statut);
            }
            
            List<TacheUrgenteDTO> dtos = taches.stream()
                    .map(tacheUrgenteMapper::toDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les tâches par priorité
     * 
     * @param priorite La priorité des tâches (FAIBLE, MOYENNE, HAUTE, TRES_HAUTE)
     * @return ResponseEntity avec la liste des tâches avec la priorité spécifiée (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/priorite/HAUTE
     */
    @GetMapping("/priorite/{priorite}")
    public ResponseEntity<List<TacheUrgente>> getTachesByPriorite(@PathVariable PrioriteTache priorite) {
        try {
            List<TacheUrgente> taches = tacheUrgenteService.getTachesByPriorite(priorite);
            return ResponseEntity.ok(taches);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les tâches urgentes (échéance proche, filtrées selon le rôle)
     * 
     * @param dateLimite Date limite pour considérer une tâche comme urgente (optionnel, défaut: 3 jours)
     * @param authHeader Token JWT pour filtrer selon l'utilisateur
     * @return ResponseEntity avec la liste des tâches urgentes (200 OK)
     */
    @GetMapping("/urgentes")
    public ResponseEntity<List<TacheUrgenteDTO>> getTachesUrgentes(
            @RequestParam(required = false) LocalDateTime dateLimite,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur user = userExtractionService.extractUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            if (dateLimite == null) {
                dateLimite = LocalDateTime.now().plusDays(3); // Par défaut 3 jours
            }
            
            List<TacheUrgente> allTaches = tacheUrgenteService.getTachesUrgentes(dateLimite);
            List<TacheUrgente> taches;
            
            if (user.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                taches = allTaches;
            } else if (user.getRoleUtilisateur().name().contains("CHEF")) {
                // Chef voit les tâches urgentes de son département
                taches = allTaches.stream()
                        .filter(t -> t.getChefCreateur() != null && t.getChefCreateur().getId().equals(user.getId()) ||
                                    (t.getAgentAssigné() != null && isAgentInChefDepartment(user, t.getAgentAssigné())))
                        .collect(Collectors.toList());
            } else {
                // Agent voit ses tâches urgentes
                taches = allTaches.stream()
                        .filter(t -> t.getAgentAssigné() != null && t.getAgentAssigné().getId().equals(user.getId()))
                        .collect(Collectors.toList());
            }
            
            List<TacheUrgenteDTO> dtos = taches.stream()
                    .map(tacheUrgenteMapper::toDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les tâches en retard (filtrées selon le rôle)
     * 
     * @param authHeader Token JWT pour filtrer selon l'utilisateur
     * @return ResponseEntity avec la liste des tâches en retard (200 OK)
     */
    @GetMapping("/en-retard")
    public ResponseEntity<List<TacheUrgenteDTO>> getTachesEnRetard(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur user = userExtractionService.extractUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<TacheUrgente> allTaches = tacheUrgenteService.getTachesEnRetard();
            List<TacheUrgente> taches;
            
            if (user.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                taches = allTaches;
            } else if (user.getRoleUtilisateur().name().contains("CHEF")) {
                // Chef voit les tâches en retard de son département
                taches = allTaches.stream()
                        .filter(t -> t.getChefCreateur() != null && t.getChefCreateur().getId().equals(user.getId()) ||
                                    (t.getAgentAssigné() != null && isAgentInChefDepartment(user, t.getAgentAssigné())))
                        .collect(Collectors.toList());
            } else {
                // Agent voit ses tâches en retard
                taches = allTaches.stream()
                        .filter(t -> t.getAgentAssigné() != null && t.getAgentAssigné().getId().equals(user.getId()))
                        .collect(Collectors.toList());
            }
            
            List<TacheUrgenteDTO> dtos = taches.stream()
                    .map(tacheUrgenteMapper::toDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les tâches par agent et priorité
     * 
     * @param agentId L'ID de l'agent
     * @param priorite La priorité des tâches
     * @return ResponseEntity avec la liste des tâches correspondantes (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/agent/1/priorite/HAUTE
     */
    @GetMapping("/agent/{agentId}/priorite/{priorite}")
    public ResponseEntity<List<TacheUrgente>> getTachesByAgentAndPriorite(
            @PathVariable Long agentId, 
            @PathVariable PrioriteTache priorite) {
        try {
            List<TacheUrgente> taches = tacheUrgenteService.getTachesByAgentAndPriorite(agentId, priorite);
            return ResponseEntity.ok(taches);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS D'ACTIONS ====================

    /**
     * Marque une tâche comme terminée (Agent uniquement)
     * 
     * @param tacheId L'ID de la tâche
     * @param request DTO avec commentaire
     * @param authHeader Token JWT pour vérifier les permissions
     * @return ResponseEntity avec la tâche mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     */
    @PostMapping("/{tacheId}/complete")
    @PreAuthorize("hasAnyRole('AGENT_DOSSIER', 'AGENT_RECOUVREMENT_AMIABLE', 'AGENT_RECOUVREMENT_JURIDIQUE', 'AGENT_FINANCE', 'SUPER_ADMIN')")
    public ResponseEntity<TacheUrgenteDTO> marquerComplete(
            @PathVariable Long tacheId,
            @RequestBody(required = false) CompleteTacheRequest request,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur user = userExtractionService.extractUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            TacheUrgente tache = tacheUrgenteService.getTacheUrgenteById(tacheId);
            
            // Vérifier que l'utilisateur est l'agent assigné ou le chef créateur
            boolean canComplete = false;
            if (user.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                canComplete = true;
            } else if (tache.getAgentAssigné() != null && tache.getAgentAssigné().getId().equals(user.getId())) {
                canComplete = true;
            } else if (tache.getChefCreateur() != null && tache.getChefCreateur().getId().equals(user.getId())) {
                canComplete = true;
            }
            
            if (!canComplete) {
                return ResponseEntity.status(403).build();
            }
            
            String commentaire = request != null ? request.getCommentaire() : null;
            TacheUrgente updatedTache = tacheUrgenteService.marquerComplete(tacheId, commentaire);
            return ResponseEntity.ok(tacheUrgenteMapper.toDTO(updatedTache));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marque une tâche comme en cours (Agent uniquement)
     * 
     * @param tacheId L'ID de la tâche
     * @param authHeader Token JWT pour vérifier les permissions
     * @return ResponseEntity avec la tâche mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     */
    @PostMapping("/{tacheId}/en-cours")
    @PreAuthorize("hasAnyRole('AGENT_DOSSIER', 'AGENT_RECOUVREMENT_AMIABLE', 'AGENT_RECOUVREMENT_JURIDIQUE', 'AGENT_FINANCE', 'SUPER_ADMIN')")
    public ResponseEntity<TacheUrgenteDTO> marquerEnCours(
            @PathVariable Long tacheId,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur user = userExtractionService.extractUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            TacheUrgente tache = tacheUrgenteService.getTacheUrgenteById(tacheId);
            
            // Vérifier que l'utilisateur est l'agent assigné ou le chef créateur
            boolean canUpdate = false;
            if (user.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                canUpdate = true;
            } else if (tache.getAgentAssigné() != null && tache.getAgentAssigné().getId().equals(user.getId())) {
                canUpdate = true;
            } else if (tache.getChefCreateur() != null && tache.getChefCreateur().getId().equals(user.getId())) {
                canUpdate = true;
            }
            
            if (!canUpdate) {
                return ResponseEntity.status(403).build();
            }
            
            TacheUrgente updatedTache = tacheUrgenteService.marquerEnCours(tacheId);
            return ResponseEntity.ok(tacheUrgenteMapper.toDTO(updatedTache));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Annule une tâche (Chef créateur ou SuperAdmin uniquement)
     * 
     * @param tacheId L'ID de la tâche
     * @param request DTO avec raison d'annulation
     * @param authHeader Token JWT pour vérifier les permissions
     * @return ResponseEntity avec la tâche mise à jour (200 OK) ou erreur (400 BAD_REQUEST)
     */
    @PostMapping("/{tacheId}/annuler")
    @PreAuthorize("hasAnyRole('CHEF_DEPARTEMENT_DOSSIER', 'CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE', 'CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE', 'CHEF_DEPARTEMENT_FINANCE', 'SUPER_ADMIN')")
    public ResponseEntity<TacheUrgenteDTO> annulerTache(
            @PathVariable Long tacheId,
            @RequestBody AnnulerTacheRequest request,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            Utilisateur user = userExtractionService.extractUserFromToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            TacheUrgente tache = tacheUrgenteService.getTacheUrgenteById(tacheId);
            
            // Vérifier que l'utilisateur est le chef créateur ou super admin
            boolean canCancel = false;
            if (user.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
                canCancel = true;
            } else if (tache.getChefCreateur() != null && tache.getChefCreateur().getId().equals(user.getId())) {
                canCancel = true;
            }
            
            if (!canCancel) {
                return ResponseEntity.status(403).build();
            }
            
            TacheUrgente updatedTache = tacheUrgenteService.annulerTache(tacheId, request.getRaison());
            return ResponseEntity.ok(tacheUrgenteMapper.toDTO(updatedTache));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Méthode utilitaire pour vérifier si un agent appartient au département d'un chef
     */
    private boolean isAgentInChefDepartment(Utilisateur chef, Utilisateur agent) {
        RoleUtilisateur roleChef = chef.getRoleUtilisateur();
        RoleUtilisateur roleAgent = agent.getRoleUtilisateur();
        
        switch (roleChef) {
            case CHEF_DEPARTEMENT_DOSSIER:
                return roleAgent == RoleUtilisateur.AGENT_DOSSIER;
            case CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE:
                return roleAgent == RoleUtilisateur.AGENT_RECOUVREMENT_AMIABLE;
            case CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE:
                return roleAgent == RoleUtilisateur.AGENT_RECOUVREMENT_JURIDIQUE;
            case CHEF_DEPARTEMENT_FINANCE:
                return roleAgent == RoleUtilisateur.AGENT_FINANCE;
            default:
                return false;
        }
    }

    // ==================== ENDPOINTS DE STATISTIQUES ====================

    /**
     * Compte les tâches par agent
     * 
     * @param agentId L'ID de l'agent
     * @return ResponseEntity avec le nombre de tâches de l'agent (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/statistiques/agent/1
     */
    @GetMapping("/statistiques/agent/{agentId}")
    public ResponseEntity<Long> countTachesByAgent(@PathVariable Long agentId) {
        try {
            long count = tacheUrgenteService.countTachesByAgent(agentId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compte les tâches par statut
     * 
     * @param statut Le statut des tâches
     * @return ResponseEntity avec le nombre de tâches avec ce statut (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/statistiques/statut/EN_COURS
     */
    @GetMapping("/statistiques/statut/{statut}")
    public ResponseEntity<Long> countTachesByStatut(@PathVariable StatutTache statut) {
        try {
            long count = tacheUrgenteService.countTachesByStatut(statut);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compte les tâches par agent et statut
     * 
     * @param agentId L'ID de l'agent
     * @param statut Le statut des tâches
     * @return ResponseEntity avec le nombre de tâches correspondantes (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/statistiques/agent/1/statut/EN_COURS
     */
    @GetMapping("/statistiques/agent/{agentId}/statut/{statut}")
    public ResponseEntity<Long> countTachesByAgentAndStatut(
            @PathVariable Long agentId, 
            @PathVariable StatutTache statut) {
        try {
            long count = tacheUrgenteService.countTachesByAgentAndStatut(agentId, statut);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compte les tâches urgentes
     * 
     * @return ResponseEntity avec le nombre de tâches urgentes (200 OK)
     * 
     * @example
     * GET /api/taches-urgentes/statistiques/urgentes
     */
    @GetMapping("/statistiques/urgentes")
    public ResponseEntity<Long> countTachesUrgentes() {
        try {
            long count = tacheUrgenteService.countTachesUrgentes();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
