# 🔧 Correction Erreur Affectation Dossier - dossier_utilisateurs

## ❌ Erreur Actuelle

```
Field 'dossiers_id' doesn't have a default value
[insert into dossier_utilisateurs (dossier_id,utilisateur_id) values (?,?)]
```

## 🔍 Diagnostic

Le problème vient de la structure de la table `dossier_utilisateurs` dans la base de données. La table a probablement un champ `dossiers_id` (au pluriel) qui :
1. N'est pas nullable (NOT NULL)
2. N'a pas de valeur par défaut
3. N'est pas inclus dans l'INSERT généré par JPA

Le mapping JPA dans `Dossier.java` utilise `dossier_id` (au singulier) :
```java
@JoinTable(
    name = "dossier_utilisateurs",
    joinColumns = @JoinColumn(name = "dossier_id"),
    inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
)
```

## ✅ Solutions

### Solution 1 : Supprimer le champ dossiers_id (RECOMMANDÉE)

Si `dossiers_id` et `dossier_id` sont redondants, supprimez `dossiers_id` :

```sql
ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS dossiers_id;
```

### Solution 2 : Rendre dossiers_id nullable

Si `dossiers_id` doit exister pour une raison spécifique :

```sql
ALTER TABLE dossier_utilisateurs MODIFY COLUMN dossiers_id BIGINT DEFAULT NULL;
```

### Solution 3 : Donner une valeur par défaut à dossiers_id

```sql
ALTER TABLE dossier_utilisateurs MODIFY COLUMN dossiers_id BIGINT DEFAULT 0;
```

### Solution 4 : Modifier le mapping JPA (si dossiers_id est nécessaire)

Si `dossiers_id` est vraiment nécessaire, modifiez l'entité pour l'inclure. Mais cela nécessiterait une entité de jointure personnalisée au lieu d'utiliser `@ManyToMany`.

## 📋 Étapes de Correction

1. **Vérifier la structure de la table** :
   ```sql
   DESCRIBE dossier_utilisateurs;
   -- ou
   SHOW CREATE TABLE dossier_utilisateurs;
   ```

2. **Exécuter le script de correction** :
   - Ouvrez `fix_dossier_utilisateurs_table.sql`
   - Exécutez la commande appropriée selon votre cas

3. **Vérifier que la table a la bonne structure** :
   ```sql
   DESCRIBE dossier_utilisateurs;
   ```
   
   La table devrait avoir :
   - `dossier_id` (BIGINT, NOT NULL)
   - `utilisateur_id` (BIGINT, NOT NULL)
   - Clé primaire composite sur (dossier_id, utilisateur_id)
   - Clés étrangères vers `dossier` et `utilisateur`

4. **Tester l'affectation** :
   - Appelez `PUT /api/dossiers/{id}/affecter/recouvrement-amiable`
   - Vérifiez qu'il n'y a plus d'erreur

## 🔧 Code Backend (Déjà Correct)

Le code dans `DossierServiceImpl.affecterAuRecouvrementAmiable()` est correct :

```java
// Initialiser la liste utilisateurs si elle est null
if (dossier.getUtilisateurs() == null) {
    dossier.setUtilisateurs(new ArrayList<>());
}

// Ajouter le chef à la liste des utilisateurs associés
if (!dossier.getUtilisateurs().contains(chefAmiable)) {
    dossier.getUtilisateurs().add(chefAmiable);
}

// Ajouter les agents
for (Utilisateur agent : agentsAmiables) {
    if (!dossier.getUtilisateurs().contains(agent)) {
        dossier.getUtilisateurs().add(agent);
    }
}

// Sauvegarder
return dossierRepository.save(dossier);
```

JPA/Hibernate génère automatiquement l'INSERT dans `dossier_utilisateurs` avec `dossier_id` et `utilisateur_id`. Le problème vient uniquement de la structure de la table.

## ✅ Solution Rapide

Exécutez simplement :

```sql
ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS dossiers_id;
```

Puis testez à nouveau l'affectation.

## 📝 Note

Si vous avez besoin de conserver `dossiers_id` pour une raison spécifique (par exemple, pour la compatibilité avec d'anciens scripts), vous devrez :
1. Créer une entité de jointure `DossierUtilisateur`
2. Utiliser `@OneToMany` au lieu de `@ManyToMany`
3. Gérer manuellement les insertions

Mais dans la plupart des cas, `dossiers_id` est redondant avec `dossier_id` et peut être supprimé.

