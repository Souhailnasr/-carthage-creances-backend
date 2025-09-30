package projet.carthagecreance_backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Notification  implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypeNotification type;

    @Column(name = "titre", nullable = false, length = 255)
    private String titre;
    @Column(name = "message", length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutNotification statut = StatutNotification.NON_LUE;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_lecture")
    private LocalDateTime dateLecture;

    @Column(name = "entite_id")
    private Long entiteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entite_type")
    private TypeEntite entiteType;

    @Column(name = "lien_action", length = 500)
    private String lienAction;
}
