package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.MontantDossierDTO;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Service.AuditLogService;
import projet.carthagecreance_backend.Service.DossierMontantService;
import projet.carthagecreance_backend.Service.NotificationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class DossierMontantServiceImpl implements DossierMontantService {
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public Dossier updateMontants(Long dossierId, MontantDossierDTO dto) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Sauvegarder l'état avant
        Map<String, Object> before = new HashMap<>();
        before.put("montantTotal", dossier.getMontantTotal());
        before.put("montantRecouvre", dossier.getMontantRecouvre());
        before.put("montantRestant", dossier.getMontantRestant());
        before.put("etatDossier", dossier.getEtatDossier());
        
        // Mettre à jour montantTotal si fourni
        if (dto.getMontantTotal() != null) {
            if (dto.getMontantTotal().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Le montant total ne peut pas être négatif");
            }
            dossier.setMontantTotal(dto.getMontantTotal().doubleValue());
        }
        
        // Mettre à jour montantRecouvre si fourni
        if (dto.getMontantRecouvre() != null) {
            if (dto.getMontantRecouvre().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Le montant recouvré ne peut pas être négatif");
            }
            
            if (dto.getUpdateMode() == ModeMiseAJour.ADD) {
                // Ajouter au montant existant
                BigDecimal current = dossier.getMontantRecouvre() != null ? 
                    BigDecimal.valueOf(dossier.getMontantRecouvre()) : BigDecimal.ZERO;
                dossier.setMontantRecouvre(current.add(dto.getMontantRecouvre()).doubleValue());
            } else {
                // Remplacer le montant
                dossier.setMontantRecouvre(dto.getMontantRecouvre().doubleValue());
            }
        }
        
        // Recalculer montantRestant et état
        dossier = recalculerMontantRestantEtEtat(dossier);
        
        // Sauvegarder l'état après
        Map<String, Object> after = new HashMap<>();
        after.put("montantTotal", dossier.getMontantTotal());
        after.put("montantRecouvre", dossier.getMontantRecouvre());
        after.put("montantRestant", dossier.getMontantRestant());
        after.put("etatDossier", dossier.getEtatDossier());
        
        // Créer un audit log
        try {
            auditLogService.logChangement(
                dossierId,
                null, // userId sera récupéré depuis le contexte de sécurité
                TypeChangementAudit.AMOUNT_UPDATE,
                before,
                after,
                "Mise à jour des montants du dossier"
            );
        } catch (Exception e) {
            // Logger l'erreur mais ne pas faire échouer la mise à jour
            System.err.println("Erreur lors de la création de l'audit log: " + e.getMessage());
        }
        
        return dossierRepository.save(dossier);
    }
    
    @Override
    public Dossier updateMontantRecouvreAmiable(Long dossierId, BigDecimal montantRecouvre, ModeMiseAJour updateMode) {
        MontantDossierDTO dto = MontantDossierDTO.builder()
                .montantRecouvre(montantRecouvre)
                .updateMode(updateMode)
                .build();
        return updateMontants(dossierId, dto);
    }
    
    @Override
    public Dossier recalculerMontantRestantEtEtat(Dossier dossier) {
        if (dossier.getMontantTotal() == null) {
            dossier.setMontantTotal(0.0);
        }
        if (dossier.getMontantRecouvre() == null) {
            dossier.setMontantRecouvre(0.0);
        }
        
        // Calculer le montant restant
        BigDecimal montantTotal = BigDecimal.valueOf(dossier.getMontantTotal());
        BigDecimal montantRecouvre = BigDecimal.valueOf(dossier.getMontantRecouvre());
        BigDecimal montantRestant = montantTotal.subtract(montantRecouvre);
        
        // S'assurer que montantRestant n'est pas négatif
        if (montantRestant.compareTo(BigDecimal.ZERO) < 0) {
            montantRestant = BigDecimal.ZERO;
            // Ajuster montantRecouvre si nécessaire
            dossier.setMontantRecouvre(montantTotal.doubleValue());
        }
        
        dossier.setMontantRestant(montantRestant.doubleValue());
        
        // Calculer l'état selon les règles tunisiennes
        if (montantRestant.compareTo(BigDecimal.ZERO) == 0) {
            dossier.setEtatDossier(EtatDossier.RECOVERED_TOTAL);
        } else if (montantRecouvre.compareTo(BigDecimal.ZERO) > 0) {
            dossier.setEtatDossier(EtatDossier.RECOVERED_PARTIAL);
        } else {
            dossier.setEtatDossier(EtatDossier.NOT_RECOVERED);
        }
        
        return dossier;
    }
}

