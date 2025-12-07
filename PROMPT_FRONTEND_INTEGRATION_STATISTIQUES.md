# 📋 Prompt Frontend : Intégration Complète des Statistiques

## 🎯 Objectif

Intégrer complètement les statistiques dans le frontend pour qu'elles soient affichées en temps réel dans les interfaces appropriées, avec mise à jour automatique après chaque action.

---

## 📡 APIs Disponibles

### 1. Statistiques Globales (SuperAdmin)

**Endpoint :** `GET /api/statistiques/globales`  
**Accès :** SUPER_ADMIN uniquement  
**Headers :** `Authorization: Bearer {token}`

**Réponse JSON :**
```json
{
  "totalDossiers": 10,
  "dossiersEnCours": 5,
  "dossiersValides": 8,
  "dossiersRejetes": 1,
  "dossiersClotures": 2,
  "dossiersCreesCeMois": 3,
  "dossiersPhaseCreation": 2,
  "dossiersPhaseEnquete": 3,
  "dossiersPhaseAmiable": 4,
  "dossiersPhaseJuridique": 1,
  "totalEnquetes": 5,
  "enquetesCompletees": 3,
  "actionsAmiables": 12,
  "actionsAmiablesCompletees": 8,
  "documentsHuissierCrees": 15,
  "documentsHuissierCompletes": 10,
  "actionsHuissierCrees": 7,
  "actionsHuissierCompletes": 4,
  "audiencesTotales": 6,
  "audiencesProchaines": 2,
  "tachesCompletees": 20,
  "tachesEnCours": 5,
  "tachesEnRetard": 2,
  "tauxReussiteGlobal": 20.0,
  "montantRecouvre": 50000.0,
  "montantEnCours": 80000.0
}
```

### 2. Statistiques par Période (SuperAdmin)

**Endpoint :** `GET /api/statistiques/periode?dateDebut=2025-12-01&dateFin=2025-12-31`  
**Accès :** SUPER_ADMIN uniquement

### 3. Statistiques Département (Chef)

**Endpoint :** `GET /api/statistiques/departement`  
**Accès :** CHEF_DEPARTEMENT_* uniquement

### 4. Statistiques Agent

**Endpoint :** `GET /api/statistiques/mes-dossiers`  
**Accès :** AGENT_* uniquement

### 5. Recalcul Manuel (SuperAdmin)

**Endpoint :** `POST /api/statistiques/recalculer`  
**Accès :** SUPER_ADMIN uniquement  
**Action :** Force le recalcul immédiat des statistiques

---

## 🔄 Logique d'Affichage

### Détection du Rôle

Le frontend doit détecter le rôle de l'utilisateur et appeler l'endpoint approprié :

```typescript
// Exemple de logique
const userRole = this.authService.getUserRole();

if (userRole === 'SUPER_ADMIN') {
    // Appeler /api/statistiques/globales
} else if (userRole.startsWith('CHEF_')) {
    // Appeler /api/statistiques/departement
} else if (userRole.startsWith('AGENT_')) {
    // Appeler /api/statistiques/mes-dossiers
}
```

---

## 📊 Mapping Statistiques → Interface

### Tableau de Correspondance

| Clé API | Label Interface | Emplacement Suggéré |
|---------|-----------------|---------------------|
| `totalDossiers` | "Total Dossiers" | Dashboard principal |
| `dossiersEnCours` | "Dossiers en Cours" | Dashboard principal |
| `dossiersValides` | "Dossiers Validés" | Dashboard principal |
| `dossiersRejetes` | "Dossiers Rejetés" | Dashboard principal |
| `dossiersClotures` | "Dossiers Clôturés" | Dashboard principal |
| `dossiersCreesCeMois` | "Dossiers Créés ce Mois" | Dashboard principal |
| `dossiersPhaseCreation` | "Phase Création" | Vue par phase |
| `dossiersPhaseEnquete` | "Phase Enquête" | Vue par phase |
| `dossiersPhaseAmiable` | "Phase Amiable" | Vue par phase |
| `dossiersPhaseJuridique` | "Phase Juridique" | Vue par phase |
| `totalEnquetes` | "Total Enquêtes" | Dashboard enquêtes |
| `enquetesCompletees` | "Enquêtes Complétées" | Dashboard enquêtes |
| `actionsAmiables` | "Actions Amiables" | Dashboard actions |
| `actionsAmiablesCompletees` | "Actions Complétées" | Dashboard actions |
| `documentsHuissierCrees` | "Documents Créés" | Dashboard huissier |
| `documentsHuissierCompletes` | "Documents Complétés" | Dashboard huissier |
| `actionsHuissierCrees` | "Actions Créées" | Dashboard huissier |
| `actionsHuissierCompletes` | "Actions Complétées" | Dashboard huissier |
| `audiencesTotales` | "Total Audiences" | Dashboard audiences |
| `audiencesProchaines` | "Audiences Prochaines" | Dashboard audiences |
| `tachesCompletees` | "Tâches Complétées" | Dashboard tâches |
| `tachesEnCours` | "Tâches en Cours" | Dashboard tâches |
| `tachesEnRetard` | "Tâches en Retard" | Dashboard tâches |
| `tauxReussiteGlobal` | "Taux de Réussite" | Dashboard principal (pourcentage) |
| `montantRecouvre` | "Montant Recouvré" | Dashboard financier |
| `montantEnCours` | "Montant en Cours" | Dashboard financier |

---

## 🔄 Mise à Jour Automatique

### Stratégie de Rafraîchissement

Le frontend doit rafraîchir les statistiques :

1. **Au chargement de la page** : Appel initial de l'API
2. **Après chaque action importante** :
   - Création de dossier → Rafraîchir
   - Création d'enquête → Rafraîchir
   - Création d'action → Rafraîchir
   - Validation d'enquête → Rafraîchir
   - etc.
3. **Rafraîchissement périodique** : Toutes les 30 secondes (optionnel)

### Exemple d'Implémentation

```typescript
// Service de statistiques
class StatistiqueService {
  private statsSubject = new BehaviorSubject<Stats | null>(null);
  public stats$ = this.statsSubject.asObservable();
  
  loadStats(): Observable<Stats> {
    const endpoint = this.getEndpointByRole();
    return this.http.get<Stats>(endpoint).pipe(
      tap(stats => this.statsSubject.next(stats))
    );
  }
  
  refreshAfterAction(): void {
    // Attendre 1 seconde pour laisser le temps au backend de recalculer
    setTimeout(() => {
      this.loadStats().subscribe();
    }, 1000);
  }
}

// Dans les composants
onDossierCreated() {
  this.dossierService.createDossier(data).subscribe(() => {
    // Rafraîchir les statistiques après création
    this.statistiqueService.refreshAfterAction();
  });
}
```

---

## 🎨 Interfaces à Créer/Modifier

### 1. Dashboard Principal (SuperAdmin)

**Composant :** `dashboard-admin.component.ts`

**Statistiques à afficher :**
- Total Dossiers (grande carte)
- Dossiers en Cours (carte)
- Dossiers Validés (carte)
- Dossiers Clôturés (carte)
- Dossiers Créés ce Mois (carte)
- Taux de Réussite (graphique ou pourcentage)
- Montant Recouvré (carte financière)
- Montant en Cours (carte financière)

**Layout suggéré :**
```
┌─────────────────────────────────────────┐
│  Total Dossiers: 10                     │
│  Dossiers en Cours: 5                   │
│  Dossiers Validés: 8                    │
│  Dossiers Clôturés: 2                   │
│  Dossiers Créés ce Mois: 3              │
│  Taux de Réussite: 20%                  │
│  Montant Recouvré: 50,000 TND           │
│  Montant en Cours: 80,000 TND           │
└─────────────────────────────────────────┘
```

### 2. Dashboard Enquêtes

**Composant :** `dashboard-enquetes.component.ts`

**Statistiques à afficher :**
- Total Enquêtes (carte)
- Enquêtes Complétées (carte)
- Taux de Complétion (pourcentage)

### 3. Dashboard Actions

**Composant :** `dashboard-actions.component.ts`

**Statistiques à afficher :**
- Actions Amiables (carte)
- Actions Complétées (carte)
- Taux de Complétion (pourcentage)

### 4. Dashboard Huissier

**Composant :** `dashboard-huissier.component.ts`

**Statistiques à afficher :**
- Documents Créés (carte)
- Documents Complétés (carte)
- Actions Créées (carte)
- Actions Complétées (carte)

### 5. Dashboard Audiences

**Composant :** `dashboard-audiences.component.ts`

**Statistiques à afficher :**
- Total Audiences (carte)
- Audiences Prochaines (carte - prochaines 7 jours)

### 6. Dashboard Tâches

**Composant :** `dashboard-taches.component.ts`

**Statistiques à afficher :**
- Tâches Complétées (carte)
- Tâches en Cours (carte)
- Tâches en Retard (carte - alerte si > 0)

### 7. Vue par Phase

**Composant :** `dashboard-phases.component.ts`

**Statistiques à afficher :**
- Phase Création (carte)
- Phase Enquête (carte)
- Phase Amiable (carte)
- Phase Juridique (carte)

**Layout suggéré (graphique) :**
```
┌─────────────────────────────────────────┐
│  Phase Création: 2                      │
│  Phase Enquête: 3                       │
│  Phase Amiable: 4                       │
│  Phase Juridique: 1                     │
│  [Graphique en barres ou camembert]     │
└─────────────────────────────────────────┘
```

---

## 🔧 Service Angular à Créer

### Fichier : `statistique.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AuthService } from './auth.service';

export interface StatistiquesGlobales {
  totalDossiers: number;
  dossiersEnCours: number;
  dossiersValides: number;
  dossiersRejetes: number;
  dossiersClotures: number;
  dossiersCreesCeMois: number;
  dossiersPhaseCreation: number;
  dossiersPhaseEnquete: number;
  dossiersPhaseAmiable: number;
  dossiersPhaseJuridique: number;
  totalEnquetes: number;
  enquetesCompletees: number;
  actionsAmiables: number;
  actionsAmiablesCompletees: number;
  documentsHuissierCrees: number;
  documentsHuissierCompletes: number;
  actionsHuissierCrees: number;
  actionsHuissierCompletes: number;
  audiencesTotales: number;
  audiencesProchaines: number;
  tachesCompletees: number;
  tachesEnCours: number;
  tachesEnRetard: number;
  tauxReussiteGlobal: number;
  montantRecouvre: number;
  montantEnCours: number;
}

@Injectable({
  providedIn: 'root'
})
export class StatistiqueService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/statistiques';
  private statsSubject = new BehaviorSubject<StatistiquesGlobales | null>(null);
  public stats$ = this.statsSubject.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  /**
   * Charge les statistiques selon le rôle de l'utilisateur
   */
  loadStatistiques(): Observable<StatistiquesGlobales> {
    const endpoint = this.getEndpointByRole();
    const headers = this.getHeaders();
    
    return this.http.get<StatistiquesGlobales>(endpoint, { headers }).pipe(
      tap(stats => {
        console.log('Statistiques chargées:', stats);
        this.statsSubject.next(stats);
      })
    );
  }

  /**
   * Rafraîchit les statistiques après une action
   */
  refreshAfterAction(): void {
    // Attendre 1 seconde pour laisser le temps au backend de recalculer
    setTimeout(() => {
      this.loadStatistiques().subscribe({
        next: () => console.log('Statistiques rafraîchies'),
        error: (err) => console.error('Erreur rafraîchissement stats:', err)
      });
    }, 1000);
  }

  /**
   * Force le recalcul des statistiques (SuperAdmin uniquement)
   */
  recalculerStatistiques(): Observable<string> {
    const headers = this.getHeaders();
    return this.http.post<string>(`${this.apiUrl}/recalculer`, {}, { headers });
  }

  /**
   * Détermine l'endpoint selon le rôle
   */
  private getEndpointByRole(): string {
    const role = this.authService.getUserRole();
    
    if (role === 'SUPER_ADMIN') {
      return `${this.apiUrl}/globales`;
    } else if (role?.startsWith('CHEF_')) {
      return `${this.apiUrl}/departement`;
    } else if (role?.startsWith('AGENT_')) {
      return `${this.apiUrl}/mes-dossiers`;
    }
    
    // Par défaut, essayer globales
    return `${this.apiUrl}/globales`;
  }

  /**
   * Crée les headers avec le token
   */
  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }
}
```

---

## 📱 Composants à Créer/Modifier

### 1. Composant Dashboard Principal

**Fichier :** `dashboard-admin.component.ts`

**Fonctionnalités :**
- Charger les statistiques au `ngOnInit()`
- Afficher les statistiques dans des cartes
- Rafraîchir après chaque action
- Afficher un indicateur de chargement

**Template suggéré :**
```html
<div class="dashboard-stats">
  <div class="stat-card">
    <h3>Total Dossiers</h3>
    <p class="stat-value">{{ stats?.totalDossiers || 0 }}</p>
  </div>
  
  <div class="stat-card">
    <h3>Dossiers en Cours</h3>
    <p class="stat-value">{{ stats?.dossiersEnCours || 0 }}</p>
  </div>
  
  <!-- Autres cartes... -->
</div>
```

### 2. Intégration dans les Composants d'Action

**Exemple :** `create-enquete.component.ts`

**Modification nécessaire :**
```typescript
onEnqueteCreated() {
  this.enqueteService.createEnquete(data).subscribe({
    next: () => {
      // Rafraîchir les statistiques après création
      this.statistiqueService.refreshAfterAction();
      // Afficher message de succès
    },
    error: (err) => {
      // Gérer l'erreur
    }
  });
}
```

---

## 🔄 Points d'Intégration Frontend

### Actions qui Déclenchent le Rafraîchissement

| Action | Composant | Méthode à Modifier |
|--------|-----------|-------------------|
| Création de dossier | `create-dossier.component.ts` | `onDossierCreated()` |
| Modification de dossier | `edit-dossier.component.ts` | `onDossierUpdated()` |
| Validation de dossier | `validate-dossier.component.ts` | `onDossierValidated()` |
| Création d'enquête | `create-enquete.component.ts` | `onEnqueteCreated()` |
| Validation d'enquête | `validate-enquete.component.ts` | `onEnqueteValidated()` |
| Création d'action amiable | `create-action.component.ts` | `onActionCreated()` |
| Création de document huissier | `create-document-huissier.component.ts` | `onDocumentCreated()` |
| Création d'action huissier | `create-action-huissier.component.ts` | `onActionHuissierCreated()` |
| Création d'audience | `create-audience.component.ts` | `onAudienceCreated()` |
| Affectation avocat/huissier | `assign-avocat-huissier.component.ts` | `onAffectationDone()` |
| Validation de frais | `validate-frais.component.ts` | `onFraisValidated()` |
| Génération de facture | `generate-facture.component.ts` | `onFactureGenerated()` |

---

## ✅ Checklist d'Intégration

### Service Statistique
- [ ] Créer `statistique.service.ts`
- [ ] Implémenter `loadStatistiques()`
- [ ] Implémenter `refreshAfterAction()`
- [ ] Implémenter détection de rôle pour endpoint
- [ ] Gérer les erreurs (401, 403, 500)

### Composants Dashboard
- [ ] Créer/modifier `dashboard-admin.component.ts`
- [ ] Créer/modifier `dashboard-enquetes.component.ts`
- [ ] Créer/modifier `dashboard-actions.component.ts`
- [ ] Créer/modifier `dashboard-huissier.component.ts`
- [ ] Créer/modifier `dashboard-audiences.component.ts`
- [ ] Créer/modifier `dashboard-taches.component.ts`
- [ ] Créer/modifier `dashboard-phases.component.ts`

### Intégration Actions
- [ ] Ajouter `refreshAfterAction()` dans création de dossier
- [ ] Ajouter `refreshAfterAction()` dans modification de dossier
- [ ] Ajouter `refreshAfterAction()` dans validation de dossier
- [ ] Ajouter `refreshAfterAction()` dans création d'enquête
- [ ] Ajouter `refreshAfterAction()` dans validation d'enquête
- [ ] Ajouter `refreshAfterAction()` dans création d'action
- [ ] Ajouter `refreshAfterAction()` dans création de document huissier
- [ ] Ajouter `refreshAfterAction()` dans création d'action huissier
- [ ] Ajouter `refreshAfterAction()` dans création d'audience
- [ ] Ajouter `refreshAfterAction()` dans affectation avocat/huissier
- [ ] Ajouter `refreshAfterAction()` dans validation de frais
- [ ] Ajouter `refreshAfterAction()` dans génération de facture

### Affichage
- [ ] Afficher toutes les statistiques dans les interfaces appropriées
- [ ] Utiliser des cartes/étiquettes visuellement claires
- [ ] Afficher des indicateurs de chargement
- [ ] Gérer les cas où les statistiques sont null/undefined
- [ ] Formater les nombres (ex: 1,000 au lieu de 1000)
- [ ] Formater les pourcentages (ex: 20.5% au lieu de 20.5)
- [ ] Formater les montants (ex: 50,000 TND)

### Gestion d'Erreurs
- [ ] Gérer l'erreur 401 (token expiré) → Rediriger vers login
- [ ] Gérer l'erreur 403 (pas les droits) → Afficher message
- [ ] Gérer l'erreur 500 (erreur serveur) → Afficher message d'erreur
- [ ] Gérer les timeouts → Afficher message

---

## 🎨 Exemples de Templates

### Carte de Statistique

```html
<div class="stat-card">
  <div class="stat-header">
    <h3>{{ label }}</h3>
    <span class="stat-icon">{{ icon }}</span>
  </div>
  <div class="stat-value">
    {{ value | number }}
  </div>
  <div class="stat-trend" *ngIf="trend">
    <span [class]="trend > 0 ? 'positive' : 'negative'">
      {{ trend > 0 ? '+' : '' }}{{ trend }}%
    </span>
  </div>
</div>
```

### Dashboard avec Graphiques

```html
<div class="dashboard-container">
  <!-- Cartes de statistiques -->
  <div class="stats-grid">
    <app-stat-card 
      *ngFor="let stat of statsList" 
      [label]="stat.label"
      [value]="stats[stat.key]"
      [icon]="stat.icon">
    </app-stat-card>
  </div>
  
  <!-- Graphiques -->
  <div class="charts-grid">
    <app-chart 
      type="bar"
      [data]="phaseData"
      title="Dossiers par Phase">
    </app-chart>
    
    <app-chart 
      type="pie"
      [data]="statusData"
      title="Répartition des Statuts">
    </app-chart>
  </div>
</div>
```

---

## 🔍 Tests à Effectuer

### Test 1 : Chargement Initial
1. Se connecter en tant que SuperAdmin
2. Accéder au dashboard
3. **Vérifier :** Les statistiques s'affichent correctement
4. **Vérifier :** Les valeurs correspondent aux données réelles

### Test 2 : Mise à Jour après Action
1. Créer un nouveau dossier
2. **Vérifier :** `totalDossiers` s'incrémente
3. **Vérifier :** `dossiersCreesCeMois` s'incrémente
4. **Vérifier :** L'affichage se met à jour automatiquement

### Test 3 : Création d'Enquête
1. Créer une nouvelle enquête
2. **Vérifier :** `totalEnquetes` s'incrémente
3. **Vérifier :** `enquetesCompletees` reste à 0 (si non validée)
4. Valider l'enquête
5. **Vérifier :** `enquetesCompletees` s'incrémente

### Test 4 : Différents Rôles
1. Tester avec SuperAdmin → Voir toutes les statistiques
2. Tester avec Chef → Voir statistiques du département
3. Tester avec Agent → Voir statistiques personnelles

### Test 5 : Gestion d'Erreurs
1. Déconnecter l'utilisateur
2. **Vérifier :** Message d'erreur 401 affiché
3. Reconnecter
4. **Vérifier :** Les statistiques se rechargent

---

## 📝 Notes Importantes

### Format des Nombres

- **Nombres entiers** : Afficher avec séparateur de milliers (ex: 1,000)
- **Pourcentages** : Afficher avec 1-2 décimales (ex: 20.5%)
- **Montants** : Afficher avec devise et formatage (ex: 50,000 TND)

### Performance

- **Cache** : Mettre en cache les statistiques pendant 30 secondes
- **Rafraîchissement** : Ne pas rafraîchir trop souvent (max 1 fois par seconde)
- **Lazy Loading** : Charger les statistiques seulement quand nécessaire

### Accessibilité

- **Labels clairs** : Utiliser des labels descriptifs
- **Unités** : Toujours afficher les unités (TND, %, etc.)
- **Couleurs** : Utiliser des couleurs cohérentes (vert = positif, rouge = négatif)

---

## 🔗 Références

- **API Base URL :** `http://localhost:8089/carthage-creance/api`
- **Documentation Backend :** Voir `RAPPORT_RECALCUL_AUTOMATIQUE_STATISTIQUES.md`
- **Structure JSON :** Voir section "APIs Disponibles" ci-dessus

---

## ✅ Résultat Attendu

Après intégration complète :
- ✅ Toutes les statistiques sont affichées dans les interfaces appropriées
- ✅ Les statistiques se mettent à jour automatiquement après chaque action
- ✅ Les statistiques sont calculées en temps réel (pas de délai)
- ✅ Les statistiques sont formatées correctement (nombres, pourcentages, montants)
- ✅ La gestion d'erreurs est robuste
- ✅ L'expérience utilisateur est fluide et réactive

