# 🔐 Correction du Problème de Cryptage des Mots de Passe

## 🐛 Problème Identifié

Lors de la mise à jour d'un utilisateur via `PUT /api/users/{id}`, le mot de passe était stocké **en clair** dans la base de données au lieu d'être crypté (hashé) avec BCrypt.

**Conséquence** : L'utilisateur ne pouvait pas se connecter car Spring Security compare le mot de passe fourni (qui sera hashé) avec le mot de passe stocké (qui était en clair).

---

## ✅ Solution Appliquée

### Correction dans `UtilisateurServiceImpl.updateUtilisateur()`

**Avant** (❌ Incorrect) :
```java
if (utilisateurDetails.getMotDePasse() != null && !utilisateurDetails.getMotDePasse().isEmpty()) {
    existingUtilisateur.setMotDePasse(utilisateurDetails.getMotDePasse()); // ❌ Mot de passe en clair
}
```

**Après** (✅ Correct) :
```java
if (utilisateurDetails.getMotDePasse() != null && !utilisateurDetails.getMotDePasse().isEmpty()) {
    String encodedPassword = passwordEncoder.encode(utilisateurDetails.getMotDePasse()); // ✅ Cryptage avec BCrypt
    existingUtilisateur.setMotDePasse(encodedPassword);
}
```

### Vérification des Autres Endroits

Tous les endroits où le mot de passe est modifié encodent maintenant correctement :

1. ✅ `createUtilisateur()` - Ligne 98-100 : Encode le mot de passe
2. ✅ `updateUtilisateur()` - Ligne 207-208 : **CORRIGÉ** - Encode maintenant le mot de passe
3. ✅ `reinitialiserMotDePasse()` - Ligne 578-579 : Encode le mot de passe

---

## 🔧 Correction des Mots de Passe Existants en Clair

Si vous avez des mots de passe stockés en clair dans la base de données, vous devez les réinitialiser.

### Option 1 : Réinitialiser Individuellement

Utilisez l'endpoint de réinitialisation pour chaque utilisateur concerné :

```bash
PUT /api/users/{userId}/reset-password
{
  "nouveauMotDePasse": "nouveauMotDePasse123"
}
```

### Option 2 : Script SQL pour Identifier les Mots de Passe en Clair

Les mots de passe BCrypt commencent toujours par `$2a$`, `$2b$`, ou `$2y$`. Vous pouvez identifier les mots de passe en clair avec cette requête :

```sql
-- Identifier les utilisateurs avec des mots de passe potentiellement en clair
SELECT id, email, nom, prenom, 
       CASE 
         WHEN mot_de_passe LIKE '$2%' THEN 'Crypté (BCrypt)'
         ELSE '⚠️ Potentiellement en clair'
       END as statut_cryptage
FROM utilisateur
ORDER BY statut_cryptage;
```

### Option 3 : Endpoint de Correction Automatique (Optionnel)

Si vous souhaitez créer un endpoint pour corriger automatiquement tous les mots de passe en clair, voici un exemple :

```java
@PutMapping("/fix-passwords")
public ResponseEntity<?> corrigerMotsDePasseEnClair() {
    // ⚠️ ATTENTION : Cet endpoint doit être sécurisé et accessible uniquement aux admins
    // Il réinitialise tous les mots de passe avec un mot de passe temporaire
    // Les utilisateurs devront ensuite réinitialiser leur mot de passe
    
    List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
    int count = 0;
    
    for (Utilisateur user : utilisateurs) {
        String motDePasse = user.getMotDePasse();
        // Vérifier si le mot de passe est déjà crypté (commence par $2)
        if (motDePasse == null || !motDePasse.startsWith("$2")) {
            // Réinitialiser avec un mot de passe temporaire
            String tempPassword = "TempPass123!"; // ⚠️ À changer selon votre politique
            utilisateurService.reinitialiserMotDePasse(user.getId(), tempPassword);
            count++;
        }
    }
    
    return ResponseEntity.ok(Map.of(
        "message", "Mots de passe corrigés",
        "nombreUtilisateursCorriges", count
    ));
}
```

**⚠️ Note** : Cette approche n'est pas recommandée car elle réinitialise tous les mots de passe, même ceux qui sont déjà cryptés mais qui ne commencent pas par `$2` (cas rare mais possible).

---

## 🧪 Test de Vérification

### Test 1 : Vérifier le Format BCrypt

Un mot de passe BCrypt a toujours ce format :
```
$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
│  │  │
│  │  └─ Hash (60 caractères)
│  └───── Coût (nombre de rounds)
└──────── Algorithme (2a, 2b, ou 2y)
```

**Exemple de mot de passe BCrypt valide** :
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

### Test 2 : Tester la Connexion

Après avoir corrigé le mot de passe, testez la connexion :

```bash
POST /auth/authenticate
{
  "email": "chef.dossier@example.com",
  "password": "nouveauMotDePasse123"
}
```

**Réponse attendue** : 200 OK avec un token JWT

---

## 📋 Checklist de Correction

- [x] ✅ Correction du code dans `updateUtilisateur()` pour encoder le mot de passe
- [ ] 🔄 Identifier les utilisateurs avec des mots de passe en clair dans la base de données
- [ ] 🔄 Réinitialiser les mots de passe des utilisateurs concernés
- [ ] ✅ Tester la connexion avec un utilisateur corrigé
- [ ] ✅ Vérifier que les nouveaux mots de passe sont bien cryptés dans la base

---

## 🔍 Comment Vérifier si un Mot de Passe est Crypté

### Méthode 1 : Via la Base de Données

```sql
-- Vérifier le format d'un mot de passe spécifique
SELECT id, email, 
       LEFT(mot_de_passe, 7) as prefixe,
       CASE 
         WHEN mot_de_passe LIKE '$2%' THEN '✅ Crypté (BCrypt)'
         ELSE '❌ Potentiellement en clair'
       END as statut
FROM utilisateur
WHERE email = 'chef.dossier@example.com';
```

### Méthode 2 : Via l'API

Créez un endpoint de vérification (à des fins de debug uniquement) :

```java
@GetMapping("/{id}/check-password-format")
public ResponseEntity<?> verifierFormatMotDePasse(@PathVariable Long id) {
    Optional<Utilisateur> user = utilisateurService.getUtilisateurById(id);
    if (user.isPresent()) {
        String motDePasse = user.get().getMotDePasse();
        boolean estCrypte = motDePasse != null && motDePasse.startsWith("$2");
        
        return ResponseEntity.ok(Map.of(
            "userId", id,
            "email", user.get().getEmail(),
            "estCrypte", estCrypte,
            "prefixe", motDePasse != null ? motDePasse.substring(0, Math.min(7, motDePasse.length())) : "NULL"
        ));
    }
    return ResponseEntity.notFound().build();
}
```

---

## ⚠️ Important : Sécurité

1. **Ne jamais stocker les mots de passe en clair** dans la base de données
2. **Toujours utiliser BCrypt** ou un autre algorithme de hashage sécurisé
3. **Ne jamais logger les mots de passe** (même cryptés) dans les logs de production
4. **Limiter l'accès** aux endpoints de réinitialisation de mot de passe

---

## 🎯 Résumé

✅ **Problème résolu** : Le mot de passe est maintenant correctement crypté lors de la mise à jour via `PUT /api/users/{id}`

✅ **Action requise** : Réinitialiser les mots de passe existants qui sont en clair dans la base de données

✅ **Prévention** : Tous les nouveaux mots de passe seront automatiquement cryptés

---

## 📞 Support

Si vous rencontrez encore des problèmes après cette correction :

1. Vérifiez que le `passwordEncoder` est bien injecté dans `UtilisateurServiceImpl`
2. Vérifiez que les mots de passe dans la base de données commencent par `$2`
3. Testez la connexion avec un utilisateur dont le mot de passe a été réinitialisé
4. Vérifiez les logs du serveur pour d'éventuelles erreurs

