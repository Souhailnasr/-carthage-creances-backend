# 🎯 Prompt Frontend : Intégration Mise à Jour Automatique Score IA après Audiences

## 📋 Vue d'Ensemble

Le backend a été modifié pour **recalculer automatiquement le score IA** après chaque création ou modification d'audience. Le frontend doit maintenant :

1. **Détecter** que le score IA a été mis à jour après une opération sur une audience
2. **Rafraîchir** l'affichage du score IA dans l'interface
3. **Afficher** un indicateur visuel que le score a été recalculé
4. **Gérer** les cas où le recalcul est en cours (loading state)

---

## 🔄 Comportement Backend

### **Ce qui se passe côté backend :**

1. **Création d'audience** (`POST /api/audiences`)
   - L'audience est sauvegardée
   - Un événement `DossierDataChangedEvent` est publié
   - Le recalcul automatique du score IA est déclenché (asynchrone)
   - Le dossier est mis à jour avec le nouveau score IA

2. **Modification d'audience** (`PUT /api/audiences/{id}`)
   - L'audience est mise à jour
   - Un événement `DossierDataChangedEvent` est publié
   - Le recalcul automatique du score IA est déclenché (asynchrone)
   - Le dossier est mis à jour avec le nouveau score IA

### **Timing du Recalcul :**

- ⚠️ **Asynchrone** : Le recalcul se fait en arrière-plan
- ⏱️ **Délai** : 1-3 secondes après la sauvegarde de l'audience
- ✅ **Automatique** : Aucune action frontend requise pour déclencher le recalcul

---

## 📝 PROMPT 1 : Mise à Jour du Service Audience

**Prompt à copier dans Cursor AI :**

```
Dans le service AudienceService (src/app/services/audience.service.ts), modifiez les méthodes createAudience() et updateAudience() pour :

1. Après une création/modification réussie, attendre 2 secondes puis récupérer le dossier mis à jour pour obtenir le nouveau score IA
2. Retourner à la fois l'audience créée/modifiée ET le dossier mis à jour avec le nouveau score IA
3. Gérer les erreurs de récupération du dossier (ne pas faire échouer l'opération si le dossier ne peut pas être récupéré)

Structure de réponse suggérée :
- Retourner un objet contenant { audience, dossier } au lieu de juste l'audience
- Ou émettre deux événements séparés : un pour l'audience, un pour le dossier mis à jour
```

**Code suggéré :**

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError, timer } from 'rxjs';
import { catchError, switchMap, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AudienceService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api';
  private dossierApiUrl = `${this.apiUrl}/dossiers`;

  constructor(private http: HttpClient) {}

  /**
   * Crée une audience et récupère le dossier mis à jour avec le nouveau score IA
   */
  createAudience(audienceRequest: AudienceRequest): Observable<{ audience: Audience; dossier?: Dossier }> {
    return this.http.post<Audience>(`${this.apiUrl}/audiences`, audienceRequest).pipe(
      switchMap((audience) => {
        // Attendre 2 secondes pour laisser le temps au backend de recalculer le score IA
        return timer(2000).pipe(
          switchMap(() => {
            // Récupérer le dossier mis à jour
            const dossierId = audience.dossier?.id || audienceRequest.dossierId;
            if (dossierId) {
              return this.http.get<Dossier>(`${this.dossierApiUrl}/${dossierId}`).pipe(
                map((dossier) => ({ audience, dossier })),
                catchError(() => {
                  // Si la récupération du dossier échoue, retourner quand même l'audience
                  console.warn('Impossible de récupérer le dossier mis à jour');
                  return [{ audience }];
                })
              );
            }
            return [{ audience }];
          })
        );
      }),
      catchError((error) => {
        console.error('Erreur lors de la création de l\'audience:', error);
        let errorMessage = 'Erreur lors de la création de l\'audience';
        if (error.error?.message) {
          errorMessage = error.error.message;
        }
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Met à jour une audience et récupère le dossier mis à jour avec le nouveau score IA
   */
  updateAudience(id: number, audienceRequest: AudienceRequest): Observable<{ audience: Audience; dossier?: Dossier }> {
    return this.http.put<Audience>(`${this.apiUrl}/audiences/${id}`, audienceRequest).pipe(
      switchMap((audience) => {
        // Attendre 2 secondes pour laisser le temps au backend de recalculer le score IA
        return timer(2000).pipe(
          switchMap(() => {
            // Récupérer le dossier mis à jour
            const dossierId = audience.dossier?.id || audienceRequest.dossierId;
            if (dossierId) {
              return this.http.get<Dossier>(`${this.dossierApiUrl}/${dossierId}`).pipe(
                map((dossier) => ({ audience, dossier })),
                catchError(() => {
                  // Si la récupération du dossier échoue, retourner quand même l'audience
                  console.warn('Impossible de récupérer le dossier mis à jour');
                  return [{ audience }];
                })
              );
            }
            return [{ audience }];
          })
        );
      }),
      catchError((error) => {
        console.error('Erreur lors de la mise à jour de l\'audience:', error);
        let errorMessage = 'Erreur lors de la mise à jour de l\'audience';
        if (error.error?.message) {
          errorMessage = error.error.message;
        }
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}
```

---

## 📝 PROMPT 2 : Mise à Jour du Composant Formulaire Audience

**Prompt à copier dans Cursor AI :**

```
Dans le composant AudienceFormComponent (src/app/components/audience-form/audience-form.component.ts), modifiez la méthode onSubmit() pour :

1. Afficher un message de succès avec indication que le score IA est en cours de recalcul
2. Après réception de la réponse, mettre à jour l'affichage du score IA si le dossier est retourné
3. Émettre un événement pour notifier les composants parents que le score IA a été mis à jour
4. Afficher un indicateur visuel pendant le recalcul (2 secondes)

Ajoutez :
- Une propriété pour stocker l'état de recalcul : `recalculatingScore = false`
- Un message de notification : "Audience enregistrée. Recalcul du score IA en cours..."
- Une mise à jour du score IA dans l'interface après réception du dossier mis à jour
```

**Code suggéré :**

```typescript
export class AudienceFormComponent implements OnInit {
  recalculatingScore = false;
  
  // ... autres propriétés

  onSubmit(): void {
    if (this.audienceForm.invalid) {
      this.audienceForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.recalculatingScore = true; // Indicateur de recalcul
    
    const formValue = this.audienceForm.value;
    const audienceRequest: AudienceRequest = {
      dateAudience: formValue.dateAudience,
      dateProchaine: formValue.dateProchaine || undefined,
      tribunalType: formValue.tribunalType || undefined,
      lieuTribunal: formValue.lieuTribunal || undefined,
      commentaireDecision: formValue.commentaireDecision || undefined,
      resultat: formValue.resultat || undefined,
      dossierId: formValue.dossierId,
      avocatId: formValue.avocatId || undefined,
      huissierId: formValue.huissierId || undefined
    };

    if (!audienceRequest.dossierId) {
      this.snackBar.open('Le dossier est obligatoire', 'Fermer', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });
      this.loading = false;
      this.recalculatingScore = false;
      return;
    }

    const request = this.isEditMode && this.data.audience?.id
      ? this.audienceService.updateAudience(this.data.audience.id, audienceRequest)
      : this.audienceService.createAudience(audienceRequest);

    // Afficher un message indiquant que le recalcul est en cours
    this.snackBar.open(
      this.isEditMode 
        ? 'Audience modifiée. Recalcul du score IA en cours...' 
        : 'Audience créée. Recalcul du score IA en cours...',
      'Fermer',
      {
        duration: 3000,
        panelClass: ['info-snackbar']
      }
    );

    request.subscribe({
      next: (response) => {
        const { audience, dossier } = response;
        
        // Si le dossier mis à jour est retourné, mettre à jour le score IA
        if (dossier) {
          // Émettre un événement pour notifier les composants parents
          this.dialogRef.close({ 
            audience, 
            dossier, 
            scoreUpdated: true 
          });
          
          // Afficher un message de succès avec le nouveau score
          this.snackBar.open(
            `Score IA mis à jour : ${dossier.riskScore?.toFixed(1) || 'N/A'} (${dossier.riskLevel || 'N/A'})`,
            'Fermer',
            {
              duration: 5000,
              panelClass: ['success-snackbar']
            }
          );
        } else {
          // Si le dossier n'est pas retourné, fermer normalement
          this.dialogRef.close(audience);
          
          this.snackBar.open(
            this.isEditMode ? 'Audience modifiée avec succès' : 'Audience créée avec succès',
            'Fermer',
            {
              duration: 3000,
              panelClass: ['success-snackbar']
            }
          );
        }
        
        this.loading = false;
        this.recalculatingScore = false;
      },
      error: (error) => {
        console.error('Erreur lors de la sauvegarde:', error);
        const errorMessage = error.message || 'Erreur lors de la sauvegarde';
        this.snackBar.open(errorMessage, 'Fermer', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.loading = false;
        this.recalculatingScore = false;
      }
    });
  }
}
```

---

## 📝 PROMPT 3 : Mise à Jour du Composant Liste Audiences

**Prompt à copier dans Cursor AI :**

```
Dans le composant qui affiche la liste des audiences (ex: gestion-audiences.component.ts), modifiez le code pour :

1. Écouter les événements de mise à jour du score IA après création/modification d'audience
2. Rafraîchir automatiquement l'affichage du score IA du dossier concerné
3. Afficher un indicateur visuel (badge, animation) quand le score IA est mis à jour
4. Recharger les données du dossier si nécessaire

Ajoutez :
- Une méthode pour rafraîchir le score IA : `refreshDossierScore(dossierId: number)`
- Un indicateur visuel pour montrer que le score a été mis à jour récemment
- Une logique pour mettre à jour le score affiché sans recharger toute la page
```

**Code suggéré :**

```typescript
export class GestionAudiencesComponent implements OnInit {
  dossier: Dossier | null = null;
  scoreUpdated = false;

  constructor(
    private audienceService: AudienceService,
    private dossierService: DossierService,
    private dialog: MatDialog
  ) {}

  /**
   * Ouvre le formulaire d'audience et écoute les mises à jour du score IA
   */
  openAudienceForm(audience?: Audience): void {
    const dialogRef = this.dialog.open(AudienceFormComponent, {
      width: '800px',
      data: { audience, dossierId: this.dossier?.id }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Si le score IA a été mis à jour
        if (result.scoreUpdated && result.dossier) {
          // Mettre à jour le dossier local avec le nouveau score
          this.dossier = result.dossier;
          this.scoreUpdated = true;
          
          // Afficher un indicateur visuel pendant 5 secondes
          setTimeout(() => {
            this.scoreUpdated = false;
          }, 5000);
          
          // Optionnel : Recharger les audiences pour avoir les données à jour
          this.loadAudiences();
        } else if (result.audience) {
          // Si seulement l'audience a été retournée, recharger les audiences
          this.loadAudiences();
        }
      }
    });
  }

  /**
   * Rafraîchit le score IA du dossier
   */
  refreshDossierScore(dossierId: number): void {
    this.dossierService.getDossierById(dossierId).subscribe({
      next: (dossier) => {
        this.dossier = dossier;
        this.scoreUpdated = true;
        
        // Masquer l'indicateur après 5 secondes
        setTimeout(() => {
          this.scoreUpdated = false;
        }, 5000);
      },
      error: (error) => {
        console.error('Erreur lors du rafraîchissement du score:', error);
      }
    });
  }
}
```

**Template HTML suggéré :**

```html
<!-- Afficher le score IA avec indicateur de mise à jour -->
<div class="score-ia-container" [class.score-updated]="scoreUpdated">
  <mat-card>
    <mat-card-header>
      <mat-card-title>Score IA</mat-card-title>
      <span *ngIf="scoreUpdated" class="update-badge">
        <mat-icon>refresh</mat-icon>
        Mis à jour
      </span>
    </mat-card-header>
    <mat-card-content>
      <div class="score-display">
        <div class="score-value">{{ dossier?.riskScore?.toFixed(1) || 'N/A' }}</div>
        <div class="score-level" [ngClass]="getRiskLevelClass(dossier?.riskLevel)">
          {{ dossier?.riskLevel || 'N/A' }}
        </div>
        <div class="score-date" *ngIf="dossier?.datePrediction">
          Mis à jour : {{ dossier.datePrediction | date:'short' }}
        </div>
      </div>
    </mat-card-content>
  </mat-card>
</div>
```

**CSS suggéré :**

```css
.score-ia-container {
  position: relative;
}

.score-updated {
  animation: scoreUpdatePulse 0.5s ease-in-out;
}

@keyframes scoreUpdatePulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); background-color: rgba(76, 175, 80, 0.1); }
  100% { transform: scale(1); }
}

.update-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background-color: #4caf50;
  color: white;
  border-radius: 12px;
  font-size: 12px;
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.score-display {
  text-align: center;
  padding: 16px;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  color: #1976d2;
  margin-bottom: 8px;
}

.score-level {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 8px;
}

.score-level.faible { color: #4caf50; }
.score-level.moyen { color: #ff9800; }
.score-level.élevé { color: #f44336; }

.score-date {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
}
```

---

## 📝 PROMPT 4 : Mise à Jour du Service Dossier (Optionnel)

**Prompt à copier dans Cursor AI :**

```
Dans le service DossierService (src/app/services/dossier.service.ts), ajoutez une méthode pour récupérer uniquement le score IA d'un dossier sans charger toutes les données :

1. Créer une méthode : `getDossierScore(dossierId: number): Observable<DossierScore>`
2. Cette méthode appelle un endpoint optimisé (ou récupère le dossier complet mais ne retourne que le score)
3. Utiliser cette méthode pour rafraîchir rapidement le score après une opération sur une audience

Interface suggérée :
```typescript
interface DossierScore {
  id: number;
  riskScore: number;
  riskLevel: string;
  etatPrediction: string;
  datePrediction: Date;
}
```
```

**Code suggéré :**

```typescript
@Injectable({
  providedIn: 'root'
})
export class DossierService {
  private apiUrl = 'http://localhost:8089/carthage-creance/api/dossiers';

  constructor(private http: HttpClient) {}

  /**
   * Récupère uniquement le score IA d'un dossier (optimisé)
   */
  getDossierScore(dossierId: number): Observable<DossierScore> {
    return this.http.get<Dossier>(`${this.apiUrl}/${dossierId}`).pipe(
      map((dossier) => ({
        id: dossier.id!,
        riskScore: dossier.riskScore || 0,
        riskLevel: dossier.riskLevel || 'N/A',
        etatPrediction: dossier.etatPrediction || 'NOT_RECOVERED',
        datePrediction: dossier.datePrediction || new Date()
      })),
      catchError((error) => {
        console.error('Erreur lors de la récupération du score:', error);
        return throwError(() => new Error('Erreur lors de la récupération du score IA'));
      })
    );
  }
}
```

---

## 🎨 PROMPT 5 : Amélioration UX - Indicateur de Recalcul

**Prompt à copier dans Cursor AI :**

```
Ajoutez un indicateur visuel élégant pour montrer que le score IA est en cours de recalcul après une opération sur une audience :

1. Créer un composant ou une directive pour l'indicateur de recalcul
2. Afficher une animation de chargement subtile pendant le recalcul (2 secondes)
3. Afficher une animation de succès quand le score est mis à jour
4. Utiliser des couleurs cohérentes avec le design system

Fonctionnalités :
- Animation de chargement (spinner ou skeleton)
- Badge "Recalcul en cours..."
- Animation de transition quand le score change
- Message de confirmation "Score mis à jour"
```

**Code suggéré (Composant) :**

```typescript
@Component({
  selector: 'app-score-ia-indicator',
  template: `
    <div class="score-indicator" [class.recalculating]="recalculating" [class.updated]="updated">
      <div class="score-content">
        <div class="score-value">{{ score?.toFixed(1) || 'N/A' }}</div>
        <div class="score-level" [ngClass]="level">{{ levelLabel }}</div>
      </div>
      
      <div class="indicator-overlay" *ngIf="recalculating">
        <mat-spinner diameter="24"></mat-spinner>
        <span>Recalcul en cours...</span>
      </div>
      
      <div class="update-badge" *ngIf="updated && !recalculating">
        <mat-icon>check_circle</mat-icon>
        <span>Mis à jour</span>
      </div>
    </div>
  `,
  styles: [`
    .score-indicator {
      position: relative;
      padding: 16px;
      border-radius: 8px;
      background: white;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      transition: all 0.3s ease;
    }
    
    .recalculating {
      opacity: 0.7;
      pointer-events: none;
    }
    
    .updated {
      animation: updatePulse 0.5s ease-in-out;
    }
    
    @keyframes updatePulse {
      0%, 100% { transform: scale(1); }
      50% { transform: scale(1.02); box-shadow: 0 4px 8px rgba(76, 175, 80, 0.3); }
    }
    
    .indicator-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.9);
      border-radius: 8px;
      gap: 8px;
    }
    
    .update-badge {
      position: absolute;
      top: -8px;
      right: -8px;
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 4px 8px;
      background: #4caf50;
      color: white;
      border-radius: 12px;
      font-size: 11px;
      animation: fadeInSlide 0.3s ease;
    }
    
    @keyframes fadeInSlide {
      from {
        opacity: 0;
        transform: translateY(-10px) scale(0.8);
      }
      to {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
    }
  `]
})
export class ScoreIaIndicatorComponent {
  @Input() score: number | null = null;
  @Input() level: string | null = null;
  @Input() recalculating = false;
  @Input() updated = false;
  
  get levelLabel(): string {
    const labels: { [key: string]: string } = {
      'FAIBLE': 'Faible',
      'MOYEN': 'Moyen',
      'ÉLEVÉ': 'Élevé',
      'ELEVE': 'Élevé'
    };
    return labels[this.level || ''] || this.level || 'N/A';
  }
}
```

---

## ✅ Checklist d'Intégration

- [ ] **Service AudienceService** : Modifié pour récupérer le dossier mis à jour après création/modification
- [ ] **Composant AudienceFormComponent** : Affiche le message de recalcul et gère la réponse avec dossier
- [ ] **Composant Liste Audiences** : Écoute les mises à jour et rafraîchit l'affichage du score
- [ ] **Indicateur visuel** : Animation/indicateur pendant le recalcul et après la mise à jour
- [ ] **Gestion d'erreurs** : Gestion gracieuse si le dossier ne peut pas être récupéré
- [ ] **Tests** : Tester la création et modification d'audience avec vérification du score mis à jour

---

## 🔍 Points d'Attention

1. **Timing** : Le recalcul prend 1-3 secondes. Le délai de 2 secondes dans le service est une estimation. Ajustez si nécessaire.

2. **Gestion d'erreurs** : Si la récupération du dossier échoue, l'opération sur l'audience ne doit pas échouer. L'utilisateur peut toujours recharger manuellement.

3. **Performance** : Éviter de recharger toutes les données du dossier si seul le score IA a changé.

4. **UX** : L'indicateur de recalcul doit être subtil mais visible. Ne pas surcharger l'interface.

---

**Date** : 2024-12-03  
**Version Backend** : ✅ Recalcul automatique activé  
**Version Frontend** : ⏳ À implémenter



