# 🔧 Solution : Erreur "Data truncated for column 'change_type'"

## 🎯 Problème Identifié

L'erreur `Data truncated for column 'change_type' at row 1` se produit lors de l'insertion dans la table `audit_logs` lors de l'appel à `markAsCompleted`.

**Cause** : La colonne `change_type` dans la table MySQL `audit_logs` est probablement un **ENUM** avec des valeurs limitées qui ne correspondent pas à toutes les valeurs de l'enum Java `TypeChangementAudit`.

Le code essaie d'insérer `DOCUMENT_UPDATE`, mais cette valeur n'existe peut-être pas dans l'ENUM MySQL.

**Stack trace** :
```
at projet.carthagecreance_backend.Service.Impl.DocumentHuissierServiceImpl.markAsCompleted(DocumentHuissierServiceImpl.java:182)
```

---

## ✅ Solution

### **ÉTAPE 1 : Vérifier la Structure de la Table**

Dans phpMyAdmin, exécutez :

```sql
DESCRIBE audit_logs;
```

**OU**

```sql
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = 'carthage_creances'
    AND TABLE_NAME = 'audit_logs'
    AND COLUMN_NAME = 'change_type';
```

Cela vous montrera si la colonne `change_type` est un ENUM et quelles valeurs elle accepte.

---

### **ÉTAPE 2 : Corriger la Colonne**

**Option RECOMMANDÉE : Changer en VARCHAR**

C'est la solution la plus flexible. Dans phpMyAdmin, exécutez :

```sql
ALTER TABLE audit_logs 
MODIFY COLUMN change_type VARCHAR(50) NOT NULL;
```

**Option 2 : Mettre à jour l'ENUM avec toutes les valeurs**

Si vous voulez garder un ENUM, ajoutez toutes les valeurs de `TypeChangementAudit` :

```sql
ALTER TABLE audit_logs 
MODIFY COLUMN change_type ENUM(
    'DOCUMENT_CREATE',
    'DOCUMENT_UPDATE',
    'DOSSIER_CREATE',
    'DOSSIER_UPDATE',
    'DOSSIER_DELETE',
    'AMOUNT_UPDATE',
    'ACTION_CREATE',
    'ACTION_UPDATE',
    'ACTION_DELETE'
) NOT NULL;
```

**⚠️ ATTENTION** : Vérifiez d'abord toutes les valeurs dans `TypeChangementAudit.java` avant d'exécuter cette commande.

---

### **ÉTAPE 3 : Vérifier**

Après la modification, vérifiez avec :

```sql
DESCRIBE audit_logs;
```

**Résultat attendu** :
```
change_type | varchar(50) | NO | NULL
```

---

### **ÉTAPE 4 : Redémarrer le Serveur**

Après avoir corrigé la colonne :

1. **Arrêtez** le serveur Spring Boot
2. **Redémarrez** le serveur
3. **Testez** l'endpoint :
   ```
   PUT http://localhost:8089/carthage-creance/api/huissier/document/1/complete
   ```

---

## 🔍 Vérification des Valeurs de TypeChangementAudit

Pour voir toutes les valeurs possibles, consultez :
- `src/main/java/projet/carthagecreance_backend/Entity/TypeChangementAudit.java`

Valeurs connues :
- `DOCUMENT_CREATE`
- `DOCUMENT_UPDATE`
- `AMOUNT_UPDATE`
- `ACTION_CREATE`
- (et d'autres...)

---

## 📝 Script SQL Complet

Un script SQL complet est disponible dans : `CORRIGER_TABLE_AUDIT_LOGS.sql`

---

## ✅ Après Correction

Une fois la colonne corrigée, l'endpoint `/complete` devrait fonctionner correctement sans erreur de transaction rollback.

**Note** : L'erreur de transaction rollback est une **conséquence** de l'erreur d'insertion dans `audit_logs`. Une fois la colonne corrigée, la transaction devrait se committer correctement.

