package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Huissier;
import projet.carthagecreance_backend.Service.HuissierService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/huissiers")
@CrossOrigin(origins = "*")
public class HuissierController {

    @Autowired
    private HuissierService huissierService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<Huissier> createHuissier(@RequestBody Huissier huissier) {
        try {
            Huissier createdHuissier = huissierService.createHuissier(huissier);
            return new ResponseEntity<>(createdHuissier, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Huissier> getHuissierById(@PathVariable Long id) {
        Optional<Huissier> huissier = huissierService.getHuissierById(id);
        return huissier.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Huissier>> getAllHuissiers() {
        List<Huissier> huissiers = huissierService.getAllHuissiers();
        return new ResponseEntity<>(huissiers, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Huissier> updateHuissier(@PathVariable Long id, @RequestBody Huissier huissier) {
        try {
            Huissier updatedHuissier = huissierService.updateHuissier(id, huissier);
            return new ResponseEntity<>(updatedHuissier, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHuissier(@PathVariable Long id) {
        try {
            huissierService.deleteHuissier(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Search Operations
    @GetMapping("/search/name")
    public ResponseEntity<List<Huissier>> getHuissiersByName(@RequestParam String name) {
        List<Huissier> huissiers = huissierService.getHuissiersByName(name);
        return new ResponseEntity<>(huissiers, HttpStatus.OK);
    }

    @GetMapping("/search/firstname")
    public ResponseEntity<List<Huissier>> getHuissiersByFirstName(@RequestParam String firstName) {
        List<Huissier> huissiers = huissierService.getHuissiersByFirstName(firstName);
        return new ResponseEntity<>(huissiers, HttpStatus.OK);
    }

    @GetMapping("/search/fullname")
    public ResponseEntity<List<Huissier>> getHuissiersByFullName(
            @RequestParam String name, 
            @RequestParam String firstName) {
        List<Huissier> huissiers = huissierService.getHuissiersByFullName(name, firstName);
        return new ResponseEntity<>(huissiers, HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Huissier> getHuissierByEmail(@PathVariable String email) {
        Optional<Huissier> huissier = huissierService.getHuissierByEmail(email);
        return huissier.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Huissier> getHuissierByPhone(@PathVariable String phone) {
        Optional<Huissier> huissier = huissierService.getHuissierByPhone(phone);
        return huissier.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<Huissier>> getHuissiersBySpecialty(@PathVariable String specialty) {
        List<Huissier> huissiers = huissierService.getHuissiersBySpecialty(specialty);
        return new ResponseEntity<>(huissiers, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Huissier>> searchHuissiers(@RequestParam String searchTerm) {
        List<Huissier> huissiers = huissierService.searchHuissiers(searchTerm);
        return new ResponseEntity<>(huissiers, HttpStatus.OK);
    }

    @GetMapping("/with-dossiers")
    public ResponseEntity<List<Huissier>> getHuissiersWithDossiers() {
        List<Huissier> huissiers = huissierService.getHuissiersWithDossiers();
        return new ResponseEntity<>(huissiers, HttpStatus.OK);
    }

    @GetMapping("/without-dossiers")
    public ResponseEntity<List<Huissier>> getHuissiersWithoutDossiers() {
        List<Huissier> huissiers = huissierService.getHuissiersWithoutDossiers();
        return new ResponseEntity<>(huissiers, HttpStatus.OK);
    }

    // Validation Operations
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = huissierService.existsByEmail(email);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @GetMapping("/exists/phone/{phone}")
    public ResponseEntity<Boolean> existsByPhone(@PathVariable String phone) {
        boolean exists = huissierService.existsByPhone(phone);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
}
