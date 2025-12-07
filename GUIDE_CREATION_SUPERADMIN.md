# 🚀 Guide de Création d'un SuperAdmin

## 📋 Vue d'Ensemble

Ce guide explique comment créer un utilisateur **SUPER_ADMIN** dans le système lorsque la base de données est vide.

## 🎯 Méthode 1 : Script SQL (Recommandé pour les tests)

### Étape 1 : Exécuter le script SQL

```bash
# Option 1 : Via PowerShell
Get-Content create_superadmin.sql | mysql -u root -p carthage_creances

# Option 2 : Via MySQL directement
mysql -u root -p carthage_creances < create_superadmin.sql
```

### Étape 2 : Vérifier la création

```sql
SELECT * FROM utilisateur WHERE email = 'admin@carthage.com';
```

### Étape 3 : Se connecter

**Identifiants par défaut :**
- **Email:** `admin@carthage.com`
- **Mot de passe:** `admin123`
- **Rôle:** `SUPER_ADMIN`

---

## 🎯 Méthode 2 : Via l'API REST (Recommandé pour la production)

### Option A : Endpoint `/api/users`

**Requête POST :**
```http
POST http://localhost:8089/carthage-creance/api/users
Content-Type: application/json

{
  "nom": "Admin",
  "prenom": "System",
  "email": "admin@carthage.com",
  "motDePasse": "admin123",
  "roleUtilisateur": "SUPER_ADMIN"
}
```

**Réponse attendue :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "email": "admin@carthage.com",
  "nom": "Admin",
  "prenom": "System",
  "role": "SUPER_ADMIN",
  "errors": null
}
```

### Option B : Endpoint `/auth/register`

**Requête POST :**
```http
POST http://localhost:8089/carthage-creance/auth/register
Content-Type: application/json

{
  "firstName": "Admin",
  "lastName": "System",
  "email": "admin@carthage.com",
  "password": "admin123",
  "role": "SUPER_ADMIN"
}
```

**Réponse attendue :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "errors": null
}
```

---

## 🔐 Authentification après création

Une fois le SuperAdmin créé, vous pouvez vous authentifier :

```http
POST http://localhost:8089/carthage-creance/auth/authenticate
Content-Type: application/json

{
  "email": "admin@carthage.com",
  "password": "admin123"
}
```

**Réponse :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "email": "admin@carthage.com",
  "nom": "Admin",
  "prenom": "System",
  "role": "SUPER_ADMIN"
}
```

---

## 🛠️ Utilisation avec Postman

1. **Créer une nouvelle requête POST**
2. **URL :** `http://localhost:8089/carthage-creance/api/users`
3. **Headers :**
   - `Content-Type: application/json`
4. **Body (raw JSON) :**
   ```json
   {
     "nom": "Admin",
     "prenom": "System",
     "email": "admin@carthage.com",
     "motDePasse": "admin123",
     "roleUtilisateur": "SUPER_ADMIN"
   }
   ```
5. **Envoyer la requête**

---

## 📝 Notes Importantes

### Sécurité
- ⚠️ **Changez le mot de passe par défaut** après la première connexion
- ⚠️ Le mot de passe dans le script SQL est hashé avec **BCrypt**
- ⚠️ Pour changer le mot de passe, utilisez l'endpoint de mise à jour ou recréez l'utilisateur

### Rôles disponibles
- `SUPER_ADMIN` - Administrateur système
- `CHEF_DEPARTEMENT_DOSSIER` - Chef de département dossiers
- `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE` - Chef de département recouvrement amiable
- `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE` - Chef de département recouvrement juridique
- `CHEF_DEPARTEMENT_FINANCE` - Chef de département finance
- `AGENT_DOSSIER` - Agent dossiers
- `AGENT_RECOUVREMENT_AMIABLE` - Agent recouvrement amiable
- `AGENT_RECOUVREMENT_JURIDIQUE` - Agent recouvrement juridique
- `AGENT_FINANCE` - Agent finance

### Vérification
Pour vérifier que le SuperAdmin a été créé correctement :

```sql
SELECT 
    id,
    nom,
    prenom,
    email,
    role_utilisateur,
    actif,
    date_creation
FROM utilisateur 
WHERE role_utilisateur = 'SUPER_ADMIN';
```

---

## 🐛 Dépannage

### Erreur : "Un utilisateur avec cet email existe déjà"
- L'utilisateur existe déjà dans la base
- Supprimez-le d'abord ou utilisez un autre email

### Erreur : "Le rôle utilisateur est obligatoire"
- Assurez-vous que le champ `roleUtilisateur` est bien présent dans la requête JSON

### Erreur de connexion à la base de données
- Vérifiez que MySQL est démarré
- Vérifiez les paramètres de connexion dans `application.properties`

---

## ✅ Checklist de création

- [ ] Base de données `carthage_creances` existe
- [ ] Table `utilisateur` existe
- [ ] Script SQL exécuté OU requête API envoyée
- [ ] SuperAdmin créé avec succès
- [ ] Authentification fonctionne
- [ ] Token JWT reçu et valide

---

## 📞 Support

Si vous rencontrez des problèmes :
1. Vérifiez les logs de l'application Spring Boot
2. Vérifiez les logs MySQL
3. Vérifiez que le port 8089 est disponible
4. Vérifiez que la base de données est accessible


