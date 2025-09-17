package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Enquette;
import projet.carthagecreance_backend.Repository.EnquetteRepository;
import projet.carthagecreance_backend.Service.EnquetteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EnquetteServiceImpl implements EnquetteService {

    @Autowired
    private EnquetteRepository enquetteRepository;

    @Override
    public Enquette createEnquette(Enquette enquette) {
        return enquetteRepository.save(enquette);
    }

    @Override
    public Optional<Enquette> getEnquetteById(Long id) {
        return enquetteRepository.findById(id);
    }

    @Override
    public List<Enquette> getAllEnquettes() {
        return enquetteRepository.findAll();
    }

    @Override
    public Enquette updateEnquette(Long id, Enquette enquette) {
        if (enquetteRepository.existsById(id)) {
            enquette.setId(id);
            return enquetteRepository.save(enquette);
        }
        throw new RuntimeException("Enquette not found with id: " + id);
    }

    @Override
    public void deleteEnquette(Long id) {
        if (enquetteRepository.existsById(id)) {
            enquetteRepository.deleteById(id);
        } else {
            throw new RuntimeException("Enquette not found with id: " + id);
        }
    }

    @Override
    public Optional<Enquette> getEnquetteByDossier(Long dossierId) {
        return enquetteRepository.findByDossierId(dossierId);
    }

    @Override
    public List<Enquette> getEnquettesByCreationDate(LocalDate date) {
        return enquetteRepository.findByDateCreation(date);
    }

    @Override
    public List<Enquette> getEnquettesByCreationDateRange(LocalDate startDate, LocalDate endDate) {
        return enquetteRepository.findByDateCreationBetween(startDate, endDate);
    }

    @Override
    public List<Enquette> getEnquettesBySector(String sector) {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getSecteurActivite() != null && 
                           e.getSecteurActivite().toLowerCase().contains(sector.toLowerCase()))
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesByLegalForm(String legalForm) {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getFormeJuridique() != null && 
                           e.getFormeJuridique().toLowerCase().contains(legalForm.toLowerCase()))
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesByPDG(String pdg) {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getPdg() != null && 
                           e.getPdg().toLowerCase().contains(pdg.toLowerCase()))
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesByCapitalRange(Double minCapital, Double maxCapital) {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getCapital() != null && 
                           e.getCapital() >= minCapital && e.getCapital() <= maxCapital)
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesByRevenueRange(Double minRevenue, Double maxRevenue) {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getChiffreAffaire() != null && 
                           e.getChiffreAffaire() >= minRevenue && e.getChiffreAffaire() <= maxRevenue)
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesByStaffRange(Integer minStaff, Integer maxStaff) {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getEffectif() != null && 
                           e.getEffectif() >= minStaff && e.getEffectif() <= maxStaff)
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesWithRealEstate() {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getBienImmobilier() != null && !e.getBienImmobilier().isEmpty())
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesWithMovableProperty() {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getBienMobilier() != null && !e.getBienMobilier().isEmpty())
                .toList();
    }

    @Override
    public List<Enquette> getEnquettesWithObservations() {
        // Implémentation basique - à étendre selon les besoins
        return getAllEnquettes().stream()
                .filter(e -> e.getObservations() != null && !e.getObservations().isEmpty())
                .toList();
    }
}
