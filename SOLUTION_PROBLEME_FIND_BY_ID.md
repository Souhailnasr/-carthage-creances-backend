# Solution au Problème : findById() échoue alors que l'enquête existe

## Problème Identifié

L'erreur `Enquette not found with id: 8` se produit alors que l'enquête **existe bien dans la base de données**.

**Cause** : `findById()` essaie de charger l'entité `Enquette` avec sa relation `@OneToOne` avec `Dossier`. Si :
- Le `Dossier` référencé par `dossier_id` n'existe plus
- Le `Dossier` est dans un état invalide
- Il y a un problème de lazy loading

Alors `findById()` échoue même si l'enquête existe dans la table `enquette`.

## Solution Appliquée

### 1. Ajout de Requêtes Natives dans le Repository

**Fichier** : `EnquetteRepository.java`

Ajout de deux nouvelles méthodes qui utilisent des requêtes SQL natives pour éviter le chargement des relations :

```java
// Vérifier l'existence d'une enquête par ID (sans charger les relations)
@Query(value = "SELECT COUNT(*) > 0 FROM enquette WHERE id = :id", nativeQuery = true)
boolean existsByIdNative(@Param("id") Long id);

// Trouver une enquête par ID sans charger la relation Dossier (pour la suppression)
@Query(value = "SELECT * FROM enquette WHERE id = :id", nativeQuery = true)
Optional<Enquette> findByIdNative(@Param("id") Long id);
```

### 2. Modification de la Méthode deleteEnquette()

**Fichier** : `EnquetteServiceImpl.java`

**Changements** :
- Utilisation de `existsByIdNative()` au lieu de `findById()` pour vérifier l'existence
- Utilisation de `deleteById()` directement (ne nécessite pas de charger l'entité)
- Ajout de logs détaillés pour le débogage
- Vérification post-suppression avec `existsByIdNative()`

**Code** :
```java
@Transactional
public void deleteEnquete(Long id) {
    // Vérifier avec une requête native (évite le chargement des relations)
    boolean exists = enquetteRepository.existsByIdNative(id);
    
    if (!exists) {
        throw new RuntimeException("Enquette not found with id: " + id);
    }
    
    // Supprimer les validations associées
    List<ValidationEnquete> validations = validationEnqueteRepository.findByEnqueteId(id);
    if (!validations.isEmpty()) {
        validationEnqueteRepository.deleteAll(validations);
    }
    
    // Supprimer l'enquête directement (ne charge pas l'entité)
    enquetteRepository.deleteById(id);
    
    // Vérifier la suppression avec une requête native
    if (enquetteRepository.existsByIdNative(id)) {
        throw new RuntimeException("L'enquête n'a pas pu être supprimée");
    }
}
```

## Pourquoi ça fonctionne maintenant ?

1. **Requête native** : `existsByIdNative()` fait une requête SQL directe `SELECT COUNT(*) > 0 FROM enquette WHERE id = :id` qui ne charge pas les relations
2. **deleteById()** : Cette méthode ne nécessite pas de charger l'entité complète, elle fait juste `DELETE FROM enquette WHERE id = :id`
3. **Pas de lazy loading** : On évite complètement le chargement de la relation `Dossier` qui causait le problème

## Vérification dans la Base de Données

### Vérifier si le Dossier existe

Exécutez cette requête SQL dans phpMyAdmin :

```sql
-- Vérifier l'enquête et son dossier
SELECT 
    e.id as enquete_id,
    e.rapport_code,
    e.dossier_id,
    d.id as dossier_exists,
    d.numero_dossier
FROM enquette e
LEFT JOIN dossier d ON e.dossier_id = d.id
WHERE e.id = 8;
```

**Si `dossier_exists` est `NULL`** : Le dossier référencé n'existe plus. C'est probablement la cause du problème avec `findById()`.

**Solution** : Mettre à jour ou supprimer la référence au dossier :

```sql
-- Option 1 : Mettre à jour pour pointer vers un dossier existant
UPDATE enquette SET dossier_id = (SELECT id FROM dossier LIMIT 1) WHERE id = 8;

-- Option 2 : Supprimer la référence (si la relation le permet)
-- UPDATE enquette SET dossier_id = NULL WHERE id = 8; -- Peut ne pas fonctionner si nullable = false
```

## Test de la Solution

1. **Redémarrez le serveur backend**
2. **Testez la suppression** avec Postman :
   ```
   DELETE http://localhost:8089/carthage-creance/api/enquettes/8
   Authorization: Bearer {token}
   ```
3. **Vérifiez les logs** dans la console du backend :
   ```
   Enquête ID 8 trouvée dans la base de données
   Nombre de validations à supprimer: X
   Validations supprimées avec succès
   deleteById() appelé pour l'enquête 8
   Vérification: L'enquête 8 a bien été supprimée de la base de données
   ```
4. **Vérifiez dans phpMyAdmin** que l'enquête a bien été supprimée

## Si le Problème Persiste

Si vous voyez toujours l'erreur après ces modifications, vérifiez :

1. **Le dossier référencé existe-t-il ?**
   ```sql
   SELECT * FROM dossier WHERE id = (SELECT dossier_id FROM enquette WHERE id = 8);
   ```

2. **Y a-t-il des contraintes de clé étrangère ?**
   ```sql
   SHOW CREATE TABLE enquette;
   ```

3. **Les logs du backend** indiqueront exactement où ça bloque

## Résumé

- ✅ Utilisation de requêtes natives pour éviter le lazy loading
- ✅ `deleteById()` au lieu de charger puis supprimer l'entité
- ✅ Logs détaillés pour le débogage
- ✅ Vérification post-suppression

La suppression devrait maintenant fonctionner même si la relation `Dossier` pose problème.


