# 💼 Prompts Frontend – Gestion Financière Complète

Ce guide décrit, composant par composant, comment construire les interfaces Angular dédiées au chef financier et à ses agents. Chaque prompt est prêt à être copié dans Cursor pour générer le code correspondant, tout en respectant les APIs backend existantes.

## 📋 Endpoints API Disponibles

### Analytics & Dashboard
- `GET /api/finances/analytics/dashboard` - Statistiques du dashboard
- `GET /api/finances/analytics/stats?startDate=&endDate=` - Statistiques par période
- `GET /api/finances/analytics/alerts?niveau=&phase=` - Liste des alertes
- `GET /api/finances/analytics/alerts/dossier/{dossierId}` - Alertes d'un dossier
- `GET /api/finances/analytics/repartition` - Répartition des frais
- `GET /api/finances/analytics/evolution?startDate=&endDate=` - Évolution mensuelle
- `GET /api/finances/analytics/roi-agents` - Classement ROI par agent
- `GET /api/finances/analytics/roi/agent/{agentId}` - ROI d'un agent spécifique
- `GET /api/finances/analytics/dossier/{dossierId}/stats` - Stats d'un dossier
- `GET /api/finances/analytics/insights` - Insights financiers
- `PUT /api/finances/analytics/insights/{insightId}/traite` - Marquer insight comme traité
- `GET /api/finances/analytics/export-excel?typeRapport=&startDate=&endDate=` - Export Excel

### Flux de Frais
- `GET /api/frais` - Liste tous les frais
- `GET /api/frais/{id}` - Détail d'un frais
- `POST /api/frais` - Créer un frais
- `PUT /api/frais/{id}` - Modifier un frais
- `DELETE /api/frais/{id}` - Supprimer un frais
- `GET /api/frais/dossier/{dossierId}` - Frais d'un dossier
- `GET /api/frais/statut/{statut}` - Frais par statut
- `GET /api/frais/en-attente` - Frais en attente de validation
- `GET /api/frais/phase/{phase}` - Frais par phase
- `GET /api/frais/date-range?startDate=&endDate=` - Frais par période
- `PUT /api/frais/{id}/valider` - Valider un frais
- `PUT /api/frais/{id}/rejeter` - Rejeter un frais
- `POST /api/frais/action/{actionId}` - Créer frais depuis action
- `POST /api/frais/enquete/{enqueteId}` - Créer frais depuis enquête
- `POST /api/frais/audience/{audienceId}` - Créer frais depuis audience
- `GET /api/frais/dossier/{dossierId}/total` - Total frais d'un dossier
- `GET /api/frais/statut/{statut}/total` - Total frais par statut
- `POST /api/frais/import-csv` - Import CSV (multipart/form-data, file)

### Factures
- `GET /api/factures` - Liste toutes les factures
- `GET /api/factures/{id}` - Détail d'une facture
- `POST /api/factures` - Créer une facture
- `PUT /api/factures/{id}` - Modifier une facture
- `DELETE /api/factures/{id}` - Supprimer une facture
- `GET /api/factures/numero/{numero}` - Facture par numéro
- `GET /api/factures/dossier/{dossierId}` - Factures d'un dossier
- `GET /api/factures/statut/{statut}` - Factures par statut
- `GET /api/factures/en-retard` - Factures en retard
- `POST /api/factures/dossier/{dossierId}/generer?periodeDebut=&periodeFin=` - Générer facture automatique
- `PUT /api/factures/{id}/finaliser` - Finaliser une facture
- `PUT /api/factures/{id}/envoyer` - Envoyer une facture
- `PUT /api/factures/{id}/relancer` - Relancer une facture
- `GET /api/factures/{id}/pdf` - Télécharger PDF de la facture

### Tarifs Catalogue
- `GET /api/tarifs` - Liste tous les tarifs
- `GET /api/tarifs/{id}` - Détail d'un tarif
- `POST /api/tarifs` - Créer un tarif
- `PUT /api/tarifs/{id}` - Modifier un tarif
- `DELETE /api/tarifs/{id}` - Supprimer un tarif
- `GET /api/tarifs/phase/{phase}` - Tarifs par phase
- `GET /api/tarifs/actifs` - Tarifs actifs
- `GET /api/tarifs/categorie/{categorie}` - Tarifs par catégorie
- `GET /api/tarifs/recherche?phase=&categorie=&actif=` - Recherche de tarifs

### Paiements
- `GET /api/paiements` - Liste tous les paiements
- `GET /api/paiements/{id}` - Détail d'un paiement
- `POST /api/paiements` - Créer un paiement
- `PUT /api/paiements/{id}` - Modifier un paiement
- `DELETE /api/paiements/{id}` - Supprimer un paiement
- `GET /api/paiements/facture/{factureId}` - Paiements d'une facture
- `GET /api/paiements/statut/{statut}` - Paiements par statut
- `PUT /api/paiements/{id}/valider` - Valider un paiement
- `PUT /api/paiements/{id}/refuser` - Refuser un paiement

---

## 🔹 Prompt 1 – Service Finance étendu
```
Dans le projet Angular, crée un service `ChefFinanceService` responsable de la consommation des endpoints financiers.

Fichier : `src/app/services/chef-finance.service.ts`

1. Interfaces à déclarer :
```typescript
export interface FinanceStats {
  totalFraisEngages: number;
  montantRecouvre: number;
  fraisRecuperes: number;
  netGenere: number;
  repartitionFrais: { categorie: string; montant: number }[];
  evolutionMensuelle: { mois: string; frais: number; recouvre: number }[];
}

export interface FinanceAlert {
  id: number;
  type: 'FRAIS_ELEVES' | 'DOSSIER_INACTIF' | 'BUDGET_DEPASSE' | 'ACTION_RISQUE';
  message: string;
  dossierId: number;
  agent?: string;
  niveau: 'INFO' | 'WARNING' | 'DANGER';
  dateDeclenchement: string;
}

export interface AgentRoi {
  agentId: number;
  agentNom: string;
  montantRecouvre: number;
  fraisEngages: number;
  roiPourcentage: number;
}

export interface FluxFrais {
  id: number;
  dossierId: number;
  phase: 'CREATION' | 'AMIABLE' | 'ENQUETE' | 'JURIDIQUE';
  categorie: string;
  quantite: number;
  tarifUnitaire: number;
  montant: number;
  statut: 'EN_ATTENTE' | 'VALIDE' | 'REJETE' | 'FACTURE' | 'PAYE';
  dateAction: string;
  justificatifUrl?: string;
  agent?: string;
  commentaire?: string;
}

export interface TarifCatalogue {
  id: number;
  phase: string;
  categorie: string;
  fournisseur?: string;
  tarifUnitaire: number;
  devise: string;
  dateDebut: string;
  dateFin?: string;
  actif: boolean;
}

export interface RapportFinance {
  id: number;
  type: 'MENSUEL' | 'CLIENT' | 'AGENT' | 'SECTEUR';
  periode: { debut: string; fin: string };
  generePar: string;
  dateGeneration: string;
  urlPdf?: string;
  urlExcel?: string;
}
```

2. Méthodes du service (exemple d'implémentation) :
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ChefFinanceService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Dashboard & Analytics
  getDashboardStats(): Observable<FinanceStats> {
    return this.http.get<FinanceStats>(`${this.baseUrl}/finances/analytics/dashboard`);
  }

  getStatsByDateRange(startDate: string, endDate: string): Observable<FinanceStats> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<FinanceStats>(`${this.baseUrl}/finances/analytics/stats`, { params });
  }

  getAlerts(niveau?: string, phase?: string): Observable<FinanceAlert[]> {
    let params = new HttpParams();
    if (niveau) params = params.set('niveau', niveau);
    if (phase) params = params.set('phase', phase);
    return this.http.get<FinanceAlert[]>(`${this.baseUrl}/finances/analytics/alerts`, { params });
  }

  getAgentRoiClassement(): Observable<AgentRoi[]> {
    return this.http.get<any[]>(`${this.baseUrl}/finances/analytics/roi-agents`).pipe(
      map(agents => agents.map(a => ({
        agentId: a.agentId,
        agentNom: a.agentNom,
        montantRecouvre: a.montantRecouvre || 0,
        fraisEngages: a.fraisEngages || 0,
        roiPourcentage: a.roiPourcentage || 0
      })))
    );
  }

  // Flux de Frais
  getFraisByDossier(dossierId: number): Observable<FluxFrais[]> {
    return this.http.get<FluxFrais[]>(`${this.baseUrl}/frais/dossier/${dossierId}`);
  }

  getFraisEnAttente(): Observable<FluxFrais[]> {
    return this.http.get<FluxFrais[]>(`${this.baseUrl}/frais/en-attente`);
  }

  validerFrais(fraisId: number, commentaire?: string): Observable<FluxFrais> {
    return this.http.put<FluxFrais>(`${this.baseUrl}/frais/${fraisId}/valider`, { commentaire });
  }

  rejeterFrais(fraisId: number, motif: string): Observable<FluxFrais> {
    return this.http.put<FluxFrais>(`${this.baseUrl}/frais/${fraisId}/rejeter`, { motif });
  }

  importFraisCSV(file: File): Observable<{ success: number; errors: number; succes: any[]; erreurs: any[] }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.baseUrl}/frais/import-csv`, formData);
  }

  // Factures
  getFacturesByDossier(dossierId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/factures/dossier/${dossierId}`);
  }

  genererFactureAutomatique(dossierId: number, periodeDebut?: string, periodeFin?: string): Observable<any> {
    let params = new HttpParams();
    if (periodeDebut) params = params.set('periodeDebut', periodeDebut);
    if (periodeFin) params = params.set('periodeFin', periodeFin);
    return this.http.post<any>(`${this.baseUrl}/factures/dossier/${dossierId}/generer`, null, { params });
  }

  downloadFacturePDF(factureId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/factures/${factureId}/pdf`, {
      responseType: 'blob'
    });
  }

  // Tarifs
  getTarifs(): Observable<TarifCatalogue[]> {
    return this.http.get<TarifCatalogue[]>(`${this.baseUrl}/tarifs`);
  }

  createTarif(tarif: Partial<TarifCatalogue>): Observable<TarifCatalogue> {
    return this.http.post<TarifCatalogue>(`${this.baseUrl}/tarifs`, tarif);
  }

  updateTarif(id: number, tarif: Partial<TarifCatalogue>): Observable<TarifCatalogue> {
    return this.http.put<TarifCatalogue>(`${this.baseUrl}/tarifs/${id}`, tarif);
  }

  // Export Excel
  exportRapportExcel(typeRapport: string, startDate: string, endDate: string, filtres?: any): Observable<Blob> {
    let params = new HttpParams()
      .set('typeRapport', typeRapport)
      .set('startDate', startDate)
      .set('endDate', endDate);
    if (filtres) {
      Object.keys(filtres).forEach(key => params = params.set(key, filtres[key]));
    }
    return this.http.get(`${this.baseUrl}/finances/analytics/export-excel`, {
      params,
      responseType: 'blob'
    });
  }

  // Insights
  getInsights(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/finances/analytics/insights`);
  }

  marquerInsightTraite(insightId: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/finances/analytics/insights/${insightId}/traite`, null);
  }
}
```

3. **Important** : Pour les téléchargements (PDF/Excel), utiliser `responseType: 'blob'` et `FileSaver` pour sauvegarder le fichier côté client.
```

---

## 🔹 Prompt 2 – `FinanceDashboardComponent`
```
Crée `FinanceDashboardComponent` (chef financier) dans `src/app/components/finance-dashboard/` :

Fonctionnalités :
1. Cards metrics (Total frais, Montant recouvré, Frais récupérés, Net généré)
2. Graphiques :
   - `ngx-charts-pie-chart` pour répartition des frais par catégorie
   - `ngx-charts-line-chart` pour évolution mensuelle Frais vs Recouvré
3. Tableau ROI par agent (MatTable + tri + barre horizontale de performance)
4. Section alertes :
   - Liste paginée filtrable par type/niveau/agent
   - Boutons “Voir dossier” (navigue vers détail) / “Assigner”
5. Affichage responsive (grid)

Méthodes clés :
```typescript
loadDashboard(): void {
  forkJoin({
    stats: this.financeService.getDashboardStats(),
    alerts: this.financeService.getAlerts(),
    agents: this.financeService.getAgentRoiClassement()
  }).subscribe(({ stats, alerts, agents }) => {
    this.stats = stats;
    this.alerts = alerts;
    this.agentRoi = agents;
    this.pieData = stats.repartitionFrais.map(...);
    this.lineData = buildLineSeries(stats.evolutionMensuelle);
  });
}
```

HTML : 3 sections (`metrics-grid`, `charts-grid`, `alerts-table`). Utiliser `mat-card`, `mat-table`, `mat-chip`, `mat-progress-bar`.
```

---

## 🔹 Prompt 3 – `DossierFinanceTabComponent`
```
Ajouter un onglet “Finance” dans la page de détail dossier (`src/app/components/dossier-detail/finance-tab/`).

Contenu :
- Tableau des frais (phase, catégorie, quantité, tarif, montant, statut, justificatif)
- Boutons d’action ligne pour `Valider` / `Rejeter` (si EN_ATTENTE) → ouvre `MatDialog` confirmation
- Carte synthèse :
  - Total par phase (progress bars)
  - Ratio frais/montant dû (gauge colorée)
  - Statut facture + bouton `Générer facture`
- Timeline factures (puces chronologiques avec montant, statut, lien PDF)
- Téléchargement PDF :
  ```typescript
  downloadFacturePDF(factureId: number): void {
    this.financeService.downloadFacturePDF(factureId).subscribe(
      (blob: Blob) => {
        const filename = `facture_${factureId}.pdf`;
        saveAs(blob, filename);
      }
    );
  }
  ```

Services utilisés : `getFraisByDossier`, `genererFacture`, `getHistoriqueFactures`.
Prévoir un `BehaviorSubject` pour rafraîchir après action.
```

---

## 🔹 Prompt 4 – `FraisValidationComponent`
```
Créer une page “Validation des frais” (`src/app/components/frais-validation/`).

Fonctionnalités :
- Table filtrable (phase, agent, montant min/max, date) avec `MatTableDataSource`
- Colonnes : Dossier (lien), Phase, Catégorie, Montant, Agent, Date, Statut, Actions
- Boutons `Valider` / `Rejeter` → `MatDialog` pour saisir commentaire (obligatoire si rejet)
- Indicateurs en haut : nombre de frais en attente, montant total
- Détail (drawer ou dialog) affichant justificatif, commentaire, lien vers action source
- Rafraîchissement après chaque action + snackbars de confirmation
```

---

## 🔹 Prompt 5 – `TarifCatalogueComponent`
```
Mettre en place une interface de gestion des tarifs (`src/app/components/tarif-catalogue/`).

Vue principale :
- Tableau avec colonnes Phase, Catégorie, Fournisseur, Tarif, Devise, DateEffet, Actif, Actions
- Boutons `Ajouter`, `Modifier`, `Dupliquer`, `Planifier fin`
- Filtre global + filtres par phase/catégorie/fournisseur

Dialog formulaire :
- Étapes : Informations générales → Période de validité → Preview impact
- Champs : phase (select), catégorie (select + autocompletion), fournisseur, tarif, devise, date début, date fin optionnelle, actif

Timeline versions :
- Liste vertical de toutes les versions d’un tarif (chips “Actif/Expiré”)

Bouton “Simuler coût” :
- Mini formulaire quantite + sélection tarif → affiche estimation coût total
```

---

## 🔹 Prompt 6 – `FraisImportComponent`
```
Créer un assistant d’import CSV (`src/app/components/frais-import/`).

Étapes UI (MatStepper) :
1. Upload fichier → stockage `FormData`
2. Mapping des colonnes (select pour dossier_id, phase, catégorie, quantite, tarifUnitaire, fournisseur, date)
3. Aperçu (table des 10 premières lignes avec validation en temps réel)
4. Résumé + bouton “Importer”

Après POST `/api/frais/import-csv` :
- Afficher rapport : `{ success: number, errors: number, succes: [...], erreurs: [...] }`
- Liste détaillée des lignes en erreur avec motif
- Format CSV attendu (colonnes) :
  ```
  dossier_id,phase,categorie,quantite,tarif_unitaire,fournisseur,date_action
  1,AMIABLE,APPEL,1,5.00,Fournisseur A,2024-01-15
  2,JURIDIQUE,HUISSIER,1,200.00,Cabinet B,2024-01-16
  ```
- Bouton “Télécharger rapport d'erreurs” (générer un CSV avec les erreurs)
```

---

## 🔹 Prompt 7 – `FinanceReportingComponent`
```
Interface reporting (`src/app/components/finance-reporting/`).

Fonctionnalités :
- Sélecteurs : type de rapport (MENSUEL, CLIENT, AGENT, SECTEUR), période (date range), filtres additionnels (client, agent, secteur)
- Boutons `Générer aperçu`, `Exporter Excel`
- Aperçu : composant combinant table + graphique (utiliser `ngx-charts` ou `apexcharts`)
- Historique : table des rapports déjà générés avec colonnes Date, Type, Période, Utilisateur, Téléchargements
- Gestion des téléchargements via `FileSaver` :
  ```typescript
  import { saveAs } from 'file-saver';
  
  downloadExcel(typeRapport: string, startDate: string, endDate: string): void {
    this.financeService.exportRapportExcel(typeRapport, startDate, endDate).subscribe(
      (blob: Blob) => {
        const filename = `rapport_${typeRapport}_${startDate}_${endDate}.xlsx`;
        saveAs(blob, filename);
        this.snackBar.open('Rapport téléchargé avec succès', 'Fermer', { duration: 3000 });
      },
      error => {
        this.snackBar.open('Erreur lors du téléchargement', 'Fermer', { duration: 3000 });
      }
    );
  }
  ```
```

---

## 🔹 Prompt 8 – `FinanceInsightsComponent`
```
Créer un composant “Insights financiers” (`src/app/components/finance-insights/`).

Comportement :
- Récupérer `/api/finances/insights`
- Grouper par catégorie (Optimisation coûts, Risques dossier, Performance agents)
- Afficher sous forme de cards avec icône, message, action recommandée
- Bouton `Marquer comme traité` → appelle `marquerInsightTraite`
- Possibilité de filtrer par dossier/agent/catégorie
```

---

## ✅ Logiciel cible (résumé)
- Tous les frais sont centralisés via `FluxFrais` et liés aux actions/phases.
- Les tarifs unitaires sont administrés par le chef financier, sans valeurs en dur.
- La facturation est automatisée (quantités × tarifs, génération PDF, suivi statut).
- Le dashboard offre visibilité temps réel (metrics + graphiques + alertes).
- Les workflows de validation, l’import CSV et le reporting assurent contrôle & traçabilité.
- Les insights (IA légère) aident à optimiser coûts et performance.

Ces prompts couvrent l’ensemble des écrans nécessaires pour le chef financier et son agent, prêts à être utilisés dans Cursor pour générer les composants Angular correspondants.

