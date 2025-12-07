# 📋 Changements Frontend : Filtrage par Créateur d'Utilisateur

## 🎯 Objectif

Implémenter le filtrage des utilisateurs par créateur côté frontend pour que :
- **Les chefs** ne voient que les agents qu'ils ont créés
- **Le SUPER_ADMIN** voit tous les utilisateurs

---

## ✅ Modifications Backend Appliquées

### 1. Nouveau Champ dans l'Entité

**Champ ajouté :** `createur` (ManyToOne vers Utilisateur)

### 2. Nouvelle Migration SQL

**Fichier :** `V1_5__Add_Createur_To_Utilisateur.sql`
- Ajoute la colonne `createur_id` (nullable)
- Ajoute la contrainte de clé étrangère
- Ajoute un index pour les performances

### 3. Modifications des Endpoints

**Endpoint modifié :** `GET /api/admin/utilisateurs`
- **Avant :** Retournait tous les utilisateurs
- **Après :** 
  - **SUPER_ADMIN** : Voit tous les utilisateurs
  - **CHEF** : Voit uniquement les utilisateurs qu'il a créés

**Endpoint modifié :** `POST /api/admin/utilisateurs`
- Enregistre automatiquement le créateur (utilisateur connecté)
- Le créateur est extrait du token JWT

**Endpoint modifié :** `GET /api/users/chef/{chefId}`
- **Avant :** Retournait tous les agents du département du chef
- **Après :** Retourne uniquement les agents créés par ce chef
- **Exception :** SUPER_ADMIN voit tous les agents

---

## 🔄 Changements Nécessaires Côté Frontend

### ⚠️ **IMPORTANT : Aucun Changement Nécessaire dans les Appels API**

Les endpoints fonctionnent automatiquement avec le filtre selon le rôle de l'utilisateur connecté. Le frontend n'a **pas besoin** de modifier les appels API existants.

**Cependant**, il y a des **améliorations recommandées** pour une meilleure expérience utilisateur.

---

## 📊 Comportement des Endpoints

### 1. GET /api/admin/utilisateurs

**Comportement automatique :**
- Le backend extrait l'utilisateur connecté depuis le token JWT
- Si SUPER_ADMIN → Retourne tous les utilisateurs
- Si CHEF → Retourne uniquement les utilisateurs créés par ce chef
- Si autre rôle → Retourne liste vide

**Frontend :** Aucun changement nécessaire dans l'appel API

**Exemple d'appel (inchangé) :**
```typescript
this.http.get(`${this.apiUrl}/admin/utilisateurs`, {
  params: { role, actif, recherche, page, size }
})
```

**Résultat :**
- Le backend applique automatiquement le filtre selon le rôle
- Le frontend reçoit la liste filtrée sans modification

---

### 2. POST /api/admin/utilisateurs

**Comportement automatique :**
- Le backend extrait le créateur depuis le token JWT
- Le créateur est automatiquement enregistré dans `createur_id`
- Pas besoin de passer le créateur dans le body

**Frontend :** Aucun changement nécessaire dans l'appel API

**Exemple d'appel (inchangé) :**
```typescript
this.http.post(`${this.apiUrl}/admin/utilisateurs`, utilisateurData)
```

**Résultat :**
- Le créateur est automatiquement défini par le backend
- Le nouvel utilisateur est lié au créateur

---

### 3. GET /api/users/chef/{chefId}

**Comportement modifié :**
- **Avant :** Retournait tous les agents du département
- **Après :** Retourne uniquement les agents créés par ce chef
- **Exception :** Si chefId = SUPER_ADMIN → Retourne tous les agents

**Frontend :** Aucun changement nécessaire si vous utilisez déjà cet endpoint

**Exemple d'appel (inchangé) :**
```typescript
this.http.get(`${this.apiUrl}/users/chef/${chefId}`)
```

**Résultat :**
- Le backend filtre automatiquement par créateur
- Le frontend reçoit uniquement les agents du chef

---

## 🎨 Améliorations Frontend Recommandées (Optionnelles)

### 1. Affichage du Créateur dans la Liste

**Recommandation :** Afficher qui a créé chaque utilisateur dans la liste

**Exemple d'affichage :**
```typescript
// Dans le composant de liste
<div *ngIf="utilisateur.createur">
  Créé par: {{ utilisateur.createur.nom }} {{ utilisateur.createur.prenom }}
</div>
```

**Note :** Le champ `createur` n'est pas inclus dans la réponse JSON par défaut (à cause de `@JsonIgnore`). Si vous voulez l'afficher, vous devrez :
- Soit modifier le backend pour inclure `createurId` dans la réponse
- Soit faire un appel séparé pour récupérer les informations du créateur

---

### 2. Badge "Mes Agents" pour les Chefs

**Recommandation :** Ajouter un badge ou un indicateur visuel pour montrer que les chefs ne voient que leurs agents

**Exemple :**
```html
<div class="info-badge" *ngIf="currentUser.role !== 'SUPER_ADMIN'">
  <mat-icon>info</mat-icon>
  <span>Vous ne voyez que les agents que vous avez créés</span>
</div>
```

---

### 3. Message d'Information pour SUPER_ADMIN

**Recommandation :** Afficher un message indiquant que le SUPER_ADMIN voit tous les utilisateurs

**Exemple :**
```html
<div class="admin-badge" *ngIf="currentUser.role === 'SUPER_ADMIN'">
  <mat-icon>admin_panel_settings</mat-icon>
  <span>Vue complète : Tous les utilisateurs</span>
</div>
```

---

### 4. Filtre Visuel par Créateur (Optionnel)

**Recommandation :** Ajouter un filtre visuel pour permettre au SUPER_ADMIN de filtrer par créateur

**Exemple :**
```typescript
// Dans le composant
filterByCreator(createurId: number | null) {
  if (createurId === null) {
    // Afficher tous les utilisateurs
    this.loadAllUsers();
  } else {
    // Filtrer par créateur (nécessite un nouvel endpoint backend)
    this.loadUsersByCreator(createurId);
  }
}
```

**Note :** Cela nécessiterait un nouvel endpoint backend : `GET /api/admin/utilisateurs?createurId={id}`

---

## 📋 Checklist Frontend

### ✅ Vérifications (Sans Modification Nécessaire)

- [ ] **GET /api/admin/utilisateurs** : Vérifier que la liste s'affiche correctement
  - SUPER_ADMIN doit voir tous les utilisateurs
  - CHEF doit voir uniquement ses agents
- [ ] **POST /api/admin/utilisateurs** : Vérifier que la création fonctionne
  - Le créateur est automatiquement défini par le backend
- [ ] **GET /api/users/chef/{chefId}** : Vérifier que le filtrage fonctionne
  - Les chefs ne voient que leurs agents

### 🎨 Améliorations Optionnelles

- [ ] Ajouter un badge/info pour indiquer le filtrage actif
- [ ] Afficher le créateur dans la liste (si backend modifié)
- [ ] Ajouter un message pour SUPER_ADMIN (vue complète)
- [ ] Ajouter un filtre par créateur pour SUPER_ADMIN (nécessite nouvel endpoint)

---

## 🔍 Tests à Effectuer

### Test 1 : Création d'Agent par un Chef

**Scénario :**
1. Se connecter en tant que CHEF_DEPARTEMENT_DOSSIER
2. Créer un nouvel agent (AGENT_DOSSIER)
3. Vérifier que l'agent apparaît dans la liste du chef
4. Se connecter en tant qu'un autre chef du même département
5. Vérifier que l'agent n'apparaît PAS dans sa liste

**Résultat attendu :**
- ✅ Chaque chef ne voit que ses propres agents

---

### Test 2 : Vue SUPER_ADMIN

**Scénario :**
1. Se connecter en tant que SUPER_ADMIN
2. Accéder à la liste des utilisateurs
3. Vérifier que TOUS les utilisateurs sont visibles (tous les chefs et tous les agents)

**Résultat attendu :**
- ✅ SUPER_ADMIN voit tous les utilisateurs

---

### Test 3 : Création par SUPER_ADMIN

**Scénario :**
1. Se connecter en tant que SUPER_ADMIN
2. Créer un nouvel agent
3. Vérifier que l'agent apparaît dans la liste
4. Vérifier que le créateur est bien SUPER_ADMIN (ou NULL)

**Résultat attendu :**
- ✅ L'agent est créé avec succès
- ✅ Le créateur est enregistré correctement

---

## 📝 Résumé des Changements

| Aspect | État |
|--------|-----|
| **Modifications API nécessaires** | ❌ Aucune |
| **Modifications d'appels HTTP** | ❌ Aucune |
| **Comportement automatique** | ✅ Filtrage automatique selon le rôle |
| **Améliorations optionnelles** | ✅ Voir section "Améliorations Recommandées" |

---

## 🎯 Points Clés

1. **Aucun changement nécessaire** : Les endpoints fonctionnent automatiquement avec le filtre
2. **Filtrage transparent** : Le backend applique le filtre selon le rôle de l'utilisateur connecté
3. **SUPER_ADMIN exception** : Le SUPER_ADMIN voit tous les utilisateurs (pas de filtre)
4. **Chefs filtrés** : Les chefs ne voient que les utilisateurs qu'ils ont créés

---

## 🔄 Flux Complet

### Scénario : Chef crée un Agent

```
1. Chef se connecte (token JWT contient son ID)
   ↓
2. Chef accède à la page de création d'utilisateur
   ↓
3. Chef remplit le formulaire et soumet
   ↓
4. Frontend envoie POST /api/admin/utilisateurs
   (avec token JWT dans le header Authorization)
   ↓
5. Backend extrait le créateur depuis le token
   ↓
6. Backend crée l'utilisateur avec createur_id = chef connecté
   ↓
7. Backend retourne l'utilisateur créé
   ↓
8. Frontend affiche le succès
   ↓
9. Chef accède à la liste des utilisateurs
   ↓
10. Frontend envoie GET /api/admin/utilisateurs
    (avec token JWT dans le header)
    ↓
11. Backend extrait le chef depuis le token
    ↓
12. Backend filtre : findByCreateurId(chefId)
    ↓
13. Backend retourne uniquement les agents créés par ce chef
    ↓
14. Frontend affiche la liste filtrée
```

### Scénario : SUPER_ADMIN accède à la liste

```
1. SUPER_ADMIN se connecte
   ↓
2. SUPER_ADMIN accède à la liste des utilisateurs
   ↓
3. Frontend envoie GET /api/admin/utilisateurs
   ↓
4. Backend détecte SUPER_ADMIN
   ↓
5. Backend retourne TOUS les utilisateurs (pas de filtre)
   ↓
6. Frontend affiche tous les utilisateurs
```

---

## ✅ Conclusion

### Changements Nécessaires : **AUCUN**

- ✅ Les endpoints fonctionnent automatiquement
- ✅ Le filtrage est transparent pour le frontend
- ✅ Aucune modification d'appels API nécessaire

### Améliorations Recommandées (Optionnelles)

- ✅ Ajouter des badges/informations visuelles
- ✅ Afficher le créateur dans la liste (si backend modifié)
- ✅ Ajouter des messages informatifs selon le rôle

---

**Date :** 2025-01-05  
**Status :** ✅ Aucun changement frontend nécessaire - Filtrage automatique par le backend

