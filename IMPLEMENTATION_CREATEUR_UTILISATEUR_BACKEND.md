# ✅ Implémentation : Champ Créateur pour Utilisateur

## 🎯 Objectif

Ajouter le champ `createur_id` dans l'entité `Utilisateur` et implémenter le filtrage pour que :
- **Les chefs** ne voient que les agents qu'ils ont créés
- **Le SUPER_ADMIN** voit tous les utilisateurs

---

## 📋 Modifications Backend Appliquées

### 1. Entité `Utilisateur`

**Fichier :** `src/main/java/projet/carthagecreance_backend/Entity/Utilisateur.java`

**Ajouts :**
```java
/**
 * Utilisateur qui a créé cet utilisateur (chef créateur)
 * Nullable pour les utilisateurs existants et SUPER_ADMIN
 */
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "createur_id", nullable = true)
@JsonIgnore
private Utilisateur createur;

/**
 * Liste des utilisateurs créés par cet utilisateur (si c'est un chef)
 */
@OneToMany(mappedBy = "createur", fetch = FetchType.LAZY)
@Builder.Default
@JsonIgnore
private List<Utilisateur> utilisateursCrees = new ArrayList<>();
```

**Caractéristiques :**
- `nullable = true` : Pour les utilisateurs existants et SUPER_ADMIN
- `FetchType.LAZY` : Performance optimale
- `@JsonIgnore` : Évite la récursion infinie dans les réponses JSON

---

### 2. Migration SQL

**Fichier :** `src/main/resources/db/migration/V1_5__Add_Createur_To_Utilisateur.sql`

**Contenu :**
```sql
-- Ajouter la colonne createur_id (nullable pour les utilisateurs existants)
ALTER TABLE utilisateur 
ADD COLUMN createur_id BIGINT NULL;

-- Ajouter la contrainte de clé étrangère
ALTER TABLE utilisateur 
ADD CONSTRAINT FK_utilisateur_createur 
FOREIGN KEY (createur_id) REFERENCES utilisateur(id) ON DELETE SET NULL;

-- Ajouter un index pour améliorer les performances
CREATE INDEX idx_utilisateur_createur ON utilisateur(createur_id);
```

**Caractéristiques :**
- `ON DELETE SET NULL` : Si le créateur est supprimé, `createur_id` devient NULL (historique préservé)
- Index pour optimiser les requêtes de filtrage

---

### 3. Repository `UtilisateurRepository`

**Fichier :** `src/main/java/projet/carthagecreance_backend/Repository/UtilisateurRepository.java`

**Ajouts :**
```java
/**
 * Trouve tous les utilisateurs créés par un créateur spécifique
 */
List<Utilisateur> findByCreateurId(Long createurId);

/**
 * Trouve tous les utilisateurs créés par un créateur avec un rôle spécifique
 */
List<Utilisateur> findByCreateurIdAndRoleUtilisateur(Long createurId, RoleUtilisateur roleUtilisateur);
```

**Utilisation :**
- `findByCreateurId()` : Pour récupérer tous les utilisateurs créés par un chef
- `findByCreateurIdAndRoleUtilisateur()` : Pour filtrer par créateur ET rôle (optimisé)

---

### 4. Service `UtilisateurService` - Interface

**Fichier :** `src/main/java/projet/carthagecreance_backend/Service/UtilisateurService.java`

**Modification :**
```java
// Avant
AuthenticationResponse createUtilisateur(Utilisateur utilisateur);

// Après
AuthenticationResponse createUtilisateur(Utilisateur utilisateur, Utilisateur createur);
```

**Raison :** Permet de passer le créateur lors de la création

---

### 5. Service `UtilisateurServiceImpl` - Implémentation

**Fichier :** `src/main/java/projet/carthagecreance_backend/Service/Impl/UtilisateurServiceImpl.java`

#### 5.1. Modification de `createUtilisateur()`

**Ajout :**
```java
// ✅ Définir le créateur (si fourni et si ce n'est pas un SUPER_ADMIN)
if (createur != null && createur.getId() != null) {
    // Ne pas définir de créateur pour les SUPER_ADMIN
    if (utilisateur.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN) {
        utilisateur.setCreateur(createur);
    }
}
```

**Comportement :**
- Si créateur fourni → Enregistre le créateur
- Si SUPER_ADMIN créé → Pas de créateur (NULL)
- Si créateur = null → Pas de créateur (pour utilisateurs existants)

#### 5.2. Modification de `getAgentsByChef()`

**Avant :**
```java
// Chef dossier : tous les agents dossier
agents = utilisateurRepository.findByRoleUtilisateur(RoleUtilisateur.AGENT_DOSSIER);
```

**Après :**
```java
// ✅ Chef dossier : uniquement les agents dossier créés par ce chef
agents = utilisateurRepository.findByCreateurIdAndRoleUtilisateur(chefId, RoleUtilisateur.AGENT_DOSSIER);
```

**Comportement :**
- **SUPER_ADMIN** : Voit tous les agents (pas de filtre par créateur)
- **CHEF** : Voit uniquement les agents qu'il a créés (filtré par `createur_id`)

---

### 6. Controller `AdminUtilisateurController`

**Fichier :** `src/main/java/projet/carthagecreance_backend/Controller/AdminUtilisateurController.java`

#### 6.1. Modification de `createUtilisateur()`

**Ajout :**
```java
// ✅ Extraire le créateur depuis le token JWT
Utilisateur createur = userExtractionService.extractUserFromToken(authHeader);

// ✅ Vérifier les droits : SUPER_ADMIN ou CHEF peut créer
if (createur.getRoleUtilisateur() != RoleUtilisateur.SUPER_ADMIN && 
    !estChef(createur.getRoleUtilisateur())) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "Vous n'avez pas les droits pour créer un utilisateur"));
}

// ✅ Créer l'utilisateur avec le créateur
utilisateurService.createUtilisateur(utilisateur, createur);
```

**Comportement :**
- Extrait le créateur depuis le token JWT
- Vérifie les droits (SUPER_ADMIN ou CHEF)
- Passe le créateur au service

#### 6.2. Modification de `getAllUtilisateurs()`

**Ajout :**
```java
// ✅ Extraire l'utilisateur connecté pour appliquer le filtre par créateur
Utilisateur utilisateurConnecte = userExtractionService.extractUserFromToken(authHeader);

List<Utilisateur> tousUtilisateurs;

// ✅ Filtrer selon le rôle de l'utilisateur connecté
if (utilisateurConnecte.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN) {
    // SUPER_ADMIN voit tous les utilisateurs
    tousUtilisateurs = utilisateurRepository.findAll();
} else if (estChef(utilisateurConnecte.getRoleUtilisateur())) {
    // Chef ne voit que les utilisateurs qu'il a créés
    tousUtilisateurs = utilisateurRepository.findByCreateurId(utilisateurConnecte.getId());
} else {
    // Autres rôles : liste vide
    tousUtilisateurs = new ArrayList<>();
}
```

**Comportement :**
- **SUPER_ADMIN** : Voit tous les utilisateurs
- **CHEF** : Voit uniquement les utilisateurs qu'il a créés
- **Autres rôles** : Liste vide (pas d'accès)

#### 6.3. Ajout de la méthode `estChef()`

**Ajout :**
```java
/**
 * Vérifie si un rôle est un rôle de chef
 */
private boolean estChef(RoleUtilisateur role) {
    return role == RoleUtilisateur.CHEF_DEPARTEMENT_DOSSIER ||
           role == RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE ||
           role == RoleUtilisateur.CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE ||
           role == RoleUtilisateur.CHEF_DEPARTEMENT_FINANCE;
}
```

---

### 7. Controller `UtilisateurController`

**Fichier :** `src/main/java/projet/carthagecreance_backend/Controller/UtilisateurController.java`

**Modification :**
```java
// ✅ Pour l'inscription publique, pas de créateur (null)
AuthenticationResponse response = utilisateurService.createUtilisateur(utilisateur, null);
```

**Raison :** L'endpoint `/api/users` est utilisé pour l'inscription publique, donc pas de créateur

---

## 🔄 Nouveau Flux de Création d'Utilisateur

```
1. Chef/SUPER_ADMIN se connecte
   ↓
2. Chef/SUPER_ADMIN accède à la page de création
   ↓
3. Chef/SUPER_ADMIN remplit le formulaire
   ↓
4. Frontend envoie POST /api/admin/utilisateurs
   (avec token JWT dans Authorization header)
   ↓
5. Backend extrait le créateur depuis le token
   ↓
6. Backend vérifie les droits (SUPER_ADMIN ou CHEF)
   ↓
7. Backend crée l'utilisateur avec createur_id = créateur
   (sauf si SUPER_ADMIN créé → createur_id = NULL)
   ↓
8. Backend retourne l'utilisateur créé
   ↓
9. Frontend affiche le succès
```

---

## 🔄 Nouveau Flux de Listing des Utilisateurs

```
1. Utilisateur se connecte (Chef ou SUPER_ADMIN)
   ↓
2. Utilisateur accède à la liste des utilisateurs
   ↓
3. Frontend envoie GET /api/admin/utilisateurs
   (avec token JWT dans Authorization header)
   ↓
4. Backend extrait l'utilisateur connecté depuis le token
   ↓
5. Backend vérifie le rôle :
   - Si SUPER_ADMIN → findAll() (tous les utilisateurs)
   - Si CHEF → findByCreateurId(chefId) (ses agents uniquement)
   - Sinon → Liste vide
   ↓
6. Backend applique les autres filtres (role, actif, recherche)
   ↓
7. Backend retourne la liste filtrée
   ↓
8. Frontend affiche la liste
```

---

## 📊 Comportement par Rôle

| Rôle | Création | Listing |
|------|----------|---------|
| **SUPER_ADMIN** | Peut créer tous les types d'utilisateurs | Voit **TOUS** les utilisateurs |
| **CHEF_DEPARTEMENT_DOSSIER** | Peut créer AGENT_DOSSIER | Voit uniquement **ses agents** |
| **CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE** | Peut créer AGENT_RECOUVREMENT_AMIABLE | Voit uniquement **ses agents** |
| **CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE** | Peut créer AGENT_RECOUVREMENT_JURIDIQUE | Voit uniquement **ses agents** |
| **CHEF_DEPARTEMENT_FINANCE** | Peut créer AGENT_FINANCE | Voit uniquement **ses agents** |
| **Autres rôles** | ❌ Pas de droits | ❌ Liste vide |

---

## ✅ Résultat

### Avant l'implémentation :
- ❌ Tous les chefs du même département voyaient tous les agents
- ❌ Pas de traçabilité (qui a créé quel agent)
- ❌ Pas de séparation des responsabilités

### Après l'implémentation :
- ✅ Chaque chef ne voit que ses propres agents
- ✅ Traçabilité complète (createur_id enregistré)
- ✅ Séparation claire des responsabilités
- ✅ SUPER_ADMIN voit tous les utilisateurs (exception)

---

## 🧪 Tests à Effectuer

### Test 1 : Création par Chef

1. Se connecter en tant que `CHEF_DEPARTEMENT_DOSSIER`
2. Créer un agent `AGENT_DOSSIER`
3. Vérifier dans la base : `SELECT createur_id FROM utilisateur WHERE email = 'agent@example.com'`
4. Résultat attendu : `createur_id` = ID du chef connecté

### Test 2 : Listing par Chef

1. Se connecter en tant que `CHEF_DEPARTEMENT_DOSSIER` (ID = 1)
2. Créer 2 agents
3. Se connecter en tant qu'un autre `CHEF_DEPARTEMENT_DOSSIER` (ID = 2)
4. Créer 1 agent
5. Accéder à la liste des utilisateurs avec le chef ID = 1
6. Résultat attendu : Voit uniquement ses 2 agents (pas celui du chef ID = 2)

### Test 3 : Listing par SUPER_ADMIN

1. Se connecter en tant que `SUPER_ADMIN`
2. Accéder à la liste des utilisateurs
3. Résultat attendu : Voit TOUS les utilisateurs (tous les chefs et tous les agents)

---

## 📝 Fichiers Modifiés

| Fichier | Modification |
|---------|-------------|
| `Utilisateur.java` | ✅ Ajout du champ `createur` et `utilisateursCrees` |
| `V1_5__Add_Createur_To_Utilisateur.sql` | ✅ Migration SQL |
| `UtilisateurRepository.java` | ✅ Ajout de `findByCreateurId()` et `findByCreateurIdAndRoleUtilisateur()` |
| `UtilisateurService.java` | ✅ Modification signature `createUtilisateur()` |
| `UtilisateurServiceImpl.java` | ✅ Modification `createUtilisateur()` et `getAgentsByChef()` |
| `AdminUtilisateurController.java` | ✅ Modification `createUtilisateur()` et `getAllUtilisateurs()` |
| `UtilisateurController.java` | ✅ Modification `createUtilisateur()` (passe null) |

---

## ⚠️ Points d'Attention

### 1. Migration des Données Existantes

**Problème :** Les utilisateurs existants auront `createur_id = NULL`

**Solution :** C'est normal et attendu. Les nouveaux utilisateurs créés après la migration auront leur `createur_id` défini.

### 2. Suppression du Créateur

**Comportement :** `ON DELETE SET NULL`
- Si un chef créateur est supprimé, les agents créés auront `createur_id = NULL`
- L'historique est préservé (les agents ne sont pas supprimés)

### 3. SUPER_ADMIN

**Comportement :** 
- Les SUPER_ADMIN créés n'ont pas de créateur (`createur_id = NULL`)
- C'est normal car ils sont généralement créés manuellement ou auto-créés

---

## 🎯 Prochaines Étapes

1. **Exécuter la migration SQL** : `V1_5__Add_Createur_To_Utilisateur.sql`
2. **Redémarrer le backend** : Pour charger les nouvelles modifications
3. **Tester la création** : Créer un agent et vérifier que `createur_id` est défini
4. **Tester le listing** : Vérifier que les chefs ne voient que leurs agents
5. **Tester SUPER_ADMIN** : Vérifier que SUPER_ADMIN voit tous les utilisateurs

---

**Date :** 2025-01-05  
**Status :** ✅ Implémentation complète - Prêt pour tests

