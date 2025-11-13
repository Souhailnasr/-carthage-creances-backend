# Explication du Problème de Suppression d'Enquête

## Problème Observé

1. **L'enquête se supprime de l'interface** (message de succès affiché)
2. **L'enquête réapparaît** après un rafraîchissement
3. **L'enquête reste dans la base de données** (visible dans phpMyAdmin)

## Cause Racine

Le problème vient de la **gestion silencieuse des exceptions** dans le contrôleur et d'une possible **contrainte de base de données** liée à la relation `@OneToOne` entre `Enquette` et `Dossier`.

### Analyse du Code Actuel

**Controller (`EnquetteController.java`)** :
```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteEnquette(@PathVariable Long id) {
    try {
        enquetteService.deleteEnquette(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204
    } catch (RuntimeException e) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
    }
}
```

**Problème** : Le contrôleur catch seulement `RuntimeException`, mais :
- Les exceptions JPA/Hibernate (`DataIntegrityViolationException`, `ConstraintViolationException`) ne sont pas des `RuntimeException` directes
- Si une exception de contrainte se produit, elle peut être catchée par le `GlobalExceptionHandler` et retourner 500, mais le frontend peut ne pas le voir
- Ou la transaction est rollback silencieusement et le contrôleur retourne quand même 204

**Service (`EnquetteServiceImpl.java`)** :
```java
@Transactional
public void deleteEnquette(Long id) {
    // Vérifier que l'enquête existe
    if (!enquetteRepository.existsById(id)) {
        throw new RuntimeException("Enquette not found with id: " + id);
    }
    
    // Supprimer toutes les validations associées
    List<ValidationEnquete> validations = validationEnqueteRepository.findByEnqueteId(id);
    if (!validations.isEmpty()) {
        validationEnqueteRepository.deleteAll(validations);
    }
    
    // Supprimer l'enquête
    enquetteRepository.deleteById(id);
}
```

**Problème potentiel** :
- La relation `@OneToOne` avec `Dossier` a `nullable = false`
- Si la base de données a une contrainte de clé étrangère qui empêche la suppression, `deleteById()` peut échouer silencieusement
- La transaction `@Transactional` peut être rollback sans lever d'exception visible

### Relation Problématique

**Enquette.java** :
```java
@OneToOne(optional = false)
@JoinColumn(name = "dossier_id", nullable = false)
private Dossier dossier;
```

**Dossier.java** :
```java
@OneToOne(mappedBy = "dossier", cascade = CascadeType.ALL)
private Enquette enquette;
```

**Problème** :
- `Enquette` a une clé étrangère `dossier_id` vers `Dossier`
- La contrainte `nullable = false` signifie que chaque `Enquette` DOIT avoir un `Dossier`
- Si on essaie de supprimer l'`Enquette` sans supprimer ou modifier le `Dossier`, la base de données peut rejeter la suppression
- La cascade `CascadeType.ALL` sur `Dossier` signifie que si on supprime `Dossier`, l'`Enquette` sera supprimée, mais l'inverse n'est pas vrai

## Scénario Probable

1. **Frontend envoie DELETE** `/api/enquettes/8`
2. **Backend reçoit la requête** et appelle `deleteEnquette(8)`
3. **Les validations sont supprimées** avec succès
4. **Tentative de suppression de l'enquête** : `enquetteRepository.deleteById(8)`
5. **La base de données rejette** la suppression à cause d'une contrainte (peut-être une contrainte de clé étrangère ou un trigger)
6. **Hibernate/JPA rollback la transaction** silencieusement
7. **Aucune exception n'est levée** (ou elle est catchée ailleurs)
8. **Le contrôleur retourne 204 NO_CONTENT** (pensant que tout s'est bien passé)
9. **Le frontend affiche le message de succès** et supprime l'enquête de la liste locale
10. **Quand le frontend recharge** la liste, l'enquête réapparaît car elle n'a jamais été supprimée

## Solutions

### Solution 1 : Améliorer la Gestion des Exceptions

Modifier le contrôleur pour catch toutes les exceptions et logger les erreurs :

```java
@DeleteMapping("/{id}")
public ResponseEntity<?> deleteEnquette(@PathVariable Long id) {
    try {
        enquetteService.deleteEnquette(id);
        // Vérifier que l'enquête a bien été supprimée
        if (enquetteRepository.existsById(id)) {
            logger.error("L'enquête {} n'a pas été supprimée malgré l'appel à deleteEnquette", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la suppression de l'enquête");
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (DataIntegrityViolationException e) {
        logger.error("Contrainte de base de données empêche la suppression de l'enquête {}", id, e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("Impossible de supprimer l'enquête : contrainte de base de données");
    } catch (RuntimeException e) {
        logger.error("Erreur lors de la suppression de l'enquête {}", id, e);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
        logger.error("Erreur inattendue lors de la suppression de l'enquête {}", id, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Erreur serveur lors de la suppression");
    }
}
```

### Solution 2 : Vérifier la Suppression Réelle

Modifier le service pour vérifier que la suppression a bien eu lieu :

```java
@Transactional
public void deleteEnquette(Long id) {
    // Vérifier que l'enquête existe
    if (!enquetteRepository.existsById(id)) {
        throw new RuntimeException("Enquette not found with id: " + id);
    }
    
    // Supprimer toutes les validations associées
    List<ValidationEnquete> validations = validationEnqueteRepository.findByEnqueteId(id);
    if (!validations.isEmpty()) {
        validationEnqueteRepository.deleteAll(validations);
    }
    
    // Supprimer l'enquête
    enquetteRepository.deleteById(id);
    
    // Vérifier que la suppression a bien eu lieu
    if (enquetteRepository.existsById(id)) {
        throw new RuntimeException("L'enquête n'a pas pu être supprimée. Contrainte de base de données ?");
    }
}
```

### Solution 3 : Gérer la Relation avec Dossier

Si l'enquête ne peut pas être supprimée à cause de la relation avec `Dossier`, il faut soit :

**Option A** : Supprimer aussi le `Dossier` (si c'est le comportement souhaité)
```java
@Transactional
public void deleteEnquette(Long id) {
    Enquette enquette = enquetteRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Enquette not found with id: " + id));
    
    // Supprimer toutes les validations associées
    List<ValidationEnquete> validations = validationEnqueteRepository.findByEnqueteId(id);
    if (!validations.isEmpty()) {
        validationEnqueteRepository.deleteAll(validations);
    }
    
    // Récupérer le dossier associé
    Dossier dossier = enquette.getDossier();
    
    // Supprimer l'enquête
    enquetteRepository.deleteById(id);
    
    // Optionnel : Supprimer aussi le dossier si nécessaire
    // dossierRepository.deleteById(dossier.getId());
}
```

**Option B** : Détacher l'enquête du dossier avant suppression
```java
@Transactional
public void deleteEnquette(Long id) {
    Enquette enquette = enquetteRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Enquette not found with id: " + id));
    
    // Supprimer toutes les validations associées
    List<ValidationEnquete> validations = validationEnqueteRepository.findByEnqueteId(id);
    if (!validations.isEmpty()) {
        validationEnqueteRepository.deleteAll(validations);
    }
    
    // Détacher l'enquête du dossier
    Dossier dossier = enquette.getDossier();
    if (dossier != null) {
        dossier.setEnquette(null);
        dossierRepository.save(dossier);
    }
    
    // Supprimer l'enquête
    enquetteRepository.deleteById(id);
}
```

## Recommandation

**Solution recommandée** : Combiner Solution 1 + Solution 2 pour :
1. Logger toutes les erreurs
2. Vérifier que la suppression a bien eu lieu
3. Retourner des messages d'erreur clairs au frontend
4. Identifier la cause exacte du problème

Ensuite, selon la cause identifiée, appliquer la Solution 3 appropriée.

## Vérification

Pour identifier la cause exacte, ajoutez des logs détaillés :

```java
@Transactional
public void deleteEnquette(Long id) {
    logger.info("Début de la suppression de l'enquête {}", id);
    
    // Vérifier que l'enquête existe
    if (!enquetteRepository.existsById(id)) {
        logger.warn("Enquête {} n'existe pas", id);
        throw new RuntimeException("Enquette not found with id: " + id);
    }
    
    // Supprimer toutes les validations associées
    List<ValidationEnquete> validations = validationEnqueteRepository.findByEnqueteId(id);
    logger.info("Suppression de {} validations associées", validations.size());
    if (!validations.isEmpty()) {
        validationEnqueteRepository.deleteAll(validations);
        logger.info("Validations supprimées avec succès");
    }
    
    // Supprimer l'enquête
    logger.info("Tentative de suppression de l'enquête {}", id);
    try {
        enquetteRepository.deleteById(id);
        logger.info("deleteById() appelé pour l'enquête {}", id);
    } catch (Exception e) {
        logger.error("Erreur lors de l'appel à deleteById() pour l'enquête {}", id, e);
        throw e;
    }
    
    // Vérifier que la suppression a bien eu lieu
    boolean stillExists = enquetteRepository.existsById(id);
    if (stillExists) {
        logger.error("L'enquête {} existe toujours après deleteById() !", id);
        throw new RuntimeException("L'enquête n'a pas pu être supprimée");
    }
    
    logger.info("Enquête {} supprimée avec succès", id);
}
```

Ces logs permettront d'identifier exactement où le problème se produit.

