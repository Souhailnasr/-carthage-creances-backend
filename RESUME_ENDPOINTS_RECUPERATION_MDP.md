# 📋 Résumé : Endpoints et Opérations pour Récupération de Mot de Passe

## 🎯 Objectif

Ce document résume rapidement tous les endpoints backend et opérations frontend nécessaires pour implémenter la récupération de mot de passe oublié.

---

## 🔌 Endpoints Backend Requis

### 1. Demande de Réinitialisation

**Endpoint :** `POST /api/auth/forgot-password`

**Request :**
```json
{
  "email": "utilisateur@example.com"
}
```

**Réponse :**
```json
{
  "message": "Si cet email existe, un lien de réinitialisation vous a été envoyé",
  "success": true
}
```

**Opérations Backend :**
1. Vérifier que l'email existe
2. Générer un token unique (UUID ou token aléatoire)
3. Stocker le token dans la base de données avec expiration (24h)
4. Envoyer un email avec le lien de réinitialisation
5. Retourner une réponse générique (sécurité)

**Sécurité :**
- Rate limiting : 3 demandes/heure par email, 10 demandes/heure par IP
- Ne pas révéler si l'email existe ou non

---

### 2. Validation du Token

**Endpoint :** `GET /api/auth/reset-password/validate?token={token}`

**Réponse (Token valide) :**
```json
{
  "valid": true,
  "message": "Token valide"
}
```

**Réponse (Token invalide/expiré) :**
```json
{
  "valid": false,
  "message": "Token invalide ou expiré",
  "error": "TOKEN_INVALID"
}
```

**Opérations Backend :**
1. Rechercher le token dans la base de données
2. Vérifier que le token est ACTIF
3. Vérifier que le token n'est pas expiré
4. Retourner le statut de validation

---

### 3. Réinitialisation du Mot de Passe

**Endpoint :** `POST /api/auth/reset-password`

**Request :**
```json
{
  "token": "abc123...",
  "newPassword": "NouveauMotDePasse123!",
  "confirmPassword": "NouveauMotDePasse123!"
}
```

**Réponse (Succès) :**
```json
{
  "message": "Mot de passe réinitialisé avec succès",
  "success": true
}
```

**Réponse (Erreur) :**
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

**Opérations Backend :**
1. Valider le token (existe, actif, non expiré)
2. Valider que les deux mots de passe correspondent
3. Valider la force du mot de passe (longueur, complexité)
4. Hasher le nouveau mot de passe (BCrypt)
5. Mettre à jour le mot de passe de l'utilisateur
6. Marquer le token comme UTILISE
7. Optionnel : Invalider toutes les sessions actives

**Validation du Mot de Passe :**
- Longueur minimale : 8 caractères
- Au moins une majuscule, une minuscule, un chiffre, un caractère spécial

---

### 4. Renvoyer un Email (Optionnel)

**Endpoint :** `POST /api/auth/forgot-password/resend`

**Request :**
```json
{
  "email": "utilisateur@example.com"
}
```

**Opérations Backend :**
1. Invalider tous les tokens ACTIFS existants pour cet email
2. Générer un nouveau token
3. Envoyer un nouvel email
4. Rate limiting : 3 renvois/heure

---

## 🗄️ Structure de Base de Données

### Table : `password_reset_token`

| Colonne | Type | Description |
|---------|------|-------------|
| `id` | Long | Primary Key |
| `token` | String (Unique, Indexé) | Token unique |
| `utilisateur_id` | Long (FK) | Référence vers `utilisateur` |
| `date_creation` | DateTime | Date de création |
| `date_expiration` | DateTime | Date d'expiration (création + 24h) |
| `statut` | Enum | ACTIF, UTILISE, EXPIRE |
| `date_utilisation` | DateTime (Nullable) | Date d'utilisation si utilisé |

**Contraintes :**
- Un utilisateur peut avoir plusieurs tokens (historique)
- Seul un token ACTIF peut être utilisé
- Les tokens expirés sont automatiquement marqués comme EXPIRE

---

## 🎨 Interfaces Frontend Requises

### 1. Page "Mot de Passe Oublié"

**URL :** `/forgot-password`

**Composants :**
- Formulaire avec champ email
- Bouton "Envoyer le lien de réinitialisation"
- Lien "Retour à la connexion"
- Message de confirmation après envoi

**Service Angular :**
```typescript
requestPasswordReset(email: string): Observable<{message: string, success: boolean}>
```

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

**Service Angular :**
```typescript
validateToken(token: string): Observable<{valid: boolean, message: string}>
resetPassword(token: string, newPassword: string, confirmPassword: string): Observable<{message: string, success: boolean}>
```

**Fonctionnalités :**
- Validation du token au chargement de la page
- Validation des mots de passe (correspondance, force)
- Appel à `POST /api/auth/reset-password`
- Redirection vers la page de connexion après succès
- Gestion des erreurs

---

## 🔒 Sécurité et Bonnes Pratiques

### Génération du Token

- **Type :** UUID v4 ou token aléatoire de 32+ caractères
- **Stockage :** Optionnel : Hasher le token dans la base de données
- **Unicité :** Un token unique par demande

### Expiration

- **Durée :** 24 heures (standard)
- **Vérification :** À chaque utilisation
- **Nettoyage :** Cron job pour supprimer les tokens expirés

### Rate Limiting

- **Par email :** 3 demandes/heure
- **Par IP :** 10 demandes/heure
- **Implémentation :** Redis ou en mémoire

### Validation du Mot de Passe

- **Longueur minimale :** 8 caractères
- **Complexité :** Majuscule, minuscule, chiffre, caractère spécial
- **Exemple valide :** `MotDePasse123!`

### Envoi d'Email

- **Service :** SendGrid, AWS SES, Mailgun, Postmark
- **Template :** Professionnel avec lien HTTPS
- **Informations :** Date, heure, IP (sécurité)

---

## 📋 Checklist d'Implémentation

### Backend

- [ ] Créer l'entité `PasswordResetToken`
- [ ] Créer le repository `PasswordResetTokenRepository`
- [ ] Créer le service `PasswordResetService`
- [ ] Créer le contrôleur `PasswordResetController`
- [ ] Configurer l'envoi d'email
- [ ] Implémenter le rate limiting
- [ ] Créer un cron job pour nettoyer les tokens expirés
- [ ] Créer la migration Flyway

### Frontend

- [ ] Créer la page "Mot de passe oublié" (`/forgot-password`)
- [ ] Créer la page "Réinitialisation" (`/reset-password`)
- [ ] Créer le service `PasswordResetService`
- [ ] Ajouter le lien "Mot de passe oublié ?" sur la page de connexion
- [ ] Implémenter la validation des formulaires
- [ ] Gérer les erreurs et afficher les messages appropriés

### Tests

- [ ] Tester la demande de réinitialisation
- [ ] Tester la validation du token
- [ ] Tester la réinitialisation du mot de passe
- [ ] Tester l'expiration du token
- [ ] Tester le rate limiting
- [ ] Tester les cas d'erreur

---

## 🔄 Flux Complet (Résumé)

1. **Utilisateur** : Clique sur "Mot de passe oublié ?" → Saisit son email
2. **Frontend** : Appelle `POST /api/auth/forgot-password`
3. **Backend** : Génère un token → Stocke dans la DB → Envoie un email
4. **Utilisateur** : Reçoit l'email → Clique sur le lien
5. **Frontend** : Valide le token avec `GET /api/auth/reset-password/validate`
6. **Utilisateur** : Saisit son nouveau mot de passe (2 fois)
7. **Frontend** : Appelle `POST /api/auth/reset-password`
8. **Backend** : Valide le token → Met à jour le mot de passe → Marque le token comme UTILISE
9. **Frontend** : Redirige vers la page de connexion

---

## ⚠️ Points d'Attention

1. **Sécurité :**
   - Ne jamais révéler si un email existe ou non
   - Utiliser HTTPS pour tous les liens
   - Valider la force du mot de passe

2. **Expérience Utilisateur :**
   - Message clair et actionnable
   - Feedback immédiat
   - Gestion des erreurs appropriée

3. **Performance :**
   - Nettoyer les tokens expirés périodiquement
   - Indexer les colonnes utilisées dans les requêtes

4. **Conformité :**
   - RGPD : Informer l'utilisateur
   - Logs : Traçabilité des tentatives
   - Audit : Historique des changements

---

**Date :** 2025-01-05  
**Status :** ✅ Résumé complet - Référence rapide

