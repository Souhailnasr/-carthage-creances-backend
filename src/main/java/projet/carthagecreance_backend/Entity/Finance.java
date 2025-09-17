package projet.carthagecreance_backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Finance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String devise;
    private LocalDate dateOperation;
    private String description;

    // ✅ Frais Avocat & Huissier
    private Double fraisAvocat;
    private Double fraisHuissier;

    // ✅ Relation avec Dossier
    @OneToOne
    @JoinColumn(name = "dossier_id")
    private Dossier dossier;

    // ✅ Liste des Actions liées à cette Finance
    @OneToMany(mappedBy = "finance", cascade = CascadeType.ALL)
    private List<Action> actions;

    // ✅ Calcul total des frais des actions
    public Double calculerTotalActions() {
        if (actions == null || actions.isEmpty()) return 0.0;
        return actions.stream()
                .mapToDouble(Action::getTotalCout)
                .sum();
    }

    // ✅ Calcul total global (Actions + Avocat + Huissier)
    public Double calculerTotalGlobal() {
        double totalActions = calculerTotalActions();
        double totalAvocat = (fraisAvocat != null ? fraisAvocat : 0.0);
        double totalHuissier = (fraisHuissier != null ? fraisHuissier : 0.0);
        return totalActions + totalAvocat + totalHuissier;
    }
}

