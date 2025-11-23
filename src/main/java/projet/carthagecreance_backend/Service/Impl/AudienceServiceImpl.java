package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projet.carthagecreance_backend.Entity.Audience;
import projet.carthagecreance_backend.Entity.Avocat;
import projet.carthagecreance_backend.Entity.DecisionResult;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Huissier;
import projet.carthagecreance_backend.Entity.TribunalType;
import projet.carthagecreance_backend.DTO.AudienceRequestDTO;
import projet.carthagecreance_backend.Repository.AudienceRepository;
import projet.carthagecreance_backend.Repository.AvocatRepository;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Repository.HuissierRepository;
import projet.carthagecreance_backend.Service.AudienceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AudienceServiceImpl implements AudienceService {

    private static final Logger logger = LoggerFactory.getLogger(AudienceServiceImpl.class);

    @Autowired
    private AudienceRepository audienceRepository;
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private AvocatRepository avocatRepository;
    
    @Autowired
    private HuissierRepository huissierRepository;
    
    @Autowired
    private projet.carthagecreance_backend.Service.AutomaticNotificationService automaticNotificationService;

    @Override
    @Transactional
    public Audience createAudience(Audience audience) {
        Audience savedAudience = audienceRepository.save(audience);
        
        // Notification automatique de création d'audience
        try {
            if (savedAudience.getDossier() != null) {
                automaticNotificationService.notifierCreationAudience(savedAudience, savedAudience.getDossier());
                // Vérifier si c'est une audience prochaine
                if (savedAudience.getDateProchaine() != null) {
                    automaticNotificationService.notifierAudienceProchaine(savedAudience, savedAudience.getDossier());
                }
            }
        } catch (Exception e) {
            logger.warn("Erreur lors de la notification automatique de création d'audience: {}", e.getMessage());
        }
        
        return savedAudience;
    }
    
    /**
     * Crée une audience à partir d'un DTO
     * Charge les entités depuis la base de données pour éviter les problèmes de détachement JPA
     */
    @Transactional
    public Audience createAudienceFromDTO(AudienceRequestDTO dto) {
        logger.info("Création d'une audience depuis DTO: {}", dto);
        
        Audience audience = Audience.builder()
                .dateAudience(dto.getDateAudience())
                .dateProchaine(dto.getDateProchaine())
                .tribunalType(dto.getTribunalType())
                .lieuTribunal(dto.getLieuTribunal())
                .commentaireDecision(dto.getCommentaireDecision())
                .resultat(dto.getResultat())
                .build();
        
        // Charger le dossier depuis la base de données
        Long dossierId = dto.getDossierIdValue();
        if (dossierId != null) {
            logger.info("Chargement du dossier avec ID: {}", dossierId);
            Dossier dossier = dossierRepository.findById(dossierId)
                    .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
            audience.setDossier(dossier);
            logger.info("Dossier {} assigné à l'audience", dossier.getId());
        } else {
            throw new RuntimeException("Le dossier est obligatoire pour créer une audience");
        }
        
        // Charger l'avocat depuis la base de données (optionnel)
        Long avocatId = dto.getAvocatIdValue();
        if (avocatId != null) {
            logger.info("Chargement de l'avocat avec ID: {}", avocatId);
            Avocat avocat = avocatRepository.findById(avocatId)
                    .orElseThrow(() -> new RuntimeException("Avocat non trouvé avec l'ID: " + avocatId));
            audience.setAvocat(avocat);
            logger.info("Avocat {} assigné à l'audience", avocat.getId());
        }
        
        // Charger l'huissier depuis la base de données (optionnel)
        Long huissierId = dto.getHuissierIdValue();
        if (huissierId != null) {
            logger.info("Chargement de l'huissier avec ID: {}", huissierId);
            Huissier huissier = huissierRepository.findById(huissierId)
                    .orElseThrow(() -> new RuntimeException("Huissier non trouvé avec l'ID: " + huissierId));
            audience.setHuissier(huissier);
            logger.info("Huissier {} assigné à l'audience", huissier.getId());
        }
        
        Audience savedAudience = audienceRepository.save(audience);
        logger.info("Audience créée avec succès, ID: {}, dossier_id: {}", 
                savedAudience.getId(), 
                savedAudience.getDossier() != null ? savedAudience.getDossier().getId() : "NULL");
        
        // Notification automatique de création d'audience
        try {
            if (savedAudience.getDossier() != null) {
                automaticNotificationService.notifierCreationAudience(savedAudience, savedAudience.getDossier());
                // Vérifier si c'est une audience prochaine
                if (savedAudience.getDateProchaine() != null) {
                    automaticNotificationService.notifierAudienceProchaine(savedAudience, savedAudience.getDossier());
                }
            }
        } catch (Exception e) {
            logger.warn("Erreur lors de la notification automatique de création d'audience: {}", e.getMessage());
        }
        
        return savedAudience;
    }
    
    /**
     * Met à jour une audience à partir d'un DTO
     */
    @Transactional
    public Audience updateAudienceFromDTO(Long id, AudienceRequestDTO dto) {
        logger.info("Mise à jour de l'audience {} depuis DTO", id);
        
        Audience audience = audienceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Audience non trouvée avec l'ID: " + id));
        
        // Mettre à jour les champs simples
        audience.setDateAudience(dto.getDateAudience());
        audience.setDateProchaine(dto.getDateProchaine());
        audience.setTribunalType(dto.getTribunalType());
        audience.setLieuTribunal(dto.getLieuTribunal());
        audience.setCommentaireDecision(dto.getCommentaireDecision());
        audience.setResultat(dto.getResultat());
        
        // Charger et mettre à jour le dossier
        Long dossierId = dto.getDossierIdValue();
        if (dossierId != null) {
            Dossier dossier = dossierRepository.findById(dossierId)
                    .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
            audience.setDossier(dossier);
        }
        
        // Charger et mettre à jour l'avocat
        Long avocatId = dto.getAvocatIdValue();
        if (avocatId != null) {
            Avocat avocat = avocatRepository.findById(avocatId)
                    .orElseThrow(() -> new RuntimeException("Avocat non trouvé avec l'ID: " + avocatId));
            audience.setAvocat(avocat);
        } else {
            audience.setAvocat(null); // Retirer l'avocat si null
        }
        
        // Charger et mettre à jour l'huissier
        Long huissierId = dto.getHuissierIdValue();
        if (huissierId != null) {
            Huissier huissier = huissierRepository.findById(huissierId)
                    .orElseThrow(() -> new RuntimeException("Huissier non trouvé avec l'ID: " + huissierId));
            audience.setHuissier(huissier);
        } else {
            audience.setHuissier(null); // Retirer l'huissier si null
        }
        
        return audienceRepository.save(audience);
    }

    @Override
    public Optional<Audience> getAudienceById(Long id) {
        return audienceRepository.findById(id);
    }

    @Override
    public List<Audience> getAllAudiences() {
        return audienceRepository.findAll();
    }

    @Override
    public Audience updateAudience(Long id, Audience audience) {
        if (audienceRepository.existsById(id)) {
            audience.setId(id);
            return audienceRepository.save(audience);
        }
        throw new RuntimeException("Audience not found with id: " + id);
    }

    @Override
    public void deleteAudience(Long id) {
        if (audienceRepository.existsById(id)) {
            audienceRepository.deleteById(id);
        } else {
            throw new RuntimeException("Audience not found with id: " + id);
        }
    }

    @Override
    public List<Audience> getAudiencesByDossier(Long dossierId) {
        return audienceRepository.findByDossierId(dossierId);
    }

    @Override
    public List<Audience> getAudiencesByDate(LocalDate date) {
        return audienceRepository.findByDateAudience(date);
    }

    @Override
    public List<Audience> getAudiencesByDateRange(LocalDate startDate, LocalDate endDate) {
        return audienceRepository.findByDateAudienceBetween(startDate, endDate);
    }

    @Override
    public List<Audience> getAudiencesByTribunalType(TribunalType tribunalType) {
        return audienceRepository.findByTribunalType(tribunalType);
    }

    @Override
    public List<Audience> getAudiencesByResult(DecisionResult result) {
        return audienceRepository.findByResultat(result);
    }

    @Override
    public List<Audience> getAudiencesByAvocat(Long avocatId) {
        return audienceRepository.findByAvocatId(avocatId);
    }

    @Override
    public List<Audience> getAudiencesByHuissier(Long huissierId) {
        return audienceRepository.findByHuissierId(huissierId);
    }

    @Override
    public List<Audience> getAudiencesByLocation(String location) {
        return audienceRepository.findByLieuTribunalContainingIgnoreCase(location);
    }

    @Override
    public List<Audience> getUpcomingAudiences() {
        return audienceRepository.findAudiencesAVenir(LocalDate.now());
    }

    @Override
    public List<Audience> getPastAudiences() {
        return audienceRepository.findAudiencesPassees(LocalDate.now());
    }

    @Override
    public List<Audience> getNextAudienceByDossier(Long dossierId) {
        return audienceRepository.findProchaineAudienceParDossier(dossierId, LocalDate.now());
    }
}
