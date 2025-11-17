package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Data
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    @JsonIgnore // Évite la récursion infinie lors de la sérialisation
    private Dossier dossier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avocat_id", nullable = true)
    private Avocat avocat;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hussier_id", nullable = true)
    private Huissier huissier;
}

