package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Debiteur;

import java.util.List;
import java.util.Optional;

public interface DebiteurService {
    
    // CRUD Operations
    Debiteur createDebiteur(Debiteur debiteur);
    Optional<Debiteur> getDebiteurById(Long id);
    List<Debiteur> getAllDebiteurs();
    Debiteur updateDebiteur(Long id, Debiteur debiteur);
    void deleteDebiteur(Long id);
    
    // Search Operations
    Optional<Debiteur> getDebiteurByCode(String codeCreance);
    List<Debiteur> getDebiteursByName(String name);
    List<Debiteur> getDebiteursByFirstName(String firstName);
    List<Debiteur> getDebiteursByFullName(String name, String firstName);
    Optional<Debiteur> getDebiteurByEmail(String email);
    Optional<Debiteur> getDebiteurByPhone(String phone);
    List<Debiteur> getDebiteursByCity(String city);
    List<Debiteur> getDebiteursByPostalCode(String postalCode);
    List<Debiteur> getDebiteursByCityAndPostalCode(String city, String postalCode);
    List<Debiteur> getDebiteursWithElectedAddress();
    List<Debiteur> searchDebiteurs(String searchTerm);
    
    // Validation Operations
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByCode(String codeCreance);
}
