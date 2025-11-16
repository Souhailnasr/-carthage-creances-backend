# Prompt Backend : Implémentation de l'Affectation et Clôture des Dossiers

## 🎯 PROMPT 1 : Implémentation des Méthodes d'Affectation dans DossierService

**Prompt à copier dans Cursor AI :**

```
Dans le fichier DossierService.java, ajoutez les méthodes suivantes à l'interface :

1. affecterAuRecouvrementAmiable(Long dossierId): Dossier
   - Affecte un dossier validé au chef du département recouvrement amiable
   - Retourne le dossier mis à jour

2. affecterAuRecouvrementJuridique(Long dossierId): Dossier
   - Affecte un dossier validé au chef du département recouvrement juridique
   - Retourne le dossier mis à jour

3. cloturerDossier(Long dossierId): Dossier
   - Clôture un dossier (change le statut à CLOTURE et met la date de clôture)
   - Retourne le dossier mis à jour

IMPORTANT : Ces méthodes doivent être ajoutées dans la section "Affectations" de l'interface.
```

---

## 🎯 PROMPT 2 : Implémentation des Méthodes dans DossierServiceImpl

**Prompt à copier dans Cursor AI :**

```
Dans le fichier DossierServiceImpl.java, implémentez les méthodes suivantes :

1. affecterAuRecouvrementAmiable(Long dossierId):
   - Vérifier que le dossier existe
   - Vérifier que le dossier a le statut VALIDE (sinon throw RuntimeException)
   - Trouver le chef du département recouvrement amiable (RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE)
   - Si aucun chef amiable trouvé, throw RuntimeException("Aucun chef du département recouvrement amiable trouvé")
   - Assigner ce chef comme agentResponsable du dossier
   - Mettre à jour dossierStatus si nécessaire
   - Sauvegarder et retourner le dossier

2. affecterAuRecouvrementJuridique(Long dossierId):
   - Vérifier que le dossier existe
   - Vérifier que le dossier a le statut VALIDE (sinon throw RuntimeException)
   - Trouver le chef du département recouvrement juridique (RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE)
   - Si aucun chef juridique trouvé, throw RuntimeException("Aucun chef du département recouvrement juridique trouvé")
   - Assigner ce chef comme agentResponsable du dossier
   - Mettre à jour dossierStatus si nécessaire
   - Sauvegarder et retourner le dossier

3. cloturerDossier(Long dossierId):
   - Vérifier que le dossier existe
   - Vérifier que le dossier a le statut VALIDE (sinon throw RuntimeException)
   - Mettre dossierStatus à DossierStatus.CLOTURE
   - Mettre dateCloture à la date actuelle (new Date())
   - Sauvegarder et retourner le dossier

4. Implémentez aussi assignerAgentResponsable(Long dossierId, Long agentId):
   - Vérifier que le dossier existe
   - Vérifier que l'agent existe
   - Assigner l'agent comme agentResponsable
   - Sauvegarder et retourner le dossier

CODE EXEMPLE :

```java
@Override
@Transactional
public Dossier affecterAuRecouvrementAmiable(Long dossierId) {
    // Vérifier que le dossier existe
    Dossier dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
    
    // Vérifier que le dossier est validé
    if (dossier.getStatut() != Statut.VALIDE || !Boolean.TRUE.equals(dossier.getValide())) {
        throw new RuntimeException("Seuls les dossiers validés peuvent être affectés au recouvrement amiable");
    }
    
    // Trouver le chef du département recouvrement amiable
    List<Utilisateur> chefsAmiables = utilisateurRepository.findByRoleUtilisateur(
        RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE
    );
    
    if (chefsAmiables.isEmpty()) {
        throw new RuntimeException("Aucun chef du département recouvrement amiable trouvé");
    }
    
    // Prendre le premier chef amiable disponible
    Utilisateur chefAmiable = chefsAmiables.get(0);
    
    // Assigner le chef comme agent responsable
    dossier.setAgentResponsable(chefAmiable);
    
    // Sauvegarder
    return dossierRepository.save(dossier);
}

@Override
@Transactional
public Dossier affecterAuRecouvrementJuridique(Long dossierId) {
    // Même logique que affecterAuRecouvrementAmiable mais avec CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE
    // ...
}

@Override
@Transactional
public Dossier cloturerDossier(Long dossierId) {
    Dossier dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
    
    // Vérifier que le dossier est validé
    if (dossier.getStatut() != Statut.VALIDE || !Boolean.TRUE.equals(dossier.getValide())) {
        throw new RuntimeException("Seuls les dossiers validés peuvent être clôturés");
    }
    
    // Clôturer le dossier
    dossier.setDossierStatus(DossierStatus.CLOTURE);
    dossier.setDateCloture(new java.util.Date());
    
    return dossierRepository.save(dossier);
}

@Override
@Transactional
public Dossier assignerAgentResponsable(Long dossierId, Long agentId) {
    Dossier dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
    
    Utilisateur agent = utilisateurRepository.findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + agentId));
    
    dossier.setAgentResponsable(agent);
    return dossierRepository.save(dossier);
}
```

IMPORTANT :
- Utiliser @Transactional pour toutes ces méthodes
- Gérer les erreurs avec des messages clairs
- Vérifier toujours que le dossier existe
- Vérifier que le dossier est VALIDE avant affectation/clôture
```

---

## 🎯 PROMPT 3 : Ajout des Endpoints dans DossierController

**Prompt à copier dans Cursor AI :**

```
Dans le fichier DossierController.java, ajoutez les endpoints suivants dans la section "ENDPOINTS D'AFFECTATION" :

1. PUT /api/dossiers/{id}/affecter/recouvrement-amiable
   - Appelle dossierService.affecterAuRecouvrementAmiable(id)
   - Retourne 200 OK avec le dossier mis à jour
   - Gère les erreurs 400 (dossier non validé, chef non trouvé) et 404 (dossier non trouvé)

2. PUT /api/dossiers/{id}/affecter/recouvrement-juridique
   - Appelle dossierService.affecterAuRecouvrementJuridique(id)
   - Retourne 200 OK avec le dossier mis à jour
   - Gère les erreurs 400 (dossier non validé, chef non trouvé) et 404 (dossier non trouvé)

3. PUT /api/dossiers/{id}/cloturer
   - Appelle dossierService.cloturerDossier(id)
   - Retourne 200 OK avec le dossier mis à jour
   - Gère les erreurs 400 (dossier non validé) et 404 (dossier non trouvé)

CODE EXEMPLE :

```java
/**
 * Affecte un dossier validé au recouvrement amiable
 * 
 * @param id L'ID du dossier à affecter
 * @return ResponseEntity avec le dossier mis à jour (200 OK) ou erreur
 * 
 * @example
 * PUT /api/dossiers/1/affecter/recouvrement-amiable
 */
@PutMapping("/{id}/affecter/recouvrement-amiable")
public ResponseEntity<?> affecterAuRecouvrementAmiable(@PathVariable Long id) {
    try {
        Dossier updatedDossier = dossierService.affecterAuRecouvrementAmiable(id);
        return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
    } catch (RuntimeException e) {
        logger.error("Erreur lors de l'affectation au recouvrement amiable: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Erreur d'affectation",
            "message", e.getMessage(),
            "timestamp", new Date().toString()
        ));
    } catch (Exception e) {
        logger.error("Erreur interne lors de l'affectation: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Erreur interne du serveur",
                    "message", "Erreur lors de l'affectation: " + e.getMessage(),
                    "timestamp", new Date().toString()
                ));
    }
}

/**
 * Affecte un dossier validé au recouvrement juridique
 * 
 * @param id L'ID du dossier à affecter
 * @return ResponseEntity avec le dossier mis à jour (200 OK) ou erreur
 * 
 * @example
 * PUT /api/dossiers/1/affecter/recouvrement-juridique
 */
@PutMapping("/{id}/affecter/recouvrement-juridique")
public ResponseEntity<?> affecterAuRecouvrementJuridique(@PathVariable Long id) {
    try {
        Dossier updatedDossier = dossierService.affecterAuRecouvrementJuridique(id);
        return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
    } catch (RuntimeException e) {
        logger.error("Erreur lors de l'affectation au recouvrement juridique: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Erreur d'affectation",
            "message", e.getMessage(),
            "timestamp", new Date().toString()
        ));
    } catch (Exception e) {
        logger.error("Erreur interne lors de l'affectation: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Erreur interne du serveur",
                    "message", "Erreur lors de l'affectation: " + e.getMessage(),
                    "timestamp", new Date().toString()
                ));
    }
}

/**
 * Clôture un dossier validé
 * 
 * @param id L'ID du dossier à clôturer
 * @return ResponseEntity avec le dossier mis à jour (200 OK) ou erreur
 * 
 * @example
 * PUT /api/dossiers/1/cloturer
 */
@PutMapping("/{id}/cloturer")
public ResponseEntity<?> cloturerDossier(@PathVariable Long id) {
    try {
        Dossier updatedDossier = dossierService.cloturerDossier(id);
        return new ResponseEntity<>(updatedDossier, HttpStatus.OK);
    } catch (RuntimeException e) {
        logger.error("Erreur lors de la clôture du dossier: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Erreur de clôture",
            "message", e.getMessage(),
            "timestamp", new Date().toString()
        ));
    } catch (Exception e) {
        logger.error("Erreur interne lors de la clôture: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Erreur interne du serveur",
                    "message", "Erreur lors de la clôture: " + e.getMessage(),
                    "timestamp", new Date().toString()
                ));
    }
}
```

IMPORTANT :
- Utiliser les mêmes patterns de gestion d'erreurs que les autres endpoints
- Logger les erreurs
- Retourner des messages d'erreur clairs
```

---

## 🎯 PROMPT 4 : Endpoint pour Récupérer les Dossiers Validés Disponibles

**Prompt à copier dans Cursor AI :**

```
Dans le fichier DossierController.java, ajoutez un endpoint pour récupérer les dossiers validés disponibles pour l'affectation :

GET /api/dossiers/valides-disponibles
- Retourne la liste des dossiers avec statut VALIDE et dossierStatus != CLOTURE
- Supporte la pagination, le tri et la recherche
- Paramètres optionnels : page, size, sort, search

CODE EXEMPLE :

```java
/**
 * Récupère les dossiers validés disponibles pour l'affectation
 * 
 * @param page Numéro de page (optionnel, défaut: 0)
 * @param size Taille de la page (optionnel, défaut: 10)
 * @param sort Champ de tri (optionnel, défaut: "dateCreation")
 * @param direction Direction du tri (optionnel, défaut: "DESC")
 * @param search Terme de recherche (optionnel)
 * @return ResponseEntity avec la liste paginée des dossiers validés
 * 
 * @example
 * GET /api/dossiers/valides-disponibles?page=0&size=10&sort=dateCreation&direction=DESC&search=Dossier61
 */
@GetMapping("/valides-disponibles")
public ResponseEntity<?> getDossiersValidesDisponibles(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "dateCreation") String sort,
        @RequestParam(defaultValue = "DESC") String direction,
        @RequestParam(required = false) String search) {
    try {
        Map<String, Object> result = dossierService.getDossiersValidesDisponibles(page, size, sort, direction, search);
        return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
        logger.error("Erreur lors de la récupération des dossiers validés: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Erreur interne du serveur",
                    "message", "Erreur lors de la récupération: " + e.getMessage()
                ));
    }
}
```

Dans DossierService.java, ajoutez la méthode :
```java
Map<String, Object> getDossiersValidesDisponibles(int page, int size, String sort, String direction, String search);
```

Dans DossierServiceImpl.java, implémentez la méthode pour filtrer les dossiers avec :
- statut = VALIDE
- valide = true
- dossierStatus != CLOTURE
- Recherche par numeroDossier, titre, etc.
```

---

## ✅ Checklist de Vérification Backend

- [ ] Les méthodes sont ajoutées dans DossierService.java
- [ ] Les méthodes sont implémentées dans DossierServiceImpl.java
- [ ] Les endpoints sont ajoutés dans DossierController.java
- [ ] La validation du statut VALIDE est effectuée
- [ ] La recherche des chefs par rôle fonctionne
- [ ] La clôture met à jour dossierStatus et dateCloture
- [ ] Les erreurs sont gérées avec des messages clairs
- [ ] Les transactions sont gérées avec @Transactional




