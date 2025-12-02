package projet.carthagecreance_backend.Service.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.PaiementDTO;
import projet.carthagecreance_backend.Entity.Facture;
import projet.carthagecreance_backend.Entity.Paiement;
import projet.carthagecreance_backend.Entity.StatutPaiement;
import projet.carthagecreance_backend.Repository.FactureRepository;
import projet.carthagecreance_backend.Repository.PaiementRepository;
import projet.carthagecreance_backend.Service.PaiementService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaiementServiceImpl implements PaiementService {

    private static final Logger logger = LoggerFactory.getLogger(PaiementServiceImpl.class);

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private FactureRepository factureRepository;

    @Override
    public Paiement createPaiement(PaiementDTO dto) {
        logger.info("Création d'un paiement pour la facture ID: {}", dto.getFactureId());

        Facture facture = factureRepository.findById(dto.getFactureId())
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + dto.getFactureId()));

        Paiement paiement = Paiement.builder()
                .facture(facture)
                .datePaiement(dto.getDatePaiement() != null ? dto.getDatePaiement() : LocalDate.now())
                .montant(dto.getMontant())
                .modePaiement(dto.getModePaiement())
                .reference(dto.getReference())
                .statut(dto.getStatut() != null ? dto.getStatut() : StatutPaiement.EN_ATTENTE)
                .commentaire(dto.getCommentaire())
                .build();

        return paiementRepository.save(paiement);
    }

    @Override
    public Optional<Paiement> getPaiementById(Long id) {
        return paiementRepository.findById(id);
    }

    @Override
    public List<Paiement> getAllPaiements() {
        return paiementRepository.findAll();
    }

    @Override
    public List<Paiement> getPaiementsByFacture(Long factureId) {
        return paiementRepository.findByFactureId(factureId);
    }

    @Override
    public List<Paiement> getPaiementsByStatut(StatutPaiement statut) {
        return paiementRepository.findByStatut(statut);
    }

    @Override
    public List<Paiement> getPaiementsByDateRange(LocalDate startDate, LocalDate endDate) {
        return paiementRepository.findByDatePaiementBetween(startDate, endDate);
    }

    @Override
    public Paiement updatePaiement(Long id, PaiementDTO dto) {
        logger.info("Mise à jour du paiement ID: {}", id);
        
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec l'ID: " + id));

        paiement.setDatePaiement(dto.getDatePaiement());
        paiement.setMontant(dto.getMontant());
        paiement.setModePaiement(dto.getModePaiement());
        paiement.setReference(dto.getReference());
        paiement.setStatut(dto.getStatut());
        paiement.setCommentaire(dto.getCommentaire());

        return paiementRepository.save(paiement);
    }

    @Override
    public void deletePaiement(Long id) {
        logger.info("Suppression du paiement ID: {}", id);
        paiementRepository.deleteById(id);
    }

    @Autowired
    private projet.carthagecreance_backend.Service.FactureService factureService;
    
    @Override
    public Paiement validerPaiement(Long id) {
        logger.info("Validation du paiement ID: {}", id);
        
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec l'ID: " + id));

        paiement.setStatut(StatutPaiement.VALIDE);
        Paiement paiementValide = paiementRepository.save(paiement);
        
        // ✅ NOUVEAU : Vérifier et mettre à jour automatiquement le statut de la facture
        if (paiement.getFacture() != null) {
            try {
                factureService.verifierEtMettreAJourStatutFacture(paiement.getFacture().getId());
                logger.info("Statut de la facture {} vérifié après validation du paiement {}", 
                        paiement.getFacture().getId(), id);
            } catch (Exception e) {
                logger.warn("Erreur lors de la vérification du statut de la facture: {}", e.getMessage());
                // Ne pas bloquer la validation du paiement en cas d'erreur
            }
        }
        
        return paiementValide;
    }

    @Override
    public Paiement refuserPaiement(Long id, String motif) {
        logger.info("Refus du paiement ID: {}", id);
        
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec l'ID: " + id));

        paiement.setStatut(StatutPaiement.REFUSE);
        paiement.setCommentaire(motif);
        return paiementRepository.save(paiement);
    }

    @Override
    public Double calculerTotalPaiementsByFacture(Long factureId) {
        Double total = paiementRepository.calculerTotalPaiementsByFacture(factureId);
        return total != null ? total : 0.0;
    }

    @Override
    public Double calculerTotalPaiementsByDateRange(LocalDate startDate, LocalDate endDate) {
        Double total = paiementRepository.calculerTotalPaiementsByDateRange(startDate, endDate);
        return total != null ? total : 0.0;
    }
}

