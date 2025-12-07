# 🔍 Vérification des Points Backend

## 🎯 Points à Vérifier

1. L'endpoint `/api/finances/dossier/{dossierId}/traitements` doit exister ou le fallback doit fonctionner correctement
2. La contrainte d'unicité pour les tarifs doit être (audienceId + categorie) et non seulement audienceId
3. Si le backend utilise avocatId pour les honoraires d'avocat, il faudra ajuster le mapping

---

## ✅ POINT 1 : Endpoint `/api/finances/dossier/{dossierId}/traitements`

### Status : ✅ **EXISTE ET FONCTIONNEL**

**Fichier :** `src/main/java/projet/carthagecreance_backend/Controller/FinanceController.java`  
**Ligne :** 249-259

```java
/**
 * GET /api/finances/dossier/{dossierId}/traitements
 * Récupère tous les traitements d'un dossier organisés par phase
 */
@GetMapping("/dossier/{dossierId}/traitements")
public ResponseEntity<?> getTraitementsDossier(@PathVariable Long dossierId) {
    try {
        TraitementsDossierDTO dto = tarifDossierService.getTraitementsDossier(dossierId);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    } catch (RuntimeException e) {
        return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
    } catch (Exception e) {
        return new ResponseEntity<>(java.util.Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

**Service implémenté :** `TarifDossierService.getTraitementsDossier()` dans `TarifDossierServiceImpl.java`

**✅ Conclusion :** L'endpoint existe et fonctionne correctement. Aucun fallback nécessaire.

---

## ⚠️ POINT 2 : Contrainte d'Unicité pour les Tarifs (audienceId + categorie)

### Status : ❌ **PROBLÈME IDENTIFIÉ**

### Analyse Actuelle

**Fichier :** `src/main/java/projet/carthagecreance_backend/Entity/TarifDossier.java`

**Problème :**
- ❌ **Aucune contrainte d'unicité** définie dans l'entité `TarifDossier`
- ❌ La méthode `findByDossierIdAndAudienceId()` dans le repository ne vérifie que `(dossierId, audienceId)`
- ❌ Il est possible de créer plusieurs tarifs pour la même audience avec des catégories différentes, mais aussi plusieurs tarifs pour la même audience avec la même catégorie

**Code actuel :**
```java
@Entity
@Table(name = "tarif_dossier")
public class TarifDossier {
    // ...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audience_id")
    @JsonIgnore
    private Audience audience;
    
    @Column(nullable = false, length = 100)
    private String categorie;
    // ...
}
```

**Repository actuel :**
```java
@Query("SELECT t FROM TarifDossier t WHERE t.dossier.id = :dossierId AND t.audience.id = :audienceId")
Optional<TarifDossier> findByDossierIdAndAudienceId(@Param("dossierId") Long dossierId, @Param("audienceId") Long audienceId);
```

**Problème identifié :**
- La méthode `findByDossierIdAndAudienceId()` ne prend pas en compte la `categorie`
- Il est possible d'avoir plusieurs tarifs pour la même audience avec la même catégorie
- La contrainte d'unicité devrait être : `(dossierId, audienceId, categorie)` ou au minimum `(audienceId, categorie)`

### Solution Requise

**1. Ajouter une contrainte d'unicité dans l'entité :**

```java
@Entity
@Table(name = "tarif_dossier", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"audience_id", "categorie"}, 
                            name = "uk_tarif_audience_categorie")
       })
public class TarifDossier {
    // ...
}
```

**2. Modifier le repository pour inclure la catégorie :**

```java
@Query("SELECT t FROM TarifDossier t WHERE t.dossier.id = :dossierId AND t.audience.id = :audienceId AND t.categorie = :categorie")
Optional<TarifDossier> findByDossierIdAndAudienceIdAndCategorie(
    @Param("dossierId") Long dossierId, 
    @Param("audienceId") Long audienceId,
    @Param("categorie") String categorie);
```

**3. Modifier la méthode `createTarif()` pour vérifier l'unicité :**

```java
@Override
public TarifDossierDTO createTarif(Long dossierId, TarifDossierRequest request) {
    // ...
    
    // Vérifier l'unicité si audienceId est fourni
    if (request.getAudienceId() != null && request.getCategorie() != null) {
        Optional<TarifDossier> existing = tarifDossierRepository
            .findByDossierIdAndAudienceIdAndCategorie(dossierId, request.getAudienceId(), request.getCategorie());
        
        if (existing.isPresent()) {
            throw new RuntimeException("Un tarif existe déjà pour cette audience (" + request.getAudienceId() + 
                                    ") avec la catégorie (" + request.getCategorie() + ")");
        }
    }
    
    // ...
}
```

**4. Créer une migration SQL pour ajouter la contrainte :**

```sql
-- Ajouter la contrainte d'unicité (audience_id, categorie)
ALTER TABLE tarif_dossier
ADD CONSTRAINT uk_tarif_audience_categorie 
UNIQUE (audience_id, categorie);
```

**⚠️ Note importante :** Avant d'ajouter la contrainte, vérifier qu'il n'y a pas de doublons dans la base de données :

```sql
-- Vérifier les doublons existants
SELECT audience_id, categorie, COUNT(*) as count
FROM tarif_dossier
WHERE audience_id IS NOT NULL
GROUP BY audience_id, categorie
HAVING COUNT(*) > 1;
```

---

## ⚠️ POINT 3 : Utilisation d'avocatId pour les Honoraires d'Avocat

### Status : ⚠️ **NÉCESSITE VÉRIFICATION**

### Analyse Actuelle

**Fichier :** `src/main/java/projet/carthagecreance_backend/Service/Impl/TarifDossierServiceImpl.java`  
**Lignes :** 316-330

**Code actuel :**
```java
// Tarif pour l'avocat (si présent)
if (audience.getAvocat() != null) {
    // Chercher un tarif avec catégorie "AVOCAT" pour cette audience
    List<TarifDossier> tarifsAvocat = tarifDossierRepository.findByDossierId(dossierId).stream()
        .filter(t -> t.getPhase() == PhaseFrais.JURIDIQUE && 
                t.getCategorie().contains("AVOCAT") &&
                t.getAudience() != null && t.getAudience().getId().equals(audience.getId()))
        .collect(Collectors.toList());
    
    if (!tarifsAvocat.isEmpty()) {
        TarifDossier tarifAvocat = tarifsAvocat.get(0);
        dto.setCoutAvocat(tarifAvocat.getCoutUnitaire());
        dto.setTarifAvocat(mapToTarifDTO(tarifAvocat));
    }
}
```

**Problème identifié :**
- ❌ Le code cherche les tarifs d'avocat en filtrant par `categorie.contains("AVOCAT")` et `audience.getId()`
- ❌ Il n'utilise **PAS** directement `avocatId` pour identifier le tarif
- ❌ Le tarif d'avocat est lié à l'audience, pas directement à l'avocat

**Entité TarifDossier :**
- ❌ Il n'y a **PAS** de champ `avocatId` dans `TarifDossier`
- ✅ Le tarif d'avocat est identifié par :
  - `audienceId` (via la relation `audience`)
  - `categorie` contenant "AVOCAT"

**Entité Audience :**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "avocat_id", nullable = true)
private Avocat avocat;
```

### Solution Requise

**Option 1 : Ajouter avocatId dans TarifDossierRequest (Recommandé)**

**Problème :** `TarifDossierRequest` n'a **PAS** de champ `avocatId`. Si le frontend veut créer un tarif d'avocat, il doit actuellement envoyer `audienceId`, mais il pourrait vouloir envoyer `avocatId` directement.

**Modifications requises :**

**1. Ajouter avocatId dans TarifDossierRequest :**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifDossierRequest {
    // ... champs existants ...
    private Long audienceId;
    private Long avocatId;  // ✅ NOUVEAU : Pour les honoraires d'avocat
    private Long enqueteId;
}
```

**2. Modifier `createTarif()` pour gérer avocatId :**
```java
@Override
public TarifDossierDTO createTarif(Long dossierId, TarifDossierRequest request) {
    // ...
    
    // Si avocatId est fourni (pour honoraires d'avocat)
    if (request.getAvocatId() != null && request.getCategorie() != null && 
        request.getCategorie().contains("AVOCAT")) {
        
        // Trouver l'audience associée à cet avocat pour ce dossier
        List<Audience> audiences = audienceRepository.findByDossierId(dossierId).stream()
            .filter(a -> a.getAvocat() != null && a.getAvocat().getId().equals(request.getAvocatId()))
            .collect(Collectors.toList());
        
        if (audiences.isEmpty()) {
            throw new RuntimeException("Aucune audience trouvée pour l'avocat " + request.getAvocatId() + 
                                    " dans le dossier " + dossierId);
        }
        
        // Utiliser la première audience trouvée (ou la plus récente)
        Audience audience = audiences.stream()
            .max(Comparator.comparing(Audience::getDateAudience))
            .orElse(audiences.get(0));
        
        tarif.setAudience(audience);
    }
    
    // ...
}
```

**Option 2 : Ajouter un champ avocatId dans TarifDossier (Approche alternative)**

**Avantages :**
- Plus direct pour les honoraires d'avocat
- Permet de créer des tarifs d'avocat sans audience

**Modification de l'entité :**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "avocat_id")
@JsonIgnore
private Avocat avocat;
```

**Modification du repository :**
```java
@Query("SELECT t FROM TarifDossier t WHERE t.dossier.id = :dossierId AND t.avocat.id = :avocatId AND t.categorie = :categorie")
Optional<TarifDossier> findByDossierIdAndAvocatIdAndCategorie(
    @Param("dossierId") Long dossierId, 
    @Param("avocatId") Long avocatId,
    @Param("categorie") String categorie);
```

**⚠️ Recommandation :** Utiliser l'**Option 1** (ajouter avocatId dans TarifDossierRequest) car :
- Plus intuitif pour le frontend (pas besoin de connaître l'audienceId)
- Permet de créer des honoraires d'avocat directement
- Le backend peut trouver l'audience associée automatiquement
- Si plusieurs audiences pour le même avocat, utiliser la plus récente

---

## 📋 Résumé des Actions Requises

### ✅ Point 1 : Endpoint `/api/finances/dossier/{dossierId}/traitements`
- **Status :** ✅ Existe et fonctionne
- **Action :** Aucune action requise

### ⚠️ Point 2 : Contrainte d'unicité (audienceId + categorie)
- **Status :** ❌ Problème identifié
- **Actions requises :**
  1. Ajouter contrainte d'unicité dans l'entité `TarifDossier`
  2. Créer migration SQL pour ajouter la contrainte
  3. Modifier le repository pour inclure `categorie` dans la recherche
  4. Modifier `createTarif()` pour vérifier l'unicité avant création
  5. Vérifier et nettoyer les doublons existants dans la base

### ⚠️ Point 3 : Utilisation d'avocatId pour honoraires
- **Status :** ⚠️ Nécessite ajustement
- **Actions requises :**
  1. Ajouter champ `avocatId` dans `TarifDossierRequest`
  2. Modifier `createTarif()` pour gérer `avocatId` dans la requête
  3. Si `avocatId` est fourni ET `categorie` contient "AVOCAT" :
     - Trouver l'audience associée à cet avocat pour ce dossier
     - Utiliser l'audience la plus récente si plusieurs
     - Créer le tarif avec `audienceId` + `categorie`
  4. Documenter le mapping : `avocatId` → `audienceId` + `categorie`

---

## 🔧 Prompts pour Implémentation

### Prompt 1 : Ajouter Contrainte d'Unicité

```
Je dois ajouter une contrainte d'unicité pour les tarifs d'audience dans l'entité TarifDossier.

**Contexte :**
- Fichier : TarifDossier.java
- Problème : Actuellement, il est possible de créer plusieurs tarifs pour la même audience avec la même catégorie
- Solution : Ajouter une contrainte d'unicité sur (audience_id, categorie)

**À faire :**

1. Modifier l'annotation @Table dans TarifDossier.java :
   - Ajouter uniqueConstraints avec audience_id et categorie

2. Créer une migration SQL :
   - Vérifier d'abord les doublons existants
   - Ajouter la contrainte UNIQUE (audience_id, categorie)

3. Modifier TarifDossierRepository :
   - Ajouter méthode findByDossierIdAndAudienceIdAndCategorie()

4. Modifier TarifDossierServiceImpl.createTarif() :
   - Vérifier l'unicité avant de créer un nouveau tarif
   - Lever une exception si un tarif existe déjà pour (audienceId, categorie)
```

### Prompt 2 : Ajouter avocatId et gérer dans createTarif()

```
Je dois ajouter le support de avocatId pour créer des tarifs d'honoraires d'avocat.

**Contexte :**
- Fichier 1 : TarifDossierRequest.java (DTO)
- Fichier 2 : TarifDossierServiceImpl.java (Service)
- Problème : TarifDossierRequest n'a pas de champ avocatId, le frontend doit actuellement envoyer audienceId

**À faire :**

1. Dans TarifDossierRequest.java :
   - Ajouter champ private Long avocatId;

2. Dans TarifDossierServiceImpl.createTarif() :
   - Après la validation du dossier, vérifier si request.getAvocatId() != null
   - Si avocatId est fourni ET categorie contient "AVOCAT" :
     - Trouver les audiences associées à cet avocat pour ce dossier
     - Si aucune audience trouvée, lever une exception explicite
     - Utiliser l'audience la plus récente (max dateAudience) si plusieurs
     - Définir tarif.setAudience(audience)
   - Si audienceId est aussi fourni, prioriser audienceId (plus explicite)

3. Vérifier l'unicité :
   - Utiliser findByDossierIdAndAudienceIdAndCategorie()
   - Lever une exception si un tarif existe déjà pour (audienceId, categorie)

4. Documenter :
   - Le mapping avocatId → audienceId + categorie
   - Les catégories acceptées : "AVOCAT", "HONORAIRES_AVOCAT", etc.
   - Priorité : audienceId > avocatId (si les deux sont fournis)
```

---

**Date de création :** 2025-01-05  
**Status :** ✅ Vérification complétée - Actions requises identifiées


