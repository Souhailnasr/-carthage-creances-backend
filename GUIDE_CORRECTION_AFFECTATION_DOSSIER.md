# 🔧 Guide Complet - Correction Problème Affectation Dossier

## 🎯 Objectif

Corriger la structure de la table `dossier_utilisateurs` pour permettre l'affectation des dossiers aux utilisateurs.

## ❌ Problème Actuel

La table `dossier_utilisateurs` a des colonnes redondantes avec des contraintes de clé étrangère qui empêchent leur suppression :
- `dossiers_id` (pluriel) - REDONDANT
- `utilisateurs_id` (pluriel) - REDONDANT avec contrainte FK
- `dossier_id` (singulier) - UTILISÉ PAR JPA ✅
- `utilisateur_id` (singulier) - UTILISÉ PAR JPA ✅

## ✅ Solution : Étapes à Suivre

### ÉTAPE 1 : Ouvrir phpMyAdmin

1. Ouvrez votre navigateur
2. Allez sur `http://localhost/phpmyadmin`
3. Sélectionnez la base de données `carthage_creances`
4. Cliquez sur la table `dossier_utilisateurs`

### ÉTAPE 2 : Vérifier la Structure Actuelle

1. Cliquez sur l'onglet **"Structure"**
2. Notez quelles colonnes existent
3. Notez les contraintes de clé étrangère listées dans la section "Indexes"

### ÉTAPE 3 : Identifier Toutes les Contraintes

1. Cliquez sur l'onglet **"SQL"**
2. Exécutez cette requête pour voir toutes les contraintes :

```sql
SELECT 
    CONSTRAINT_NAME, 
    COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs';
```

3. **Notez tous les noms de contraintes** (FK9pgk4cpeaa53jbc8xifuirxbv, FK7qab2690496t98ral, FKI3p4vah2dskmyyfe, etc.)

### ÉTAPE 4 : Supprimer TOUTES les Contraintes

Dans l'onglet **"SQL"**, exécutez ces commandes **UNE PAR UNE** :

```sql
-- Supprimer la contrainte identifiée dans l'erreur
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY FK9pgk4cpeaa53jbc8xifuirxbv;

-- Supprimer les autres contraintes (remplacez par les noms réels trouvés à l'étape 3)
ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY FK7qab2690496t98ral;

ALTER TABLE dossier_utilisateurs 
DROP FOREIGN KEY FKI3p4vah2dskmyyfe;
```

**Important** :
- Si une contrainte n'existe pas, vous aurez une erreur, **ignorez-la et continuez**
- Si vous avez trouvé d'autres contraintes à l'étape 3, supprimez-les aussi :
  ```sql
  ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY [NOM_CONTRAINTE];
  ```

### ÉTAPE 5 : Vérifier qu'il ne Reste Plus de Contraintes

Exécutez cette requête :

```sql
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs'
  AND CONSTRAINT_NAME LIKE 'FK%';
```

Si cette requête retourne encore des contraintes, supprimez-les toutes.

### ÉTAPE 6 : Supprimer les Colonnes Redondantes

Maintenant que toutes les contraintes sont supprimées, supprimez les colonnes :

```sql
-- Supprimer utilisateurs_id (si elle existe)
ALTER TABLE dossier_utilisateurs DROP COLUMN utilisateurs_id;

-- Si vous obtenez une erreur "#1091 - Can't DROP COLUMN", c'est que la colonne n'existe pas
-- C'est normal, continuez quand même
```

**Note** : Si `dossiers_id` existe encore, supprimez-la aussi :
```sql
ALTER TABLE dossier_utilisateurs DROP COLUMN dossiers_id;
```

### ÉTAPE 7 : Ajouter la Clé Primaire Composite

```sql
ALTER TABLE dossier_utilisateurs 
ADD PRIMARY KEY (dossier_id, utilisateur_id);
```

**Si vous obtenez une erreur** "Duplicate key name 'PRIMARY'", c'est que la clé primaire existe déjà, **ignorez l'erreur et continuez**.

### ÉTAPE 8 : Ajouter les Nouvelles Clés Étrangères

```sql
-- Clé étrangère vers dossier
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;

-- Clé étrangère vers utilisateur
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;
```

**Si vous obtenez une erreur** "Duplicate key name", c'est que la contrainte existe déjà, **ignorez l'erreur**.

### ÉTAPE 9 : Vérifier la Structure Finale

Exécutez :

```sql
DESCRIBE dossier_utilisateurs;
```

**Résultat attendu** :

| Field          | Type        | Null | Key | Default | Extra |
|----------------|-------------|------|-----|---------|-------|
| dossier_id     | bigint(20)   | NO   | PRI | NULL    |       |
| utilisateur_id | bigint(20)   | NO   | PRI | NULL    |       |

La table doit avoir **uniquement 2 colonnes** :
- `dossier_id` (PRIMARY KEY)
- `utilisateur_id` (PRIMARY KEY)

### ÉTAPE 10 : Tester l'Affectation

1. Redémarrez votre application Spring Boot
2. Testez l'affectation d'un dossier :
   - Appelez `PUT /api/dossiers/{id}/affecter/recouvrement-amiable`
   - Vérifiez qu'il n'y a plus d'erreur
3. Vérifiez dans phpMyAdmin :
   - Allez dans la table `dossier_utilisateurs`
   - Cliquez sur "Browse"
   - Vous devriez voir les associations créées

## 📋 Script SQL Complet (À Exécuter dans l'Ordre)

```sql
-- 1. Identifier toutes les contraintes
SELECT 
    CONSTRAINT_NAME, 
    COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs';

-- 2. Supprimer toutes les contraintes (UNE PAR UNE)
ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY FK9pgk4cpeaa53jbc8xifuirxbv;
ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY FK7qab2690496t98ral;
ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY FKI3p4vah2dskmyyfe;
-- (Ajoutez les autres contraintes trouvées à l'étape 1)

-- 3. Vérifier qu'il ne reste plus de contraintes
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'carthage_creances' 
  AND TABLE_NAME = 'dossier_utilisateurs'
  AND CONSTRAINT_NAME LIKE 'FK%';

-- 4. Supprimer les colonnes redondantes
ALTER TABLE dossier_utilisateurs DROP COLUMN utilisateurs_id;
-- (Ignorez l'erreur si la colonne n'existe pas)

-- 5. Ajouter la clé primaire composite
ALTER TABLE dossier_utilisateurs 
ADD PRIMARY KEY (dossier_id, utilisateur_id);
-- (Ignorez l'erreur si la clé primaire existe déjà)

-- 6. Ajouter les clés étrangères
ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_dossier 
FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;

ALTER TABLE dossier_utilisateurs
ADD CONSTRAINT fk_dossier_utilisateurs_utilisateur 
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE;
-- (Ignorez les erreurs si les contraintes existent déjà)

-- 7. Vérifier la structure finale
DESCRIBE dossier_utilisateurs;
```

## ⚠️ Notes Importantes

1. **Exécutez les commandes UNE PAR UNE** : Ne copiez pas tout le script d'un coup
2. **Ignorez les erreurs normales** :
   - "Foreign key doesn't exist" → La contrainte n'existe pas, continuez
   - "Column doesn't exist" → La colonne n'existe pas, continuez
   - "Primary key already exists" → La clé primaire existe déjà, continuez
   - "Duplicate key name" → La contrainte existe déjà, continuez
3. **Vérifiez après chaque étape** : Utilisez `DESCRIBE dossier_utilisateurs;` pour voir l'état
4. **Sauvegarde** : La table est actuellement vide, donc pas de risque de perte de données

## ✅ Résultat Attendu

Après correction :
- ✅ La table `dossier_utilisateurs` a uniquement 2 colonnes
- ✅ La clé primaire composite est créée
- ✅ Les clés étrangères sont correctement configurées
- ✅ L'affectation des dossiers fonctionne sans erreur
- ✅ JPA peut insérer correctement dans la table

## 🎯 Checklist

- [ ] Étape 1 : Ouvrir phpMyAdmin
- [ ] Étape 2 : Vérifier la structure actuelle
- [ ] Étape 3 : Identifier toutes les contraintes
- [ ] Étape 4 : Supprimer toutes les contraintes
- [ ] Étape 5 : Vérifier qu'il ne reste plus de contraintes
- [ ] Étape 6 : Supprimer les colonnes redondantes
- [ ] Étape 7 : Ajouter la clé primaire composite
- [ ] Étape 8 : Ajouter les nouvelles clés étrangères
- [ ] Étape 9 : Vérifier la structure finale
- [ ] Étape 10 : Tester l'affectation

## 🆘 En Cas de Problème

Si vous avez encore des erreurs :

1. **Vérifiez toutes les contraintes** :
   ```sql
   SELECT CONSTRAINT_NAME 
   FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
   WHERE TABLE_SCHEMA = 'carthage_creances' 
     AND TABLE_NAME = 'dossier_utilisateurs';
   ```

2. **Supprimez-les toutes** :
   ```sql
   ALTER TABLE dossier_utilisateurs DROP FOREIGN KEY [NOM_CONTRAINTE];
   ```

3. **Recréez la table si nécessaire** :
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

---

**Une fois ces étapes terminées, l'affectation des dossiers devrait fonctionner correctement !**

