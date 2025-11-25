package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.DocumentHuissier;
import projet.carthagecreance_backend.Entity.StatutDocumentHuissier;
import projet.carthagecreance_backend.Entity.TypeDocumentHuissier;

import java.util.List;

@Repository
public interface DocumentHuissierRepository extends JpaRepository<DocumentHuissier, Long> {
    
    List<DocumentHuissier> findByDossierId(Long dossierId);
    
    List<DocumentHuissier> findByTypeDocument(TypeDocumentHuissier typeDocument);
    
    List<DocumentHuissier> findByStatus(StatutDocumentHuissier status);
    
    List<DocumentHuissier> findByDossierIdAndStatus(Long dossierId, StatutDocumentHuissier status);
    
    List<DocumentHuissier> findByStatusAndNotified(StatutDocumentHuissier status, Boolean notified);
}

