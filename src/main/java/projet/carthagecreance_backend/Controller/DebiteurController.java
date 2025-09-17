package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Debiteur;
import projet.carthagecreance_backend.Service.DebiteurService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/debiteurs")
@CrossOrigin(origins = "*")
public class DebiteurController {

    @Autowired
    private DebiteurService debiteurService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<Debiteur> createDebiteur(@RequestBody Debiteur debiteur) {
        try {
            Debiteur createdDebiteur = debiteurService.createDebiteur(debiteur);
            return new ResponseEntity<>(createdDebiteur, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Debiteur> getDebiteurById(@PathVariable Long id) {
        Optional<Debiteur> debiteur = debiteurService.getDebiteurById(id);
        return debiteur.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Debiteur>> getAllDebiteurs() {
        List<Debiteur> debiteurs = debiteurService.getAllDebiteurs();
        return new ResponseEntity<>(debiteurs, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Debiteur> updateDebiteur(@PathVariable Long id, @RequestBody Debiteur debiteur) {
        try {
            Debiteur updatedDebiteur = debiteurService.updateDebiteur(id, debiteur);
            return new ResponseEntity<>(updatedDebiteur, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDebiteur(@PathVariable Long id) {
        try {
            debiteurService.deleteDebiteur(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Search Operations
    @GetMapping("/code/{codeCreance}")
    public ResponseEntity<Debiteur> getDebiteurByCode(@PathVariable String codeCreance) {
        Optional<Debiteur> debiteur = debiteurService.getDebiteurByCode(codeCreance);
        return debiteur.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Debiteur>> getDebiteursByName(@RequestParam String name) {
        List<Debiteur> debiteurs = debiteurService.getDebiteursByName(name);
        return new ResponseEntity<>(debiteurs, HttpStatus.OK);
    }

    @GetMapping("/search/firstname")
    public ResponseEntity<List<Debiteur>> getDebiteursByFirstName(@RequestParam String firstName) {
        List<Debiteur> debiteurs = debiteurService.getDebiteursByFirstName(firstName);
        return new ResponseEntity<>(debiteurs, HttpStatus.OK);
    }

    @GetMapping("/search/fullname")
    public ResponseEntity<List<Debiteur>> getDebiteursByFullName(
            @RequestParam String name, 
            @RequestParam String firstName) {
        List<Debiteur> debiteurs = debiteurService.getDebiteursByFullName(name, firstName);
        return new ResponseEntity<>(debiteurs, HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Debiteur> getDebiteurByEmail(@PathVariable String email) {
        Optional<Debiteur> debiteur = debiteurService.getDebiteurByEmail(email);
        return debiteur.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Debiteur> getDebiteurByPhone(@PathVariable String phone) {
        Optional<Debiteur> debiteur = debiteurService.getDebiteurByPhone(phone);
        return debiteur.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Debiteur>> getDebiteursByCity(@PathVariable String city) {
        List<Debiteur> debiteurs = debiteurService.getDebiteursByCity(city);
        return new ResponseEntity<>(debiteurs, HttpStatus.OK);
    }

    @GetMapping("/postal-code/{postalCode}")
    public ResponseEntity<List<Debiteur>> getDebiteursByPostalCode(@PathVariable String postalCode) {
        List<Debiteur> debiteurs = debiteurService.getDebiteursByPostalCode(postalCode);
        return new ResponseEntity<>(debiteurs, HttpStatus.OK);
    }

    @GetMapping("/city-postal")
    public ResponseEntity<List<Debiteur>> getDebiteursByCityAndPostalCode(
            @RequestParam String city, 
            @RequestParam String postalCode) {
        List<Debiteur> debiteurs = debiteurService.getDebiteursByCityAndPostalCode(city, postalCode);
        return new ResponseEntity<>(debiteurs, HttpStatus.OK);
    }

    @GetMapping("/with-elected-address")
    public ResponseEntity<List<Debiteur>> getDebiteursWithElectedAddress() {
        List<Debiteur> debiteurs = debiteurService.getDebiteursWithElectedAddress();
        return new ResponseEntity<>(debiteurs, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Debiteur>> searchDebiteurs(@RequestParam String searchTerm) {
        List<Debiteur> debiteurs = debiteurService.searchDebiteurs(searchTerm);
        return new ResponseEntity<>(debiteurs, HttpStatus.OK);
    }

    // Validation Operations
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = debiteurService.existsByEmail(email);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @GetMapping("/exists/phone/{phone}")
    public ResponseEntity<Boolean> existsByPhone(@PathVariable String phone) {
        boolean exists = debiteurService.existsByPhone(phone);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @GetMapping("/exists/code/{codeCreance}")
    public ResponseEntity<Boolean> existsByCode(@PathVariable String codeCreance) {
        boolean exists = debiteurService.existsByCode(codeCreance);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
}
