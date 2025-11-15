# 📋 Guide Complet : Workflow des Actions en Recouvrement Amiable

## 🎯 Vue d'Ensemble

Ce guide explique comment les actions de recouvrement sont gérées dans les interfaces des chefs de recouvrement amiable, et comment elles influencent les décisions de passage au finance ou au juridique.

---

## 📊 Structure de l'Entité Action

### Champs Principaux

```typescript
interface Action {
  id: number;
  type: TypeAction;              // APPEL, EMAIL, VISITE, LETTRE, AUTRE
  reponseDebiteur: ReponseDebiteur | null;  // POSITIVE, NEGATIVE, ou null
  dateAction: Date;              // Date à laquelle l'action a été effectuée
  nbOccurrences: number;          // Nombre de fois que l'action a été effectuée
  coutUnitaire: number;          // Coût unitaire de l'action
  dossier: Dossier;               // Dossier associé
  finance?: Finance;              // Relation avec Finance (optionnel)
}
```

### Types d'Actions Disponibles

- **APPEL** : Appel téléphonique au débiteur
- **EMAIL** : Envoi d'un email au débiteur
- **VISITE** : Visite physique au débiteur
- **LETTRE** : Envoi d'une lettre recommandée
- **AUTRE** : Autre type d'action non listé

### Réponses du Débiteur

- **POSITIVE** : Le débiteur a répondu de manière positive (collaboratif)
- **NEGATIVE** : Le débiteur a répondu de manière négative (non collaboratif)
- **null** : Aucune réponse enregistrée

---

## 🔄 Workflow Complet des Actions

### Étape 1 : Affectation du Dossier au Recouvrement Amiable

```
1. Un dossier validé est affecté au recouvrement amiable
2. Le chef amiable devient agentResponsable
3. Le typeRecouvrement est mis à AMIABLE
4. Le chef et ses agents sont ajoutés à la liste utilisateurs
```

### Étape 2 : Application d'Actions de Recouvrement

```
1. Le chef ou un agent ouvre les détails du dossier
2. Accède à l'onglet "Actions"
3. Clique sur "Ajouter une Action"
4. Remplit le formulaire :
   - Type d'action (APPEL, EMAIL, VISITE, LETTRE, AUTRE)
   - Date de l'action (par défaut : aujourd'hui)
   - Nombre d'occurrences (ex: 2 appels)
   - Coût unitaire (ex: 5 TND par appel)
   - Réponse du débiteur (POSITIVE, NEGATIVE, ou null)
5. Enregistre l'action
```

### Étape 3 : Analyse des Actions et Décision

```
Après chaque action, le système analyse :

1. Analyse de Collaboration :
   - Calcule le pourcentage de réponses POSITIVE vs NEGATIVE
   - Détermine si le débiteur est collaboratif
   - Affiche une recommandation

2. Évaluation pour Finance :
   - Si 2+ réponses POSITIVE récentes (30 derniers jours)
   - → Recommande de passer au Finance
   - Le débiteur semble prêt à payer

3. Évaluation pour Juridique :
   - Si 3+ réponses NEGATIVE
   - Ou aucune réponse après 5 actions
   - → Recommande de passer au Recouvrement Juridique
   - Le débiteur ne répond pas favorablement
```

### Étape 4 : Passage au Finance ou Juridique

```
Si recommandation Finance :
1. Le chef clique sur "Passer au Finance"
2. Confirmation requise
3. Le dossier est affecté au département finance
4. Le typeRecouvrement peut être mis à jour (si un enum Finance existe)

Si recommandation Juridique :
1. Le chef clique sur "Passer au Recouvrement Juridique"
2. Confirmation requise
3. Appel à PUT /api/dossiers/{id}/affecter/recouvrement-juridique
4. Le dossier est affecté au chef juridique
5. Le typeRecouvrement est mis à JURIDIQUE
```

---

## 📱 Interfaces Utilisateur Requises

### 1. Tableau des Actions d'un Dossier

**Localisation** : Onglet "Actions" dans les détails d'un dossier

**Fonctionnalités** :
- Affiche toutes les actions du dossier (triées par date, plus récentes en premier)
- Filtres : par type, par réponse, par date
- Statistiques : nombre d'actions, positives, négatives, coût total
- Actions : Ajouter, Modifier, Supprimer

**Colonnes** :
- Date Action
- Type Action (badge coloré)
- Nombre d'occurrences
- Coût unitaire
- Coût total
- Réponse Débiteur (badge vert/rouge/gris)
- Actions (modifier/supprimer)

### 2. Dialog d'Ajout/Modification d'Action

**Fonctionnalités** :
- Formulaire avec tous les champs
- Calcul automatique du coût total
- Messages contextuels selon la réponse
- Boutons d'action rapide :
  - "Enregistrer et Passer au Finance" (si POSITIVE)
  - "Enregistrer et Passer au Juridique" (si NEGATIVE)

### 3. Composant de Recommandations

**Localisation** : Onglet "Recommandations" dans les détails d'un dossier

**Affichage** :
- Card "Analyse de Collaboration"
  - Pourcentage de réponses positives
  - Statut : Collaboratif / Non Collaboratif
- Card "Recommandation Finance" (si applicable)
  - Message : "Le débiteur semble prêt à payer"
  - Bouton "Passer au Finance"
  - Liste des actions positives récentes
- Card "Recommandation Juridique" (si applicable)
  - Message : "Le débiteur ne répond pas favorablement"
  - Bouton "Passer au Recouvrement Juridique"
  - Liste des actions négatives

### 4. Vue d'Ensemble Actions (Dashboard)

**Localisation** : Dashboard du chef recouvrement amiable

**Affichage** :
- Statistiques globales :
  - Total d'actions aujourd'hui
  - Total d'actions cette semaine
  - Dossiers nécessitant attention (3+ actions négatives)
  - Coût total des actions
- Graphiques :
  - Répartition par type d'action
  - Évolution dans le temps
  - Répartition des réponses
- Liste des dossiers nécessitant attention

---

## 🎯 Règles Métier Détaillées

### Règle 1 : Passage au Finance

**Conditions** :
- Au moins 2 réponses POSITIVE récentes (dans les 30 derniers jours)
- OU une réponse POSITIVE très récente (7 derniers jours) avec engagement de paiement

**Action** :
- Le chef peut passer le dossier au finance
- Le dossier est marqué comme prêt pour le paiement
- Les actions positives sont conservées pour référence

### Règle 2 : Passage au Juridique

**Conditions** :
- 3+ réponses NEGATIVE consécutives
- OU aucune réponse après 5 actions de recouvrement
- OU refus explicite de payer (réponse NEGATIVE avec commentaire)

**Action** :
- Le chef peut passer le dossier au recouvrement juridique
- Le dossier est affecté au chef juridique
- Le typeRecouvrement est mis à JURIDIQUE
- Toutes les actions sont conservées pour référence

### Règle 3 : Continuer le Recouvrement Amiable

**Conditions** :
- Mixte de réponses POSITIVE et NEGATIVE
- Réponses récentes (débiteur réactif)
- Pas assez de réponses pour prendre une décision

**Action** :
- Continuer les actions de recouvrement amiable
- Surveiller la tendance (amélioration ou dégradation)
- Réévaluer après chaque nouvelle action

---

## 📊 Exemples de Scénarios

### Scénario 1 : Débiteur Collaboratif

```
Jour 1 : Action APPEL → Réponse POSITIVE (débiteur promet de payer)
Jour 5 : Action EMAIL → Réponse POSITIVE (confirme le paiement)
Jour 10 : Action APPEL → Réponse POSITIVE (fixe une date de paiement)

Analyse :
- 3 réponses POSITIVE en 10 jours
- Pourcentage : 100% positif
- Recommandation : PASSER AU FINANCE

Action : Le chef passe le dossier au finance
```

### Scénario 2 : Débiteur Non Collaboratif

```
Jour 1 : Action APPEL → Réponse NEGATIVE (refuse de payer)
Jour 5 : Action EMAIL → Pas de réponse
Jour 10 : Action VISITE → Réponse NEGATIVE (refuse de recevoir)
Jour 15 : Action LETTRE → Pas de réponse
Jour 20 : Action APPEL → Réponse NEGATIVE (menace de ne plus répondre)

Analyse :
- 3 réponses NEGATIVE
- 2 actions sans réponse
- Pourcentage : 60% négatif
- Recommandation : PASSER AU JURIDIQUE

Action : Le chef passe le dossier au recouvrement juridique
```

### Scénario 3 : Débiteur Hésitant

```
Jour 1 : Action APPEL → Réponse POSITIVE (promet de payer)
Jour 5 : Action EMAIL → Pas de réponse
Jour 10 : Action APPEL → Réponse NEGATIVE (change d'avis)
Jour 15 : Action VISITE → Réponse POSITIVE (nouveau engagement)

Analyse :
- Mixte de réponses
- Tendance : instable
- Recommandation : CONTINUER LE RECOUVREMENT AMIABLE

Action : Le chef continue les actions, surveille la tendance
```

---

## 🔧 Intégration Technique

### APIs Backend Utilisées

1. **GET /api/actions/dossier/{dossierId}**
   - Récupère toutes les actions d'un dossier

2. **POST /api/actions**
   - Crée une nouvelle action
   - Body : {type, dateAction, nbOccurrences, coutUnitaire, reponseDebiteur, dossier: {id}}

3. **PUT /api/actions/{id}**
   - Modifie une action existante

4. **DELETE /api/actions/{id}**
   - Supprime une action

5. **GET /api/actions/dossier/{dossierId}/total-cost**
   - Retourne le coût total des actions

6. **GET /api/actions/dossier/{dossierId}/reponse/{reponse}**
   - Filtre les actions par réponse

7. **PUT /api/dossiers/{id}/affecter/recouvrement-juridique**
   - Passe le dossier au juridique

### Services Frontend Requis

1. **ActionService**
   - Toutes les opérations CRUD sur les actions
   - Filtrage et recherche

2. **DecisionRecouvrementService**
   - Analyse de collaboration
   - Évaluation pour finance/juridique
   - Recommandations

3. **ChefRecouvrementAmiableService**
   - Méthodes spécifiques au chef
   - Intégration avec les actions

---

## ✅ Checklist d'Implémentation

### Phase 1 : Services
- [ ] Créer ActionService avec toutes les méthodes
- [ ] Créer DecisionRecouvrementService
- [ ] Mettre à jour ChefRecouvrementAmiableService

### Phase 2 : Composants
- [ ] Composant tableau des actions
- [ ] Composant dialog ajout/modification
- [ ] Composant recommandations
- [ ] Composant vue d'ensemble

### Phase 3 : Intégration
- [ ] Intégrer dans les détails dossier (onglets)
- [ ] Intégrer dans le dashboard chef
- [ ] Tester le flux complet

### Phase 4 : Tests
- [ ] Tests unitaires
- [ ] Tests E2E
- [ ] Tests de performance

---

## 📝 Notes Importantes

1. **Historique** : Toutes les actions sont conservées, même après passage au finance/juridique
2. **Coûts** : Le coût total des actions est calculé automatiquement
3. **Décisions** : Les recommandations sont automatiques mais la décision finale appartient au chef
4. **Notifications** : Envisager des notifications quand un dossier nécessite attention
5. **Rapports** : Possibilité d'exporter les actions en Excel/PDF pour reporting

---

**Ce workflow permet une gestion complète et traçable des actions de recouvrement, avec des recommandations intelligentes pour optimiser le processus de recouvrement.**

