package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Action implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeAction type;  // Exemple: RELANCE, SAISIE, MISE_EN_DEMEURE

    private LocalDate dateAction;

    // Nombre de fois que l’action a été effectuée
    private Integer nbOccurrences;

    // Coût unitaire de l’action
    private Double coutUnitaire;

    // Relation avec le Dossier
    @ManyToOne
    @JoinColumn(name = "dossier_id")
    @JsonIgnore // Évite la récursion infinie
    private Dossier dossier;

    // Relation avec Finance (pour le regroupement des frais)
    @ManyToOne
    @JoinColumn(name = "finance_id")
    @JsonIgnore // Évite la récursion infinie
    private Finance finance;

    // ✅ Méthode utilitaire pour calculer le coût total de cette action
    public Double getTotalCout() {
        if (nbOccurrences == null || coutUnitaire == null) return 0.0;
        return nbOccurrences * coutUnitaire;
    }
}

