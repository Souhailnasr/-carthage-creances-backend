package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.DTO.PaiementDTO;
import projet.carthagecreance_backend.Entity.Paiement;
import projet.carthagecreance_backend.Entity.StatutPaiement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaiementService {
    Paiement createPaiement(PaiementDTO dto);
    Optional<Paiement> getPaiementById(Long id);
    List<Paiement> getAllPaiements();
    List<Paiement> getPaiementsByFacture(Long factureId);
    List<Paiement> getPaiementsByStatut(StatutPaiement statut);
    List<Paiement> getPaiementsByDateRange(LocalDate startDate, LocalDate endDate);
    Paiement updatePaiement(Long id, PaiementDTO dto);
    void deletePaiement(Long id);
    Paiement validerPaiement(Long id);
    Paiement refuserPaiement(Long id, String motif);
    Double calculerTotalPaiementsByFacture(Long factureId);
    Double calculerTotalPaiementsByDateRange(LocalDate startDate, LocalDate endDate);
}

