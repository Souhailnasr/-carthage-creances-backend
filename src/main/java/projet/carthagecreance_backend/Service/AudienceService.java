package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Audience;
import projet.carthagecreance_backend.Entity.DecisionResult;
import projet.carthagecreance_backend.Entity.TribunalType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AudienceService {
    
    // CRUD Operations
    Audience createAudience(Audience audience);
    Optional<Audience> getAudienceById(Long id);
    List<Audience> getAllAudiences();
    Audience updateAudience(Long id, Audience audience);
    void deleteAudience(Long id);
    
    // Search Operations
    List<Audience> getAudiencesByDossier(Long dossierId);
    List<Audience> getAudiencesByDate(LocalDate date);
    List<Audience> getAudiencesByDateRange(LocalDate startDate, LocalDate endDate);
    List<Audience> getAudiencesByTribunalType(TribunalType tribunalType);
    List<Audience> getAudiencesByResult(DecisionResult result);
    List<Audience> getAudiencesByAvocat(Long avocatId);
    List<Audience> getAudiencesByHuissier(Long huissierId);
    List<Audience> getAudiencesByLocation(String location);
    
    // Special Operations
    List<Audience> getUpcomingAudiences();
    List<Audience> getPastAudiences();
    List<Audience> getNextAudienceByDossier(Long dossierId);
}
