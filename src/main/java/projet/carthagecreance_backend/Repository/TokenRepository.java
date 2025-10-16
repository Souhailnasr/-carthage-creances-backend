package projet.carthagecreance_backend.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import projet.carthagecreance_backend.Entity.Token;


public interface TokenRepository extends JpaRepository<Token, Integer> {

    @Query(value = "select t from Token t inner join t.user u where u.id = :id and (t.expired = false or t.revoked = false)")
    List<Token> findAllValidTokenByUser(Long id);

    Optional<Token> findByToken(String token);

    boolean existsByToken(String token);
}
