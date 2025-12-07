package projet.carthagecreance_backend.Service.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.FluxFraisDTO;
import projet.carthagecreance_backend.DTO.ValidationFraisDTO;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.Service.FluxFraisService;
import projet.carthagecreance_backend.Service.TarifCatalogueService;

import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

@Service
@Transactional
public class FluxFraisServiceImpl implements FluxFraisService {

    private static final Logger logger = LoggerFactory.getLogger(FluxFraisServiceImpl.class);

    @Autowired
    private FluxFraisRepository fluxFraisRepository;

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private EnquetteRepository enquetteRepository;

    @Autowired
    private AudienceRepository audienceRepository;

    @Autowired
    private AvocatRepository avocatRepository;

    @Autowired
    private HuissierRepository huissierRepository;

    @Autowired
    private TarifCatalogueService tarifCatalogueService;
    
    @Autowired
    private projet.carthagecreance_backend.Service.StatistiqueService statistiqueService;

    @Override
    public FluxFrais createFluxFrais(FluxFraisDTO dto) {
        logger.info("Création d'un flux de frais: phase={}, categorie={}, dossierId={}", 
                dto.getPhase(), dto.getCategorie(), dto.getDossierId());

        Dossier dossier = dossierRepository.findById(dto.getDossierId())
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dto.getDossierId()));

        FluxFrais fluxFrais = FluxFrais.builder()
                .phase(dto.getPhase())
                .categorie(dto.getCategorie())
                .quantite(dto.getQuantite() != null ? dto.getQuantite() : 1)
                .dateAction(dto.getDateAction() != null ? dto.getDateAction() : LocalDate.now())
                .justificatifUrl(dto.getJustificatifUrl())
                .commentaire(dto.getCommentaire())
                .dossier(dossier)
                .statut(StatutFrais.EN_ATTENTE)
                .build();

        // Si tarif unitaire fourni, l'utiliser, sinon chercher dans le catalogue
        if (dto.getTarifUnitaire() != null) {
            fluxFrais.setTarifUnitaire(dto.getTarifUnitaire());
        } else {
            Optional<TarifCatalogue> tarif = tarifCatalogueService.getTarifActifByPhaseAndCategorie(
                    dto.getPhase(), dto.getCategorie(), LocalDate.now());
            if (tarif.isPresent()) {
                fluxFrais.setTarifUnitaire(tarif.get().getTarifUnitaire());
            } else {
                logger.warn("Aucun tarif trouvé pour phase={}, categorie={}", dto.getPhase(), dto.getCategorie());
            }
        }

        // Calculer le montant
        if (fluxFrais.getTarifUnitaire() != null && fluxFrais.getQuantite() != null) {
            fluxFrais.setMontant(fluxFrais.getQuantite() * fluxFrais.getTarifUnitaire());
        }

        // Lier les relations optionnelles
        if (dto.getActionId() != null) {
            Action action = actionRepository.findById(dto.getActionId())
                    .orElseThrow(() -> new RuntimeException("Action non trouvée avec l'ID: " + dto.getActionId()));
            fluxFrais.setAction(action);
        }

        if (dto.getEnqueteId() != null) {
            Enquette enquete = enquetteRepository.findById(dto.getEnqueteId())
                    .orElseThrow(() -> new RuntimeException("Enquête non trouvée avec l'ID: " + dto.getEnqueteId()));
            fluxFrais.setEnquette(enquete);
        }

        if (dto.getAudienceId() != null) {
            Audience audience = audienceRepository.findById(dto.getAudienceId())
                    .orElseThrow(() -> new RuntimeException("Audience non trouvée avec l'ID: " + dto.getAudienceId()));
            fluxFrais.setAudience(audience);
        }

        if (dto.getAvocatId() != null) {
            Avocat avocat = avocatRepository.findById(dto.getAvocatId())
                    .orElseThrow(() -> new RuntimeException("Avocat non trouvé avec l'ID: " + dto.getAvocatId()));
            fluxFrais.setAvocat(avocat);
        }

        if (dto.getHuissierId() != null) {
            Huissier huissier = huissierRepository.findById(dto.getHuissierId())
                    .orElseThrow(() -> new RuntimeException("Huissier non trouvé avec l'ID: " + dto.getHuissierId()));
            fluxFrais.setHuissier(huissier);
        }

        return fluxFraisRepository.save(fluxFrais);
    }

    @Override
    public Optional<FluxFrais> getFluxFraisById(Long id) {
        return fluxFraisRepository.findById(id);
    }

    @Override
    public List<FluxFrais> getAllFluxFrais() {
        return fluxFraisRepository.findAll();
    }

    @Override
    public List<FluxFrais> getFluxFraisByDossier(Long dossierId) {
        return fluxFraisRepository.findByDossierId(dossierId);
    }

    @Override
    public List<FluxFrais> getFluxFraisByStatut(StatutFrais statut) {
        return fluxFraisRepository.findByStatut(statut);
    }

    @Override
    public List<FluxFrais> getFluxFraisEnAttente() {
        return fluxFraisRepository.findByStatut(StatutFrais.EN_ATTENTE);
    }

    @Override
    public List<FluxFrais> getFluxFraisByPhase(PhaseFrais phase) {
        return fluxFraisRepository.findByPhase(phase);
    }

    @Override
    public List<FluxFrais> getFluxFraisByDateRange(LocalDate startDate, LocalDate endDate) {
        return fluxFraisRepository.findByDateActionBetween(startDate, endDate);
    }

    @Override
    public FluxFrais updateFluxFrais(Long id, FluxFraisDTO dto) {
        logger.info("Mise à jour du flux de frais ID: {}", id);
        
        FluxFrais fluxFrais = fluxFraisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flux de frais non trouvé avec l'ID: " + id));

        fluxFrais.setPhase(dto.getPhase());
        fluxFrais.setCategorie(dto.getCategorie());
        fluxFrais.setQuantite(dto.getQuantite());
        fluxFrais.setTarifUnitaire(dto.getTarifUnitaire());
        fluxFrais.setDateAction(dto.getDateAction());
        fluxFrais.setJustificatifUrl(dto.getJustificatifUrl());
        fluxFrais.setCommentaire(dto.getCommentaire());

        // Recalculer le montant
        if (fluxFrais.getTarifUnitaire() != null && fluxFrais.getQuantite() != null) {
            fluxFrais.setMontant(fluxFrais.getQuantite() * fluxFrais.getTarifUnitaire());
        }

        return fluxFraisRepository.save(fluxFrais);
    }

    @Override
    public void deleteFluxFrais(Long id) {
        logger.info("Suppression du flux de frais ID: {}", id);
        fluxFraisRepository.deleteById(id);
    }

    @Override
    public FluxFrais validerFrais(Long id, ValidationFraisDTO dto) {
        logger.info("Validation du flux de frais ID: {}", id);
        
        FluxFrais fluxFrais = fluxFraisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flux de frais non trouvé avec l'ID: " + id));

        fluxFrais.setStatut(StatutFrais.VALIDE);
        if (dto.getCommentaire() != null) {
            fluxFrais.setCommentaire(dto.getCommentaire());
        }

        FluxFrais savedFluxFrais = fluxFraisRepository.save(fluxFrais);
        
        // Recalcul automatique des statistiques (asynchrone)
        try {
            statistiqueService.recalculerStatistiquesAsync();
        } catch (Exception e) {
            logger.warn("Erreur lors du recalcul automatique des statistiques après validation de frais: {}", e.getMessage());
        }
        
        return savedFluxFrais;
    }

    @Override
    public FluxFrais rejeterFrais(Long id, ValidationFraisDTO dto) {
        logger.info("Rejet du flux de frais ID: {}", id);
        
        FluxFrais fluxFrais = fluxFraisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flux de frais non trouvé avec l'ID: " + id));

        if (dto.getMotif() == null || dto.getMotif().trim().isEmpty()) {
            throw new RuntimeException("Le motif est obligatoire pour rejeter un frais");
        }

        fluxFrais.setStatut(StatutFrais.REJETE);
        fluxFrais.setCommentaire(dto.getMotif());

        return fluxFraisRepository.save(fluxFrais);
    }

    @Override
    public FluxFrais creerFraisDepuisAction(Long actionId) {
        logger.info("Création automatique de frais depuis action ID: {}", actionId);
        
        Action action = actionRepository.findById(actionId)
                .orElseThrow(() -> new RuntimeException("Action non trouvée avec l'ID: " + actionId));

        // Déterminer la phase selon le type de recouvrement du dossier
        PhaseFrais phase = action.getDossier().getTypeRecouvrement() == TypeRecouvrement.AMIABLE 
                ? PhaseFrais.AMIABLE 
                : PhaseFrais.JURIDIQUE;

        // Déterminer la catégorie selon le type d'action
        String categorie = mapTypeActionToCategorie(action.getType());

        FluxFraisDTO dto = FluxFraisDTO.builder()
                .phase(phase)
                .categorie(categorie)
                .quantite(action.getNbOccurrences())
                .dossierId(action.getDossier().getId())
                .actionId(actionId)
                .dateAction(action.getDateAction())
                .build();

        return createFluxFrais(dto);
    }

    @Override
    public FluxFrais creerFraisDepuisEnquete(Long enqueteId) {
        logger.info("Création automatique de frais depuis enquête ID: {}", enqueteId);
        
        Enquette enquete = enquetteRepository.findById(enqueteId)
                .orElseThrow(() -> new RuntimeException("Enquête non trouvée avec l'ID: " + enqueteId));

        FluxFraisDTO dto = FluxFraisDTO.builder()
                .phase(PhaseFrais.ENQUETE)
                .categorie("ENQUETE")
                .quantite(1)
                .dossierId(enquete.getDossier().getId())
                .enqueteId(enqueteId)
                .dateAction(LocalDate.now())
                .build();

        return createFluxFrais(dto);
    }

    @Override
    public FluxFrais creerFraisDepuisAudience(Long audienceId) {
        logger.info("Création automatique de frais depuis audience ID: {}", audienceId);
        
        Audience audience = audienceRepository.findById(audienceId)
                .orElseThrow(() -> new RuntimeException("Audience non trouvée avec l'ID: " + audienceId));

        FluxFraisDTO dto = FluxFraisDTO.builder()
                .phase(PhaseFrais.JURIDIQUE)
                .categorie("AUDIENCE")
                .quantite(1)
                .dossierId(audience.getDossier().getId())
                .audienceId(audienceId)
                .dateAction(audience.getDateAudience())
                .build();

        if (audience.getAvocat() != null) {
            dto.setAvocatId(audience.getAvocat().getId());
        }
        if (audience.getHuissier() != null) {
            dto.setHuissierId(audience.getHuissier().getId());
        }

        return createFluxFrais(dto);
    }

    @Override
    public Double calculerTotalFraisByDossier(Long dossierId) {
        Double total = fluxFraisRepository.calculerTotalFraisByDossier(dossierId);
        return total != null ? total : 0.0;
    }

    @Override
    public Double calculerTotalFraisByStatut(StatutFrais statut) {
        Double total = fluxFraisRepository.calculerTotalFraisByStatut(statut);
        return total != null ? total : 0.0;
    }

    @Override
    public Map<String, Object> importerFraisDepuisCSV(byte[] csvContent) {
        logger.info("Import CSV des frais, taille: {} bytes", csvContent.length);
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> succes = new ArrayList<>();
        List<Map<String, Object>> erreurs = new ArrayList<>();
        int ligneNum = 0;
        
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(csvContent), "UTF-8"));
            List<String[]> lignes = reader.readAll();
            
            // Ignorer l'en-tête si présent
            int startIndex = 0;
            if (lignes.size() > 0 && (lignes.get(0)[0].toLowerCase().contains("dossier") || 
                                      lignes.get(0)[0].toLowerCase().contains("phase"))) {
                startIndex = 1;
            }
            
            for (int i = startIndex; i < lignes.size(); i++) {
                ligneNum = i + 1;
                String[] ligne = lignes.get(i);
                
                try {
                    // Format attendu: dossier_id, phase, categorie, quantite, tarif_unitaire, fournisseur, date_action
                    if (ligne.length < 4) {
                        erreurs.add(creerErreur(ligneNum, "Ligne incomplète (minimum 4 colonnes requises)"));
                        continue;
                    }
                    
                    Long dossierId = Long.parseLong(ligne[0].trim());
                    PhaseFrais phase = PhaseFrais.valueOf(ligne[1].trim().toUpperCase());
                    String categorie = ligne[2].trim();
                    Integer quantite = ligne.length > 3 ? Integer.parseInt(ligne[3].trim()) : 1;
                    Double tarifUnitaire = ligne.length > 4 && !ligne[4].trim().isEmpty() 
                            ? Double.parseDouble(ligne[4].trim()) : null;
                    String fournisseur = ligne.length > 5 ? ligne[5].trim() : null;
                    LocalDate dateAction = ligne.length > 6 && !ligne[6].trim().isEmpty()
                            ? LocalDate.parse(ligne[6].trim(), DateTimeFormatter.ISO_DATE)
                            : LocalDate.now();
                    
                    // Vérifier que le dossier existe
                    if (!dossierRepository.existsById(dossierId)) {
                        erreurs.add(creerErreur(ligneNum, "Dossier ID " + dossierId + " non trouvé"));
                        continue;
                    }
                    
                    // Créer le flux de frais
                    FluxFraisDTO dto = FluxFraisDTO.builder()
                            .dossierId(dossierId)
                            .phase(phase)
                            .categorie(categorie)
                            .quantite(quantite)
                            .tarifUnitaire(tarifUnitaire)
                            .dateAction(dateAction)
                            .commentaire(fournisseur != null ? "Import CSV - Fournisseur: " + fournisseur : "Import CSV")
                            .build();
                    
                    FluxFrais frais = createFluxFrais(dto);
                    succes.add(Map.of(
                            "ligne", ligneNum,
                            "id", frais.getId(),
                            "dossierId", dossierId,
                            "montant", frais.getMontant() != null ? frais.getMontant() : 0.0
                    ));
                    
                } catch (Exception e) {
                    erreurs.add(creerErreur(ligneNum, e.getMessage()));
                }
            }
            
            reader.close();
            
        } catch (Exception e) {
            logger.error("Erreur lors de la lecture du CSV: {}", e.getMessage(), e);
            erreurs.add(creerErreur(0, "Erreur de lecture du fichier: " + e.getMessage()));
        }
        
        result.put("success", succes.size());
        result.put("errors", erreurs.size());
        result.put("succes", succes);
        result.put("erreurs", erreurs);
        
        logger.info("Import CSV terminé: {} succès, {} erreurs", succes.size(), erreurs.size());
        return result;
    }
    
    private Map<String, Object> creerErreur(int ligne, String message) {
        Map<String, Object> erreur = new HashMap<>();
        erreur.put("ligne", ligne);
        erreur.put("message", message);
        return erreur;
    }
    
    private String mapTypeActionToCategorie(TypeAction typeAction) {
        return switch (typeAction) {
            case APPEL -> "APPEL";
            case EMAIL -> "EMAIL";
            case VISITE -> "VISITE";
            case LETTRE -> "LETTRE";
            case AUTRE -> "AUTRE";
        };
    }
}

