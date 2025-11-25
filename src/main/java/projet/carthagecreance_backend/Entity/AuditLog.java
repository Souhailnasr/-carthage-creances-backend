package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog implements Serializable {
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

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private TypeChangementAudit changeType;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String before; // JSON string

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String after; // JSON string

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "description", length = 500)
    private String description;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}

