package projet.carthagecreance_backend.Repository;

import projet.carthagecreance_backend.Entity.PasswordResetToken;
import projet.carthagecreance_backend.Entity.TokenStatut;
import projet.carthagecreance_backend.Entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    /**
     * Trouve un token par sa valeur
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Trouve tous les tokens actifs d'un utilisateur
     */
    List<PasswordResetToken> findByUtilisateurAndStatut(Utilisateur utilisateur, TokenStatut statut);
    
    /**
     * Trouve tous les tokens d'un utilisateur (tous statuts confondus)
     */
    List<PasswordResetToken> findByUtilisateur(Utilisateur utilisateur);
    
    /**
     * Trouve tous les tokens actifs d'un utilisateur par email
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.utilisateur.email = :email AND prt.statut = :statut")
    List<PasswordResetToken> findByUtilisateurEmailAndStatut(@Param("email") String email, @Param("statut") TokenStatut statut);
    
    /**
     * Compte le nombre de tokens actifs créés récemment pour un utilisateur (rate limiting)
     */
    @Query("SELECT COUNT(prt) FROM PasswordResetToken prt WHERE prt.utilisateur.email = :email AND prt.statut = :statut AND prt.dateCreation >= :since")
    long countByUtilisateurEmailAndStatutAndDateCreationAfter(
        @Param("email") String email, 
        @Param("statut") TokenStatut statut, 
        @Param("since") LocalDateTime since
    );
    
    /**
     * Marque tous les tokens actifs d'un utilisateur comme expirés
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.statut = 'EXPIRE' WHERE prt.utilisateur.email = :email AND prt.statut = 'ACTIF'")
    void invalidateActiveTokensByEmail(@Param("email") String email);
    
    /**
     * Marque les tokens expirés automatiquement
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.statut = 'EXPIRE' WHERE prt.statut = 'ACTIF' AND prt.dateExpiration < :now")
    void expireTokens(@Param("now") LocalDateTime now);
    
    /**
     * Supprime les tokens expirés ou utilisés de plus de 7 jours (nettoyage)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE (prt.statut = 'EXPIRE' OR prt.statut = 'UTILISE') AND prt.dateCreation < :cutoffDate")
    void deleteOldTokens(@Param("cutoffDate") LocalDateTime cutoffDate);
}

