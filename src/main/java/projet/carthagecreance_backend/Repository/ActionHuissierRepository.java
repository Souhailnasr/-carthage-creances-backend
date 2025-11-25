package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.ActionHuissier;
import projet.carthagecreance_backend.Entity.TypeActionHuissier;

import java.util.List;

@Repository
public interface ActionHuissierRepository extends JpaRepository<ActionHuissier, Long> {
    
    List<ActionHuissier> findByDossierId(Long dossierId);
    
    List<ActionHuissier> findByTypeAction(TypeActionHuissier typeAction);
    
    List<ActionHuissier> findByDossierIdAndTypeAction(Long dossierId, TypeActionHuissier typeAction);
}

