package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Creancier;
import projet.carthagecreance_backend.Entity.Debiteur;
import projet.carthagecreance_backend.Entity.Type;
import projet.carthagecreance_backend.Repository.DebiteurRepository;
import projet.carthagecreance_backend.Service.DebiteurService;

import java.util.List;
import java.util.Optional;

@Service
public class DebiteurServiceImpl implements DebiteurService {

    @Autowired
    private DebiteurRepository debiteurRepository;

    @Override
    public Debiteur createDebiteur(Debiteur debiteur) {
        return debiteurRepository.save(debiteur);
    }

    @Override
    public Optional<Debiteur> getDebiteurById(Long id) {
        return debiteurRepository.findById(id);
    }

    @Override
    public List<Debiteur> getAllDebiteurs() {
        return debiteurRepository.findAll();
    }

    @Override
    public Debiteur updateDebiteur(Long id, Debiteur debiteur) {
        if (debiteurRepository.existsById(id)) {
            debiteur.setId(id);
            return debiteurRepository.save(debiteur);
        }
        throw new RuntimeException("Debiteur not found with id: " + id);
    }

    @Override
    public void deleteDebiteur(Long id) {
        if (debiteurRepository.existsById(id)) {
            debiteurRepository.deleteById(id);
        } else {
            throw new RuntimeException("Debiteur not found with id: " + id);
        }
    }

    @Override
    public Optional<Debiteur> getDebiteurByCode(String codeCreance) {
        return debiteurRepository.findByCodeCreance(codeCreance);
    }

    @Override
    public List<Debiteur> getDebiteursByName(String name) {
        return debiteurRepository.findByNomContainingIgnoreCase(name);
    }

    @Override
    public List<Debiteur> getDebiteursByFirstName(String firstName) {
        return debiteurRepository.findByPrenomContainingIgnoreCase(firstName);
    }

    @Override
    public List<Debiteur> getDebiteursByFullName(String name, String firstName) {
        Optional<Debiteur> debiteur = debiteurRepository.findByNomAndPrenom(name, firstName);
        return debiteur.map(List::of).orElse(List.of());
    }

    @Override
    public Optional<Debiteur> getDebiteurByEmail(String email) {
        return debiteurRepository.findByEmail(email);
    }

    @Override
    public Optional<Debiteur> getDebiteurByPhone(String phone) {
        return debiteurRepository.findByTelephone(phone);
    }

    @Override
    public List<Debiteur> getDebiteursByCity(String city) {
        return debiteurRepository.findByVilleContainingIgnoreCase(city);
    }

    @Override
    public List<Debiteur> getDebiteursByPostalCode(String postalCode) {
        return debiteurRepository.findByCodePostal(postalCode);
    }

    @Override
    public List<Debiteur> getDebiteursByCityAndPostalCode(String city, String postalCode) {
        return debiteurRepository.findByVilleAndCodePostal(city, postalCode);
    }

    @Override
    public List<Debiteur> getDebiteursWithElectedAddress() {
        return debiteurRepository.findDebiteursAvecAdresseElue();
    }

    @Override
    public List<Debiteur> searchDebiteurs(String searchTerm) {
        return debiteurRepository.findByNomOuPrenomContaining(searchTerm);
    }

    @Override
    public boolean existsByEmail(String email) {
        return debiteurRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return debiteurRepository.existsByTelephone(phone);
    }

    @Override
    public boolean existsByCode(String codeCreance) {
        return debiteurRepository.existsByCodeCreance(codeCreance);
    }
    
    // ==================== IMPLÉMENTATION DES MÉTHODES POUR TYPE ====================
    
    @Override
    public List<Debiteur> getDebiteursByType(Type type) {
        return debiteurRepository.findByType(type);
    }
    
    @Override
    public List<Debiteur> getDebiteursByTypeAndCity(Type type, String city) {
        return debiteurRepository.findByTypeAndVille(type, city);
    }
    
    @Override
    public List<Debiteur> getDebiteursByTypeAndPostalCode(Type type, String postalCode) {
        return debiteurRepository.findByTypeAndCodePostal(type, postalCode);
    }
    
    @Override
    public List<Debiteur> getDebiteursByTypeAndCityAndPostalCode(Type type, String city, String postalCode) {
        return debiteurRepository.findByTypeAndVilleAndCodePostal(type, city, postalCode);
    }
    
    @Override
    public List<Object[]> getDebiteurCountByType() {
        return debiteurRepository.compterDebiteursParType();
    }
    
    @Override
    public List<Object[]> getDebiteurCountByTypeAndCity() {
        return debiteurRepository.compterDebiteursParTypeEtVille();
    }
    
    @Override
    public List<Debiteur> getDebiteursPersonnePhysique() {
        return debiteurRepository.findDebiteursPersonnePhysique();
    }
    
    @Override
    public List<Debiteur> getDebiteursPersonneMorale() {
        return debiteurRepository.findDebiteursPersonneMorale();
    }
    
    @Override
    public List<Debiteur> getDebiteursWithoutType() {
        return debiteurRepository.findDebiteursSansType();
    }
}
