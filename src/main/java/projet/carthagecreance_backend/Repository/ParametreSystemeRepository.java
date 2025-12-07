package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.ParametreSysteme;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParametreSystemeRepository extends JpaRepository<ParametreSysteme, Long> {
    Optional<ParametreSysteme> findByCategorieAndCle(String categorie, String cle);
    List<ParametreSysteme> findByCategorie(String categorie);
    List<ParametreSysteme> findAllByOrderByCategorieAscCleAsc();
}

