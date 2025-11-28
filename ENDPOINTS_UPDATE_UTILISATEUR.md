# 📝 Endpoints pour Modifier les Détails d'un Utilisateur

## 🔑 Endpoint Principal : Mise à Jour Complète

### `PUT /api/users/{id}`

**Description** : Met à jour les informations d'un utilisateur (nom, prénom, email, mot de passe, rôle).

**URL** : `http://localhost:8080/api/users/{id}`

**Méthode** : `PUT`

**Headers** :
```
Content-Type: application/json
Authorization: Bearer {token}  // Optionnel selon votre configuration
```

**Paramètres de chemin** :
- `id` (Long, requis) : ID de l'utilisateur à modifier

**Body (JSON)** :
```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "motDePasse": "nouveauMotDePasse123",  // Optionnel : seulement si changement de mot de passe
  "roleUtilisateur": "AGENT_DOSSIER"      // Optionnel : seulement si changement de rôle
}
```

**Champs modifiables** :
- ✅ `nom` : Nom de l'utilisateur
- ✅ `prenom` : Prénom de l'utilisateur
- ✅ `email` : Email de l'utilisateur (avec vérification d'unicité)
- ✅ `motDePasse` : Mot de passe (seulement si fourni et non vide)
- ✅ `roleUtilisateur` : Rôle de l'utilisateur (seulement si fourni)

**Champs NON modifiables via cet endpoint** :
- ❌ `id` : Ne peut pas être modifié
- ❌ `dateCreation` : Ne peut pas être modifié
- ❌ `derniereConnexion` : Mis à jour automatiquement lors de la connexion
- ❌ `derniereDeconnexion` : Mis à jour automatiquement lors de la déconnexion
- ❌ `actif` : Calculé automatiquement (voir endpoint dédié)
- ❌ `chefCreateur` : Non modifiable via cet endpoint (à ajouter si nécessaire)

**Réponses** :

**200 OK** - Utilisateur mis à jour avec succès
```json
{
  "id": 50,
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "roleUtilisateur": "AGENT_DOSSIER",
  "actif": true,
  "dateCreation": "2024-01-10T08:00:00",
  "derniereConnexion": "2024-01-15T10:30:00",
  "derniereDeconnexion": null
}
```

**400 Bad Request** - Erreur de validation
```json
"Un utilisateur avec cet email existe déjà."
```

**404 Not Found** - Utilisateur non trouvé
```json
null
```

**500 Internal Server Error** - Erreur serveur
```json
"Erreur lors de la mise à jour de l'utilisateur: {message}"
```

**Exemple d'utilisation (cURL)** :
```bash
curl -X PUT "http://localhost:8080/api/users/50" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@example.com"
  }'
```

**Exemple d'utilisation (Angular/TypeScript)** :
```typescript
updateUtilisateur(id: number, utilisateur: Partial<Utilisateur>): Observable<Utilisateur> {
  return this.http.put<Utilisateur>(`${this.apiUrl}/users/${id}`, utilisateur);
}

// Utilisation
this.utilisateurService.updateUtilisateur(50, {
  nom: "Dupont",
  prenom: "Jean",
  email: "jean.dupont@example.com"
}).subscribe({
  next: (user) => console.log('Utilisateur mis à jour:', user),
  error: (error) => console.error('Erreur:', error)
});
```

---

## 🔄 Endpoint : Mise à Jour du Statut Actif

### `PUT /api/users/{userId}/statut-actif`

**Description** : Met à jour manuellement le statut actif d'un utilisateur en fonction de ses dates de connexion/déconnexion.

**URL** : `http://localhost:8080/api/users/{userId}/statut-actif`

**Méthode** : `PUT`

**Paramètres de chemin** :
- `userId` (Long, requis) : ID de l'utilisateur

**Body** : Aucun (le statut est calculé automatiquement)

**Réponse 200 OK** :
```json
{
  "message": "Statut actif mis à jour",
  "userId": 50,
  "email": "jean.dupont@example.com",
  "actif": true,
  "derniere_connexion": "2024-01-15T10:30:00",
  "derniere_deconnexion": null
}
```

**Exemple d'utilisation** :
```typescript
mettreAJourStatutActif(userId: number): Observable<any> {
  return this.http.put(`${this.apiUrl}/users/${userId}/statut-actif`, {});
}
```

---

## 🔄 Endpoint : Mise à Jour du Statut Actif de Tous les Utilisateurs

### `PUT /api/users/statut-actif/tous`

**Description** : Met à jour le statut actif de tous les utilisateurs (utile pour un job de maintenance).

**URL** : `http://localhost:8080/api/users/statut-actif/tous`

**Méthode** : `PUT`

**Body** : Aucun

**Réponse 200 OK** :
```json
{
  "message": "Statut actif mis à jour pour tous les utilisateurs",
  "nombreUtilisateursMisAJour": 15
}
```

---

## 📋 Récapitulatif des Endpoints

| Endpoint | Méthode | Description | Champs Modifiables |
|----------|---------|-------------|-------------------|
| `/api/users/{id}` | PUT | Mise à jour complète | nom, prenom, email, motDePasse, roleUtilisateur |
| `/api/users/{userId}/statut-actif` | PUT | Mise à jour statut actif | actif (calculé automatiquement) |
| `/api/users/statut-actif/tous` | PUT | Mise à jour statut actif tous | actif (pour tous les utilisateurs) |

---

## ⚠️ Notes Importantes

### 1. **Mot de Passe**
- Le mot de passe n'est mis à jour **que si** il est fourni et non vide dans le body
- Si vous ne voulez pas changer le mot de passe, ne l'incluez pas dans le body
- Le mot de passe est automatiquement encodé par le backend

### 2. **Email**
- L'email est vérifié pour l'unicité avant la mise à jour
- Si l'email existe déjà pour un autre utilisateur, une erreur 400 est retournée

### 3. **Rôle**
- Le rôle peut être modifié via cet endpoint
- Une notification est envoyée si le rôle change

### 4. **Champs Non Modifiables**
- `id`, `dateCreation` : Ne peuvent jamais être modifiés
- `derniereConnexion`, `derniereDeconnexion` : Mis à jour automatiquement
- `actif` : Calculé automatiquement (utiliser l'endpoint dédié pour forcer la mise à jour)
- `chefCreateur` : **Non modifiable via cet endpoint** (voir section suivante)

---

## 🔧 Amélioration Suggérée : Mise à Jour du Chef Créateur

Si vous souhaitez permettre la modification du `chefCreateur` d'un agent, vous devez :

1. **Modifier le service** `UtilisateurServiceImpl.updateUtilisateur()` :
```java
// Ajouter dans updateUtilisateur()
if (utilisateurDetails.getChefCreateur() != null && utilisateurDetails.getChefCreateur().getId() != null) {
    Utilisateur nouveauChef = utilisateurRepository.findById(utilisateurDetails.getChefCreateur().getId())
            .orElseThrow(() -> new IllegalArgumentException("Chef introuvable"));
    
    if (!(estChef(nouveauChef.getRoleUtilisateur()) || nouveauChef.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN)) {
        throw new IllegalArgumentException("L'utilisateur n'est pas un chef autorisé");
    }
    
    existingUtilisateur.setChefCreateur(nouveauChef);
} else if (utilisateurDetails.getChefId() != null) {
    Utilisateur nouveauChef = utilisateurRepository.findById(utilisateurDetails.getChefId())
            .orElseThrow(() -> new IllegalArgumentException("Chef introuvable"));
    
    if (!(estChef(nouveauChef.getRoleUtilisateur()) || nouveauChef.getRoleUtilisateur() == RoleUtilisateur.SUPER_ADMIN)) {
        throw new IllegalArgumentException("L'utilisateur n'est pas un chef autorisé");
    }
    
    existingUtilisateur.setChefCreateur(nouveauChef);
}
```

2. **Body JSON pour changer le chef** :
```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "chefId": 46  // Nouveau chef créateur
}
```

---

## 📝 Exemples Complets

### Exemple 1 : Mise à jour du nom et prénom uniquement
```json
PUT /api/users/50
{
  "nom": "Martin",
  "prenom": "Pierre"
}
```

### Exemple 2 : Mise à jour de l'email
```json
PUT /api/users/50
{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "nouveau.email@example.com"
}
```

### Exemple 3 : Changement de mot de passe
```json
PUT /api/users/50
{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "motDePasse": "nouveauMotDePasse123"
}
```

### Exemple 4 : Changement de rôle
```json
PUT /api/users/50
{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "roleUtilisateur": "CHEF_DEPARTEMENT_DOSSIER"
}
```

### Exemple 5 : Mise à jour complète
```json
PUT /api/users/50
{
  "nom": "Martin",
  "prenom": "Pierre",
  "email": "pierre.martin@example.com",
  "motDePasse": "nouveauMotDePasse123",
  "roleUtilisateur": "AGENT_FINANCE"
}
```

---

## 🎯 Utilisation Frontend (Angular)

### Service TypeScript
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Utilisateur } from '../models/utilisateur.model';

@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Met à jour un utilisateur
   */
  updateUtilisateur(id: number, utilisateur: Partial<Utilisateur>): Observable<Utilisateur> {
    return this.http.put<Utilisateur>(`${this.apiUrl}/users/${id}`, utilisateur);
  }

  /**
   * Met à jour le statut actif d'un utilisateur
   */
  mettreAJourStatutActif(userId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/users/${userId}/statut-actif`, {});
  }
}
```

### Composant TypeScript
```typescript
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UtilisateurService } from '../services/utilisateur.service';
import { Utilisateur } from '../models/utilisateur.model';

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html'
})
export class EditUserComponent {
  userForm: FormGroup;
  userId: number;

  constructor(
    private fb: FormBuilder,
    private utilisateurService: UtilisateurService
  ) {
    this.userForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      motDePasse: [''], // Optionnel
      roleUtilisateur: ['']
    });
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      const formData = this.userForm.value;
      
      // Ne pas envoyer le mot de passe s'il est vide
      if (!formData.motDePasse) {
        delete formData.motDePasse;
      }

      this.utilisateurService.updateUtilisateur(this.userId, formData).subscribe({
        next: (user) => {
          console.log('Utilisateur mis à jour:', user);
          alert('Utilisateur mis à jour avec succès');
        },
        error: (error) => {
          console.error('Erreur:', error);
          alert('Erreur lors de la mise à jour: ' + (error.error || error.message));
        }
      });
    }
  }
}
```

---

## ✅ Checklist de Mise à Jour

- [ ] Vérifier que l'utilisateur existe avant la mise à jour
- [ ] Valider les données (email valide, champs requis)
- [ ] Gérer les erreurs (email déjà utilisé, utilisateur non trouvé)
- [ ] Ne pas envoyer le mot de passe s'il n'est pas modifié
- [ ] Afficher un message de succès après la mise à jour
- [ ] Rafraîchir les données affichées après la mise à jour

---

## 🔍 Dépannage

### Problème : "Un utilisateur avec cet email existe déjà"
**Solution** : Vérifier que l'email n'est pas déjà utilisé par un autre utilisateur

### Problème : 404 Not Found
**Solution** : Vérifier que l'ID de l'utilisateur est correct

### Problème : Le mot de passe n'est pas mis à jour
**Solution** : Vérifier que le mot de passe est bien fourni dans le body et non vide

### Problème : Le rôle n'est pas mis à jour
**Solution** : Vérifier que le `roleUtilisateur` est fourni dans le body avec une valeur valide

