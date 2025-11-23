package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.DTO.TarifCatalogueDTO;
import projet.carthagecreance_backend.Entity.PhaseFrais;
import projet.carthagecreance_backend.Entity.TarifCatalogue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TarifCatalogueService {
    TarifCatalogue createTarif(TarifCatalogueDTO dto);
    Optional<TarifCatalogue> getTarifById(Long id);
    List<TarifCatalogue> getAllTarifs();
    List<TarifCatalogue> getTarifsActifs();
    List<TarifCatalogue> getTarifsByPhase(PhaseFrais phase);
    List<TarifCatalogue> getTarifsByCategorie(String categorie);
    Optional<TarifCatalogue> getTarifActifByPhaseAndCategorie(PhaseFrais phase, String categorie, LocalDate date);
    TarifCatalogue updateTarif(Long id, TarifCatalogueDTO dto);
    void deleteTarif(Long id);
    void desactiverTarif(Long id);
    List<TarifCatalogue> getHistoriqueTarif(Long id);
}

