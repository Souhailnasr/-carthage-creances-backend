package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Enquette;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnquetteRepository extends JpaRepository<Enquette, Long> {

    // Rechercher par date de création
    List<Enquette> findByDateCreation(LocalDate dateCreation);

    // Rechercher par date de création entre deux dates
    List<Enquette> findByDateCreationBetween(LocalDate dateDebut, LocalDate dateFin);


    // Rechercher par dossier
    @Query("SELECT e FROM Enquette e WHERE e.dossier.id = :dossierId")
    Optional<Enquette> findByDossierId(@Param("dossierId") Long dossierId);
}
