package projet.carthagecreance_backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_agents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PerformanceAgent implements Serializable {
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Utilisateur agent;

    @Column(name = "periode", length = 50, nullable = false)
    private String periode;

    @Column(name = "dossiers_traites")
    private Integer dossiersTraites = 0;

    @Column(name = "dossiers_valides")
    private Integer dossiersValides = 0;

    @Column(name = "enquetes_completees")
    private Integer enquetesCompletees = 0;
    @Column(name = "score")
    private Double score = 0.0;

    @Column(name = "date_calcul", nullable = false)
    private LocalDateTime dateCalcul = LocalDateTime.now();

    @Column(name = "commentaires", length = 1000)
    private String commentaires;

    @Column(name = "objectif")
    private Integer objectif;

    @Column(name = "taux_reussite")
    private Double tauxReussite;
}
