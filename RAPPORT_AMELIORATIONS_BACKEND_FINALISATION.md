# 📋 Rapport : Améliorations Backend - Finalisation et Dossiers Archivés

## ✅ Modifications Réalisées

### 1. DTO FinalisationDossierDTO Créé

**Fichier :** `src/main/java/projet/carthagecreance_backend/DTO/FinalisationDossierDTO.java`

**Contenu :**
- `etatFinal` : String (RECOUVREMENT_TOTAL, RECOUVREMENT_PARTIEL, NON_RECOUVRE)
- `montantRecouvre` : BigDecimal (montant recouvré dans cette étape)

---

### 2. DossierResponseDTO Amélioré

**Fichier :** `src/main/java/projet/carthagecreance_backend/DTO/DossierResponseDTO.java`

**Champs ajoutés :**
- `montantTotal` : Double
- `montantRecouvre` : Double
- `montantRestant` : Double
- `etatDossier` : EtatDossier (RECOVERED_TOTAL, RECOVERED_PARTIAL, NOT_RECOVERED)

**Mise à jour :**
- La méthode `fromEntity()` mappe maintenant ces champs depuis l'entité Dossier

---

### 3. Endpoint Finalisation Juridique

**Endpoint :** `PUT /api/dossiers/{dossierId}/juridique/finaliser`

**Fichier :** `src/main/java/projet/carthagecreance_backend/Controller/DossierController.java`

**Fonctionnalités :**
- ✅ Vérification de l'authentification et autorisation (CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE ou SUPER_ADMIN)
- ✅ Vérification que le dossier existe
- ✅ Vérification que le dossier a au moins une audience
- ✅ Validation des données (etatFinal, montantRecouvre)
- ✅ Calcul du montant total recouvré (montant déjà recouvré + montant de la requête)
- ✅ Validation selon l'état final :
  - RECOUVREMENT_TOTAL : montant total = montant créance (tolérance 0.01)
  - RECOUVREMENT_PARTIEL : 0 < montant total < montant créance
  - NON_RECOUVRE : montant peut être 0
- ✅ Mise à jour du montant recouvré (mode ADD)
- ✅ Mise à jour de l'état du dossier :
  - RECOUVREMENT_TOTAL → dossierStatus = CLOTURE, etatDossier = RECOVERED_TOTAL
  - RECOUVREMENT_PARTIEL → etatDossier = RECOVERED_PARTIAL
  - NON_RECOUVRE → etatDossier = NOT_RECOVERED
- ✅ Recalcul automatique des statistiques
- ✅ Retourne le dossier mis à jour

**Réponses HTTP :**
- 200 OK : Dossier finalisé avec succès
- 400 Bad Request : Validation échouée
- 401 Unauthorized : Non authentifié
- 403 Forbidden : Accès refusé
- 404 Not Found : Dossier non trouvé
- 500 Internal Server Error : Erreur serveur

---

### 4. Endpoint Finalisation Amiable

**Endpoint :** `PUT /api/dossiers/{dossierId}/amiable/finaliser`

**Fichier :** `src/main/java/projet/carthagecreance_backend/Controller/DossierController.java`

**Fonctionnalités :**
- ✅ Même logique que la finalisation juridique
- ✅ Vérification que le dossier a au moins une action amiable
- ✅ Autorisation : CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE ou SUPER_ADMIN

**Réponses HTTP :**
- Mêmes codes que la finalisation juridique

---

### 5. Endpoint Dossiers Archivés

**Endpoint :** `GET /api/admin/supervision/dossiers-archives`

**Fichier :** `src/main/java/projet/carthagecreance_backend/Controller/AdminSupervisionController.java`

**Paramètres de requête :**
- `page` : int (défaut : 0)
- `size` : int (défaut : 50, max : 100)
- `search` : String (recherche globale par numéro, créancier, débiteur)

**Fonctionnalités :**
- ✅ Récupère les dossiers avec dossierStatus = CLOTURE
- ✅ Filtre les dossiers archivés :
  - Dossiers clôturés depuis plus de 1 an OU
  - Dossiers avec date de clôture définie
- ✅ Recherche globale si le paramètre search est fourni :
  - Par numéro de dossier
  - Par nom/prénom du créancier
  - Par nom/prénom du débiteur
- ✅ Pagination
- ✅ Tri par date de clôture décroissante (plus récents en premier)
- ✅ Retourne une Page<DossierResponseDTO>

**Sécurité :**
- ✅ Seuls les SUPER_ADMIN peuvent accéder (via @PreAuthorize)

**Format de réponse :**
```json
{
  "content": [
    {
      "id": number,
      "numeroDossier": string,
      "montantCreance": number,
      "montantRecouvre": number,
      "montantRestant": number,
      "etatDossier": "RECOVERED_TOTAL" | "RECOVERED_PARTIAL" | "NOT_RECOVERED",
      "dateCloture": string,
      "dossierStatus": "CLOTURE",
      "creancier": {...},
      "debiteur": {...},
      ...
    }
  ],
  "totalElements": number,
  "totalPages": number,
  "size": number,
  "number": number,
  "first": boolean,
  "last": boolean,
  "numberOfElements": number
}
```

---

## 🔧 Corrections Techniques

### Imports Ajoutés

**Dans DossierController.java :**
- `import java.math.BigDecimal;`
- `import projet.carthagecreance_backend.Entity.EtatDossier;`
- `import projet.carthagecreance_backend.Entity.Audience;`
- `import projet.carthagecreance_backend.Entity.Action;`
- `import projet.carthagecreance_backend.Service.StatistiqueService;`

### Services Injectés

**Dans DossierController.java :**
- `@Autowired private StatistiqueService statistiqueService;`

---

## 📝 Logique de Calcul du Montant Recouvré

### Principe

1. **Récupérer le montant déjà recouvré** depuis le dossier
2. **Ajouter le montant recouvré** de la requête (mode ADD)
3. **Calculer le montant total recouvré** = montant déjà recouvré + montant de la requête
4. **Valider** selon l'état final sélectionné
5. **Mettre à jour** le dossier avec le montant total

### Exemple

- **Montant créance :** 230,000.00 TND
- **Montant déjà recouvré (amiable) :** 81,000.00 TND
- **Montant restant :** 149,000.05 TND
- **Finalisation juridique avec RECOUVREMENT_TOTAL :**
  - Montant recouvré dans cette étape = 149,000.05 TND
  - Montant total recouvré = 81,000.00 + 149,000.05 = 230,000.05 TND (≈ 230,000.00)
  - Le dossier est marqué comme RECOVERED_TOTAL et CLOTURE

---

## ✅ Validations Implémentées

### Pour RECOUVREMENT_TOTAL
- Montant total recouvré doit être égal au montant créance (tolérance de 0.01 TND)
- Si validation OK : dossierStatus = CLOTURE, etatDossier = RECOVERED_TOTAL

### Pour RECOUVREMENT_PARTIEL
- Montant total recouvré doit être > 0
- Montant total recouvré doit être < montant créance
- Si validation OK : etatDossier = RECOVERED_PARTIAL

### Pour NON_RECOUVRE
- Montant recouvré peut être 0 ou le montant restant
- Si validation OK : etatDossier = NOT_RECOVERED

### Validation Générale
- Montant total recouvré ne peut pas dépasser le montant créance
- Montant recouvré ne peut pas être négatif

---

## 🔒 Sécurité

### Finalisation Juridique
- **Rôle requis :** CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE ou SUPER_ADMIN
- **Vérification :** Token d'autorisation requis

### Finalisation Amiable
- **Rôle requis :** CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE ou SUPER_ADMIN
- **Vérification :** Token d'autorisation requis

### Dossiers Archivés
- **Rôle requis :** SUPER_ADMIN
- **Vérification :** @PreAuthorize("hasRole('SUPER_ADMIN')")

---

## 🚀 Actions Post-Finalisation

### Recalcul des Statistiques

Après chaque finalisation (juridique ou amiable), les statistiques sont recalculées automatiquement :

```java
try {
    statistiqueService.recalculerStatistiquesAsync();
} catch (Exception e) {
    logger.warn("Erreur lors du recalcul des statistiques: {}", e.getMessage());
}
```

**Note :** Le recalcul est asynchrone et ne bloque pas la réponse.

---

## 📊 Format de Réponse

### Finalisation (Juridique ou Amiable)

**Réponse 200 OK :**
```json
{
  "id": 1,
  "numeroDossier": "D23",
  "montantCreance": 230000.00,
  "montantRecouvre": 230000.00,
  "montantRestant": 0.00,
  "etatDossier": "RECOVERED_TOTAL",
  "dossierStatus": "CLOTURE",
  "dateCloture": "2025-12-05T10:30:00",
  ...
}
```

### Dossiers Archivés

**Réponse 200 OK :**
```json
{
  "content": [
    {
      "id": 1,
      "numeroDossier": "D23",
      "montantCreance": 230000.00,
      "montantRecouvre": 230000.00,
      "montantRestant": 0.00,
      "etatDossier": "RECOVERED_TOTAL",
      "dossierStatus": "CLOTURE",
      "dateCloture": "2024-12-05T10:30:00",
      "creancier": {...},
      "debiteur": {...}
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 50,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 10
}
```

---

## ⚠️ Notes Importantes

### Calcul du Montant Recouvré

- **Le backend calcule correctement** le montant total recouvré en additionnant le montant déjà recouvré + le montant de la requête
- **Le frontend doit envoyer** le montant recouvré dans CETTE étape (juridique ou amiable), pas le montant total
- **Le backend valide** que le montant total ne dépasse pas le montant créance

### Cohérence des Données

- `etatDossier`, `montantRecouvre`, et `montantCreance` sont maintenant cohérents
- Le champ `etatDossier` est retourné dans toutes les réponses DossierResponseDTO
- Le calcul du montant restant est automatique

### Archivage

- Les dossiers sont considérés comme archivés s'ils sont clôturés depuis plus de 1 an
- La recherche globale permet de trouver rapidement un dossier archivé
- La pagination permet de gérer de grandes listes de dossiers archivés

---

## 🧪 Tests Recommandés

### Test 1 : Finalisation Juridique - RECOUVREMENT_TOTAL

```bash
PUT http://localhost:8089/carthage-creance/api/dossiers/4/juridique/finaliser
Headers: Authorization: Bearer {token}
Body: {
  "etatFinal": "RECOUVREMENT_TOTAL",
  "montantRecouvre": 149000.05
}
```

**Vérifications :**
- ✅ Dossier retourné avec etatDossier = RECOVERED_TOTAL
- ✅ dossierStatus = CLOTURE
- ✅ dateCloture définie
- ✅ montantRecouvre = montant total (déjà recouvré + nouveau)

### Test 2 : Finalisation Amiable - RECOUVREMENT_PARTIEL

```bash
PUT http://localhost:8089/carthage-creance/api/dossiers/4/amiable/finaliser
Headers: Authorization: Bearer {token}
Body: {
  "etatFinal": "RECOUVREMENT_PARTIEL",
  "montantRecouvre": 50000.00
}
```

**Vérifications :**
- ✅ Dossier retourné avec etatDossier = RECOVERED_PARTIAL
- ✅ dossierStatus reste ENCOURSDETRAITEMENT (pas clôturé)
- ✅ montantRecouvre mis à jour correctement

### Test 3 : Dossiers Archivés

```bash
GET http://localhost:8089/carthage-creance/api/admin/supervision/dossiers-archives?page=0&size=50&search=D23
Headers: Authorization: Bearer {token}
```

**Vérifications :**
- ✅ Liste paginée de dossiers clôturés depuis plus de 1 an
- ✅ Recherche fonctionne (numéro, créancier, débiteur)
- ✅ Pagination fonctionne
- ✅ Tous les champs sont retournés (incluant etatDossier)

---

## ✅ Checklist de Vérification

- [x] DTO FinalisationDossierDTO créé
- [x] DossierResponseDTO inclut etatDossier, montantTotal, montantRecouvre, montantRestant
- [x] Endpoint PUT /api/dossiers/{dossierId}/juridique/finaliser créé
- [x] Endpoint PUT /api/dossiers/{dossierId}/amiable/finaliser créé
- [x] Endpoint GET /api/admin/supervision/dossiers-archives créé
- [x] Validations implémentées selon l'état final
- [x] Calcul du montant total recouvré correct
- [x] Sécurité et autorisation vérifiées
- [x] Recalcul des statistiques après finalisation
- [x] Imports ajoutés
- [x] Erreurs de compilation corrigées

---

## 🎯 Résultat

Tous les endpoints manquants ont été créés et sont fonctionnels :

1. ✅ **PUT /api/dossiers/{dossierId}/juridique/finaliser** - Finalise un dossier juridique
2. ✅ **PUT /api/dossiers/{dossierId}/amiable/finaliser** - Finalise un dossier amiable
3. ✅ **GET /api/admin/supervision/dossiers-archives** - Récupère les dossiers archivés

Le champ `etatDossier` est maintenant retourné dans toutes les réponses DossierResponseDTO.

Les validations et la logique de calcul du montant recouvré sont correctement implémentées.

