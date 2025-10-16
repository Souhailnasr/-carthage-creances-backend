package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Creancier;
import projet.carthagecreance_backend.Entity.Type;

import java.util.List;
import java.util.Optional;

public interface CreancierService {
    
    // CRUD Operations
    Creancier createCreancier(Creancier creancier);
    Optional<Creancier> getCreancierById(Long id);
    List<Creancier> getAllCreanciers();
    Creancier updateCreancier(Long id, Creancier creancier);
    void deleteCreancier(Long id);
    
    // Search Operations
    Optional<Creancier> getCreancierByCode(String codeCreancier);
    Optional<Creancier> getCreancierByCodeCreance(String codeCreance);
    List<Creancier> getCreanciersByName(String name);
    List<Creancier> getCreanciersByFirstName(String firstName);
    List<Creancier> getCreanciersByFullName(String name, String firstName);
    Optional<Creancier> getCreancierByEmail(String email);
    Optional<Creancier> getCreancierByPhone(String phone);
    List<Creancier> getCreanciersByCity(String city);
    List<Creancier> getCreanciersByPostalCode(String postalCode);
    List<Creancier> getCreanciersByCityAndPostalCode(String city, String postalCode);
    List<Creancier> searchCreanciers(String searchTerm);
    
    // Validation Operations
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByCode(String codeCreancier);
    boolean existsByCodeCreance(String codeCreance);
    
    // ==================== MÉTHODES POUR TYPE ====================
    
    // Search Operations by Type
    List<Creancier> getCreanciersByType(Type type);
    List<Creancier> getCreanciersByTypeAndCity(Type type, String city);
    List<Creancier> getCreanciersByTypeAndPostalCode(Type type, String postalCode);
    List<Creancier> getCreanciersByTypeAndCityAndPostalCode(Type type, String city, String postalCode);
    
    // Statistics Operations for Type
    List<Object[]> getCreancierCountByType();
    List<Object[]> getCreancierCountByTypeAndCity();
    
    // Specific Type Operations
    List<Creancier> getCreanciersPersonnePhysique();
    List<Creancier> getCreanciersPersonneMorale();
    List<Creancier> getCreanciersWithoutType();
}
