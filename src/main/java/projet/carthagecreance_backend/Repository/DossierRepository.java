package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.DossierStatus;
import projet.carthagecreance_backend.Entity.Statut;
import projet.carthagecreance_backend.Entity.TypeRecouvrement;
import projet.carthagecreance_backend.Entity.Urgence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, Long>, JpaSpecificationExecutor<Dossier> {
    
    // Rechercher par numéro de dossier
    Optional<Dossier> findByNumeroDossier(String numeroDossier);
    
    // Rechercher par titre
    List<Dossier> findByTitreContainingIgnoreCase(String titre);
    
    // Rechercher par description
    List<Dossier> findByDescriptionContainingIgnoreCase(String description);
    
    // Rechercher par urgence
    List<Dossier> findByUrgence(Urgence urgence);
    
    // Rechercher par avocat
    List<Dossier> findByAvocatId(Long avocatId);
    
    // Rechercher par huissier
    List<Dossier> findByHuissierId(Long huissierId);
    
    // Rechercher par créancier
    List<Dossier> findByCreancierId(Long creancierId);
    
    // Rechercher par débiteur
    List<Dossier> findByDebiteurId(Long debiteurId);
    
    // Rechercher par utilisateur
    @Query("SELECT d FROM Dossier d JOIN d.utilisateurs u WHERE u.id = :utilisateurId")
    List<Dossier> findByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
    
    // Rechercher par date de création
    List<Dossier> findByDateCreation(Date dateCreation);
    
    // Rechercher par date de création entre deux dates
    List<Dossier> findByDateCreationBetween(Date dateDebut, Date dateFin);
    
    // Rechercher par date de clôture
    List<Dossier> findByDateCloture(Date dateCloture);
    
    // Rechercher les dossiers ouverts (sans date de clôture)
    @Query("SELECT d FROM Dossier d WHERE d.dateCloture IS NULL")
    List<Dossier> findDossiersOuverts();
    
    // Rechercher les dossiers fermés (avec date de clôture)
    @Query("SELECT d FROM Dossier d WHERE d.dateCloture IS NOT NULL")
    List<Dossier> findDossiersFermes();
    
    // Rechercher par montant de créance
    List<Dossier> findByMontantCreance(Double montantCreance);
    
    // Rechercher par montant de créance supérieur à
    List<Dossier> findByMontantCreanceGreaterThan(Double montant);
    
    // Rechercher par montant de créance inférieur à
    List<Dossier> findByMontantCreanceLessThan(Double montant);
    
    // Rechercher par montant de créance entre deux valeurs
    List<Dossier> findByMontantCreanceBetween(Double montantMin, Double montantMax);
    
    // Rechercher par titre ou description (recherche globale)
    @Query("SELECT d FROM Dossier d WHERE LOWER(d.titre) LIKE LOWER(CONCAT('%', :recherche, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :recherche, '%'))")
    List<Dossier> findByTitreOuDescriptionContaining(@Param("recherche") String recherche);
    
    // Vérifier l'existence par numéro de dossier
    boolean existsByNumeroDossier(String numeroDossier);
    
    // Compter les dossiers par urgence
    @Query("SELECT d.urgence, COUNT(d) FROM Dossier d GROUP BY d.urgence")
    List<Object[]> compterDossiersParUrgence();
    
    // Compter les dossiers par avocat
    @Query("SELECT d.avocat.nom, d.avocat.prenom, COUNT(d) FROM Dossier d WHERE d.avocat IS NOT NULL GROUP BY d.avocat.id, d.avocat.nom, d.avocat.prenom")
    List<Object[]> compterDossiersParAvocat();
    
    // Compter les dossiers par huissier
    @Query("SELECT d.huissier.nom, d.huissier.prenom, COUNT(d) FROM Dossier d WHERE d.huissier IS NOT NULL GROUP BY d.huissier.id, d.huissier.nom, d.huissier.prenom")
    List<Object[]> compterDossiersParHuissier();
    
    // Calculer le montant total des créances
    @Query("SELECT COALESCE(SUM(d.montantCreance), 0) FROM Dossier d")
    Double calculerMontantTotalCreances();
    
    // Calculer le montant total des créances par urgence
    @Query("SELECT d.urgence, COALESCE(SUM(d.montantCreance), 0) FROM Dossier d GROUP BY d.urgence")
    List<Object[]> calculerMontantTotalParUrgence();
    
    // Rechercher les dossiers récents (derniers 30 jours)
    @Query("SELECT d FROM Dossier d WHERE d.dateCreation >= :dateLimite ORDER BY d.dateCreation DESC")
    List<Dossier> findDossiersRecents(@Param("dateLimite") Date dateLimite);
    
    // Rechercher les dossiers avec enquête
    @Query("SELECT d FROM Dossier d WHERE d.enquette IS NOT NULL")
    List<Dossier> findDossiersAvecEnquete();
    
    // Rechercher les dossiers avec audiences
    @Query("SELECT d FROM Dossier d WHERE SIZE(d.audiences) > 0")
    List<Dossier> findDossiersAvecAudiences();
    
    // Rechercher les dossiers avec actions
    @Query("SELECT d FROM Dossier d WHERE SIZE(d.actions) > 0")
    List<Dossier> findDossiersAvecActions();
    
    // ==================== Nouvelles méthodes pour le workflow ====================
    
    // Rechercher par statut de dossier
    List<Dossier> findByDossierStatus(DossierStatus dossierStatus);
    
    // Nouveau: Rechercher par statut (enum Statut)
    List<Dossier> findByStatut(Statut statut);
    
    // Nouveau: Filtrer par statut et agent responsable
    List<Dossier> findByStatutAndAgentResponsableId(Statut statut, Long agentId);
    
    // Nouveau: Filtrer par statut et agent créateur
    List<Dossier> findByStatutAndAgentCreateurId(Statut statut, Long agentId);
    
    // Rechercher par agent créateur
    @Query("SELECT d FROM Dossier d WHERE d.agentCreateur.id = :agentId")
    List<Dossier> findByAgentCreateurId(@Param("agentId") Long agentId);
    
    // Rechercher par agent responsable
    @Query("SELECT d FROM Dossier d WHERE d.agentResponsable.id = :agentId")
    List<Dossier> findByAgentResponsableId(@Param("agentId") Long agentId);
    
    // Compter par statut de dossier
    long countByDossierStatus(DossierStatus dossierStatus);
    
    // Compter par date de création après une date
    long countByDateCreationAfter(Date date);
    
    // Compter par utilisateur
    @Query("SELECT COUNT(d) FROM Dossier d JOIN d.utilisateurs u WHERE u.id = :utilisateurId")
    long countByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
    
    // Compter par agent créateur
    @Query("SELECT COUNT(d) FROM Dossier d WHERE d.agentCreateur.id = :agentId")
    long countByAgentCreateurId(@Param("agentId") Long agentId);
    
    // ==================== Méthodes pour le type de recouvrement ====================
    
    // Rechercher par type de recouvrement avec pagination
    Page<Dossier> findByTypeRecouvrement(TypeRecouvrement typeRecouvrement, Pageable pageable);
    
    // Rechercher par type de recouvrement, validé et en cours
    @Query("SELECT d FROM Dossier d WHERE d.typeRecouvrement = :typeRecouvrement " +
           "AND d.valide = true AND d.dossierStatus = :dossierStatus AND d.dateCloture IS NULL")
    Page<Dossier> findByTypeRecouvrementAndValideAndDossierStatus(
        @Param("typeRecouvrement") TypeRecouvrement typeRecouvrement,
        @Param("dossierStatus") DossierStatus dossierStatus,
        Pageable pageable
    );
    
    // Rechercher par type de recouvrement (liste simple)
    List<Dossier> findByTypeRecouvrement(TypeRecouvrement typeRecouvrement);
    
    // Compter par type de recouvrement
    long countByTypeRecouvrement(TypeRecouvrement typeRecouvrement);
}
