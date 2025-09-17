package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Urgence;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Service.DossierService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DossierServiceImpl implements DossierService {

    @Autowired
    private DossierRepository dossierRepository;

    @Override
    public Dossier createDossier(Dossier dossier) {
        return dossierRepository.save(dossier);
    }

    @Override
    public Optional<Dossier> getDossierById(Long id) {
        return dossierRepository.findById(id);
    }

    @Override
    public List<Dossier> getAllDossiers() {
        return dossierRepository.findAll();
    }

    @Override
    public Dossier updateDossier(Long id, Dossier dossier) {
        if (dossierRepository.existsById(id)) {
            dossier.setId(id);
            return dossierRepository.save(dossier);
        }
        throw new RuntimeException("Dossier not found with id: " + id);
    }

    @Override
    public void deleteDossier(Long id) {
        if (dossierRepository.existsById(id)) {
            dossierRepository.deleteById(id);
        } else {
            throw new RuntimeException("Dossier not found with id: " + id);
        }
    }

    @Override
    public Optional<Dossier> getDossierByNumber(String numeroDossier) {
        return dossierRepository.findByNumeroDossier(numeroDossier);
    }

    @Override
    public List<Dossier> getDossiersByTitle(String title) {
        return dossierRepository.findByTitreContainingIgnoreCase(title);
    }

    @Override
    public List<Dossier> getDossiersByDescription(String description) {
        return dossierRepository.findByDescriptionContainingIgnoreCase(description);
    }

    @Override
    public List<Dossier> getDossiersByUrgency(Urgence urgency) {
        return dossierRepository.findByUrgence(urgency);
    }

    @Override
    public List<Dossier> getDossiersByAvocat(Long avocatId) {
        return dossierRepository.findByAvocatId(avocatId);
    }

    @Override
    public List<Dossier> getDossiersByHuissier(Long huissierId) {
        return dossierRepository.findByHuissierId(huissierId);
    }

    @Override
    public List<Dossier> getDossiersByCreancier(Long creancierId) {
        return dossierRepository.findByCreancierId(creancierId);
    }

    @Override
    public List<Dossier> getDossiersByDebiteur(Long debiteurId) {
        return dossierRepository.findByDebiteurId(debiteurId);
    }

    @Override
    public List<Dossier> getDossiersByUser(Long userId) {
        return dossierRepository.findByUtilisateurId(userId);
    }

    @Override
    public List<Dossier> getDossiersByCreationDate(Date date) {
        return dossierRepository.findByDateCreation(date);
    }

    @Override
    public List<Dossier> getDossiersByCreationDateRange(Date startDate, Date endDate) {
        return dossierRepository.findByDateCreationBetween(startDate, endDate);
    }

    @Override
    public List<Dossier> getDossiersByClosureDate(Date date) {
        return dossierRepository.findByDateCloture(date);
    }

    @Override
    public List<Dossier> getDossiersByAmount(Double amount) {
        return dossierRepository.findByMontantCreance(amount);
    }

    @Override
    public List<Dossier> getDossiersByAmountRange(Double minAmount, Double maxAmount) {
        return dossierRepository.findByMontantCreanceBetween(minAmount, maxAmount);
    }

    @Override
    public List<Dossier> searchDossiers(String searchTerm) {
        return dossierRepository.findByTitreOuDescriptionContaining(searchTerm);
    }

    @Override
    public List<Dossier> getOpenDossiers() {
        return dossierRepository.findDossiersOuverts();
    }

    @Override
    public List<Dossier> getClosedDossiers() {
        return dossierRepository.findDossiersFermes();
    }

    @Override
    public List<Dossier> getRecentDossiers() {
        // Derniers 30 jours
        long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
        Date thirtyDaysAgo = new Date(System.currentTimeMillis() - thirtyDaysInMillis);
        return dossierRepository.findDossiersRecents(thirtyDaysAgo);
    }

    @Override
    public boolean existsByNumber(String numeroDossier) {
        return dossierRepository.existsByNumeroDossier(numeroDossier);
    }
}
