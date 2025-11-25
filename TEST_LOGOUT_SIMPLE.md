# 🧪 Test Simple : Vérifier pourquoi derniere_deconnexion reste NULL

## ⚡ Test Rapide en 3 Étapes

### Étape 1 : Tester l'Endpoint de Test

**Commande :**
```bash
POST http://localhost:8089/carthage-creance/auth/test-logout/1
```

**Remplacer `1` par l'ID d'un utilisateur réel de votre base.**

**Avec curl :**
```bash
curl -X POST "http://localhost:8089/carthage-creance/auth/test-logout/1"
```

**Résultat Attendu :**
```json
{
  "message": "Test logout effectué",
  "userId": 1,
  "email": "user@example.com",
  "avant": "NULL",
  "apres_set": "2025-11-25T18:30:00",
  "apres_save": "2025-11-25T18:30:00",
  "apres_verification": "2025-11-25T18:30:00"
}
```

**Si `apres_verification` est NULL :**
→ **Problème de sauvegarde JPA** (voir solution ci-dessous)

**Si `apres_verification` n'est PAS NULL :**
→ La sauvegarde fonctionne ! Le problème vient de l'appel logout normal.

---

### Étape 2 : Vérifier dans la Base de Données

**Après l'étape 1, exécutez dans phpMyAdmin :**

```sql
SELECT id, email, derniere_connexion, derniere_deconnexion 
FROM utilisateur 
WHERE id = 1;
```

**Si `derniere_deconnexion` est remplie :**
→ ✅ La sauvegarde fonctionne !

**Si `derniere_deconnexion` est toujours NULL :**
→ ❌ Problème de sauvegarde JPA (voir solution ci-dessous)

---

### Étape 3 : Tester le Logout Normal

**1. Se connecter pour obtenir un token :**
```bash
POST http://localhost:8089/carthage-creance/auth/authenticate
Body: {
  "email": "votre_email@example.com",
  "password": "votre_mot_de_passe"
}
```

**2. Copier le token de la réponse**

**3. Appeler le logout :**
```bash
POST http://localhost:8089/carthage-creance/auth/logout
Headers:
  Authorization: Bearer {votre_token}
  Content-Type: application/json
```

**4. Vérifier les logs backend :**
Vous devriez voir :
```
=== DÉBUT LOGOUT ===
Logout: Token JWT extrait (longueur: XXX)
Logout: Token trouvé, ID: XXX
Logout: Utilisateur trouvé - ID: XXX, Email: XXX
Logout: Avant mise à jour - derniere_deconnexion actuelle: null
Logout: Après setDerniereDeconnexion - valeur: 2025-11-25T...
Logout: Utilisateur sauvegardé (saveAndFlush) - ID: XXX, derniere_deconnexion: 2025-11-25T...
Logout: Vérification DB - derniere_deconnexion dans DB: 2025-11-25T...
Logout: ✅ SUCCÈS - derniere_deconnexion correctement sauvegardée: 2025-11-25T...
```

**5. Vérifier dans la base de données :**
```sql
SELECT id, email, derniere_connexion, derniere_deconnexion 
FROM utilisateur 
WHERE email = 'votre_email@example.com';
```

---

## 🔧 Solutions selon les Résultats

### Solution 1 : Si l'endpoint de test fonctionne mais pas le logout normal

**Problème :** Le LogoutService n'est pas appelé ou la transaction est annulée.

**Vérifications :**
1. Vérifier les logs backend lors du logout
2. Si vous ne voyez PAS "=== DÉBUT LOGOUT ===" → Le LogoutService n'est pas appelé
3. Si vous voyez "=== DÉBUT LOGOUT ===" mais "Vérification DB - derniere_deconnexion dans DB: null" → Problème de transaction

**Solution :**
Le code a déjà été modifié pour utiliser `REQUIRES_NEW` et `saveAndFlush`. Si ça ne fonctionne toujours pas, il faut vérifier que le frontend appelle bien `/auth/logout` avec le header Authorization.

---

### Solution 2 : Si l'endpoint de test ne fonctionne PAS (apres_verification est NULL)

**Problème :** Problème de mapping JPA ou de configuration de la base de données.

**Vérifications :**
1. Vérifier le type de la colonne dans la base :
```sql
DESCRIBE utilisateur;
```
La colonne `derniere_deconnexion` doit être de type `DATETIME` ou `TIMESTAMP`.

2. Vérifier que l'entité Java correspond :
```java
@Column(name = "derniere_deconnexion")
private LocalDateTime derniereDeconnexion;
```

**Solution :**
Si le type de colonne est incorrect, modifiez-le :
```sql
ALTER TABLE utilisateur 
MODIFY COLUMN derniere_deconnexion DATETIME NULL;
```

---

### Solution 3 : Si vous ne voyez AUCUN log "=== DÉBUT LOGOUT ==="

**Problème :** Le LogoutService n'est jamais appelé.

**Causes possibles :**
1. Le frontend n'appelle pas `/auth/logout`
2. Spring Security intercepte avant le contrôleur
3. Le LogoutHandler n'est pas enregistré

**Vérifications :**
1. Ouvrir F12 → Network dans le navigateur
2. Cliquer sur "Déconnexion"
3. Vérifier si une requête `POST /auth/logout` apparaît

**Si la requête n'apparaît PAS :**
→ Le frontend n'appelle pas l'endpoint (problème frontend)

**Si la requête apparaît mais Status est 404 :**
→ Problème de routing (vérifier l'URL)

**Si la requête apparaît et Status est 200 :**
→ Le LogoutService devrait être appelé (vérifier les logs backend)

---

## 📝 Checklist de Diagnostic

- [ ] L'endpoint `/auth/test-logout/{userId}` fonctionne et `apres_verification` n'est PAS NULL
- [ ] Dans la base, `derniere_deconnexion` est remplie après le test
- [ ] Les logs backend montrent "=== DÉBUT LOGOUT ===" lors du logout normal
- [ ] Les logs montrent "✅ SUCCÈS - derniere_deconnexion correctement sauvegardée"
- [ ] Dans Network (F12), la requête POST /auth/logout apparaît
- [ ] Le header Authorization est présent dans la requête
- [ ] Dans la base, `derniere_deconnexion` est remplie après le logout normal

**Si toutes les cases sont cochées sauf la dernière :**
→ Le problème vient de la transaction ou du flush (déjà corrigé avec `REQUIRES_NEW` et `saveAndFlush`)

**Si certaines cases ne sont pas cochées :**
→ Suivre les solutions correspondantes ci-dessus

---

## 🚨 Test d'Urgence : Forcer la Mise à Jour

Si rien ne fonctionne, utilisez cette requête SQL directe pour forcer la mise à jour :

```sql
UPDATE utilisateur 
SET derniere_deconnexion = NOW() 
WHERE id = 1;
```

**Si ça fonctionne :**
→ Le problème vient de JPA/Spring, pas de la base de données.

**Si ça ne fonctionne PAS :**
→ Problème de base de données (vérifier les permissions, les contraintes, etc.)

---

## 📞 Informations à Fournir pour le Débogage

Si le problème persiste, fournissez :

1. **Résultat de l'endpoint de test :**
   ```bash
   POST /auth/test-logout/1
   ```
   (Copier-coller la réponse JSON complète)

2. **Logs backend lors du logout :**
   (Copier-coller tous les logs qui commencent par "Logout:")

3. **Résultat de la requête SQL :**
   ```sql
   SELECT id, email, derniere_connexion, derniere_deconnexion 
   FROM utilisateur 
   WHERE id = 1;
   ```

4. **Capture d'écran de Network (F12) :**
   (Montrer la requête POST /auth/logout avec les headers)

Avec ces informations, on pourra identifier précisément où se trouve le problème.

