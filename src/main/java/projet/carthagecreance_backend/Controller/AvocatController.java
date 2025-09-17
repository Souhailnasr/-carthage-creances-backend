package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Avocat;
import projet.carthagecreance_backend.Service.AvocatService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/avocats")
@CrossOrigin(origins = "*")
public class AvocatController {

    @Autowired
    private AvocatService avocatService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<Avocat> createAvocat(@RequestBody Avocat avocat) {
        try {
            Avocat createdAvocat = avocatService.createAvocat(avocat);
            return new ResponseEntity<>(createdAvocat, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Avocat> getAvocatById(@PathVariable Long id) {
        Optional<Avocat> avocat = avocatService.getAvocatById(id);
        return avocat.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Avocat>> getAllAvocats() {
        List<Avocat> avocats = avocatService.getAllAvocats();
        return new ResponseEntity<>(avocats, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Avocat> updateAvocat(@PathVariable Long id, @RequestBody Avocat avocat) {
        try {
            Avocat updatedAvocat = avocatService.updateAvocat(id, avocat);
            return new ResponseEntity<>(updatedAvocat, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvocat(@PathVariable Long id) {
        try {
            avocatService.deleteAvocat(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Search Operations
    @GetMapping("/search/name")
    public ResponseEntity<List<Avocat>> getAvocatsByName(@RequestParam String name) {
        List<Avocat> avocats = avocatService.getAvocatsByName(name);
        return new ResponseEntity<>(avocats, HttpStatus.OK);
    }

    @GetMapping("/search/firstname")
    public ResponseEntity<List<Avocat>> getAvocatsByFirstName(@RequestParam String firstName) {
        List<Avocat> avocats = avocatService.getAvocatsByFirstName(firstName);
        return new ResponseEntity<>(avocats, HttpStatus.OK);
    }

    @GetMapping("/search/fullname")
    public ResponseEntity<List<Avocat>> getAvocatsByFullName(
            @RequestParam String name, 
            @RequestParam String firstName) {
        List<Avocat> avocats = avocatService.getAvocatsByFullName(name, firstName);
        return new ResponseEntity<>(avocats, HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Avocat> getAvocatByEmail(@PathVariable String email) {
        Optional<Avocat> avocat = avocatService.getAvocatByEmail(email);
        return avocat.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Avocat> getAvocatByPhone(@PathVariable String phone) {
        Optional<Avocat> avocat = avocatService.getAvocatByPhone(phone);
        return avocat.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<Avocat>> getAvocatsBySpecialty(@PathVariable String specialty) {
        List<Avocat> avocats = avocatService.getAvocatsBySpecialty(specialty);
        return new ResponseEntity<>(avocats, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Avocat>> searchAvocats(@RequestParam String searchTerm) {
        List<Avocat> avocats = avocatService.searchAvocats(searchTerm);
        return new ResponseEntity<>(avocats, HttpStatus.OK);
    }

    // Validation Operations
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = avocatService.existsByEmail(email);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @GetMapping("/exists/phone/{phone}")
    public ResponseEntity<Boolean> existsByPhone(@PathVariable String phone) {
        boolean exists = avocatService.existsByPhone(phone);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
}
