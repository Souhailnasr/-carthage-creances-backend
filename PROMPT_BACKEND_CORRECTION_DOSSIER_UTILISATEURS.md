# 🔧 PROMPT BACKEND - Correction Erreur Affectation Dossier

## ❌ Erreur Actuelle

```
Field 'dossiers_id' doesn't have a default value
[insert into dossier_utilisateurs (dossier_id,utilisateur_id) values (?,?)]
```

## 🎯 PROMPT À COPIER DANS CURSOR AI (Backend)

```
Dans le projet Spring Boot backend, corrigez l'erreur lors de l'affectation d'un dossier au recouvrement amiable.

ERREUR ACTUELLE:
- Lors de l'appel PUT /api/dossiers/{dossierId}/affecter/recouvrement-amiable
- Erreur SQL: Field 'dossiers_id' doesn't have a default value
- Table concernée: dossier_utilisateurs
- INSERT: insert into dossier_utilisateurs (dossier_id,utilisateur_id) values (?,?)

PROBLÈME IDENTIFIÉ:
La table `dossier_utilisateurs` dans la base de données a probablement un champ `dossiers_id` (au pluriel) qui :
1. N'est pas nullable (NOT NULL)
2. N'a pas de valeur par défaut
3. N'est pas inclus dans l'INSERT généré par JPA

Le mapping JPA dans Dossier.java utilise `dossier_id` (au singulier) dans le @JoinTable, mais la table réelle a un champ `dossiers_id` qui est requis.

SOLUTION RECOMMANDÉE:

1. Vérifiez d'abord la structure de la table dans la base de données:
   Exécutez cette requête SQL:
   ```sql
   DESCRIBE dossier_utilisateurs;
   -- ou
   SHOW CREATE TABLE dossier_utilisateurs;
   ```

2. Si la table a un champ `dossiers_id` qui est redondant avec `dossier_id`, supprimez-le:
   ```sql
   ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS dossiers_id;
   ```

3. Si `dossiers_id` doit exister pour une raison spécifique, rendez-le nullable:
   ```sql
   ALTER TABLE dossier_utilisateurs MODIFY COLUMN dossiers_id BIGINT DEFAULT NULL;
   ```

4. Vérifiez que la table a la structure correcte après correction:
   ```sql
   DESCRIBE dossier_utilisateurs;
   ```
   
   La table devrait avoir:
   - `dossier_id` (BIGINT, NOT NULL) - utilisé par JPA
   - `utilisateur_id` (BIGINT, NOT NULL) - utilisé par JPA
   - Clé primaire composite sur (dossier_id, utilisateur_id)
   - Clés étrangères vers `dossier` et `utilisateur`

5. Si la table n'existe pas ou a une structure incorrecte, recréez-la:
   ```sql
   DROP TABLE IF EXISTS dossier_utilisateurs;
   
   CREATE TABLE dossier_utilisateurs (
       dossier_id BIGINT NOT NULL,
       utilisateur_id BIGINT NOT NULL,
       PRIMARY KEY (dossier_id, utilisateur_id),
       CONSTRAINT fk_dossier_utilisateurs_dossier 
           FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE,
       CONSTRAINT fk_dossier_utilisateurs_utilisateur 
           FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
   ```

6. Vérifiez que le mapping JPA dans Dossier.java est correct:
   Fichier: src/main/java/projet/carthagecreance_backend/Entity/Dossier.java
   
   Le mapping devrait être:
   ```java
   @ManyToMany
   @JoinTable(
       name = "dossier_utilisateurs",
       joinColumns = @JoinColumn(name = "dossier_id"),
       inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
   )
   @Builder.Default
   private List<Utilisateur> utilisateurs = new ArrayList<>();
   ```
   
   Ce mapping est DÉJÀ CORRECT dans le code actuel.

7. Le code dans DossierServiceImpl.affecterAuRecouvrementAmiable() est aussi correct:
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
   
   // Sauvegarder - JPA génère automatiquement l'INSERT
   return dossierRepository.save(dossier);
   ```

8. Après avoir corrigé la table, testez l'affectation:
   - Appelez PUT /api/dossiers/{id}/affecter/recouvrement-amiable
   - Vérifiez qu'il n'y a plus d'erreur
   - Vérifiez que les données sont bien insérées dans dossier_utilisateurs

IMPORTANT:
- Le problème vient UNIQUEMENT de la structure de la table dans la base de données
- Le code Java est déjà correct
- JPA/Hibernate génère automatiquement l'INSERT avec dossier_id et utilisateur_id
- Il faut juste s'assurer que la table n'a pas de champ dossiers_id requis qui n'est pas rempli

SCRIPT SQL DE CORRECTION RAPIDE:
Exécutez ce script SQL dans votre base de données:
```sql
-- Supprimer dossiers_id si redondant (SOLUTION RECOMMANDÉE)
ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS dossiers_id;

-- Vérifier la structure
DESCRIBE dossier_utilisateurs;
```

Si vous avez besoin de conserver dossiers_id pour une raison spécifique:
```sql
-- Rendre dossiers_id nullable
ALTER TABLE dossier_utilisateurs MODIFY COLUMN dossiers_id BIGINT DEFAULT NULL;
```

Mais dans la plupart des cas, dossiers_id est redondant avec dossier_id et peut être supprimé.
```

---

## 🔍 Étapes de Diagnostic

1. **Vérifier la structure de la table:**
   ```sql
   DESCRIBE dossier_utilisateurs;
   ```

2. **Vérifier les contraintes:**
   ```sql
   SHOW CREATE TABLE dossier_utilisateurs;
   ```

3. **Vérifier le mapping JPA:**
   - Le mapping dans `Dossier.java` utilise `dossier_id` (ligne 75)
   - C'est correct, pas besoin de modifier

4. **Vérifier le code d'insertion:**
   - Le code dans `DossierServiceImpl` ajoute les utilisateurs à la liste
   - JPA génère automatiquement l'INSERT
   - C'est correct, pas besoin de modifier

---

## ✅ Solution Rapide

**Exécutez simplement ce script SQL:**

```sql
ALTER TABLE dossier_utilisateurs DROP COLUMN IF EXISTS dossiers_id;
```

Puis testez à nouveau l'affectation. Le problème devrait être résolu.

---

## 📝 Fichiers Créés

1. **fix_dossier_utilisateurs_table.sql** - Script SQL de correction
2. **CORRECTION_ERREUR_DOSSIER_UTILISATEURS.md** - Documentation complète
3. **PROMPT_BACKEND_CORRECTION_DOSSIER_UTILISATEURS.md** - Ce prompt

---

## 🎯 Résumé

- **Problème**: Table `dossier_utilisateurs` a un champ `dossiers_id` requis qui n'est pas rempli
- **Cause**: Structure de table incorrecte (champ redondant)
- **Solution**: Supprimer `dossiers_id` ou le rendre nullable
- **Code Java**: Déjà correct, pas besoin de modification
- **Action requise**: Exécuter le script SQL de correction

