package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dossier_id", nullable = false)
    private Long dossierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", insertable = false, updatable = false)
    @JsonIgnore
    private Dossier dossier;

    @Column(name = "rule_code", length = 100, nullable = false)
    private String ruleCode; // Ex: "ESCALATE_TO_ORDONNANCE", "INITIATE_EXECUTION", etc.

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private PrioriteRecommendation priority = PrioriteRecommendation.MEDIUM;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "acknowledged")
    @Builder.Default
    private Boolean acknowledged = false;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "acknowledged_by")
    private Long acknowledgedBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

