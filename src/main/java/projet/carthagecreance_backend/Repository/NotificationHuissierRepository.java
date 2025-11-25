package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.NotificationHuissier;
import projet.carthagecreance_backend.Entity.TypeNotificationHuissier;
import projet.carthagecreance_backend.Entity.CanalNotification;

import java.util.List;

@Repository
public interface NotificationHuissierRepository extends JpaRepository<NotificationHuissier, Long> {
    
    List<NotificationHuissier> findByDossierId(Long dossierId);
    
    List<NotificationHuissier> findByType(TypeNotificationHuissier type);
    
    List<NotificationHuissier> findByChannel(CanalNotification channel);
    
    List<NotificationHuissier> findByDossierIdAndAcked(Long dossierId, Boolean acked);
    
    @Query("SELECT n FROM NotificationHuissier n WHERE n.dossierId = :dossierId AND n.acked = false ORDER BY n.createdAt DESC")
    List<NotificationHuissier> findUnacknowledgedByDossierId(@Param("dossierId") Long dossierId);
}

