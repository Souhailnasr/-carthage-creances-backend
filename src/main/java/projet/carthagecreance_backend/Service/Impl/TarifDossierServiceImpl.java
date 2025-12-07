package projet.carthagecreance_backend.Service.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.*;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.Service.TarifDossierService;
import projet.carthagecreance_backend.Service.FactureService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des tarifs de dossier
 */
@Service
@Transactional
public class TarifDossierServiceImpl implements TarifDossierService {
    
    private static final Logger logger = LoggerFactory.getLogger(TarifDossierServiceImpl.class);
    
    @Autowired
    private TarifDossierRepository tarifDossierRepository;
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private EnquetteRepository enquetteRepository;
    
    @Autowired
    private ActionRepository actionRepository;
    
    @Autowired
    private ActionHuissierRepository actionHuissierRepository;
    
    @Autowired
    private DocumentHuissierRepository documentHuissierRepository;
    
    @Autowired
    private AudienceRepository audienceRepository;
    
    @Autowired
    private FinanceRepository financeRepository;
    
    @Autowired
    private AvocatRepository avocatRepository;
    
    @Autowired
    private FactureService factureService;
    
    // Constantes pour les frais fixes selon l'annexe
    private static final BigDecimal FRAIS_CREATION_DOSSIER = new BigDecimal("250.00");
    private static final BigDecimal FRAIS_ENQUETE_PRECONTENTIEUSE = new BigDecimal("300.00");
    private static final BigDecimal AVANCE_RECOUVREMENT_JURIDIQUE = new BigDecimal("1000.00");
    private static final BigDecimal ATTESTATION_CARENCE = new BigDecimal("500.00");
    
    // Taux de commission selon annexe
    private static final BigDecimal TAUX_COMMISSION_RELANCE = new BigDecimal("0.05");  // 5%
    private static final BigDecimal TAUX_COMMISSION_AMIABLE = new BigDecimal("0.12");  // 12%
    private static final BigDecimal TAUX_COMMISSION_JURIDIQUE = new BigDecimal("0.15");  // 15%
    private static final BigDecimal TAUX_COMMISSION_INTERETS = new BigDecimal("0.50");  // 50%
    
    @Override
    public TraitementsDossierDTO getTraitementsDossier(Long dossierId) {
        Dossier dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        TraitementsDossierDTO dto = new TraitementsDossierDTO();
        dto.setDossierId(dossierId);
        
        // Phase CREATION
        PhaseCreationDTO phaseCreation = buildPhaseCreation(dossier);
        dto.setPhaseCreation(phaseCreation);
        
        // Phase ENQUETE
        Optional<Enquette> enqueteOpt = enquetteRepository.findByDossierId(dossierId);
        if (enqueteOpt.isPresent()) {
            PhaseEnqueteDTO phaseEnquete = buildPhaseEnquete(dossier, enqueteOpt.get());
            dto.setPhaseEnquete(phaseEnquete);
        }
        
        // Phase AMIABLE
        PhaseAmiableDTO phaseAmiable = buildPhaseAmiable(dossierId);
        dto.setPhaseAmiable(phaseAmiable);
        
        // Phase JURIDIQUE
        PhaseJuridiqueDTO phaseJuridique = buildPhaseJuridique(dossierId);
        dto.setPhaseJuridique(phaseJuridique);
        
        return dto;
    }
    
    private PhaseCreationDTO buildPhaseCreation(Dossier dossier) {
        PhaseCreationDTO phaseCreation = new PhaseCreationDTO();
        
        TraitementDTO traitementCreation = new TraitementDTO();
        traitementCreation.setType("OUVERTURE_DOSSIER");
        traitementCreation.setDate(convertToLocalDate(dossier.getDateCreation()));
        traitementCreation.setFraisFixe(FRAIS_CREATION_DOSSIER);
        
        Optional<TarifDossier> tarifCreation = tarifDossierRepository
            .findByDossierIdAndPhaseAndCategorie(dossier.getId(), PhaseFrais.CREATION, "OUVERTURE_DOSSIER");
        
        if (tarifCreation.isPresent()) {
            traitementCreation.setTarifExistant(mapToTarifDTO(tarifCreation.get()));
            traitementCreation.setStatut(tarifCreation.get().getStatut().name());
        } else {
            // Créer automatiquement le tarif avec statut VALIDE
            TarifDossier nouveauTarif = createTarifCreationAutomatique(dossier);
            traitementCreation.setTarifExistant(mapToTarifDTO(nouveauTarif));
            traitementCreation.setStatut(StatutTarif.VALIDE.name());
        }
        
        phaseCreation.setTraitements(List.of(traitementCreation));
        return phaseCreation;
    }
    
    @Override
    public TarifDossier createTarifCreationAutomatique(Dossier dossier) {
        TarifDossier tarif = TarifDossier.builder()
            .dossier(dossier)
            .phase(PhaseFrais.CREATION)
            .categorie("OUVERTURE_DOSSIER")
            .typeElement("Ouverture de dossier")
            .coutUnitaire(FRAIS_CREATION_DOSSIER)
            .quantite(1)
            .montantTotal(FRAIS_CREATION_DOSSIER)
            .statut(StatutTarif.VALIDE)
            .dateCreation(LocalDateTime.now())
            .dateValidation(LocalDateTime.now())
            .commentaire("Frais fixe selon annexe - Validation automatique")
            .build();
        
        return tarifDossierRepository.save(tarif);
    }
    
    private PhaseEnqueteDTO buildPhaseEnquete(Dossier dossier, Enquette enquete) {
        PhaseEnqueteDTO phaseEnquete = new PhaseEnqueteDTO();
        
        // Enquête précontentieuse (obligatoire)
        TraitementDTO enquetePrecontentieuse = new TraitementDTO();
        enquetePrecontentieuse.setType("ENQUETE_PRECONTENTIEUSE");
        enquetePrecontentieuse.setDate(enquete.getDateCreation() != null ? enquete.getDateCreation() : LocalDate.now());
        enquetePrecontentieuse.setFraisFixe(FRAIS_ENQUETE_PRECONTENTIEUSE);
        
        Optional<TarifDossier> tarifEnquete = tarifDossierRepository
            .findByDossierIdAndPhaseAndCategorie(dossier.getId(), PhaseFrais.ENQUETE, "ENQUETE_PRECONTENTIEUSE");
        
        if (tarifEnquete.isPresent()) {
            enquetePrecontentieuse.setTarifExistant(mapToTarifDTO(tarifEnquete.get()));
            enquetePrecontentieuse.setStatut(tarifEnquete.get().getStatut().name());
        } else {
            // Créer automatiquement le tarif avec statut VALIDE
            TarifDossier nouveauTarif = createTarifEnqueteAutomatique(dossier, enquete);
            enquetePrecontentieuse.setTarifExistant(mapToTarifDTO(nouveauTarif));
            enquetePrecontentieuse.setStatut(StatutTarif.VALIDE.name());
        }
        
        phaseEnquete.setEnquetePrecontentieuse(enquetePrecontentieuse);
        
        // Traitements possibles (optionnels)
        List<TraitementPossibleDTO> traitementsPossibles = Arrays.asList(
            createTraitementPossible("EXPERTISE", "Expertise", dossier.getId()),
            createTraitementPossible("DEPLACEMENT", "Déplacement", dossier.getId()),
            createTraitementPossible("AUTRES", "Autres traitements", dossier.getId())
        );
        phaseEnquete.setTraitementsPossibles(traitementsPossibles);
        
        return phaseEnquete;
    }
    
    /**
     * Crée automatiquement le tarif d'enquête lors de la validation
     */
    @Override
    public TarifDossier createTarifEnqueteAutomatique(Dossier dossier, Enquette enquete) {
        // Vérifier si un tarif existe déjà
        Optional<TarifDossier> existing = tarifDossierRepository
            .findByDossierIdAndPhaseAndCategorie(dossier.getId(), PhaseFrais.ENQUETE, "ENQUETE_PRECONTENTIEUSE");
        if (existing.isPresent()) {
            return existing.get();
        }
        
        TarifDossier tarif = TarifDossier.builder()
            .dossier(dossier)
            .enquete(enquete)
            .phase(PhaseFrais.ENQUETE)
            .categorie("ENQUETE_PRECONTENTIEUSE")
            .typeElement("Enquête Précontentieuse")
            .coutUnitaire(FRAIS_ENQUETE_PRECONTENTIEUSE)
            .quantite(1)
            .montantTotal(FRAIS_ENQUETE_PRECONTENTIEUSE)
            .statut(StatutTarif.VALIDE)
            .dateCreation(LocalDateTime.now())
            .dateValidation(LocalDateTime.now())
            .commentaire("Frais fixe selon annexe - Validation automatique")
            .enquete(enquete)
            .build();
        
        return tarifDossierRepository.save(tarif);
    }
    
    private TraitementPossibleDTO createTraitementPossible(String type, String libelle, Long dossierId) {
        TraitementPossibleDTO dto = new TraitementPossibleDTO();
        dto.setType(type);
        dto.setLibelle(libelle);
        
        Optional<TarifDossier> tarif = tarifDossierRepository
            .findByDossierIdAndPhaseAndCategorie(dossierId, PhaseFrais.ENQUETE, type);
        
        if (tarif.isPresent()) {
            dto.setTarifExistant(mapToTarifDTO(tarif.get()));
            dto.setStatut(tarif.get().getStatut().name());
        } else {
            dto.setStatut("EN_ATTENTE_TARIF");
        }
        
        return dto;
    }
    
    private PhaseAmiableDTO buildPhaseAmiable(Long dossierId) {
        PhaseAmiableDTO phaseAmiable = new PhaseAmiableDTO();
        
        List<Action> actions = actionRepository.findByDossierId(dossierId);
        List<ActionAmiableTraitementDTO> actionsDTO = actions.stream()
            .filter(action -> action.getDossier() != null && 
                    action.getDossier().getTypeRecouvrement() == TypeRecouvrement.AMIABLE)
            .map(action -> {
                ActionAmiableTraitementDTO dto = new ActionAmiableTraitementDTO();
                dto.setId(action.getId());
                dto.setType(action.getType() != null ? action.getType().name() : null);
                dto.setDate(action.getDateAction());
                dto.setOccurrences(action.getNbOccurrences());
                
                Optional<TarifDossier> tarif = tarifDossierRepository
                    .findByDossierIdAndActionId(dossierId, action.getId());
                
                if (tarif.isPresent()) {
                    // Priorité 1 : Coût du tarif
                    dto.setCoutUnitaire(tarif.get().getCoutUnitaire());
                    dto.setTarifExistant(mapToTarifDTO(tarif.get()));
                    dto.setStatut(tarif.get().getStatut().name());
                } else if (action.getCoutUnitaire() != null && action.getCoutUnitaire() > 0) {
                    // Priorité 2 : Coût de l'action (saisi lors de la création)
                    dto.setCoutUnitaire(BigDecimal.valueOf(action.getCoutUnitaire()));
                    dto.setStatut("EN_ATTENTE_TARIF");
                } else {
                    dto.setStatut("EN_ATTENTE_TARIF");
                }
                
                return dto;
            })
            .collect(Collectors.toList());
        
        phaseAmiable.setActions(actionsDTO);
        return phaseAmiable;
    }
    
    private PhaseJuridiqueDTO buildPhaseJuridique(Long dossierId) {
        PhaseJuridiqueDTO phaseJuridique = new PhaseJuridiqueDTO();
        
        // Documents Huissier
        List<DocumentHuissier> documents = documentHuissierRepository.findByDossierId(dossierId);
        List<DocumentHuissierTraitementDTO> documentsDTO = documents.stream().map(doc -> {
            DocumentHuissierTraitementDTO dto = new DocumentHuissierTraitementDTO();
            dto.setId(doc.getId());
            dto.setType(doc.getTypeDocument() != null ? doc.getTypeDocument().name() : null);
            dto.setDate(doc.getDateCreation());
            
            Optional<TarifDossier> tarif = tarifDossierRepository
                .findByDossierIdAndDocumentHuissierId(dossierId, doc.getId());
            
            if (tarif.isPresent()) {
                dto.setCoutUnitaire(tarif.get().getCoutUnitaire());
                dto.setTarifExistant(mapToTarifDTO(tarif.get()));
                dto.setStatut(tarif.get().getStatut().name());
            } else {
                dto.setStatut("EN_ATTENTE_TARIF");
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        // Actions Huissier
        List<ActionHuissier> actionsHuissier = actionHuissierRepository.findByDossierId(dossierId);
        List<ActionHuissierTraitementDTO> actionsHuissierDTO = actionsHuissier.stream().map(action -> {
            ActionHuissierTraitementDTO dto = new ActionHuissierTraitementDTO();
            dto.setId(action.getId());
            dto.setType(action.getTypeAction() != null ? action.getTypeAction().name() : null);
            dto.setDate(action.getDateAction());
            
            Optional<TarifDossier> tarif = tarifDossierRepository
                .findByDossierIdAndActionHuissierId(dossierId, action.getId());
            
            if (tarif.isPresent()) {
                dto.setCoutUnitaire(tarif.get().getCoutUnitaire());
                dto.setTarifExistant(mapToTarifDTO(tarif.get()));
                dto.setStatut(tarif.get().getStatut().name());
            } else {
                dto.setStatut("EN_ATTENTE_TARIF");
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        // Audiences
        List<Audience> audiences = audienceRepository.findByDossierId(dossierId);
        List<AudienceTraitementDTO> audiencesDTO = audiences.stream().map(audience -> {
            AudienceTraitementDTO dto = new AudienceTraitementDTO();
            dto.setId(audience.getId());
            dto.setDate(audience.getDateAudience());
            dto.setType(audience.getTribunalType() != null ? audience.getTribunalType().name() : null);
            
            if (audience.getAvocat() != null) {
                dto.setAvocatId(audience.getAvocat().getId());
                dto.setAvocatNom(audience.getAvocat().getNom());
            }
            
            // Tarif pour l'audience
            Optional<TarifDossier> tarifAudience = tarifDossierRepository
                .findByDossierIdAndAudienceId(dossierId, audience.getId());
            
            if (tarifAudience.isPresent()) {
                dto.setCoutAudience(tarifAudience.get().getCoutUnitaire());
                dto.setTarifAudience(mapToTarifDTO(tarifAudience.get()));
            }
            
            // Tarif pour l'avocat (si présent)
            if (audience.getAvocat() != null) {
                // Chercher un tarif avec catégorie "AVOCAT" pour cette audience
                List<TarifDossier> tarifsAvocat = tarifDossierRepository.findByDossierId(dossierId).stream()
                    .filter(t -> t.getPhase() == PhaseFrais.JURIDIQUE && 
                            t.getCategorie().contains("AVOCAT") &&
                            t.getAudience() != null && t.getAudience().getId().equals(audience.getId()))
                    .collect(Collectors.toList());
                
                if (!tarifsAvocat.isEmpty()) {
                    TarifDossier tarifAvocat = tarifsAvocat.get(0);
                    dto.setCoutAvocat(tarifAvocat.getCoutUnitaire());
                    dto.setTarifAvocat(mapToTarifDTO(tarifAvocat));
                }
            }
            
            // Déterminer le statut global
            if (tarifAudience.isPresent()) {
                dto.setStatut(tarifAudience.get().getStatut().name());
            } else {
                dto.setStatut("EN_ATTENTE_TARIF");
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        phaseJuridique.setDocumentsHuissier(documentsDTO);
        phaseJuridique.setActionsHuissier(actionsHuissierDTO);
        phaseJuridique.setAudiences(audiencesDTO);
        
        return phaseJuridique;
    }
    
    @Override
    public TarifDossierDTO createTarif(Long dossierId, TarifDossierRequest request) {
        Dossier dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        TarifDossier tarif = TarifDossier.builder()
            .dossier(dossier)
            .phase(request.getPhase())
            .categorie(request.getCategorie())
            .typeElement(request.getTypeElement())
            .coutUnitaire(request.getCoutUnitaire())
            .quantite(request.getQuantite() != null ? request.getQuantite() : 1)
            .statut(StatutTarif.EN_ATTENTE_VALIDATION)
            .dateCreation(LocalDateTime.now())
            .commentaire(request.getCommentaire())
            .build();
        
        // Gérer avocatId si fourni (pour honoraires d'avocat)
        Long audienceIdFinal = request.getAudienceId();
        if (request.getAvocatId() != null && request.getCategorie() != null && 
            request.getCategorie().toUpperCase().contains("AVOCAT")) {
            
            // Si audienceId est aussi fourni, le prioriser (plus explicite)
            if (request.getAudienceId() == null) {
                // Trouver l'audience associée à cet avocat pour ce dossier
                List<Audience> audiences = audienceRepository.findByDossierId(dossierId).stream()
                    .filter(a -> a.getAvocat() != null && a.getAvocat().getId().equals(request.getAvocatId()))
                    .sorted(Comparator.comparing(Audience::getDateAudience, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
                
                if (audiences.isEmpty()) {
                    throw new RuntimeException("Aucune audience trouvée pour l'avocat " + request.getAvocatId() + 
                                            " dans le dossier " + dossierId);
                }
                
                // Utiliser l'audience la plus récente
                Audience audience = audiences.get(0);
                audienceIdFinal = audience.getId();
                logger.info("Mapping avocatId {} vers audienceId {} pour le dossier {}", 
                    request.getAvocatId(), audienceIdFinal, dossierId);
            }
        }
        
        // Lier aux traitements spécifiques si fournis
        if (request.getActionId() != null) {
            Action action = actionRepository.findById(request.getActionId())
                .orElseThrow(() -> new RuntimeException("Action non trouvée"));
            
            // ✅ VÉRIFIER SI UN TARIF EXISTE DÉJÀ POUR CETTE ACTION
            Optional<TarifDossier> existingTarif = tarifDossierRepository
                .findByDossierIdAndActionId(dossierId, request.getActionId());
            
            if (existingTarif.isPresent()) {
                // ✅ TARIF EXISTE : Mettre à jour le coût si différent, puis valider
                TarifDossier tarifExistant = existingTarif.get();
                
                logger.info("Tarif existant trouvé pour action {} dans dossier {}: ID={}, statut={}", 
                    request.getActionId(), dossierId, tarifExistant.getId(), tarifExistant.getStatut());
                
                // Mettre à jour le coût unitaire si différent
                boolean coutModifie = false;
                if (request.getCoutUnitaire() != null && 
                    !request.getCoutUnitaire().equals(tarifExistant.getCoutUnitaire())) {
                    tarifExistant.setCoutUnitaire(request.getCoutUnitaire());
                    coutModifie = true;
                    logger.info("Mise à jour du coût unitaire: {} -> {}", 
                        tarifExistant.getCoutUnitaire(), request.getCoutUnitaire());
                }
                
                // Mettre à jour la quantité si fournie et différente
                if (request.getQuantite() != null && 
                    !request.getQuantite().equals(tarifExistant.getQuantite())) {
                    tarifExistant.setQuantite(request.getQuantite());
                    coutModifie = true;
                }
                
                // Recalculer le montant total si le coût ou la quantité a changé
                if (coutModifie) {
                    BigDecimal nouveauMontant = tarifExistant.getCoutUnitaire()
                        .multiply(BigDecimal.valueOf(tarifExistant.getQuantite()));
                    tarifExistant.setMontantTotal(nouveauMontant);
                }
                
                // Mettre à jour le commentaire si fourni
                if (request.getCommentaire() != null && !request.getCommentaire().trim().isEmpty()) {
                    tarifExistant.setCommentaire(request.getCommentaire());
                }
                
                // ✅ NE PAS VALIDER AUTOMATIQUEMENT : Garder le statut actuel ou EN_ATTENTE_VALIDATION
                // Si le tarif était déjà validé, on le garde validé
                // Si le tarif était en attente, on le garde en attente (validation manuelle requise)
                if (tarifExistant.getStatut() == null || tarifExistant.getStatut() == StatutTarif.EN_ATTENTE_VALIDATION) {
                    tarifExistant.setStatut(StatutTarif.EN_ATTENTE_VALIDATION);
                    tarifExistant.setDateValidation(null); // Pas de date de validation si pas encore validé
                }
                // Si déjà VALIDE, on garde VALIDE (pas de changement)
                
                TarifDossier saved = tarifDossierRepository.save(tarifExistant);
                logger.info("Tarif existant mis à jour (validation manuelle requise): ID={}, Dossier={}, Action={}, Statut={}, Montant={}", 
                    saved.getId(), dossierId, request.getActionId(), saved.getStatut(), saved.getMontantTotal());
                
                return mapToTarifDTO(saved);
            } else {
                // ✅ TARIF N'EXISTE PAS : Créer avec statut EN_ATTENTE_VALIDATION (validation manuelle requise)
                tarif.setAction(action);
                tarif.setStatut(StatutTarif.EN_ATTENTE_VALIDATION); // ✅ Validation manuelle requise
                tarif.setDateValidation(null); // Pas de date de validation si pas encore validé
                logger.info("Nouveau tarif créé (validation manuelle requise) pour action {} dans dossier {}", 
                    request.getActionId(), dossierId);
            }
        }
        
        if (request.getDocumentHuissierId() != null) {
            DocumentHuissier doc = documentHuissierRepository.findById(request.getDocumentHuissierId())
                .orElseThrow(() -> new RuntimeException("Document huissier non trouvé"));
            tarif.setDocumentHuissier(doc);
        }
        
        if (request.getActionHuissierId() != null) {
            ActionHuissier actionHuissier = actionHuissierRepository.findById(request.getActionHuissierId())
                .orElseThrow(() -> new RuntimeException("Action huissier non trouvée"));
            tarif.setActionHuissier(actionHuissier);
        }
        
        if (audienceIdFinal != null) {
            Audience audience = audienceRepository.findById(audienceIdFinal)
                .orElseThrow(() -> new RuntimeException("Audience non trouvée"));
            tarif.setAudience(audience);
            
            // Vérifier l'unicité (audienceId + categorie)
            if (request.getCategorie() != null) {
                Optional<TarifDossier> existing = tarifDossierRepository
                    .findByDossierIdAndAudienceIdAndCategorie(dossierId, audienceIdFinal, request.getCategorie());
                
                if (existing.isPresent()) {
                    throw new RuntimeException("Un tarif existe déjà pour cette audience (" + audienceIdFinal + 
                                            ") avec la catégorie (" + request.getCategorie() + ")");
                }
            }
        }
        
        if (request.getEnqueteId() != null) {
            Enquette enquete = enquetteRepository.findById(request.getEnqueteId())
                .orElseThrow(() -> new RuntimeException("Enquête non trouvée"));
            tarif.setEnquete(enquete);
        }
        
        // Calculer montantTotal
        tarif.setMontantTotal(tarif.getCoutUnitaire().multiply(BigDecimal.valueOf(tarif.getQuantite())));
        
        // ✅ Si c'est un tarif d'action amiable et qu'il n'existait pas, il a déjà été validé automatiquement
        // Sinon, pour les autres types (audience, document, etc.), garder EN_ATTENTE_VALIDATION
        
        TarifDossier saved = tarifDossierRepository.save(tarif);
        
        // Note: updateStatutValidationTarifs() sera appelé lors de la validation manuelle
        // Pas besoin de l'appeler ici car le tarif est en EN_ATTENTE_VALIDATION
        
        logger.info("Tarif créé avec succès: ID={}, Dossier={}, Phase={}, Catégorie={}, Statut={}", 
            saved.getId(), dossierId, request.getPhase(), request.getCategorie(), saved.getStatut());
        
        return mapToTarifDTO(saved);
    }
    
    @Override
    public TarifDossierDTO validerTarif(Long tarifId, String commentaire) {
        TarifDossier tarif = tarifDossierRepository.findById(tarifId)
            .orElseThrow(() -> new RuntimeException("Tarif non trouvé avec l'ID: " + tarifId));
        
        if (tarif.getStatut() != StatutTarif.EN_ATTENTE_VALIDATION) {
            throw new RuntimeException("Le tarif n'est pas en attente de validation");
        }
        
        tarif.setStatut(StatutTarif.VALIDE);
        tarif.setDateValidation(LocalDateTime.now());
        if (commentaire != null && !commentaire.isEmpty()) {
            tarif.setCommentaire(commentaire);
        }
        
        TarifDossier saved = tarifDossierRepository.save(tarif);
        
        // Mettre à jour le statut de validation des tarifs du Finance
        updateStatutValidationTarifs(tarif.getDossier().getId());
        
        logger.info("Tarif validé: ID={}, Dossier={}", tarifId, tarif.getDossier().getId());
        
        return mapToTarifDTO(saved);
    }
    
    @Override
    public TarifDossierDTO rejeterTarif(Long tarifId, String commentaire) {
        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new RuntimeException("Un commentaire est obligatoire pour rejeter un tarif");
        }
        
        TarifDossier tarif = tarifDossierRepository.findById(tarifId)
            .orElseThrow(() -> new RuntimeException("Tarif non trouvé avec l'ID: " + tarifId));
        
        if (tarif.getStatut() != StatutTarif.EN_ATTENTE_VALIDATION) {
            throw new RuntimeException("Le tarif n'est pas en attente de validation");
        }
        
        tarif.setStatut(StatutTarif.REJETE);
        tarif.setDateValidation(LocalDateTime.now());
        tarif.setCommentaire(commentaire);
        
        TarifDossier saved = tarifDossierRepository.save(tarif);
        
        logger.info("Tarif rejeté: ID={}, Dossier={}, Commentaire={}", tarifId, tarif.getDossier().getId(), commentaire);
        
        return mapToTarifDTO(saved);
    }
    
    @Override
    public ValidationEtatDTO getValidationEtat(Long dossierId) {
        Dossier dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        ValidationEtatDTO dto = new ValidationEtatDTO();
        dto.setDossierId(dossierId);
        
        Map<String, ValidationEtatPhaseDTO> phases = new HashMap<>();
        
        // Phase CREATION
        ValidationEtatPhaseDTO phaseCreation = getValidationEtatPhase(dossierId, PhaseFrais.CREATION);
        phases.put("CREATION", phaseCreation);
        
        // Phase ENQUETE
        ValidationEtatPhaseDTO phaseEnquete = getValidationEtatPhase(dossierId, PhaseFrais.ENQUETE);
        phases.put("ENQUETE", phaseEnquete);
        
        // Phase AMIABLE
        ValidationEtatPhaseDTO phaseAmiable = getValidationEtatPhase(dossierId, PhaseFrais.AMIABLE);
        phases.put("AMIABLE", phaseAmiable);
        
        // Phase JURIDIQUE
        ValidationEtatPhaseDTO phaseJuridique = getValidationEtatPhase(dossierId, PhaseFrais.JURIDIQUE);
        phases.put("JURIDIQUE", phaseJuridique);
        
        dto.setPhases(phases);
        
        // Déterminer le statut global
        StatutValidationTarifs statutGlobal = determineStatutGlobal(phases);
        dto.setStatutGlobal(statutGlobal);
        
        // Peut générer facture si tous les tarifs sont validés
        dto.setPeutGenererFacture(statutGlobal == StatutValidationTarifs.TOUS_TARIFS_VALIDES);
        
        return dto;
    }
    
    private ValidationEtatPhaseDTO getValidationEtatPhase(Long dossierId, PhaseFrais phase) {
        List<TarifDossier> tarifs = tarifDossierRepository.findByDossierIdAndPhase(dossierId, phase);
        
        int tarifsTotal = tarifs.size();
        int tarifsValides = (int) tarifs.stream()
            .filter(t -> t.getStatut() == StatutTarif.VALIDE)
            .count();
        
        String statut;
        if (tarifsTotal == 0) {
            statut = "AUCUN_TARIF";
        } else if (tarifsValides == tarifsTotal) {
            statut = "VALIDE";
        } else if (tarifs.stream().anyMatch(t -> t.getStatut() == StatutTarif.REJETE)) {
            statut = "REJETE";
        } else {
            statut = "EN_ATTENTE_VALIDATION";
        }
        
        ValidationEtatPhaseDTO dto = new ValidationEtatPhaseDTO();
        dto.setStatut(statut);
        dto.setTarifsTotal(tarifsTotal);
        dto.setTarifsValides(tarifsValides);
        
        return dto;
    }
    
    private StatutValidationTarifs determineStatutGlobal(Map<String, ValidationEtatPhaseDTO> phases) {
        ValidationEtatPhaseDTO creation = phases.get("CREATION");
        ValidationEtatPhaseDTO enquete = phases.get("ENQUETE");
        ValidationEtatPhaseDTO amiable = phases.get("AMIABLE");
        ValidationEtatPhaseDTO juridique = phases.get("JURIDIQUE");
        
        // Vérifier si toutes les phases sont validées
        boolean toutesValidees = creation.getStatut().equals("VALIDE") &&
            (enquete.getTarifsTotal() == 0 || enquete.getStatut().equals("VALIDE")) &&
            (amiable.getTarifsTotal() == 0 || amiable.getStatut().equals("VALIDE")) &&
            (juridique.getTarifsTotal() == 0 || juridique.getStatut().equals("VALIDE"));
        
        if (toutesValidees) {
            return StatutValidationTarifs.TOUS_TARIFS_VALIDES;
        }
        
        // Vérifier les phases une par une
        if (creation.getStatut().equals("VALIDE")) {
            if (enquete.getStatut().equals("VALIDE") || enquete.getTarifsTotal() == 0) {
                if (amiable.getStatut().equals("VALIDE") || amiable.getTarifsTotal() == 0) {
                    if (juridique.getStatut().equals("VALIDE") || juridique.getTarifsTotal() == 0) {
                        return StatutValidationTarifs.TOUS_TARIFS_VALIDES;
                    }
                    return StatutValidationTarifs.TARIFS_AMIABLE_VALIDES;
                }
                return StatutValidationTarifs.TARIFS_ENQUETE_VALIDES;
            }
            return StatutValidationTarifs.TARIFS_CREATION_VALIDES;
        }
        
        return StatutValidationTarifs.EN_COURS;
    }
    
    private void updateStatutValidationTarifs(Long dossierId) {
        Optional<Finance> financeOpt = financeRepository.findByDossierId(dossierId);
        if (financeOpt.isPresent()) {
            Finance finance = financeOpt.get();
            ValidationEtatDTO validationEtat = getValidationEtat(dossierId);
            finance.setStatutValidationTarifs(validationEtat.getStatutGlobal());
            financeRepository.save(finance);
        }
    }
    
    @Override
    public DetailFactureDTO getDetailFacture(Long dossierId) {
        Dossier dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        DetailFactureDTO dto = new DetailFactureDTO();
        
        // Frais création (phase CREATION)
        List<TarifDossier> tarifsCreation = tarifDossierRepository
            .findByDossierIdAndPhase(dossierId, PhaseFrais.CREATION)
            .stream()
            .filter(t -> t.getStatut() == StatutTarif.VALIDE)
            .collect(Collectors.toList());
        BigDecimal fraisCreation = tarifsCreation.stream()
            .map(TarifDossier::getMontantTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setFraisCreationDossier(fraisCreation);
        
        // ✅ Frais enquête (phase ENQUETE) - IMPORTANT : inclure le 300 TND fixe
        List<TarifDossier> tarifsEnquete = tarifDossierRepository
            .findByDossierIdAndPhase(dossierId, PhaseFrais.ENQUETE)
            .stream()
            .filter(t -> t.getStatut() == StatutTarif.VALIDE)
            .collect(Collectors.toList());
        BigDecimal fraisEnquete = tarifsEnquete.stream()
            .map(TarifDossier::getMontantTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setFraisEnquete(fraisEnquete);
        
        // Frais amiable (phase AMIABLE)
        List<TarifDossier> tarifsAmiable = tarifDossierRepository
            .findByDossierIdAndPhase(dossierId, PhaseFrais.AMIABLE)
            .stream()
            .filter(t -> t.getStatut() == StatutTarif.VALIDE)
            .collect(Collectors.toList());
        BigDecimal fraisAmiable = tarifsAmiable.stream()
            .map(TarifDossier::getMontantTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setCoutActionsAmiable(fraisAmiable);
        
        // Frais juridique (phase JURIDIQUE)
        List<TarifDossier> tarifsJuridique = tarifDossierRepository
            .findByDossierIdAndPhase(dossierId, PhaseFrais.JURIDIQUE)
            .stream()
            .filter(t -> t.getStatut() == StatutTarif.VALIDE)
            .collect(Collectors.toList());
        BigDecimal fraisJuridique = tarifsJuridique.stream()
            .map(TarifDossier::getMontantTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setCoutActionsJuridique(fraisJuridique);
        
        // Frais avocat et huissier (depuis Finance)
        Optional<Finance> financeOpt = financeRepository.findByDossierId(dossierId);
        BigDecimal fraisAvocat = BigDecimal.ZERO;
        BigDecimal fraisHuissier = BigDecimal.ZERO;
        BigDecimal coutGestionTotal = BigDecimal.ZERO;
        
        if (financeOpt.isPresent()) {
            Finance finance = financeOpt.get();
            fraisAvocat = finance.getFraisAvocat() != null ? 
                BigDecimal.valueOf(finance.getFraisAvocat()) : BigDecimal.ZERO;
            fraisHuissier = finance.getFraisHuissier() != null ? 
                BigDecimal.valueOf(finance.getFraisHuissier()) : BigDecimal.ZERO;
            coutGestionTotal = finance.calculerCoutGestionTotal() != null ? 
                BigDecimal.valueOf(finance.calculerCoutGestionTotal()) : BigDecimal.ZERO;
        }
        
        dto.setFraisAvocat(fraisAvocat);
        dto.setFraisHuissier(fraisHuissier);
        dto.setCoutGestionTotal(coutGestionTotal);
        
        // ✅ Calcul des commissions selon l'annexe
        BigDecimal commissionAmiable = calculerCommissionAmiable(dossier);
        BigDecimal commissionJuridique = calculerCommissionJuridique(dossier);
        BigDecimal commissionInterets = calculerCommissionInterets(dossier);
        
        dto.setCommissionAmiable(commissionAmiable);
        dto.setCommissionJuridique(commissionJuridique);
        dto.setCommissionInterets(commissionInterets);
        
        // Calcul du total HT
        BigDecimal totalHT = fraisCreation
            .add(fraisEnquete)  // ✅ INCLURE LES FRAIS D'ENQUÊTE
            .add(coutGestionTotal)
            .add(fraisAmiable)
            .add(fraisJuridique)
            .add(fraisAvocat)
            .add(fraisHuissier)
            .add(commissionAmiable)
            .add(commissionJuridique)
            .add(commissionInterets);
        
        dto.setTotalHT(totalHT);
        
        // TVA (19%)
        BigDecimal tva = totalHT.multiply(new BigDecimal("0.19"));
        dto.setTva(tva);
        
        // Total TTC
        BigDecimal totalTTC = totalHT.add(tva);
        dto.setTotalTTC(totalTTC);
        dto.setTotalFacture(totalTTC);  // Alias
        
        return dto;
    }
    
    /**
     * Calcule la commission amiable (12% du montant recouvré en phase amiable)
     */
    private BigDecimal calculerCommissionAmiable(Dossier dossier) {
        if (dossier.getMontantRecouvrePhaseAmiable() != null && 
            dossier.getMontantRecouvrePhaseAmiable() > 0) {
            return BigDecimal.valueOf(dossier.getMontantRecouvrePhaseAmiable())
                .multiply(TAUX_COMMISSION_AMIABLE);
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Calcule la commission juridique (15% du montant recouvré en phase juridique)
     */
    private BigDecimal calculerCommissionJuridique(Dossier dossier) {
        if (dossier.getMontantRecouvrePhaseJuridique() != null && 
            dossier.getMontantRecouvrePhaseJuridique() > 0) {
            return BigDecimal.valueOf(dossier.getMontantRecouvrePhaseJuridique())
                .multiply(TAUX_COMMISSION_JURIDIQUE);
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Calcule la commission sur intérêts (50% du montant des intérêts recouvrés)
     */
    private BigDecimal calculerCommissionInterets(Dossier dossier) {
        // Vérifier si le champ montantInteretsRecouvres existe
        // Pour l'instant, on retourne 0 si le champ n'existe pas
        try {
            // Utiliser la réflexion pour vérifier si le champ existe
            java.lang.reflect.Field field = dossier.getClass().getDeclaredField("montantInteretsRecouvres");
            field.setAccessible(true);
            Double montantInterets = (Double) field.get(dossier);
            if (montantInterets != null && montantInterets > 0) {
                return BigDecimal.valueOf(montantInterets)
                    .multiply(TAUX_COMMISSION_INTERETS);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Le champ n'existe pas encore, on retourne 0
            logger.debug("Champ montantInteretsRecouvres non trouvé dans Dossier, commission intérêts = 0");
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Crée automatiquement l'avance sur frais de recouvrement judiciaire (1000 TND)
     */
    @Override
    public TarifDossier createAvanceRecouvrementJuridique(Dossier dossier) {
        // Vérifier si un tarif existe déjà
        Optional<TarifDossier> existing = tarifDossierRepository
            .findByDossierIdAndPhaseAndCategorie(dossier.getId(), PhaseFrais.JURIDIQUE, "AVANCE_RECOUVREMENT_JURIDIQUE");
        if (existing.isPresent()) {
            return existing.get();
        }
        
        TarifDossier tarif = TarifDossier.builder()
            .dossier(dossier)
            .phase(PhaseFrais.JURIDIQUE)
            .categorie("AVANCE_RECOUVREMENT_JURIDIQUE")
            .typeElement("Avance sur frais de recouvrement judiciaire")
            .coutUnitaire(AVANCE_RECOUVREMENT_JURIDIQUE)
            .quantite(1)
            .montantTotal(AVANCE_RECOUVREMENT_JURIDIQUE)
            .statut(StatutTarif.VALIDE)
            .dateCreation(LocalDateTime.now())
            .dateValidation(LocalDateTime.now())
            .commentaire("Avance fixe selon annexe - Création automatique lors du passage en phase juridique")
            .build();
        
        return tarifDossierRepository.save(tarif);
    }
    
    /**
     * Crée un tarif pour l'attestation de carence (500 TND) - Manuel
     */
    @Override
    public TarifDossier createTarifAttestationCarence(Long dossierId, String commentaire) {
        Dossier dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Vérifier si un tarif existe déjà
        Optional<TarifDossier> existing = tarifDossierRepository
            .findByDossierIdAndPhaseAndCategorie(dossierId, PhaseFrais.JURIDIQUE, "ATTESTATION_CARENCE");
        if (existing.isPresent()) {
            throw new RuntimeException("Un tarif d'attestation de carence existe déjà pour ce dossier");
        }
        
        TarifDossier tarif = TarifDossier.builder()
            .dossier(dossier)
            .phase(PhaseFrais.JURIDIQUE)
            .categorie("ATTESTATION_CARENCE")
            .typeElement("Attestation de carence à la demande du mandant")
            .coutUnitaire(ATTESTATION_CARENCE)
            .quantite(1)
            .montantTotal(ATTESTATION_CARENCE)
            .statut(StatutTarif.EN_ATTENTE_VALIDATION)
            .dateCreation(LocalDateTime.now())
            .commentaire(commentaire != null ? commentaire : "Attestation de carence - À la demande du mandant")
            .build();
        
        return tarifDossierRepository.save(tarif);
    }
    
    @Override
    public projet.carthagecreance_backend.DTO.FactureDTO genererFacture(Long dossierId) {
        logger.info("🔍 [GENERER-FACTURE] Début génération facture pour dossier {}", dossierId);
        
        // Vérifier que tous les tarifs sont validés
        ValidationEtatDTO validationEtat = getValidationEtat(dossierId);
        if (!validationEtat.getPeutGenererFacture()) {
            logger.error("❌ [GENERER-FACTURE] Tous les tarifs ne sont pas validés. Statut: {}", 
                validationEtat.getStatutGlobal());
            throw new RuntimeException("Impossible de générer la facture : tous les tarifs ne sont pas validés. Statut: " + validationEtat.getStatutGlobal());
        }
        
        logger.info("✅ [GENERER-FACTURE] Validation OK: statutGlobal={}", validationEtat.getStatutGlobal());
        
        // ✅ Récupérer les tarifs validés pour vérification
        List<TarifDossier> tarifsValides = tarifDossierRepository.findByDossierIdAndStatut(
            dossierId, StatutTarif.VALIDE);
        
        logger.info("📊 [GENERER-FACTURE] {} tarifs validés trouvés pour le dossier {}", 
            tarifsValides.size(), dossierId);
        
        if (tarifsValides.isEmpty()) {
            logger.error("❌ [GENERER-FACTURE] Aucun tarif validé trouvé malgré la validation OK");
            
            // Diagnostic complet
            List<TarifDossier> tousTarifs = tarifDossierRepository.findByDossierId(dossierId);
            logger.error("📊 [GENERER-FACTURE] Diagnostic - Tous les tarifs: {}", tousTarifs.size());
            tousTarifs.forEach(t -> logger.error("  - ID {}: phase={}, statut={}, montant={}", 
                t.getId(), t.getPhase(), t.getStatut(), t.getMontantTotal()));
            
            throw new RuntimeException("Aucun tarif validé trouvé pour générer la facture");
        }
        
        // Calculer les montants pour vérification
        DetailFactureDTO detail = getDetailFacture(dossierId);
        logger.info("💰 [GENERER-FACTURE] Détail facture calculé: totalHT={}, totalTTC={}", 
            detail.getTotalHT(), detail.getTotalTTC());
        
        // Générer la facture via le service FactureService (qui utilise maintenant TarifDossier)
        Dossier dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));
        
        LocalDate periodeDebut = convertToLocalDate(dossier.getDateCreation());
        LocalDate periodeFin = dossier.getDateCloture() != null ? 
            convertToLocalDate(dossier.getDateCloture()) : LocalDate.now();
        
        logger.info("📅 [GENERER-FACTURE] Période: {} - {}", periodeDebut, periodeFin);
        
        Facture factureEntity = factureService.genererFactureAutomatique(dossierId, periodeDebut, periodeFin);
        
        // Convertir en DTO
        projet.carthagecreance_backend.DTO.FactureDTO facture = projet.carthagecreance_backend.DTO.FactureDTO.builder()
            .id(factureEntity.getId())
            .numeroFacture(factureEntity.getNumeroFacture())
            .dossierId(dossierId)
            .periodeDebut(factureEntity.getPeriodeDebut())
            .periodeFin(factureEntity.getPeriodeFin())
            .dateEmission(factureEntity.getDateEmission())
            .dateEcheance(factureEntity.getDateEcheance())
            .montantHT(factureEntity.getMontantHT())
            .montantTTC(factureEntity.getMontantTTC())
            .tva(factureEntity.getTva())
            .statut(factureEntity.getStatut())
            .build();
        
        // Mettre à jour le statut de validation des tarifs
        Optional<Finance> financeOpt = financeRepository.findByDossierId(dossierId);
        if (financeOpt.isPresent()) {
            Finance finance = financeOpt.get();
            finance.setStatutValidationTarifs(StatutValidationTarifs.FACTURE_GENEREE);
            financeRepository.save(finance);
        }
        
        logger.info("Facture générée pour le dossier {}: {}", dossierId, facture.getNumeroFacture());
        
        return facture;
    }
    
    @Override
    public Optional<TarifDossier> getTarifById(Long tarifId) {
        return tarifDossierRepository.findById(tarifId);
    }
    
    @Override
    public List<TarifDossierDTO> getTarifsByDossier(Long dossierId) {
        List<TarifDossier> tarifs = tarifDossierRepository.findByDossierId(dossierId);
        return tarifs.stream()
            .map(this::mapToTarifDTO)
            .collect(Collectors.toList());
    }
    
    // Méthodes utilitaires de mapping
    private TarifDossierDTO mapToTarifDTO(TarifDossier tarif) {
        TarifDossierDTO dto = TarifDossierDTO.builder()
            .id(tarif.getId())
            .dossierId(tarif.getDossier() != null ? tarif.getDossier().getId() : null)
            .phase(tarif.getPhase())
            .categorie(tarif.getCategorie())
            .typeElement(tarif.getTypeElement())
            .coutUnitaire(tarif.getCoutUnitaire())
            .quantite(tarif.getQuantite())
            .montantTotal(tarif.getMontantTotal())
            .statut(tarif.getStatut())
            .dateCreation(tarif.getDateCreation())
            .dateValidation(tarif.getDateValidation())
            .commentaire(tarif.getCommentaire())
            .build();
        
        if (tarif.getDocumentHuissier() != null) {
            dto.setDocumentHuissierId(tarif.getDocumentHuissier().getId());
        }
        if (tarif.getActionHuissier() != null) {
            dto.setActionHuissierId(tarif.getActionHuissier().getId());
        }
        if (tarif.getAudience() != null) {
            dto.setAudienceId(tarif.getAudience().getId());
        }
        if (tarif.getAction() != null) {
            dto.setActionId(tarif.getAction().getId());
        }
        if (tarif.getEnquete() != null) {
            dto.setEnqueteId(tarif.getEnquete().getId());
        }
        
        return dto;
    }
    
    private LocalDate convertToLocalDate(java.util.Date date) {
        if (date == null) return LocalDate.now();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

