package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.DocumentHuissierDTO;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.DocumentHuissierRepository;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Service.AuditLogService;
import projet.carthagecreance_backend.Service.DocumentHuissierService;
import projet.carthagecreance_backend.Service.NotificationHuissierService;
import projet.carthagecreance_backend.Service.RecommendationService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DocumentHuissierServiceImpl implements DocumentHuissierService {
    
    @Autowired
    private DocumentHuissierRepository documentHuissierRepository;
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private NotificationHuissierService notificationHuissierService;
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Override
    public DocumentHuissier createDocument(DocumentHuissierDTO dto) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dto.getDossierId())
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dto.getDossierId()));
        
        // Déterminer le délai légal selon le type
        Integer delaiLegal = dto.getDelaiLegalDays();
        if (delaiLegal == null) {
            if (dto.getTypeDocument() == TypeDocumentHuissier.PV_MISE_EN_DEMEURE) {
                delaiLegal = 10;
            } else {
                delaiLegal = 20;
            }
        }
        
        // Créer le document
        DocumentHuissier document = DocumentHuissier.builder()
                .dossierId(dto.getDossierId())
                .typeDocument(dto.getTypeDocument())
                .dateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : Instant.now())
                .delaiLegalDays(delaiLegal)
                .pieceJointeUrl(dto.getPieceJointeUrl())
                .huissierName(dto.getHuissierName())
                .status(StatutDocumentHuissier.PENDING)
                .notified(false)
                .build();
        
        DocumentHuissier saved = documentHuissierRepository.save(document);
        
        // Créer un audit log
        try {
            Map<String, Object> after = new HashMap<>();
            after.put("documentId", saved.getId());
            after.put("typeDocument", saved.getTypeDocument());
            after.put("delaiLegalDays", saved.getDelaiLegalDays());
            
            auditLogService.logChangement(
                dto.getDossierId(),
                null,
                TypeChangementAudit.DOCUMENT_CREATE,
                new HashMap<>(),
                after,
                "Création du document huissier: " + saved.getTypeDocument()
            );
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'audit log: " + e.getMessage());
        }
        
        // Créer les notifications programmées (rappel et expiration)
        try {
            notificationHuissierService.scheduleDocumentNotifications(saved);
        } catch (Exception e) {
            System.err.println("Erreur lors de la programmation des notifications: " + e.getMessage());
        }
        
        // Créer une recommandation initiale
        try {
            recommendationService.createRecommendationForDocument(saved);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la recommandation: " + e.getMessage());
        }
        
        return saved;
    }
    
    @Override
    @Transactional(readOnly = true)
    public DocumentHuissier getDocumentById(Long id) {
        return documentHuissierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'ID: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DocumentHuissier> getDocumentsByDossier(Long dossierId) {
        return documentHuissierRepository.findByDossierId(dossierId);
    }
    
    @Override
    public DocumentHuissier markAsExpired(Long documentId) {
        DocumentHuissier document = documentHuissierRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'ID: " + documentId));
        
        document.setStatus(StatutDocumentHuissier.EXPIRED);
        DocumentHuissier saved = documentHuissierRepository.save(document);
        
        // Créer une notification d'expiration
        try {
            notificationHuissierService.notifyDocumentExpired(saved);
        } catch (Exception e) {
            System.err.println("Erreur lors de la notification d'expiration: " + e.getMessage());
        }
        
        // Créer une recommandation d'escalade
        try {
            recommendationService.createRecommendationForExpiredDocument(saved);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la recommandation: " + e.getMessage());
        }
        
        return saved;
    }
}

