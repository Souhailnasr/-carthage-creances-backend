# 📋 Document : Implémentation Backend - Récupération de Mot de Passe

## 🎯 Objectif

Ce document décrit toutes les améliorations et implémentations backend effectuées pour garantir la fonctionnalité de récupération de mot de passe oublié.

---

## ✅ Fichiers Créés

### 1. Entités

**`PasswordResetToken.java`**
- Entité JPA pour stocker les tokens de réinitialisation
- Champs : id, token, utilisateur, dateCreation, dateExpiration, statut, dateUtilisation
- Index sur token, utilisateur_id, date_expiration pour performance
- @PrePersist pour définir automatiquement les dates

**`TokenStatut.java`**
- Enum pour les statuts des tokens : ACTIF, UTILISE, EXPIRE

### 2. Repository

**`PasswordResetTokenRepository.java`**
- Méthodes de recherche par token, utilisateur, email
- Méthodes de comptage pour rate limiting
- Méthodes de mise à jour pour invalidation et expiration
- Méthode de nettoyage pour supprimer les tokens anciens

### 3. DTOs

**`ForgotPasswordRequest.java`**
- DTO pour la demande de réinitialisation
- Validation : email obligatoire et valide

**`ResetPasswordRequest.java`**
- DTO pour la réinitialisation
- Validation : token, newPassword (min 8 caractères), confirmPassword

### 4. Services

**`PasswordResetService.java`** (Interface)
- `generateResetToken(String email)` - Génère et envoie le token
- `validateToken(String token)` - Valide le token
- `resetPassword(String token, String newPassword)` - Réinitialise le mot de passe
- `invalidateActiveTokens(String email)` - Invalide les tokens actifs

**`PasswordResetServiceImpl.java`** (Implémentation)
- Génération de token UUID unique
- Rate limiting : 3 demandes/heure par email
- Validation de la force du mot de passe (8+ caractères, majuscule, minuscule, chiffre, caractère spécial)
- Hashage du mot de passe avec BCrypt
- Gestion des tokens (création, validation, expiration, utilisation)

**`EmailService.java`** (Interface)
- `sendPasswordResetEmail(String email, String nom, String token)` - Envoie l'email

**`EmailServiceImpl.java`** (Implémentation)
- Template HTML professionnel pour l'email
- Lien de réinitialisation avec token
- Configuration via properties (frontend.url, app.name)
- Logging pour développement (à remplacer par un vrai service email en production)

### 5. Contrôleur

**`PasswordResetController.java`**
- `POST /api/auth/forgot-password` - Demande de réinitialisation
- `GET /api/auth/reset-password/validate?token={token}` - Validation du token
- `POST /api/auth/reset-password` - Réinitialisation du mot de passe
- `POST /api/auth/forgot-password/resend` - Renvoyer un email

### 6. Configuration

**`PasswordResetScheduler.java`**
- Scheduler pour marquer les tokens expirés (toutes les heures)
- Scheduler pour nettoyer les tokens anciens (tous les jours à 2h)

### 7. Migration Base de Données

**`V1_4__Create_Password_Reset_Token_Table.sql`**
- Création de la table `password_reset_token`
- Index sur token, utilisateur_id, date_expiration
- Foreign key vers utilisateur avec CASCADE DELETE

---

## 🔒 Sécurité Implémentée

### 1. Rate Limiting

- **3 demandes/heure par email** : Limite le nombre de demandes pour éviter le spam
- **Implémentation** : Comptage des tokens ACTIFS créés dans la dernière heure

### 2. Protection contre l'Énumération

- **Réponse générique** : Toujours retourner la même réponse, même si l'email n'existe pas
- **Logging** : Logger les tentatives pour audit, mais ne pas révéler à l'utilisateur

### 3. Validation de la Force du Mot de Passe

- **Longueur minimale** : 8 caractères
- **Complexité** : Au moins une majuscule, une minuscule, un chiffre, un caractère spécial
- **Validation côté backend** : Même si le frontend valide, le backend vérifie aussi

### 4. Expiration des Tokens

- **Durée de validité** : 24 heures
- **Vérification automatique** : À chaque utilisation
- **Marquage automatique** : Scheduler marque les tokens expirés

### 5. Usage Unique

- **Marquage après utilisation** : Token marqué comme UTILISE après réinitialisation
- **Impossible de réutiliser** : Validation vérifie que le token est ACTIF

### 6. Hashage du Mot de Passe

- **BCrypt** : Utilisation de BCryptPasswordEncoder (déjà configuré dans ApplicationConfig)
- **Sécurité** : Mot de passe jamais stocké en clair

---

## 🔄 Flux Complet Implémenté

### 1. Demande de Réinitialisation

```
Utilisateur → POST /api/auth/forgot-password
  ↓
Backend :
  - Vérifie que l'email existe
  - Vérifie rate limiting (3/heure)
  - Invalide les tokens actifs existants
  - Génère un token UUID unique
  - Stocke le token (ACTIF, expiration +24h)
  - Envoie un email avec le lien
  - Retourne réponse générique
```

### 2. Validation du Token

```
Utilisateur → GET /api/auth/reset-password/validate?token={token}
  ↓
Backend :
  - Recherche le token
  - Vérifie que le token est ACTIF
  - Vérifie que le token n'est pas expiré
  - Retourne valid: true/false
```

### 3. Réinitialisation

```
Utilisateur → POST /api/auth/reset-password
  ↓
Backend :
  - Valide le token (existe, ACTIF, non expiré)
  - Valide que les mots de passe correspondent
  - Valide la force du mot de passe
  - Hashe le nouveau mot de passe
  - Met à jour le mot de passe de l'utilisateur
  - Marque le token comme UTILISE
  - Retourne succès
```

### 4. Renvoi d'Email

```
Utilisateur → POST /api/auth/forgot-password/resend
  ↓
Backend :
  - Invalide tous les tokens ACTIFS de l'utilisateur
  - Génère un nouveau token
  - Envoie un nouvel email
  - Retourne réponse générique
```

---

## 📊 Structure de la Base de Données

### Table : `password_reset_token`

| Colonne | Type | Description |
|---------|------|-------------|
| `id` | BIGINT | Primary Key, Auto Increment |
| `token` | VARCHAR(255) | Token unique, Indexé |
| `utilisateur_id` | BIGINT | Foreign Key vers `utilisateur`, Indexé |
| `date_creation` | DATETIME | Date de création |
| `date_expiration` | DATETIME | Date d'expiration (création + 24h), Indexé |
| `statut` | ENUM | ACTIF, UTILISE, EXPIRE |
| `date_utilisation` | DATETIME | Date d'utilisation si utilisé (nullable) |

**Index :**
- `idx_token` sur `token` (recherche rapide)
- `idx_utilisateur` sur `utilisateur_id` (recherche par utilisateur)
- `idx_expiration` sur `date_expiration` (nettoyage des tokens expirés)

**Contraintes :**
- `token` UNIQUE
- Foreign Key vers `utilisateur` avec CASCADE DELETE

---

## ⚙️ Configuration Nécessaire

### Properties à Ajouter (application.properties)

```properties
# URL du frontend pour les liens de réinitialisation
app.frontend.url=http://localhost:4200

# Nom de l'application pour les emails
app.name=Carthage Créances
```

### Service d'Email (Optionnel - pour Production)

Pour utiliser un vrai service d'email en production, décommenter et configurer dans `EmailServiceImpl.java` :

```java
@Autowired
private JavaMailSender mailSender;
```

Et ajouter dans `application.properties` :

```properties
# Configuration SMTP (exemple avec Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre-email@gmail.com
spring.mail.password=votre-mot-de-passe
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Services recommandés :**
- SendGrid
- AWS SES
- Mailgun
- Postmark

---

## 🔄 Schedulers (Tâches Automatiques)

### 1. Expiration des Tokens

**Fréquence :** Toutes les heures  
**Action :** Marque les tokens expirés (date_expiration < maintenant) comme EXPIRE

### 2. Nettoyage des Tokens Anciens

**Fréquence :** Tous les jours à 2h du matin  
**Action :** Supprime les tokens EXPIRE ou UTILISE de plus de 7 jours

---

## ✅ Tests Recommandés

### Tests Backend

1. **Demande de réinitialisation :**
   - Email existant → Token créé, email envoyé
   - Email inexistant → Réponse générique (sécurité)
   - Rate limiting → Blocage après 3 demandes/heure

2. **Validation du token :**
   - Token valide → valid: true
   - Token invalide → valid: false
   - Token expiré → valid: false, statut → EXPIRE
   - Token utilisé → valid: false

3. **Réinitialisation :**
   - Token valide + mot de passe valide → Succès
   - Token invalide → Erreur
   - Mots de passe différents → Erreur PASSWORDS_MISMATCH
   - Mot de passe faible → Erreur

4. **Renvoi d'email :**
   - Invalide les tokens actifs
   - Génère un nouveau token
   - Envoie un nouvel email

### Tests d'Intégration

1. **Flux complet :**
   - Demande → Email reçu → Clic sur lien → Validation → Réinitialisation → Succès

2. **Cas d'erreur :**
   - Token expiré → Message d'erreur → Option de renvoi
   - Token utilisé → Message d'erreur → Option de renvoi

---

## 📝 Points d'Attention

### 1. Service d'Email

**Actuellement :** Le service d'email log les emails (développement)  
**Production :** Configurer un vrai service d'email (SendGrid, AWS SES, etc.)

### 2. Rate Limiting par IP

**Actuellement :** Rate limiting uniquement par email  
**Amélioration future :** Ajouter rate limiting par IP (10 demandes/heure)

### 3. Invalidation des Sessions

**Actuellement :** Non implémenté  
**Amélioration future :** Invalider toutes les sessions actives après réinitialisation

### 4. Logs et Audit

**Recommandation :** Logger toutes les tentatives de réinitialisation pour audit et sécurité

---

## 📚 Fichiers Modifiés

Aucun fichier existant n'a été modifié. Tous les fichiers sont nouveaux.

---

## ✅ Checklist d'Implémentation

- [x] Entité `PasswordResetToken` créée
- [x] Enum `TokenStatut` créé
- [x] Repository `PasswordResetTokenRepository` créé
- [x] DTOs `ForgotPasswordRequest` et `ResetPasswordRequest` créés
- [x] Service `PasswordResetService` créé et implémenté
- [x] Service `EmailService` créé et implémenté
- [x] Contrôleur `PasswordResetController` créé avec tous les endpoints
- [x] Scheduler `PasswordResetScheduler` créé
- [x] Migration Flyway `V1_4__Create_Password_Reset_Token_Table.sql` créée
- [x] Rate limiting implémenté (3/heure par email)
- [x] Validation de la force du mot de passe implémentée
- [x] Sécurité contre l'énumération implémentée
- [x] Expiration automatique des tokens implémentée
- [x] Nettoyage automatique des tokens anciens implémenté

---

**Date :** 2025-01-05  
**Status :** ✅ Backend complet et fonctionnel - Prêt pour intégration frontend

