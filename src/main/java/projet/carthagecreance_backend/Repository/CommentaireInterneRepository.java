package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.CommentaireInterne;

import java.util.List;

@Repository
public interface CommentaireInterneRepository extends JpaRepository<CommentaireInterne, Long> {
    List<CommentaireInterne> findByDossierIdOrderByDateCreationDesc(Long dossierId);
    List<CommentaireInterne> findByDossierIdAndVisibleParChefTrueOrderByDateCreationDesc(Long dossierId);
    List<CommentaireInterne> findByAuteurIdOrderByDateCreationDesc(Long auteurId);
}

