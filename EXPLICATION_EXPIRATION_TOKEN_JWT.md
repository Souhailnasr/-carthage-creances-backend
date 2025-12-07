# 🔐 Explication : Pourquoi le Token JWT Expire ?

## 🎯 Pourquoi les Tokens JWT Expirent ?

### 1. **Sécurité**

Les tokens JWT expirent pour des raisons de sécurité :

- **Réduction de la surface d'attaque** : Si un token est volé ou intercepté, il ne sera valide que pendant une durée limitée
- **Limitation des dommages** : Même si un attaquant obtient un token, il ne pourra l'utiliser que jusqu'à son expiration
- **Protection contre les tokens oubliés** : Si un utilisateur oublie de se déconnecter, le token expirera automatiquement
- **Conformité aux bonnes pratiques** : Les tokens avec expiration sont une recommandation de sécurité standard

---

## ⏱️ Durée d'Expiration Actuelle

### Configuration dans `application.properties`

```properties
# JWT Configuration
application.security.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
application.security.jwt.expiration=86400000  # 24 heures (en millisecondes)
jdj.secure.token.validity=3600                 # 1 heure (en secondes)
```

### ⚠️ **Problème Identifié**

Il y a **deux configurations différentes** :

1. **`application.security.jwt.expiration=86400000`** (24 heures en millisecondes)
   - **Non utilisée actuellement** dans le code

2. **`jdj.secure.token.validity=3600`** (1 heure en secondes)
   - **Utilisée actuellement** dans `JwtService.java`

### Code Utilisé

Dans `JwtService.java` (ligne 42-43) :
```java
@Value("${jdj.secure.token.validity}")
private int tokenValidityInSeconds;
```

Et dans la méthode `generateToken()` (ligne 77) :
```java
long validityMs = (long) getTokenValidityInSeconds() * 1000L;
return Jwts.builder()
    .setExpiration(new Date(nowMs + validityMs))
    ...
```

**Résultat :** Le token expire après **1 heure (3600 secondes)**.

---

## 📊 Calcul de l'Expiration

### Exemple Concret

Si vous vous connectez le **4 décembre 2025 à 21:32:06 UTC** :
- **Date d'émission** : `2025-12-04T21:32:06Z`
- **Durée de validité** : 3600 secondes = 1 heure
- **Date d'expiration** : `2025-12-04T22:32:06Z`

Si vous essayez d'utiliser le token le **5 décembre 2025 à 04:38:45 UTC** :
- **Différence** : ~7 heures après l'expiration
- **Résultat** : Token expiré → Erreur `ExpiredJwtException`

---

## 🔧 Comment Modifier la Durée d'Expiration

### Option 1 : Augmenter la Durée (Recommandé pour le Développement)

Modifiez `src/main/resources/application.properties` :

```properties
# Avant (1 heure)
jdj.secure.token.validity=3600

# Après (24 heures = 86400 secondes)
jdj.secure.token.validity=86400

# Ou (7 jours = 604800 secondes)
jdj.secure.token.validity=604800
```

### Option 2 : Utiliser la Configuration Existante

Vous pouvez aussi utiliser `application.security.jwt.expiration` en modifiant `JwtService.java` :

```java
// Au lieu de :
@Value("${jdj.secure.token.validity}")
private int tokenValidityInSeconds;

// Utiliser :
@Value("${application.security.jwt.expiration}")
private long tokenValidityInMilliseconds;

// Et dans generateToken() :
long validityMs = tokenValidityInMilliseconds; // Déjà en millisecondes
```

---

## 📋 Durées Recommandées

### Pour le Développement
- **24 heures (86400 secondes)** : Pratique pour tester sans se reconnecter constamment

### Pour la Production
- **1 heure (3600 secondes)** : Sécurisé, mais nécessite un rafraîchissement automatique
- **15 minutes (900 secondes)** : Très sécurisé, mais peut être gênant pour l'utilisateur
- **8 heures (28800 secondes)** : Équilibre entre sécurité et confort utilisateur

### Avec Refresh Token
- **Access Token : 15 minutes** : Court, sécurisé
- **Refresh Token : 7 jours** : Permet de renouveler l'access token sans se reconnecter

---

## 🔄 Solution : Refresh Token (Recommandé)

### Concept

Au lieu d'augmenter la durée du token, implémentez un système de **refresh token** :

1. **Access Token** : Durée courte (15 minutes - 1 heure)
   - Utilisé pour les requêtes API
   - Expire rapidement pour la sécurité

2. **Refresh Token** : Durée longue (7 jours - 30 jours)
   - Stocké dans un cookie HttpOnly (plus sécurisé)
   - Utilisé uniquement pour obtenir un nouveau access token
   - Peut être révoqué si compromis

### Avantages

- ✅ **Sécurité** : Access token expire rapidement
- ✅ **Confort** : Utilisateur ne se reconnecte pas souvent
- ✅ **Contrôle** : Possibilité de révoquer le refresh token
- ✅ **Meilleure pratique** : Standard de l'industrie

---

## 🛠️ Correction Immédiate

### Pour Augmenter la Durée à 24 Heures

1. **Modifiez `application.properties`** :
```properties
jdj.secure.token.validity=86400  # 24 heures en secondes
```

2. **Redémarrez l'application**

3. **Testez** : Connectez-vous et vérifiez que le token est valide pendant 24 heures

---

## 📊 Comparaison des Durées

| Durée | Secondes | Cas d'Usage |
|-------|----------|-------------|
| 15 minutes | 900 | Très sécurisé, nécessite refresh token |
| 1 heure | 3600 | **Actuel** - Équilibre sécurité/confort |
| 8 heures | 28800 | Confortable pour une journée de travail |
| 24 heures | 86400 | Pratique pour le développement |
| 7 jours | 604800 | Nécessite refresh token |

---

## ⚠️ Attention

### Ne Pas Désactiver l'Expiration

**❌ Ne faites jamais :**
```java
// ❌ MAUVAIS - Token qui n'expire jamais
.setExpiration(null)
```

**Pourquoi ?**
- Risque de sécurité majeur
- Token volé = accès permanent
- Impossible de révoquer le token

### Bonne Pratique

Toujours définir une expiration, même longue :
```java
// ✅ BON - Token avec expiration
.setExpiration(new Date(nowMs + validityMs))
```

---

## 🔍 Vérifier l'Expiration d'un Token

### Dans le Frontend (JavaScript)

```javascript
// Décoder le token JWT (sans vérifier la signature)
const token = localStorage.getItem('token');
const payload = JSON.parse(atob(token.split('.')[1]));
const expirationDate = new Date(payload.exp * 1000);
const now = new Date();

if (now > expirationDate) {
    console.log('Token expiré !');
    // Rediriger vers la page de connexion
}
```

### Dans le Backend (Java)

Le backend vérifie automatiquement l'expiration lors de chaque requête via `JwtService.isTokenExpired()`.

---

## 📝 Résumé

### Pourquoi le Token Expire ?

1. **Sécurité** : Limite les risques en cas de vol
2. **Bonnes pratiques** : Standard de l'industrie
3. **Contrôle** : Permet de révoquer l'accès automatiquement

### Durée Actuelle

- **1 heure (3600 secondes)** configurée dans `jdj.secure.token.validity`

### Comment Modifier

1. Modifiez `application.properties` :
   ```properties
   jdj.secure.token.validity=86400  # 24 heures
   ```

2. Redémarrez l'application

### Recommandation

- **Développement** : 24 heures (86400 secondes)
- **Production** : 1 heure avec refresh token

---

**Date de création :** 2025-12-05
**Version :** 1.0




