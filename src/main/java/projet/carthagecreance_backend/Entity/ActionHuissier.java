package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "actions_huissier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionHuissier implements Serializable {
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

    @Enumerated(EnumType.STRING)
    @Column(name = "type_action", nullable = false)
    private TypeActionHuissier typeAction;

    @Column(name = "montant_recouvre", precision = 19, scale = 2)
    private BigDecimal montantRecouvre;

    @Column(name = "montant_restant", precision = 19, scale = 2)
    private BigDecimal montantRestant;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_dossier")
    private EtatDossier etatDossier;

    @Column(name = "date_action", nullable = false)
    private Instant dateAction;

    @Column(name = "piece_jointe_url", length = 500)
    private String pieceJointeUrl;

    @Column(name = "huissier_name", length = 255)
    private String huissierName;

    @PrePersist
    protected void onCreate() {
        if (dateAction == null) {
            dateAction = Instant.now();
        }
    }
}

