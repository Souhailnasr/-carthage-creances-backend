package projet.carthagecreance_backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "taches_urgentes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TacheUrgente  implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titre", nullable = false, length = 255)
    private String titre;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypeTache type;

    @Enumerated(EnumType.STRING)
    @Column(name = "priorite", nullable = false)
    private PrioriteTache priorite;
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutTache statut = StatutTache.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_assigné_id", nullable = false)
    private Utilisateur agentAssigné;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_createur_id", nullable = false)
    private Utilisateur chefCreateur;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_echeance", nullable = false)
    private LocalDateTime dateEcheance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id")
    private Dossier dossier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquete_id")
    private Enquette enquete;

    @Column(name = "date_completion")
    private LocalDateTime dateCompletion;

    @Column(name = "commentaires", length = 1000)
    private String commentaires;
}
