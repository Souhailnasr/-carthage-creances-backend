# 📋 Prompts Frontend : Intégration de la Récupération de Mot de Passe

## 🎯 Objectif

Ce document contient tous les prompts nécessaires pour intégrer la fonctionnalité de récupération de mot de passe oublié dans le frontend Angular, avec les bonnes interfaces et la consommation correcte des APIs.

---

## 📋 Prompt 1 : Service Angular pour la Récupération de Mot de Passe

### Prompt

```
Je dois créer un service Angular pour gérer la récupération de mot de passe oublié dans mon application Angular de gestion de recouvrement de créances.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et RxJS
- Service HTTP avec HttpClient

**Backend disponible :**
- Endpoint : `POST /api/auth/forgot-password` - Demande de réinitialisation
  - Request : `{ "email": "utilisateur@example.com" }`
  - Response : `{ "message": "Si cet email existe...", "success": true }`
- Endpoint : `GET /api/auth/reset-password/validate?token={token}` - Validation du token
  - Response (valide) : `{ "valid": true, "message": "Token valide" }`
  - Response (invalide) : `{ "valid": false, "message": "Token invalide ou expiré", "error": "TOKEN_INVALID" }`
- Endpoint : `POST /api/auth/reset-password` - Réinitialisation du mot de passe
  - Request : `{ "token": "...", "newPassword": "...", "confirmPassword": "..." }`
  - Response (succès) : `{ "message": "Mot de passe réinitialisé avec succès", "success": true }`
  - Response (erreur) : `{ "message": "...", "error": "..." }`
- Endpoint : `POST /api/auth/forgot-password/resend` - Renvoyer un email
  - Request : `{ "email": "utilisateur@example.com" }`
  - Response : `{ "message": "...", "success": true }`

**Tâches :**
1. Créer le service `password-reset.service.ts` avec les méthodes suivantes :
   - `requestPasswordReset(email: string): Observable<{message: string, success: boolean}>`
   - `validateToken(token: string): Observable<{valid: boolean, message: string, error?: string}>`
   - `resetPassword(token: string, newPassword: string, confirmPassword: string): Observable<{message: string, success: boolean, error?: string}>`
   - `resendResetEmail(email: string): Observable<{message: string, success: boolean}>`
2. Utiliser HttpClient pour les appels HTTP
3. Gérer les erreurs avec catchError et retourner des observables avec des valeurs par défaut
4. Utiliser l'URL de base de l'API : `http://localhost:8089/carthage-creance/api`
5. Ajouter les headers nécessaires (Content-Type: application/json)

**Structure attendue :**
- Service : `password-reset.service.ts`
- Interface TypeScript : Définir les types pour les requêtes et réponses
- Injection : Injectable avec `providedIn: 'root'`

**Exigences :**
- Utiliser RxJS (Observable, catchError, throwError)
- Gérer les erreurs HTTP (400, 500, etc.)
- Retourner des messages d'erreur clairs
- Utiliser des observables (pas de promesses)
```

---

## 📋 Prompt 2 : Page "Mot de Passe Oublié"

### Prompt

```
Je dois créer une page "Mot de passe oublié" dans mon application Angular de gestion de recouvrement de créances.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Page accessible depuis la page de connexion

**Backend disponible :**
- Endpoint : `POST /api/auth/forgot-password`
  - Request : `{ "email": "utilisateur@example.com" }`
  - Response : `{ "message": "Si cet email existe, un lien de réinitialisation vous a été envoyé", "success": true }`

**Tâches :**
1. Créer le composant `forgot-password.component.ts` et `forgot-password.component.html`
2. Créer la route `/forgot-password` dans le module de routing
3. Afficher un formulaire avec :
   - Champ email (mat-form-field avec validation)
   - Bouton "Envoyer le lien de réinitialisation" (mat-raised-button)
   - Lien "Retour à la connexion" (mat-button ou lien)
4. Valider le format de l'email (pattern Angular)
5. Appeler le service `requestPasswordReset()` lors de la soumission
6. Afficher un message de confirmation après l'envoi (snackbar Material)
7. Gérer les erreurs et afficher des messages appropriés
8. Afficher un spinner pendant l'envoi

**Structure attendue :**
- Composant : `forgot-password.component.ts`
- Template : `forgot-password.component.html`
- Styles : `forgot-password.component.scss`
- Route : `/forgot-password`

**Exigences :**
- Utiliser Angular Material pour tous les composants UI
- Utiliser Reactive Forms pour le formulaire
- Validation en temps réel de l'email
- Message de confirmation clair : "Si cet email existe, un lien de réinitialisation vous a été envoyé"
- Design cohérent avec la page de connexion
- Responsive design (mobile/tablette/desktop)
- Utiliser RxJS pour la gestion asynchrone
```

---

## 📋 Prompt 3 : Page "Réinitialisation de Mot de Passe"

### Prompt

```
Je dois créer une page "Réinitialisation de mot de passe" dans mon application Angular de gestion de recouvrement de créances.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Page accessible via un lien dans l'email avec un token dans l'URL

**Backend disponible :**
- Endpoint : `GET /api/auth/reset-password/validate?token={token}` - Validation du token
  - Response (valide) : `{ "valid": true, "message": "Token valide" }`
  - Response (invalide) : `{ "valid": false, "message": "Token invalide ou expiré", "error": "TOKEN_INVALID" }`
- Endpoint : `POST /api/auth/reset-password` - Réinitialisation
  - Request : `{ "token": "...", "newPassword": "...", "confirmPassword": "..." }`
  - Response (succès) : `{ "message": "Mot de passe réinitialisé avec succès", "success": true }`
  - Response (erreur) : `{ "message": "...", "error": "PASSWORDS_MISMATCH" | "TOKEN_INVALID" | "RESET_FAILED" }`

**Tâches :**
1. Créer le composant `reset-password.component.ts` et `reset-password.component.html`
2. Créer la route `/reset-password` avec paramètre optionnel `token` dans l'URL
3. Au chargement de la page :
   - Extraire le token de l'URL (query parameter)
   - Appeler `validateToken()` pour valider le token
   - Si invalide : Afficher un message d'erreur et un lien pour renvoyer un email
   - Si valide : Afficher le formulaire de réinitialisation
4. Formulaire de réinitialisation avec :
   - Champ "Nouveau mot de passe" (mat-form-field avec type="password")
   - Champ "Confirmer le nouveau mot de passe" (mat-form-field avec type="password")
   - Indicateur de force du mot de passe (barre de progression ou texte)
   - Bouton "Réinitialiser le mot de passe" (mat-raised-button)
5. Validation du formulaire :
   - Longueur minimale : 8 caractères
   - Au moins une majuscule, une minuscule, un chiffre, un caractère spécial
   - Les deux mots de passe doivent correspondre
6. Appeler `resetPassword()` lors de la soumission
7. Afficher un message de succès et rediriger vers la page de connexion
8. Gérer les erreurs (token invalide, mots de passe différents, etc.)

**Structure attendue :**
- Composant : `reset-password.component.ts`
- Template : `reset-password.component.html`
- Styles : `reset-password.component.scss`
- Route : `/reset-password?token={token}`

**Exigences :**
- Utiliser Angular Material pour tous les composants UI
- Utiliser Reactive Forms pour le formulaire
- Validation en temps réel de la force du mot de passe
- Afficher les critères de validation (liste avec checkmarks)
- Indicateur visuel de la force du mot de passe
- Gérer les cas d'erreur avec des messages clairs
- Afficher un spinner pendant la validation et la réinitialisation
- Design cohérent avec le reste de l'application
- Responsive design
- Utiliser RxJS pour la gestion asynchrone
```

---

## 📋 Prompt 4 : Ajout du Lien "Mot de Passe Oublié ?" sur la Page de Connexion

### Prompt

```
Je dois ajouter un lien "Mot de passe oublié ?" sur la page de connexion de mon application Angular.

**Contexte :**
- Frontend Angular avec TypeScript et Angular Material
- Page de connexion existante avec formulaire de connexion

**Tâches :**
1. Modifier le composant de connexion existant
2. Ajouter un lien "Mot de passe oublié ?" sous le champ mot de passe
3. Le lien doit rediriger vers `/forgot-password`
4. Utiliser un style discret mais visible (lien Material ou bouton texte)
5. Positionner le lien de manière logique (sous le champ mot de passe, avant le bouton de connexion)

**Structure attendue :**
- Modifier : `login.component.html` (ou composant de connexion existant)
- Ajouter : Lien avec routerLink vers `/forgot-password`

**Exigences :**
- Utiliser Angular Material pour le style
- Lien cliquable et visible
- Design cohérent avec la page de connexion
- Responsive design
```

---

## 📋 Prompt 5 : Validation de la Force du Mot de Passe (Composant Réutilisable)

### Prompt

```
Je dois créer un composant réutilisable pour valider et afficher la force du mot de passe dans mon application Angular.

**Contexte :**
- Frontend Angular avec TypeScript et Angular Material
- Utilisé dans la page de réinitialisation de mot de passe

**Règles de validation :**
- Longueur minimale : 8 caractères
- Au moins une majuscule
- Au moins une minuscule
- Au moins un chiffre
- Au moins un caractère spécial

**Tâches :**
1. Créer le composant `password-strength.component.ts` et `password-strength.component.html`
2. Le composant doit accepter le mot de passe en input (`@Input()`)
3. Afficher une liste de critères avec des checkmarks (✓ ou ✗) :
   - "Au moins 8 caractères"
   - "Au moins une majuscule"
   - "Au moins une minuscule"
   - "Au moins un chiffre"
   - "Au moins un caractère spécial"
4. Afficher une barre de progression ou un indicateur de force (faible, moyen, fort)
5. Mettre à jour en temps réel lorsque le mot de passe change

**Structure attendue :**
- Composant : `password-strength.component.ts`
- Template : `password-strength.component.html`
- Styles : `password-strength.component.scss`

**Exigences :**
- Utiliser Angular Material pour les icônes (check, close)
- Utiliser des couleurs pour indiquer la force (rouge = faible, orange = moyen, vert = fort)
- Mise à jour en temps réel (OnChanges)
- Design clair et lisible
- Réutilisable dans d'autres formulaires (inscription, changement de mot de passe, etc.)
```

---

## 📋 Prompt 6 : Gestion des Erreurs et Messages Utilisateur

### Prompt

```
Je dois améliorer la gestion des erreurs et des messages utilisateur pour la récupération de mot de passe dans mon application Angular.

**Contexte :**
- Frontend Angular avec TypeScript et Angular Material
- Service de récupération de mot de passe existant
- Pages de réinitialisation et de demande de réinitialisation

**Cas d'erreur à gérer :**
1. **Token invalide ou expiré :**
   - Message : "Le lien de réinitialisation est invalide ou a expiré"
   - Action : Afficher un bouton "Renvoyer un email de réinitialisation"
2. **Mots de passe ne correspondent pas :**
   - Message : "Les mots de passe ne correspondent pas"
   - Action : Surligner les champs en erreur
3. **Mot de passe trop faible :**
   - Message : "Le mot de passe ne respecte pas les critères de sécurité"
   - Action : Afficher les critères non respectés
4. **Erreur réseau :**
   - Message : "Erreur de connexion. Veuillez réessayer plus tard"
   - Action : Permettre de réessayer
5. **Rate limiting (trop de demandes) :**
   - Message : "Trop de demandes. Veuillez réessayer dans quelques minutes"
   - Action : Désactiver le bouton temporairement

**Tâches :**
1. Créer un service de gestion des erreurs `error-handler.service.ts` (optionnel, ou utiliser un service existant)
2. Créer des messages d'erreur clairs et actionnables
3. Utiliser des snackbars Material pour les messages de succès/erreur
4. Afficher des messages d'erreur contextuels dans les formulaires
5. Gérer les erreurs HTTP (400, 401, 404, 500, etc.)
6. Afficher des indicateurs de chargement pendant les opérations

**Structure attendue :**
- Service : `error-handler.service.ts` (optionnel)
- Utilisation : Dans les composants de réinitialisation

**Exigences :**
- Messages clairs et compréhensibles pour l'utilisateur
- Actions possibles pour chaque type d'erreur
- Utiliser Angular Material Snackbar pour les notifications
- Design cohérent avec le reste de l'application
- Gestion des erreurs réseau (timeout, connexion perdue)
```

---

## 📋 Prompt 7 : Intégration Complète - Tous les Composants

### Prompt

```
Je dois intégrer complètement la fonctionnalité de récupération de mot de passe oublié dans mon application Angular de gestion de recouvrement de créances.

**Contexte :**
- Backend Spring Boot avec endpoints REST fonctionnels
- Frontend Angular avec TypeScript, RxJS, et Angular Material
- Application existante avec système d'authentification

**Fonctionnalités à intégrer :**
1. Service Angular pour les appels API
2. Page "Mot de passe oublié" (`/forgot-password`)
3. Page "Réinitialisation de mot de passe" (`/reset-password?token={token}`)
4. Lien "Mot de passe oublié ?" sur la page de connexion
5. Composant de validation de force du mot de passe (optionnel mais recommandé)
6. Gestion des erreurs et messages utilisateur

**Backend disponible :**
- `POST /api/auth/forgot-password` - Demande de réinitialisation
- `GET /api/auth/reset-password/validate?token={token}` - Validation du token
- `POST /api/auth/reset-password` - Réinitialisation du mot de passe
- `POST /api/auth/forgot-password/resend` - Renvoyer un email

**Tâches complètes :**
1. **Service :**
   - Créer `password-reset.service.ts` avec toutes les méthodes nécessaires
   - Gérer les erreurs HTTP
   - Utiliser RxJS (Observable, catchError)

2. **Pages :**
   - Créer la page "Mot de passe oublié" avec formulaire et validation
   - Créer la page "Réinitialisation" avec validation du token et formulaire
   - Ajouter les routes dans le module de routing

3. **Intégration :**
   - Ajouter le lien "Mot de passe oublié ?" sur la page de connexion
   - Intégrer le composant de validation de force du mot de passe (optionnel)

4. **Gestion des erreurs :**
   - Gérer tous les cas d'erreur possibles
   - Afficher des messages clairs et actionnables
   - Utiliser des snackbars Material pour les notifications

5. **UX/UI :**
   - Design cohérent avec le reste de l'application
   - Responsive design (mobile/tablette/desktop)
   - Indicateurs de chargement
   - Messages de confirmation

**Structure attendue :**
- Service : `password-reset.service.ts`
- Composants : `forgot-password.component.ts`, `reset-password.component.ts`
- Optionnel : `password-strength.component.ts`
- Routes : `/forgot-password`, `/reset-password`

**Exigences :**
- Utiliser Angular Material pour tous les composants UI
- Utiliser Reactive Forms pour les formulaires
- Validation en temps réel
- Gestion complète des erreurs
- Design professionnel et moderne
- Responsive design
- Utiliser RxJS pour la gestion asynchrone
- Tester tous les cas d'usage (succès, erreurs, token invalide, etc.)

**Références :**
- Voir `GUIDE_RECUPERATION_MOT_DE_PASSE.md` pour les détails du backend
- Voir `RESUME_ENDPOINTS_RECUPERATION_MDP.md` pour les endpoints
```

---

## 📋 Résumé des Prompts

### Prompts par Priorité

1. **Priorité Haute :**
   - Prompt 1 : Service Angular pour la Récupération de Mot de Passe
   - Prompt 2 : Page "Mot de Passe Oublié"
   - Prompt 3 : Page "Réinitialisation de Mot de Passe"
   - Prompt 4 : Ajout du Lien sur la Page de Connexion

2. **Priorité Moyenne :**
   - Prompt 5 : Validation de la Force du Mot de Passe (Composant Réutilisable)
   - Prompt 6 : Gestion des Erreurs et Messages Utilisateur

3. **Priorité Basse :**
   - Prompt 7 : Intégration Complète (pour vérifier que tout est bien intégré)

---

## 📝 Notes d'Utilisation

### Comment Utiliser ces Prompts

1. **Copier le prompt complet** dans votre outil de développement IA (ChatGPT, Claude, etc.)
2. **Adapter le prompt** selon votre structure de code existante
3. **Implémenter étape par étape** en suivant l'ordre de priorité
4. **Tester chaque fonctionnalité** après chaque implémentation
5. **Itérer** si nécessaire pour affiner les résultats

### Structure Recommandée

Pour chaque prompt :
1. Lire attentivement le contexte et les exigences
2. Vérifier que les endpoints backend sont disponibles et fonctionnels
3. Implémenter les modifications étape par étape
4. Tester chaque fonctionnalité avec des données réelles
5. Documenter les changements

### Tests Recommandés

Pour chaque fonctionnalité implémentée :
1. Tester le cas de succès
2. Tester les cas d'erreur (token invalide, email inexistant, etc.)
3. Tester la validation des formulaires
4. Tester le responsive design
5. Tester la gestion des erreurs réseau

---

**Date :** 2025-01-05  
**Status :** ✅ Prompts prêts pour intégration frontend

