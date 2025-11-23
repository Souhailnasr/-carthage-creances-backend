package projet.carthagecreance_backend.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "tarifs_catalogue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifCatalogue implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhaseFrais phase;

    @Column(nullable = false)
    private String categorie;

    private String description;

    private String fournisseur;

    @Column(nullable = false)
    private Double tarifUnitaire;

    @Builder.Default
    private String devise = "TND";

    @Builder.Default
    private LocalDate dateDebut = LocalDate.now();

    private LocalDate dateFin;

    @Builder.Default
    private Boolean actif = true;
}

