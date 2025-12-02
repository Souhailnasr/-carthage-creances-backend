package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité pour gérer les tarifs spécifiques par dossier avec validation
 */
@Entity
@Table(name = "tarif_dossier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifDossier implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    @JsonIgnore
    private Dossier dossier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhaseFrais phase;

    @Column(nullable = false, length = 100)
    private String categorie;  // Ex: "OUVERTURE_DOSSIER", "ENQUETE_PRECONTENTIEUSE", "EXPERTISE", "DEPLACEMENT", "DOCUMENT_HUISSIER", "ACTION_HUISSIER", "AUDIENCE"

    @Column(nullable = false, length = 200)
    private String typeElement;  // Ex: "Ouverture de dossier", "Enquête Précontentieuse", "Expertise", "Signification"

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal coutUnitaire;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantite = 1;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal montantTotal;  // Calculé : coutUnitaire × quantite

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutTarif statut = StatutTarif.EN_ATTENTE_VALIDATION;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column
    private LocalDateTime dateValidation;

    @Column(length = 1000)
    private String commentaire;

    // Relations optionnelles vers les traitements spécifiques
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_huissier_id")
    @JsonIgnore
    private DocumentHuissier documentHuissier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_huissier_id")
    @JsonIgnore
    private ActionHuissier actionHuissier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audience_id")
    @JsonIgnore
    private Audience audience;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    @JsonIgnore
    private Action action;  // Action amiable

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquete_id")
    @JsonIgnore
    private Enquette enquete;

    @PrePersist
    @PreUpdate
    private void calculateMontantTotal() {
        if (coutUnitaire != null && quantite != null) {
            montantTotal = coutUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}

