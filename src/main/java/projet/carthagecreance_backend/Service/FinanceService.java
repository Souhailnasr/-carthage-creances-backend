package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Finance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FinanceService {
    
    // CRUD Operations
    Finance createFinance(Finance finance);
    Optional<Finance> getFinanceById(Long id);
    List<Finance> getAllFinances();
    Finance updateFinance(Long id, Finance finance);
    void deleteFinance(Long id);
    
    // Search Operations
    Optional<Finance> getFinanceByDossier(Long dossierId);
    List<Finance> getFinancesByCurrency(String currency);
    List<Finance> getFinancesByOperationDate(LocalDate date);
    List<Finance> getFinancesByOperationDateRange(LocalDate startDate, LocalDate endDate);
    List<Finance> getFinancesByDescription(String description);
    List<Finance> getFinancesWithAvocatFees();
    List<Finance> getFinancesWithHuissierFees();
    List<Finance> getFinancesWithActions();
    List<Finance> getRecentFinances();
    
    // Calculated Operations
    Double calculateTotalAvocatFees();
    Double calculateTotalHuissierFees();
    Double calculateTotalGlobalFees();
    Double calculateTotalFeesByCurrency(String currency);
    Double calculateTotalActionCosts(Long financeId);
}
