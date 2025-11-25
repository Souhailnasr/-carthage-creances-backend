package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "notifications_huissier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationHuissier implements Serializable {
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
    @Column(name = "type", nullable = false)
    private TypeNotificationHuissier type;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private CanalNotification channel;

    @Column(name = "message", length = 2000, nullable = false)
    private String message;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload; // JSON string

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "acked")
    @Builder.Default
    private Boolean acked = false;

    @Column(name = "recommendation_id")
    private Long recommendationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", insertable = false, updatable = false)
    @JsonIgnore
    private Recommendation recommendation;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

