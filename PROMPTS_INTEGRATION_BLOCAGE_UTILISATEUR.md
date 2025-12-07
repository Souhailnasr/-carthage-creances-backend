# 🔒 Prompts d'Intégration : Blocage/Déblocage d'Utilisateur

## 📋 Identification de l'Endpoint Backend

### Endpoint de Désactivation (Blocage)

**URL :** `PUT /api/admin/utilisateurs/{id}/desactiver`  
**Contrôleur :** `AdminUtilisateurController.java`  
**Ligne :** 361-421  
**Méthode :** `desactiverUtilisateur()`

**Autorisation :**
- `@PreAuthorize("hasRole('SUPER_ADMIN')")` - Seul le SuperAdmin peut désactiver
- Vérification du token JWT dans le header `Authorization`

**Headers Requis :**
```
Authorization: Bearer {token}
```

**Paramètres :**
- `{id}` : ID de l'utilisateur à désactiver (PathVariable)

**Réponse Succès (200 OK) :**
```json
{
  "id": 1,
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "actif": false,
  "roleUtilisateur": "AGENT_DOSSIER",
  ...
}
```

**Réponses d'Erreur :**
- `400 Bad Request` : `{"error": "L'ID utilisateur est requis"}`
- `400 Bad Request` : `{"error": "Impossible de désactiver un Superadmin"}`
- `401 Unauthorized` : Si le token est invalide ou manquant
- `404 Not Found` : `{"error": "Utilisateur non trouvé avec l'ID: {id}"}`
- `500 Internal Server Error` : Erreur serveur

**Protection Spéciale :**
- ❌ **Impossible de désactiver un SuperAdmin** (retourne erreur 400)

---

### Endpoint d'Activation (Déblocage)

**URL :** `PUT /api/admin/utilisateurs/{id}/activer`  
**Contrôleur :** `AdminUtilisateurController.java`  
**Ligne :** 302-355  
**Méthode :** `activerUtilisateur()`

**Autorisation :**
- `@PreAuthorize("hasRole('SUPER_ADMIN')")` - Seul le SuperAdmin peut activer
- Vérification du token JWT dans le header `Authorization`

**Headers Requis :**
```
Authorization: Bearer {token}
```

**Paramètres :**
- `{id}` : ID de l'utilisateur à activer (PathVariable)

**Réponse Succès (200 OK) :**
```json
{
  "id": 1,
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "actif": true,
  "roleUtilisateur": "AGENT_DOSSIER",
  ...
}
```

**Réponses d'Erreur :**
- `400 Bad Request` : `{"error": "L'ID utilisateur est requis"}`
- `401 Unauthorized` : Si le token est invalide ou manquant
- `404 Not Found` : `{"error": "Utilisateur non trouvé avec l'ID: {id}"}`
- `500 Internal Server Error` : Erreur serveur

---

## 📝 Structure de l'Entité Utilisateur

**Champ `actif` :**
- Type : `Boolean`
- Valeur par défaut : `false`
- Description : Indique si l'utilisateur est actif (non bloqué) ou inactif (bloqué)

**Logique de calcul automatique :**
- Le champ `actif` peut être calculé automatiquement basé sur `derniereConnexion` et `derniereDeconnexion`
- Mais les endpoints `/activer` et `/desactiver` forcent manuellement la valeur

---

## 🎨 Prompts pour l'Intégration Frontend

---

## PROMPT 1 : Créer le Service Angular pour le Blocage/Déblocage

```
Je dois créer un service Angular pour gérer le blocage et le déblocage d'utilisateurs.

**Contexte :**
- Backend Spring Boot avec endpoint : `PUT /api/admin/utilisateurs/{id}/desactiver` et `PUT /api/admin/utilisateurs/{id}/activer`
- Autorisation : SUPER_ADMIN uniquement
- Headers requis : `Authorization: Bearer {token}`

**Fichier à créer/modifier :** `src/app/core/services/admin-utilisateur.service.ts`

**Fonctionnalités requises :**

1. **Méthode `bloquerUtilisateur(userId: number): Observable<Utilisateur>`**
   - Appelle `PUT /api/admin/utilisateurs/{userId}/desactiver`
   - Headers : `Authorization: Bearer {token}`
   - Retourne l'utilisateur mis à jour avec `actif: false`
   - Gère les erreurs :
     - 400 : "Impossible de désactiver un Superadmin" ou "L'ID utilisateur est requis"
     - 401 : Token invalide
     - 404 : Utilisateur non trouvé
     - 500 : Erreur serveur

2. **Méthode `debloquerUtilisateur(userId: number): Observable<Utilisateur>`**
   - Appelle `PUT /api/admin/utilisateurs/{userId}/activer`
   - Headers : `Authorization: Bearer {token}`
   - Retourne l'utilisateur mis à jour avec `actif: true`
   - Gère les erreurs :
     - 400 : "L'ID utilisateur est requis"
     - 401 : Token invalide
     - 404 : Utilisateur non trouvé
     - 500 : Erreur serveur

3. **Gestion des erreurs :**
   - Utiliser `catchError` pour intercepter les erreurs HTTP
   - Retourner des messages d'erreur clairs et traduits en français
   - Logger les erreurs pour le debugging

**Interface Utilisateur :**
```typescript
export interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  actif: boolean;
  roleUtilisateur: string;
  // ... autres champs
}
```

**Base URL :** `http://localhost:8089/carthage-creance/api/admin/utilisateurs`

Générez le code complet du service avec :
- Imports nécessaires (HttpClient, Observable, catchError, throwError)
- Injection de HttpClient et AuthService
- Méthodes complètes avec gestion d'erreurs
- Headers avec token JWT
- Types TypeScript appropriés
```

---

## PROMPT 2 : Créer/Modifier le Composant de Liste des Utilisateurs

```
Je dois créer ou modifier le composant Angular pour afficher la liste des utilisateurs avec des boutons de blocage/déblocage.

**Contexte :**
- Interface de gestion des utilisateurs pour SuperAdmin
- Affichage d'un tableau avec tous les utilisateurs
- Colonne "Statut" avec badge (Actif/Inactif)
- Bouton "Bloquer" pour les utilisateurs actifs
- Bouton "Débloquer" pour les utilisateurs inactifs
- Protection : Ne pas afficher le bouton pour le SuperAdmin actuel

**Fichier à créer/modifier :** `src/app/admin/components/gestion-utilisateurs/gestion-utilisateurs.component.ts`

**Fonctionnalités requises :**

1. **Affichage de la liste :**
   - Tableau avec colonnes : Nom, Prénom, Email, Rôle, Statut, Actions
   - Badge pour le statut :
     - Vert "Actif" si `actif === true`
     - Rouge "Bloqué" si `actif === false`
   - Pagination si nécessaire

2. **Bouton de blocage/déblocage :**
   - Si `actif === true` : Afficher bouton "Bloquer" (icône lock ou ban)
   - Si `actif === false` : Afficher bouton "Débloquer" (icône unlock)
   - Désactiver le bouton si l'utilisateur est un SuperAdmin
   - Afficher un indicateur de chargement pendant l'opération

3. **Méthode `bloquerUtilisateur(user: Utilisateur): void`**
   - Afficher une confirmation : "Êtes-vous sûr de vouloir bloquer cet utilisateur ?"
   - Appeler `adminUtilisateurService.bloquerUtilisateur(user.id)`
   - En cas de succès :
     - Mettre à jour la liste (recharger ou mettre à jour l'utilisateur localement)
     - Afficher un message de succès : "Utilisateur bloqué avec succès"
   - En cas d'erreur :
     - Afficher un message d'erreur approprié
     - Si erreur 400 : "Impossible de bloquer un SuperAdmin"
     - Si erreur 404 : "Utilisateur non trouvé"
     - Si erreur 401 : "Vous n'êtes pas autorisé à effectuer cette action"

4. **Méthode `debloquerUtilisateur(user: Utilisateur): void`**
   - Afficher une confirmation : "Êtes-vous sûr de vouloir débloquer cet utilisateur ?"
   - Appeler `adminUtilisateurService.debloquerUtilisateur(user.id)`
   - En cas de succès :
     - Mettre à jour la liste (recharger ou mettre à jour l'utilisateur localement)
     - Afficher un message de succès : "Utilisateur débloqué avec succès"
   - En cas d'erreur :
     - Afficher un message d'erreur approprié

5. **Protection :**
   - Ne pas afficher le bouton si `user.roleUtilisateur === 'SUPER_ADMIN'`
   - Ou afficher le bouton désactivé avec un tooltip : "Impossible de bloquer un SuperAdmin"

**Template HTML :**
- Utiliser Angular Material pour le tableau (mat-table)
- Utiliser mat-icon pour les icônes (lock, lock_open)
- Utiliser mat-button avec couleur appropriée (warn pour bloquer, primary pour débloquer)
- Utiliser mat-dialog pour les confirmations
- Utiliser mat-snackbar pour les messages de succès/erreur

Générez le code complet du composant avec :
- Imports nécessaires
- Décorateur @Component
- Propriétés du composant
- Méthodes ngOnInit, bloquerUtilisateur, debloquerUtilisateur
- Gestion des erreurs
- Messages utilisateur
```

---

## PROMPT 3 : Créer le Template HTML pour la Liste des Utilisateurs

```
Je dois créer le template HTML pour afficher la liste des utilisateurs avec les boutons de blocage/déblocage.

**Fichier :** `src/app/admin/components/gestion-utilisateurs/gestion-utilisateurs.component.html`

**Structure requise :**

1. **En-tête :**
   - Titre : "Gestion des Utilisateurs"
   - Bouton "Actualiser" pour recharger la liste

2. **Tableau des utilisateurs :**
   - Utiliser `mat-table` d'Angular Material
   - Colonnes :
     - Nom
     - Prénom
     - Email
     - Rôle
     - Statut (avec badge coloré)
     - Actions (boutons bloquer/débloquer)

3. **Colonne Statut :**
   - Badge vert "Actif" si `user.actif === true`
   - Badge rouge "Bloqué" si `user.actif === false`
   - Utiliser `mat-chip` ou `mat-badge`

4. **Colonne Actions :**
   - Bouton "Bloquer" (icône lock, couleur warn) si `user.actif === true`
   - Bouton "Débloquer" (icône lock_open, couleur primary) si `user.actif === false`
   - Désactiver le bouton si `user.roleUtilisateur === 'SUPER_ADMIN'`
   - Afficher un spinner si l'opération est en cours

5. **Pagination :**
   - Utiliser `mat-paginator` si nécessaire

6. **Messages :**
   - Snackbar pour les messages de succès/erreur
   - Dialog de confirmation avant blocage/déblocage

**Style :**
- Design moderne et cohérent avec le reste de l'application
- Responsive pour mobile
- Couleurs appropriées (rouge pour bloquer, vert pour débloquer)

Générez le template HTML complet avec :
- Structure mat-table
- Colonnes définies
- Boutons avec icônes
- Badges de statut
- Gestion des états (loading, disabled)
- Responsive design
```

---

## PROMPT 4 : Ajouter les Styles CSS

```
Je dois créer les styles CSS pour le composant de gestion des utilisateurs.

**Fichier :** `src/app/admin/components/gestion-utilisateurs/gestion-utilisateurs.component.css`

**Styles requis :**

1. **Tableau :**
   - Largeur 100%
   - Lignes alternées (zebra striping)
   - Hover effect sur les lignes
   - Espacement approprié

2. **Badges de statut :**
   - Badge "Actif" : Fond vert clair, texte vert foncé
   - Badge "Bloqué" : Fond rouge clair, texte rouge foncé
   - Border-radius arrondi
   - Padding approprié

3. **Boutons d'action :**
   - Espacement entre les boutons
   - Taille d'icône appropriée
   - État disabled avec opacité réduite

4. **Responsive :**
   - Sur mobile : Tableau scrollable horizontalement
   - Colonnes importantes toujours visibles

5. **Loading state :**
   - Spinner centré pendant le chargement
   - Overlay semi-transparent

Générez les styles CSS complets avec :
- Classes pour le tableau
- Classes pour les badges
- Classes pour les boutons
- Media queries pour responsive
- Animations si nécessaire
```

---

## PROMPT 5 : Intégrer dans le Module Admin

```
Je dois intégrer le composant de gestion des utilisateurs dans le module admin.

**Fichier à modifier :** `src/app/admin/admin.module.ts`

**Modifications requises :**

1. **Imports :**
   - Importer `GestionUtilisateursComponent`
   - Importer `AdminUtilisateurService` dans les providers
   - Importer les modules Angular Material nécessaires :
     - MatTableModule
     - MatButtonModule
     - MatIconModule
     - MatChipsModule
     - MatDialogModule
     - MatSnackBarModule
     - MatPaginatorModule
     - MatProgressSpinnerModule

2. **Déclarations :**
   - Ajouter `GestionUtilisateursComponent` dans `declarations`

3. **Exports :**
   - Exporter `GestionUtilisateursComponent` si nécessaire

4. **Providers :**
   - Ajouter `AdminUtilisateurService` dans `providers`

5. **Routing :**
   - Ajouter la route dans le module de routing admin :
     - Path : `/admin/utilisateurs`
     - Component : `GestionUtilisateursComponent`
     - Guard : Vérifier que l'utilisateur est SUPER_ADMIN

Générez le code complet du module avec toutes les modifications nécessaires.
```

---

## PROMPT 6 : Ajouter la Route dans le Routing Admin

```
Je dois ajouter la route pour la gestion des utilisateurs dans le routing admin.

**Fichier à modifier :** `src/app/admin/admin-routing.module.ts` (ou équivalent)

**Route à ajouter :**

```typescript
{
  path: 'utilisateurs',
  component: GestionUtilisateursComponent,
  canActivate: [SuperAdminGuard], // Vérifier que l'utilisateur est SUPER_ADMIN
  data: {
    title: 'Gestion des Utilisateurs',
    roles: ['SUPER_ADMIN']
  }
}
```

**Guard à créer si nécessaire :**
- `SuperAdminGuard` : Vérifie que l'utilisateur connecté a le rôle SUPER_ADMIN
- Redirige vers la page d'accueil si l'utilisateur n'est pas SuperAdmin

Générez le code complet du routing avec la route et le guard si nécessaire.
```

---

## PROMPT 7 : Ajouter le Lien dans le Menu Admin

```
Je dois ajouter un lien vers la gestion des utilisateurs dans le menu de navigation admin.

**Fichier à modifier :** `src/app/admin/components/admin-nav/admin-nav.component.html` (ou équivalent)

**Lien à ajouter :**

- Icône : `people` ou `admin_panel_settings`
- Texte : "Gestion des Utilisateurs"
- Route : `/admin/utilisateurs`
- Visible uniquement pour SUPER_ADMIN

**Structure :**
```html
<mat-nav-list>
  <a mat-list-item routerLink="/admin/utilisateurs" routerLinkActive="active">
    <mat-icon>people</mat-icon>
    <span>Gestion des Utilisateurs</span>
  </a>
</mat-nav-list>
```

Générez le code HTML pour le menu avec le nouveau lien.
```

---

## 📋 Checklist d'Intégration

### Backend
- [x] ✅ Endpoint `/api/admin/utilisateurs/{id}/desactiver` existe
- [x] ✅ Endpoint `/api/admin/utilisateurs/{id}/activer` existe
- [x] ✅ Autorisation SUPER_ADMIN configurée
- [x] ✅ Protection contre la désactivation de SuperAdmin

### Frontend
- [ ] Créer `AdminUtilisateurService` avec méthodes `bloquerUtilisateur()` et `debloquerUtilisateur()`
- [ ] Créer/modifier `GestionUtilisateursComponent`
- [ ] Créer le template HTML avec tableau et boutons
- [ ] Ajouter les styles CSS
- [ ] Intégrer dans le module admin
- [ ] Ajouter la route dans le routing
- [ ] Ajouter le lien dans le menu admin
- [ ] Tester le blocage/déblocage
- [ ] Gérer les erreurs (SuperAdmin, utilisateur non trouvé, etc.)
- [ ] Ajouter les confirmations avant action
- [ ] Ajouter les messages de succès/erreur

---

## 🧪 Tests à Effectuer

1. **Test de blocage :**
   - Se connecter en tant que SuperAdmin
   - Accéder à la liste des utilisateurs
   - Cliquer sur "Bloquer" pour un utilisateur actif
   - Vérifier que la confirmation s'affiche
   - Confirmer
   - Vérifier que l'utilisateur est bloqué (badge rouge, bouton "Débloquer")
   - Vérifier le message de succès

2. **Test de déblocage :**
   - Cliquer sur "Débloquer" pour un utilisateur bloqué
   - Vérifier que la confirmation s'affiche
   - Confirmer
   - Vérifier que l'utilisateur est débloqué (badge vert, bouton "Bloquer")
   - Vérifier le message de succès

3. **Test de protection SuperAdmin :**
   - Essayer de bloquer un SuperAdmin
   - Vérifier que le bouton est désactivé ou que l'erreur s'affiche
   - Vérifier le message d'erreur : "Impossible de bloquer un SuperAdmin"

4. **Test d'autorisation :**
   - Se connecter avec un rôle autre que SuperAdmin
   - Vérifier que la page n'est pas accessible (guard)
   - Vérifier la redirection

---

## 📝 Notes Importantes

1. **Autorisation :**
   - Seul le SuperAdmin peut bloquer/débloquer des utilisateurs
   - Vérifier le rôle côté frontend avant d'afficher les boutons

2. **Protection SuperAdmin :**
   - Ne pas afficher le bouton de blocage pour les SuperAdmin
   - Ou afficher un message explicatif

3. **Gestion des erreurs :**
   - Toujours afficher des messages clairs à l'utilisateur
   - Logger les erreurs pour le debugging

4. **Confirmation :**
   - Toujours demander confirmation avant de bloquer/débloquer
   - Message clair : "Êtes-vous sûr de vouloir bloquer cet utilisateur ?"

5. **Mise à jour de la liste :**
   - Après blocage/déblocage, mettre à jour la liste localement
   - Ou recharger la liste depuis le serveur

---

**Date de création :** 2025-01-05  
**Status :** ✅ Prompts prêts pour intégration


