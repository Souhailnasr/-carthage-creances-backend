package projet.carthagecreance_backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "validation_enquetes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ValidationEnquete implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquete_id", nullable = false)
    private Enquette enquete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_createur_id", nullable = false)
    private Utilisateur agentCreateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_validateur_id")
    private Utilisateur chefValidateur;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutValidation statut = StatutValidation.EN_ATTENTE;

    @Column(name = "commentaires", length = 1000)
    private String commentaires;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}
