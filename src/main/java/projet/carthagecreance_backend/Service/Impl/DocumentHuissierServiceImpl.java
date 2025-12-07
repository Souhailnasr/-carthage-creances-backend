package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.DocumentHuissierDTO;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.DocumentHuissierRepository;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Service.DocumentHuissierService;
import projet.carthagecreance_backend.Service.NotificationHuissierService;
import projet.carthagecreance_backend.Service.AutomaticNotificationService;
import projet.carthagecreance_backend.Service.RecommendationService;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class DocumentHuissierServiceImpl implements DocumentHuissierService {
    
    @Autowired
    private DocumentHuissierRepository documentHuissierRepository;
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private NotificationHuissierService notificationHuissierService; // Gardé pour compatibilité
    
    @Autowired
    private AutomaticNotificationService automaticNotificationService; // Nouveau système unifié
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private projet.carthagecreance_backend.Service.StatistiqueService statistiqueService;
    
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
        
        // Créer les notifications programmées (rappel et expiration) - Ancien système
        try {
            notificationHuissierService.scheduleDocumentNotifications(saved);
        } catch (Exception e) {
            System.err.println("Erreur lors de la programmation des notifications (ancien système): " + e.getMessage());
        }
        
        // Créer une notification via le système unifié
        try {
            automaticNotificationService.notifierCreationDocumentHuissier(saved, dossier);
        } catch (Exception e) {
            System.err.println("Erreur lors de la notification unifiée: " + e.getMessage());
        }
        
        // Créer une recommandation initiale
        try {
            recommendationService.createRecommendationForDocument(saved);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la recommandation: " + e.getMessage());
        }
        
        // Recalcul automatique des statistiques (asynchrone)
        try {
            statistiqueService.recalculerStatistiquesAsync();
        } catch (Exception e) {
            System.err.println("Erreur lors du recalcul automatique des statistiques après création de document huissier: " + e.getMessage());
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

    @Override
    public DocumentHuissier markAsCompleted(Long documentId) {
        System.out.println("=== Service markAsCompleted - Document ID: " + documentId + " ===");

        DocumentHuissier document = documentHuissierRepository.findById(documentId)
                .orElseThrow(() -> {
                    System.err.println("❌ Document non trouvé avec l'ID: " + documentId);
                    return new RuntimeException("Document non trouvé avec l'ID: " + documentId);
                });

        System.out.println("Document trouvé - Statut actuel: " + document.getStatus());

        // Vérifier les contraintes : ne peut marquer comme complété que si PENDING
        if (document.getStatus() == StatutDocumentHuissier.EXPIRED) {
            System.err.println("❌ Tentative de marquer un document EXPIRED comme complété");
            throw new RuntimeException("Impossible de marquer un document expiré comme complété");
        }

        if (document.getStatus() == StatutDocumentHuissier.COMPLETED) {
            System.err.println("❌ Tentative de marquer un document déjà COMPLETED");
            throw new RuntimeException("Le document est déjà marqué comme complété");
        }

        // Marquer comme complété
        System.out.println("Changement de statut: " + document.getStatus() + " -> COMPLETED");
        document.setStatus(StatutDocumentHuissier.COMPLETED);

        try {
            DocumentHuissier saved = documentHuissierRepository.save(document);
            System.out.println("✅ Document sauvegardé avec succès - Nouveau statut: " + saved.getStatus());

            return saved;
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de la sauvegarde du document: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la sauvegarde du document: " + e.getMessage(), e);
        }
    }
    }


