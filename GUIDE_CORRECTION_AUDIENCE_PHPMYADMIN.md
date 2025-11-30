# 🔧 Guide de Correction - Vérification de la Table Audiences dans phpMyAdmin

## ❌ Erreur Rencontrée

```
#1109 - Unknown table 'audience' in information_schema
```

## ✅ Solution

Le problème vient du fait que vous essayez d'exécuter plusieurs requêtes en même temps dans phpMyAdmin. **Exécutez-les UNE PAR UNE**.

---

## 📋 Étapes à Suivre

### **ÉTAPE 1 : Vérifier la Structure de la Table (MÉTHODE SIMPLE)**

Dans phpMyAdmin, dans l'onglet **SQL**, exécutez cette requête **SEULE** :

```sql
DESCRIBE audiences;
```

**OU** utilisez l'onglet **Structure** de la table `audiences` dans phpMyAdmin (plus simple).

**Résultat attendu** : Vous devriez voir toutes les colonnes de la table, y compris :
- `dossier_id`
- `avocat_id`
- `huissier_id` (ou `hussier_id` si c'est mal nommé)

---

### **ÉTAPE 2 : Si la Colonne s'appelle "hussier_id" (avec faute)**

Si vous voyez `hussier_id` au lieu de `huissier_id`, exécutez cette commande :

```sql
ALTER TABLE audiences CHANGE COLUMN hussier_id huissier_id BIGINT NULL;
```

**⚠️ ATTENTION** : Exécutez cette commande **UNIQUEMENT** si la colonne s'appelle vraiment `hussier_id`.

---

### **ÉTAPE 3 : Vérifier les Données**

Pour voir les données existantes :

```sql
SELECT id, dossier_id, avocat_id, huissier_id, date_audience 
FROM audiences 
LIMIT 10;
```

---

### **ÉTAPE 4 : Voir la Structure Complète**

Pour voir la structure complète avec les contraintes :

```sql
SHOW CREATE TABLE audiences;
```

---

## 🎯 Méthode Alternative (Plus Simple)

Au lieu d'utiliser SQL, vous pouvez :

1. **Cliquer sur la table `audiences`** dans le sidebar de phpMyAdmin
2. **Cliquer sur l'onglet "Structure"**
3. **Vérifier visuellement** si la colonne s'appelle `huissier_id` ou `hussier_id`
4. **Si c'est `hussier_id`**, cliquez sur "Modifier" et renommez-la en `huissier_id`

---

## ✅ Vérifications à Faire

- [ ] La colonne s'appelle bien `huissier_id` (et non `hussier_id`)
- [ ] La colonne est de type `BIGINT`
- [ ] La colonne est `NULL` (nullable = YES)
- [ ] Il n'y a pas d'erreur lors de la requête `DESCRIBE audiences;`

---

## 🚨 Si le Problème Persiste

Si vous continuez à avoir des erreurs :

1. **Vérifiez que vous êtes dans la bonne base de données** : `carthage_creances`
2. **Vérifiez que la table existe** : `SHOW TABLES LIKE 'audiences';`
3. **Exécutez les requêtes UNE PAR UNE**, pas toutes en même temps
4. **Utilisez l'interface graphique** de phpMyAdmin (onglet Structure) au lieu de SQL

---

**Bon diagnostic ! 🔍**

