// Fichier : src/main/java/projet/carthagecreance_backend/Service/Impl/UtilisateurServiceImpl.java
package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.RoleUtilisateur; // Ajout de l'import
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Service.UtilisateurService;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public Optional<Utilisateur> getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id);
    }

    @Override
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @Override
    public Utilisateur updateUtilisateur(Long id, Utilisateur utilisateurDetails) { // Renommé le paramètre pour plus de clarté
        Optional<Utilisateur> optionalUtilisateur = utilisateurRepository.findById(id);
        if (optionalUtilisateur.isPresent()) {
            Utilisateur existingUtilisateur = optionalUtilisateur.get();
            // Mettre à jour les champs nécessaires
            existingUtilisateur.setNom(utilisateurDetails.getNom());
            existingUtilisateur.setPrenom(utilisateurDetails.getPrenom());
            // Ne mettez à jour le mot de passe que si un nouveau est fourni
            if (utilisateurDetails.getMotDePasse() != null && !utilisateurDetails.getMotDePasse().isEmpty()) {
                existingUtilisateur.setMotDePasse(utilisateurDetails.getMotDePasse()); // Pour test, sans hash
            }
            // Mettre à jour le rôle si fourni
            if (utilisateurDetails.getRoleUtilisateur() != null) {
                existingUtilisateur.setRoleUtilisateur(utilisateurDetails.getRoleUtilisateur());
            }
            // Mettre à jour l'email avec vérification d'unicité si nécessaire
            if (utilisateurDetails.getEmail() != null && !utilisateurDetails.getEmail().equals(existingUtilisateur.getEmail())) {
                if (utilisateurRepository.existsByEmail(utilisateurDetails.getEmail())) {
                    throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
                }
                existingUtilisateur.setEmail(utilisateurDetails.getEmail());
            }

            return utilisateurRepository.save(existingUtilisateur);
        } else {
            throw new RuntimeException("Utilisateur non trouvé avec id: " + id);
        }
    }

    // >>>>>>>>> CORRECTION : Implémentation de la méthode avec l'enum <<<<<<<<<
    @Override
    public List<Utilisateur> getUtilisateursByRoleUtilisateur(RoleUtilisateur roleUtilisateur) { // Changement ici
        // Validation si le rôle est null
        if (roleUtilisateur == null) {
            throw new IllegalArgumentException("Le rôle ne peut pas être null.");
        }
        // Appel au repository avec l'enum
        return utilisateurRepository.findByRoleUtilisateur(roleUtilisateur); // Changement ici
    }

    @Override
    public void deleteUtilisateur(Long id) {
        if (utilisateurRepository.existsById(id)) {
            utilisateurRepository.deleteById(id);
        } else {
            throw new RuntimeException("Utilisateur non trouvé avec id: " + id);
        }
    }

    // Méthodes de recherche
    @Override
    public List<Utilisateur> getUtilisateursByName(String name) {
        return utilisateurRepository.findByNomContainingIgnoreCase(name);
    }

    @Override
    public List<Utilisateur> getUtilisateursByFirstName(String firstName) {
        return utilisateurRepository.findByPrenomContainingIgnoreCase(firstName);
    }

    @Override
    public List<Utilisateur> getUtilisateursByFullName(String name, String firstName) {
        return utilisateurRepository.findByNomAndPrenom(name, firstName);
    }

    @Override
    public Optional<Utilisateur> getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    @Override
    public List<Utilisateur> searchUtilisateurs(String searchTerm) {
        return utilisateurRepository.findByNomOuPrenomContaining(searchTerm);
    }

    @Override
    public List<Utilisateur> getUtilisateursWithDossiers() {
        return utilisateurRepository.findUtilisateursAvecDossiers();
    }

    @Override
    public List<Utilisateur> getUtilisateursWithoutDossiers() {
        return utilisateurRepository.findUtilisateursSansDossiers();
    }

    // >>>>>>>>> SUPPRESSION : Cette méthode est redondante et incorrecte <<<<<<<<<
    // @Override
    // public List<Utilisateur> getUtilisateursByRoleUtilisateur(String roleutilisateur) {
    //     return List.of(); // Implémentation vide et incorrecte
    // }

    @Override
    public Optional<Utilisateur> authenticate(String email, String password) {
        return utilisateurRepository.findByEmailAndMotDePasse(email, password);
    }

    @Override
    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }
}