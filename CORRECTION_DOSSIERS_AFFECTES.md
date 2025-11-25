# 🔧 Correction : Dossiers Affectés à un Agent

## Problèmes Identifiés

### 1. ❌ Erreur 400 : "La taille de page doit être entre 1 et 100"
**Cause :** Le frontend envoie `size=1000` ce qui dépasse la limite maximale de 100.

### 2. ❌ Les dossiers affectés ne s'affichent pas
**Cause :** La méthode `getDossiersByAgent` ne cherchait que dans la table de jointure `dossier_utilisateurs`, mais pas dans `agent_responsable_id` de la table `dossier`.

---

## ✅ Corrections Appliquées

### 1. Correction de la Méthode de Récupération

**Avant :**
```java
public List<Dossier> getDossiersByAgent(Long agentId) {
    return dossierRepository.findByUtilisateurId(agentId); // ❌ Ne cherche que dans dossier_utilisateurs
}
```

**Après :**
```java
public List<Dossier> getDossiersByAgent(Long agentId) {
    // ✅ Cherche dans agent_responsable_id ET dans dossier_utilisateurs
    return dossierRepository.findDossiersAffectesByAgent(agentId);
}
```

**Nouvelle méthode dans le Repository :**
```java
@Query("SELECT DISTINCT d FROM Dossier d " +
       "LEFT JOIN d.utilisateurs u " +
       "WHERE (d.agentResponsable.id = :agentId OR u.id = :agentId)")
List<Dossier> findDossiersAffectesByAgent(@Param("agentId") Long agentId);
```

Cette méthode cherche les dossiers dans **deux endroits** :
1. **`agent_responsable_id`** dans la table `dossier` (affectation directe)
2. **Table de jointure `dossier_utilisateurs`** (affectation via relation Many-to-Many)

### 2. Nouvel Endpoint avec Pagination

**Nouveau endpoint :** `GET /api/dossiers/agent/{agentId}/paginated`

**Paramètres :**
- `page` : Numéro de page (défaut: 0)
- `size` : Taille de la page (défaut: 10, **max: 100**)
- `sort` : Champ de tri (défaut: "dateCreation")

**Exemple :**
```bash
GET /api/dossiers/agent/50/paginated?page=0&size=10&sort=dateCreation
```

**Réponse :**
```json
{
  "content": [...],
  "totalElements": 5,
  "totalPages": 1,
  "currentPage": 0,
  "size": 10,
  "first": true,
  "last": true,
  "numberOfElements": 5
}
```

**Validation :**
- Si `size > 100`, retourne une erreur 400 : "La taille de page doit être entre 1 et 100"
- Si `size > 100` est envoyé, la taille est automatiquement limitée à 100

---

## 📋 Endpoints Disponibles

### 1. Liste Simple (Sans Pagination)
```
GET /api/dossiers/agent/{agentId}
```
Retourne tous les dossiers affectés à l'agent (sans pagination).

### 2. Liste avec Pagination (Recommandé)
```
GET /api/dossiers/agent/{agentId}/paginated?page=0&size=10&sort=dateCreation
```
Retourne les dossiers avec pagination (limite `size` à 100 max).

---

## 🔍 Vérification dans la Base de Données

### Vérifier les Affectations Directes (agent_responsable_id)

```sql
SELECT id, numero_dossier, titre, agent_responsable_id 
FROM dossier 
WHERE agent_responsable_id = 50;  -- Remplacer 50 par l'ID de l'agent
```

### Vérifier les Affectations via Table de Jointure

```sql
SELECT d.id, d.numero_dossier, d.titre, du.utilisateur_id 
FROM dossier d
JOIN dossier_utilisateurs du ON d.id = du.dossier_id
WHERE du.utilisateur_id = 50;  -- Remplacer 50 par l'ID de l'agent
```

### Vérifier Toutes les Affectations (Combinaison)

```sql
-- Dossiers où l'agent est responsable (agent_responsable_id)
SELECT id, numero_dossier, titre, 'agent_responsable' as type_affectation
FROM dossier 
WHERE agent_responsable_id = 50

UNION

-- Dossiers dans la table de jointure
SELECT d.id, d.numero_dossier, d.titre, 'dossier_utilisateurs' as type_affectation
FROM dossier d
JOIN dossier_utilisateurs du ON d.id = du.dossier_id
WHERE du.utilisateur_id = 50;
```

---

## 🧪 Test des Corrections

### 1. Tester l'Endpoint Simple

```bash
curl -X GET "http://localhost:8089/carthage-creance/api/dossiers/agent/50" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Résultat Attendu :**
- Liste JSON de tous les dossiers affectés à l'agent 50
- Inclut les dossiers avec `agent_responsable_id = 50`
- Inclut les dossiers dans `dossier_utilisateurs` avec `utilisateur_id = 50`

### 2. Tester l'Endpoint avec Pagination

```bash
curl -X GET "http://localhost:8089/carthage-creance/api/dossiers/agent/50/paginated?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Résultat Attendu :**
- Objet JSON avec `content`, `totalElements`, `totalPages`, etc.
- Maximum 10 dossiers par page (selon `size=10`)

### 3. Tester avec size > 100 (Doit Échouer)

```bash
curl -X GET "http://localhost:8089/carthage-creance/api/dossiers/agent/50/paginated?page=0&size=1000" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Résultat Attendu :**
- Status: `400 Bad Request`
- Message: `"La taille de page doit être entre 1 et 100"`

---

## 🔧 Corrections Frontend Nécessaires

### 1. Utiliser le Bon Endpoint

**❌ Ancien code (causait l'erreur 400) :**
```typescript
// Ne pas utiliser /api/dossiers avec size=1000
this.http.get(`/api/dossiers?page=0&size=1000`)
```

**✅ Nouveau code (recommandé) :**
```typescript
// Utiliser l'endpoint spécifique pour les dossiers de l'agent
this.http.get(`/api/dossiers/agent/${agentId}/paginated?page=0&size=10`)
```

### 2. Limiter la Taille de Page

**Dans le service frontend :**
```typescript
getDossiersAffectes(agentId: number, page: number = 0, size: number = 10): Observable<any> {
  // Limiter size à 100 maximum
  const limitedSize = Math.min(size, 100);
  
  return this.http.get(`${this.apiUrl}/dossiers/agent/${agentId}/paginated`, {
    params: {
      page: page.toString(),
      size: limitedSize.toString(),
      sort: 'dateCreation'
    }
  });
}
```

### 3. Gérer la Pagination

**Dans le composant :**
```typescript
loadDossiersAffectes() {
  this.dossierService.getDossiersAffectes(this.currentAgentId, this.currentPage, this.pageSize)
    .subscribe({
      next: (response) => {
        this.dossiers = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des dossiers:', error);
      }
    });
}
```

---

## 📝 Checklist de Vérification

- [ ] L'endpoint `/api/dossiers/agent/{agentId}` retourne les dossiers avec `agent_responsable_id`
- [ ] L'endpoint retourne aussi les dossiers dans `dossier_utilisateurs`
- [ ] L'endpoint `/api/dossiers/agent/{agentId}/paginated` fonctionne avec `size <= 100`
- [ ] L'endpoint rejette les requêtes avec `size > 100` (erreur 400)
- [ ] Le frontend utilise le bon endpoint avec `size <= 100`
- [ ] Les dossiers s'affichent correctement dans l'interface

---

## 🚨 Si les Dossiers Ne S'Affichent Toujours Pas

### Vérifier dans la Base de Données

1. **Vérifier que l'agent a des dossiers affectés :**
   ```sql
   SELECT COUNT(*) FROM dossier WHERE agent_responsable_id = 50;
   SELECT COUNT(*) FROM dossier_utilisateurs WHERE utilisateur_id = 50;
   ```

2. **Vérifier l'ID de l'agent connecté :**
   - Dans les logs backend, vérifier quel `agentId` est utilisé
   - Comparer avec les `agent_responsable_id` dans la table `dossier`

3. **Vérifier les relations :**
   - Les dossiers doivent avoir `agent_responsable_id` non NULL
   - Ou des entrées dans `dossier_utilisateurs`

### Vérifier les Logs Backend

Après avoir appelé l'endpoint, vérifier les logs :
- Y a-t-il des erreurs ?
- Combien de dossiers sont retournés ?
- L'agentId utilisé est-il correct ?

---

## 📞 Informations à Fournir pour le Débogage

Si le problème persiste, fournissez :

1. **Résultat de la requête SQL :**
   ```sql
   SELECT id, numero_dossier, agent_responsable_id 
   FROM dossier 
   WHERE agent_responsable_id = [ID_AGENT];
   ```

2. **Logs backend lors de l'appel :**
   - L'agentId utilisé
   - Le nombre de dossiers retournés
   - Les erreurs éventuelles

3. **Requête HTTP complète :**
   - URL appelée
   - Headers (surtout Authorization)
   - Réponse reçue

Avec ces informations, on pourra identifier précisément le problème.

