package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Facture;
import projet.carthagecreance_backend.Entity.FactureStatut;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    Optional<Facture> findByNumeroFacture(String numeroFacture);

    List<Facture> findByDossierId(Long dossierId);

    List<Facture> findByStatut(FactureStatut statut);

    @Query("SELECT f FROM Facture f WHERE f.statut = 'EMISE' AND f.dateEcheance < :date")
    List<Facture> findFacturesEnRetard(@Param("date") LocalDate date);

    @Query("SELECT MAX(CAST(SUBSTRING(f.numeroFacture, 6) AS int)) FROM Facture f WHERE f.numeroFacture LIKE :prefix%")
    Integer findMaxNumeroFacture(@Param("prefix") String prefix);
}

