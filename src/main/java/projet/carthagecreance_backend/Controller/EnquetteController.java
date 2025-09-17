package projet.carthagecreance_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.Entity.Enquette;
import projet.carthagecreance_backend.Service.EnquetteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/enquettes")
@CrossOrigin(origins = "*")
public class EnquetteController {

    @Autowired
    private EnquetteService enquetteService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<Enquette> createEnquette(@RequestBody Enquette enquette) {
        try {
            Enquette createdEnquette = enquetteService.createEnquette(enquette);
            return new ResponseEntity<>(createdEnquette, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Enquette> getEnquetteById(@PathVariable Long id) {
        Optional<Enquette> enquette = enquetteService.getEnquetteById(id);
        return enquette.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Enquette>> getAllEnquettes() {
        List<Enquette> enquettes = enquetteService.getAllEnquettes();
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Enquette> updateEnquette(@PathVariable Long id, @RequestBody Enquette enquette) {
        try {
            Enquette updatedEnquette = enquetteService.updateEnquette(id, enquette);
            return new ResponseEntity<>(updatedEnquette, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnquette(@PathVariable Long id) {
        try {
            enquetteService.deleteEnquette(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Search Operations
    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<Enquette> getEnquetteByDossier(@PathVariable Long dossierId) {
        Optional<Enquette> enquette = enquetteService.getEnquetteByDossier(dossierId);
        return enquette.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/creation-date/{date}")
    public ResponseEntity<List<Enquette>> getEnquettesByCreationDate(@PathVariable LocalDate date) {
        List<Enquette> enquettes = enquetteService.getEnquettesByCreationDate(date);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/creation-date-range")
    public ResponseEntity<List<Enquette>> getEnquettesByCreationDateRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        List<Enquette> enquettes = enquetteService.getEnquettesByCreationDateRange(startDate, endDate);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/sector/{sector}")
    public ResponseEntity<List<Enquette>> getEnquettesBySector(@PathVariable String sector) {
        List<Enquette> enquettes = enquetteService.getEnquettesBySector(sector);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/legal-form/{legalForm}")
    public ResponseEntity<List<Enquette>> getEnquettesByLegalForm(@PathVariable String legalForm) {
        List<Enquette> enquettes = enquetteService.getEnquettesByLegalForm(legalForm);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/pdg/{pdg}")
    public ResponseEntity<List<Enquette>> getEnquettesByPDG(@PathVariable String pdg) {
        List<Enquette> enquettes = enquetteService.getEnquettesByPDG(pdg);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/capital-range")
    public ResponseEntity<List<Enquette>> getEnquettesByCapitalRange(
            @RequestParam Double minCapital, 
            @RequestParam Double maxCapital) {
        List<Enquette> enquettes = enquetteService.getEnquettesByCapitalRange(minCapital, maxCapital);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/revenue-range")
    public ResponseEntity<List<Enquette>> getEnquettesByRevenueRange(
            @RequestParam Double minRevenue, 
            @RequestParam Double maxRevenue) {
        List<Enquette> enquettes = enquetteService.getEnquettesByRevenueRange(minRevenue, maxRevenue);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/staff-range")
    public ResponseEntity<List<Enquette>> getEnquettesByStaffRange(
            @RequestParam Integer minStaff, 
            @RequestParam Integer maxStaff) {
        List<Enquette> enquettes = enquetteService.getEnquettesByStaffRange(minStaff, maxStaff);
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    // Special Operations
    @GetMapping("/with-real-estate")
    public ResponseEntity<List<Enquette>> getEnquettesWithRealEstate() {
        List<Enquette> enquettes = enquetteService.getEnquettesWithRealEstate();
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/with-movable-property")
    public ResponseEntity<List<Enquette>> getEnquettesWithMovableProperty() {
        List<Enquette> enquettes = enquetteService.getEnquettesWithMovableProperty();
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }

    @GetMapping("/with-observations")
    public ResponseEntity<List<Enquette>> getEnquettesWithObservations() {
        List<Enquette> enquettes = enquetteService.getEnquettesWithObservations();
        return new ResponseEntity<>(enquettes, HttpStatus.OK);
    }
}
