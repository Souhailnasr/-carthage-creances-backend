package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.DTO.FactureDTO;
import projet.carthagecreance_backend.Entity.Facture;
import projet.carthagecreance_backend.Entity.FactureStatut;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FactureService {
    Facture createFacture(FactureDTO dto);
    Optional<Facture> getFactureById(Long id);
    Optional<Facture> getFactureByNumero(String numero);
    List<Facture> getAllFactures();
    List<Facture> getFacturesByDossier(Long dossierId);
    List<Facture> getFacturesByStatut(FactureStatut statut);
    List<Facture> getFacturesEnRetard();
    Facture genererFactureAutomatique(Long dossierId, LocalDate periodeDebut, LocalDate periodeFin);
    Facture finaliserFacture(Long id);
    Facture envoyerFacture(Long id);
    Facture relancerFacture(Long id);
    byte[] genererPdfFacture(Long id);
    String genererNumeroFacture();
    Facture updateFacture(Long id, FactureDTO dto);
    void deleteFacture(Long id);
    Double calculerMontantHT(List<Long> tarifDossierIds);
    Double calculerMontantTTC(Double montantHT, Double tauxTVA);
}

