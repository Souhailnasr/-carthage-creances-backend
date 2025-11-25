package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents_huissier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentHuissier implements Serializable {
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
    @Column(name = "type_document", nullable = false)
    private TypeDocumentHuissier typeDocument;

    @Column(name = "date_creation", nullable = false)
    private Instant dateCreation;

    @Column(name = "delai_legal_days", nullable = false)
    private Integer delaiLegalDays; // 10 pour PV_MISE_EN_DEMEURE, 20 pour ORDONNANCE_PAIEMENT

    @Column(name = "piece_jointe_url", length = 500)
    private String pieceJointeUrl;

    @Column(name = "huissier_name", length = 255)
    private String huissierName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private StatutDocumentHuissier status = StatutDocumentHuissier.PENDING;

    @Column(name = "notified")
    @Builder.Default
    private Boolean notified = false;

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = Instant.now();
        }
        if (delaiLegalDays == null) {
            // Définir le délai selon le type de document
            if (typeDocument == TypeDocumentHuissier.PV_MISE_EN_DEMEURE) {
                delaiLegalDays = 10;
            } else {
                delaiLegalDays = 20;
            }
        }
    }
}

