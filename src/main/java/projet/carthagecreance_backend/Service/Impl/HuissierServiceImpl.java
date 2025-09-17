package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Huissier;
import projet.carthagecreance_backend.Repository.HuissierRepository;
import projet.carthagecreance_backend.Service.HuissierService;

import java.util.List;
import java.util.Optional;

@Service
public class HuissierServiceImpl implements HuissierService {

    @Autowired
    private HuissierRepository huissierRepository;

    @Override
    public Huissier createHuissier(Huissier huissier) {
        return huissierRepository.save(huissier);
    }

    @Override
    public Optional<Huissier> getHuissierById(Long id) {
        return huissierRepository.findById(id);
    }

    @Override
    public List<Huissier> getAllHuissiers() {
        return huissierRepository.findAll();
    }

    @Override
    public Huissier updateHuissier(Long id, Huissier huissier) {
        if (huissierRepository.existsById(id)) {
            huissier.setId(id);
            return huissierRepository.save(huissier);
        }
        throw new RuntimeException("Huissier not found with id: " + id);
    }

    @Override
    public void deleteHuissier(Long id) {
        if (huissierRepository.existsById(id)) {
            huissierRepository.deleteById(id);
        } else {
            throw new RuntimeException("Huissier not found with id: " + id);
        }
    }

    @Override
    public List<Huissier> getHuissiersByName(String name) {
        return huissierRepository.findByNomContainingIgnoreCase(name);
    }

    @Override
    public List<Huissier> getHuissiersByFirstName(String firstName) {
        return huissierRepository.findByPrenomContainingIgnoreCase(firstName);
    }

    @Override
    public List<Huissier> getHuissiersByFullName(String name, String firstName) {
        return huissierRepository.findByNomAndPrenom(name, firstName);
    }

    @Override
    public Optional<Huissier> getHuissierByEmail(String email) {
        return huissierRepository.findByEmail(email);
    }

    @Override
    public Optional<Huissier> getHuissierByPhone(String phone) {
        return huissierRepository.findByTelephone(phone);
    }

    @Override
    public List<Huissier> getHuissiersBySpecialty(String specialty) {
        return huissierRepository.findBySpecialiteContainingIgnoreCase(specialty);
    }

    @Override
    public List<Huissier> searchHuissiers(String searchTerm) {
        return huissierRepository.findByNomOuPrenomContaining(searchTerm);
    }

    @Override
    public List<Huissier> getHuissiersWithDossiers() {
        return huissierRepository.findHuissiersAvecPlusDeDossiers();
    }

    @Override
    public List<Huissier> getHuissiersWithoutDossiers() {
        return huissierRepository.findHuissiersSansDossiers();
    }

    @Override
    public boolean existsByEmail(String email) {
        return huissierRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return huissierRepository.existsByTelephone(phone);
    }
}
