package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.PrioriteRecommendation;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationDTO {
    private Long id;
    private Long dossierId;
    private String ruleCode;
    private String title;
    private String description;
    private PrioriteRecommendation priority;
    private Instant createdAt;
    private Boolean acknowledged;
    private Instant acknowledgedAt;
    private Long acknowledgedBy;
}
