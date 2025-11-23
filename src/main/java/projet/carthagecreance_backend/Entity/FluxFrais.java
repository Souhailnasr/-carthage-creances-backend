package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "flux_frais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FluxFrais implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhaseFrais phase;

    @Column(nullable = false)
    private String categorie;

    @Builder.Default
    private Integer quantite = 1;

    private Double tarifUnitaire;

    private Double montant;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutFrais statut = StatutFrais.EN_ATTENTE;

    @Builder.Default
    private LocalDate dateAction = LocalDate.now();

    private String justificatifUrl;

    @Column(length = 1000)
    private String commentaire;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dossier_id")
    @JsonIgnore
    private Dossier dossier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    @JsonIgnore
    private Action action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquete_id")
    @JsonIgnore
    private Enquette enquette;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audience_id")
    @JsonIgnore
    private Audience audience;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avocat_id")
    @JsonIgnore
    private Avocat avocat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "huissier_id")
    @JsonIgnore
    private Huissier huissier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id")
    @JsonIgnore
    private Facture facture;

    @PrePersist
    @PreUpdate
    private void prePersist() {
        if (quantite == null) {
            quantite = 1;
        }
        if (dateAction == null) {
            dateAction = LocalDate.now();
        }
        if (statut == null) {
            statut = StatutFrais.EN_ATTENTE;
        }
        if (montant == null && tarifUnitaire != null) {
            montant = quantite * tarifUnitaire;
        }
    }
}

