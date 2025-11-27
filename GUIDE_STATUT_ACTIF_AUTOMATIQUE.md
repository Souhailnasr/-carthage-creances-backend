# 🔄 Guide : Statut Actif Automatique

## Problème Identifié

Le champ `actif` dans la table `utilisateur` était toujours égal à `1` (true) par défaut et n'était jamais mis à jour automatiquement selon les dates de connexion/déconnexion.

## ✅ Solution Implémentée

### 1. Méthode de Calcul Automatique dans l'Entité

**Fichier :** `src/main/java/projet/carthagecreance_backend/Entity/Utilisateur.java`

**Nouvelles méthodes ajoutées :**

```java
/**
 * Calcule automatiquement si l'utilisateur est actif (connecté) ou inactif (déconnecté)
 * basé sur les dates de connexion et déconnexion.
 * 
 * Logique :
 * - Actif (1) si : derniere_connexion existe ET (derniere_deconnexion est NULL OU derniere_connexion > derniere_deconnexion)
 * - Inactif (0) si : derniere_deconnexion existe ET derniere_deconnexion >= derniere_connexion
 * 
 * @return true si l'utilisateur est actif (connecté), false sinon
 */
public boolean calculerStatutActif() {
    // Si aucune date de connexion, considérer comme inactif
    if (derniereConnexion == null) {
        return false;
    }
    
    // Si pas de date de déconnexion, l'utilisateur est actif (connecté)
    if (derniereDeconnexion == null) {
        return true;
    }
    
    // Si derniere_connexion est plus récente que derniere_deconnexion, l'utilisateur est actif
    // Sinon, il est inactif (s'est déconnecté après sa dernière connexion)
    return derniereConnexion.isAfter(derniereDeconnexion);
}

/**
 * Met à jour le champ actif en fonction des dates de connexion/déconnexion
 */
public void mettreAJourStatutActif() {
    this.actif = calculerStatutActif();
}
```

### 2. Mise à Jour Automatique lors de la Connexion

**Fichier :** `src/main/java/projet/carthagecreance_backend/SecurityServices/AuthenticationService.java`

**Modification :**
```java
user.setDerniereConnexion(LocalDateTime.now());
user.setDerniereDeconnexion(null);
// Mettre à jour le statut actif : utilisateur connecté = actif
user.mettreAJourStatutActif();
var userWithAudit = repository.save(user);
```

**Résultat :** Lors de la connexion, `actif` est automatiquement mis à `1` (true).

### 3. Mise à Jour Automatique lors de la Déconnexion

**Fichier :** `src/main/java/projet/carthagecreance_backend/SecurityServices/LogoutService.java`

**Modification :**
```java
user.setDerniereDeconnexion(now);
// Mettre à jour le statut actif : utilisateur déconnecté = inactif
user.mettreAJourStatutActif();
logger.info("Logout: Statut actif mis à jour: {}", user.getActif());
Utilisateur savedUser = utilisateurRepository.saveAndFlush(user);
```

**Résultat :** Lors de la déconnexion, `actif` est automatiquement mis à `0` (false).

### 4. Méthodes dans le Service

**Fichier :** `src/main/java/projet/carthagecreance_backend/Service/UtilisateurService.java`

**Nouvelles méthodes :**
```java
/**
 * Met à jour le statut actif d'un utilisateur
 */
Utilisateur mettreAJourStatutActif(Long userId);

/**
 * Met à jour le statut actif de tous les utilisateurs
 */
int mettreAJourStatutActifTous();
```

### 5. Endpoints REST

**Fichier :** `src/main/java/projet/carthagecreance_backend/Controller/UtilisateurController.java`

**Nouveaux endpoints :**

#### Mettre à jour le statut actif d'un utilisateur
```
PUT /api/users/{userId}/statut-actif
```

**Exemple :**
```bash
curl -X PUT "http://localhost:8089/carthage-creance/api/users/50/statut-actif"
```

**Réponse :**
```json
{
  "message": "Statut actif mis à jour",
  "userId": 50,
  "email": "user@example.com",
  "actif": true,
  "derniere_connexion": "2025-11-25T19:30:00",
  "derniere_deconnexion": "NULL"
}
```

#### Mettre à jour le statut actif de tous les utilisateurs
```
PUT /api/users/statut-actif/tous
```

**Exemple :**
```bash
curl -X PUT "http://localhost:8089/carthage-creance/api/users/statut-actif/tous"
```

**Réponse :**
```json
{
  "message": "Statut actif mis à jour pour tous les utilisateurs",
  "nombreUtilisateursMisAJour": 15
}
```

### 6. Filtrage des Agents Actifs

**Fichier :** `src/main/java/projet/carthagecreance_backend/Service/Impl/UtilisateurServiceImpl.java`

**Modification de `getAgentsActifs()` :**
```java
@Override
public List<Utilisateur> getAgentsActifs() {
    return utilisateurRepository.findAll().stream()
            .filter(u -> estAgent(u.getRoleUtilisateur()))
            .filter(u -> u.getEmail() != null && !u.getEmail().isEmpty())
            .filter(u -> {
                // Mettre à jour le statut actif avant de filtrer
                u.mettreAJourStatutActif();
                return u.getActif() != null && u.getActif();
            })
            .collect(Collectors.toList());
}
```

**Résultat :** L'endpoint `GET /api/users/agents/actifs` retourne uniquement les agents avec `actif = 1`.

---

## 📋 Logique de Calcul

### Règles de Calcul

1. **Si `derniere_connexion` est NULL :**
   - → `actif = 0` (inactif)
   - L'utilisateur ne s'est jamais connecté

2. **Si `derniere_connexion` existe ET `derniere_deconnexion` est NULL :**
   - → `actif = 1` (actif)
   - L'utilisateur est connecté (pas encore déconnecté)

3. **Si `derniere_connexion` existe ET `derniere_deconnexion` existe :**
   - Si `derniere_connexion > derniere_deconnexion` → `actif = 1` (actif)
   - Si `derniere_deconnexion >= derniere_connexion` → `actif = 0` (inactif)

### Exemples

| derniere_connexion | derniere_deconnexion | actif | Explication |
|-------------------|---------------------|-------|-------------|
| NULL | NULL | 0 | Jamais connecté |
| 2025-11-25 10:00 | NULL | 1 | Connecté, pas encore déconnecté |
| 2025-11-25 10:00 | 2025-11-25 09:00 | 1 | Dernière connexion après dernière déconnexion (connecté) |
| 2025-11-25 10:00 | 2025-11-25 11:00 | 0 | Dernière déconnexion après dernière connexion (déconnecté) |
| 2025-11-25 10:00 | 2025-11-25 10:00 | 0 | Même date/heure, considéré comme déconnecté |

---

## 🧪 Tests

### Test 1 : Connexion

1. **Se connecter :**
   ```bash
   POST /auth/authenticate
   Body: { "email": "user@example.com", "password": "password" }
   ```

2. **Vérifier dans la base :**
   ```sql
   SELECT id, email, actif, derniere_connexion, derniere_deconnexion 
   FROM utilisateur 
   WHERE email = 'user@example.com';
   ```

3. **Résultat attendu :**
   - `actif = 1` (true)
   - `derniere_connexion` = date/heure actuelle
   - `derniere_deconnexion` = NULL

### Test 2 : Déconnexion

1. **Se déconnecter :**
   ```bash
   POST /auth/logout
   Headers: Authorization: Bearer {token}
   ```

2. **Vérifier dans la base :**
   ```sql
   SELECT id, email, actif, derniere_connexion, derniere_deconnexion 
   FROM utilisateur 
   WHERE email = 'user@example.com';
   ```

3. **Résultat attendu :**
   - `actif = 0` (false)
   - `derniere_connexion` = date/heure de connexion
   - `derniere_deconnexion` = date/heure actuelle

### Test 3 : Mise à Jour Manuelle

1. **Mettre à jour le statut d'un utilisateur :**
   ```bash
   PUT /api/users/50/statut-actif
   ```

2. **Vérifier la réponse :**
   - `actif` doit être calculé selon les dates
   - Les dates doivent être affichées dans la réponse

### Test 4 : Mise à Jour de Tous les Utilisateurs

1. **Mettre à jour tous les utilisateurs :**
   ```bash
   PUT /api/users/statut-actif/tous
   ```

2. **Vérifier la réponse :**
   - `nombreUtilisateursMisAJour` doit indiquer combien d'utilisateurs ont été mis à jour

---

## 🔄 Scheduler (Optionnel)

Pour maintenir le statut actif à jour automatiquement, vous pouvez créer un scheduler :

```java
@Component
public class StatutActifScheduler {
    
    @Autowired
    private UtilisateurService utilisateurService;
    
    /**
     * Met à jour le statut actif de tous les utilisateurs toutes les heures
     */
    @Scheduled(fixedRate = 3600000) // 1 heure en millisecondes
    public void mettreAJourStatutActifPeriodique() {
        int count = utilisateurService.mettreAJourStatutActifTous();
        logger.info("Statut actif mis à jour pour {} utilisateurs", count);
    }
}
```

---

## 📝 Checklist de Vérification

- [ ] Le champ `actif` est mis à jour automatiquement lors de la connexion
- [ ] Le champ `actif` est mis à jour automatiquement lors de la déconnexion
- [ ] La méthode `calculerStatutActif()` fonctionne correctement
- [ ] L'endpoint `PUT /api/users/{userId}/statut-actif` fonctionne
- [ ] L'endpoint `PUT /api/users/statut-actif/tous` fonctionne
- [ ] L'endpoint `GET /api/users/agents/actifs` retourne uniquement les agents actifs
- [ ] Les tests confirment que `actif` change selon les dates

---

## 🚨 Notes Importantes

1. **Mise à jour automatique :** Le statut `actif` est maintenant mis à jour automatiquement lors de chaque connexion/déconnexion.

2. **Calcul en temps réel :** La méthode `calculerStatutActif()` calcule le statut en temps réel basé sur les dates, donc même si le champ `actif` n'est pas à jour dans la base, le calcul sera correct.

3. **Performance :** Pour de grandes bases de données, utilisez le scheduler pour mettre à jour périodiquement plutôt que de calculer à chaque requête.

4. **Compatibilité :** Les utilisateurs existants avec `actif = 1` mais sans dates de connexion seront considérés comme inactifs lors du calcul.

---

## 🔧 Correction des Données Existantes

Pour corriger les données existantes, exécutez :

```bash
PUT /api/users/statut-actif/tous
```

Cela mettra à jour le statut `actif` de tous les utilisateurs selon leurs dates de connexion/déconnexion.

---

Le statut `actif` est maintenant calculé automatiquement et mis à jour lors de chaque connexion/déconnexion ! ✅

