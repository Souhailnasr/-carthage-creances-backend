package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceStatsDTO {
    private Double totalFraisEngages;
    private Double montantRecouvre;
    private Double fraisRecuperes;
    private Double netGenere;
    private List<RepartitionFraisDTO> repartitionFrais;
    private List<EvolutionMensuelleDTO> evolutionMensuelle;
    private List<AgentRoiDTO> agentRoi;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RepartitionFraisDTO {
        private String categorie;
        private Double montant;
        private Double pourcentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EvolutionMensuelleDTO {
        private String mois;
        private Double frais;
        private Double recouvre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgentRoiDTO {
        private Long agentId;
        private String agentNom;
        private Double montantRecouvre;
        private Double fraisEngages;
        private Double roiPourcentage;
    }
}

