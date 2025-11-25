package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.CanalNotification;
import projet.carthagecreance_backend.Entity.TypeNotificationHuissier;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationHuissierDTO {
    private Long id;
    private Long dossierId;
    private TypeNotificationHuissier type;
    private CanalNotification channel;
    private String message;
    private Map<String, Object> payload;
    private Instant createdAt;
    private Instant sentAt;
    private Boolean acked;
    private Long recommendationId;
}
