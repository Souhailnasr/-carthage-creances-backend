package projet.carthagecreance_backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audience implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateAudience;
    private LocalDate dateProchaine; // prochaine audience si reportée

    @Enumerated(EnumType.STRING)
    private TribunalType tribunalType;

    private String lieuTribunal;

    private String commentaireDecision;

    @Enumerated(EnumType.STRING)
    private DecisionResult resultat;

    @ManyToOne
    @JoinColumn(name = "dossier_id")
    private Dossier dossier;

    @ManyToOne
    @JoinColumn(name = "avocat_id")
    private Avocat avocat;
    @ManyToOne
    @JoinColumn(name = "hussier_id")
    private Huissier huissier;
}

