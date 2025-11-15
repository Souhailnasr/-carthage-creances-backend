# Guide Complet : Affectation et Clôture des Dossiers

## 📋 Vue d'ensemble

Ce guide explique comment implémenter la fonctionnalité d'affectation des dossiers validés vers les départements de recouvrement (amiable ou juridique) et la clôture des dossiers.

### Fonctionnalités à Implémenter

1. **Affectation au Recouvrement Amiable** : Affecte un dossier validé au chef du département recouvrement amiable
2. **Affectation au Recouvrement Juridique** : Affecte un dossier validé au chef du département recouvrement juridique
3. **Clôture de Dossier** : Clôture un dossier validé (statut CLOTURE, date de clôture)
4. **Liste des Dossiers Validés Disponibles** : Affiche uniquement les dossiers avec statut VALIDE et non clôturés

---

## 🎯 Règles Métier

### Conditions d'Affectation

- ✅ Le dossier doit avoir le **statut VALIDE**
- ✅ Le dossier doit avoir **valide = true**
- ✅ Le dossier ne doit **pas être clôturé** (dossierStatus != CLOTURE)
- ✅ Un **chef du département** correspondant doit exister dans la base de données

### Conditions de Clôture

- ✅ Le dossier doit avoir le **statut VALIDE**
- ✅ Le dossier doit avoir **valide = true**
- ✅ Le dossier ne doit **pas être déjà clôturé**

### Rôles Concernés

- **CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE** : Chef du département recouvrement amiable
- **CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE** : Chef du département recouvrement juridique
- **CHEF_DEPARTEMENT_DOSSIER** : Peut affecter et clôturer les dossiers

---

## 🔧 Implémentation Backend

### Étape 1 : Ajouter les Méthodes dans l'Interface

**Fichier** : `DossierService.java`

**Prompt à utiliser** : Voir `PROMPT_BACKEND_AFFECTATION_DOSSIERS.md` - PROMPT 1

### Étape 2 : Implémenter les Méthodes

**Fichier** : `DossierServiceImpl.java`

**Prompt à utiliser** : Voir `PROMPT_BACKEND_AFFECTATION_DOSSIERS.md` - PROMPT 2

**Méthodes à implémenter** :
- `affecterAuRecouvrementAmiable(Long dossierId)`
- `affecterAuRecouvrementJuridique(Long dossierId)`
- `cloturerDossier(Long dossierId)`
- `assignerAgentResponsable(Long dossierId, Long agentId)` (déjà existante mais retourne null)
- `getDossiersValidesDisponibles(...)` (pour la liste)

### Étape 3 : Ajouter les Endpoints

**Fichier** : `DossierController.java`

**Prompt à utiliser** : Voir `PROMPT_BACKEND_AFFECTATION_DOSSIERS.md` - PROMPT 3 et PROMPT 4

**Endpoints à créer** :
- `PUT /api/dossiers/{id}/affecter/recouvrement-amiable`
- `PUT /api/dossiers/{id}/affecter/recouvrement-juridique`
- `PUT /api/dossiers/{id}/cloturer`
- `GET /api/dossiers/valides-disponibles`

---

## 🎨 Implémentation Frontend

### Étape 1 : Mettre à Jour le Service

**Fichier** : `dossier.service.ts`

**Prompt à utiliser** : Voir `PROMPT_FRONTEND_AFFECTATION_DOSSIERS.md` - PROMPT 1

**Méthodes à ajouter** :
- `affecterAuRecouvrementAmiable(dossierId: number)`
- `affecterAuRecouvrementJuridique(dossierId: number)`
- `cloturerDossier(dossierId: number)`
- `getDossiersValidesDisponibles(params?: {...})`

### Étape 2 : Mettre à Jour le Composant

**Fichier** : `affectation-dossiers.component.ts` et `.html`

**Prompt à utiliser** : Voir `PROMPT_FRONTEND_AFFECTATION_DOSSIERS.md` - PROMPT 2

**Fonctionnalités à implémenter** :
- Chargement des dossiers validés disponibles
- Recherche avec debounce
- Filtres et tri
- Actions d'affectation (amiable, juridique, clôture)
- Dialogs de confirmation
- Notifications de succès/erreur

### Étape 3 : Créer le Dialog de Confirmation

**Fichier** : `confirmation-dialog.component.ts` et `.html`

**Prompt à utiliser** : Voir `PROMPT_FRONTEND_AFFECTATION_DOSSIERS.md` - PROMPT 3

---

## 📊 Structure des Données

### Dossier (Backend)

```java
- id: Long
- statut: Statut (EN_ATTENTE_VALIDATION, VALIDE, REJETE)
- valide: Boolean
- dossierStatus: DossierStatus (ENCOURSDETRAITEMENT, CLOTURE)
- agentResponsable: Utilisateur (le chef affecté)
- dateCloture: Date (rempli lors de la clôture)
```

### Dossier (Frontend)

```typescript
interface Dossier {
  id: number;
  numeroDossier: string;
  titre: string;
  montantCreance: number;
  statut: 'EN_ATTENTE_VALIDATION' | 'VALIDE' | 'REJETE';
  valide: boolean;
  dossierStatus: 'ENCOURSDETRAITEMENT' | 'CLOTURE';
  agentResponsable?: Utilisateur;
  dateCloture?: string;
  creancier: Creancier;
  debiteur: Debiteur;
  urgence: 'FAIBLE' | 'MOYENNE' | 'ELEVEE';
  dateCreation: string;
}
```

---

## 🔄 Flux d'Affectation

### Affectation au Recouvrement Amiable

```
1. Utilisateur sélectionne un dossier validé
2. Clique sur "Affecter au Recouvrement Amiable"
3. Dialog de confirmation s'affiche
4. Utilisateur confirme
5. Frontend appelle PUT /api/dossiers/{id}/affecter/recouvrement-amiable
6. Backend :
   - Vérifie que le dossier existe
   - Vérifie que statut = VALIDE et valide = true
   - Trouve le chef amiable (CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE)
   - Assigne le chef comme agentResponsable
   - Sauvegarde le dossier
7. Frontend reçoit le dossier mis à jour
8. Affiche un message de succès
9. Rafraîchit la liste
```

### Clôture de Dossier

```
1. Utilisateur sélectionne un dossier validé
2. Clique sur "Clôturer"
3. Dialog de confirmation avec avertissement s'affiche
4. Utilisateur confirme
5. Frontend appelle PUT /api/dossiers/{id}/cloturer
6. Backend :
   - Vérifie que le dossier existe
   - Vérifie que statut = VALIDE et valide = true
   - Met dossierStatus = CLOTURE
   - Met dateCloture = maintenant
   - Sauvegarde le dossier
7. Frontend reçoit le dossier mis à jour
8. Affiche un message de succès
9. Rafraîchit la liste (le dossier disparaît car il est clôturé)
```

---

## 📝 Endpoints API

### 1. Affecter au Recouvrement Amiable

```
PUT /api/dossiers/{id}/affecter/recouvrement-amiable
Headers: Authorization: Bearer {token}
Body: (vide)

Réponse 200 OK:
{
  "id": 1,
  "numeroDossier": "Dossier61",
  "statut": "VALIDE",
  "valide": true,
  "agentResponsable": {
    "id": 5,
    "nom": "Chef Amiable",
    "roleUtilisateur": "CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE"
  },
  ...
}

Réponse 400 Bad Request:
{
  "error": "Erreur d'affectation",
  "message": "Seuls les dossiers validés peuvent être affectés au recouvrement amiable",
  "timestamp": "..."
}
```

### 2. Affecter au Recouvrement Juridique

```
PUT /api/dossiers/{id}/affecter/recouvrement-juridique
Headers: Authorization: Bearer {token}
Body: (vide)

Réponse: Même format que pour amiable
```

### 3. Clôturer un Dossier

```
PUT /api/dossiers/{id}/cloturer
Headers: Authorization: Bearer {token}
Body: (vide)

Réponse 200 OK:
{
  "id": 1,
  "numeroDossier": "Dossier61",
  "statut": "VALIDE",
  "valide": true,
  "dossierStatus": "CLOTURE",
  "dateCloture": "2025-11-13T19:30:00",
  ...
}
```

### 4. Récupérer les Dossiers Validés Disponibles

```
GET /api/dossiers/valides-disponibles?page=0&size=10&sort=dateCreation&direction=DESC&search=Dossier61
Headers: Authorization: Bearer {token}

Réponse 200 OK:
{
  "content": [
    {
      "id": 1,
      "numeroDossier": "Dossier61",
      "titre": "Dossier Agent61",
      "montantCreance": 89900.00,
      "statut": "VALIDE",
      "valide": true,
      "dossierStatus": "ENCOURSDETRAITEMENT",
      ...
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "currentPage": 0,
  "size": 10
}
```

---

## ✅ Checklist Complète

### Backend

- [ ] Méthodes ajoutées dans `DossierService.java`
- [ ] Méthodes implémentées dans `DossierServiceImpl.java`
- [ ] Endpoints ajoutés dans `DossierController.java`
- [ ] Validation du statut VALIDE implémentée
- [ ] Recherche des chefs par rôle implémentée
- [ ] Clôture met à jour dossierStatus et dateCloture
- [ ] Gestion des erreurs avec messages clairs
- [ ] Endpoint pour récupérer les dossiers validés disponibles

### Frontend

- [ ] Méthodes ajoutées dans `DossierService`
- [ ] Composant charge les dossiers validés
- [ ] Recherche avec debounce implémentée
- [ ] Filtres et tri fonctionnent
- [ ] Boutons d'affectation fonctionnent
- [ ] Bouton de clôture fonctionne
- [ ] Dialogs de confirmation créés
- [ ] Messages de succès/erreur affichés
- [ ] Liste se rafraîchit après actions
- [ ] Pagination fonctionne

---

## 🧪 Tests

### Test Backend (Postman)

1. **Tester l'affectation amiable** :
   - PUT `/api/dossiers/1/affecter/recouvrement-amiable`
   - Vérifier que `agentResponsable` est le chef amiable

2. **Tester l'affectation juridique** :
   - PUT `/api/dossiers/1/affecter/recouvrement-juridique`
   - Vérifier que `agentResponsable` est le chef juridique

3. **Tester la clôture** :
   - PUT `/api/dossiers/1/cloturer`
   - Vérifier que `dossierStatus = CLOTURE` et `dateCloture` est rempli

4. **Tester la liste** :
   - GET `/api/dossiers/valides-disponibles`
   - Vérifier que seuls les dossiers VALIDE et non clôturés sont retournés

### Test Frontend

1. Ouvrir la page d'affectation
2. Vérifier que seuls les dossiers validés s'affichent
3. Tester la recherche
4. Tester les filtres et tri
5. Tester l'affectation amiable
6. Tester l'affectation juridique
7. Tester la clôture
8. Vérifier les messages de succès/erreur

---

## 📚 Documents de Référence

1. **`PROMPT_BACKEND_AFFECTATION_DOSSIERS.md`**
   - Tous les prompts pour l'implémentation backend
   - 4 prompts détaillés avec code

2. **`PROMPT_FRONTEND_AFFECTATION_DOSSIERS.md`**
   - Tous les prompts pour l'implémentation frontend
   - 3 prompts détaillés avec code

3. **Ce document** (`GUIDE_COMPLET_AFFECTATION_DOSSIERS.md`)
   - Vue d'ensemble complète
   - Règles métier
   - Structure des données
   - Flux d'affectation
   - Checklist

---

## ⚠️ Points d'Attention

1. **Vérification du statut** : Toujours vérifier que le dossier est VALIDE avant affectation/clôture
2. **Existence des chefs** : Vérifier qu'un chef existe avant d'affecter
3. **Clôture irréversible** : La clôture est définitive, bien confirmer avec l'utilisateur
4. **Permissions** : Seuls les chefs peuvent affecter/clôturer (vérifier les rôles)
5. **Pagination** : Implémenter la pagination côté serveur pour les performances

---

**Bon développement ! 🚀**


