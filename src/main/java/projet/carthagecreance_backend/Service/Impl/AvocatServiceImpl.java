package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Avocat;
import projet.carthagecreance_backend.Repository.AvocatRepository;
import projet.carthagecreance_backend.Service.AvocatService;

import java.util.List;
import java.util.Optional;

@Service
public class AvocatServiceImpl implements AvocatService {

    @Autowired
    private AvocatRepository avocatRepository;

    @Override
    public Avocat createAvocat(Avocat avocat) {
        return avocatRepository.save(avocat);
    }

    @Override
    public Optional<Avocat> getAvocatById(Long id) {
        return avocatRepository.findById(id);
    }

    @Override
    public List<Avocat> getAllAvocats() {
        return avocatRepository.findAll();
    }

    @Override
    public Avocat updateAvocat(Long id, Avocat avocat) {
        if (avocatRepository.existsById(id)) {
            avocat.setId(id);
            return avocatRepository.save(avocat);
        }
        throw new RuntimeException("Avocat not found with id: " + id);
    }

    @Override
    public void deleteAvocat(Long id) {
        if (avocatRepository.existsById(id)) {
            avocatRepository.deleteById(id);
        } else {
            throw new RuntimeException("Avocat not found with id: " + id);
        }
    }

    @Override
    public List<Avocat> getAvocatsByName(String name) {
        return avocatRepository.findByNomContainingIgnoreCase(name);
    }

    @Override
    public List<Avocat> getAvocatsByFirstName(String firstName) {
        return avocatRepository.findByPrenomContainingIgnoreCase(firstName);
    }

    @Override
    public List<Avocat> getAvocatsByFullName(String name, String firstName) {
        return avocatRepository.findByNomAndPrenom(name, firstName);
    }

    @Override
    public Optional<Avocat> getAvocatByEmail(String email) {
        return avocatRepository.findByEmail(email);
    }

    @Override
    public Optional<Avocat> getAvocatByPhone(String phone) {
        return avocatRepository.findByTelephone(phone);
    }

    @Override
    public List<Avocat> getAvocatsBySpecialty(String specialty) {
        return avocatRepository.findBySpecialiteContainingIgnoreCase(specialty);
    }

    @Override
    public List<Avocat> searchAvocats(String searchTerm) {
        return avocatRepository.findByNomOuPrenomContaining(searchTerm);
    }

    @Override
    public boolean existsByEmail(String email) {
        return avocatRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return avocatRepository.existsByTelephone(phone);
    }
}
