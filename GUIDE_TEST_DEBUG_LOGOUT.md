# 🔧 Guide de Test et Débogage : derniere_deconnexion

Ce guide vous aide à identifier pourquoi `derniere_deconnexion` reste NULL malgré toutes les corrections.

---

## 📋 Étapes de Débogage

### Étape 1 : Vérifier les Logs Backend

**Après avoir cliqué sur "Déconnexion" dans le frontend, vérifiez les logs backend.**

**✅ Logs Attendus (si tout fonctionne) :**
```
=== DÉBUT LOGOUT ===
Logout: Token JWT extrait (longueur: XXX)
Logout: Token trouvé, ID: XXX
Logout: Utilisateur trouvé - ID: XXX, Email: XXX
Logout: Avant mise à jour - derniere_deconnexion actuelle: null
Logout: Après setDerniereDeconnexion - valeur: 2025-11-25T18:30:00
Logout: Utilisateur sauvegardé - ID: XXX, derniere_deconnexion: 2025-11-25T18:30:00
Logout: Vérification DB - derniere_deconnexion dans DB: 2025-11-25T18:30:00
Logout: derniere_deconnexion mise à jour pour l'utilisateur XXX: 2025-11-25T18:30:00
Logout: Token révoqué
=== FIN LOGOUT ===
```

**❌ Si vous NE voyez PAS ces logs :**
- Le frontend n'appelle pas `/auth/logout`
- Ou l'endpoint n'est pas atteint
- **Action :** Vérifier dans la console du navigateur (F12 → Network) si une requête POST vers `/auth/logout` apparaît

**❌ Si vous voyez :**
```
Logout: Pas de header Authorization ou format invalide
```
→ Le frontend n'envoie pas le header `Authorization`

**❌ Si vous voyez :**
```
Logout: Token non trouvé dans la base de données
```
→ Le token envoyé n'existe pas dans la base

**❌ Si vous voyez :**
```
Logout: Utilisateur non trouvé pour le token
```
→ Problème de relation Token-Utilisateur (déjà corrigé avec JOIN FETCH)

**❌ Si vous voyez :**
```
Logout: Vérification DB - derniere_deconnexion dans DB: null
```
→ La sauvegarde ne fonctionne pas (problème de transaction ou de mapping)

---

### Étape 2 : Tester avec l'Endpoint de Test

**J'ai créé un endpoint de test pour forcer la mise à jour :**

```bash
POST http://localhost:8089/carthage-creance/auth/test-logout/{userId}
```

**Exemple avec curl :**
```bash
curl -X POST "http://localhost:8089/carthage-creance/auth/test-logout/1" \
  -H "Content-Type: application/json"
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
→ La sauvegarde ne fonctionne pas (problème de transaction ou de mapping JPA)

**Si `apres_verification` n'est PAS NULL :**
→ La sauvegarde fonctionne, le problème vient de l'appel logout normal

---

### Étape 3 : Vérifier dans la Console du Navigateur

**1. Ouvrir la console (F12)**
**2. Aller dans l'onglet "Network"**
**3. Filtrer par "logout"**
**4. Cliquer sur "Déconnexion"**
**5. Vérifier :**

- ✅ Une requête `POST /auth/logout` apparaît
- ✅ Status: `200 OK`
- ✅ Request Headers contient `Authorization: Bearer ...`
- ✅ Response: `{"message":"Logout successful"}`

**Si la requête n'apparaît PAS :**
→ Le frontend n'appelle pas l'endpoint (problème dans le code frontend)

**Si la requête apparaît mais Status est `401` ou `404` :**
→ Problème d'authentification ou de routing

---

### Étape 4 : Vérifier le Nom de la Colonne dans la Base

**Dans phpMyAdmin, vérifiez le nom exact de la colonne :**

```sql
DESCRIBE utilisateur;
```

**Vérifiez que la colonne s'appelle bien :**
- `derniere_deconnexion` (avec le "n" à la fin)

**Si la colonne s'appelle différemment (ex: `derniere_deconnexio` sans le "n") :**
→ Il faut corriger le nom de la colonne dans la base de données

**Correction SQL :**
```sql
ALTER TABLE utilisateur 
CHANGE COLUMN derniere_deconnexio derniere_deconnexion DATETIME NULL;
```

---

### Étape 5 : Tester Directement avec Postman

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

**4. Vérifier les logs backend**

**5. Vérifier dans la base de données :**
```sql
SELECT id, email, derniere_connexion, derniere_deconnexion 
FROM utilisateur 
WHERE email = 'votre_email@example.com';
```

---

### Étape 6 : Vérifier la Configuration Spring Security

**Vérifiez que le LogoutService est bien intégré dans `SecurityConfiguration.java` :**

```java
.logout(logout -> logout
    .logoutUrl("/auth/logout")
    .addLogoutHandler(logoutService)  // ← Doit être présent
    .logoutSuccessHandler(...)
)
```

**Si `addLogoutHandler(logoutService)` n'est pas présent :**
→ Le LogoutService n'est jamais appelé

---

## 🔍 Diagnostic selon les Résultats

### Scénario 1 : Aucun log "=== DÉBUT LOGOUT ==="

**Problème :** Le LogoutService n'est jamais appelé

**Causes possibles :**
1. Le frontend n'appelle pas `/auth/logout`
2. L'endpoint n'est pas configuré correctement
3. Le LogoutService n'est pas intégré dans Spring Security

**Solutions :**
1. Vérifier dans Network (F12) si la requête apparaît
2. Vérifier que `addLogoutHandler(logoutService)` est présent dans SecurityConfiguration
3. Vérifier que l'endpoint `/auth/logout` existe dans AuthenticationController

---

### Scénario 2 : Logs "=== DÉBUT LOGOUT ===" mais "Vérification DB - derniere_deconnexion dans DB: null"

**Problème :** La sauvegarde ne fonctionne pas

**Causes possibles :**
1. Problème de transaction (rollback silencieux)
2. Problème de mapping JPA (nom de colonne incorrect)
3. Problème de flush (la transaction n'est pas commitée)

**Solutions :**
1. Vérifier le nom de la colonne dans la base : `DESCRIBE utilisateur;`
2. Vérifier que `@Column(name = "derniere_deconnexion")` correspond au nom dans la base
3. Ajouter `@Transactional(propagation = Propagation.REQUIRES_NEW)` pour isoler la transaction
4. Tester avec l'endpoint `/auth/test-logout/{userId}` pour voir si la sauvegarde fonctionne

---

### Scénario 3 : Logs montrent que la sauvegarde fonctionne mais la DB reste NULL

**Problème :** La transaction est annulée après le logout

**Causes possibles :**
1. Une exception est levée après la sauvegarde
2. La transaction est marquée rollback-only
3. Un autre service annule la transaction

**Solutions :**
1. Vérifier qu'il n'y a pas d'exception après la sauvegarde dans les logs
2. Utiliser `@Transactional(propagation = Propagation.REQUIRES_NEW)` pour isoler
3. Vérifier qu'aucun autre service n'annule la transaction

---

### Scénario 4 : Le frontend n'appelle pas `/auth/logout`

**Problème :** Le code frontend ne fait pas l'appel HTTP

**Solutions :**
1. Vérifier le service d'authentification frontend
2. Vérifier que la méthode `logout()` fait bien `POST /auth/logout`
3. Vérifier que le header `Authorization` est inclus
4. Utiliser le code de référence fourni dans `PROMPT_DEBUG_LOGOUT_JWT_FRONTEND.md`

---

## 🧪 Test Complet : Procédure Pas à Pas

### 1. Test avec l'Endpoint de Test

```bash
# Remplacer 1 par l'ID d'un utilisateur réel
curl -X POST "http://localhost:8089/carthage-creance/auth/test-logout/1"
```

**Vérifier la réponse :**
- Si `apres_verification` n'est PAS NULL → La sauvegarde fonctionne
- Si `apres_verification` est NULL → Problème de sauvegarde (voir Scénario 2)

### 2. Vérifier dans la Base de Données

```sql
SELECT id, email, derniere_connexion, derniere_deconnexion 
FROM utilisateur 
WHERE id = 1;
```

**Si `derniere_deconnexion` est remplie après le test :**
→ La sauvegarde fonctionne, le problème vient de l'appel logout normal

**Si `derniere_deconnexion` reste NULL :**
→ Problème de sauvegarde (voir Scénario 2)

### 3. Test avec Postman

1. Se connecter pour obtenir un token
2. Appeler `/auth/logout` avec le token
3. Vérifier les logs backend
4. Vérifier dans la base de données

### 4. Vérifier le Frontend

1. Ouvrir F12 → Network
2. Cliquer sur "Déconnexion"
3. Vérifier qu'une requête `POST /auth/logout` apparaît
4. Vérifier que le header `Authorization` est présent

---

## 📝 Checklist Finale

- [ ] Les logs backend montrent "=== DÉBUT LOGOUT ==="
- [ ] Les logs montrent "Logout: Utilisateur trouvé"
- [ ] Les logs montrent "Logout: derniere_deconnexion mise à jour"
- [ ] Les logs montrent "Logout: Vérification DB - derniere_deconnexion dans DB: [DATE]"
- [ ] Dans Network (F12), la requête POST /auth/logout apparaît
- [ ] Le header Authorization est présent dans la requête
- [ ] L'endpoint de test `/auth/test-logout/{userId}` fonctionne
- [ ] La colonne dans la base s'appelle bien `derniere_deconnexion`
- [ ] La base de données montre `derniere_deconnexion` remplie après logout

**Si toutes les cases sont cochées sauf la dernière :**
→ Le problème vient de la sauvegarde (voir Scénario 2)

**Si certaines cases ne sont pas cochées :**
→ Suivre les solutions du scénario correspondant

---

## 🚨 Solution d'Urgence : Forcer la Mise à Jour

Si rien ne fonctionne, vous pouvez créer un endpoint qui force la mise à jour sans passer par le logout :

```java
@PostMapping("/force-logout/{userId}")
public ResponseEntity<?> forceLogout(@PathVariable Long userId) {
    Utilisateur user = utilisateurRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    
    user.setDerniereDeconnexion(LocalDateTime.now());
    utilisateurRepository.saveAndFlush(user); // Force le flush
    
    return ResponseEntity.ok(Map.of(
        "message", "Logout forcé",
        "userId", userId,
        "derniere_deconnexion", user.getDerniereDeconnexion().toString()
    ));
}
```

Utilisez ce guide pour identifier précisément où se trouve le problème.

