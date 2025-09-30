package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Notification;
import projet.carthagecreance_backend.Entity.StatutNotification;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Trouver les notifications par utilisateur
    List<Notification> findByUtilisateurId(Long userId);

    // Trouver les notifications non lues
    List<Notification> findByUtilisateurIdAndStatut(Long userId, StatutNotification statut);

    // Trouver les notifications récentes
    @Query("SELECT n FROM Notification n WHERE n.utilisateur.id = :userId AND n.dateCreation >= :dateDebut ORDER BY n.dateCreation DESC")
    List<Notification> findNotificationsRecent(@Param("userId") Long userId, @Param("dateDebut") LocalDateTime dateDebut);

    // Compter les notifications non lues
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.utilisateur.id = :userId AND n.statut = 'NON_LUE'")
    Long countNotificationsNonLues(@Param("userId") Long userId);

    // Trouver les notifications par type et utilisateur
    List<Notification> findByUtilisateurIdAndType(Long userId, projet.carthagecreance_backend.Entity.TypeNotification type);

    // Trouver les notifications par entité
    List<Notification> findByEntiteIdAndEntiteType(Long entiteId, projet.carthagecreance_backend.Entity.TypeEntite entiteType);

    // Trouver les notifications par type
    List<Notification> findByType(projet.carthagecreance_backend.Entity.TypeNotification type);

    // Trouver les notifications par statut
    List<Notification> findByStatut(StatutNotification statut);

    // Trouver les notifications par date de création
    @Query("SELECT n FROM Notification n WHERE n.dateCreation BETWEEN :dateDebut AND :dateFin ORDER BY n.dateCreation DESC")
    List<Notification> findByDateCreationBetween(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    // Trouver les notifications par date de lecture
    @Query("SELECT n FROM Notification n WHERE n.dateLecture BETWEEN :dateDebut AND :dateFin ORDER BY n.dateLecture DESC")
    List<Notification> findByDateLectureBetween(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);

    // Trouver les notifications lues récemment
    @Query("SELECT n FROM Notification n WHERE n.statut = 'LUE' AND n.dateLecture >= :dateDebut ORDER BY n.dateLecture DESC")
    List<Notification> findNotificationsLuesRecent(@Param("dateDebut") LocalDateTime dateDebut);

    // Trouver les notifications non lues depuis plus de X jours
    @Query("SELECT n FROM Notification n WHERE n.statut = 'NON_LUE' AND n.dateCreation <= :dateLimite ORDER BY n.dateCreation ASC")
    List<Notification> findNotificationsNonLuesDepuis(@Param("dateLimite") LocalDateTime dateLimite);

    // Compter les notifications par utilisateur
    long countByUtilisateurId(Long userId);

    // Compter les notifications par type
    long countByType(projet.carthagecreance_backend.Entity.TypeNotification type);

    // Compter les notifications par entité
    long countByEntiteIdAndEntiteType(Long entiteId, projet.carthagecreance_backend.Entity.TypeEntite entiteType);

    // Trouver les notifications par utilisateur et type
    List<Notification> findByUtilisateurIdAndTypeAndStatut(Long userId, projet.carthagecreance_backend.Entity.TypeNotification type, StatutNotification statut);

    // Trouver les notifications par utilisateur et entité
    List<Notification> findByUtilisateurIdAndEntiteIdAndEntiteType(Long userId, Long entiteId, projet.carthagecreance_backend.Entity.TypeEntite entiteType);

    // Vérifier l'existence d'une notification pour une entité
    boolean existsByEntiteIdAndEntiteType(Long entiteId, projet.carthagecreance_backend.Entity.TypeEntite entiteType);

    // Vérifier l'existence d'une notification non lue pour un utilisateur
    boolean existsByUtilisateurIdAndStatut(Long userId, StatutNotification statut);

    // Trouver les notifications par utilisateur avec pagination
    @Query("SELECT n FROM Notification n WHERE n.utilisateur.id = :userId ORDER BY n.dateCreation DESC")
    List<Notification> findByUtilisateurIdOrderByDateCreationDesc(@Param("userId") Long userId);

    // Trouver les notifications non lues par utilisateur avec pagination
    @Query("SELECT n FROM Notification n WHERE n.utilisateur.id = :userId AND n.statut = 'NON_LUE' ORDER BY n.dateCreation DESC")
    List<Notification> findNotificationsNonLuesByUserOrderByDateCreationDesc(@Param("userId") Long userId);
}
