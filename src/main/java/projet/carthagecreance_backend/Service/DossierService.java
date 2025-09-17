package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Urgence;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DossierService {
    
    // CRUD Operations
    Dossier createDossier(Dossier dossier);
    Optional<Dossier> getDossierById(Long id);
    List<Dossier> getAllDossiers();
    Dossier updateDossier(Long id, Dossier dossier);
    void deleteDossier(Long id);
    
    // Search Operations
    Optional<Dossier> getDossierByNumber(String numeroDossier);
    List<Dossier> getDossiersByTitle(String title);
    List<Dossier> getDossiersByDescription(String description);
    List<Dossier> getDossiersByUrgency(Urgence urgency);
    List<Dossier> getDossiersByAvocat(Long avocatId);
    List<Dossier> getDossiersByHuissier(Long huissierId);
    List<Dossier> getDossiersByCreancier(Long creancierId);
    List<Dossier> getDossiersByDebiteur(Long debiteurId);
    List<Dossier> getDossiersByUser(Long userId);
    List<Dossier> getDossiersByCreationDate(Date date);
    List<Dossier> getDossiersByCreationDateRange(Date startDate, Date endDate);
    List<Dossier> getDossiersByClosureDate(Date date);
    List<Dossier> getDossiersByAmount(Double amount);
    List<Dossier> getDossiersByAmountRange(Double minAmount, Double maxAmount);
    List<Dossier> searchDossiers(String searchTerm);
    
    // Special Operations
    List<Dossier> getOpenDossiers();
    List<Dossier> getClosedDossiers();
    List<Dossier> getRecentDossiers();
    
    // Validation Operations
    boolean existsByNumber(String numeroDossier);
}
