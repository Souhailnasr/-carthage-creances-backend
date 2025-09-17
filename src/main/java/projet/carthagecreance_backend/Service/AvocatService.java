package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Avocat;

import java.util.List;
import java.util.Optional;

public interface AvocatService {
    
    // CRUD Operations
    Avocat createAvocat(Avocat avocat);
    Optional<Avocat> getAvocatById(Long id);
    List<Avocat> getAllAvocats();
    Avocat updateAvocat(Long id, Avocat avocat);
    void deleteAvocat(Long id);
    
    // Search Operations
    List<Avocat> getAvocatsByName(String name);
    List<Avocat> getAvocatsByFirstName(String firstName);
    List<Avocat> getAvocatsByFullName(String name, String firstName);
    Optional<Avocat> getAvocatByEmail(String email);
    Optional<Avocat> getAvocatByPhone(String phone);
    List<Avocat> getAvocatsBySpecialty(String specialty);
    List<Avocat> searchAvocats(String searchTerm);
    
    // Validation Operations
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
