package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Finance;
import projet.carthagecreance_backend.Repository.FinanceRepository;
import projet.carthagecreance_backend.Service.FinanceService;
import projet.carthagecreance_backend.Service.CoutCalculationService;
import projet.carthagecreance_backend.Mapper.FinanceMapper;
import projet.carthagecreance_backend.DTO.FinanceDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired
    private FinanceRepository financeRepository;
    
    @Autowired
    private CoutCalculationService coutCalculationService;
    
    @Autowired
    private FinanceMapper financeMapper;

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
    
    @Override
    public Double calculerFactureFinale(Long dossierId) {
        Optional<Finance> financeOpt = financeRepository.findByDossierId(dossierId);
        if (financeOpt.isEmpty()) {
            throw new RuntimeException("Finance non trouvée pour le dossier ID: " + dossierId);
        }
        return financeOpt.get().calculerFactureFinale();
    }
    
    @Override
    public java.util.Map<String, Object> getDetailFacture(Long dossierId) {
        Optional<Finance> financeOpt = financeRepository.findByDossierId(dossierId);
        if (financeOpt.isEmpty()) {
            throw new RuntimeException("Finance non trouvée pour le dossier ID: " + dossierId);
        }
        Finance finance = financeOpt.get();
        
        java.util.Map<String, Object> detail = new java.util.HashMap<>();
        detail.put("fraisCreationDossier", finance.getFraisCreationDossier() != null ? finance.getFraisCreationDossier() : 0.0);
        detail.put("coutGestionTotal", finance.calculerCoutGestionTotal());
        detail.put("coutActionsAmiable", finance.getCoutActionsAmiable() != null ? finance.getCoutActionsAmiable() : 0.0);
        detail.put("coutActionsJuridique", finance.getCoutActionsJuridique() != null ? finance.getCoutActionsJuridique() : 0.0);
        detail.put("fraisAvocat", finance.getFraisAvocat() != null ? finance.getFraisAvocat() : 0.0);
        detail.put("fraisHuissier", finance.getFraisHuissier() != null ? finance.getFraisHuissier() : 0.0);
        detail.put("totalFacture", finance.calculerFactureFinale());
        
        return detail;
    }
    
    @Override
    public Finance recalculerCoutsDossier(Long dossierId) {
        // Utiliser le service dédié pour recalculer tous les coûts
        return coutCalculationService.recalculerTousLesCouts(dossierId);
    }
    
    @Override
    public java.util.Map<String, Object> getStatistiquesCouts() {
        List<Finance> allFinances = financeRepository.findAll();
        
        double totalFraisCreation = allFinances.stream()
                .mapToDouble(f -> f.getFraisCreationDossier() != null ? f.getFraisCreationDossier() : 0.0)
                .sum();
        
        double totalFraisGestion = allFinances.stream()
                .mapToDouble(Finance::calculerCoutGestionTotal)
                .sum();
        
        double totalActionsAmiable = allFinances.stream()
                .mapToDouble(f -> f.getCoutActionsAmiable() != null ? f.getCoutActionsAmiable() : 0.0)
                .sum();
        
        double totalActionsJuridique = allFinances.stream()
                .mapToDouble(f -> f.getCoutActionsJuridique() != null ? f.getCoutActionsJuridique() : 0.0)
                .sum();
        
        double totalAvocat = calculateTotalAvocatFees();
        double totalHuissier = calculateTotalHuissierFees();
        double grandTotal = totalFraisCreation + totalFraisGestion + totalActionsAmiable + 
                           totalActionsJuridique + totalAvocat + totalHuissier;
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalFraisCreation", totalFraisCreation);
        stats.put("totalFraisGestion", totalFraisGestion);
        stats.put("totalActionsAmiable", totalActionsAmiable);
        stats.put("totalActionsJuridique", totalActionsJuridique);
        stats.put("totalAvocat", totalAvocat);
        stats.put("totalHuissier", totalHuissier);
        stats.put("grandTotal", grandTotal);
        
        return stats;
    }
    
    @Override
    public java.util.Map<String, Object> getCoutsParDossier(Long dossierId) {
        return getDetailFacture(dossierId);
    }
    
    @Override
    public org.springframework.data.domain.Page<Finance> getDossiersAvecCouts(int page, int size, String sort) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, 
                org.springframework.data.domain.Sort.by(sort != null ? sort : "dateOperation").descending()
        );
        // ✅ Utiliser la méthode avec EntityGraph pour charger la relation Dossier
        return financeRepository.findAllWithDossier(pageable);
    }
    
    /**
     * ✅ NOUVELLE MÉTHODE : Retourne les dossiers avec coûts en DTO (avec dossierId)
     * Utilisée par le frontend pour avoir accès au dossierId
     */
    @Override
    public org.springframework.data.domain.Page<FinanceDTO> getDossiersAvecCoutsDTO(int page, int size, String sort) {
        org.springframework.data.domain.Page<Finance> finances = getDossiersAvecCouts(page, size, sort);
        return financeMapper.toDTOPage(finances);
    }
    
    @Override
    public List<Finance> getFacturesEnAttente() {
        return financeRepository.findAll().stream()
                .filter(f -> !Boolean.TRUE.equals(f.getFactureFinalisee()))
                .filter(f -> f.getDossier() != null && 
                           f.getDossier().getDossierStatus() != null &&
                           f.getDossier().getDossierStatus().name().equals("CLOTURE"))
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public Finance finaliserFacture(Long dossierId) {
        Optional<Finance> financeOpt = financeRepository.findByDossierId(dossierId);
        if (financeOpt.isEmpty()) {
            throw new RuntimeException("Finance non trouvée pour le dossier ID: " + dossierId);
        }
        Finance finance = financeOpt.get();
        finance.setFactureFinalisee(true);
        finance.setDateFacturation(LocalDate.now());
        return financeRepository.save(finance);
    }
}
