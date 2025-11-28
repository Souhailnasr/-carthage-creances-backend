# 🔐 Réinitialisation du Mot de Passe du Chef Dossier

## ⚠️ Important : Sécurité

**Les mots de passe sont stockés de manière cryptée (hashés avec BCrypt) et ne peuvent PAS être récupérés en clair.** C'est une bonne pratique de sécurité qui protège les données sensibles.

**Solution** : Réinitialiser le mot de passe avec un nouveau mot de passe que vous définissez.

---

## 🔍 Étape 1 : Trouver le Chef Dossier

### Option A : Par Email

**Endpoint** : `GET /api/users/chef-dossier/email/{email}`

**Exemple** :
```bash
GET http://localhost:8080/api/users/chef-dossier/email/chef.dossier@example.com
```

**Réponse 200 OK** :
```json
{
  "id": 46,
  "nom": "Chef",
  "prenom": "Dossier",
  "email": "chef.dossier@example.com",
  "roleUtilisateur": "CHEF_DEPARTEMENT_DOSSIER"
}
```

**Réponse 404 Not Found** :
```json
{
  "error": "Chef dossier non trouvé avec cet email"
}
```

**Réponse 400 Bad Request** :
```json
{
  "error": "L'utilisateur avec cet email n'est pas un chef dossier"
}
```

### Option B : Lister Tous les Chefs Dossier

**Endpoint** : `GET /api/users/chef-dossier/all`

**Exemple** :
```bash
GET http://localhost:8080/api/users/chef-dossier/all
```

**Réponse 200 OK** :
```json
[
  {
    "id": 46,
    "nom": "Chef",
    "prenom": "Dossier",
    "email": "chef.dossier@example.com",
    "roleUtilisateur": "CHEF_DEPARTEMENT_DOSSIER"
  },
  {
    "id": 47,
    "nom": "Autre",
    "prenom": "Chef",
    "email": "autre.chef@example.com",
    "roleUtilisateur": "CHEF_DEPARTEMENT_DOSSIER"
  }
]
```

### Option C : Par ID (si vous connaissez déjà l'ID)

**Endpoint** : `GET /api/users/{id}`

**Exemple** :
```bash
GET http://localhost:8080/api/users/46
```

---

## 🔄 Étape 2 : Réinitialiser le Mot de Passe

Une fois que vous avez l'ID du chef dossier, utilisez l'endpoint de réinitialisation.

### Endpoint : `PUT /api/users/{userId}/reset-password`

**URL** : `http://localhost:8080/api/users/{userId}/reset-password`

**Méthode** : `PUT`

**Headers** :
```
Content-Type: application/json
```

**Body (JSON)** :
```json
{
  "nouveauMotDePasse": "nouveauMotDePasse123"
}
```

**Paramètres de chemin** :
- `userId` (Long, requis) : ID du chef dossier (obtenu à l'étape 1)

**Réponse 200 OK** :
```json
{
  "message": "Mot de passe réinitialisé avec succès",
  "userId": 46,
  "email": "chef.dossier@example.com"
}
```

**Réponse 400 Bad Request** :
```json
{
  "error": "Le nouveau mot de passe est requis"
}
```

**Réponse 404 Not Found** :
```json
{
  "error": "Utilisateur non trouvé",
  "message": "Utilisateur non trouvé avec l'ID: 46"
}
```

---

## 📝 Exemples d'Utilisation

### Exemple 1 : Avec cURL

```bash
# 1. Trouver le chef dossier par email
curl -X GET "http://localhost:8080/api/users/chef-dossier/email/chef.dossier@example.com"

# 2. Réinitialiser le mot de passe (remplacer 46 par l'ID obtenu)
curl -X PUT "http://localhost:8080/api/users/46/reset-password" \
  -H "Content-Type: application/json" \
  -d '{
    "nouveauMotDePasse": "nouveauMotDePasse123"
  }'
```

### Exemple 2 : Avec Postman

1. **Trouver le chef dossier** :
   - Méthode : `GET`
   - URL : `http://localhost:8080/api/users/chef-dossier/email/chef.dossier@example.com`
   - Headers : Aucun
   - Body : Aucun

2. **Réinitialiser le mot de passe** :
   - Méthode : `PUT`
   - URL : `http://localhost:8080/api/users/46/reset-password` (remplacer 46 par l'ID obtenu)
   - Headers : `Content-Type: application/json`
   - Body (raw JSON) :
     ```json
     {
       "nouveauMotDePasse": "nouveauMotDePasse123"
     }
     ```

### Exemple 3 : Avec Angular/TypeScript

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Trouve le chef dossier par email
   */
  getChefDossierByEmail(email: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/users/chef-dossier/email/${email}`);
  }

  /**
   * Trouve tous les chefs dossier
   */
  getAllChefDossier(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users/chef-dossier/all`);
  }

  /**
   * Réinitialise le mot de passe d'un utilisateur
   */
  reinitialiserMotDePasse(userId: number, nouveauMotDePasse: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/users/${userId}/reset-password`, {
      nouveauMotDePasse: nouveauMotDePasse
    });
  }
}

// Utilisation dans un composant
export class ResetPasswordComponent {
  constructor(private utilisateurService: UtilisateurService) {}

  async resetChefDossierPassword(email: string, nouveauMotDePasse: string) {
    try {
      // 1. Trouver le chef dossier
      const chef = await this.utilisateurService.getChefDossierByEmail(email).toPromise();
      console.log('Chef dossier trouvé:', chef);

      // 2. Réinitialiser le mot de passe
      const result = await this.utilisateurService
        .reinitialiserMotDePasse(chef.id, nouveauMotDePasse)
        .toPromise();
      
      console.log('Mot de passe réinitialisé:', result);
      alert('Mot de passe réinitialisé avec succès !');
    } catch (error) {
      console.error('Erreur:', error);
      alert('Erreur lors de la réinitialisation du mot de passe');
    }
  }
}
```

### Exemple 4 : Script Python

```python
import requests

# Configuration
BASE_URL = "http://localhost:8080/api"
CHEF_EMAIL = "chef.dossier@example.com"
NOUVEAU_MOT_DE_PASSE = "nouveauMotDePasse123"

# 1. Trouver le chef dossier
response = requests.get(f"{BASE_URL}/users/chef-dossier/email/{CHEF_EMAIL}")
if response.status_code == 200:
    chef = response.json()
    chef_id = chef["id"]
    print(f"Chef dossier trouvé: {chef['nom']} {chef['prenom']} (ID: {chef_id})")
    
    # 2. Réinitialiser le mot de passe
    reset_response = requests.put(
        f"{BASE_URL}/users/{chef_id}/reset-password",
        json={"nouveauMotDePasse": NOUVEAU_MOT_DE_PASSE},
        headers={"Content-Type": "application/json"}
    )
    
    if reset_response.status_code == 200:
        print("✅ Mot de passe réinitialisé avec succès !")
        print(reset_response.json())
    else:
        print(f"❌ Erreur: {reset_response.status_code}")
        print(reset_response.json())
else:
    print(f"❌ Chef dossier non trouvé: {response.status_code}")
    print(response.json())
```

---

## 🔐 Sécurité et Bonnes Pratiques

### ✅ Recommandations

1. **Mot de passe fort** :
   - Minimum 8 caractères
   - Mélange de lettres majuscules et minuscules
   - Au moins un chiffre
   - Au moins un caractère spécial

2. **Communication sécurisée** :
   - Utilisez HTTPS en production
   - Ne partagez jamais le nouveau mot de passe par email non crypté
   - Communiquez le nouveau mot de passe de manière sécurisée (en personne, par téléphone, etc.)

3. **Après réinitialisation** :
   - Le chef dossier devra se connecter avec le nouveau mot de passe
   - Il est recommandé de changer le mot de passe après la première connexion

### ⚠️ Avertissements

- **Ne stockez jamais les mots de passe en clair** dans votre code ou vos fichiers
- **Ne partagez jamais les mots de passe** par des canaux non sécurisés
- **Limitez l'accès** à l'endpoint de réinitialisation (ajoutez une authentification si nécessaire)

---

## 📋 Checklist de Réinitialisation

- [ ] Trouver l'ID du chef dossier (par email ou liste)
- [ ] Définir un nouveau mot de passe fort
- [ ] Appeler l'endpoint `PUT /api/users/{userId}/reset-password`
- [ ] Vérifier la réponse (200 OK)
- [ ] Informer le chef dossier du nouveau mot de passe de manière sécurisée
- [ ] Tester la connexion avec le nouveau mot de passe

---

## 🐛 Dépannage

### Problème : "Chef dossier non trouvé avec cet email"
**Solution** : 
- Vérifier que l'email est correct
- Utiliser `GET /api/users/chef-dossier/all` pour lister tous les chefs dossier
- Vérifier que l'utilisateur a bien le rôle `CHEF_DEPARTEMENT_DOSSIER`

### Problème : "Le nouveau mot de passe est requis"
**Solution** : Vérifier que le body contient bien `{"nouveauMotDePasse": "..."}`

### Problème : "Utilisateur non trouvé"
**Solution** : Vérifier que l'ID de l'utilisateur est correct

### Problème : Le mot de passe ne fonctionne pas après réinitialisation
**Solution** :
- Vérifier que le mot de passe a bien été envoyé dans la requête
- Vérifier qu'il n'y a pas d'espaces avant/après le mot de passe
- Essayer de se connecter avec le nouveau mot de passe exactement tel qu'il a été défini

---

## 📚 Endpoints Disponibles

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/users/chef-dossier/email/{email}` | GET | Trouve le chef dossier par email |
| `/api/users/chef-dossier/all` | GET | Liste tous les chefs dossier |
| `/api/users/{userId}/reset-password` | PUT | Réinitialise le mot de passe d'un utilisateur |
| `/api/users/{id}` | GET | Récupère un utilisateur par ID |

---

## ✅ Résumé Rapide

1. **Trouver le chef dossier** :
   ```bash
   GET /api/users/chef-dossier/email/chef.dossier@example.com
   ```

2. **Réinitialiser le mot de passe** :
   ```bash
   PUT /api/users/46/reset-password
   {
     "nouveauMotDePasse": "nouveauMotDePasse123"
   }
   ```

3. **Tester la connexion** avec le nouveau mot de passe

---

## 🔄 Alternative : Mise à Jour via PUT /api/users/{id}

Vous pouvez aussi utiliser l'endpoint de mise à jour standard :

```bash
PUT /api/users/46
{
  "nom": "Chef",
  "prenom": "Dossier",
  "email": "chef.dossier@example.com",
  "motDePasse": "nouveauMotDePasse123"
}
```

**Note** : L'endpoint `PUT /api/users/{id}/reset-password` est plus spécifique et dédié à cette tâche, mais les deux fonctionnent.

