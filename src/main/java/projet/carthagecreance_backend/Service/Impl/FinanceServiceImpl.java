package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Finance;
import projet.carthagecreance_backend.Repository.FinanceRepository;
import projet.carthagecreance_backend.Service.FinanceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired
    private FinanceRepository financeRepository;

    @Override
    public Finance createFinance(Finance finance) {
        return financeRepository.save(finance);
    }

    @Override
    public Optional<Finance> getFinanceById(Long id) {
        return financeRepository.findById(id);
    }

    @Override
    public List<Finance> getAllFinances() {
        return financeRepository.findAll();
    }

    @Override
    public Finance updateFinance(Long id, Finance finance) {
        if (financeRepository.existsById(id)) {
            finance.setId(id);
            return financeRepository.save(finance);
        }
        throw new RuntimeException("Finance not found with id: " + id);
    }

    @Override
    public void deleteFinance(Long id) {
        if (financeRepository.existsById(id)) {
            financeRepository.deleteById(id);
        } else {
            throw new RuntimeException("Finance not found with id: " + id);
        }
    }

    @Override
    public Optional<Finance> getFinanceByDossier(Long dossierId) {
        return financeRepository.findByDossierId(dossierId);
    }

    @Override
    public List<Finance> getFinancesByCurrency(String currency) {
        return financeRepository.findByDevise(currency);
    }

    @Override
    public List<Finance> getFinancesByOperationDate(LocalDate date) {
        return financeRepository.findByDateOperation(date);
    }

    @Override
    public List<Finance> getFinancesByOperationDateRange(LocalDate startDate, LocalDate endDate) {
        return financeRepository.findByDateOperationBetween(startDate, endDate);
    }

    @Override
    public List<Finance> getFinancesByDescription(String description) {
        return financeRepository.findByDescriptionContainingIgnoreCase(description);
    }

    @Override
    public List<Finance> getFinancesWithAvocatFees() {
        return financeRepository.findFinancesAvecFraisAvocat();
    }

    @Override
    public List<Finance> getFinancesWithHuissierFees() {
        return financeRepository.findFinancesAvecFraisHuissier();
    }

    @Override
    public List<Finance> getFinancesWithActions() {
        return financeRepository.findFinancesAvecActions();
    }

    @Override
    public List<Finance> getRecentFinances() {
        return financeRepository.findFinancesRecentes(LocalDate.now().minusDays(30));
    }

    @Override
    public Double calculateTotalAvocatFees() {
        return financeRepository.calculerTotalFraisAvocat();
    }

    @Override
    public Double calculateTotalHuissierFees() {
        return financeRepository.calculerTotalFraisHuissier();
    }

    @Override
    public Double calculateTotalGlobalFees() {
        return financeRepository.calculerTotalGlobalFrais();
    }

    @Override
    public Double calculateTotalFeesByCurrency(String currency) {
        // Implémentation basique - à étendre selon les besoins
        return getFinancesByCurrency(currency).stream()
                .mapToDouble(f -> (f.getFraisAvocat() != null ? f.getFraisAvocat() : 0.0) + 
                                 (f.getFraisHuissier() != null ? f.getFraisHuissier() : 0.0))
                .sum();
    }

    @Override
    public Double calculateTotalActionCosts(Long financeId) {
        return financeRepository.calculerCoutTotalActions(financeId);
    }
}
