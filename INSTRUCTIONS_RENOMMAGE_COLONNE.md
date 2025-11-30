# 🔧 Instructions pour Renommer la Colonne dans phpMyAdmin

## ✅ Problème Identifié

La colonne dans la base de données s'appelle **`hussier_id`** (avec une faute - il manque un 'i') alors que le code Java attend **`huissier_id`**.

C'est exactement ce qui cause l'erreur "Transaction silently rolled back" !

---

## 🛠️ Solution : Renommer la Colonne

### **Méthode 1 : Via l'Interface Graphique (Recommandé)**

1. Dans phpMyAdmin, cliquez sur la table **`audiences`** dans le sidebar
2. Cliquez sur l'onglet **"Structure"**
3. Trouvez la ligne avec **`hussier_id`** (colonne #10)
4. Cliquez sur **"Modifier"** (icône crayon) à droite de cette ligne
5. Dans le champ **"Name"**, changez `hussier_id` en **`huissier_id`**
6. Cliquez sur **"Save"** (Enregistrer)

### **Méthode 2 : Via SQL**

1. Dans phpMyAdmin, cliquez sur l'onglet **"SQL"**
2. Copiez et exécutez cette commande :

```sql
ALTER TABLE audiences 
CHANGE COLUMN hussier_id huissier_id BIGINT NULL;
```

3. Cliquez sur **"Go"** (Exécuter)

---

## ✅ Vérification

Après avoir renommé la colonne, vérifiez avec :

```sql
DESCRIBE audiences;
```

Vous devriez maintenant voir **`huissier_id`** (avec deux 'i') au lieu de `hussier_id`.

---

## 🎯 Après la Correction

Une fois la colonne renommée :

1. **Redémarrez le serveur Spring Boot**
2. **Testez à nouveau la création d'audience**
3. **L'erreur devrait être résolue !**

---

**C'est exactement le problème ! Une fois la colonne renommée, tout devrait fonctionner. 🎉**

