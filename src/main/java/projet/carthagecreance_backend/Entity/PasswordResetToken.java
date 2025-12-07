package projet.carthagecreance_backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token", indexes = {
    @Index(name = "idx_token", columnList = "token"),
    @Index(name = "idx_utilisateur", columnList = "utilisateur_id"),
    @Index(name = "idx_expiration", columnList = "date_expiration")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 255)
    private String token;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
    
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TokenStatut statut = TokenStatut.ACTIF;
    
    @Column(name = "date_utilisation", nullable = true)
    private LocalDateTime dateUtilisation;
    
    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (dateExpiration == null) {
            dateExpiration = dateCreation.plusHours(24); // 24 heures de validité
        }
    }
}

