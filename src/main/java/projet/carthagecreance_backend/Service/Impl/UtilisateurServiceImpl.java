package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public Utilisateur updateUtilisateur(Long id, Utilisateur utilisateur) {
        if (utilisateurRepository.existsById(id)) {
            utilisateur.setId(id);
            return utilisateurRepository.save(utilisateur);
        }
        throw new RuntimeException("Utilisateur not found with id: " + id);
    }

    @Override
    public void deleteUtilisateur(Long id) {
        if (utilisateurRepository.existsById(id)) {
            utilisateurRepository.deleteById(id);
        } else {
            throw new RuntimeException("Utilisateur not found with id: " + id);
        }
    }

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

    @Override
    public Optional<Utilisateur> authenticate(String email, String password) {
        return utilisateurRepository.findByEmailAndMotDePasse(email, password);
    }

    @Override
    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }
}
