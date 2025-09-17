package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Creancier;
import projet.carthagecreance_backend.Repository.CreancierRepository;
import projet.carthagecreance_backend.Service.CreancierService;

import java.util.List;
import java.util.Optional;

@Service
public class CreancierServiceImpl implements CreancierService {

    @Autowired
    private CreancierRepository creancierRepository;

    @Override
    public Creancier createCreancier(Creancier creancier) {
        return creancierRepository.save(creancier);
    }

    @Override
    public Optional<Creancier> getCreancierById(Long id) {
        return creancierRepository.findById(id);
    }

    @Override
    public List<Creancier> getAllCreanciers() {
        return creancierRepository.findAll();
    }

    @Override
    public Creancier updateCreancier(Long id, Creancier creancier) {
        if (creancierRepository.existsById(id)) {
            creancier.setId(id);
            return creancierRepository.save(creancier);
        }
        throw new RuntimeException("Creancier not found with id: " + id);
    }

    @Override
    public void deleteCreancier(Long id) {
        if (creancierRepository.existsById(id)) {
            creancierRepository.deleteById(id);
        } else {
            throw new RuntimeException("Creancier not found with id: " + id);
        }
    }

    @Override
    public Optional<Creancier> getCreancierByCode(String codeCreancier) {
        return creancierRepository.findByCodeCreancier(codeCreancier);
    }

    @Override
    public Optional<Creancier> getCreancierByCodeCreance(String codeCreance) {
        return creancierRepository.findByCodeCreance(codeCreance);
    }

    @Override
    public List<Creancier> getCreanciersByName(String name) {
        return creancierRepository.findByNomContainingIgnoreCase(name);
    }

    @Override
    public List<Creancier> getCreanciersByFirstName(String firstName) {
        return creancierRepository.findByPrenomContainingIgnoreCase(firstName);
    }

    @Override
    public List<Creancier> getCreanciersByFullName(String name, String firstName) {
        return creancierRepository.findByNomAndPrenom(name, firstName);
    }

    @Override
    public Optional<Creancier> getCreancierByEmail(String email) {
        return creancierRepository.findByEmail(email);
    }

    @Override
    public Optional<Creancier> getCreancierByPhone(String phone) {
        return creancierRepository.findByTelephone(phone);
    }

    @Override
    public List<Creancier> getCreanciersByCity(String city) {
        return creancierRepository.findByVilleContainingIgnoreCase(city);
    }

    @Override
    public List<Creancier> getCreanciersByPostalCode(String postalCode) {
        return creancierRepository.findByCodePostal(postalCode);
    }

    @Override
    public List<Creancier> getCreanciersByCityAndPostalCode(String city, String postalCode) {
        return creancierRepository.findByVilleAndCodePostal(city, postalCode);
    }

    @Override
    public List<Creancier> searchCreanciers(String searchTerm) {
        return creancierRepository.findByNomOuPrenomContaining(searchTerm);
    }

    @Override
    public boolean existsByEmail(String email) {
        return creancierRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return creancierRepository.existsByTelephone(phone);
    }

    @Override
    public boolean existsByCode(String codeCreancier) {
        return creancierRepository.existsByCodeCreancier(codeCreancier);
    }

    @Override
    public boolean existsByCodeCreance(String codeCreance) {
        return creancierRepository.existsByCodeCreance(codeCreance);
    }
}
