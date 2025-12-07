# 📋 Guide Complet d'Intégration Frontend

## 🎯 Objectif

Ce guide fournit toutes les informations nécessaires pour intégrer correctement toutes les fonctionnalités backend dans le frontend Angular, en s'assurant que tous les APIs sont consommés convenablement dans les interfaces appropriées.

---

## 📊 Table des Matières

1. [APIs Disponibles](#apis-disponibles)
2. [Mapping Dashboard → Endpoints](#mapping-dashboard--endpoints)
3. [Prompts d'Intégration](#prompts-dintégration)
4. [Exemples de Code](#exemples-de-code)
5. [Checklist d'Intégration](#checklist-dintégration)

---

## 🔌 APIs Disponibles

### 1. Statistiques Globales

**Endpoint :** `GET /api/statistiques/globales`  
**Autorisation :** `SUPER_ADMIN`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :**
```json
{
  "totalDossiers": 100,
  "dossiersEnCours": 50,
  "dossiersClotures": 30,
  "totalEnquetes": 80,
  "enquetesCompletees": 60,
  "enquetesEnCours": 20,
  "montantRecouvrePhaseAmiable": 500000.00,
  "montantRecouvrePhaseJuridique": 300000.00,
  "montantRecouvre": 800000.00,
  "montantEnCours": 200000.00,
  ...
}
```

**Utilisation Frontend :**
- Dashboard SuperAdmin - Statistiques Globales
- Tableau de bord principal

---

### 2. Statistiques Financières

**Endpoint :** `GET /api/statistiques/financieres`  
**Autorisation :** `SUPER_ADMIN`, `CHEF_DEPARTEMENT_FINANCE`  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :**
```json
{
  "montantRecouvrePhaseAmiable": 500000.00,
  "montantRecouvrePhaseJuridique": 300000.00,
  "montantRecouvre": 800000.00,
  "montantEnCours": 200000.00,
  "totalFraisEngages": 50000.00,
  "fraisRecuperes": 40000.00,
  "netGenere": 750000.00,
  "totalFactures": 150,
  "facturesPayees": 100,
  "facturesEnAttente": 50,
  "totalPaiements": 200,
  "paiementsCeMois": 25
}
```

**Utilisation Frontend :**
- Dashboard SuperAdmin - Supervision Finance
- Dashboard Chef Finance

---

### 3. Statistiques Recouvrement par Phase (Global)

**Endpoint :** `GET /api/statistiques/recouvrement-par-phase`  
**Autorisation :** `SUPER_ADMIN`, `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :**
```json
{
  "montantRecouvrePhaseAmiable": 500000.00,
  "montantRecouvrePhaseJuridique": 300000.00,
  "montantRecouvreTotal": 800000.00,
  "dossiersAvecRecouvrementAmiable": 45,
  "dossiersAvecRecouvrementJuridique": 30,
  "tauxRecouvrementAmiable": 62.5,
  "tauxRecouvrementJuridique": 37.5,
  "tauxRecouvrementTotal": 100.0,
  "montantTotalCreances": 800000.00
}
```

**Utilisation Frontend :**
- Dashboard SuperAdmin - Supervision Recouvrement Amiable
- Dashboard SuperAdmin - Supervision Recouvrement Juridique

---

### 4. Statistiques Recouvrement par Phase (Département)

**Endpoint :** `GET /api/statistiques/recouvrement-par-phase/departement`  
**Autorisation :** `CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE`, `CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE`, `CHEF_DEPARTEMENT_FINANCE`, `SUPER_ADMIN`  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :** (Même structure que l'endpoint global, mais filtré par département)

**Utilisation Frontend :**
- Dashboard Chef Amiable
- Dashboard Chef Juridique
- Dashboard Chef Finance (optionnel)

---

### 5. Blocage/Déblocage Utilisateur

**Endpoint Activer :** `PUT /api/admin/utilisateurs/{id}/activer`  
**Endpoint Désactiver :** `PUT /api/admin/utilisateurs/{id}/desactiver`  
**Autorisation :** `SUPER_ADMIN` uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :**
```json
{
  "id": 1,
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "actif": true,
  "roleUtilisateur": "AGENT_DOSSIER",
  ...
}
```

**Utilisation Frontend :**
- Page de gestion des utilisateurs
- Tableau des utilisateurs avec boutons d'action

---

### 6. Détail de Facture

**Endpoint :** `GET /api/finances/dossier/{dossierId}/detail-facture`  
**Autorisation :** Selon le rôle (généralement `CHEF_DEPARTEMENT_FINANCE`, `SUPER_ADMIN`)  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :**
```json
{
  "fraisCreationDossier": 250.00,
  "fraisEnquete": 300.00,
  "coutGestionTotal": 1000.00,
  "coutActionsAmiable": 500.00,
  "coutActionsJuridique": 800.00,
  "fraisAvocat": 1200.00,
  "fraisHuissier": 600.00,
  "commissionAmiable": 600.00,
  "commissionJuridique": 450.00,
  "commissionInterets": 250.00,
  "totalHT": 5350.00,
  "tva": 1016.50,
  "totalTTC": 6366.50,
  "totalFacture": 6366.50
}
```

**Utilisation Frontend :**
- Page de détail de facture
- Affichage des commissions

---

## 📊 Mapping Dashboard → Endpoints

### Dashboard SuperAdmin

| Section | Endpoint | Champs à Afficher |
|---------|----------|-------------------|
| Statistiques Globales | `GET /api/statistiques/globales` | `montantRecouvrePhaseAmiable`, `montantRecouvrePhaseJuridique`, `montantRecouvre`, `enquetesEnCours`, etc. |
| Supervision Recouvrement Amiable | `GET /api/statistiques/recouvrement-par-phase` | `montantRecouvrePhaseAmiable`, `dossiersAvecRecouvrementAmiable`, `tauxRecouvrementAmiable` |
| Supervision Recouvrement Juridique | `GET /api/statistiques/recouvrement-par-phase` | `montantRecouvrePhaseJuridique`, `dossiersAvecRecouvrementJuridique`, `tauxRecouvrementJuridique` |
| Supervision Finance | `GET /api/statistiques/financieres` | Tous les champs, avec focus sur les montants par phase |

### Dashboard Chef Amiable

| Section | Endpoint | Champs à Afficher |
|---------|----------|-------------------|
| Recouvrement Amiable | `GET /api/statistiques/recouvrement-par-phase/departement` | `montantRecouvrePhaseAmiable` (prioritaire), `dossiersAvecRecouvrementAmiable`, `tauxRecouvrementAmiable` |
| Vue d'Ensemble | `GET /api/statistiques/recouvrement-par-phase/departement` | `montantRecouvreTotal`, `montantTotalCreances`, graphique comparatif |

### Dashboard Chef Juridique

| Section | Endpoint | Champs à Afficher |
|---------|----------|-------------------|
| Recouvrement Juridique | `GET /api/statistiques/recouvrement-par-phase/departement` | `montantRecouvrePhaseJuridique` (prioritaire), `dossiersAvecRecouvrementJuridique`, `tauxRecouvrementJuridique` |
| Vue d'Ensemble | `GET /api/statistiques/recouvrement-par-phase/departement` | `montantRecouvreTotal`, `montantTotalCreances`, graphique comparatif |

### Dashboard Chef Finance

| Section | Endpoint | Champs à Afficher |
|---------|----------|-------------------|
| Recouvrement par Phase | `GET /api/statistiques/financieres` | `montantRecouvrePhaseAmiable`, `montantRecouvrePhaseJuridique`, graphique comparatif |
| Résumé Financier | `GET /api/statistiques/financieres` | `montantRecouvre`, `montantEnCours`, `totalFraisEngages`, `fraisRecuperes`, `netGenere` |
| Factures et Paiements | `GET /api/statistiques/financieres` | `totalFactures`, `facturesPayees`, `facturesEnAttente`, `totalPaiements`, `paiementsCeMois` |

### Page Gestion Utilisateurs

| Fonctionnalité | Endpoint | Action |
|----------------|----------|--------|
| Activer Utilisateur | `PUT /api/admin/utilisateurs/{id}/activer` | Débloquer un utilisateur |
| Désactiver Utilisateur | `PUT /api/admin/utilisateurs/{id}/desactiver` | Bloquer un utilisateur |

---

## 📋 Prompts d'Intégration

### Prompt Principal : Intégration Complète

```
Je dois intégrer toutes les nouvelles fonctionnalités backend dans mon application Angular de gestion de recouvrement de créances.

**Contexte :**
- Backend Spring Boot avec endpoints REST
- Frontend Angular avec TypeScript et Angular Material
- Plusieurs dashboards selon les rôles (SuperAdmin, Chef Amiable, Chef Juridique, Chef Finance)

**Fonctionnalités à intégrer :**

1. **Statistiques Manquantes :**
   - Total Factures, Factures Payées, Factures en Attente
   - Total Paiements, Paiements ce Mois
   - Enquêtes en Cours (corriger l'affichage négatif)

2. **Montants Recouvrés par Phase :**
   - Montant recouvré en phase amiable
   - Montant recouvré en phase juridique
   - Affichage dans tous les dashboards appropriés

3. **Blocage/Déblocage Utilisateur :**
   - Bouton dans la page de gestion des utilisateurs
   - Confirmation avant action
   - Mise à jour du tableau après action

4. **Commissions :**
   - Affichage dans le détail de facture
   - Commission amiable, juridique, intérêts

5. **Tarifs Automatiques :**
   - Affichage des tarifs créés automatiquement
   - Badge "Automatique" pour les tarifs automatiques

**Endpoints Backend Disponibles :**

1. `GET /api/statistiques/globales` - Retourne `montantRecouvrePhaseAmiable`, `montantRecouvrePhaseJuridique`, `enquetesEnCours`
2. `GET /api/statistiques/financieres` - Retourne `montantRecouvrePhaseAmiable`, `montantRecouvrePhaseJuridique`, `totalFactures`, `facturesPayees`, `facturesEnAttente`, `totalPaiements`, `paiementsCeMois`
3. `GET /api/statistiques/recouvrement-par-phase` - Retourne statistiques détaillées par phase
4. `GET /api/statistiques/recouvrement-par-phase/departement` - Retourne statistiques par phase filtrées par département
5. `PUT /api/admin/utilisateurs/{id}/activer` - Active un utilisateur
6. `PUT /api/admin/utilisateurs/{id}/desactiver` - Désactive un utilisateur
7. `GET /api/finances/dossier/{dossierId}/detail-facture` - Retourne `commissionAmiable`, `commissionJuridique`, `commissionInterets`

**Tâches :**

1. **Services Angular :**
   - Créer/modifier `statistique.service.ts` avec toutes les méthodes nécessaires
   - Créer/modifier `utilisateur.service.ts` avec méthodes `activerUtilisateur()` et `desactiverUtilisateur()`
   - Créer/modifier `facture.service.ts` avec méthode `getDetailFacture()`

2. **Interfaces TypeScript :**
   - Définir `StatistiquesGlobales` avec tous les champs
   - Définir `StatistiquesFinancieres` avec tous les champs
   - Définir `StatistiquesRecouvrementParPhase` avec tous les champs
   - Définir `DetailFactureDTO` avec les champs de commission
   - Définir `Utilisateur` avec champ `actif`

3. **Composants Dashboard :**
   - Dashboard SuperAdmin : Intégrer les montants par phase dans toutes les sections
   - Dashboard Chef Amiable : Afficher montant recouvré amiable (prioritaire)
   - Dashboard Chef Juridique : Afficher montant recouvré juridique (prioritaire)
   - Dashboard Chef Finance : Afficher montants par phase avec graphique

4. **Composants Fonctionnels :**
   - Page Gestion Utilisateurs : Ajouter boutons blocage/déblocage
   - Page Détail Facture : Afficher section commissions
   - Page Validation Tarifs : Afficher badge "Automatique" pour les tarifs automatiques

5. **Composant Réutilisable (Optionnel) :**
   - Créer `montants-par-phase.component.ts` pour afficher les montants par phase de manière réutilisable

**Exigences :**

- Utiliser Angular Material pour tous les composants UI
- Utiliser Chart.js ou Angular Material Charts pour les graphiques
- Formater les montants avec le pipe `currency` d'Angular
- Afficher un indicateur de chargement pendant la récupération des données
- Gérer les erreurs avec des messages utilisateur appropriés
- Utiliser des observables RxJS (pas de promesses)
- Design professionnel et moderne
- Responsive design (adaptation mobile/tablette/desktop)
- Utiliser des couleurs cohérentes (vert pour amiable, bleu pour juridique)

**Références :**
- Voir `PROMPTS_FRONTEND_AMELIORATIONS.md` pour des prompts détaillés par fonctionnalité
- Voir `PROMPTS_FRONTEND_MONTANTS_RECOUVRES_PAR_PHASE.md` pour des prompts spécifiques aux montants par phase
- Voir `DOCUMENT_VERIFICATION_ENDPOINTS_MONTANTS_PAR_PHASE.md` pour les détails des endpoints
```

---

## 📝 Exemples de Code TypeScript

### Service Statistique

```typescript
// statistique.service.ts

export interface StatistiquesGlobales {
  totalDossiers: number;
  dossiersEnCours: number;
  montantRecouvrePhaseAmiable: number;
  montantRecouvrePhaseJuridique: number;
  montantRecouvre: number;
  enquetesEnCours: number;
  // ... autres champs
}

export interface StatistiquesFinancieres {
  montantRecouvrePhaseAmiable: number;
  montantRecouvrePhaseJuridique: number;
  montantRecouvre: number;
  totalFactures: number;
  facturesPayees: number;
  facturesEnAttente: number;
  totalPaiements: number;
  paiementsCeMois: number;
  // ... autres champs
}

export interface StatistiquesRecouvrementParPhase {
  montantRecouvrePhaseAmiable: number;
  montantRecouvrePhaseJuridique: number;
  montantRecouvreTotal: number;
  dossiersAvecRecouvrementAmiable: number;
  dossiersAvecRecouvrementJuridique: number;
  tauxRecouvrementAmiable: number;
  tauxRecouvrementJuridique: number;
  // ... autres champs
}

@Injectable({
  providedIn: 'root'
})
export class StatistiqueService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/statistiques';

  constructor(private http: HttpClient) {}

  getStatistiquesGlobales(): Observable<StatistiquesGlobales> {
    return this.http.get<StatistiquesGlobales>(`${this.apiUrl}/globales`);
  }

  getStatistiquesFinancieres(): Observable<StatistiquesFinancieres> {
    return this.http.get<StatistiquesFinancieres>(`${this.apiUrl}/financieres`);
  }

  getStatistiquesRecouvrementParPhase(): Observable<StatistiquesRecouvrementParPhase> {
    return this.http.get<StatistiquesRecouvrementParPhase>(`${this.apiUrl}/recouvrement-par-phase`);
  }

  getStatistiquesRecouvrementParPhaseDepartement(): Observable<StatistiquesRecouvrementParPhase> {
    return this.http.get<StatistiquesRecouvrementParPhase>(`${this.apiUrl}/recouvrement-par-phase/departement`);
  }
}
```

### Service Utilisateur

```typescript
// utilisateur.service.ts

export interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  actif: boolean;
  roleUtilisateur: string;
  // ... autres champs
}

@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/admin/utilisateurs';

  constructor(private http: HttpClient) {}

  activerUtilisateur(id: number): Observable<Utilisateur> {
    return this.http.put<Utilisateur>(`${this.apiUrl}/${id}/activer`, {});
  }

  desactiverUtilisateur(id: number): Observable<Utilisateur> {
    return this.http.put<Utilisateur>(`${this.apiUrl}/${id}/desactiver`, {});
  }
}
```

### Composant Dashboard avec Montants par Phase

```typescript
// dashboard-example.component.ts

export class DashboardExampleComponent implements OnInit {
  montantAmiable: number = 0;
  montantJuridique: number = 0;
  montantTotal: number = 0;
  loading: boolean = false;

  constructor(private statistiqueService: StatistiqueService) {}

  ngOnInit(): void {
    this.loadStatistiques();
  }

  loadStatistiques(): void {
    this.loading = true;
    this.statistiqueService.getStatistiquesRecouvrementParPhase()
      .pipe(
        finalize(() => this.loading = false),
        catchError(error => {
          console.error('Erreur lors du chargement des statistiques:', error);
          return of({
            montantRecouvrePhaseAmiable: 0,
            montantRecouvrePhaseJuridique: 0,
            montantRecouvreTotal: 0
          });
        })
      )
      .subscribe(data => {
        this.montantAmiable = data.montantRecouvrePhaseAmiable ?? 0;
        this.montantJuridique = data.montantRecouvrePhaseJuridique ?? 0;
        this.montantTotal = data.montantRecouvreTotal ?? 0;
      });
  }
}
```

```html
<!-- dashboard-example.component.html -->

<mat-card class="montant-amiable">
  <mat-card-header>
    <mat-card-title>Recouvrement Amiable</mat-card-title>
  </mat-card-header>
  <mat-card-content>
    <h2>{{ montantAmiable | currency:'TND':'symbol':'1.2-2' }}</h2>
    <p>Taux: {{ (montantAmiable / montantTotal * 100) | number:'1.2-2' }}%</p>
  </mat-card-content>
</mat-card>

<mat-card class="montant-juridique">
  <mat-card-header>
    <mat-card-title>Recouvrement Juridique</mat-card-title>
  </mat-card-header>
  <mat-card-content>
    <h2>{{ montantJuridique | currency:'TND':'symbol':'1.2-2' }}</h2>
    <p>Taux: {{ (montantJuridique / montantTotal * 100) | number:'1.2-2' }}%</p>
  </mat-card-content>
</mat-card>
```

---

## ✅ Checklist d'Intégration Complète

### Phase 1 : Services et Interfaces

- [ ] Créer/modifier `statistique.service.ts` avec toutes les méthodes
- [ ] Créer/modifier `utilisateur.service.ts` avec méthodes blocage/déblocage
- [ ] Créer/modifier `facture.service.ts` avec méthode `getDetailFacture()`
- [ ] Définir toutes les interfaces TypeScript nécessaires

### Phase 2 : Statistiques

- [ ] Intégrer les statistiques manquantes (factures, paiements, enquêtes)
- [ ] Afficher les montants recouvrés par phase dans les statistiques globales
- [ ] Corriger l'affichage des enquêtes en cours

### Phase 3 : Dashboards

- [ ] Dashboard SuperAdmin - Section Recouvrement Amiable
- [ ] Dashboard SuperAdmin - Section Recouvrement Juridique
- [ ] Dashboard SuperAdmin - Section Finance
- [ ] Dashboard Chef Amiable - Montants par phase
- [ ] Dashboard Chef Juridique - Montants par phase
- [ ] Dashboard Chef Finance - Montants par phase

### Phase 4 : Fonctionnalités

- [ ] Bouton blocage/déblocage utilisateur
- [ ] Affichage des commissions dans le détail de facture
- [ ] Affichage des tarifs automatiques

### Phase 5 : Interface

- [ ] Design cohérent et professionnel
- [ ] Responsive design
- [ ] Gestion des erreurs
- [ ] Indicateurs de chargement
- [ ] Graphiques pour visualiser les données

---

## 📚 Documents de Référence

### Backend

- `DOCUMENT_CORRECTIONS_BACKEND_COMPLET.md` - Toutes les corrections backend
- `DOCUMENT_VERIFICATION_ENDPOINTS_MONTANTS_PAR_PHASE.md` - Vérification des endpoints

### Frontend

- `PROMPTS_FRONTEND_AMELIORATIONS.md` - 7 prompts généraux
- `PROMPTS_FRONTEND_MONTANTS_RECOUVRES_PAR_PHASE.md` - 8 prompts spécifiques montants par phase
- `GUIDE_INTEGRATION_FRONTEND_COMPLET.md` - Ce document

---

**Date :** 2025-01-05  
**Status :** ✅ Guide complet - Prêt pour intégration frontend

