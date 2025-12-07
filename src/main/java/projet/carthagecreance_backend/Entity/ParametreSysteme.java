package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entité pour les paramètres système
 * Configuration centralisée de l'application
 */
@Entity
@Table(name = "parametres_systeme", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"categorie", "cle"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametreSysteme implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "categorie", nullable = false, length = 50)
    private String categorie; // GENERAUX, ARCHIVAGE, ALERTES, EMAILS, SECURITE, SAUVEGARDE

    @Column(name = "cle", nullable = false, length = 100)
    private String cle;

    @Column(name = "valeur", columnDefinition = "TEXT")
    private String valeur;

    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private String type = "STRING"; // STRING, NUMBER, BOOLEAN, JSON

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_modification")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateModification;

    @Column(name = "modifie_par")
    private Long modifiePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifie_par", insertable = false, updatable = false)
    @JsonIgnore
    private Utilisateur modificateur;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}

