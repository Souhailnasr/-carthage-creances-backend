package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.DTO.FluxFraisDTO;
import projet.carthagecreance_backend.DTO.ValidationFraisDTO;
import projet.carthagecreance_backend.Entity.FluxFrais;
import projet.carthagecreance_backend.Entity.PhaseFrais;
import projet.carthagecreance_backend.Entity.StatutFrais;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FluxFraisService {
    FluxFrais createFluxFrais(FluxFraisDTO dto);
    Optional<FluxFrais> getFluxFraisById(Long id);
    List<FluxFrais> getAllFluxFrais();
    List<FluxFrais> getFluxFraisByDossier(Long dossierId);
    List<FluxFrais> getFluxFraisByStatut(StatutFrais statut);
    List<FluxFrais> getFluxFraisEnAttente();
    List<FluxFrais> getFluxFraisByPhase(PhaseFrais phase);
    List<FluxFrais> getFluxFraisByDateRange(LocalDate startDate, LocalDate endDate);
    FluxFrais updateFluxFrais(Long id, FluxFraisDTO dto);
    void deleteFluxFrais(Long id);
    FluxFrais validerFrais(Long id, ValidationFraisDTO dto);
    FluxFrais rejeterFrais(Long id, ValidationFraisDTO dto);
    FluxFrais creerFraisDepuisAction(Long actionId);
    FluxFrais creerFraisDepuisEnquete(Long enqueteId);
    FluxFrais creerFraisDepuisAudience(Long audienceId);
    Double calculerTotalFraisByDossier(Long dossierId);
    Double calculerTotalFraisByStatut(StatutFrais statut);
    
    // Import CSV
    Map<String, Object> importerFraisDepuisCSV(byte[] csvContent);
}

