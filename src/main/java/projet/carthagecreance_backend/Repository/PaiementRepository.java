package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Paiement;
import projet.carthagecreance_backend.Entity.StatutPaiement;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findByFactureId(Long factureId);

    List<Paiement> findByStatut(StatutPaiement statut);

    List<Paiement> findByDatePaiementBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.facture.id = :factureId")
    Double calculerTotalPaiementsByFacture(@Param("factureId") Long factureId);

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.datePaiement BETWEEN :startDate AND :endDate")
    Double calculerTotalPaiementsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

