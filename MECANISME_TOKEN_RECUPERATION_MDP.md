# 🔐 Mécanisme de Token pour Récupération de Mot de Passe

## 🎯 Principe

Le mécanisme utilise un **token unique et temporaire** qui sert de "clé" pour réinitialiser le mot de passe sans connaître l'ancien mot de passe.

---

## 🔑 Qu'est-ce qu'un Token ?

Un **token** est une chaîne de caractères unique et aléatoire qui :
- ✅ Identifie de manière unique une demande de réinitialisation
- ✅ Est lié à un utilisateur spécifique
- ✅ A une durée de vie limitée (24 heures)
- ✅ Ne peut être utilisé qu'une seule fois

**Exemples de tokens :**
- UUID : `550e8400-e29b-41d4-a716-446655440000`
- Token aléatoire : `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`

---

## 📊 Cycle de Vie d'un Token

```
┌─────────────────────────────────────────────────────────────┐
│                    CYCLE DE VIE D'UN TOKEN                  │
└─────────────────────────────────────────────────────────────┘

1. CRÉATION
   ┌──────────────┐
   │ Demande de   │
   │ réinitialisation │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Génération   │
   │ du token     │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Stockage DB  │
   │ Statut: ACTIF│
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Envoi email  │
   │ avec lien    │
   └──────────────┘

2. VALIDATION
   ┌──────────────┐
   │ Clic sur     │
   │ le lien      │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Vérification │
   │ du token     │
   └──────┬───────┘
          │
          ├─── Token valide ────► Afficher formulaire
          │
          └─── Token invalide ───► Afficher erreur

3. UTILISATION
   ┌──────────────┐
   │ Saisie du    │
   │ nouveau MDP  │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Validation  │
   │ du token     │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Mise à jour  │
   │ du MDP       │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Marquer      │
   │ UTILISE      │
   └──────────────┘

4. EXPIRATION
   ┌──────────────┐
   │ Après 24h    │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Marquer      │
   │ EXPIRE       │
   └──────────────┘
```

---

## 🔄 Flux Détaillé avec Token

### Étape 1 : Demande de Réinitialisation

```
Utilisateur                    Frontend                    Backend                    Base de Données
    │                              │                           │                              │
    │─── Clic "MDP oublié" ───────►│                           │                              │
    │                              │                           │                              │
    │─── Saisit email ────────────►│                           │                              │
    │                              │                           │                              │
    │                              │─── POST /forgot-password ─►│                              │
    │                              │      {email: "..."}       │                              │
    │                              │                           │─── Vérifier email ───────────►│
    │                              │                           │◄── Email existe ───────────────│
    │                              │                           │                              │
    │                              │                           │─── Générer token ─────────────│
    │                              │                           │    Token: "abc123..."        │
    │                              │                           │                              │
    │                              │                           │─── Insérer token ────────────►│
    │                              │                           │    Statut: ACTIF             │
    │                              │                           │    Expiration: +24h           │
    │                              │                           │                              │
    │                              │                           │─── Envoyer email ────────────►│
    │                              │                           │    Lien: /reset?token=abc123 │
    │                              │                           │                              │
    │                              │◄── {success: true} ────────│                              │
    │◄── Message confirmation ────│                           │                              │
    │                              │                           │                              │
```

### Étape 2 : Clic sur le Lien

```
Utilisateur                    Frontend                    Backend                    Base de Données
    │                              │                           │                              │
    │─── Clic sur lien email ─────►│                           │                              │
    │    /reset?token=abc123       │                           │                              │
    │                              │                           │                              │
    │                              │─── GET /validate?token ───►│                              │
    │                              │      token: "abc123"       │                              │
    │                              │                           │─── Rechercher token ──────────►│
    │                              │                           │◄── Token trouvé ─────────────│
    │                              │                           │                              │
    │                              │                           │─── Vérifier statut ──────────│
    │                              │                           │    Statut: ACTIF ✓           │
    │                              │                           │                              │
    │                              │                           │─── Vérifier expiration ──────│
    │                              │                           │    Non expiré ✓              │
    │                              │                           │                              │
    │                              │◄── {valid: true} ──────────│                              │
    │◄── Afficher formulaire ──────│                           │                              │
    │                              │                           │                              │
```

### Étape 3 : Réinitialisation

```
Utilisateur                    Frontend                    Backend                    Base de Données
    │                              │                           │                              │
    │─── Saisit nouveau MDP ──────►│                           │                              │
    │                              │                           │                              │
    │                              │─── POST /reset-password ──►│                              │
    │                              │      {token, newPassword} │                              │
    │                              │                           │─── Rechercher token ──────────►│
    │                              │                           │◄── Token trouvé ─────────────│
    │                              │                           │                              │
    │                              │                           │─── Vérifier statut ──────────│
    │                              │                           │    Statut: ACTIF ✓           │
    │                              │                           │                              │
    │                              │                           │─── Vérifier expiration ──────│
    │                              │                           │    Non expiré ✓              │
    │                              │                           │                              │
    │                              │                           │─── Valider MDP ──────────────│
    │                              │                           │    Force, correspondance ✓   │
    │                              │                           │                              │
    │                              │                           │─── Hasher nouveau MDP ──────│
    │                              │                           │                              │
    │                              │                           │─── Mettre à jour MDP ────────►│
    │                              │                           │    Utilisateur               │
    │                              │                           │                              │
    │                              │                           │─── Marquer token UTILISE ────►│
    │                              │                           │    Statut: UTILISE            │
    │                              │                           │                              │
    │                              │◄── {success: true} ────────│                              │
    │◄── Redirection connexion ────│                           │                              │
    │                              │                           │                              │
```

---

## 🗄️ États d'un Token

```
┌─────────────────────────────────────────────────────────────┐
│                    ÉTATS D'UN TOKEN                         │
└─────────────────────────────────────────────────────────────┘

ACTIF
  │
  │   ┌─────────────────┐
  │   │ Token créé      │
  │   │ Statut: ACTIF   │
  │   │ Non expiré      │
  │   │ Non utilisé     │
  │   └─────────────────┘
  │
  │   Utilisable pour réinitialisation
  │
  ├─────────────────────────────────┐
  │                                 │
  ▼                                 ▼
UTILISE                          EXPIRE
  │                                 │
  │   ┌─────────────────┐   ┌─────────────────┐
  │   │ Token utilisé   │   │ Token expiré    │
  │   │ Statut: UTILISE │   │ Statut: EXPIRE  │
  │   │ Date utilisation│   │ Date > 24h      │
  │   └─────────────────┘   └─────────────────┘
  │                                 │
  │   Non réutilisable              │   Non utilisable
  │                                 │
```

---

## 🔒 Sécurité du Token

### Caractéristiques de Sécurité

1. **Unicité**
   - Chaque token est unique
   - Impossible de deviner un token valide
   - Probabilité de collision : négligeable

2. **Temporalité**
   - Durée de vie limitée (24 heures)
   - Expiration automatique
   - Nettoyage périodique

3. **Usage Unique**
   - Un token ne peut être utilisé qu'une fois
   - Après utilisation, le token est marqué comme UTILISE
   - Impossible de réutiliser un token utilisé

4. **Lien avec l'Utilisateur**
   - Chaque token est lié à un utilisateur spécifique
   - Impossible d'utiliser un token pour un autre utilisateur
   - Vérification de l'association token-utilisateur

### Protection contre les Attaques

1. **Attaque par Force Brute**
   - **Protection :** Token long et aléatoire (32+ caractères)
   - **Probabilité :** 1 sur 10^64 (négligeable)

2. **Attaque par Énumération**
   - **Protection :** Ne pas révéler si un email existe
   - **Réponse :** Toujours la même réponse générique

3. **Attaque par Replay**
   - **Protection :** Token à usage unique
   - **Comportement :** Après utilisation, le token est invalidé

4. **Attaque par Spam**
   - **Protection :** Rate limiting (3/heure par email, 10/heure par IP)
   - **Comportement :** Blocage après limite atteinte

---

## 📋 Exemple Concret

### Scénario : Utilisateur oublie son mot de passe

1. **Demande (10:00)**
   - Email : `jean.dupont@example.com`
   - Token généré : `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`
   - Statut : `ACTIF`
   - Expiration : `11/01/2025 10:00` (24h plus tard)

2. **Email envoyé (10:00)**
   - Lien : `https://app.com/reset-password?token=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`

3. **Clic sur le lien (14:30)**
   - Token validé : `ACTIF` ✓, Non expiré ✓
   - Formulaire affiché

4. **Réinitialisation (14:35)**
   - Nouveau mot de passe : `NouveauMDP123!`
   - Token utilisé : Statut → `UTILISE`
   - Mot de passe mis à jour

5. **Tentative de réutilisation (15:00)**
   - Token vérifié : Statut = `UTILISE` ✗
   - Erreur : "Token déjà utilisé"

6. **Expiration (11/01/2025 10:00)**
   - Token vérifié : Date > 24h ✗
   - Statut : `EXPIRE`
   - Erreur : "Token expiré"

---

## 🔄 Alternatives au Token dans l'URL

### Option 1 : Code à 6 Chiffres

**Flux :**
1. Demande → Code envoyé par email/SMS
2. Utilisateur saisit le code
3. Validation du code
4. Réinitialisation du mot de passe

**Avantages :**
- Plus simple pour l'utilisateur
- Peut être envoyé par SMS (plus sécurisé)

**Inconvénients :**
- Nécessite un service SMS (coût)
- Code à saisir manuellement (erreurs possibles)

### Option 2 : Token dans le Header

**Flux :**
1. Demande → Token envoyé par email
2. Utilisateur copie le token
3. Saisit le token dans un champ
4. Réinitialisation du mot de passe

**Avantages :**
- Token non visible dans l'URL
- Plus sécurisé (pas dans l'historique)

**Inconvénients :**
- Moins pratique (copier-coller)
- Risque d'erreur de saisie

---

## ✅ Avantages du Mécanisme de Token

1. **Sécurité**
   - Pas besoin de connaître l'ancien mot de passe
   - Token unique et temporaire
   - Usage unique

2. **Simplicité**
   - Un simple clic sur le lien
   - Pas de saisie manuelle
   - Expérience utilisateur fluide

3. **Traçabilité**
   - Historique des demandes
   - Audit des réinitialisations
   - Détection d'abus

4. **Flexibilité**
   - Durée de vie configurable
   - Possibilité de renvoyer un email
   - Gestion des erreurs claire

---

**Date :** 2025-01-05  
**Status :** ✅ Mécanisme expliqué - Prêt pour implémentation

