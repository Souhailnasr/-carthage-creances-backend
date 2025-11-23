package projet.carthagecreance_backend.Service.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.TarifCatalogueDTO;
import projet.carthagecreance_backend.Entity.PhaseFrais;
import projet.carthagecreance_backend.Entity.TarifCatalogue;
import projet.carthagecreance_backend.Repository.TarifCatalogueRepository;
import projet.carthagecreance_backend.Service.TarifCatalogueService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TarifCatalogueServiceImpl implements TarifCatalogueService {

    private static final Logger logger = LoggerFactory.getLogger(TarifCatalogueServiceImpl.class);

    @Autowired
    private TarifCatalogueRepository tarifRepository;

    @Override
    public TarifCatalogue createTarif(TarifCatalogueDTO dto) {
        logger.info("Création d'un nouveau tarif: phase={}, categorie={}", dto.getPhase(), dto.getCategorie());
        
        TarifCatalogue tarif = TarifCatalogue.builder()
                .phase(dto.getPhase())
                .categorie(dto.getCategorie())
                .description(dto.getDescription())
                .fournisseur(dto.getFournisseur())
                .tarifUnitaire(dto.getTarifUnitaire())
                .devise(dto.getDevise() != null ? dto.getDevise() : "TND")
                .dateDebut(dto.getDateDebut() != null ? dto.getDateDebut() : LocalDate.now())
                .dateFin(dto.getDateFin())
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .build();
        
        return tarifRepository.save(tarif);
    }

    @Override
    public Optional<TarifCatalogue> getTarifById(Long id) {
        return tarifRepository.findById(id);
    }

    @Override
    public List<TarifCatalogue> getAllTarifs() {
        return tarifRepository.findAll();
    }

    @Override
    public List<TarifCatalogue> getTarifsActifs() {
        return tarifRepository.findByActifTrue();
    }

    @Override
    public List<TarifCatalogue> getTarifsByPhase(PhaseFrais phase) {
        return tarifRepository.findByPhaseAndActifTrue(phase);
    }

    @Override
    public List<TarifCatalogue> getTarifsByCategorie(String categorie) {
        return tarifRepository.findByCategorie(categorie);
    }

    @Override
    public Optional<TarifCatalogue> getTarifActifByPhaseAndCategorie(PhaseFrais phase, String categorie, LocalDate date) {
        return tarifRepository.findTarifActifByPhaseAndCategorie(phase, categorie, date != null ? date : LocalDate.now());
    }

    @Override
    public TarifCatalogue updateTarif(Long id, TarifCatalogueDTO dto) {
        logger.info("Mise à jour du tarif ID: {}", id);
        
        TarifCatalogue tarif = tarifRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarif non trouvé avec l'ID: " + id));
        
        tarif.setPhase(dto.getPhase());
        tarif.setCategorie(dto.getCategorie());
        tarif.setDescription(dto.getDescription());
        tarif.setFournisseur(dto.getFournisseur());
        tarif.setTarifUnitaire(dto.getTarifUnitaire());
        tarif.setDevise(dto.getDevise());
        tarif.setDateDebut(dto.getDateDebut());
        tarif.setDateFin(dto.getDateFin());
        tarif.setActif(dto.getActif());
        
        return tarifRepository.save(tarif);
    }

    @Override
    public void deleteTarif(Long id) {
        logger.info("Suppression du tarif ID: {}", id);
        tarifRepository.deleteById(id);
    }

    @Override
    public void desactiverTarif(Long id) {
        logger.info("Désactivation du tarif ID: {}", id);
        TarifCatalogue tarif = tarifRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarif non trouvé avec l'ID: " + id));
        tarif.setActif(false);
        tarifRepository.save(tarif);
    }

    @Override
    public List<TarifCatalogue> getHistoriqueTarif(Long id) {
        return tarifRepository.findHistoriqueByTarifId(id);
    }
}

