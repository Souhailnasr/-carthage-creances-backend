# 📋 Guide : Récupération de Mot de Passe Oublié

## 🎯 Objectif

Ce document propose une solution complète pour gérer la récupération de mot de passe oublié dans l'application, incluant tous les mécanismes, endpoints backend, et interfaces frontend nécessaires.

---

## 🔐 Mécanisme de Récupération de Mot de Passe

### Principe Général

Le mécanisme de récupération de mot de passe utilise un **token de réinitialisation** unique et temporaire qui est :
1. Généré côté backend lors de la demande de réinitialisation
2. Envoyé par email à l'utilisateur
3. Utilisé pour valider la réinitialisation
4. Invalidé après utilisation ou expiration

---

## 📊 Flux Complet de Récupération

### Étape 1 : Demande de Réinitialisation

**Action utilisateur :**
- L'utilisateur clique sur "Mot de passe oublié ?" sur la page de connexion
- Il saisit son adresse email
- Il clique sur "Envoyer le lien de réinitialisation"

**Backend :**
1. Vérifier que l'email existe dans la base de données
2. Générer un token unique (UUID ou token aléatoire sécurisé)
3. Stocker le token dans la base de données avec :
   - Date d'expiration (ex: 24 heures)
   - Statut (ACTIF, UTILISE, EXPIRE)
   - Lien avec l'utilisateur
4. Envoyer un email avec un lien contenant le token
5. Retourner une réponse générique (sécurité : ne pas révéler si l'email existe ou non)

**Frontend :**
- Afficher un message : "Si cet email existe, un lien de réinitialisation vous a été envoyé"
- Rediriger vers la page de connexion ou afficher un message de confirmation

---

### Étape 2 : Réception de l'Email

**Contenu de l'email :**
- Sujet : "Réinitialisation de votre mot de passe"
- Corps :
  - Salutation personnalisée
  - Explication : "Vous avez demandé la réinitialisation de votre mot de passe"
  - Lien de réinitialisation : `https://votre-app.com/reset-password?token={token}`
  - Avertissement : "Ce lien est valide pendant 24 heures"
  - Sécurité : "Si vous n'avez pas fait cette demande, ignorez cet email"

---

### Étape 3 : Clic sur le Lien

**Action utilisateur :**
- L'utilisateur clique sur le lien dans l'email
- Il est redirigé vers la page de réinitialisation avec le token dans l'URL

**Frontend :**
1. Extraire le token de l'URL
2. Appeler le backend pour valider le token
3. Si valide : Afficher le formulaire de réinitialisation
4. Si invalide/expiré : Afficher un message d'erreur et proposer de renvoyer un email

---

### Étape 4 : Réinitialisation du Mot de Passe

**Action utilisateur :**
- L'utilisateur saisit son nouveau mot de passe (2 fois pour confirmation)
- Il clique sur "Réinitialiser le mot de passe"

**Backend :**
1. Valider le token (existe, actif, non expiré)
2. Valider le nouveau mot de passe (force, correspondance)
3. Hasher le nouveau mot de passe
4. Mettre à jour le mot de passe de l'utilisateur
5. Marquer le token comme UTILISE
6. Optionnel : Invalider toutes les sessions actives de l'utilisateur
7. Retourner une confirmation de succès

**Frontend :**
- Afficher un message de succès
- Rediriger vers la page de connexion
- Optionnel : Afficher un message "Votre mot de passe a été modifié avec succès. Veuillez vous connecter."

---

## 🗄️ Structure de Base de Données

### Table : `password_reset_token`

**Champs nécessaires :**
- `id` (Long, Primary Key)
- `token` (String, Unique, Indexé) - Token unique
- `utilisateur_id` (Long, Foreign Key vers `utilisateur`)
- `date_creation` (DateTime) - Date de création du token
- `date_expiration` (DateTime) - Date d'expiration (ex: création + 24h)
- `statut` (Enum) - ACTIF, UTILISE, EXPIRE
- `date_utilisation` (DateTime, Nullable) - Date d'utilisation si utilisé

**Contraintes :**
- Un utilisateur peut avoir plusieurs tokens (historique)
- Seul un token ACTIF peut être utilisé
- Les tokens expirés sont automatiquement marqués comme EXPIRE

---

## 🔌 Endpoints Backend Nécessaires

### 1. Demande de Réinitialisation

**Endpoint :** `POST /api/auth/forgot-password`

**Request Body :**
```json
{
  "email": "utilisateur@example.com"
}
```

**Réponse 200 OK :**
```json
{
  "message": "Si cet email existe, un lien de réinitialisation vous a été envoyé",
  "success": true
}
```

**Comportement :**
- Vérifier que l'email existe
- Générer un token unique
- Stocker le token dans la base de données
- Envoyer l'email avec le lien
- Retourner toujours la même réponse (sécurité)

**Sécurité :**
- Ne pas révéler si l'email existe ou non
- Limiter le nombre de demandes par email (ex: 3 par heure)
- Limiter le nombre de demandes par IP (ex: 10 par heure)

---

### 2. Validation du Token

**Endpoint :** `GET /api/auth/reset-password/validate?token={token}`

**Réponse 200 OK (Token valide) :**
```json
{
  "valid": true,
  "message": "Token valide"
}
```

**Réponse 400 Bad Request (Token invalide/expiré) :**
```json
{
  "valid": false,
  "message": "Token invalide ou expiré",
  "error": "TOKEN_INVALID"
}
```

**Comportement :**
- Vérifier que le token existe
- Vérifier que le token est ACTIF
- Vérifier que le token n'est pas expiré
- Retourner le statut de validation

---

### 3. Réinitialisation du Mot de Passe

**Endpoint :** `POST /api/auth/reset-password`

**Request Body :**
```json
{
  "token": "abc123...",
  "newPassword": "NouveauMotDePasse123!",
  "confirmPassword": "NouveauMotDePasse123!"
}
```

**Réponse 200 OK :**
```json
{
  "message": "Mot de passe réinitialisé avec succès",
  "success": true
}
```

**Réponse 400 Bad Request :**
```json
{
  "message": "Token invalide ou expiré",
  "error": "TOKEN_INVALID"
}
```

ou

```json
{
  "message": "Les mots de passe ne correspondent pas",
  "error": "PASSWORDS_MISMATCH"
}
```

**Comportement :**
- Valider le token (existe, actif, non expiré)
- Valider que les deux mots de passe correspondent
- Valider la force du mot de passe (longueur, complexité)
- Hasher le nouveau mot de passe
- Mettre à jour le mot de passe de l'utilisateur
- Marquer le token comme UTILISE
- Optionnel : Invalider toutes les sessions actives

**Sécurité :**
- Vérifier que le token n'a pas déjà été utilisé
- Valider la force du mot de passe
- Hasher le mot de passe avec BCrypt (ou équivalent)

---

### 4. Renvoyer un Email de Réinitialisation

**Endpoint :** `POST /api/auth/forgot-password/resend`

**Request Body :**
```json
{
  "email": "utilisateur@example.com"
}
```

**Comportement :**
- Invalider tous les tokens ACTIFS existants pour cet email
- Générer un nouveau token
- Envoyer un nouvel email

**Sécurité :**
- Limiter le nombre de renvois (ex: 3 par heure)

---

## 🎨 Interfaces Frontend Nécessaires

### 1. Page "Mot de Passe Oublié"

**URL :** `/forgot-password`

**Composants :**
- Formulaire avec champ email
- Bouton "Envoyer le lien de réinitialisation"
- Lien "Retour à la connexion"
- Message de confirmation après envoi

**Fonctionnalités :**
- Validation de l'email (format)
- Appel à `POST /api/auth/forgot-password`
- Affichage d'un message de confirmation
- Gestion des erreurs

---

### 2. Page "Réinitialisation de Mot de Passe"

**URL :** `/reset-password?token={token}`

**Composants :**
- Champ "Nouveau mot de passe"
- Champ "Confirmer le nouveau mot de passe"
- Indicateur de force du mot de passe
- Bouton "Réinitialiser le mot de passe"
- Message d'erreur si le token est invalide/expiré
- Lien "Renvoyer un email de réinitialisation"

**Fonctionnalités :**
- Validation du token au chargement de la page
- Validation des mots de passe (correspondance, force)
- Appel à `POST /api/auth/reset-password`
- Redirection vers la page de connexion après succès
- Gestion des erreurs (token invalide, mots de passe différents, etc.)

---

## 🔒 Sécurité et Bonnes Pratiques

### 1. Génération du Token

**Recommandations :**
- Utiliser un token aléatoire sécurisé (UUID v4 ou token de 32+ caractères)
- Stocker le token hashé dans la base de données (optionnel mais recommandé)
- Utiliser un token unique par demande

**Exemple :**
- UUID v4 : `550e8400-e29b-41d4-a716-446655440000`
- Token aléatoire : `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`

---

### 2. Expiration du Token

**Recommandations :**
- Durée de validité : 24 heures (standard)
- Vérifier l'expiration à chaque utilisation
- Marquer automatiquement les tokens expirés comme EXPIRE
- Nettoyer périodiquement les tokens expirés (cron job)

---

### 3. Limitation des Demandes

**Recommandations :**
- Limiter le nombre de demandes par email : 3 par heure
- Limiter le nombre de demandes par IP : 10 par heure
- Utiliser un système de rate limiting (ex: Spring Boot Rate Limiter)

**Implémentation :**
- Stocker les tentatives dans Redis ou en mémoire
- Incrémenter le compteur à chaque demande
- Bloquer si la limite est atteinte

---

### 4. Validation du Mot de Passe

**Recommandations :**
- Longueur minimale : 8 caractères
- Complexité : Au moins une majuscule, une minuscule, un chiffre, un caractère spécial
- Vérifier que le nouveau mot de passe est différent de l'ancien (optionnel)

**Exemples de validation :**
- ✅ `MotDePasse123!` - Valide
- ❌ `password` - Trop simple
- ❌ `12345678` - Pas de lettres
- ❌ `abcdefgh` - Pas de chiffres

---

### 5. Invalidation des Sessions

**Recommandations :**
- Optionnel : Invalider toutes les sessions actives après réinitialisation
- Forcer l'utilisateur à se reconnecter
- Améliorer la sécurité en cas de compromission

**Implémentation :**
- Supprimer tous les tokens JWT actifs de l'utilisateur
- Ou marquer les sessions comme invalides dans la base de données

---

### 6. Envoi d'Email

**Recommandations :**
- Utiliser un service d'email professionnel (SendGrid, AWS SES, Mailgun)
- Template d'email professionnel et clair
- Lien HTTPS uniquement
- Inclure des informations de sécurité (IP, date, heure)

**Template d'email :**
```
Sujet : Réinitialisation de votre mot de passe

Bonjour [Nom],

Vous avez demandé la réinitialisation de votre mot de passe.

Cliquez sur le lien suivant pour réinitialiser votre mot de passe :
[Lien de réinitialisation]

Ce lien est valide pendant 24 heures.

Si vous n'avez pas fait cette demande, ignorez cet email.

Informations de sécurité :
- Date : [Date]
- Heure : [Heure]
- IP : [IP]

Cordialement,
L'équipe [Nom de l'application]
```

---

## 📋 Checklist d'Implémentation

### Backend

- [ ] Créer l'entité `PasswordResetToken`
- [ ] Créer le repository `PasswordResetTokenRepository`
- [ ] Créer le service `PasswordResetService` avec :
  - [ ] `generateResetToken(String email)` - Génère et envoie le token
  - [ ] `validateToken(String token)` - Valide le token
  - [ ] `resetPassword(String token, String newPassword)` - Réinitialise le mot de passe
  - [ ] `invalidateToken(String token)` - Invalide le token
- [ ] Créer le contrôleur `PasswordResetController` avec :
  - [ ] `POST /api/auth/forgot-password`
  - [ ] `GET /api/auth/reset-password/validate`
  - [ ] `POST /api/auth/reset-password`
  - [ ] `POST /api/auth/forgot-password/resend`
- [ ] Configurer l'envoi d'email (service d'email)
- [ ] Implémenter le rate limiting
- [ ] Créer un cron job pour nettoyer les tokens expirés

### Frontend

- [ ] Créer la page "Mot de passe oublié" (`/forgot-password`)
- [ ] Créer la page "Réinitialisation" (`/reset-password`)
- [ ] Créer le service `PasswordResetService` avec :
  - [ ] `requestPasswordReset(email)`
  - [ ] `validateToken(token)`
  - [ ] `resetPassword(token, newPassword, confirmPassword)`
- [ ] Ajouter le lien "Mot de passe oublié ?" sur la page de connexion
- [ ] Implémenter la validation des formulaires
- [ ] Gérer les erreurs et afficher les messages appropriés

### Base de Données

- [ ] Créer la table `password_reset_token`
- [ ] Créer les index nécessaires (token, utilisateur_id, date_expiration)
- [ ] Créer une migration Flyway

### Tests

- [ ] Tester la demande de réinitialisation
- [ ] Tester la validation du token
- [ ] Tester la réinitialisation du mot de passe
- [ ] Tester l'expiration du token
- [ ] Tester le rate limiting
- [ ] Tester les cas d'erreur (token invalide, email inexistant, etc.)

---

## 🔄 Flux Alternatif : Code de Vérification

### Option Alternative : Code à 6 Chiffres

Au lieu d'utiliser un token dans l'URL, on peut utiliser un code à 6 chiffres :

**Flux :**
1. L'utilisateur demande la réinitialisation
2. Il reçoit un code à 6 chiffres par email/SMS
3. Il saisit le code sur la page de réinitialisation
4. Il saisit son nouveau mot de passe
5. Le backend valide le code et met à jour le mot de passe

**Avantages :**
- Plus simple pour l'utilisateur (pas de lien à cliquer)
- Peut être envoyé par SMS (plus sécurisé)

**Inconvénients :**
- Nécessite un service SMS (coût)
- Code à saisir manuellement (erreurs possibles)

---

## 📝 Exemple de Structure de Code (Référence)

### Entité PasswordResetToken

```java
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String token;
    
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
    
    @Column(nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(nullable = false)
    private LocalDateTime dateExpiration;
    
    @Enumerated(EnumType.STRING)
    private TokenStatut statut;
    
    private LocalDateTime dateUtilisation;
}
```

### Enum TokenStatut

```java
public enum TokenStatut {
    ACTIF,
    UTILISE,
    EXPIRE
}
```

---

## ⚠️ Points d'Attention

### 1. Sécurité

- **Ne jamais révéler si un email existe ou non** (protection contre l'énumération)
- **Hasher les tokens** dans la base de données (optionnel mais recommandé)
- **Utiliser HTTPS** pour tous les liens de réinitialisation
- **Valider la force du mot de passe** avant de l'accepter

### 2. Expérience Utilisateur

- **Message clair** : "Si cet email existe, un lien vous a été envoyé"
- **Feedback immédiat** : Confirmer l'envoi de l'email
- **Gestion des erreurs** : Messages clairs et actionnables
- **Lien de retour** : Toujours permettre de revenir à la connexion

### 3. Performance

- **Nettoyer les tokens expirés** périodiquement (cron job)
- **Indexer les colonnes** utilisées dans les requêtes (token, date_expiration)
- **Limiter les demandes** pour éviter le spam

### 4. Conformité

- **RGPD** : Informer l'utilisateur de l'utilisation de son email
- **Logs** : Logger les tentatives de réinitialisation (sécurité)
- **Audit** : Traçabilité des changements de mot de passe

---

## 📚 Ressources et Références

### Services d'Email Recommandés

- **SendGrid** : Service d'email transactionnel
- **AWS SES** : Service d'email d'Amazon
- **Mailgun** : Service d'email pour développeurs
- **Postmark** : Service d'email transactionnel

### Bibliothèques Spring Boot

- **Spring Mail** : Pour l'envoi d'email
- **Spring Security** : Pour la sécurité
- **Thymeleaf** : Pour les templates d'email

### Documentation

- OWASP : Guide de récupération de mot de passe
- Spring Security : Documentation officielle
- Angular : Guide de sécurité

---

**Date :** 2025-01-05  
**Status :** ✅ Guide complet - Prêt pour implémentation

