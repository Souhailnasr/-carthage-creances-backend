package projet.carthagecreance_backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.SecurityServices.UserExtractionService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la supervision des dossiers par Superadmin
 * Endpoints spécifiques pour la gestion avancée des dossiers
 */
@RestController
@RequestMapping("/api/admin/supervision")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminSupervisionController {

    private static final Logger logger = LoggerFactory.getLogger(AdminSupervisionController.class);

    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private CommentaireInterneRepository commentaireInterneRepository;
    
    @Autowired
    private UserExtractionService userExtractionService;

    /**
     * GET /api/admin/supervision/dossiers-clotures/statistiques
     * Retourne les statistiques des dossiers clôturés
     */
    @GetMapping("/dossiers-clotures/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiquesDossiersClotures() {
        try {
            List<Dossier> tousDossiers = dossierRepository.findAll();
            List<Dossier> dossiersClotures = tousDossiers.stream()
                    .filter(d -> d.getDossierStatus() == DossierStatus.CLOTURE)
                    .collect(Collectors.toList());
            
            long totalClotures = dossiersClotures.size();
            
            // Variation période précédente (simplifié - comparaison avec il y a 30 jours)
            LocalDateTime ilY30Jours = LocalDateTime.now().minusDays(30);
            long clotures30Jours = dossiersClotures.stream()
                    .filter(d -> d.getDateCloture() != null &&
                                d.getDateCloture().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().isAfter(ilY30Jours))
                    .count();
            
            long clotures60Jours = dossiersClotures.stream()
                    .filter(d -> d.getDateCloture() != null &&
                                d.getDateCloture().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().isAfter(LocalDateTime.now().minusDays(60)))
                    .count();
            
            double variation = clotures60Jours > 0 ? 
                    ((clotures30Jours - clotures60Jours) * 100.0 / clotures60Jours) : 0.0;
            
            // Taux recouvrement
            double montantTotalCreances = tousDossiers.stream()
                    .filter(d -> d.getMontantCreance() != null)
                    .mapToDouble(Dossier::getMontantCreance)
                    .sum();
            
            double montantTotalRecouvre = dossiersClotures.stream()
                    .filter(d -> d.getMontantRecouvre() != null)
                    .mapToDouble(Dossier::getMontantRecouvre)
                    .sum();
            
            double tauxRecouvrement = montantTotalCreances > 0 ? 
                    (montantTotalRecouvre / montantTotalCreances) * 100 : 0.0;
            
            // Répartition par motif (utiliser statut ou autre champ si disponible)
            Map<String, Long> repartitionParMotif = dossiersClotures.stream()
                    .collect(Collectors.groupingBy(
                            d -> d.getDossierStatus() != null ? d.getDossierStatus().name() : "INCONNU",
                            Collectors.counting()
                    ));
            
            // Temps moyen par département
            Map<String, Double> tempsMoyenParDepartement = new HashMap<>();
            for (TypeRecouvrement type : TypeRecouvrement.values()) {
                List<Dossier> dossiersType = dossiersClotures.stream()
                        .filter(d -> d.getTypeRecouvrement() == type &&
                                    d.getDateCloture() != null &&
                                    d.getDateCreation() != null)
                        .collect(Collectors.toList());
                
                if (!dossiersType.isEmpty()) {
                    double tempsMoyen = dossiersType.stream()
                            .mapToDouble(d -> ChronoUnit.DAYS.between(
                                    d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                                    d.getDateCloture().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                            .average()
                            .orElse(0.0);
                    
                    tempsMoyenParDepartement.put(type.name(), Math.round(tempsMoyen * 100.0) / 100.0);
                }
            }
            
            double tempsMoyenGlobal = dossiersClotures.stream()
                    .filter(d -> d.getDateCloture() != null && d.getDateCreation() != null)
                    .mapToDouble(d -> ChronoUnit.DAYS.between(
                            d.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                            d.getDateCloture().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                    .average()
                    .orElse(0.0);
            
            Map<String, Object> response = Map.of(
                    "totalClotures", totalClotures,
                    "variationPeriodePrecedente", Math.round(variation * 100.0) / 100.0,
                    "tauxRecouvrement", Math.round(tauxRecouvrement * 100.0) / 100.0,
                    "montantTotalRecouvre", montantTotalRecouvre,
                    "repartitionParMotif", repartitionParMotif,
                    "tempsMoyen", Map.of(
                            "global", Math.round(tempsMoyenGlobal * 100.0) / 100.0,
                            "parDepartement", tempsMoyenParDepartement
                    )
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/admin/supervision/dossiers/{id}/commentaire-interne
     * Ajoute un commentaire interne sur un dossier
     */
    @PostMapping("/dossiers/{id}/commentaire-interne")
    public ResponseEntity<CommentaireInterne> ajouterCommentaireInterne(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Vérifier que l'ID dossier n'est pas null
            if (id == null) {
                return ResponseEntity.badRequest()
                        .body(null);
            }
            
            // Vérifier que le dossier existe avec findById pour avoir l'entité complète
            Optional<Dossier> dossierOpt = dossierRepository.findById(id);
            if (dossierOpt.isEmpty()) {
                logger.warn("Tentative d'ajout de commentaire sur un dossier inexistant: ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            Dossier dossier = dossierOpt.get();
            
            // Vérifier que le dossier a un ID valide
            if (dossier.getId() == null) {
                logger.error("Erreur : Le dossier n'a pas d'ID valide");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            // Extraire l'utilisateur Superadmin connecté depuis le token
            Utilisateur superadmin = userExtractionService.extractUserFromToken(authHeader);
            if (superadmin == null) {
                logger.warn("Impossible d'extraire l'utilisateur Superadmin depuis le token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Vérifier que le Superadmin a un ID valide
            if (superadmin.getId() == null) {
                logger.error("Erreur : Le Superadmin n'a pas d'ID valide");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            // Vérifier que l'utilisateur a bien le rôle SUPER_ADMIN
            if (superadmin.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN) {
                logger.warn("Utilisateur {} n'a pas le rôle SUPER_ADMIN", superadmin.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            String commentaire = (String) request.get("commentaire");
            if (commentaire == null || commentaire.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Boolean visibleParChef = request.get("visibleParChef") != null ? 
                    (Boolean) request.get("visibleParChef") : true;
            
            CommentaireInterne commentaireInterne = CommentaireInterne.builder()
                    .dossierId(dossier.getId()) // Utiliser l'ID du dossier validé
                    .auteurId(superadmin.getId()) // Utiliser l'ID du Superadmin connecté
                    .commentaire(commentaire)
                    .visibleParChef(visibleParChef)
                    .build();
            
            CommentaireInterne saved = commentaireInterneRepository.save(commentaireInterne);
            
            // Vérifier que la sauvegarde a réussi
            if (saved == null || saved.getId() == null) {
                logger.error("Erreur : La sauvegarde du commentaire a retourné null ou sans ID");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            logger.info("Commentaire interne ajouté par Superadmin {} sur le dossier {}", 
                    superadmin.getId(), dossier.getId());
            
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout du commentaire: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/admin/supervision/dossiers-archives
     * Récupère les dossiers archivés (clôturés depuis plus de 1 an ou avec date de clôture)
     * 
     * @param page Numéro de page (défaut: 0)
     * @param size Taille de la page (défaut: 50, max: 100)
     * @param search Terme de recherche global (numéro, créancier, débiteur)
     * @return Page de dossiers archivés
     */
    @GetMapping("/dossiers-archives")
    public ResponseEntity<?> getDossiersArchives(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @RequestParam(name = "search", required = false) String search) {
        try {
            // Validation des paramètres
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le numéro de page doit être >= 0"));
            }
            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La taille de page doit être entre 1 et 100"));
            }
            
            // Calculer la date limite (1 an avant aujourd'hui)
            java.time.LocalDate dateLimite = java.time.LocalDate.now().minusYears(1);
            
            // Récupérer tous les dossiers clôturés
            List<Dossier> tousDossiers = dossierRepository.findAll();
            
            // Filtrer les dossiers archivés
            List<Dossier> dossiersArchives = tousDossiers.stream()
                    .filter(d -> {
                        // Dossiers avec statut CLOTURE
                        if (d.getDossierStatus() != DossierStatus.CLOTURE) {
                            return false;
                        }
                        
                        // Dossiers clôturés depuis plus de 1 an OU avec date de clôture définie
                        if (d.getDateCloture() != null) {
                            // Convertir Date en LocalDate pour comparaison
                            java.time.LocalDate dateCloture = d.getDateCloture().toInstant()
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate();
                            return dateCloture.isBefore(dateLimite) || dateCloture.isEqual(dateLimite);
                        }
                        
                        // Si pas de date de clôture mais dossier clôturé, considérer comme archivé
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            // Appliquer la recherche globale si fournie
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase().trim();
                dossiersArchives = dossiersArchives.stream()
                        .filter(d -> {
                            // Recherche par numéro de dossier
                            if (d.getNumeroDossier() != null && 
                                d.getNumeroDossier().toLowerCase().contains(searchLower)) {
                                return true;
                            }
                            
                            // Recherche par créancier
                            if (d.getCreancier() != null) {
                                String nomCreancier = (d.getCreancier().getNom() != null ? d.getCreancier().getNom() : "") +
                                                     " " + (d.getCreancier().getPrenom() != null ? d.getCreancier().getPrenom() : "");
                                if (nomCreancier.toLowerCase().contains(searchLower)) {
                                    return true;
                                }
                            }
                            
                            // Recherche par débiteur
                            if (d.getDebiteur() != null) {
                                String nomDebiteur = (d.getDebiteur().getNom() != null ? d.getDebiteur().getNom() : "") +
                                                    " " + (d.getDebiteur().getPrenom() != null ? d.getDebiteur().getPrenom() : "");
                                if (nomDebiteur.toLowerCase().contains(searchLower)) {
                                    return true;
                                }
                            }
                            
                            return false;
                        })
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Trier par date de clôture décroissante (plus récents en premier)
            dossiersArchives.sort((d1, d2) -> {
                if (d1.getDateCloture() == null && d2.getDateCloture() == null) {
                    return 0;
                }
                if (d1.getDateCloture() == null) {
                    return 1;
                }
                if (d2.getDateCloture() == null) {
                    return -1;
                }
                return d2.getDateCloture().compareTo(d1.getDateCloture());
            });
            
            // Appliquer la pagination
            int totalElements = dossiersArchives.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int start = page * size;
            int end = Math.min(start + size, totalElements);
            
            List<Dossier> pageContent = start < totalElements ? 
                    dossiersArchives.subList(start, end) : 
                    new java.util.ArrayList<>();
            
            // Convertir en DTO pour éviter les boucles de référence
            List<projet.carthagecreance_backend.DTO.DossierResponseDTO> content = pageContent.stream()
                    .map(projet.carthagecreance_backend.DTO.DossierResponseDTO::fromEntity)
                    .collect(java.util.stream.Collectors.toList());
            
            // Construire la réponse paginée
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("content", content);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("size", size);
            response.put("number", page);
            response.put("first", page == 0);
            response.put("last", page >= totalPages - 1);
            response.put("numberOfElements", content.size());
            
            logger.info("Récupération de {} dossiers archivés (page {}, taille {})", 
                    totalElements, page, size);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des dossiers archivés: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des dossiers archivés: " + e.getMessage()));
        }
    }
}

