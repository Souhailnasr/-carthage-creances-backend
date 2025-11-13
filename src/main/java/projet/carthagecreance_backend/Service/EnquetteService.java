package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Enquette;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EnquetteService {
    
    // CRUD Operations
    Enquette createEnquette(Enquette enquette);
    Optional<Enquette> getEnquetteById(Long id);
    
    /**
     * Vérifie si une enquête existe par son ID (utilise une requête native pour éviter les problèmes avec dossier_id = NULL)
     * @param id L'ID de l'enquête
     * @return true si l'enquête existe, false sinon
     */
    boolean existsById(Long id);
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
    
    // Validation Operations
    void validerEnquette(Long enquetteId, Long chefId);
    void rejeterEnquette(Long enquetteId, String commentaire);
    
    // Statistics Operations
    long countTotalEnquettes();
    long countEnquettesByStatut(projet.carthagecreance_backend.Entity.Statut statut);
    long countEnquettesValides();
    long countEnquettesNonValides();
    long countEnquettesCreesCeMois();
    long countEnquettesByAgentCreateur(Long agentId);
    long countEnquettesByAgentResponsable(Long agentId);
}
