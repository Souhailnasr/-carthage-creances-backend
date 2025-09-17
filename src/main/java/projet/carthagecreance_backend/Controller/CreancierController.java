package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Creancier;
import projet.carthagecreance_backend.Service.CreancierService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/creanciers")
@CrossOrigin(origins = "*")
public class CreancierController {

    @Autowired
    private CreancierService creancierService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<Creancier> createCreancier(@RequestBody Creancier creancier) {
        try {
            Creancier createdCreancier = creancierService.createCreancier(creancier);
            return new ResponseEntity<>(createdCreancier, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Creancier> getCreancierById(@PathVariable Long id) {
        Optional<Creancier> creancier = creancierService.getCreancierById(id);
        return creancier.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Creancier>> getAllCreanciers() {
        List<Creancier> creanciers = creancierService.getAllCreanciers();
        return new ResponseEntity<>(creanciers, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Creancier> updateCreancier(@PathVariable Long id, @RequestBody Creancier creancier) {
        try {
            Creancier updatedCreancier = creancierService.updateCreancier(id, creancier);
            return new ResponseEntity<>(updatedCreancier, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCreancier(@PathVariable Long id) {
        try {
            creancierService.deleteCreancier(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Search Operations
    @GetMapping("/code/{codeCreancier}")
    public ResponseEntity<Creancier> getCreancierByCode(@PathVariable String codeCreancier) {
        Optional<Creancier> creancier = creancierService.getCreancierByCode(codeCreancier);
        return creancier.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Creancier>> getCreanciersByName(@RequestParam String name) {
        List<Creancier> creanciers = creancierService.getCreanciersByName(name);
        return new ResponseEntity<>(creanciers, HttpStatus.OK);
    }

    @GetMapping("/search/firstname")
    public ResponseEntity<List<Creancier>> getCreanciersByFirstName(@RequestParam String firstName) {
        List<Creancier> creanciers = creancierService.getCreanciersByFirstName(firstName);
        return new ResponseEntity<>(creanciers, HttpStatus.OK);
    }

    @GetMapping("/search/fullname")
    public ResponseEntity<List<Creancier>> getCreanciersByFullName(
            @RequestParam String name, 
            @RequestParam String firstName) {
        List<Creancier> creanciers = creancierService.getCreanciersByFullName(name, firstName);
        return new ResponseEntity<>(creanciers, HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Creancier> getCreancierByEmail(@PathVariable String email) {
        Optional<Creancier> creancier = creancierService.getCreancierByEmail(email);
        return creancier.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Creancier> getCreancierByPhone(@PathVariable String phone) {
        Optional<Creancier> creancier = creancierService.getCreancierByPhone(phone);
        return creancier.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Creancier>> getCreanciersByCity(@PathVariable String city) {
        List<Creancier> creanciers = creancierService.getCreanciersByCity(city);
        return new ResponseEntity<>(creanciers, HttpStatus.OK);
    }

    @GetMapping("/postal-code/{postalCode}")
    public ResponseEntity<List<Creancier>> getCreanciersByPostalCode(@PathVariable String postalCode) {
        List<Creancier> creanciers = creancierService.getCreanciersByPostalCode(postalCode);
        return new ResponseEntity<>(creanciers, HttpStatus.OK);
    }

    @GetMapping("/city-postal")
    public ResponseEntity<List<Creancier>> getCreanciersByCityAndPostalCode(
            @RequestParam String city, 
            @RequestParam String postalCode) {
        List<Creancier> creanciers = creancierService.getCreanciersByCityAndPostalCode(city, postalCode);
        return new ResponseEntity<>(creanciers, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Creancier>> searchCreanciers(@RequestParam String searchTerm) {
        List<Creancier> creanciers = creancierService.searchCreanciers(searchTerm);
        return new ResponseEntity<>(creanciers, HttpStatus.OK);
    }

    // Validation Operations
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = creancierService.existsByEmail(email);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @GetMapping("/exists/phone/{phone}")
    public ResponseEntity<Boolean> existsByPhone(@PathVariable String phone) {
        boolean exists = creancierService.existsByPhone(phone);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @GetMapping("/exists/code/{codeCreancier}")
    public ResponseEntity<Boolean> existsByCode(@PathVariable String codeCreancier) {
        boolean exists = creancierService.existsByCode(codeCreancier);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
}
