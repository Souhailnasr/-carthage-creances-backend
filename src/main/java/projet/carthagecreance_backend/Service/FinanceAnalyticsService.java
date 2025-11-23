package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.DTO.FinanceAlertDTO;
import projet.carthagecreance_backend.DTO.FinanceStatsDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FinanceAnalyticsService {
    FinanceStatsDTO getDashboardStats();
    FinanceStatsDTO getStatsByDateRange(LocalDate startDate, LocalDate endDate);
    List<FinanceAlertDTO> getAlerts(String niveau, String phase);
    List<FinanceAlertDTO> getAlertsByDossier(Long dossierId);
    Map<String, Object> getRepartitionFrais();
    Map<String, Object> getEvolutionMensuelle(LocalDate startDate, LocalDate endDate);
    List<Map<String, Object>> getAgentRoiClassement();
    Map<String, Object> getStatistiquesDossier(Long dossierId);
    Double calculerRoiAgent(Long agentId);
    List<Map<String, Object>> getInsights();
    void marquerInsightTraite(Long insightId);
    
    // Export Excel
    byte[] exporterRapportExcel(String typeRapport, LocalDate startDate, LocalDate endDate, Map<String, Object> filtres);
}

