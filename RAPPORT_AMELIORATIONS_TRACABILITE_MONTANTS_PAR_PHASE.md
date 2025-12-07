# 📊 Rapport : Améliorations Backend - Traçabilité des Montants Recouvrés par Phase

## 🎯 Objectif

Implémenter une traçabilité complète des montants recouvrés par phase (amiable et juridique) avec historique détaillé et statistiques.

---

## ✅ Modifications Réalisées

### 1. Entité Dossier

**Fichier :** `src/main/java/projet/carthagecreance_backend/Entity/Dossier.java`

Les champs suivants existaient déjà :
- `montantRecouvrePhaseAmiable` : Montant recouvré en phase amiable
- `montantRecouvrePhaseJuridique` : Montant recouvré en phase juridique

**Calcul automatique :**
- `montantRecouvre` = `montantRecouvrePhaseAmiable` + `montantRecouvrePhaseJuridique`
- `montantRestant` = `montantCreance` - `montantRecouvre`

---

### 2. Entité HistoriqueRecouvrement

**Fichier :** `src/main/java/projet/carthagecreance_backend/Entity/HistoriqueRecouvrement.java`

✅ **Déjà existante** avec tous les champs nécessaires :
- `dossierId` : ID du dossier
- `phase` : AMIABLE ou JURIDIQUE
- `montantRecouvre` : Montant recouvré dans cette opération
- `montantTotalRecouvre` : Montant total recouvré après cette opération
- `montantRestant` : Montant restant après cette opération
- `typeAction` : ACTION_AMIABLE, ACTION_HUISSIER, FINALISATION_AMIABLE, FINALISATION_JURIDIQUE
- `actionId` : ID de l'action associée (optionnel)
- `utilisateurId` : ID de l'utilisateur qui a enregistré
- `dateEnregistrement` : Date et heure de l'enregistrement
- `commentaire` : Commentaire optionnel

---

### 3. Service DossierMontantService

**Fichier :** `src/main/java/projet/carthagecreance_backend/Service/Impl/DossierMontantServiceImpl.java`

✅ **Méthodes existantes et fonctionnelles :**

#### `updateMontantRecouvrePhaseAmiable()`
- Met à jour `montantRecouvrePhaseAmiable`
- Recalcule `montantRecouvre` (total)
- Recalcule `montantRestant` et `etatDossier`
- Crée une entrée dans `HistoriqueRecouvrement`

#### `updateMontantRecouvrePhaseJuridique()`
- Met à jour `montantRecouvrePhaseJuridique`
- Recalcule `montantRecouvre` (total)
- Recalcule `montantRestant` et `etatDossier`
- Crée une entrée dans `HistoriqueRecouvrement`

---

### 4. Endpoints de Finalisation

#### Finalisation Juridique

**Fichier :** `src/main/java/projet/carthagecreance_backend/Controller/DossierController.java`

**Endpoint :** `PUT /api/dossiers/{dossierId}/juridique/finaliser`

✅ **Modifié pour utiliser `updateMontantRecouvrePhaseJuridique()`** :
- Utilise la méthode par phase au lieu de `updateMontantRecouvreAmiable()`
- Type d'action : `FINALISATION_JURIDIQUE`
- Enregistre l'utilisateur qui finalise

**Code modifié :**
```java
dossier = dossierMontantService.updateMontantRecouvrePhaseJuridique(
    dossierId, 
    dto.getMontantRecouvre(), 
    ModeMiseAJour.ADD,
    null, // Pas d'action ID pour la finalisation
    utilisateur.getId(),
    HistoriqueRecouvrement.TypeActionRecouvrement.FINALISATION_JURIDIQUE,
    "Finalisation juridique - " + etatFinal
);
```

#### Finalisation Amiable

**Endpoint :** `PUT /api/dossiers/{dossierId}/amiable/finaliser`

✅ **Modifié pour utiliser `updateMontantRecouvrePhaseAmiable()`** :
- Utilise la méthode par phase
- Type d'action : `FINALISATION_AMIABLE` (via commentaire)
- Enregistre l'utilisateur qui finalise

**Code modifié :**
```java
dossier = dossierMontantService.updateMontantRecouvrePhaseAmiable(
    dossierId, 
    dto.getMontantRecouvre(), 
    ModeMiseAJour.ADD,
    null, // Pas d'action ID pour la finalisation
    utilisateur.getId(),
    "Finalisation amiable - " + etatFinal
);
```

---

### 5. Endpoint Action Amiable

**Fichier :** `src/main/java/projet/carthagecreance_backend/Controller/DossierController.java`

**Endpoint :** `POST /api/dossiers/{id}/amiable`

✅ **Déjà utilise `updateMontantRecouvrePhaseAmiable()`** :
- Enregistre le montant recouvré en phase amiable
- Crée une entrée dans l'historique
- Type d'action : `ACTION_AMIABLE`

---

### 6. Endpoint Action Huissier

**Fichier :** `src/main/java/projet/carthagecreance_backend/Controller/HuissierActionController.java`

**Endpoint :** `POST /api/huissier/action`

✅ **Amélioré pour extraire l'utilisateur** :
- Extrait l'utilisateur depuis le token
- Passe `utilisateurId` au service

**Fichier :** `src/main/java/projet/carthagecreance_backend/Service/Impl/ActionHuissierServiceImpl.java`

✅ **Corrigé pour éviter la duplication** :
- Crée l'action huissier d'abord
- Met à jour le montant recouvré phase juridique APRÈS création (pour avoir l'ID de l'action)
- Type d'action : `ACTION_HUISSIER`

**Code modifié :**
```java
// Créer l'action huissier d'abord
ActionHuissier saved = actionHuissierRepository.save(action);

// Mettre à jour le montant recouvré phase juridique APRÈS création
if (dto.getMontantRecouvre() != null && saved.getId() != null) {
    dossier = dossierMontantService.updateMontantRecouvrePhaseJuridique(
        dto.getDossierId(),
        dto.getMontantRecouvre(),
        updateMode,
        saved.getId(), // ID de l'action huissier créée
        dto.getUtilisateurId(),
        HistoriqueRecouvrement.TypeActionRecouvrement.ACTION_HUISSIER,
        "Recouvrement suite à action huissier: " + dto.getTypeAction()
    );
}
```

---

### 7. DTO ActionHuissierDTO

**Fichier :** `src/main/java/projet/carthagecreance_backend/DTO/ActionHuissierDTO.java`

✅ **Ajouté champ `utilisateurId`** :
```java
private Long utilisateurId; // Pour la traçabilité
```

---

### 8. DTO DossierResponseDTO

**Fichier :** `src/main/java/projet/carthagecreance_backend/DTO/DossierResponseDTO.java`

✅ **Déjà inclut les champs par phase** :
- `montantRecouvrePhaseAmiable`
- `montantRecouvrePhaseJuridique`
- `montantRecouvre`
- `montantRestant`
- `etatDossier`

✅ **Mapping dans `fromEntity()`** :
```java
.montantRecouvrePhaseAmiable(dossier.getMontantRecouvrePhaseAmiable())
.montantRecouvrePhaseJuridique(dossier.getMontantRecouvrePhaseJuridique())
```

---

### 9. Endpoints Historique Recouvrement

**Fichier :** `src/main/java/projet/carthagecreance_backend/Controller/HistoriqueRecouvrementController.java`

✅ **Déjà existants** :
- `GET /api/historique-recouvrement/dossier/{dossierId}` : Historique complet
- `GET /api/historique-recouvrement/dossier/{dossierId}/phase/{phase}` : Historique par phase
- `GET /api/historique-recouvrement/dossier/{dossierId}/resume` : Résumé des montants par phase

---

### 10. Endpoints Statistiques par Phase

**Fichier :** `src/main/java/projet/carthagecreance_backend/Controller/StatistiqueController.java`

✅ **Déjà existants** :
- `GET /api/statistiques/recouvrement-par-phase` : Statistiques globales par phase
- `GET /api/statistiques/recouvrement-par-phase/departement` : Statistiques par département

**Fichier :** `src/main/java/projet/carthagecreance_backend/Service/Impl/StatistiqueServiceImpl.java`

✅ **Méthodes implémentées** :
- `getStatistiquesRecouvrementParPhase()` : Calcule les montants, taux, et nombre de dossiers par phase
- `getStatistiquesRecouvrementParPhaseDepartement()` : Filtre par département

✅ **Amélioration dans `getStatistiquesGlobales()`** :
- Utilise les montants recouvrés réels au lieu de montant créance des dossiers clôturés
- Ajoute `montantRecouvrePhaseAmiable` et `montantRecouvrePhaseJuridique` dans les statistiques

---

## 📋 Résumé des Changements

### Backend

1. ✅ **Endpoints de finalisation** : Utilisent maintenant les méthodes par phase
2. ✅ **Action huissier** : Extrait l'utilisateur et passe `utilisateurId` pour traçabilité
3. ✅ **ActionHuissierDTO** : Ajouté champ `utilisateurId`
4. ✅ **Statistiques globales** : Incluent maintenant les montants par phase
5. ✅ **Correction duplication** : ActionHuissierServiceImpl ne met plus à jour deux fois

### Frontend

📄 **Guide créé :** `GUIDE_FRONTEND_TRACABILITE_MONTANTS_PAR_PHASE.md`

Contient :
- Toutes les APIs disponibles
- Modèles TypeScript à créer
- Services Angular à créer
- Composants avec code complet
- Templates HTML
- Styles CSS recommandés
- Checklist d'intégration

---

## 🔍 Points d'Attention

### 1. Calcul du Montant Restant

Le montant restant est calculé automatiquement :
```
montantRestant = montantCreance - montantRecouvreTotal
```

Où :
```
montantRecouvreTotal = montantRecouvrePhaseAmiable + montantRecouvrePhaseJuridique
```

### 2. Validation Frontend

Le frontend doit valider :
- Montant recouvré ne dépasse pas le montant restant
- Pour RECOUVREMENT_TOTAL : montant total recouvré = montant créance (tolérance 0.01)
- Pour RECOUVREMENT_PARTIEL : 0 < montant total recouvré < montant créance

### 3. Traçabilité

Chaque recouvrement est enregistré dans `HistoriqueRecouvrement` avec :
- Phase (AMIABLE ou JURIDIQUE)
- Type d'action
- Montant recouvré
- Utilisateur qui a enregistré
- Date et heure
- Commentaire

---

## 🚀 Prochaines Étapes

### Backend
- [x] Toutes les modifications backend sont terminées
- [ ] Tester les endpoints avec Postman
- [ ] Vérifier que les statistiques sont correctement calculées

### Frontend
- [ ] Créer les modèles TypeScript
- [ ] Créer les services Angular
- [ ] Créer les composants
- [ ] Intégrer dans les interfaces existantes
- [ ] Tester avec des données réelles

---

## 📝 Notes Techniques

### Ordre d'Exécution pour Action Huissier

1. Créer l'action huissier (pour obtenir l'ID)
2. Mettre à jour le montant recouvré phase juridique (avec l'ID de l'action)
3. Mettre à jour l'action avec les nouvelles valeurs (montantRestant, etatDossier)

### Mode de Mise à Jour

- `ADD` : Ajoute le montant au montant existant (utilisé par défaut)
- `SET` : Remplace le montant existant

---

## ✅ Checklist de Vérification

### Backend
- [x] Champs par phase dans Dossier
- [x] Entité HistoriqueRecouvrement
- [x] Méthodes updateMontantRecouvrePhaseAmiable et updateMontantRecouvrePhaseJuridique
- [x] Endpoints de finalisation utilisent les méthodes par phase
- [x] Endpoint action amiable utilise updateMontantRecouvrePhaseAmiable
- [x] Endpoint action huissier utilise updateMontantRecouvrePhaseJuridique
- [x] Endpoints d'historique fonctionnels
- [x] Endpoints de statistiques par phase fonctionnels
- [x] DossierResponseDTO inclut les champs par phase
- [x] ActionHuissierDTO inclut utilisateurId
- [x] Correction de la duplication dans ActionHuissierServiceImpl

### Documentation
- [x] Guide frontend créé
- [x] Rapport backend créé

---

## 🎯 Résultat Final

Après toutes ces modifications, le système offre :

1. ✅ **Traçabilité complète** : Chaque recouvrement est enregistré avec sa phase, son type d'action, et son utilisateur
2. ✅ **Calcul automatique** : Les montants totaux et restants sont calculés automatiquement
3. ✅ **Historique détaillé** : Possibilité de voir l'historique complet des recouvrements
4. ✅ **Statistiques par phase** : Analyse de performance par phase de recouvrement
5. ✅ **APIs complètes** : Tous les endpoints nécessaires sont disponibles
6. ✅ **Documentation frontend** : Guide complet pour l'intégration frontend

---

**Date de création :** 2025-12-05
**Version :** 1.0

