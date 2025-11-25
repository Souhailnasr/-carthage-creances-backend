# 🎯 Prompts pour l'Amélioration du Frontend - Système de Recouvrement Tunisien

## 📋 Vue d'Ensemble

Ce document contient tous les prompts nécessaires pour intégrer les nouvelles fonctionnalités du backend dans le frontend sans casser l'application existante.

---

## 🚀 PARTIE 1 : Intégration des Montants et États de Dossier

### Prompt 1.1 : Ajouter les Champs Montants dans l'Interface Dossier

```
You are an expert frontend developer. Integrate the new amount fields and dossier state into the existing dossier management interface.

REQUIREMENTS:

1. Add the following fields to the Dossier interface/type:
   - montantTotal: number (required)
   - montantRecouvre: number (default: 0)
   - montantRestant: number (calculated automatically)
   - etatDossier: "RECOVERED_TOTAL" | "RECOVERED_PARTIAL" | "NOT_RECOVERED"

2. Update the dossier form to include:
   - Input field for montantTotal (required, must be >= 0)
   - Display field for montantRecouvre (read-only, shows current amount)
   - Display field for montantRestant (read-only, calculated: montantTotal - montantRecouvre)
   - Display field for etatDossier (read-only, with color coding:
     * RECOVERED_TOTAL: green badge
     * RECOVERED_PARTIAL: orange badge
     * NOT_RECOVERED: red badge)

3. Update the dossier list/table to show:
   - montantTotal column
   - montantRecouvre column
   - montantRestant column
   - etatDossier column (with badge styling)

4. Ensure all API calls to GET /dossiers/{id} and POST/PUT /dossiers include these fields.

5. Add validation:
   - montantTotal must be >= 0
   - montantRecouvre must be >= 0
   - montantRestant is calculated automatically (don't allow manual input)

6. Display format:
   - All amounts should be formatted as currency (TND) with 2 decimal places
   - Example: 1,234.56 TND

Make sure the changes are backward compatible and don't break existing functionality.
```

### Prompt 1.2 : Créer l'Endpoint pour Mettre à Jour les Montants

```
You are an expert frontend developer. Create a service function and UI component to update dossier amounts.

REQUIREMENTS:

1. Create a service function `updateDossierMontants(dossierId, montantData)` that calls:
   - PUT /dossiers/{dossierId}/montant
   - Body: { montantTotal?: number, montantRecouvre?: number, updateMode: "ADD" | "SET" }

2. Create a modal/dialog component "UpdateMontantDialog" with:
   - Input for montantTotal (optional)
   - Input for montantRecouvre (optional)
   - Radio buttons or dropdown for updateMode:
     * "ADD" - Add to existing montantRecouvre
     * "SET" - Replace existing montantRecouvre
   - Validation: amounts must be >= 0
   - Display current values before update
   - Preview of new values after update

3. Add a button "Mettre à jour les montants" in the dossier detail page.

4. After successful update:
   - Show success notification
   - Refresh dossier data
   - Display updated amounts and etatDossier

5. Handle errors gracefully with user-friendly messages.

Ensure the component is reusable and follows your existing design patterns.
```

### Prompt 1.3 : Ajouter l'Action Amiable avec Montant Recouvré

```
You are an expert frontend developer. Add functionality to record amiable action responses with recovered amounts.

REQUIREMENTS:

1. Update the "Action Amiable" form to include:
   - Radio buttons or toggle for debtor response:
     * "Réponse positive" (positive response)
     * "Réponse négative" (negative response)
   - If "Réponse positive" is selected:
     * Show input field for montantRecouvre (required, >= 0)
     * Show preview of montantRestant after update
   - If "Réponse négative" is selected:
     * Hide montantRecouvre field
     * Show message: "Aucun montant recouvré"

2. Create API call:
   - POST /dossiers/{id}/amiable
   - Body: { montantRecouvre: number }

3. After submitting amiable action:
   - If positive response: call POST /dossiers/{id}/amiable with montantRecouvre
   - Update dossier display with new montantRecouvre, montantRestant, and etatDossier
   - Show success notification

4. Display the updated dossier state:
   - Show badge for etatDossier
   - Show updated amounts
   - Show message: "Montant recouvré: X TND, Montant restant: Y TND"

5. Add validation:
   - montantRecouvre cannot exceed montantTotal
   - montantRecouvre must be >= 0

Ensure the UI is intuitive and guides the user through the process.
```

---

## 🚀 PARTIE 2 : Module Huissier - Documents (Phase 1 & 2)

### Prompt 2.1 : Interface de Gestion des Documents Huissier

```
You are an expert frontend developer. Create a complete interface for managing huissier documents (Phase 1 & 2).

REQUIREMENTS:

1. Create a new page/route: "/dossiers/{id}/huissier/documents"

2. Create a table/list component "DocumentHuissierList" displaying:
   - Type de document (PV_MISE_EN_DEMEURE, ORDONNANCE_PAIEMENT, PV_NOTIFICATION_ORDONNANCE)
   - Date de création
   - Délai légal (days)
   - Date d'expiration (calculated: dateCreation + delaiLegalDays)
   - Statut (PENDING, EXPIRED, COMPLETED) with color badges:
     * PENDING: blue
     * EXPIRED: red
     * COMPLETED: green
   - Nom de l'huissier
   - Actions: View, Download (if pieceJointeUrl exists)

3. Create a form "CreateDocumentHuissierForm" with:
   - Dropdown for typeDocument (required):
     * PV_MISE_EN_DEMEURE (délai: 10 jours)
     * ORDONNANCE_PAIEMENT (délai: 20 jours)
     * PV_NOTIFICATION_ORDONNANCE (délai: 20 jours)
   - Input for huissierName (required)
   - File upload for pieceJointeUrl (optional)
   - Display calculated delaiLegalDays based on typeDocument
   - Display calculated expiration date

4. API Integration:
   - GET /huissier/documents?dossierId={id} - List documents
   - POST /huissier/document - Create document
   - Body: { dossierId, typeDocument, huissierName, pieceJointeUrl? }

5. Add visual indicators:
   - Show countdown timer for PENDING documents (days remaining)
   - Highlight EXPIRED documents in red
   - Show warning icon if document expires in < 3 days

6. Add filters:
   - Filter by typeDocument
   - Filter by status
   - Filter by huissierName

7. Add sorting:
   - By dateCreation (newest first)
   - By expiration date (soonest first)

Ensure the interface is user-friendly and follows Tunisian legal process workflow.
```

### Prompt 2.2 : Notifications et Rappels pour Documents Huissier

```
You are an expert frontend developer. Integrate notifications and reminders for huissier documents.

REQUIREMENTS:

1. Create a notification component "DocumentReminderNotification" that displays:
   - Type: DELAY_WARNING (2 days before expiration)
   - Type: DELAY_EXPIRED (after expiration)
   - Message with document type, dossier reference, and remaining amount
   - Recommendation text
   - Action button: "Voir le document"

2. Integrate with notification system:
   - GET /notifications?dossierId={id}&type=DELAY_WARNING
   - GET /notifications?dossierId={id}&type=DELAY_EXPIRED
   - Display notifications in the dossier detail page
   - Show notification badge with count

3. Create reminder display:
   - Show countdown: "X jours restants" for PENDING documents
   - Show warning: "⚠️ Expire dans X jours" if < 3 days
   - Show alert: "🔴 Délai expiré" for EXPIRED documents

4. Add auto-refresh:
   - Poll notifications every 5 minutes
   - Update document status automatically

5. Display recommendations:
   - If PV_MISE_EN_DEMEURE expired: "Déposer demande ordonnance de paiement"
   - If ORDONNANCE_PAIEMENT expired: "Initier action d'exécution (saisie)"
   - Show as actionable buttons with links to create next document/action

Ensure notifications are prominent but not intrusive.
```

---

## 🚀 PARTIE 3 : Module Huissier - Actions d'Exécution (Phase 3)

### Prompt 3.1 : Interface de Gestion des Actions Huissier

```
You are an expert frontend developer. Create a complete interface for managing huissier execution actions (Phase 3).

REQUIREMENTS:

1. Create a new page/route: "/dossiers/{id}/huissier/actions"

2. Create a table/list component "ActionHuissierList" displaying:
   - Type d'action (ACLA_TA7AFOUDHIA, ACLA_TANFITHIA, ACLA_TAW9IFIYA, ACLA_A9ARYA)
   - Date d'action
   - Montant recouvré
   - Montant restant
   - État du dossier (RECOVERED_TOTAL, RECOVERED_PARTIAL, NOT_RECOVERED)
   - Nom de l'huissier
   - Actions: View, Download (if pieceJointeUrl exists)

3. Create a form "CreateActionHuissierForm" with:
   - Dropdown for typeAction (required):
     * ACLA_TA7AFOUDHIA (العقلة التحفظية - Saisie conservatoire)
     * ACLA_TANFITHIA (العقلة التنفيذية - Saisie exécutive)
     * ACLA_TAW9IFIYA (العقلة التوقيفية - Saisie de blocage)
     * ACLA_A9ARYA (العقلة العقارية - Saisie immobilière)
   - Input for montantRecouvre (optional, >= 0)
   - Radio buttons for updateMode:
     * "ADD" - Ajouter au montant existant
     * "SET" - Remplacer le montant existant
   - Input for huissierName (required)
   - File upload for pieceJointeUrl (optional)
   - Preview section showing:
     * Current montantTotal
     * Current montantRecouvre
     * New montantRecouvre (after update)
     * Calculated montantRestant
     * Calculated etatDossier

4. API Integration:
   - GET /huissier/actions?dossierId={id} - List actions
   - POST /huissier/action - Create action
   - Body: { dossierId, typeAction, montantRecouvre?, pieceJointeUrl?, updateMode: "ADD"|"SET" }

5. Add validation:
   - montantRecouvre cannot exceed montantTotal
   - montantRecouvre must be >= 0
   - Show error if montantRecouvre > montantTotal

6. Display action history:
   - Timeline view showing all actions chronologically
   - Show progression of montantRecouvre over time
   - Show chart/graph of recovery progress

7. Add filters:
   - Filter by typeAction
   - Filter by date range
   - Filter by huissierName

Ensure the interface clearly shows the Tunisian legal execution process.
```

### Prompt 3.2 : Mise à Jour Automatique des Montants après Action

```
You are an expert frontend developer. Implement automatic amount and state updates after huissier actions.

REQUIREMENTS:

1. After creating an action huissier:
   - Automatically refresh dossier data
   - Update montantRecouvre, montantRestant, and etatDossier
   - Show success notification with updated values

2. Display updated state:
   - Show badge for new etatDossier with appropriate color
   - Show updated amounts with highlight animation
   - Show message: "Action effectuée: {typeAction}. Montant recouvré: {montantRecouvre} TND. Montant restant: {montantRestant} TND."

3. If etatDossier becomes RECOVERED_TOTAL:
   - Show celebration message: "🎉 Dossier totalement recouvré!"
   - Disable further action creation (or show warning)
   - Suggest closing the dossier

4. If etatDossier becomes RECOVERED_PARTIAL:
   - Show progress bar: "X% recouvré"
   - Show remaining amount prominently
   - Suggest next actions based on recommendations

5. Real-time updates:
   - If multiple users are viewing the same dossier, update in real-time
   - Use WebSocket or polling to sync changes

6. Add confirmation dialog:
   - Before creating action, show preview of changes
   - "Cette action va mettre à jour le montant recouvré de {current} à {new} TND. Continuer?"

Ensure the user understands the impact of each action.
```

---

## 🚀 PARTIE 4 : Système de Notifications et Recommandations

### Prompt 4.1 : Interface de Notifications Huissier

```
You are an expert frontend developer. Create a comprehensive notification system for huissier operations.

REQUIREMENTS:

1. Create a notification center component "NotificationCenter" with:
   - List of all notifications (in-app, email, SMS)
   - Filter by type:
     * DELAY_WARNING
     * DELAY_EXPIRED
     * ACTION_PERFORMED
     * AMIABLE_RESPONSE_POSITIVE
     * AMIABLE_RESPONSE_NEGATIVE
     * AMOUNT_UPDATED
     * DOCUMENT_CREATED
     * STATUS_CHANGED
   - Filter by channel: IN_APP, EMAIL, SMS, WEBHOOK
   - Filter by dossier
   - Filter by acknowledged/unacknowledged

2. Display each notification with:
   - Type badge with icon
   - Message (French + Arabic if available)
   - Dossier reference (clickable link)
   - Timestamp
   - Channel indicator
   - Acknowledge button
   - Associated recommendation (if exists)

3. API Integration:
   - GET /notifications?dossierId={id} - Get notifications
   - POST /notifications/{id}/ack - Acknowledge notification
   - Body: { userId }

4. Add notification badge:
   - Show count of unacknowledged notifications
   - Update in real-time
   - Click to open notification center

5. Notification templates (display in UI):
   - Reminder: "Rappel: {typeDocument} du dossier {dossierRef} expire le {date}. Montant restant: {montantRestant} TND."
   - Expired: "Expiration: délai légal terminé pour {typeDocument} du dossier {dossierRef}."
   - Action: "Action {typeAction} réalisée par {huissierName} pour dossier {dossierRef}. Montant recouvré: {montantRecouvre} TND."

6. Add Arabic versions (for huissier UI):
   - Reminder: "تذكير: {typeDocument} ينتهي في {date}، المبلغ المتبقي {montantRestant} د.ت."
   - Expired: "انتهاء الأجل القانوني لـ {typeDocument}، التوصية: {recommendation}"

7. Real-time updates:
   - Poll for new notifications every 30 seconds
   - Show toast notifications for new items
   - Play sound (optional, configurable)

Ensure notifications are accessible and actionable.
```

### Prompt 4.2 : Interface de Recommandations Intelligentes

```
You are an expert frontend developer. Create an intelligent recommendation system interface.

REQUIREMENTS:

1. Create a recommendations panel "RecommendationsPanel" displaying:
   - List of recommendations for a dossier
   - Priority badges (LOW, MEDIUM, HIGH)
   - Rule code
   - Title and description
   - Action button (if applicable)

2. Display recommendations with:
   - Priority color coding:
     * HIGH: red/orange
     * MEDIUM: yellow
     * LOW: blue
   - Rule code badge
   - Title (bold)
   - Description
   - Acknowledge button
   - Action button (if recommendation has actionable CTA)

3. API Integration:
   - GET /recommendations?dossierId={id} - Get recommendations
   - POST /recommendations/{id}/ack - Acknowledge recommendation
   - Body: { userId }

4. Recommendation rules mapping to UI actions:
   - ESCALATE_TO_ORDONNANCE → Button: "Créer ordonnance de paiement" → Opens create document form with type ORDONNANCE_PAIEMENT
   - INITIATE_EXECUTION → Button: "Initier action d'exécution" → Opens create action form with type ACLA_TA7AFOUDHIA
   - ASSIGN_AVOCAT → Button: "Assigner avocat" → Opens assign avocat form
   - INITIATE_BANK_SAISIE → Button: "Saisie compte bancaire" → Opens bank saisie form
   - ESCALATE_TO_DIRECTOR → Button: "Escalader au directeur" → Opens escalation form

5. Show recommendations in:
   - Dossier detail page (sidebar or panel)
   - Notification center (linked to notifications)
   - Dashboard (high priority only)

6. Add recommendation history:
   - Show acknowledged recommendations
   - Show when recommendation was created and acknowledged
   - Show who acknowledged it

7. Auto-dismiss:
   - If recommendation is acted upon (e.g., document created), auto-acknowledge related recommendations
   - If dossier state changes (e.g., RECOVERED_TOTAL), dismiss all pending recommendations

8. Recommendation examples to display:
   - "Si non payé, déposer ordonnance de paiement." (Priority: HIGH)
   - "Notifier le débiteur de l'ordonnance." (Priority: HIGH)
   - "Initier action d'exécution (saisie conservatoire)." (Priority: HIGH)
   - "Assigner un avocat pour ce dossier." (Priority: MEDIUM)
   - "Considérer la saisie du compte bancaire." (Priority: MEDIUM)

Ensure recommendations are clear, actionable, and integrated into the workflow.
```

---

## 🚀 PARTIE 5 : Scheduler et Vérifications Automatiques

### Prompt 5.1 : Affichage des Délais Légaux et Expirations

```
You are an expert frontend developer. Create UI components to display legal delays and expirations.

REQUIREMENTS:

1. Create a "LegalDelaysWidget" component showing:
   - List of documents with pending legal delays
   - Countdown timer for each document
   - Days remaining until expiration
   - Visual indicators:
     * Green: > 5 days remaining
     * Yellow: 3-5 days remaining
     * Orange: 1-2 days remaining
     * Red: Expired

2. Display in dashboard:
   - Summary card: "X documents expirant dans les 3 prochains jours"
   - Summary card: "Y documents expirés nécessitant une action"
   - List of urgent documents

3. Create "ExpiredDocumentsAlert" component:
   - Show alert banner for expired documents
   - List expired documents with:
     * Document type
     * Dossier reference
     * Days expired
     * Recommended action
   - Action button: "Créer document suivant" or "Créer action d'exécution"

4. Auto-refresh:
   - Update countdown timers every minute
   - Refresh expired documents list every 5 minutes
   - Show real-time updates

5. Add filters:
   - Show only documents expiring in next 3 days
   - Show only expired documents
   - Show all pending documents

6. Add sorting:
   - By expiration date (soonest first)
   - By priority (based on days remaining)

Ensure the UI helps users prioritize urgent actions.
```

---

## 🚀 PARTIE 6 : Audit Log et Historique

### Prompt 6.1 : Interface d'Audit Log

```
You are an expert frontend developer. Create an audit log interface to track all changes.

REQUIREMENTS:

1. Create an "AuditLogView" component displaying:
   - Timeline of all changes to a dossier
   - Change type (AMOUNT_UPDATE, DOCUMENT_CREATE, ACTION_CREATE, STATUS_UPDATE)
   - User who made the change
   - Timestamp
   - Before/after values (formatted JSON or key-value pairs)
   - Description

2. Display format:
   - Timeline view (vertical)
   - Each entry shows:
     * Icon based on change type
     * User name/email
     * Timestamp (relative: "2 hours ago")
     * Change description
     * Expandable section showing before/after values

3. API Integration:
   - GET /audit-logs?dossierId={id} - Get audit logs
   - Display in dossier detail page (new tab: "Historique")

4. Add filters:
   - Filter by change type
   - Filter by user
   - Filter by date range

5. Add search:
   - Search in descriptions
   - Search in before/after values

6. Format before/after values:
   - If amount change: "Montant total: 1000 TND → 1500 TND"
   - If status change: "État: NOT_RECOVERED → RECOVERED_PARTIAL"
   - If document created: "Document créé: PV_MISE_EN_DEMEURE"
   - Pretty print JSON for complex changes

7. Add export:
   - Export audit log as CSV
   - Export audit log as PDF

Ensure the audit log is comprehensive and easy to understand.
```

---

## 🚀 PARTIE 7 : Intégration Complète et Workflow

### Prompt 7.1 : Workflow Complet du Recouvrement Tunisien

```
You are an expert frontend developer. Create a complete workflow interface for the Tunisian debt recovery process.

REQUIREMENTS:

1. Create a "RecoveryWorkflow" component showing the 3 phases:

   PHASE 1: Mise en demeure
   - Step 1: Create PV_MISE_EN_DEMEURE document
   - Step 2: Wait 10 days (legal delay)
   - Step 3: Check if paid
   - If not paid → Proceed to Phase 2

   PHASE 2: Ordonnance de paiement
   - Step 1: Create ORDONNANCE_PAIEMENT document
   - Step 2: Create PV_NOTIFICATION_ORDONNANCE document
   - Step 3: Wait 20 days (legal delay)
   - Step 4: Check if paid
   - If not paid → Proceed to Phase 3

   PHASE 3: Exécution
   - Step 1: Create execution action (ACLA_TA7AFOUDHIA, etc.)
   - Step 2: Update montantRecouvre
   - Step 3: Check if fully recovered
   - If not → Continue with other execution actions

2. Visual workflow:
   - Show current phase with progress indicator
   - Show completed steps (green checkmark)
   - Show pending steps (gray)
   - Show current step (highlighted)
   - Show next recommended action

3. Add navigation:
   - "Créer document Phase 1" button
   - "Créer document Phase 2" button
   - "Créer action Phase 3" button
   - Quick actions based on recommendations

4. Display dossier state:
   - Current phase
   - Days remaining in current phase
   - Next action recommended
   - Overall progress (percentage recovered)

5. Add shortcuts:
   - "Action rapide" menu with common actions
   - Keyboard shortcuts for power users
   - Bulk actions for multiple dossiers

6. Integration with notifications:
   - Show workflow-related notifications
   - Link notifications to workflow steps
   - Auto-advance workflow when actions are completed

Ensure the workflow is intuitive and guides users through the legal process.
```

### Prompt 7.2 : Dashboard Amélioré avec Statistiques de Recouvrement

```
You are an expert frontend developer. Enhance the dashboard with recovery statistics and insights.

REQUIREMENTS:

1. Add recovery statistics cards:
   - Total dossiers: X
   - Montant total à recouvrer: X TND
   - Montant total recouvré: X TND
   - Taux de recouvrement: X%
   - Dossiers totalement recouvrés: X
   - Dossiers partiellement recouvrés: X
   - Dossiers non recouvrés: X

2. Add charts:
   - Pie chart: Distribution by etatDossier
   - Line chart: Montant recouvré over time
   - Bar chart: Actions huissier by type
   - Timeline: Documents created over time

3. Add alerts section:
   - Documents expiring in next 3 days
   - Expired documents requiring action
   - High-priority recommendations
   - Dossiers with no activity > 90 days

4. Add recent activity:
   - Last 10 actions (documents created, actions performed)
   - Last 10 amount updates
   - Last 10 status changes

5. Add filters:
   - Filter by date range
   - Filter by etatDossier
   - Filter by phase (1, 2, 3)

6. API Integration:
   - GET /statistics/recovery - Get recovery statistics
   - GET /statistics/documents - Get document statistics
   - GET /statistics/actions - Get action statistics

7. Real-time updates:
   - Refresh statistics every 5 minutes
   - Show loading states
   - Handle errors gracefully

Ensure the dashboard provides actionable insights.
```

---

## 🚀 PARTIE 8 : Responsive Design et Accessibilité

### Prompt 8.1 : Design Responsive et Mobile-Friendly

```
You are an expert frontend developer. Ensure all new components are responsive and mobile-friendly.

REQUIREMENTS:

1. Make all forms responsive:
   - Stack inputs vertically on mobile
   - Use full-width inputs on mobile
   - Ensure touch-friendly button sizes (min 44x44px)

2. Make tables responsive:
   - Convert to cards on mobile
   - Show only essential columns on mobile
   - Add "View more" expandable sections

3. Make notifications responsive:
   - Stack notifications vertically on mobile
   - Use swipe gestures for acknowledge action
   - Show notification count badge on mobile

4. Make workflow component responsive:
   - Show vertical timeline on mobile
   - Collapsible phases on mobile
   - Touch-friendly action buttons

5. Test on:
   - Mobile devices (iOS, Android)
   - Tablets
   - Desktop (various screen sizes)

6. Ensure:
   - No horizontal scrolling
   - Text is readable (min 16px font size)
   - Buttons are easily tappable
   - Forms are easy to fill on mobile

Ensure the application works seamlessly on all devices.
```

---

## 📝 NOTES IMPORTANTES

### Compatibilité
- Tous les nouveaux champs doivent être optionnels dans les requêtes GET pour la compatibilité ascendante
- Les valeurs par défaut doivent être gérées côté backend
- Ne pas casser les fonctionnalités existantes

### Validation
- Valider tous les montants (>= 0)
- Valider que montantRecouvre <= montantTotal
- Valider les dates et délais légaux
- Afficher des messages d'erreur clairs

### Performance
- Utiliser la pagination pour les listes longues
- Implémenter le lazy loading pour les données volumineuses
- Mettre en cache les statistiques
- Optimiser les requêtes API

### Sécurité
- Vérifier les permissions avant d'afficher les actions
- Valider les données côté client ET serveur
- Sanitizer les entrées utilisateur
- Protéger contre les injections

### Internationalisation
- Support français (principal)
- Support arabe (pour les huissiers)
- Formater les dates selon la locale
- Formater les montants avec devise (TND)

---

## ✅ Checklist d'Intégration

- [ ] Interface Dossier avec champs montants
- [ ] Endpoint mise à jour montants
- [ ] Action amiable avec montant recouvré
- [ ] Interface documents huissier (Phase 1 & 2)
- [ ] Notifications documents huissier
- [ ] Interface actions huissier (Phase 3)
- [ ] Mise à jour automatique montants
- [ ] Centre de notifications
- [ ] Panneau de recommandations
- [ ] Widget délais légaux
- [ ] Interface audit log
- [ ] Workflow complet
- [ ] Dashboard amélioré
- [ ] Design responsive
- [ ] Tests sur mobile/tablet/desktop

---

## 🎯 Priorités

1. **Priorité HAUTE** : Montants et états de dossier, Actions amiable
2. **Priorité MOYENNE** : Documents huissier, Actions huissier
3. **Priorité BASSE** : Notifications, Recommandations, Audit log

---

**Bon développement ! 🚀**

