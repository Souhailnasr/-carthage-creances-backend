// Fichier : src/main/java/projet/carthagecreance_backend/Service/Impl/DossierServiceImpl.java
package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Creancier;
import projet.carthagecreance_backend.Entity.Debiteur;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.Urgence;
import projet.carthagecreance_backend.Repository.CreancierRepository;
import projet.carthagecreance_backend.Repository.DebiteurRepository;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Service.DossierService;
import projet.carthagecreance_backend.DTO.DossierRequest; // Ajout de l'import DTO

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DossierServiceImpl implements DossierService {

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private CreancierRepository creancierRepository; // Assurez-vous que c'est injecté

    @Autowired
    private DebiteurRepository debiteurRepository; // Assurez-vous que c'est injecté

    // Implémentation de la création via DTO
    @Override
    public Dossier createDossier(DossierRequest request) { // Changement ici : DossierRequest
        try {
            // 1. Récupérer le Créancier à partir de l'ID fourni dans le DTO
            Creancier creancier = creancierRepository.findById(request.getCreancierId())
                    .orElseThrow(() -> new IllegalArgumentException("Créancier avec ID " + request.getCreancierId() + " introuvable."));

            // 2. Récupérer le Débiteur à partir de l'ID fourni dans le DTO
            Debiteur debiteur = debiteurRepository.findById(request.getDebiteurId())
                    .orElseThrow(() -> new IllegalArgumentException("Débiteur avec ID " + request.getDebiteurId() + " introuvable."));

            // 3. Construire l'entité Dossier à partir des données du DTO et des entités récupérées
            // Utilisation du Builder de Lombok pour une construction claire
            Dossier dossier = Dossier.builder()
                    .titre(request.getTitre())
                    .description(request.getDescription())
                    .numeroDossier(request.getNumeroDossier())
                    .montantCreance(request.getMontantCreance())
                    .contratSigne(request.getContratSigne()) // Peut être null si pas encore uploadé
                    .pouvoir(request.getPouvoir())           // Peut être null si pas encore uploadé
                    .urgence(request.getUrgence())
                    .dossierStatus(request.getDossierStatus())
                    .typeDocumentJustificatif(request.getTypeDocumentJustificatif()) // Ajout du nouveau champ
                    .creancier(creancier) // Lier le créancier récupéré
                    .debiteur(debiteur)   // Lier le débiteur récupéré
                    // .dateCreation sera définie automatiquement par le @PrePersist dans l'entité Dossier
                    // .dateCloture, .avocat, .huissier, .finance, .utilisateurs, .enquette,
                    // .audiences, .actions restent null ou avec leurs valeurs par défaut
                    .build();

            // 4. Sauvegarder le Dossier dans la base de données
            return dossierRepository.save(dossier);
        } catch (IllegalArgumentException e) {
            // Relancer l'exception pour qu'elle soit gérée par le contrôleur
            throw e;
        } catch (Exception e) {
            // Gérer d'autres exceptions possibles (ex: problèmes de validation, DB)
            throw new RuntimeException("Erreur lors de la création du dossier : " + e.getMessage(), e);
        }
    }


    @Override
    public Optional<Dossier> getDossierById(Long id) {
        return dossierRepository.findById(id);
    }

    @Override
    public List<Dossier> getAllDossiers() {
        return dossierRepository.findAll();
    }

    @Override
    public Dossier updateDossier(Long id, Dossier dossierDetails) { // Mise à jour avec l'entité complète
        // Vérifier si le dossier existe
        Optional<Dossier> optionalDossier = dossierRepository.findById(id);
        if (optionalDossier.isPresent()) {
            Dossier existingDossier = optionalDossier.get();
            // Mettre à jour les champs nécessaires
            // Attention: Ne pas mettre à jour creancier/debiteur ici sans logique spécifique si vous ne voulez pas les changer
            // Si vous voulez permettre le changement de créancier/débiteur via update, il faudra gérer cela spécifiquement
            existingDossier.setTitre(dossierDetails.getTitre());
            existingDossier.setDescription(dossierDetails.getDescription());
            existingDossier.setNumeroDossier(dossierDetails.getNumeroDossier());
            existingDossier.setMontantCreance(dossierDetails.getMontantCreance());
            existingDossier.setContratSigne(dossierDetails.getContratSigne());
            existingDossier.setPouvoir(dossierDetails.getPouvoir());
            existingDossier.setUrgence(dossierDetails.getUrgence());
            existingDossier.setDossierStatus(dossierDetails.getDossierStatus());
            existingDossier.setTypeDocumentJustificatif(dossierDetails.getTypeDocumentJustificatif()); // Ajout
            existingDossier.setDateCloture(dossierDetails.getDateCloture());
            existingDossier.setAvocat(dossierDetails.getAvocat()); // Si fourni
            existingDossier.setHuissier(dossierDetails.getHuissier()); // Si fourni
            // ... autres champs à mettre à jour ...
            return dossierRepository.save(existingDossier);
        } else {
            throw new RuntimeException("Dossier not found with id: " + id);
        }
    }

    @Override
    public void deleteDossier(Long id) {
        if (dossierRepository.existsById(id)) {
            dossierRepository.deleteById(id);
        } else {
            throw new RuntimeException("Dossier not found with id: " + id);
        }
    }

    @Override
    public Optional<Dossier> getDossierByNumber(String numeroDossier) {
        return dossierRepository.findByNumeroDossier(numeroDossier);
    }

    @Override
    public List<Dossier> getDossiersByTitle(String title) {
        return dossierRepository.findByTitreContainingIgnoreCase(title);
    }

    @Override
    public List<Dossier> getDossiersByDescription(String description) {
        return dossierRepository.findByDescriptionContainingIgnoreCase(description);
    }

    @Override
    public List<Dossier> getDossiersByUrgency(Urgence urgency) {
        return dossierRepository.findByUrgence(urgency);
    }

    @Override
    public List<Dossier> getDossiersByAvocat(Long avocatId) {
        return dossierRepository.findByAvocatId(avocatId);
    }

    @Override
    public List<Dossier> getDossiersByHuissier(Long huissierId) {
        return dossierRepository.findByHuissierId(huissierId);
    }

    @Override
    public List<Dossier> getDossiersByCreancier(Long creancierId) {
        return dossierRepository.findByCreancierId(creancierId);
    }

    @Override
    public List<Dossier> getDossiersByDebiteur(Long debiteurId) {
        return dossierRepository.findByDebiteurId(debiteurId);
    }

    @Override
    public List<Dossier> getDossiersByUser(Long userId) {
        // Implémentation requise si utilisé
        // return dossierRepository.findByUtilisateurId(userId);
        // Si la méthode n'est pas implémentée dans le repository ou le service, lever une exception
        throw new UnsupportedOperationException("Méthode getDossiersByUser non implémentée ou non supportée.");
        // Ou retourner une liste vide si préféré :
        // return Collections.emptyList();
    }

    @Override
    public List<Dossier> getDossiersByCreationDate(Date date) {
        return dossierRepository.findByDateCreation(date);
    }

    @Override
    public List<Dossier> getDossiersByCreationDateRange(Date startDate, Date endDate) {
        return dossierRepository.findByDateCreationBetween(startDate, endDate);
    }

    @Override
    public List<Dossier> getDossiersByClosureDate(Date date) {
        return dossierRepository.findByDateCloture(date);
    }

    @Override
    public List<Dossier> getDossiersByAmount(Double amount) {
        return dossierRepository.findByMontantCreance(amount);
    }

    @Override
    public List<Dossier> getDossiersByAmountRange(Double minAmount, Double maxAmount) {
        return dossierRepository.findByMontantCreanceBetween(minAmount, maxAmount);
    }

    @Override
    public List<Dossier> searchDossiers(String searchTerm) {
        return dossierRepository.findByTitreOuDescriptionContaining(searchTerm);
    }

    @Override
    public List<Dossier> getOpenDossiers() {
        return dossierRepository.findDossiersOuverts();
    }

    @Override
    public List<Dossier> getClosedDossiers() {
        return dossierRepository.findDossiersFermes();
    }

    @Override
    public List<Dossier> getRecentDossiers() {
        // Derniers 30 jours
        long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
        Date thirtyDaysAgo = new Date(System.currentTimeMillis() - thirtyDaysInMillis);
        return dossierRepository.findDossiersRecents(thirtyDaysAgo);
    }

    @Override
    public boolean existsByNumber(String numeroDossier) {
        return dossierRepository.existsByNumeroDossier(numeroDossier);
    }
}