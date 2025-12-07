package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entité pour les commentaires internes Superadmin
 * Visibles par le chef de département
 */
@Entity
@Table(name = "commentaires_internes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentaireInterne implements Serializable {
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

    @Column(name = "auteur_id", nullable = false)
    private Long auteurId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id", insertable = false, updatable = false)
    @JsonIgnore
    private Utilisateur auteur;

    @Column(name = "commentaire", columnDefinition = "TEXT", nullable = false)
    private String commentaire;

    @Column(name = "visible_par_chef", nullable = false)
    @Builder.Default
    private Boolean visibleParChef = true;

    @Column(name = "date_creation", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (visibleParChef == null) {
            visibleParChef = true;
        }
    }
}

