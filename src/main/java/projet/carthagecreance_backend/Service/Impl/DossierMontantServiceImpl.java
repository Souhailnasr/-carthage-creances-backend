package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.MontantDossierDTO;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Repository.HistoriqueRecouvrementRepository;
import projet.carthagecreance_backend.Service.DossierMontantService;
import projet.carthagecreance_backend.Service.NotificationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class DossierMontantServiceImpl implements DossierMontantService {
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private HistoriqueRecouvrementRepository historiqueRecouvrementRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public Dossier updateMontants(Long dossierId, MontantDossierDTO dto) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
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
        // S'assurer que montantCreance n'est pas null
        if (dossier.getMontantCreance() == null) {
            throw new IllegalArgumentException("Le montant de créance ne peut pas être null");
        }
        
        // S'assurer que montantRecouvre n'est pas null
        if (dossier.getMontantRecouvre() == null) {
            dossier.setMontantRecouvre(0.0);
        }
        
        // IMPORTANT: montantTotal doit toujours être égal à montantCreance
        dossier.setMontantTotal(dossier.getMontantCreance());
        
        // Calculer le montant restant : montantRestant = montantCreance - montantRecouvre
        BigDecimal montantCreance = BigDecimal.valueOf(dossier.getMontantCreance());
        BigDecimal montantRecouvre = BigDecimal.valueOf(dossier.getMontantRecouvre());
        BigDecimal montantRestant = montantCreance.subtract(montantRecouvre);
        
        // S'assurer que montantRestant n'est pas négatif
        if (montantRestant.compareTo(BigDecimal.ZERO) < 0) {
            montantRestant = BigDecimal.ZERO;
            // Ajuster montantRecouvre si nécessaire (ne peut pas dépasser montantCreance)
            dossier.setMontantRecouvre(montantCreance.doubleValue());
            montantRecouvre = montantCreance;
        }
        
        dossier.setMontantRestant(montantRestant.doubleValue());
        
        // Calculer l'état selon les règles :
        // RECOVERED_TOTAL : si montantRecouvre == montantCreance ET montantRestant == 0
        // RECOVERED_PARTIAL : si montantRecouvre > 0 ET montantRestant > 0
        // NOT_RECOVERED : si montantRecouvre == 0
        BigDecimal zero = BigDecimal.ZERO;
        if (montantRecouvre.compareTo(montantCreance) == 0 && montantRestant.compareTo(zero) == 0) {
            dossier.setEtatDossier(EtatDossier.RECOVERED_TOTAL);
        } else if (montantRecouvre.compareTo(zero) > 0 && montantRestant.compareTo(zero) > 0) {
            dossier.setEtatDossier(EtatDossier.RECOVERED_PARTIAL);
        } else {
            dossier.setEtatDossier(EtatDossier.NOT_RECOVERED);
        }
        
        return dossier;
    }
    
    @Override
    public Dossier updateMontantRecouvrePhaseAmiable(
            Long dossierId, 
            BigDecimal montantRecouvre, 
            ModeMiseAJour updateMode,
            Long actionId,
            Long utilisateurId,
            String commentaire) {
        
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Récupérer le montant déjà recouvré en phase amiable
        BigDecimal montantDejaRecouvrePhaseAmiable = dossier.getMontantRecouvrePhaseAmiable() != null ?
                BigDecimal.valueOf(dossier.getMontantRecouvrePhaseAmiable()) : BigDecimal.ZERO;
        
        // Calculer le nouveau montant recouvré phase amiable
        BigDecimal nouveauMontantPhaseAmiable;
        if (updateMode == ModeMiseAJour.ADD) {
            nouveauMontantPhaseAmiable = montantDejaRecouvrePhaseAmiable.add(montantRecouvre);
        } else {
            nouveauMontantPhaseAmiable = montantRecouvre;
        }
        
        // Mettre à jour le montant recouvré phase amiable
        dossier.setMontantRecouvrePhaseAmiable(nouveauMontantPhaseAmiable.doubleValue());
        
        // Calculer le montant total recouvré (amiable + juridique)
        BigDecimal montantRecouvrePhaseJuridique = dossier.getMontantRecouvrePhaseJuridique() != null ?
                BigDecimal.valueOf(dossier.getMontantRecouvrePhaseJuridique()) : BigDecimal.ZERO;
        BigDecimal montantTotalRecouvre = nouveauMontantPhaseAmiable.add(montantRecouvrePhaseJuridique);
        
        // Mettre à jour le montant recouvré total
        dossier.setMontantRecouvre(montantTotalRecouvre.doubleValue());
        
        // Recalculer montantRestant et état
        dossier = recalculerMontantRestantEtEtat(dossier);
        
        // Sauvegarder le dossier
        dossier = dossierRepository.save(dossier);
        
        // Créer l'entrée d'historique
        HistoriqueRecouvrement historique = HistoriqueRecouvrement.builder()
                .dossierId(dossierId)
                .phase(HistoriqueRecouvrement.PhaseRecouvrement.AMIABLE)
                .montantRecouvre(montantRecouvre)
                .montantTotalRecouvre(montantTotalRecouvre)
                .montantRestant(BigDecimal.valueOf(dossier.getMontantRestant()))
                .typeAction(HistoriqueRecouvrement.TypeActionRecouvrement.ACTION_AMIABLE)
                .actionId(actionId)
                .utilisateurId(utilisateurId)
                .dateEnregistrement(LocalDateTime.now())
                .commentaire(commentaire)
                .build();
        
        historiqueRecouvrementRepository.save(historique);
        
        return dossier;
    }
    
    @Override
    public Dossier updateMontantRecouvrePhaseJuridique(
            Long dossierId,
            BigDecimal montantRecouvre,
            ModeMiseAJour updateMode,
            Long actionId,
            Long utilisateurId,
            HistoriqueRecouvrement.TypeActionRecouvrement typeAction,
            String commentaire) {
        
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Récupérer le montant déjà recouvré en phase juridique
        BigDecimal montantDejaRecouvrePhaseJuridique = dossier.getMontantRecouvrePhaseJuridique() != null ?
                BigDecimal.valueOf(dossier.getMontantRecouvrePhaseJuridique()) : BigDecimal.ZERO;
        
        // Calculer le nouveau montant recouvré phase juridique
        BigDecimal nouveauMontantPhaseJuridique;
        if (updateMode == ModeMiseAJour.ADD) {
            nouveauMontantPhaseJuridique = montantDejaRecouvrePhaseJuridique.add(montantRecouvre);
        } else {
            nouveauMontantPhaseJuridique = montantRecouvre;
        }
        
        // Mettre à jour le montant recouvré phase juridique
        dossier.setMontantRecouvrePhaseJuridique(nouveauMontantPhaseJuridique.doubleValue());
        
        // Calculer le montant total recouvré (amiable + juridique)
        BigDecimal montantRecouvrePhaseAmiable = dossier.getMontantRecouvrePhaseAmiable() != null ?
                BigDecimal.valueOf(dossier.getMontantRecouvrePhaseAmiable()) : BigDecimal.ZERO;
        BigDecimal montantTotalRecouvre = montantRecouvrePhaseAmiable.add(nouveauMontantPhaseJuridique);
        
        // Mettre à jour le montant recouvré total
        dossier.setMontantRecouvre(montantTotalRecouvre.doubleValue());
        
        // Recalculer montantRestant et état
        dossier = recalculerMontantRestantEtEtat(dossier);
        
        // Sauvegarder le dossier
        dossier = dossierRepository.save(dossier);
        
        // Créer l'entrée d'historique
        HistoriqueRecouvrement historique = HistoriqueRecouvrement.builder()
                .dossierId(dossierId)
                .phase(HistoriqueRecouvrement.PhaseRecouvrement.JURIDIQUE)
                .montantRecouvre(montantRecouvre)
                .montantTotalRecouvre(montantTotalRecouvre)
                .montantRestant(BigDecimal.valueOf(dossier.getMontantRestant()))
                .typeAction(typeAction)
                .actionId(actionId)
                .utilisateurId(utilisateurId)
                .dateEnregistrement(LocalDateTime.now())
                .commentaire(commentaire)
                .build();
        
        historiqueRecouvrementRepository.save(historique);
        
        return dossier;
    }
}

