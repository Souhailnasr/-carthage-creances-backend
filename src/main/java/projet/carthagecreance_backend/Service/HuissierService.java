package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Huissier;

import java.util.List;
import java.util.Optional;

public interface HuissierService {
    
    // CRUD Operations
    Huissier createHuissier(Huissier huissier);
    Optional<Huissier> getHuissierById(Long id);
    List<Huissier> getAllHuissiers();
    Huissier updateHuissier(Long id, Huissier huissier);
    void deleteHuissier(Long id);
    
    // Search Operations
    List<Huissier> getHuissiersByName(String name);
    List<Huissier> getHuissiersByFirstName(String firstName);
    List<Huissier> getHuissiersByFullName(String name, String firstName);
    Optional<Huissier> getHuissierByEmail(String email);
    Optional<Huissier> getHuissierByPhone(String phone);
    List<Huissier> getHuissiersBySpecialty(String specialty);
    List<Huissier> searchHuissiers(String searchTerm);
    List<Huissier> getHuissiersWithDossiers();
    List<Huissier> getHuissiersWithoutDossiers();
    
    // Validation Operations
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
