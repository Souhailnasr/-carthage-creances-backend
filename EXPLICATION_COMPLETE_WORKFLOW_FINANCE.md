# 📊 Explication Complète : Logique et Workflow de Finance

## 🎯 Vue d'Ensemble

Ce document explique en détail toute la logique créée pour le module Finance et comment se déroule le workflow complet, depuis la création d'un dossier jusqu'au paiement de la facture finale.

---

## 📋 1. ARCHITECTURE GÉNÉRALE DU SYSTÈME FINANCE

### 1.1. Principe Fondamental

Le système Finance est conçu pour **centraliser tous les calculs de coûts** liés à un dossier de recouvrement. Il permet au chef financier de construire la facture finale en agrégeant tous les frais engagés.

### 1.2. Composants Principaux

Le système Finance comprend **5 entités principales** :

1. **Finance** : Entité centrale qui agrège tous les coûts d'un dossier
2. **FluxFrais** : Trace chaque frais individuel engagé (appel, visite, frais avocat, etc.)
3. **TarifCatalogue** : Catalogue centralisé des tarifs unitaires pour chaque type de frais
4. **Facture** : Facture générée automatiquement à partir des frais validés
5. **Paiement** : Enregistrement des paiements reçus pour une facture

---

## 🏗️ 2. LES ENTITÉS ET LEUR RÔLE

### 2.1. L'Entité Finance

**Rôle** : C'est l'entité centrale qui agrège **TOUS** les coûts d'un dossier.

**Champs principaux** :
- **Frais de création** : Coût fixe pour la création d'un dossier (250 TND selon l'annexe du contrat)
- **Frais de gestion** : Coût mensuel de gestion du dossier (configurable)
- **Durée de gestion** : Nombre de mois de gestion (calculé automatiquement)
- **Coûts actions amiable** : Total des coûts des actions de recouvrement amiable
- **Coûts actions juridique** : Total des coûts des actions de recouvrement juridique
- **Nombre d'actions amiable** : Compteur du nombre d'actions amiable
- **Nombre d'actions juridique** : Compteur du nombre d'actions juridique
- **Frais avocat** : Frais engagés pour les services d'un avocat
- **Frais huissier** : Frais engagés pour les services d'un huissier
- **Commission amiable** : Commission calculée sur le montant recouvré en phase amiable (12% selon l'annexe)
- **Commission juridique** : Commission calculée sur le montant recouvré en phase juridique (15% selon l'annexe)
- **Commission relance** : Commission calculée sur le montant recouvré en phase relance (5% selon l'annexe)
- **Commission intérêts** : Commission calculée sur les intérêts recouvrés (50% selon l'annexe)
- **Statut de facturation** : Indique si la facture est finalisée ou non
- **Date de facturation** : Date à laquelle la facture a été finalisée

**Calculs automatiques** :
- **Facture finale** = Frais création + (Frais gestion × Durée) + Coûts actions amiable + Coûts actions juridique + Frais avocat + Frais huissier + Commissions

**Relation** :
- **OneToOne** avec Dossier : Chaque dossier a une et une seule Finance

---

### 2.2. L'Entité FluxFrais

**Rôle** : Trace **chaque frais individuel** engagé dans le système.

**Champs principaux** :
- **Phase** : Phase du recouvrement où le frais a été engagé
  - `CREATION` : Frais de création du dossier
  - `AMIABLE` : Frais de recouvrement amiable
  - `ENQUETE` : Frais d'enquête
  - `JURIDIQUE` : Frais de recouvrement juridique
- **Catégorie** : Type de frais (APPEL, EMAIL, VISITE, HUISSIER, AVOCAT, AUDIENCE, ENQUETE, etc.)
- **Quantité** : Nombre d'occurrences (ex: 2 appels)
- **Tarif unitaire** : Prix unitaire du frais (récupéré du catalogue ou saisi manuellement)
- **Montant** : Calculé automatiquement = Quantité × Tarif unitaire
- **Statut** : Statut du frais dans le workflow
  - `EN_ATTENTE` : En attente de validation par le chef financier
  - `VALIDE` : Validé, prêt à être facturé
  - `REJETE` : Rejeté (avec motif)
  - `FACTURE` : Inclus dans une facture
  - `PAYE` : Payé
- **Date d'action** : Date à laquelle le frais a été engagé
- **Justificatif** : URL du document justificatif (facture, reçu, etc.)
- **Commentaire** : Commentaire ou motif de rejet

**Relations** :
- **ManyToOne** avec Dossier : Chaque frais appartient à un dossier
- **ManyToOne** avec Action : Peut être lié à une action (optionnel)
- **ManyToOne** avec Enquête : Peut être lié à une enquête (optionnel)
- **ManyToOne** avec Audience : Peut être lié à une audience (optionnel)
- **ManyToOne** avec Avocat : Peut être lié à un avocat (optionnel)
- **ManyToOne** avec Huissier : Peut être lié à un huissier (optionnel)
- **ManyToOne** avec Facture : Lié à une facture une fois inclus

**Calcul automatique** :
- Lors de la création ou mise à jour, le montant est calculé automatiquement : `montant = quantite × tarifUnitaire`

---

### 2.3. L'Entité TarifCatalogue

**Rôle** : Catalogue centralisé des **tarifs unitaires** pour chaque type de frais.

**Champs principaux** :
- **Phase** : Phase concernée (CREATION, AMIABLE, ENQUETE, JURIDIQUE)
- **Catégorie** : Catégorie du frais (APPEL, EMAIL, VISITE, HUISSIER, etc.)
- **Description** : Description du tarif
- **Fournisseur** : Nom du fournisseur (ex: nom de l'avocat, huissier)
- **Tarif unitaire** : Prix unitaire en devise
- **Devise** : Devise (TND par défaut)
- **Date de début** : Date de début de validité du tarif
- **Date de fin** : Date de fin de validité (optionnel, pour gérer l'historique)
- **Actif** : Indique si le tarif est actif (true/false)

**Utilisation** :
- Permet au chef financier de gérer les tarifs sans modifier le code
- Les frais sont créés automatiquement en utilisant les tarifs actifs du catalogue
- Historique des tarifs conservé pour traçabilité

---

### 2.4. L'Entité Facture

**Rôle** : Facture générée automatiquement à partir des frais validés d'un dossier.

**Champs principaux** :
- **Numéro de facture** : Numéro unique généré automatiquement (format: FACT-YYYY-NNNN)
- **Dossier** : Dossier concerné
- **Période début/fin** : Période couverte par la facture
- **Date d'émission** : Date d'émission de la facture
- **Date d'échéance** : Date limite de paiement (défaut: +30 jours)
- **Montant HT** : Montant hors taxes (somme des frais validés)
- **Montant TTC** : Montant toutes taxes comprises
- **TVA** : Taux de TVA (défaut: 19%)
- **Statut** : Statut de la facture
  - `BROUILLON` : En cours de création
  - `EMISE` : Émise au client
  - `PAYEE` : Payée
  - `EN_RETARD` : En retard de paiement
  - `ANNULEE` : Annulée
- **PDF URL** : URL du PDF généré
- **Envoyée** : Indique si la facture a été envoyée au client
- **Relance envoyée** : Indique si une relance a été envoyée

**Relations** :
- **ManyToOne** avec Dossier : Chaque facture appartient à un dossier
- **OneToMany** avec FluxFrais : Liste des frais inclus dans la facture
- **OneToMany** avec Paiement : Liste des paiements reçus

**Génération automatique** :
- Sélectionne tous les frais `VALIDE` et non facturés du dossier
- Calcule le montant HT (somme des montants des frais)
- Calcule le montant TTC (HT × (1 + TVA/100))
- Génère un numéro unique séquentiel
- Met à jour le statut des frais à `FACTURE`

---

### 2.5. L'Entité Paiement

**Rôle** : Enregistre un paiement reçu pour une facture.

**Champs principaux** :
- **Facture** : Facture concernée
- **Date de paiement** : Date du paiement
- **Montant** : Montant payé
- **Mode de paiement** : 
  - `VIREMENT` : Virement bancaire
  - `CHEQUE` : Chèque
  - `ESPECES` : Espèces
  - `TRAITE` : Traite
  - `AUTRE` : Autre mode
- **Référence** : Référence du paiement (numéro de chèque, virement, etc.)
- **Statut** : Statut du paiement
  - `EN_ATTENTE` : En attente de validation
  - `VALIDE` : Validé
  - `REFUSE` : Refusé (avec motif)
- **Commentaire** : Commentaire ou motif de refus

**Relations** :
- **ManyToOne** avec Facture : Chaque paiement appartient à une facture

---

## 🔄 3. WORKFLOW COMPLET : DE LA CRÉATION DU DOSSIER AU PAIEMENT

### 3.1. Étape 1 : Création du Dossier

**Quand** : Un agent ou un chef crée un nouveau dossier dans le système.

**Ce qui se passe** :
1. Le dossier est créé avec ses informations (créancier, débiteur, montant de créance, etc.)
2. **Automatiquement**, une entité `Finance` est créée pour ce dossier
3. **Automatiquement**, un `FluxFrais` est créé pour les frais d'ouverture :
   - Phase = `CREATION`
   - Catégorie = `OUVERTURE_DOSSIER`
   - Le système cherche le tarif dans `TarifCatalogue` pour phase=CREATION, catégorie=OUVERTURE_DOSSIER
   - Si trouvé : `tarifUnitaire` = 250 TND (selon l'annexe)
   - `montant` = 250 TND (calculé automatiquement)
   - Statut = `EN_ATTENTE` (en attente de validation)
4. Les valeurs par défaut de Finance sont initialisées :
   - `fraisCreationDossier` = 250 TND (selon l'annexe du contrat)
   - `fraisGestionDossier` = configurable
   - `dureeGestionMois` = 0 (sera calculé plus tard)
   - Tous les autres coûts = 0

**Résultat** : Le dossier a maintenant une Finance associée et un frais d'ouverture de dossier créé automatiquement, prêt à être validé.

---

### 3.2. Étape 2 : Validation du Dossier

**Quand** : Le dossier est validé par un chef.

**Ce qui se passe** :
- Le statut du dossier passe à `VALIDE`
- La Finance reste inchangée (aucun frais supplémentaire)

**Résultat** : Le dossier est prêt à être affecté au recouvrement.

---

### 3.3. Étape 3 : Affectation au Recouvrement Amiable

**Quand** : Le dossier est affecté au chef de recouvrement amiable.

**Ce qui se passe** :
- Le dossier est assigné au chef amiable et à ses agents
- Le type de recouvrement est mis à `AMIABLE`
- La Finance reste inchangée (aucun frais supplémentaire)

**Résultat** : Le dossier est prêt pour les actions de recouvrement amiable.

---

### 3.4. Étape 4 : Enregistrement d'Actions (Recouvrement Amiable)

**Quand** : Un agent ou le chef amiable enregistre une action (appel, email, visite, etc.).

**Exemple** :
- Type : APPEL
- Date : 15/11/2025
- Nombre d'occurrences : 2
- Réponse débiteur : POSITIVE

**Ce qui se passe** :

**Option A : Création automatique d'un FluxFrais**
1. L'action est enregistrée dans le système
2. **Automatiquement**, un `FluxFrais` est créé :
   - Phase = `AMIABLE` (car le dossier est en recouvrement amiable)
   - Catégorie = "APPEL" (mappé depuis le type d'action)
   - Quantité = 2 (nombre d'occurrences)
   - Le système cherche le tarif dans `TarifCatalogue` pour phase=AMIABLE, catégorie=APPEL
   - Si trouvé : `tarifUnitaire` = tarif du catalogue (ex: 5 TND)
   - Si non trouvé : le tarif peut être saisi manuellement
   - `montant` = 2 × 5 = 10 TND (calculé automatiquement)
   - Statut = `EN_ATTENTE` (en attente de validation)
3. Le `FluxFrais` est lié au dossier et à l'action

**Option B : Mise à jour directe de Finance (ancien système)**
1. L'action est enregistrée
2. Le système calcule le coût : `coutUnitaire` × `nbOccurrences`
3. La Finance est mise à jour :
   - `coutActionsAmiable` += coût calculé
   - `nombreActionsAmiable` += 1

**Résultat** : Un frais est créé et en attente de validation, OU les coûts sont directement mis à jour dans Finance.

---

### 3.5. Étape 5 : Validation des Frais par le Chef Financier

**Quand** : Le chef financier consulte la liste des frais en attente.

**Ce qui se passe** :
1. Le chef financier accède à la liste des frais avec statut `EN_ATTENTE`
2. Pour chaque frais, il peut :
   - **Valider** : Le statut passe à `VALIDE`
     - Le frais est maintenant prêt à être inclus dans une facture
   - **Rejeter** : Le statut passe à `REJETE`
     - Un motif de rejet est obligatoire
     - Le frais ne sera pas facturé

**Résultat** : Les frais validés sont prêts pour la facturation.

---

### 3.6. Étape 6 : Passage au Recouvrement Juridique

**Quand** : Si plusieurs réponses négatives du débiteur, le dossier passe au juridique.

**Ce qui se passe** :
- Le type de recouvrement est mis à `JURIDIQUE`
- La Finance reste la même (aucun changement)
- Les nouvelles actions juridique seront ajoutées à `coutActionsJuridique` (ou créeront des FluxFrais avec phase=JURIDIQUE)

**Résultat** : Le dossier est maintenant en recouvrement juridique.

---

### 3.7. Étape 7 : Création de Frais Juridiques

**Quand** : Des actions juridiques sont effectuées (audience, frais avocat, frais huissier, etc.).

**Exemples de frais juridiques** :
- **Frais avocat** : Création d'un `FluxFrais` avec phase=JURIDIQUE, catégorie=AVOCAT
- **Frais huissier** : Création d'un `FluxFrais` avec phase=JURIDIQUE, catégorie=HUISSIER
- **Frais d'audience** : Création d'un `FluxFrais` avec phase=JURIDIQUE, catégorie=AUDIENCE

**Ce qui se passe** :
1. Un `FluxFrais` est créé (manuellement ou automatiquement)
2. Le tarif est récupéré depuis `TarifCatalogue` ou saisi manuellement
3. Le montant est calculé automatiquement
4. Le statut est `EN_ATTENTE` (en attente de validation)

**Résultat** : Les frais juridiques sont enregistrés et en attente de validation.

---

### 3.8. Étape 8 : Clôture du Dossier

**Quand** : Le dossier est clôturé (recouvrement terminé ou abandonné).

**Ce qui se passe** :
1. Le statut du dossier passe à `CLOTURE`
2. La Finance est mise à jour :
   - `dureeGestionMois` = calculé automatiquement (différence entre date de création et date de clôture, en mois)
   - Exemple : Si créé le 01/01/2025 et clôturé le 01/04/2025 → 3 mois
3. Le coût total de gestion est calculé :
   - `coutGestionTotal` = `fraisGestionDossier` × `dureeGestionMois`
   - Exemple : 10 TND/mois × 3 mois = 30 TND

**Résultat** : La Finance contient maintenant tous les coûts finaux du dossier.

---

### 3.9. Étape 9 : Génération Automatique de la Facture

**Quand** : Le chef financier décide de générer une facture pour un dossier clôturé.

**Ce qui se passe** :
1. Le chef financier sélectionne un dossier et une période
2. Le système génère automatiquement une facture :
   - **Sélection des frais** : Tous les frais avec statut `VALIDE` et non encore facturés du dossier
   - **Calcul du montant HT** : Somme de tous les montants des frais sélectionnés
   - **Calcul du montant TTC** : HT × (1 + TVA/100), avec TVA = 19% par défaut
   - **Génération du numéro** : Format FACT-YYYY-NNNN (ex: FACT-2025-0001)
   - **Mise à jour des frais** : Le statut de tous les frais inclus passe à `FACTURE`
   - **Statut initial** : La facture est créée avec statut `BROUILLON`
3. La facture est liée au dossier et contient la liste des frais inclus

**Résultat** : Une facture est créée en brouillon, prête à être finalisée.

---

### 3.10. Étape 10 : Finalisation de la Facture

**Quand** : Le chef financier vérifie et finalise la facture.

**Ce qui se passe** :
1. Le chef financier consulte le détail de la facture (brouillon)
2. Il vérifie tous les frais inclus
3. Il peut recalculer les coûts si nécessaire
4. Il finalise la facture :
   - Le statut passe de `BROUILLON` à `EMISE`
   - La date d'émission est enregistrée
   - La date d'échéance est calculée (date d'émission + 30 jours par défaut)
5. Un PDF peut être généré automatiquement

**Résultat** : La facture est émise et prête à être envoyée au client.

---

### 3.11. Étape 11 : Envoi de la Facture

**Quand** : Le chef financier envoie la facture au client.

**Ce qui se passe** :
- La facture est marquée comme `envoyee = true`
- Le PDF peut être envoyé par email ou imprimé

**Résultat** : La facture a été envoyée au client.

---

### 3.12. Étape 12 : Enregistrement d'un Paiement

**Quand** : Le client effectue un paiement.

**Exemple** :
- Montant : 500 TND
- Mode : VIREMENT
- Référence : VIR-2025-001234
- Date : 20/11/2025

**Ce qui se passe** :
1. Un `Paiement` est créé :
   - Lié à la facture concernée
   - Montant = 500 TND
   - Mode de paiement = VIREMENT
   - Référence = VIR-2025-001234
   - Date de paiement = 20/11/2025
   - Statut = `EN_ATTENTE` (en attente de validation)
2. Le chef financier valide le paiement :
   - Le statut passe à `VALIDE`
   - Le système vérifie si le montant total des paiements validés couvre le montant TTC de la facture
3. Si le paiement couvre la totalité :
   - Le statut de la facture passe à `PAYEE`
   - Le statut de tous les frais inclus passe à `PAYE`

**Résultat** : Le paiement est enregistré et validé.

---

### 3.13. Étape 13 : Gestion des Retards de Paiement

**Quand** : La date d'échéance est dépassée et la facture n'est pas payée.

**Ce qui se passe** :
1. Le système détecte automatiquement les factures en retard :
   - Date d'échéance < aujourd'hui
   - Statut = `EMISE` (pas encore payée)
2. Le statut de la facture passe à `EN_RETARD`
3. Une relance peut être envoyée :
   - Le champ `relanceEnvoyee` passe à `true`
   - Un email de relance peut être généré

**Résultat** : La facture est marquée comme en retard et une relance peut être envoyée.

---

## 📊 4. SYSTÈME DE TARIFICATION (TarifCatalogue)

### 4.1. Principe

Le `TarifCatalogue` permet de **centraliser tous les tarifs unitaires** sans modifier le code. Le chef financier peut gérer les tarifs via l'interface.

### 4.2. Structure d'un Tarif

Chaque tarif est défini par :
- **Phase** : CREATION, AMIABLE, ENQUETE, ou JURIDIQUE
- **Catégorie** : APPEL, EMAIL, VISITE, HUISSIER, AVOCAT, etc.
- **Tarif unitaire** : Prix en TND
- **Période de validité** : Date de début et date de fin (optionnel)
- **Actif** : true/false

### 4.3. Tarifs selon l'Annexe du Contrat

D'après l'annexe du contrat de recouvrement fournie, voici les tarifs officiels à intégrer dans le système :

#### 4.3.1. Frais Fixes (Montants en TND)

Ces tarifs doivent être ajoutés dans le `TarifCatalogue` avec les paramètres suivants :

| Phase | Catégorie | Tarif Unitaire | Description |
|-------|-----------|----------------|-------------|
| `CREATION` | `OUVERTURE_DOSSIER` | **250 TND** | Frais fixes de réception et d'ouverture de dossier |
| `ENQUETE` | `ENQUETE_PRECONTENTIEUSE` | **300 TND** | Frais Enquête Précontentieuse |
| `JURIDIQUE` | `AVANCE_RECOUVREMENT_JUDICIAIRE` | **1000 TND** | Avance sur frais de recouvrement judiciaire |
| `JURIDIQUE` | `ATTESTATION_CARENCE` | **500 TND** | Attestation de carence à la demande du mandant |
| `AMIABLE` | `RELANCE_FACTURE_MOINS_6_MOIS` | **0 TND** | Relance Factures datées de moins de 6 mois (Gratuit) |

#### 4.3.2. Commissions (Pourcentages)

Les commissions sont calculées sur le **montant recouvré** et doivent être gérées différemment car ce sont des pourcentages, pas des montants fixes :

| Phase | Type de Commission | Taux | Base de Calcul |
|-------|-------------------|------|----------------|
| `AMIABLE` | `COMMISSION_RELANCE` | **5%** | Montant recouvré en phase relance |
| `AMIABLE` | `COMMISSION_AMIABLE` | **12%** | Montant recouvré en phase amiable |
| `JURIDIQUE` | `COMMISSION_JURIDIQUE` | **15%** | Montant recouvré en phase juridique |
| `JURIDIQUE` | `COMMISSION_INTERETS` | **50%** | Montant des intérêts recouvrés |

**Note importante** : Les commissions sont calculées **après** le recouvrement, sur le montant effectivement recouvré. Elles ne sont pas des frais fixes mais des pourcentages appliqués sur le résultat.

#### 4.3.3. Exemples de Tarifs pour Actions (À compléter)

Pour les actions courantes, les tarifs peuvent être définis comme suit (exemples) :

```
Phase: AMIABLE, Catégorie: APPEL, Tarif: 5 TND
Phase: AMIABLE, Catégorie: EMAIL, Tarif: 2 TND
Phase: AMIABLE, Catégorie: VISITE, Tarif: 20 TND
Phase: JURIDIQUE, Catégorie: AVOCAT, Tarif: 200 TND
Phase: JURIDIQUE, Catégorie: HUISSIER, Tarif: 150 TND
```

### 4.4. Utilisation Automatique

Lors de la création d'un `FluxFrais` :
1. Le système cherche le tarif actif dans le catalogue pour la phase et catégorie données
2. Si trouvé : Le tarif unitaire est utilisé automatiquement
3. Si non trouvé : Le tarif peut être saisi manuellement

### 4.5. Historique des Tarifs

- Les tarifs peuvent être désactivés (au lieu d'être supprimés)
- L'historique est conservé pour traçabilité
- Les anciens tarifs restent liés aux frais déjà créés

---

## 🔄 5. WORKFLOW DES STATUTS

### 5.1. Workflow des Frais (FluxFrais)

```
EN_ATTENTE → VALIDE → FACTURE → PAYE
     ↓
  REJETE (fin du workflow)
```

**Explication** :
1. **EN_ATTENTE** : Frais créé, en attente de validation par le chef financier
2. **VALIDE** : Frais validé, prêt à être inclus dans une facture
3. **FACTURE** : Frais inclus dans une facture
4. **PAYE** : Frais payé (la facture a été payée)
5. **REJETE** : Frais rejeté (ne sera pas facturé)

---

### 5.2. Workflow des Factures

```
BROUILLON → EMISE → PAYEE
     ↓         ↓
  ANNULEE  EN_RETARD
```

**Explication** :
1. **BROUILLON** : Facture en cours de création
2. **EMISE** : Facture finalisée et envoyée au client
3. **PAYEE** : Facture payée (tous les paiements couvrent le montant TTC)
4. **EN_RETARD** : Date d'échéance dépassée, non payée
5. **ANNULEE** : Facture annulée

---

### 5.3. Workflow des Paiements

```
EN_ATTENTE → VALIDE
     ↓
  REFUSE
```

**Explication** :
1. **EN_ATTENTE** : Paiement enregistré, en attente de validation
2. **VALIDE** : Paiement validé par le chef financier
3. **REFUSE** : Paiement refusé (avec motif)

---

## 💰 6. CALCULS AUTOMATIQUES

### 6.1. Calcul du Montant d'un Frais

```
montant = quantite × tarifUnitaire
```

**Exemple** :
- Quantité : 3 appels
- Tarif unitaire : 5 TND
- Montant : 3 × 5 = 15 TND

---

### 6.2. Calcul du Montant HT d'une Facture

```
montantHT = somme de tous les montants des frais VALIDES inclus
```

**Exemple** :
- Frais 1 : 15 TND (3 appels)
- Frais 2 : 20 TND (1 visite)
- Frais 3 : 200 TND (frais avocat)
- Montant HT : 15 + 20 + 200 = 235 TND

---

### 6.3. Calcul du Montant TTC d'une Facture

```
montantTTC = montantHT × (1 + TVA/100)
```

**Exemple** :
- Montant HT : 235 TND
- TVA : 19%
- Montant TTC : 235 × (1 + 19/100) = 235 × 1.19 = 279.65 TND

---

### 6.4. Calcul de la Durée de Gestion

```
dureeGestionMois = nombre de mois entre dateCreation et dateCloture
```

**Exemple** :
- Date création : 01/01/2025
- Date clôture : 01/04/2025
- Durée : 3 mois

---

### 6.5. Calcul du Coût Total de Gestion

```
coutGestionTotal = fraisGestionDossier × dureeGestionMois
```

**Exemple** :
- Frais gestion : 10 TND/mois
- Durée : 3 mois
- Coût total : 10 × 3 = 30 TND

---

### 6.6. Calcul de la Facture Finale (Finance)

```
factureFinale = fraisCreationDossier 
              + (fraisGestionDossier × dureeGestionMois)
              + coutActionsAmiable
              + coutActionsJuridique
              + fraisAvocat
              + fraisHuissier
              + commissionAmiable
              + commissionJuridique
              + commissionRelance
              + commissionInterets
```

**Exemple** :
- Frais création : 250 TND (selon l'annexe)
- Coût gestion (3 mois) : 30 TND
- Coûts actions amiable : 47 TND
- Coûts actions juridique : 15 TND
- Frais avocat : 200 TND
- Frais huissier : 150 TND
- Commission amiable (12% sur 1000 TND recouvré) : 120 TND
- Commission juridique (15% sur 500 TND recouvré) : 75 TND
- **Total** : 250 + 30 + 47 + 15 + 200 + 150 + 120 + 75 = **887 TND**

---

### 6.7. Calcul des Commissions

Les commissions sont calculées sur le **montant recouvré** selon la phase :

```
commissionRelance = montantRecouvreRelance × 5%
commissionAmiable = montantRecouvreAmiable × 12%
commissionJuridique = montantRecouvreJuridique × 15%
commissionInterets = montantInteretsRecouvres × 50%
```

**Exemple** :
- Montant recouvré en phase amiable : 1000 TND
- Commission amiable : 1000 × 12% = **120 TND**
- Montant recouvré en phase juridique : 500 TND
- Commission juridique : 500 × 15% = **75 TND**
- Intérêts recouvrés : 200 TND
- Commission intérêts : 200 × 50% = **100 TND**

---

## 📈 7. STATISTIQUES ET RAPPORTS

### 7.1. Statistiques Globales

Le système permet de calculer :
- Total des frais de création
- Total des frais de gestion
- Total des actions amiable
- Total des actions juridique
- Total des frais avocat
- Total des frais huissier
- Grand total

### 7.2. Statistiques par Dossier

Pour chaque dossier, on peut consulter :
- Détail de tous les frais
- Répartition par phase (CREATION, AMIABLE, ENQUETE, JURIDIQUE)
- Répartition par catégorie (APPEL, EMAIL, VISITE, etc.)
- Total par type de frais

### 7.3. ROI par Agent

Le système calcule le ROI (Retour sur Investissement) par agent :
```
ROI = ((Montant recouvré - Frais engagés) / Frais engagés) × 100
```

---

## 🔍 8. CRÉATION AUTOMATIQUE DE FRAIS

### 8.1. Depuis une Action

**Quand** : Une action est créée dans le système.

**Ce qui se passe** :
1. Le système détecte la création d'une action
2. Un `FluxFrais` est créé automatiquement :
   - Phase = déterminée selon `typeRecouvrement` du dossier (AMIABLE ou JURIDIQUE)
   - Catégorie = mappée depuis le `TypeAction` (APPEL → "APPEL", etc.)
   - Quantité = nombre d'occurrences de l'action
   - Tarif unitaire = récupéré depuis `TarifCatalogue`
   - Montant = calculé automatiquement
   - Statut = `EN_ATTENTE`
3. Le frais est lié à l'action et au dossier

---

### 8.2. Depuis une Enquête

**Quand** : Une enquête est créée.

**Ce qui se passe** :
1. Un `FluxFrais` est créé automatiquement :
   - Phase = `ENQUETE`
   - Catégorie = "ENQUETE"
   - Tarif unitaire = récupéré depuis `TarifCatalogue`
   - Statut = `EN_ATTENTE`
2. Le frais est lié à l'enquête et au dossier

---

### 8.3. Depuis une Audience

**Quand** : Une audience est créée.

**Ce qui se passe** :
1. Un `FluxFrais` est créé automatiquement :
   - Phase = `JURIDIQUE`
   - Catégorie = "AUDIENCE"
   - Tarif unitaire = récupéré depuis `TarifCatalogue`
   - Statut = `EN_ATTENTE`
2. Si un avocat est présent : Un frais supplémentaire peut être créé (catégorie=AVOCAT)
3. Si un huissier est présent : Un frais supplémentaire peut être créé (catégorie=HUISSIER)
4. Les frais sont liés à l'audience et au dossier

---

## 🎯 9. RÉSUMÉ DU WORKFLOW COMPLET

### Workflow Simplifié

```
1. Création Dossier → Finance créée automatiquement
2. Validation Dossier → Finance inchangée
3. Affectation Amiable → Finance inchangée
4. Actions Amiable → FluxFrais créés (EN_ATTENTE)
5. Validation Frais → Statut passe à VALIDE
6. Passage Juridique → Finance inchangée
7. Actions Juridique → FluxFrais créés (EN_ATTENTE)
8. Validation Frais → Statut passe à VALIDE
9. Clôture Dossier → Durée de gestion calculée
10. Génération Facture → Facture créée (BROUILLON)
11. Finalisation Facture → Statut passe à EMISE
12. Envoi Facture → Facture envoyée au client
13. Paiement Client → Paiement enregistré (EN_ATTENTE)
14. Validation Paiement → Statut passe à VALIDE
15. Facture Payée → Statut passe à PAYEE
```

---

## ✅ 10. POINTS CLÉS À RETENIR

1. **Finance** est créée automatiquement à la création d'un dossier
2. **FluxFrais** trace chaque frais individuel avec son statut
3. **TarifCatalogue** centralise tous les tarifs unitaires
4. **Facture** est générée automatiquement à partir des frais validés
5. **Paiement** enregistre les paiements reçus
6. Tous les calculs sont **automatiques** (montants, TTC, etc.)
7. Le workflow des statuts est **strict** (EN_ATTENTE → VALIDE → FACTURE → PAYE)
8. Les frais peuvent être créés **automatiquement** ou **manuellement**
9. Le chef financier **valide** tous les frais avant facturation
10. Le système gère les **retards de paiement** automatiquement

---

## 📋 11. INTÉGRATION DES TARIFS DE L'ANNEXE DU CONTRAT

### 11.1. Comment Intégrer les Tarifs de l'Annexe

L'annexe du contrat de recouvrement fournie contient des tarifs officiels qui doivent être intégrés dans le système `TarifCatalogue`. Voici comment procéder :

#### 11.1.1. Étape 1 : Identifier les Types de Tarifs

L'annexe contient **deux types de tarifs** :

1. **Frais fixes** : Montants en TND à payer d'avance
2. **Commissions** : Pourcentages calculés sur le montant recouvré

#### 11.1.2. Étape 2 : Ajouter les Frais Fixes dans TarifCatalogue

Pour chaque frais fixe de l'annexe, créer une entrée dans `TarifCatalogue` :

**Frais d'Ouverture de Dossier** :
- Phase : `CREATION`
- Catégorie : `OUVERTURE_DOSSIER`
- Tarif unitaire : `250 TND`
- Description : "Frais fixes de réception et d'ouverture de dossier"
- Date de début : Date de signature du contrat
- Actif : `true`

**Frais d'Enquête Précontentieuse** :
- Phase : `ENQUETE`
- Catégorie : `ENQUETE_PRECONTENTIEUSE`
- Tarif unitaire : `300 TND`
- Description : "Frais Enquête Précontentieuse"
- Date de début : Date de signature du contrat
- Actif : `true`

**Avance sur Frais de Recouvrement Judiciaire** :
- Phase : `JURIDIQUE`
- Catégorie : `AVANCE_RECOUVREMENT_JUDICIAIRE`
- Tarif unitaire : `1000 TND`
- Description : "Avance sur frais de recouvrement judiciaire"
- Date de début : Date de signature du contrat
- Actif : `true`

**Attestation de Carence** :
- Phase : `JURIDIQUE`
- Catégorie : `ATTESTATION_CARENCE`
- Tarif unitaire : `500 TND`
- Description : "Attestation de carence à la demande du mandant"
- Date de début : Date de signature du contrat
- Actif : `true`

**Relance Factures (< 6 mois)** :
- Phase : `AMIABLE`
- Catégorie : `RELANCE_FACTURE_MOINS_6_MOIS`
- Tarif unitaire : `0 TND` (Gratuit)
- Description : "Relance Factures datées de moins de 6 mois"
- Date de début : Date de signature du contrat
- Actif : `true`

#### 11.1.3. Étape 3 : Gérer les Commissions

Les commissions sont **différentes** des frais fixes car elles sont calculées sur le **montant recouvré**, pas sur une quantité fixe.

**Option A : Stocker les Taux de Commission dans Finance**

Ajouter des champs dans l'entité `Finance` pour stocker les taux :
- `tauxCommissionRelance` = 5%
- `tauxCommissionAmiable` = 12%
- `tauxCommissionJuridique` = 15%
- `tauxCommissionInterets` = 50%

**Option B : Créer des Entrées dans TarifCatalogue avec Type "COMMISSION"**

Créer des entrées spéciales dans `TarifCatalogue` :
- Phase : `AMIABLE`, Catégorie : `COMMISSION_RELANCE`, Tarif : `5` (pourcentage)
- Phase : `AMIABLE`, Catégorie : `COMMISSION_AMIABLE`, Tarif : `12` (pourcentage)
- Phase : `JURIDIQUE`, Catégorie : `COMMISSION_JURIDIQUE`, Tarif : `15` (pourcentage)
- Phase : `JURIDIQUE`, Catégorie : `COMMISSION_INTERETS`, Tarif : `50` (pourcentage)

**Recommandation** : Utiliser l'Option B pour centraliser tous les tarifs dans `TarifCatalogue`, mais avec un champ supplémentaire indiquant que c'est un pourcentage.

#### 11.1.4. Étape 4 : Calcul Automatique des Commissions

Lors du recouvrement d'un montant :

1. **Détecter la phase** : Le système identifie la phase (AMIABLE, JURIDIQUE, ou RELANCE)
2. **Récupérer le taux** : Le système cherche le taux de commission dans `TarifCatalogue` pour cette phase
3. **Calculer la commission** : `commission = montantRecouvre × (taux / 100)`
4. **Créer un FluxFrais** :
   - Phase = phase du recouvrement
   - Catégorie = `COMMISSION_AMIABLE` (ou `COMMISSION_JURIDIQUE`, etc.)
   - Montant = commission calculée
   - Statut = `EN_ATTENTE`
5. **Mettre à jour Finance** : Ajouter la commission au total

**Exemple** :
- Montant recouvré en phase amiable : 1000 TND
- Taux commission amiable : 12%
- Commission calculée : 1000 × 12% = 120 TND
- Un `FluxFrais` est créé avec montant = 120 TND

#### 11.1.5. Étape 5 : Mise à Jour de Finance lors de la Création du Dossier

Lors de la création d'un dossier :

1. **Créer Finance** automatiquement
2. **Créer FluxFrais pour ouverture** :
   - Chercher le tarif dans `TarifCatalogue` (phase=CREATION, catégorie=OUVERTURE_DOSSIER)
   - Si trouvé : Créer un `FluxFrais` avec montant = 250 TND
   - Si non trouvé : Utiliser la valeur par défaut de Finance (250 TND)
3. **Initialiser les taux de commission** :
   - Récupérer les taux depuis `TarifCatalogue` ou utiliser les valeurs par défaut
   - Stocker dans Finance pour référence

### 11.2. Workflow avec les Nouveaux Tarifs

#### 11.2.1. Création du Dossier

1. Dossier créé → Finance créée
2. **FluxFrais d'ouverture** créé automatiquement :
   - Montant : 250 TND (selon l'annexe)
   - Statut : `EN_ATTENTE`
3. Chef financier valide le frais d'ouverture → Statut passe à `VALIDE`

#### 11.2.2. Enquête Précontentieuse

1. Enquête créée → **FluxFrais d'enquête** créé automatiquement :
   - Phase : `ENQUETE`
   - Catégorie : `ENQUETE_PRECONTENTIEUSE`
   - Montant : 300 TND (selon l'annexe)
   - Statut : `EN_ATTENTE`
2. Chef financier valide → Statut passe à `VALIDE`

#### 11.2.3. Passage au Juridique

1. Dossier passe au juridique → **FluxFrais d'avance** créé automatiquement :
   - Phase : `JURIDIQUE`
   - Catégorie : `AVANCE_RECOUVREMENT_JUDICIAIRE`
   - Montant : 1000 TND (selon l'annexe)
   - Statut : `EN_ATTENTE`
2. Chef financier valide → Statut passe à `VALIDE`

#### 11.2.4. Recouvrement et Calcul des Commissions

1. **Montant recouvré** enregistré dans le dossier
2. **Commission calculée automatiquement** selon la phase :
   - Phase amiable : 12% du montant recouvré
   - Phase juridique : 15% du montant recouvré
   - Phase relance : 5% du montant recouvré
3. **FluxFrais de commission** créé :
   - Montant = commission calculée
   - Statut = `EN_ATTENTE`
4. Chef financier valide → Statut passe à `VALIDE`

#### 11.2.5. Intérêts Recouvrés

1. **Intérêts recouvrés** enregistrés séparément
2. **Commission intérêts** calculée : 50% des intérêts
3. **FluxFrais de commission intérêts** créé :
   - Montant = commission calculée
   - Statut = `EN_ATTENTE`
4. Chef financier valide → Statut passe à `VALIDE`

### 11.3. Exemple Complet avec les Tarifs de l'Annexe

**Scénario** : Dossier avec recouvrement amiable puis juridique

1. **Création du dossier** :
   - Frais d'ouverture : 250 TND (FluxFrais créé, validé)

2. **Enquête précontentieuse** :
   - Frais d'enquête : 300 TND (FluxFrais créé, validé)

3. **Recouvrement amiable** :
   - Montant recouvré : 2000 TND
   - Commission amiable : 2000 × 12% = 240 TND (FluxFrais créé, validé)

4. **Passage au juridique** :
   - Avance frais judiciaire : 1000 TND (FluxFrais créé, validé)

5. **Recouvrement juridique** :
   - Montant recouvré : 1500 TND
   - Commission juridique : 1500 × 15% = 225 TND (FluxFrais créé, validé)

6. **Intérêts recouvrés** :
   - Intérêts : 500 TND
   - Commission intérêts : 500 × 50% = 250 TND (FluxFrais créé, validé)

7. **Total facture** :
   - Frais d'ouverture : 250 TND
   - Frais d'enquête : 300 TND
   - Avance judiciaire : 1000 TND
   - Commission amiable : 240 TND
   - Commission juridique : 225 TND
   - Commission intérêts : 250 TND
   - **Total HT** : 2265 TND
   - **Total TTC** (avec 19% TVA) : 2265 × 1.19 = **2695.35 TND**

### 11.4. Points d'Attention

1. **Frais d'ouverture** : Payé **dès la création** du dossier (250 TND)
2. **Frais d'enquête** : Payé **lors de l'enquête** (300 TND)
3. **Avance judiciaire** : Payé **lors du passage au juridique** (1000 TND)
4. **Commissions** : Calculées **après le recouvrement** sur le montant effectivement recouvré
5. **Commission intérêts** : Calculée **séparément** sur les intérêts uniquement (50%)
6. **Relance < 6 mois** : **Gratuite** (0 TND) mais commission de 5% si recouvrement

---

**Fin du document**

