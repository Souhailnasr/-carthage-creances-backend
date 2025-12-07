package projet.carthagecreance_backend.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projet.carthagecreance_backend.Entity.Token;


public interface TokenRepository extends JpaRepository<Token, Integer> {

    @Query(value = "select t from Token t inner join t.user u where u.id = :id and (t.expired = false or t.revoked = false)")
    List<Token> findAllValidTokenByUser(Long id);
    
    /**
     * Trouve tous les tokens d'un utilisateur (valides et invalides)
     */
    @Query("SELECT t FROM Token t WHERE t.user.id = :userId")
    List<Token> findAllByUserId(@Param("userId") Long userId);

    /**
     * Trouve un token par sa valeur en chargeant explicitement l'utilisateur
     * Utilise JOIN FETCH pour éviter le problème de lazy loading
     */
    @Query("SELECT t FROM Token t LEFT JOIN FETCH t.user WHERE t.token = :token")
    Optional<Token> findByToken(String token);

    boolean existsByToken(String token);
}
