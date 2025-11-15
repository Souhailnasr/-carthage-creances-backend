# 🏦 Réorganisation Complète : Centralisation des Coûts dans Finance

## 🎯 Objectif

Centraliser **TOUS** les calculs de coûts dans l'entité Finance pour permettre au chef financier de construire la facture finale. Les interfaces de recouvrement amiable affichent uniquement le nombre d'occurrences des actions, sans les coûts.

---

## 📊 Architecture de la Solution

### Principe Fondamental

```
┌─────────────────────────────────────────────────────────┐
│                    RECOUVREMENT AMIABLE                  │
│  - Affiche : Nombre d'occurrences des actions          │
│  - N'affiche PAS : Les coûts                            │
│  - Actions enregistrées avec : type, date, nbOccurrences│
└─────────────────────────────────────────────────────────┘
                        │
                        │ (Liaison automatique)
                        ▼
┌─────────────────────────────────────────────────────────┐
│                        FINANCE                           │
│  - Calcule TOUS les coûts :                             │
│    • Coût création dossier                              │
│    • Coût gestion dossier                               │
│    • Coût actions (amiable + juridique)                │
│    • Frais avocat                                       │
│    • Frais huissier                                     │
│  - Facture finale complète                             │
└─────────────────────────────────────────────────────────┘
```

---

## 🔧 Phase 1 : Réorganisation Backend

### 📋 PROMPT 1 : Étendre l'Entité Finance avec Tous les Coûts

```
Dans le projet Spring Boot, modifiez l'entité Finance pour inclure tous les types de coûts :

Fichier : src/main/java/projet/carthagecreance_backend/Entity/Finance.java

Ajoutez les champs suivants :

1. Coûts de création et gestion :
   - fraisCreationDossier: Double
     - Coût fixe pour la création d'un dossier (ex: 50 TND)
   - fraisGestionDossier: Double
     - Coût mensuel de gestion du dossier (ex: 10 TND/mois)
   - dureeGestionMois: Integer
     - Durée de gestion en mois (calculée automatiquement)

2. Coûts des actions :
   - coutActionsAmiable: Double
     - Coût total des actions de recouvrement amiable (calculé automatiquement)
   - coutActionsJuridique: Double
     - Coût total des actions de recouvrement juridique (calculé automatiquement)
   - nombreActionsAmiable: Integer
     - Nombre total d'actions amiable (pour référence)
   - nombreActionsJuridique: Integer
     - Nombre total d'actions juridique (pour référence)

3. Méthodes de calcul à ajouter :
   - calculerCoutTotalActions(): Double
     - Somme de coutActionsAmiable + coutActionsJuridique
   - calculerCoutGestionTotal(): Double
     - fraisGestionDossier * dureeGestionMois
   - calculerFactureFinale(): Double
     - fraisCreationDossier + calculerCoutGestionTotal() + calculerCoutTotalActions() + fraisAvocat + fraisHuissier

4. Méthode utilitaire :
   - calculerDureeGestion(): Integer
     - Calcule automatiquement la durée en mois entre dateCreation du dossier et dateCloture (ou aujourd'hui)

IMPORTANT :
- Tous les champs de coût doivent être nullable (pour les dossiers existants)
- Les valeurs par défaut doivent être 0.0
- Utilisez @Builder.Default pour les valeurs par défaut
```

---

### 📋 PROMPT 2 : Créer Automatiquement Finance lors de la Création d'un Dossier

```
Dans le projet Spring Boot, modifiez DossierServiceImpl pour créer automatiquement une entité Finance lors de la création d'un dossier :

Fichier : src/main/java/projet/carthagecreance_backend/Service/Impl/DossierServiceImpl.java

Dans la méthode createDossier(), après la sauvegarde du dossier :

1. Créer automatiquement une Finance :
   - Créer une nouvelle entité Finance
   - Lier au dossier créé (OneToOne)
   - Initialiser :
     * devise = "TND" (par défaut)
     * dateOperation = LocalDate.now()
     * description = "Finance pour dossier " + numeroDossier
     * fraisCreationDossier = 50.0 (coût fixe configurable)
     * fraisGestionDossier = 10.0 (coût mensuel configurable)
     * dureeGestionMois = 0 (sera calculé plus tard)
     * Tous les autres coûts = 0.0

2. Sauvegarder la Finance :
   - Utiliser FinanceRepository.save()
   - Gérer les erreurs si la création échoue

3. Injecter FinanceRepository dans DossierServiceImpl :
   - @Autowired private FinanceRepository financeRepository;

IMPORTANT :
- La création de Finance doit être transactionnelle
- Si la création de Finance échoue, le dossier ne doit pas être créé (rollback)
- Utilisez @Transactional sur la méthode createDossier()
```

---

### 📋 PROMPT 3 : Lier Automatiquement les Actions à Finance

```
Dans le projet Spring Boot, modifiez ActionServiceImpl pour lier automatiquement les actions à Finance :

Fichier : src/main/java/projet/carthagecreance_backend/Service/Impl/ActionServiceImpl.java

Dans la méthode createAction() :

1. Vérifier que le dossier a une Finance :
   - Si le dossier n'a pas de Finance, créer une Finance automatiquement
   - Utiliser FinanceRepository.findByDossierId()

2. Lier l'action à la Finance :
   - action.setFinance(finance)
   - Sauvegarder l'action

3. Mettre à jour les coûts dans Finance :
   - Calculer le coût total de l'action (nbOccurrences * coutUnitaire)
   - Si typeRecouvrement = AMIABLE :
     * finance.setCoutActionsAmiable(finance.getCoutActionsAmiable() + coutTotal)
     * finance.setNombreActionsAmiable(finance.getNombreActionsAmiable() + 1)
   - Si typeRecouvrement = JURIDIQUE :
     * finance.setCoutActionsJuridique(finance.getCoutActionsJuridique() + coutTotal)
     * finance.setNombreActionsJuridique(finance.getNombreActionsJuridique() + 1)
   - Sauvegarder la Finance mise à jour

4. Injecter les dépendances nécessaires :
   - @Autowired private FinanceRepository financeRepository;
   - @Autowired private DossierRepository dossierRepository;

IMPORTANT :
- Cette logique doit être transactionnelle
- Si la mise à jour de Finance échoue, l'action ne doit pas être créée
- Gérer le cas où le dossier n'a pas encore de typeRecouvrement (NON_AFFECTE)
```

---

### 📋 PROMPT 4 : Mettre à Jour Finance lors de la Clôture d'un Dossier

```
Dans le projet Spring Boot, modifiez DossierServiceImpl pour mettre à jour Finance lors de la clôture :

Fichier : src/main/java/projet/carthagecreance_backend/Service/Impl/DossierServiceImpl.java

Dans la méthode cloturerDossier() :

1. Récupérer la Finance du dossier :
   - Utiliser FinanceRepository.findByDossierId()

2. Calculer et mettre à jour la durée de gestion :
   - Calculer la durée entre dateCreation et dateCloture
   - Convertir en mois (arrondi supérieur)
   - finance.setDureeGestionMois(dureeEnMois)

3. Calculer le coût total de gestion :
   - coutGestionTotal = fraisGestionDossier * dureeGestionMois
   - Mettre à jour si nécessaire

4. Sauvegarder la Finance mise à jour

IMPORTANT :
- La durée doit être calculée en mois complets (ex: 1.5 mois = 2 mois)
- Utiliser ChronoUnit.MONTHS.between() pour le calcul
```

---

### 📋 PROMPT 5 : Créer les Méthodes de Calcul dans FinanceService

```
Dans le projet Spring Boot, ajoutez les méthodes suivantes dans FinanceService et FinanceServiceImpl :

Fichier : src/main/java/projet/carthagecreance_backend/Service/FinanceService.java

Méthodes à ajouter :

1. calculerFactureFinale(Long dossierId): Double
   - Récupère la Finance du dossier
   - Calcule la facture finale complète
   - Retourne le total

2. getDetailFacture(Long dossierId): Map<String, Object>
   - Retourne le détail complet de la facture :
     * fraisCreationDossier
     * coutGestionTotal (fraisGestionDossier * dureeGestionMois)
     * coutActionsAmiable
     * coutActionsJuridique
     * fraisAvocat
     * fraisHuissier
     * totalFacture

3. recalculerCoutsDossier(Long dossierId): Finance
   - Recalcule tous les coûts d'un dossier :
     * Recalcule coutActionsAmiable (somme de toutes les actions amiable)
     * Recalcule coutActionsJuridique (somme de toutes les actions juridique)
     * Recalcule dureeGestionMois
     * Sauvegarde et retourne la Finance mise à jour

4. getStatistiquesCouts(): Map<String, Object>
   - Statistiques globales :
     * Total frais création
     * Total frais gestion
     * Total actions amiable
     * Total actions juridique
     * Total avocat
     * Total huissier
     * Grand total

5. getCoutsParDossier(Long dossierId): Map<String, Object>
   - Retourne tous les coûts d'un dossier spécifique
   - Inclut le détail de chaque type de coût

IMPORTANT :
- Toutes les méthodes doivent être transactionnelles si elles modifient des données
- Utilisez des requêtes optimisées pour les calculs
- Gérer les cas où Finance n'existe pas encore
```

---

### 📋 PROMPT 6 : Créer les Endpoints Finance pour le Chef Financier

```
Dans le projet Spring Boot, créez ou mettez à jour FinanceController :

Fichier : src/main/java/projet/carthagecreance_backend/Controller/FinanceController.java

Ajoutez les endpoints suivants :

1. GET /api/finances/dossier/{dossierId}/facture
   - Retourne le détail complet de la facture d'un dossier
   - Format : {detail: {...}, total: 1234.56}
   - Gère 404 si Finance n'existe pas

2. GET /api/finances/dossier/{dossierId}/detail
   - Retourne tous les coûts détaillés d'un dossier
   - Inclut : création, gestion, actions (amiable + juridique), avocat, huissier

3. POST /api/finances/dossier/{dossierId}/recalculer
   - Recalcule tous les coûts d'un dossier
   - Utile après modification d'actions
   - Retourne la Finance mise à jour

4. GET /api/finances/statistiques
   - Retourne les statistiques globales des coûts
   - Pour le dashboard du chef financier

5. GET /api/finances/dossiers-avec-couts
   - Retourne la liste de tous les dossiers avec leurs coûts
   - Pagination : page, size, sort
   - Filtres : date, montant min/max

6. GET /api/finances/factures-en-attente
   - Retourne les dossiers clôturés avec factures non finalisées
   - Pour le chef financier

7. PUT /api/finances/dossier/{dossierId}/finaliser-facture
   - Finalise une facture (marque comme facturée)
   - Ajoute une date de facturation
   - Retourne la Finance mise à jour

IMPORTANT :
- Tous les endpoints doivent gérer les erreurs (404, 400, 500)
- Ajouter des logs pour le débogage
- Messages d'erreur en français
- Validation des paramètres
```

---

### 📋 PROMPT 7 : Créer un Service de Calcul Automatique des Coûts

```
Dans le projet Spring Boot, créez un service dédié pour les calculs automatiques :

Fichier : src/main/java/projet/carthagecreance_backend/Service/CoutCalculationService.java

Interface avec les méthodes suivantes :

1. calculerCoutActionsAmiable(Long dossierId): Double
   - Récupère toutes les actions du dossier avec typeRecouvrement = AMIABLE
   - Calcule la somme : SUM(nbOccurrences * coutUnitaire)
   - Retourne le total

2. calculerCoutActionsJuridique(Long dossierId): Double
   - Récupère toutes les actions du dossier avec typeRecouvrement = JURIDIQUE
   - Calcule la somme : SUM(nbOccurrences * coutUnitaire)
   - Retourne le total

3. calculerDureeGestion(Long dossierId): Integer
   - Calcule la durée entre dateCreation et dateCloture (ou aujourd'hui)
   - Retourne le nombre de mois (arrondi supérieur)

4. calculerCoutGestion(Long dossierId): Double
   - Récupère fraisGestionDossier de Finance
   - Multiplie par dureeGestionMois
   - Retourne le total

5. recalculerTousLesCouts(Long dossierId): Finance
   - Recalcule tous les coûts d'un dossier
   - Met à jour la Finance
   - Retourne la Finance mise à jour

6. synchroniserActionsAvecFinance(Long dossierId): void
   - Synchronise toutes les actions d'un dossier avec sa Finance
   - Met à jour coutActionsAmiable et coutActionsJuridique
   - Met à jour nombreActionsAmiable et nombreActionsJuridique

IMPORTANT :
- Ce service doit être appelé automatiquement lors de :
  * Création d'une action
  * Modification d'une action
  * Suppression d'une action
  * Clôture d'un dossier
  * Passage au juridique
```

---

### 📋 PROMPT 8 : Modifier ActionService pour Ne Pas Calculer les Coûts dans Recouvrement Amiable

```
Dans le projet Spring Boot, modifiez ActionServiceImpl pour séparer la logique :

Fichier : src/main/java/projet/carthagecreance_backend/Service/Impl/ActionServiceImpl.java

1. Dans createAction() :
   - Enregistrer l'action avec type, dateAction, nbOccurrences, coutUnitaire, reponseDebiteur
   - Lier automatiquement à Finance
   - NE PAS exposer le calcul de coût dans les réponses pour recouvrement amiable

2. Créer une méthode séparée :
   - calculerCoutAction(Action action): Double
     - Calcul interne uniquement
     - Utilisé uniquement pour mettre à jour Finance

3. Modifier getActionsByDossier() :
   - Retourner les actions avec nbOccurrences
   - NE PAS inclure coutUnitaire dans la réponse si demandé depuis recouvrement amiable
   - Inclure coutUnitaire si demandé depuis Finance

IMPORTANT :
- Les coûts sont toujours calculés et stockés dans Finance
- Les interfaces recouvrement amiable ne voient que nbOccurrences
- Les interfaces Finance voient tous les détails
```

---

## 🎨 Phase 2 : Interfaces Frontend

### 📋 PROMPT 9 : Modifier le Service ActionService (Frontend) - Sans Coûts

```
Dans le projet Angular, modifiez ActionService pour ne pas exposer les coûts dans recouvrement amiable :

Fichier : src/app/services/action.service.ts

1. Créer une interface ActionRecouvrement (sans coûts) :
   - id, type, reponseDebiteur, dateAction, nbOccurrences
   - PAS de coutUnitaire ni totalCout

2. Créer une interface ActionFinance (avec coûts) :
   - Tous les champs y compris coutUnitaire et totalCout

3. Méthodes pour Recouvrement Amiable :
   - getActionsByDossierRecouvrement(dossierId: number): Observable<ActionRecouvrement[]>
     - GET /api/actions/dossier/{dossierId}
     - Retourne uniquement les champs nécessaires (sans coûts)
   
   - createActionRecouvrement(dossierId: number, action: ActionRecouvrement): Observable<ActionRecouvrement>
     - POST /api/actions
     - Le backend calcule automatiquement les coûts et les met dans Finance

4. Méthodes pour Finance :
   - getActionsByDossierFinance(dossierId: number): Observable<ActionFinance[]>
     - GET /api/finances/dossier/{dossierId}/actions
     - Retourne toutes les actions avec coûts détaillés

IMPORTANT :
- Les interfaces recouvrement amiable utilisent ActionRecouvrement
- Les interfaces finance utilisent ActionFinance
- Le backend gère automatiquement les coûts
```

---

### 📋 PROMPT 10 : Modifier le Composant Tableau Actions (Recouvrement Amiable)

```
Dans le projet Angular, modifiez le composant dossier-actions pour ne pas afficher les coûts :

Fichier : src/app/components/dossier-actions/dossier-actions.component.ts

Modifications :

1. Utiliser ActionRecouvrement au lieu de Action :
   - actions: ActionRecouvrement[] = []
   - Pas de propriété totalCost

2. Supprimer l'affichage des coûts :
   - Retirer la colonne "Coût unitaire"
   - Retirer la colonne "Coût total"
   - Retirer le badge "Coût total des actions"

3. Afficher uniquement :
   - Date Action
   - Type Action
   - Nombre d'occurrences (ex: "3 appels")
   - Réponse Débiteur
   - Actions (modifier/supprimer)

4. Statistiques affichées :
   - Nombre total d'actions
   - Nombre d'actions positives
   - Nombre d'actions négatives
   - PAS de coût total

5. Formulaire d'ajout :
   - Champ "Nombre d'occurrences" (ex: 2)
   - PAS de champ "Coût unitaire"
   - Le backend calcule automatiquement le coût selon le type d'action

IMPORTANT :
- Le formulaire ne demande plus coutUnitaire
- Le backend utilise des coûts prédéfinis par type d'action
- Ou le chef financier définit les coûts unitaires dans Finance
```

---

### 📋 PROMPT 11 : Créer le Service FinanceService (Frontend)

```
Dans le projet Angular, créez un service complet pour Finance :

Fichier : src/app/services/finance.service.ts

Méthodes requises :

1. getFinanceByDossier(dossierId: number): Observable<Finance>
   - GET /api/finances/dossier/{dossierId}
   - Retourne la Finance complète avec tous les coûts

2. getDetailFacture(dossierId: number): Observable<DetailFacture>
   - GET /api/finances/dossier/{dossierId}/facture
   - Retourne le détail complet de la facture

3. getCoutsParDossier(dossierId: number): Observable<CoutsDossier>
   - GET /api/finances/dossier/{dossierId}/detail
   - Retourne tous les coûts détaillés

4. recalculerCouts(dossierId: number): Observable<Finance>
   - POST /api/finances/dossier/{dossierId}/recalculer
   - Recalcule tous les coûts

5. getStatistiquesCouts(): Observable<StatistiquesCouts>
   - GET /api/finances/statistiques
   - Statistiques globales

6. getDossiersAvecCouts(params?: PaginationParams): Observable<Page<DossierAvecCouts>>
   - GET /api/finances/dossiers-avec-couts
   - Liste paginée avec coûts

7. getFacturesEnAttente(): Observable<Finance[]>
   - GET /api/finances/factures-en-attente
   - Dossiers clôturés non facturés

8. finaliserFacture(dossierId: number): Observable<Finance>
   - PUT /api/finances/dossier/{dossierId}/finaliser-facture
   - Finalise une facture

9. getActionsAvecCouts(dossierId: number): Observable<ActionFinance[]>
   - GET /api/finances/dossier/{dossierId}/actions
   - Toutes les actions avec coûts détaillés

IMPORTANT :
- Toutes les méthodes doivent gérer les erreurs
- Utiliser des interfaces TypeScript typées
- Messages d'erreur en français
```

---

### 📋 PROMPT 12 : Créer le Composant Dashboard Chef Financier

```
Dans le projet Angular, créez un composant dashboard complet pour le chef financier :

Fichier : src/app/components/chef-finance-dashboard/chef-finance-dashboard.component.ts

Fonctionnalités requises :

1. Vue d'ensemble des coûts :
   - Cards avec statistiques :
     * Total frais création (somme de tous les fraisCreationDossier)
     * Total frais gestion (somme de tous les coutGestionTotal)
     * Total actions amiable
     * Total actions juridique
     * Total frais avocat
     * Total frais huissier
     * Grand total

2. Liste des dossiers avec coûts :
   - Tableau avec colonnes :
     * Numéro dossier
     * Créancier
     * Montant créance
     * Coût création
     * Coût gestion
     * Coût actions amiable
     * Coût actions juridique
     * Frais avocat
     * Frais huissier
     * Total facture
     * Statut (facturé/non facturé)
     * Actions (voir détail, finaliser facture)

3. Filtres :
   - Par date de création
   - Par date de clôture
   - Par montant (min/max)
   - Par statut facturation
   - Recherche textuelle

4. Graphiques :
   - Répartition des coûts (pie chart)
   - Évolution des coûts dans le temps (line chart)
   - Coûts par type (bar chart)

5. Actions :
   - Voir détail facture d'un dossier
   - Finaliser une facture
   - Exporter en Excel/PDF
   - Recalculer les coûts d'un dossier

IMPORTANT :
- Utilisez FinanceService pour toutes les opérations
- Affichez les montants formatés en devise (TND)
- Responsive design
- Pagination pour la liste
```

---

### 📋 PROMPT 13 : Créer le Composant Détail Facture

```
Dans le projet Angular, créez un composant pour afficher le détail complet d'une facture :

Fichier : src/app/components/facture-detail/facture-detail.component.ts

Fonctionnalités requises :

1. Affichage du détail :
   - Section "Informations Dossier" :
     * Numéro, titre, créancier, débiteur
     * Date création, date clôture
     * Montant créance
   
   - Section "Coûts de Création et Gestion" :
     * Frais création dossier : XXX TND
     * Frais gestion (X mois × Y TND/mois) : ZZZ TND
     * Sous-total : AAA TND
   
   - Section "Coûts des Actions" :
     * Actions Recouvrement Amiable :
       - Liste des actions avec type, date, nbOccurrences, coutUnitaire, total
       - Total actions amiable : XXX TND
     * Actions Recouvrement Juridique :
       - Liste des actions avec type, date, nbOccurrences, coutUnitaire, total
       - Total actions juridique : XXX TND
     * Sous-total actions : YYY TND
   
   - Section "Frais Professionnels" :
     * Frais avocat : XXX TND
     * Frais huissier : XXX TND
     * Sous-total : YYY TND
   
   - Section "Total Facture" :
     * Grand total : ZZZ TND
     * Formaté en devise

2. Actions :
   - Bouton "Recalculer les Coûts"
   - Bouton "Finaliser la Facture"
   - Bouton "Imprimer/Exporter PDF"
   - Bouton "Retour"

3. Validation :
   - Vérifier que tous les coûts sont calculés
   - Afficher un avertissement si des coûts sont manquants
   - Proposer de recalculer si nécessaire

IMPORTANT :
- Utilisez FinanceService.getDetailFacture()
- Format professionnel pour impression
- Responsive design
```

---

### 📋 PROMPT 14 : Modifier le Dialog d'Ajout d'Action (Sans Coût)

```
Dans le projet Angular, modifiez le composant action-dialog :

Fichier : src/app/components/action-dialog/action-dialog.component.ts

Modifications :

1. Supprimer le champ "Coût Unitaire" du formulaire :
   - Retirer coutUnitaire du FormGroup
   - Le backend utilisera des coûts prédéfinis

2. Modifier l'interface ActionRecouvrement :
   - Pas de coutUnitaire
   - Seulement : type, dateAction, nbOccurrences, reponseDebiteur

3. Afficher un message informatif :
   - "Le coût sera calculé automatiquement selon le type d'action"
   - "Les coûts sont gérés dans le module Finance"

4. Supprimer l'affichage du "Coût Total" :
   - Pas de calcul ni d'affichage de coût dans le dialog

5. Validation :
   - type : required
   - dateAction : required, pas dans le futur
   - nbOccurrences : required, min: 1
   - reponseDebiteur : optional

IMPORTANT :
- Le formulaire est simplifié
- Le backend gère automatiquement les coûts
- Les coûts unitaires peuvent être configurés dans Finance
```

---

## 🔄 Phase 3 : Workflow Complet

### 📋 PROMPT 15 : Documenter le Workflow Complet de Traitement

```
Créez un document expliquant le workflow complet :

Fichier : WORKFLOW_COMPLET_TRAITEMENT.md

Workflow à documenter :

1. Création du Dossier :
   - Un dossier est créé
   - Une Finance est créée automatiquement
   - fraisCreationDossier = 50 TND (configurable)
   - fraisGestionDossier = 10 TND/mois (configurable)

2. Validation du Dossier :
   - Le dossier est validé
   - Finance reste inchangée

3. Affectation au Recouvrement Amiable :
   - Le dossier est affecté au chef amiable
   - Finance reste inchangée
   - Les actions commencent

4. Enregistrement d'Actions (Recouvrement Amiable) :
   - Le chef/agent enregistre une action :
     * Type : APPEL
     * Date : 15/11/2025
     * Nombre d'occurrences : 2
     * Réponse : POSITIVE
   - Le backend :
     * Enregistre l'action
     * Calcule automatiquement : coutUnitaire = 5 TND (configurable par type)
     * Lie l'action à Finance
     * Met à jour : coutActionsAmiable += 10 TND (2 × 5)
     * Met à jour : nombreActionsAmiable += 1

5. Passage au Recouvrement Juridique :
   - Si plusieurs réponses négatives
   - Le dossier passe au juridique
   - Finance reste la même
   - Les nouvelles actions juridique sont ajoutées à coutActionsJuridique

6. Clôture du Dossier :
   - Le dossier est clôturé
   - Finance est mise à jour :
     * dureeGestionMois = calculé (dateCreation → dateCloture)
     * coutGestionTotal = fraisGestionDossier × dureeGestionMois

7. Finalisation de la Facture (Chef Financier) :
   - Le chef financier consulte le détail
   - Vérifie tous les coûts
   - Peut recalculer si nécessaire
   - Finalise la facture
   - La facture est prête pour le client

IMPORTANT :
- Documenter chaque étape avec exemples
- Inclure les calculs détaillés
- Expliquer les règles métier
```

---

## 📊 Structure des Coûts dans Finance

### Détail de la Facture

```
┌─────────────────────────────────────────────────┐
│           FACTURE FINALE - DOSSIER #123         │
├─────────────────────────────────────────────────┤
│                                                 │
│ 1. COÛTS DE CRÉATION ET GESTION                │
│    • Frais création dossier      : 50.00 TND   │
│    • Frais gestion (3 mois)      : 30.00 TND   │
│    Sous-total                     : 80.00 TND  │
│                                                 │
│ 2. COÛTS DES ACTIONS                            │
│    • Actions Recouvrement Amiable :             │
│      - 2 appels × 5 TND          : 10.00 TND   │
│      - 1 email × 2 TND          : 2.00 TND    │
│      - 1 visite × 20 TND         : 20.00 TND   │
│      Sous-total amiable          : 32.00 TND   │
│                                                 │
│    • Actions Recouvrement Juridique :          │
│      - 1 lettre recommandée     : 15.00 TND   │
│      Sous-total juridique        : 15.00 TND   │
│                                                 │
│    Total actions                 : 47.00 TND   │
│                                                 │
│ 3. FRAIS PROFESSIONNELS                         │
│    • Frais avocat                 : 200.00 TND│
│    • Frais huissier                : 150.00 TND│
│    Sous-total                     : 350.00 TND │
│                                                 │
├─────────────────────────────────────────────────┤
│ TOTAL FACTURE                    : 477.00 TND  │
└─────────────────────────────────────────────────┘
```

---

## 🔧 Configuration des Coûts Unitaires

### 📋 PROMPT 16 : Créer un Système de Configuration des Coûts

```
Dans le projet Spring Boot, créez un système de configuration pour les coûts unitaires :

Fichier : src/main/java/projet/carthagecreance_backend/Config/CoutConfiguration.java

1. Créer une classe de configuration :
   - @Configuration
   - @ConfigurationProperties(prefix = "app.couts")
   - Propriétés :
     * fraisCreationDossier: Double = 50.0
     * fraisGestionDossierParMois: Double = 10.0
     * coutAppel: Double = 5.0
     * coutEmail: Double = 2.0
     * coutVisite: Double = 20.0
     * coutLettre: Double = 15.0
     * coutAutre: Double = 10.0

2. Créer un fichier application.properties :
   - app.couts.frais-creation-dossier=50.0
   - app.couts.frais-gestion-dossier-par-mois=10.0
   - app.couts.cout-appel=5.0
   - app.couts.cout-email=2.0
   - app.couts.cout-visite=20.0
   - app.couts.cout-lettre=15.0
   - app.couts.cout-autre=10.0

3. Utiliser dans ActionServiceImpl :
   - Injecter CoutConfiguration
   - Utiliser les coûts configurés lors de la création d'action

4. Créer un endpoint pour le chef financier :
   - GET /api/finances/config-couts
     - Retourne la configuration actuelle
   - PUT /api/finances/config-couts
     - Met à jour la configuration (nécessite rôle CHEF_FINANCE)

IMPORTANT :
- Les coûts peuvent être modifiés par le chef financier
- Les modifications s'appliquent aux nouvelles actions
- Les actions existantes conservent leur coût
```

---

## 📝 Checklist d'Implémentation

### ✅ Backend

- [ ] Étendre l'entité Finance avec tous les champs de coût
- [ ] Créer automatiquement Finance lors de la création d'un dossier
- [ ] Lier automatiquement les actions à Finance
- [ ] Mettre à jour Finance lors de la clôture
- [ ] Créer CoutCalculationService
- [ ] Créer les endpoints Finance pour le chef financier
- [ ] Créer le système de configuration des coûts
- [ ] Modifier ActionService pour ne pas exposer les coûts dans recouvrement amiable

### ✅ Frontend

- [ ] Créer FinanceService avec toutes les méthodes
- [ ] Modifier ActionService (séparer ActionRecouvrement et ActionFinance)
- [ ] Modifier le composant tableau actions (sans coûts)
- [ ] Modifier le dialog d'ajout action (sans coût)
- [ ] Créer le dashboard chef financier
- [ ] Créer le composant détail facture
- [ ] Créer les interfaces TypeScript

### ✅ Tests

- [ ] Tests unitaires pour les calculs de coût
- [ ] Tests d'intégration pour le workflow complet
- [ ] Tests E2E pour le chef financier

---

## 🎯 Résumé des Modifications

### Backend

1. **Finance** : Entité étendue avec tous les champs de coût
2. **Création automatique** : Finance créée avec chaque dossier
3. **Liaison automatique** : Actions liées à Finance automatiquement
4. **Calculs automatiques** : Tous les coûts calculés dans Finance
5. **Endpoints Finance** : API complète pour le chef financier

### Frontend

1. **Recouvrement Amiable** : Affiche uniquement nbOccurrences (pas de coûts)
2. **Finance** : Affiche tous les coûts détaillés
3. **Dashboard Chef Financier** : Vue complète avec statistiques
4. **Détail Facture** : Affichage professionnel de la facture

---

**Cette réorganisation garantit une séparation claire des responsabilités et permet au chef financier d'avoir une vue complète pour construire la facture finale.**

