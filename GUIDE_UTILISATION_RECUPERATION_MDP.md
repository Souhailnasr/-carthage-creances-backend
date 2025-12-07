# 📋 Guide d'Utilisation : Récupération de Mot de Passe

## 🎯 Objectif

Ce guide fournit toutes les informations nécessaires pour utiliser et intégrer la fonctionnalité de récupération de mot de passe oublié.

---

## 📚 Documents Disponibles

### Pour Comprendre le Mécanisme

1. **`GUIDE_RECUPERATION_MOT_DE_PASSE.md`** ⭐ **COMMENCER ICI**
   - Guide complet du mécanisme
   - Flux détaillé
   - Sécurité et bonnes pratiques

2. **`MECANISME_TOKEN_RECUPERATION_MDP.md`**
   - Explication visuelle du mécanisme de token
   - Diagrammes de flux
   - Cycle de vie d'un token

### Pour l'Implémentation Backend

3. **`DOCUMENT_IMPLÉMENTATION_BACKEND_RECUPERATION_MDP.md`**
   - Détails techniques de l'implémentation backend
   - Liste de tous les fichiers créés
   - Configuration nécessaire

4. **`RESUME_ENDPOINTS_RECUPERATION_MDP.md`**
   - Résumé rapide des endpoints
   - Structure de base de données
   - Checklist d'implémentation

### Pour l'Intégration Frontend

5. **`PROMPTS_FRONTEND_RECUPERATION_MDP.md`** ⭐ **POUR LE FRONTEND**
   - 7 prompts détaillés pour l'intégration frontend
   - Chaque prompt est complet et prêt à être utilisé

### Résumé

6. **`RESUME_COMPLET_RECUPERATION_MDP.md`**
   - Résumé complet backend + frontend
   - Checklist d'intégration
   - Points d'attention

---

## 🚀 Démarrage Rapide

### Backend

1. ✅ **Tous les fichiers sont créés**
2. ✅ **Migration Flyway créée** (`V1_4__Create_Password_Reset_Token_Table.sql`)
3. ⚠️ **Configurer les properties** dans `application.properties` :
   ```properties
   app.frontend.url=http://localhost:4200
   app.name=Carthage Créances
   ```
4. ⚠️ **Tester les endpoints** avec Postman ou un client REST
5. ⚠️ **Configurer le service d'email** (pour production)

### Frontend

1. **Lire `PROMPTS_FRONTEND_RECUPERATION_MDP.md`**
2. **Utiliser les prompts** dans l'ordre recommandé
3. **Tester chaque fonctionnalité** après implémentation

---

## 🔌 Endpoints Backend

### 1. Demande de Réinitialisation

```
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "utilisateur@example.com"
}

Response:
{
  "message": "Si cet email existe, un lien de réinitialisation vous a été envoyé",
  "success": true
}
```

### 2. Validation du Token

```
GET /api/auth/reset-password/validate?token={token}

Response (valide):
{
  "valid": true,
  "message": "Token valide"
}

Response (invalide):
{
  "valid": false,
  "message": "Token invalide ou expiré",
  "error": "TOKEN_INVALID"
}
```

### 3. Réinitialisation du Mot de Passe

```
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "abc123...",
  "newPassword": "NouveauMotDePasse123!",
  "confirmPassword": "NouveauMotDePasse123!"
}

Response (succès):
{
  "message": "Mot de passe réinitialisé avec succès",
  "success": true
}

Response (erreur):
{
  "message": "Les mots de passe ne correspondent pas",
  "error": "PASSWORDS_MISMATCH"
}
```

### 4. Renvoyer un Email

```
POST /api/auth/forgot-password/resend
Content-Type: application/json

{
  "email": "utilisateur@example.com"
}

Response:
{
  "message": "Si cet email existe, un nouveau lien de réinitialisation vous a été envoyé",
  "success": true
}
```

---

## 📋 Checklist d'Intégration

### Backend

- [x] Entité `PasswordResetToken` créée
- [x] Repository créé
- [x] Services créés
- [x] Contrôleur créé avec 4 endpoints
- [x] Migration Flyway créée
- [x] Scheduler créé
- [ ] Properties configurées (`app.frontend.url`, `app.name`)
- [ ] Service d'email configuré (pour production)
- [ ] Endpoints testés

### Frontend

- [ ] Service Angular créé
- [ ] Page "Mot de passe oublié" créée
- [ ] Page "Réinitialisation" créée
- [ ] Lien ajouté sur page de connexion
- [ ] Validation force mot de passe (optionnel)
- [ ] Gestion des erreurs
- [ ] Tests effectués

---

## ⚠️ Points d'Attention

### 1. Service d'Email

**Actuellement :** Le service log les emails (développement)  
**Production :** Configurer un vrai service d'email

**Options :**
- SendGrid
- AWS SES
- Mailgun
- Postmark
- JavaMailSender (SMTP)

### 2. Configuration

**Properties nécessaires :**
```properties
app.frontend.url=http://localhost:4200
app.name=Carthage Créances
```

### 3. Tests

**Recommandation :** Tester tous les cas :
- ✅ Succès complet
- ✅ Token invalide
- ✅ Token expiré
- ✅ Token utilisé
- ✅ Mots de passe différents
- ✅ Mot de passe faible
- ✅ Rate limiting

---

## 📝 Utilisation des Prompts Frontend

### Ordre Recommandé

1. **Prompt 1** : Service Angular (base)
2. **Prompt 2** : Page "Mot de Passe Oublié"
3. **Prompt 4** : Lien sur page de connexion
4. **Prompt 3** : Page "Réinitialisation"
5. **Prompt 5** : Validation force mot de passe (optionnel)
6. **Prompt 6** : Gestion des erreurs
7. **Prompt 7** : Vérification complète

### Méthode

1. Copier le prompt complet
2. Adapter selon votre structure
3. Implémenter étape par étape
4. Tester après chaque implémentation
5. Itérer si nécessaire

---

## 🔒 Sécurité

### Implémentée

- ✅ Rate limiting (3/heure par email)
- ✅ Protection contre l'énumération
- ✅ Validation force mot de passe
- ✅ Expiration automatique (24h)
- ✅ Usage unique
- ✅ Hashage BCrypt

### À Améliorer (Optionnel)

- ⚠️ Rate limiting par IP
- ⚠️ Invalidation des sessions après réinitialisation
- ⚠️ Logs et audit complets

---

**Date :** 2025-01-05  
**Status :** ✅ Backend complet - Prompts frontend prêts

