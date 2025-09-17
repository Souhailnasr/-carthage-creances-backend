package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Audience;
import projet.carthagecreance_backend.Entity.DecisionResult;
import projet.carthagecreance_backend.Entity.TribunalType;
import projet.carthagecreance_backend.Repository.AudienceRepository;
import projet.carthagecreance_backend.Service.AudienceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AudienceServiceImpl implements AudienceService {

    @Autowired
    private AudienceRepository audienceRepository;

    @Override
    public Audience createAudience(Audience audience) {
        return audienceRepository.save(audience);
    }

    @Override
    public Optional<Audience> getAudienceById(Long id) {
        return audienceRepository.findById(id);
    }

    @Override
    public List<Audience> getAllAudiences() {
        return audienceRepository.findAll();
    }

    @Override
    public Audience updateAudience(Long id, Audience audience) {
        if (audienceRepository.existsById(id)) {
            audience.setId(id);
            return audienceRepository.save(audience);
        }
        throw new RuntimeException("Audience not found with id: " + id);
    }

    @Override
    public void deleteAudience(Long id) {
        if (audienceRepository.existsById(id)) {
            audienceRepository.deleteById(id);
        } else {
            throw new RuntimeException("Audience not found with id: " + id);
        }
    }

    @Override
    public List<Audience> getAudiencesByDossier(Long dossierId) {
        return audienceRepository.findByDossierId(dossierId);
    }

    @Override
    public List<Audience> getAudiencesByDate(LocalDate date) {
        return audienceRepository.findByDateAudience(date);
    }

    @Override
    public List<Audience> getAudiencesByDateRange(LocalDate startDate, LocalDate endDate) {
        return audienceRepository.findByDateAudienceBetween(startDate, endDate);
    }

    @Override
    public List<Audience> getAudiencesByTribunalType(TribunalType tribunalType) {
        return audienceRepository.findByTribunalType(tribunalType);
    }

    @Override
    public List<Audience> getAudiencesByResult(DecisionResult result) {
        return audienceRepository.findByResultat(result);
    }

    @Override
    public List<Audience> getAudiencesByAvocat(Long avocatId) {
        return audienceRepository.findByAvocatId(avocatId);
    }

    @Override
    public List<Audience> getAudiencesByHuissier(Long huissierId) {
        return audienceRepository.findByHuissierId(huissierId);
    }

    @Override
    public List<Audience> getAudiencesByLocation(String location) {
        return audienceRepository.findByLieuTribunalContainingIgnoreCase(location);
    }

    @Override
    public List<Audience> getUpcomingAudiences() {
        return audienceRepository.findAudiencesAVenir(LocalDate.now());
    }

    @Override
    public List<Audience> getPastAudiences() {
        return audienceRepository.findAudiencesPassees(LocalDate.now());
    }

    @Override
    public List<Audience> getNextAudienceByDossier(Long dossierId) {
        return audienceRepository.findProchaineAudienceParDossier(dossierId, LocalDate.now());
    }
}
