# 🔧 Solution : Erreur "Data truncated for column 'type'"

## 🎯 Problème Identifié

L'erreur `Data truncated for column 'type' at row 1` se produit lors de l'insertion dans la table `notifications`.

**Cause** : La colonne `type` dans la table MySQL est probablement un **ENUM** avec des valeurs limitées qui ne correspondent pas à toutes les valeurs de l'enum Java `TypeNotification`.

Le code essaie d'insérer `AUDIENCE_CREE`, mais cette valeur n'existe peut-être pas dans l'ENUM MySQL.

---

## ✅ Solution

### **ÉTAPE 1 : Vérifier la Structure de la Table**

Dans phpMyAdmin, exécutez :

```sql
DESCRIBE notifications;
```

**OU**

```sql
SHOW CREATE TABLE notifications;
```

Cela vous montrera si la colonne `type` est un ENUM et quelles valeurs elle accepte.

---

### **ÉTAPE 2 : Corriger la Colonne**

**Option 1 : Changer en VARCHAR (RECOMMANDÉ)**

C'est la solution la plus flexible. Dans phpMyAdmin, exécutez :

```sql
ALTER TABLE notifications 
MODIFY COLUMN type VARCHAR(50) NOT NULL;
```

**Option 2 : Mettre à jour l'ENUM avec toutes les valeurs**

Si vous voulez garder un ENUM, ajoutez toutes les valeurs :

```sql
ALTER TABLE notifications 
MODIFY COLUMN type ENUM(
    'DOSSIER_CREE',
    'DOSSIER_VALIDE',
    'DOSSIER_REJETE',
    'DOSSIER_EN_ATTENTE',
    'DOSSIER_AFFECTE',
    'DOSSIER_CLOTURE',
    'ENQUETE_CREE',
    'ENQUETE_VALIDE',
    'ENQUETE_REJETE',
    'ENQUETE_EN_ATTENTE',
    'ACTION_AMIABLE_CREE',
    'ACTION_AMIABLE_COMPLETEE',
    'AUDIENCE_PROCHAINE',
    'AUDIENCE_CREE',
    'AUDIENCE_REPORTEE',
    'TACHE_URGENTE',
    'TACHE_AFFECTEE',
    'TACHE_COMPLETEE',
    'TRAITEMENT_DOSSIER',
    'RAPPEL',
    'INFO',
    'NOTIFICATION_MANUELLE'
) NOT NULL;
```

---

### **ÉTAPE 3 : Vérifier**

Après la modification, vérifiez avec :

```sql
DESCRIBE notifications;
```

---

### **ÉTAPE 4 : Redémarrer le Serveur**

Après avoir corrigé la colonne :

1. **Arrêtez** le serveur Spring Boot
2. **Redémarrez** le serveur
3. **Testez** à nouveau la création d'audience

---

## 🎯 Pourquoi cette Erreur ?

Hibernate utilise `@Enumerated(EnumType.STRING)`, ce qui signifie qu'il stocke la valeur de l'enum comme une chaîne de caractères (ex: `"AUDIENCE_CREE"`).

Si la colonne MySQL est un ENUM avec seulement quelques valeurs (ex: `ENUM('DOSSIER_CREE', 'DOSSIER_VALIDE')`), MySQL rejette toute valeur qui n'est pas dans la liste.

**Solution** : Utiliser `VARCHAR` au lieu d'ENUM pour plus de flexibilité.

---

## ✅ Après la Correction

Une fois la colonne corrigée :

1. ✅ Les notifications pourront être créées avec n'importe quelle valeur de `TypeNotification`
2. ✅ La création d'audience fonctionnera correctement
3. ✅ L'erreur "Transaction silently rolled back" devrait disparaître

---

**Action immédiate : Exécutez `ALTER TABLE notifications MODIFY COLUMN type VARCHAR(50) NOT NULL;` dans phpMyAdmin ! 🎉**

