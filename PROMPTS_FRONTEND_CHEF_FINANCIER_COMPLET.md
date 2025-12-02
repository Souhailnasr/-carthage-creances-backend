# 🎯 Prompts Complets Frontend : Interface Chef Financier

## 📋 Vue d'Ensemble

Ce document contient tous les prompts nécessaires pour créer l'interface complète du chef financier dans le frontend Angular, permettant de tester tout le workflow de finance.

---

## ✅ 1. VÉRIFICATION DES ENDPOINTS BACKEND

### 1.1. Endpoints Disponibles

Tous les endpoints suivants sont **déjà implémentés** dans le backend :

#### Finance Controller (`/api/finances`)
- ✅ `GET /api/finances` - Liste de toutes les finances
- ✅ `GET /api/finances/{id}` - Détails d'une finance
- ✅ `GET /api/finances/dossier/{dossierId}` - Finance d'un dossier
- ✅ `GET /api/finances/dossier/{dossierId}/facture` - Détail facture d'un dossier
- ✅ `GET /api/finances/dossier/{dossierId}/detail` - Coûts détaillés d'un dossier
- ✅ `POST /api/finances/dossier/{dossierId}/recalculer` - Recalculer les coûts
- ✅ `GET /api/finances/statistiques` - Statistiques globales
- ✅ `GET /api/finances/dossiers-avec-couts` - Dossiers avec coûts (paginé)
- ✅ `GET /api/finances/factures-en-attente` - Factures en attente
- ✅ `PUT /api/finances/dossier/{dossierId}/finaliser-facture` - Finaliser facture

#### FluxFrais Controller (`/api/frais`)
- ✅ `POST /api/frais` - Créer un frais
- ✅ `GET /api/frais` - Liste de tous les frais
- ✅ `GET /api/frais/{id}` - Détails d'un frais
- ✅ `GET /api/frais/dossier/{dossierId}` - Frais d'un dossier
- ✅ `GET /api/frais/en-attente` - Frais en attente de validation
- ✅ `GET /api/frais/statut/{statut}` - Frais par statut
- ✅ `GET /api/frais/phase/{phase}` - Frais par phase
- ✅ `PUT /api/frais/{id}/valider` - Valider un frais
- ✅ `PUT /api/frais/{id}/rejeter` - Rejeter un frais
- ✅ `POST /api/frais/import-csv` - Importer frais depuis CSV
- ✅ `GET /api/frais/dossier/{dossierId}/total` - Total des frais d'un dossier

#### Facture Controller (`/api/factures`)
- ✅ `POST /api/factures` - Créer une facture
- ✅ `GET /api/factures` - Liste de toutes les factures
- ✅ `GET /api/factures/{id}` - Détails d'une facture
- ✅ `GET /api/factures/dossier/{dossierId}` - Factures d'un dossier
- ✅ `GET /api/factures/statut/{statut}` - Factures par statut
- ✅ `GET /api/factures/en-retard` - Factures en retard
- ✅ `POST /api/factures/dossier/{dossierId}/generer` - Générer facture automatique
- ✅ `PUT /api/factures/{id}/finaliser` - Finaliser une facture
- ✅ `PUT /api/factures/{id}/envoyer` - Marquer comme envoyée
- ✅ `PUT /api/factures/{id}/relancer` - Envoyer une relance
- ✅ `GET /api/factures/{id}/pdf` - Télécharger PDF

#### Paiement Controller (`/api/paiements`)
- ✅ `POST /api/paiements` - Créer un paiement
- ✅ `GET /api/paiements` - Liste de tous les paiements
- ✅ `GET /api/paiements/{id}` - Détails d'un paiement
- ✅ `GET /api/paiements/facture/{factureId}` - Paiements d'une facture
- ✅ `GET /api/paiements/statut/{statut}` - Paiements par statut
- ✅ `PUT /api/paiements/{id}/valider` - Valider un paiement
- ✅ `PUT /api/paiements/{id}/refuser` - Refuser un paiement
- ✅ `GET /api/paiements/facture/{factureId}/total` - Total des paiements

#### TarifCatalogue Controller (`/api/tarifs`)
- ✅ `POST /api/tarifs` - Créer un tarif
- ✅ `GET /api/tarifs` - Liste de tous les tarifs
- ✅ `GET /api/tarifs/{id}` - Détails d'un tarif
- ✅ `GET /api/tarifs/actifs` - Tarifs actifs
- ✅ `GET /api/tarifs/phase/{phase}` - Tarifs par phase
- ✅ `PUT /api/tarifs/{id}` - Mettre à jour un tarif
- ✅ `PUT /api/tarifs/{id}/desactiver` - Désactiver un tarif
- ✅ `GET /api/tarifs/{id}/historique` - Historique d'un tarif

#### FinanceAnalytics Controller (`/api/finances/analytics`)
- ✅ `GET /api/finances/analytics/dashboard` - Dashboard statistiques
- ✅ `GET /api/finances/analytics/roi-agents` - ROI par agent
- ✅ `GET /api/finances/analytics/export-excel` - Export Excel

---

## 📦 2. PROMPT 1 : Créer les Interfaces TypeScript

```typescript
// Créer le fichier : src/app/models/finance.models.ts

export interface Finance {
  id?: number;
  devise: string;
  dateOperation: Date;
  description?: string;
  fraisAvocat?: number;
  fraisHuissier?: number;
  fraisCreationDossier?: number;
  fraisGestionDossier?: number;
  dureeGestionMois?: number;
  coutActionsAmiable?: number;
  coutActionsJuridique?: number;
  nombreActionsAmiable?: number;
  nombreActionsJuridique?: number;
  factureFinalisee?: boolean;
  dateFacturation?: Date;
  dossierId?: number;
}

export interface FluxFrais {
  id?: number;
  phase: PhaseFrais;
  categorie: string;
  quantite: number;
  tarifUnitaire?: number;
  montant?: number;
  statut: StatutFrais;
  dateAction: Date;
  justificatifUrl?: string;
  commentaire?: string;
  dossierId: number;
  actionId?: number;
  enqueteId?: number;
  audienceId?: number;
  avocatId?: number;
  huissierId?: number;
  factureId?: number;
}

export enum PhaseFrais {
  CREATION = 'CREATION',
  AMIABLE = 'AMIABLE',
  ENQUETE = 'ENQUETE',
  JURIDIQUE = 'JURIDIQUE'
}

export enum StatutFrais {
  EN_ATTENTE = 'EN_ATTENTE',
  VALIDE = 'VALIDE',
  REJETE = 'REJETE',
  FACTURE = 'FACTURE',
  PAYE = 'PAYE'
}

export interface Facture {
  id?: number;
  numeroFacture: string;
  dossierId: number;
  periodeDebut?: Date;
  periodeFin?: Date;
  dateEmission: Date;
  dateEcheance?: Date;
  montantHT: number;
  montantTTC: number;
  tva: number;
  statut: FactureStatut;
  pdfUrl?: string;
  envoyee: boolean;
  relanceEnvoyee: boolean;
}

export enum FactureStatut {
  BROUILLON = 'BROUILLON',
  EMISE = 'EMISE',
  PAYEE = 'PAYEE',
  EN_RETARD = 'EN_RETARD',
  ANNULEE = 'ANNULEE'
}

export interface Paiement {
  id?: number;
  factureId: number;
  datePaiement: Date;
  montant: number;
  modePaiement: ModePaiement;
  reference?: string;
  statut: StatutPaiement;
  commentaire?: string;
}

export enum ModePaiement {
  VIREMENT = 'VIREMENT',
  CHEQUE = 'CHEQUE',
  ESPECES = 'ESPECES',
  TRAITE = 'TRAITE',
  AUTRE = 'AUTRE'
}

export enum StatutPaiement {
  EN_ATTENTE = 'EN_ATTENTE',
  VALIDE = 'VALIDE',
  REFUSE = 'REFUSE'
}

export interface TarifCatalogue {
  id?: number;
  phase: PhaseFrais;
  categorie: string;
  description?: string;
  fournisseur?: string;
  tarifUnitaire: number;
  devise: string;
  dateDebut: Date;
  dateFin?: Date;
  actif: boolean;
}

export interface DetailFacture {
  fraisCreationDossier: number;
  coutGestionTotal: number;
  coutActionsAmiable: number;
  coutActionsJuridique: number;
  fraisAvocat: number;
  fraisHuissier: number;
  commissionAmiable?: number;
  commissionJuridique?: number;
  commissionRelance?: number;
  commissionInterets?: number;
  totalFacture: number;
}

export interface ValidationFraisDTO {
  commentaire?: string;
}

export interface StatistiquesCouts {
  totalFraisCreation: number;
  totalFraisGestion: number;
  totalActionsAmiable: number;
  totalActionsJuridique: number;
  totalAvocat: number;
  totalHuissier: number;
  grandTotal: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```

---

## 🔧 3. PROMPT 2 : Créer le Service FinanceService

```typescript
// Créer le fichier : src/app/services/finance.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Finance, DetailFacture, StatistiquesCouts, Page } from '../models/finance.models';

@Injectable({
  providedIn: 'root'
})
export class FinanceService {
  private apiUrl = 'http://localhost:8080/api/finances';

  constructor(private http: HttpClient) {}

  // Finance CRUD
  getFinanceById(id: number): Observable<Finance> {
    return this.http.get<Finance>(`${this.apiUrl}/${id}`);
  }

  getFinanceByDossier(dossierId: number): Observable<Finance> {
    return this.http.get<Finance>(`${this.apiUrl}/dossier/${dossierId}`);
  }

  getAllFinances(): Observable<Finance[]> {
    return this.http.get<Finance[]>(this.apiUrl);
  }

  updateFinance(id: number, finance: Finance): Observable<Finance> {
    return this.http.put<Finance>(`${this.apiUrl}/${id}`, finance);
  }

  // Détail Facture
  getDetailFacture(dossierId: number): Observable<DetailFacture> {
    return this.http.get<DetailFacture>(`${this.apiUrl}/dossier/${dossierId}/facture`);
  }

  getCoutsParDossier(dossierId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/dossier/${dossierId}/detail`);
  }

  // Recalcul
  recalculerCouts(dossierId: number): Observable<Finance> {
    return this.http.post<Finance>(`${this.apiUrl}/dossier/${dossierId}/recalculer`, {});
  }

  // Statistiques
  getStatistiquesCouts(): Observable<StatistiquesCouts> {
    return this.http.get<StatistiquesCouts>(`${this.apiUrl}/statistiques`);
  }

  // Dossiers avec coûts (paginé)
  getDossiersAvecCouts(page: number = 0, size: number = 10, sort: string = 'dateOperation'): Observable<Page<Finance>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    return this.http.get<Page<Finance>>(`${this.apiUrl}/dossiers-avec-couts`, { params });
  }

  // Factures en attente
  getFacturesEnAttente(): Observable<Finance[]> {
    return this.http.get<Finance[]>(`${this.apiUrl}/factures-en-attente`);
  }

  // Finaliser facture
  finaliserFacture(dossierId: number): Observable<Finance> {
    return this.http.put<Finance>(`${this.apiUrl}/dossier/${dossierId}/finaliser-facture`, {});
  }
}
```

---

## 🔧 4. PROMPT 3 : Créer le Service FluxFraisService

```typescript
// Créer le fichier : src/app/services/flux-frais.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FluxFrais, StatutFrais, PhaseFrais, ValidationFraisDTO } from '../models/finance.models';

@Injectable({
  providedIn: 'root'
})
export class FluxFraisService {
  private apiUrl = 'http://localhost:8080/api/frais';

  constructor(private http: HttpClient) {}

  // CRUD
  createFluxFrais(frais: Partial<FluxFrais>): Observable<FluxFrais> {
    return this.http.post<FluxFrais>(this.apiUrl, frais);
  }

  getFluxFraisById(id: number): Observable<FluxFrais> {
    return this.http.get<FluxFrais>(`${this.apiUrl}/${id}`);
  }

  getAllFluxFrais(): Observable<FluxFrais[]> {
    return this.http.get<FluxFrais[]>(this.apiUrl);
  }

  getFluxFraisByDossier(dossierId: number): Observable<FluxFrais[]> {
    return this.http.get<FluxFrais[]>(`${this.apiUrl}/dossier/${dossierId}`);
  }

  updateFluxFrais(id: number, frais: Partial<FluxFrais>): Observable<FluxFrais> {
    return this.http.put<FluxFrais>(`${this.apiUrl}/${id}`, frais);
  }

  deleteFluxFrais(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Validation
  validerFrais(id: number, dto?: ValidationFraisDTO): Observable<FluxFrais> {
    return this.http.put<FluxFrais>(`${this.apiUrl}/${id}/valider`, dto || {});
  }

  rejeterFrais(id: number, dto: ValidationFraisDTO): Observable<FluxFrais> {
    return this.http.put<FluxFrais>(`${this.apiUrl}/${id}/rejeter`, dto);
  }

  // Filtres
  getFluxFraisEnAttente(): Observable<FluxFrais[]> {
    return this.http.get<FluxFrais[]>(`${this.apiUrl}/en-attente`);
  }

  getFluxFraisByStatut(statut: StatutFrais): Observable<FluxFrais[]> {
    return this.http.get<FluxFrais[]>(`${this.apiUrl}/statut/${statut}`);
  }

  getFluxFraisByPhase(phase: PhaseFrais): Observable<FluxFrais[]> {
    return this.http.get<FluxFrais[]>(`${this.apiUrl}/phase/${phase}`);
  }

  getFluxFraisByDateRange(startDate: string, endDate: string): Observable<FluxFrais[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<FluxFrais[]>(`${this.apiUrl}/date-range`, { params });
  }

  // Calculs
  calculerTotalFraisByDossier(dossierId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/dossier/${dossierId}/total`);
  }

  // Import CSV
  importerFraisDepuisCSV(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.apiUrl}/import-csv`, formData);
  }

  // Création automatique
  creerFraisDepuisAction(actionId: number): Observable<FluxFrais> {
    return this.http.post<FluxFrais>(`${this.apiUrl}/action/${actionId}`, {});
  }

  creerFraisDepuisEnquete(enqueteId: number): Observable<FluxFrais> {
    return this.http.post<FluxFrais>(`${this.apiUrl}/enquete/${enqueteId}`, {});
  }

  creerFraisDepuisAudience(audienceId: number): Observable<FluxFrais> {
    return this.http.post<FluxFrais>(`${this.apiUrl}/audience/${audienceId}`, {});
  }
}
```

---

## 🔧 5. PROMPT 4 : Créer le Service FactureService

```typescript
// Créer le fichier : src/app/services/facture.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Facture, FactureStatut } from '../models/finance.models';

@Injectable({
  providedIn: 'root'
})
export class FactureService {
  private apiUrl = 'http://localhost:8080/api/factures';

  constructor(private http: HttpClient) {}

  // CRUD
  createFacture(facture: Partial<Facture>): Observable<Facture> {
    return this.http.post<Facture>(this.apiUrl, facture);
  }

  getFactureById(id: number): Observable<Facture> {
    return this.http.get<Facture>(`${this.apiUrl}/${id}`);
  }

  getAllFactures(): Observable<Facture[]> {
    return this.http.get<Facture[]>(this.apiUrl);
  }

  getFacturesByDossier(dossierId: number): Observable<Facture[]> {
    return this.http.get<Facture[]>(`${this.apiUrl}/dossier/${dossierId}`);
  }

  updateFacture(id: number, facture: Partial<Facture>): Observable<Facture> {
    return this.http.put<Facture>(`${this.apiUrl}/${id}`, facture);
  }

  deleteFacture(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Génération automatique
  genererFactureAutomatique(
    dossierId: number,
    periodeDebut?: string,
    periodeFin?: string
  ): Observable<Facture> {
    let params = new HttpParams();
    if (periodeDebut) params = params.set('periodeDebut', periodeDebut);
    if (periodeFin) params = params.set('periodeFin', periodeFin);
    return this.http.post<Facture>(`${this.apiUrl}/dossier/${dossierId}/generer`, {}, { params });
  }

  // Workflow
  finaliserFacture(id: number): Observable<Facture> {
    return this.http.put<Facture>(`${this.apiUrl}/${id}/finaliser`, {});
  }

  envoyerFacture(id: number): Observable<Facture> {
    return this.http.put<Facture>(`${this.apiUrl}/${id}/envoyer`, {});
  }

  relancerFacture(id: number): Observable<Facture> {
    return this.http.put<Facture>(`${this.apiUrl}/${id}/relancer`, {});
  }

  // Filtres
  getFacturesByStatut(statut: FactureStatut): Observable<Facture[]> {
    return this.http.get<Facture[]>(`${this.apiUrl}/statut/${statut}`);
  }

  getFacturesEnRetard(): Observable<Facture[]> {
    return this.http.get<Facture[]>(`${this.apiUrl}/en-retard`);
  }

  // PDF
  genererPdfFacture(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/pdf`, { responseType: 'blob' });
  }

  downloadPdfFacture(id: number): void {
    this.genererPdfFacture(id).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `facture-${id}.pdf`;
      link.click();
      window.URL.revokeObjectURL(url);
    });
  }
}
```

---

## 🔧 6. PROMPT 5 : Créer le Service PaiementService

```typescript
// Créer le fichier : src/app/services/paiement.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Paiement, StatutPaiement } from '../models/finance.models';

@Injectable({
  providedIn: 'root'
})
export class PaiementService {
  private apiUrl = 'http://localhost:8080/api/paiements';

  constructor(private http: HttpClient) {}

  // CRUD
  createPaiement(paiement: Partial<Paiement>): Observable<Paiement> {
    return this.http.post<Paiement>(this.apiUrl, paiement);
  }

  getPaiementById(id: number): Observable<Paiement> {
    return this.http.get<Paiement>(`${this.apiUrl}/${id}`);
  }

  getAllPaiements(): Observable<Paiement[]> {
    return this.http.get<Paiement[]>(this.apiUrl);
  }

  getPaiementsByFacture(factureId: number): Observable<Paiement[]> {
    return this.http.get<Paiement[]>(`${this.apiUrl}/facture/${factureId}`);
  }

  updatePaiement(id: number, paiement: Partial<Paiement>): Observable<Paiement> {
    return this.http.put<Paiement>(`${this.apiUrl}/${id}`, paiement);
  }

  deletePaiement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Validation
  validerPaiement(id: number): Observable<Paiement> {
    return this.http.put<Paiement>(`${this.apiUrl}/${id}/valider`, {});
  }

  refuserPaiement(id: number, motif: string): Observable<Paiement> {
    const params = new HttpParams().set('motif', motif);
    return this.http.put<Paiement>(`${this.apiUrl}/${id}/refuser`, {}, { params });
  }

  // Filtres
  getPaiementsByStatut(statut: StatutPaiement): Observable<Paiement[]> {
    return this.http.get<Paiement[]>(`${this.apiUrl}/statut/${statut}`);
  }

  getPaiementsByDateRange(startDate: string, endDate: string): Observable<Paiement[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<Paiement[]>(`${this.apiUrl}/date-range`, { params });
  }

  // Calculs
  calculerTotalPaiementsByFacture(factureId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/facture/${factureId}/total`);
  }

  calculerTotalPaiementsByDateRange(startDate: string, endDate: string): Observable<number> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<number>(`${this.apiUrl}/date-range/total`, { params });
  }
}
```

---

## 🔧 7. PROMPT 6 : Créer le Service TarifCatalogueService

```typescript
// Créer le fichier : src/app/services/tarif-catalogue.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TarifCatalogue, PhaseFrais } from '../models/finance.models';

@Injectable({
  providedIn: 'root'
})
export class TarifCatalogueService {
  private apiUrl = 'http://localhost:8080/api/tarifs';

  constructor(private http: HttpClient) {}

  // CRUD
  createTarif(tarif: Partial<TarifCatalogue>): Observable<TarifCatalogue> {
    return this.http.post<TarifCatalogue>(this.apiUrl, tarif);
  }

  getTarifById(id: number): Observable<TarifCatalogue> {
    return this.http.get<TarifCatalogue>(`${this.apiUrl}/${id}`);
  }

  getAllTarifs(): Observable<TarifCatalogue[]> {
    return this.http.get<TarifCatalogue[]>(this.apiUrl);
  }

  getTarifsActifs(): Observable<TarifCatalogue[]> {
    return this.http.get<TarifCatalogue[]>(`${this.apiUrl}/actifs`);
  }

  updateTarif(id: number, tarif: Partial<TarifCatalogue>): Observable<TarifCatalogue> {
    return this.http.put<TarifCatalogue>(`${this.apiUrl}/${id}`, tarif);
  }

  deleteTarif(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  desactiverTarif(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/desactiver`, {});
  }

  // Filtres
  getTarifsByPhase(phase: PhaseFrais): Observable<TarifCatalogue[]> {
    return this.http.get<TarifCatalogue[]>(`${this.apiUrl}/phase/${phase}`);
  }

  getTarifsByCategorie(categorie: string): Observable<TarifCatalogue[]> {
    return this.http.get<TarifCatalogue[]>(`${this.apiUrl}/categorie/${categorie}`);
  }

  // Historique
  getHistoriqueTarif(id: number): Observable<TarifCatalogue[]> {
    return this.http.get<TarifCatalogue[]>(`${this.apiUrl}/${id}/historique`);
  }
}
```

---

## 🎨 8. PROMPT 7 : Créer le Dashboard Chef Financier

```typescript
// Créer le fichier : src/app/components/chef-finance-dashboard/chef-finance-dashboard.component.ts

import { Component, OnInit } from '@angular/core';
import { FinanceService } from '../../services/finance.service';
import { FluxFraisService } from '../../services/flux-frais.service';
import { FactureService } from '../../services/facture.service';
import { PaiementService } from '../../services/paiement.service';
import { Finance, FluxFrais, Facture, StatistiquesCouts, StatutFrais } from '../../models/finance.models';

@Component({
  selector: 'app-chef-finance-dashboard',
  templateUrl: './chef-finance-dashboard.component.html',
  styleUrls: ['./chef-finance-dashboard.component.css']
})
export class ChefFinanceDashboardComponent implements OnInit {
  statistiques: StatistiquesCouts | null = null;
  fraisEnAttente: FluxFrais[] = [];
  facturesEnRetard: Facture[] = [];
  facturesEnAttente: Finance[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private financeService: FinanceService,
    private fluxFraisService: FluxFraisService,
    private factureService: FactureService
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = null;

    // Charger statistiques
    this.financeService.getStatistiquesCouts().subscribe({
      next: (stats) => {
        this.statistiques = stats;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des statistiques';
        this.loading = false;
        console.error(err);
      }
    });

    // Charger frais en attente
    this.fluxFraisService.getFluxFraisEnAttente().subscribe({
      next: (frais) => {
        this.fraisEnAttente = frais;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des frais en attente', err);
      }
    });

    // Charger factures en retard
    this.factureService.getFacturesEnRetard().subscribe({
      next: (factures) => {
        this.facturesEnRetard = factures;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des factures en retard', err);
      }
    });

    // Charger factures en attente
    this.financeService.getFacturesEnAttente().subscribe({
      next: (factures) => {
        this.facturesEnAttente = factures;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des factures en attente', err);
      }
    });
  }

  validerFrais(fraisId: number): void {
    this.fluxFraisService.validerFrais(fraisId).subscribe({
      next: () => {
        this.loadDashboard();
      },
      error: (err) => {
        this.error = 'Erreur lors de la validation du frais';
        console.error(err);
      }
    });
  }

  rejeterFrais(fraisId: number, motif: string): void {
    this.fluxFraisService.rejeterFrais(fraisId, { commentaire: motif }).subscribe({
      next: () => {
        this.loadDashboard();
      },
      error: (err) => {
        this.error = 'Erreur lors du rejet du frais';
        console.error(err);
      }
    });
  }
}
```

```html
<!-- Créer le fichier : src/app/components/chef-finance-dashboard/chef-finance-dashboard.component.html -->

<div class="dashboard-container">
  <h1>Dashboard Chef Financier</h1>

  <!-- Statistiques Globales -->
  <div class="stats-grid" *ngIf="statistiques">
    <div class="stat-card">
      <h3>Frais de Création</h3>
      <p class="stat-value">{{ statistiques.totalFraisCreation | number:'1.2-2' }} TND</p>
    </div>
    <div class="stat-card">
      <h3>Frais de Gestion</h3>
      <p class="stat-value">{{ statistiques.totalFraisGestion | number:'1.2-2' }} TND</p>
    </div>
    <div class="stat-card">
      <h3>Actions Amiable</h3>
      <p class="stat-value">{{ statistiques.totalActionsAmiable | number:'1.2-2' }} TND</p>
    </div>
    <div class="stat-card">
      <h3>Actions Juridique</h3>
      <p class="stat-value">{{ statistiques.totalActionsJuridique | number:'1.2-2' }} TND</p>
    </div>
    <div class="stat-card">
      <h3>Frais Avocat</h3>
      <p class="stat-value">{{ statistiques.totalAvocat | number:'1.2-2' }} TND</p>
    </div>
    <div class="stat-card">
      <h3>Frais Huissier</h3>
      <p class="stat-value">{{ statistiques.totalHuissier | number:'1.2-2' }} TND</p>
    </div>
    <div class="stat-card total">
      <h3>Grand Total</h3>
      <p class="stat-value">{{ statistiques.grandTotal | number:'1.2-2' }} TND</p>
    </div>
  </div>

  <!-- Frais en Attente -->
  <div class="section">
    <h2>Frais en Attente de Validation ({{ fraisEnAttente.length }})</h2>
    <table class="table">
      <thead>
        <tr>
          <th>Phase</th>
          <th>Catégorie</th>
          <th>Montant</th>
          <th>Date</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let frais of fraisEnAttente">
          <td>{{ frais.phase }}</td>
          <td>{{ frais.categorie }}</td>
          <td>{{ frais.montant | number:'1.2-2' }} TND</td>
          <td>{{ frais.dateAction | date:'short' }}</td>
          <td>
            <button (click)="validerFrais(frais.id!)" class="btn-success">Valider</button>
            <button (click)="rejeterFrais(frais.id!, '')" class="btn-danger">Rejeter</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- Factures en Retard -->
  <div class="section">
    <h2>Factures en Retard ({{ facturesEnRetard.length }})</h2>
    <table class="table">
      <thead>
        <tr>
          <th>Numéro</th>
          <th>Montant TTC</th>
          <th>Date Échéance</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let facture of facturesEnRetard">
          <td>{{ facture.numeroFacture }}</td>
          <td>{{ facture.montantTTC | number:'1.2-2' }} TND</td>
          <td>{{ facture.dateEcheance | date:'short' }}</td>
          <td>
            <button (click)="relancerFacture(facture.id!)" class="btn-warning">Relancer</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- Factures en Attente -->
  <div class="section">
    <h2>Factures en Attente de Finalisation ({{ facturesEnAttente.length }})</h2>
    <table class="table">
      <thead>
        <tr>
          <th>Dossier ID</th>
          <th>Total</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let finance of facturesEnAttente">
          <td>{{ finance.dossierId }}</td>
          <td>{{ calculerTotal(finance) | number:'1.2-2' }} TND</td>
          <td>
            <button (click)="voirDetail(finance.dossierId!)" class="btn-primary">Voir Détail</button>
            <button (click)="finaliserFacture(finance.dossierId!)" class="btn-success">Finaliser</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>
```

---

## 🎨 9. PROMPT 8 : Créer le Composant Liste des Frais

```typescript
// Créer le fichier : src/app/components/frais-list/frais-list.component.ts

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FluxFraisService } from '../../services/flux-frais.service';
import { FluxFrais, StatutFrais, PhaseFrais } from '../../models/finance.models';

@Component({
  selector: 'app-frais-list',
  templateUrl: './frais-list.component.html',
  styleUrls: ['./frais-list.component.css']
})
export class FraisListComponent implements OnInit {
  frais: FluxFrais[] = [];
  dossierId?: number;
  loading = false;
  filterStatut?: StatutFrais;
  filterPhase?: PhaseFrais;

  constructor(
    private fluxFraisService: FluxFraisService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.dossierId = params['dossierId'];
      if (this.dossierId) {
        this.loadFrais();
      } else {
        this.loadAllFrais();
      }
    });
  }

  loadFrais(): void {
    if (!this.dossierId) return;
    this.loading = true;
    this.fluxFraisService.getFluxFraisByDossier(this.dossierId).subscribe({
      next: (frais) => {
        this.frais = frais;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  loadAllFrais(): void {
    this.loading = true;
    this.fluxFraisService.getAllFluxFrais().subscribe({
      next: (frais) => {
        this.frais = frais;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  filterByStatut(statut: StatutFrais): void {
    this.filterStatut = statut;
    this.fluxFraisService.getFluxFraisByStatut(statut).subscribe({
      next: (frais) => {
        this.frais = frais;
      }
    });
  }

  filterByPhase(phase: PhaseFrais): void {
    this.filterPhase = phase;
    this.fluxFraisService.getFluxFraisByPhase(phase).subscribe({
      next: (frais) => {
        this.frais = frais;
      }
    });
  }

  validerFrais(id: number): void {
    this.fluxFraisService.validerFrais(id).subscribe({
      next: () => {
        this.loadFrais();
      }
    });
  }

  rejeterFrais(id: number): void {
    const motif = prompt('Motif du rejet :');
    if (motif) {
      this.fluxFraisService.rejeterFrais(id, { commentaire: motif }).subscribe({
        next: () => {
          this.loadFrais();
        }
      });
    }
  }
}
```

---

## 🎨 10. PROMPT 9 : Créer le Composant Détail Facture

```typescript
// Créer le fichier : src/app/components/facture-detail/facture-detail.component.ts

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FinanceService } from '../../services/finance.service';
import { FactureService } from '../../services/facture.service';
import { DetailFacture, Finance } from '../../models/finance.models';

@Component({
  selector: 'app-facture-detail',
  templateUrl: './facture-detail.component.html',
  styleUrls: ['./facture-detail.component.css']
})
export class FactureDetailComponent implements OnInit {
  dossierId!: number;
  detailFacture: DetailFacture | null = null;
  finance: Finance | null = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private financeService: FinanceService,
    private factureService: FactureService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.dossierId = params['dossierId'];
      this.loadDetail();
    });
  }

  loadDetail(): void {
    this.loading = true;
    this.financeService.getDetailFacture(this.dossierId).subscribe({
      next: (detail) => {
        this.detailFacture = detail;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });

    this.financeService.getFinanceByDossier(this.dossierId).subscribe({
      next: (finance) => {
        this.finance = finance;
      }
    });
  }

  recalculer(): void {
    this.financeService.recalculerCouts(this.dossierId).subscribe({
      next: () => {
        this.loadDetail();
      }
    });
  }

  finaliserFacture(): void {
    this.financeService.finaliserFacture(this.dossierId).subscribe({
      next: () => {
        this.loadDetail();
      }
    });
  }

  genererFacture(): void {
    this.factureService.genererFactureAutomatique(this.dossierId).subscribe({
      next: (facture) => {
        alert(`Facture ${facture.numeroFacture} générée avec succès`);
        this.router.navigate(['/factures', facture.id]);
      }
    });
  }
}
```

```html
<!-- Créer le fichier : src/app/components/facture-detail/facture-detail.component.html -->

<div class="facture-detail-container" *ngIf="detailFacture">
  <h1>Détail Facture - Dossier #{{ dossierId }}</h1>

  <div class="actions">
    <button (click)="recalculer()" class="btn-secondary">Recalculer</button>
    <button (click)="genererFacture()" class="btn-primary">Générer Facture</button>
    <button (click)="finaliserFacture()" class="btn-success">Finaliser</button>
  </div>

  <div class="facture-sections">
    <!-- Coûts de Création et Gestion -->
    <section>
      <h2>Coûts de Création et Gestion</h2>
      <table class="detail-table">
        <tr>
          <td>Frais création dossier</td>
          <td>{{ detailFacture.fraisCreationDossier | number:'1.2-2' }} TND</td>
        </tr>
        <tr>
          <td>Coût gestion total</td>
          <td>{{ detailFacture.coutGestionTotal | number:'1.2-2' }} TND</td>
        </tr>
        <tr class="subtotal">
          <td><strong>Sous-total</strong></td>
          <td><strong>{{ (detailFacture.fraisCreationDossier + detailFacture.coutGestionTotal) | number:'1.2-2' }} TND</strong></td>
        </tr>
      </table>
    </section>

    <!-- Coûts des Actions -->
    <section>
      <h2>Coûts des Actions</h2>
      <table class="detail-table">
        <tr>
          <td>Actions Recouvrement Amiable</td>
          <td>{{ detailFacture.coutActionsAmiable | number:'1.2-2' }} TND</td>
        </tr>
        <tr>
          <td>Actions Recouvrement Juridique</td>
          <td>{{ detailFacture.coutActionsJuridique | number:'1.2-2' }} TND</td>
        </tr>
        <tr class="subtotal">
          <td><strong>Sous-total actions</strong></td>
          <td><strong>{{ (detailFacture.coutActionsAmiable + detailFacture.coutActionsJuridique) | number:'1.2-2' }} TND</strong></td>
        </tr>
      </table>
    </section>

    <!-- Frais Professionnels -->
    <section>
      <h2>Frais Professionnels</h2>
      <table class="detail-table">
        <tr>
          <td>Frais avocat</td>
          <td>{{ detailFacture.fraisAvocat | number:'1.2-2' }} TND</td>
        </tr>
        <tr>
          <td>Frais huissier</td>
          <td>{{ detailFacture.fraisHuissier | number:'1.2-2' }} TND</td>
        </tr>
        <tr class="subtotal">
          <td><strong>Sous-total</strong></td>
          <td><strong>{{ (detailFacture.fraisAvocat + detailFacture.fraisHuissier) | number:'1.2-2' }} TND</strong></td>
        </tr>
      </table>
    </section>

    <!-- Commissions (si présentes) -->
    <section *ngIf="detailFacture.commissionAmiable || detailFacture.commissionJuridique">
      <h2>Commissions</h2>
      <table class="detail-table">
        <tr *ngIf="detailFacture.commissionAmiable">
          <td>Commission amiable</td>
          <td>{{ detailFacture.commissionAmiable | number:'1.2-2' }} TND</td>
        </tr>
        <tr *ngIf="detailFacture.commissionJuridique">
          <td>Commission juridique</td>
          <td>{{ detailFacture.commissionJuridique | number:'1.2-2' }} TND</td>
        </tr>
        <tr *ngIf="detailFacture.commissionInterets">
          <td>Commission intérêts</td>
          <td>{{ detailFacture.commissionInterets | number:'1.2-2' }} TND</td>
        </tr>
      </table>
    </section>

    <!-- Total Facture -->
    <section class="total-section">
      <h2>Total Facture</h2>
      <div class="total-amount">
        {{ detailFacture.totalFacture | number:'1.2-2' }} TND
      </div>
    </section>
  </div>
</div>
```

---

## 🎨 11. PROMPT 10 : Créer le Composant Gestion Tarifs

```typescript
// Créer le fichier : src/app/components/tarifs-gestion/tarifs-gestion.component.ts

import { Component, OnInit } from '@angular/core';
import { TarifCatalogueService } from '../../services/tarif-catalogue.service';
import { TarifCatalogue, PhaseFrais } from '../../models/finance.models';

@Component({
  selector: 'app-tarifs-gestion',
  templateUrl: './tarifs-gestion.component.html',
  styleUrls: ['./tarifs-gestion.component.css']
})
export class TarifsGestionComponent implements OnInit {
  tarifs: TarifCatalogue[] = [];
  tarifsActifs: TarifCatalogue[] = [];
  selectedTarif?: TarifCatalogue;
  showForm = false;
  editing = false;

  newTarif: Partial<TarifCatalogue> = {
    phase: PhaseFrais.CREATION,
    categorie: '',
    tarifUnitaire: 0,
    devise: 'TND',
    actif: true,
    dateDebut: new Date()
  };

  phases = Object.values(PhaseFrais);

  constructor(private tarifService: TarifCatalogueService) {}

  ngOnInit(): void {
    this.loadTarifs();
  }

  loadTarifs(): void {
    this.tarifService.getAllTarifs().subscribe({
      next: (tarifs) => {
        this.tarifs = tarifs;
      }
    });

    this.tarifService.getTarifsActifs().subscribe({
      next: (tarifs) => {
        this.tarifsActifs = tarifs;
      }
    });
  }

  createTarif(): void {
    this.editing = false;
    this.selectedTarif = undefined;
    this.newTarif = {
      phase: PhaseFrais.CREATION,
      categorie: '',
      tarifUnitaire: 0,
      devise: 'TND',
      actif: true,
      dateDebut: new Date()
    };
    this.showForm = true;
  }

  editTarif(tarif: TarifCatalogue): void {
    this.editing = true;
    this.selectedTarif = tarif;
    this.newTarif = { ...tarif };
    this.showForm = true;
  }

  saveTarif(): void {
    if (this.editing && this.selectedTarif?.id) {
      this.tarifService.updateTarif(this.selectedTarif.id, this.newTarif).subscribe({
        next: () => {
          this.loadTarifs();
          this.showForm = false;
        }
      });
    } else {
      this.tarifService.createTarif(this.newTarif).subscribe({
        next: () => {
          this.loadTarifs();
          this.showForm = false;
        }
      });
    }
  }

  desactiverTarif(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir désactiver ce tarif ?')) {
      this.tarifService.desactiverTarif(id).subscribe({
        next: () => {
          this.loadTarifs();
        }
      });
    }
  }

  voirHistorique(id: number): void {
    this.tarifService.getHistoriqueTarif(id).subscribe({
      next: (historique) => {
        console.log('Historique:', historique);
        // Afficher dans un modal ou dialog
      }
    });
  }
}
```

---

## 🎨 12. PROMPT 11 : Créer le Composant Liste Factures

```typescript
// Créer le fichier : src/app/components/factures-list/factures-list.component.ts

import { Component, OnInit } from '@angular/core';
import { FactureService } from '../../services/facture.service';
import { Facture, FactureStatut } from '../../models/finance.models';

@Component({
  selector: 'app-factures-list',
  templateUrl: './factures-list.component.html',
  styleUrls: ['./factures-list.component.css']
})
export class FacturesListComponent implements OnInit {
  factures: Facture[] = [];
  filterStatut?: FactureStatut;
  loading = false;

  constructor(private factureService: FactureService) {}

  ngOnInit(): void {
    this.loadFactures();
  }

  loadFactures(): void {
    this.loading = true;
    if (this.filterStatut) {
      this.factureService.getFacturesByStatut(this.filterStatut).subscribe({
        next: (factures) => {
          this.factures = factures;
          this.loading = false;
        }
      });
    } else {
      this.factureService.getAllFactures().subscribe({
        next: (factures) => {
          this.factures = factures;
          this.loading = false;
        }
      });
    }
  }

  finaliserFacture(id: number): void {
    this.factureService.finaliserFacture(id).subscribe({
      next: () => {
        this.loadFactures();
      }
    });
  }

  envoyerFacture(id: number): void {
    this.factureService.envoyerFacture(id).subscribe({
      next: () => {
        this.loadFactures();
      }
    });
  }

  relancerFacture(id: number): void {
    this.factureService.relancerFacture(id).subscribe({
      next: () => {
        this.loadFactures();
      }
    });
  }

  downloadPdf(id: number): void {
    this.factureService.downloadPdfFacture(id);
  }
}
```

---

## 🎨 13. PROMPT 12 : Créer le Composant Gestion Paiements

```typescript
// Créer le fichier : src/app/components/paiements-gestion/paiements-gestion.component.ts

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PaiementService } from '../../services/paiement.service';
import { Paiement, StatutPaiement, ModePaiement } from '../../models/finance.models';

@Component({
  selector: 'app-paiements-gestion',
  templateUrl: './paiements-gestion.component.html',
  styleUrls: ['./paiements-gestion.component.css']
})
export class PaiementsGestionComponent implements OnInit {
  paiements: Paiement[] = [];
  factureId?: number;
  totalPaiements = 0;
  showForm = false;

  newPaiement: Partial<Paiement> = {
    datePaiement: new Date(),
    montant: 0,
    modePaiement: ModePaiement.VIREMENT,
    statut: StatutPaiement.EN_ATTENTE
  };

  modesPaiement = Object.values(ModePaiement);

  constructor(
    private paiementService: PaiementService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.factureId = params['factureId'];
      if (this.factureId) {
        this.loadPaiements();
        this.loadTotal();
      } else {
        this.loadAllPaiements();
      }
    });
  }

  loadPaiements(): void {
    if (!this.factureId) return;
    this.paiementService.getPaiementsByFacture(this.factureId).subscribe({
      next: (paiements) => {
        this.paiements = paiements;
      }
    });
  }

  loadAllPaiements(): void {
    this.paiementService.getAllPaiements().subscribe({
      next: (paiements) => {
        this.paiements = paiements;
      }
    });
  }

  loadTotal(): void {
    if (!this.factureId) return;
    this.paiementService.calculerTotalPaiementsByFacture(this.factureId).subscribe({
      next: (total) => {
        this.totalPaiements = total;
      }
    });
  }

  createPaiement(): void {
    if (!this.factureId) return;
    this.newPaiement.factureId = this.factureId;
    this.paiementService.createPaiement(this.newPaiement).subscribe({
      next: () => {
        this.loadPaiements();
        this.loadTotal();
        this.showForm = false;
        this.newPaiement = {
          datePaiement: new Date(),
          montant: 0,
          modePaiement: ModePaiement.VIREMENT,
          statut: StatutPaiement.EN_ATTENTE
        };
      }
    });
  }

  validerPaiement(id: number): void {
    this.paiementService.validerPaiement(id).subscribe({
      next: () => {
        this.loadPaiements();
        this.loadTotal();
      }
    });
  }

  refuserPaiement(id: number): void {
    const motif = prompt('Motif du refus :');
    if (motif) {
      this.paiementService.refuserPaiement(id, motif).subscribe({
        next: () => {
          this.loadPaiements();
        }
      });
    }
  }
}
```

---

## 🧪 14. PROMPT 13 : Guide de Test du Workflow Complet

```markdown
# Guide de Test : Workflow Complet Finance

## Scénario de Test Complet

### Étape 1 : Initialiser les Tarifs
1. Accéder à `/tarifs`
2. Créer les tarifs de l'annexe :
   - Phase: CREATION, Catégorie: OUVERTURE_DOSSIER, Tarif: 250 TND
   - Phase: ENQUETE, Catégorie: ENQUETE_PRECONTENTIEUSE, Tarif: 300 TND
   - Phase: JURIDIQUE, Catégorie: AVANCE_RECOUVREMENT_JUDICIAIRE, Tarif: 1000 TND
   - Phase: AMIABLE, Catégorie: COMMISSION_AMIABLE, Tarif: 12 (pourcentage)
   - Phase: JURIDIQUE, Catégorie: COMMISSION_JURIDIQUE, Tarif: 15 (pourcentage)

### Étape 2 : Créer un Dossier
1. Créer un nouveau dossier
2. Vérifier qu'un FluxFrais d'ouverture (250 TND) est créé automatiquement
3. Vérifier que la Finance est créée

### Étape 3 : Valider les Frais d'Ouverture
1. Aller dans Dashboard Chef Financier
2. Voir la liste des frais en attente
3. Valider le frais d'ouverture

### Étape 4 : Créer une Enquête
1. Créer une enquête pour le dossier
2. Vérifier qu'un FluxFrais d'enquête (300 TND) est créé
3. Valider le frais d'enquête

### Étape 5 : Actions Amiable
1. Créer des actions amiable (appels, emails)
2. Vérifier que des FluxFrais sont créés automatiquement
3. Valider les frais

### Étape 6 : Recouvrement Amiable
1. Enregistrer un montant recouvré (ex: 2000 TND)
2. Vérifier qu'une commission amiable est calculée (2000 × 12% = 240 TND)
3. Valider la commission

### Étape 7 : Passage au Juridique
1. Passer le dossier au juridique
2. Vérifier qu'un FluxFrais d'avance (1000 TND) est créé
3. Valider le frais

### Étape 8 : Recouvrement Juridique
1. Enregistrer un montant recouvré juridique (ex: 1500 TND)
2. Vérifier qu'une commission juridique est calculée (1500 × 15% = 225 TND)
3. Valider la commission

### Étape 9 : Clôture du Dossier
1. Clôturer le dossier
2. Vérifier que la durée de gestion est calculée
3. Vérifier le coût total de gestion

### Étape 10 : Générer la Facture
1. Aller dans Détail Facture du dossier
2. Cliquer sur "Générer Facture"
3. Vérifier que tous les frais validés sont inclus
4. Vérifier le montant HT et TTC

### Étape 11 : Finaliser la Facture
1. Vérifier le détail de la facture
2. Cliquer sur "Finaliser"
3. Vérifier que le statut passe à EMISE

### Étape 12 : Envoyer la Facture
1. Cliquer sur "Envoyer"
2. Télécharger le PDF
3. Vérifier le contenu du PDF

### Étape 13 : Enregistrer un Paiement
1. Aller dans Gestion Paiements de la facture
2. Créer un nouveau paiement
3. Valider le paiement
4. Vérifier que le statut de la facture passe à PAYEE si le montant est couvert

### Étape 14 : Vérifier les Statistiques
1. Aller dans Dashboard Chef Financier
2. Vérifier les statistiques globales
3. Vérifier que tous les montants sont corrects
```

---

## 📝 15. PROMPT 14 : Configuration des Routes

```typescript
// Ajouter dans src/app/app-routing.module.ts

const routes: Routes = [
  // ... autres routes
  {
    path: 'finance',
    children: [
      { path: 'dashboard', component: ChefFinanceDashboardComponent },
      { path: 'frais', component: FraisListComponent },
      { path: 'frais/dossier/:dossierId', component: FraisListComponent },
      { path: 'factures', component: FacturesListComponent },
      { path: 'factures/:id', component: FactureDetailComponent },
      { path: 'factures/dossier/:dossierId', component: FactureDetailComponent },
      { path: 'paiements', component: PaiementsGestionComponent },
      { path: 'paiements/facture/:factureId', component: PaiementsGestionComponent },
      { path: 'tarifs', component: TarifsGestionComponent }
    ]
  }
];
```

---

## ✅ 16. CHECKLIST DE VÉRIFICATION

### Services
- [ ] FinanceService créé et testé
- [ ] FluxFraisService créé et testé
- [ ] FactureService créé et testé
- [ ] PaiementService créé et testé
- [ ] TarifCatalogueService créé et testé

### Composants
- [ ] Dashboard Chef Financier créé
- [ ] Liste des Frais créée
- [ ] Détail Facture créé
- [ ] Liste Factures créée
- [ ] Gestion Paiements créée
- [ ] Gestion Tarifs créée

### Interfaces
- [ ] Toutes les interfaces TypeScript créées
- [ ] Enums créés (PhaseFrais, StatutFrais, etc.)

### Routes
- [ ] Routes configurées
- [ ] Navigation fonctionnelle

### Tests
- [ ] Workflow complet testé
- [ ] Tous les endpoints fonctionnels
- [ ] Validation des frais testée
- [ ] Génération de facture testée
- [ ] Paiements testés

---

**Fin du document**

