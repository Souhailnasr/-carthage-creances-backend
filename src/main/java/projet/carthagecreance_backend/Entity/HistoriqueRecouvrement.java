package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité pour l'historique des recouvrements
 * Permet de tracer tous les montants recouvrés par phase et par action
 */
@Entity
@Table(name = "historique_recouvrement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueRecouvrement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "dossier_id", nullable = false)
    private Long dossierId;
    
    /**
     * Phase du recouvrement
     * AMIABLE : Recouvrement amiable
     * JURIDIQUE : Recouvrement juridique
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "phase", nullable = false)
    private PhaseRecouvrement phase;
    
    /**
     * Montant recouvré dans cette opération
     */
    @Column(name = "montant_recouvre", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantRecouvre;
    
    /**
     * Montant total recouvré après cette opération
     */
    @Column(name = "montant_total_recouvre", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantTotalRecouvre;
    
    /**
     * Montant restant après cette opération
     */
    @Column(name = "montant_restant", nullable = false, precision = 19, scale = 2)
    private BigDecimal montantRestant;
    
    /**
     * Type d'action qui a généré ce recouvrement
     * ACTION_AMIABLE : Action amiable avec réponse positive
     * ACTION_HUISSIER : Action huissier
     * FINALISATION_AMIABLE : Finalisation phase amiable
     * FINALISATION_JURIDIQUE : Finalisation phase juridique (audiences)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_action")
    private TypeActionRecouvrement typeAction;
    
    /**
     * ID de l'action associée (action amiable, action huissier, ou null pour finalisation)
     */
    @Column(name = "action_id")
    private Long actionId;
    
    /**
     * Utilisateur qui a enregistré ce recouvrement
     */
    @Column(name = "utilisateur_id")
    private Long utilisateurId;
    
    /**
     * Date et heure de l'enregistrement
     */
    @Column(name = "date_enregistrement", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateEnregistrement;
    
    /**
     * Commentaire optionnel
     */
    @Column(name = "commentaire", length = 500)
    private String commentaire;
    
    /**
     * Enum pour les phases de recouvrement
     */
    public enum PhaseRecouvrement {
        AMIABLE,
        JURIDIQUE
    }
    
    /**
     * Enum pour les types d'actions de recouvrement
     */
    public enum TypeActionRecouvrement {
        ACTION_AMIABLE,           // Action amiable avec réponse positive
        ACTION_HUISSIER,          // Action huissier
        FINALISATION_AMIABLE,     // Finalisation phase amiable
        FINALISATION_JURIDIQUE    // Finalisation phase juridique (audiences)
    }
}

