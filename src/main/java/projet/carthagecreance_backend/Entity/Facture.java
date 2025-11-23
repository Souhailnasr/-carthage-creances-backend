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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numeroFacture;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dossier_id")
    @JsonIgnore
    private Dossier dossier;

    private LocalDate periodeDebut;
    private LocalDate periodeFin;

    @Builder.Default
    private LocalDate dateEmission = LocalDate.now();

    private LocalDate dateEcheance;

    @Builder.Default
    private Double montantHT = 0.0;

    @Builder.Default
    private Double montantTTC = 0.0;

    @Builder.Default
    private Double tva = 0.0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FactureStatut statut = FactureStatut.BROUILLON;

    private String pdfUrl;

    @Builder.Default
    private Boolean envoyee = false;

    @Builder.Default
    private Boolean relanceEnvoyee = false;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<FluxFrais> fluxFrais = new ArrayList<>();

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Paiement> paiements = new ArrayList<>();

    public void ajouterFluxFrais(FluxFrais frais) {
        frais.setFacture(this);
        this.fluxFrais.add(frais);
    }

    public void ajouterPaiement(Paiement paiement) {
        paiement.setFacture(this);
        this.paiements.add(paiement);
    }
}

