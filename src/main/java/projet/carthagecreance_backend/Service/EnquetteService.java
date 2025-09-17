package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Enquette;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EnquetteService {
    
    // CRUD Operations
    Enquette createEnquette(Enquette enquette);
    Optional<Enquette> getEnquetteById(Long id);
    List<Enquette> getAllEnquettes();
    Enquette updateEnquette(Long id, Enquette enquette);
    void deleteEnquette(Long id);
    
    // Search Operations
    Optional<Enquette> getEnquetteByDossier(Long dossierId);
    List<Enquette> getEnquettesByCreationDate(LocalDate date);
    List<Enquette> getEnquettesByCreationDateRange(LocalDate startDate, LocalDate endDate);
    List<Enquette> getEnquettesBySector(String sector);
    List<Enquette> getEnquettesByLegalForm(String legalForm);
    List<Enquette> getEnquettesByPDG(String pdg);
    List<Enquette> getEnquettesByCapitalRange(Double minCapital, Double maxCapital);
    List<Enquette> getEnquettesByRevenueRange(Double minRevenue, Double maxRevenue);
    List<Enquette> getEnquettesByStaffRange(Integer minStaff, Integer maxStaff);
    List<Enquette> getEnquettesWithRealEstate();
    List<Enquette> getEnquettesWithMovableProperty();
    List<Enquette> getEnquettesWithObservations();
}
