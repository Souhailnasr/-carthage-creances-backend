# 📋 Explication : Comment et Quand le Statut d'un Document Huissier Change

## 🎯 Vue d'Ensemble

Le statut d'un document huissier peut avoir **3 valeurs** :
- ✅ **PENDING** : En attente (délai légal non expiré)
- ⚠️ **EXPIRED** : Délai légal expiré
- ✅ **COMPLETED** : Complété (action suivante effectuée)

---

## 📊 Cycle de Vie du Statut

```
CRÉATION → PENDING → EXPIRED (automatique) ou COMPLETED (manuel)
```

---

## 1️⃣ **PENDING** - Statut Initial

### **Quand** : À la création du document

### **Comment** :
- Le statut est défini automatiquement à `PENDING` lors de la création
- Code dans `DocumentHuissierServiceImpl.createDocument()` :
  ```java
  .status(StatutDocumentHuissier.PENDING)
  ```

### **Détails** :
- **Délai légal** :
  - `PV_MISE_EN_DEMEURE` : **10 jours**
  - `ORDONNANCE_PAIEMENT` : **20 jours**
  - `PV_NOTIFICATION_ORDONNANCE` : **20 jours**
- Le document reste en `PENDING` tant que le délai légal n'est pas expiré

---

## 2️⃣ **EXPIRED** - Expiration Automatique

### **Quand** : Automatiquement toutes les 10 minutes

### **Comment** :
Le **scheduler automatique** (`LegalDelayScheduler`) vérifie tous les documents en `PENDING` :

```java
@Scheduled(cron = "0 */10 * * * *") // Toutes les 10 minutes
public void checkExpiredDocuments() {
    // Récupère tous les documents PENDING
    List<DocumentHuissier> pendingDocuments = documentHuissierRepository.findByStatus(StatutDocumentHuissier.PENDING);
    
    for (DocumentHuissier document : pendingDocuments) {
        Instant deadline = creation.plus(delaiLegalDays, ChronoUnit.DAYS);
        
        // Si le délai est dépassé
        if (!deadline.isAfter(now)) {
            documentHuissierService.markAsExpired(document.getId());
        }
    }
}
```

### **Calcul de l'expiration** :
- **Date d'expiration** = `dateCreation + delaiLegalDays`
- **Exemple** :
  - Document créé le **1er janvier** avec délai de **10 jours**
  - Date d'expiration : **11 janvier**
  - Le scheduler vérifie toutes les 10 minutes si `now >= 11 janvier`

### **Actions déclenchées lors de l'expiration** :
1. ✅ **Changement de statut** : `PENDING` → `EXPIRED`
2. 📧 **Notification** : Envoi d'une notification d'expiration
3. 💡 **Recommandation** : Création d'une recommandation d'escalade

### **Méthode manuelle** :
Vous pouvez aussi marquer un document comme expiré manuellement via l'API :
```
PUT /api/huissier/document/{id}/expire
```

---

## 3️⃣ **COMPLETED** - Complétion Manuelle

### **Quand** : Lorsqu'une action suivante est effectuée

### **Comment** :
⚠️ **IMPORTANT** : Le statut `COMPLETED` n'est **PAS automatiquement géré** dans le code actuel !

### **Logique attendue** :
Le statut devrait passer à `COMPLETED` quand :
- Une **action huissier** est créée pour le même dossier
- Une **audience** est créée pour le même dossier
- L'utilisateur marque manuellement le document comme complété

### **Implémentation recommandée** :

#### **Option 1 : Lors de la création d'une action huissier**
```java
// Dans ActionHuissierServiceImpl.createAction()
// Après avoir créé l'action, marquer les documents précédents comme COMPLETED
List<DocumentHuissier> pendingDocuments = documentHuissierRepository
    .findByDossierIdAndStatus(dossierId, StatutDocumentHuissier.PENDING);
    
for (DocumentHuissier doc : pendingDocuments) {
    doc.setStatus(StatutDocumentHuissier.COMPLETED);
    documentHuissierRepository.save(doc);
}
```

#### **Option 2 : Lors de la création d'une audience**
```java
// Dans AudienceServiceImpl.createAudienceFromDTO()
// Après avoir créé l'audience, marquer les documents précédents comme COMPLETED
if (dossierId != null) {
    List<DocumentHuissier> pendingDocuments = documentHuissierRepository
        .findByDossierIdAndStatus(dossierId, StatutDocumentHuissier.PENDING);
        
    for (DocumentHuissier doc : pendingDocuments) {
        doc.setStatus(StatutDocumentHuissier.COMPLETED);
        documentHuissierRepository.save(doc);
    }
}
```

#### **Option 3 : Endpoint manuel**
Créer un endpoint pour marquer un document comme complété :
```java
@PutMapping("/document/{id}/complete")
public ResponseEntity<?> markDocumentAsCompleted(@PathVariable Long id) {
    DocumentHuissier document = documentHuissierRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Document non trouvé"));
    
    document.setStatus(StatutDocumentHuissier.COMPLETED);
    return ResponseEntity.ok(documentHuissierRepository.save(document));
}
```

---

## 📋 Résumé des Mécanismes

| Statut | Déclencheur | Fréquence | Automatique/Manuel |
|--------|-------------|-----------|-------------------|
| **PENDING** | Création du document | Immédiat | ✅ Automatique |
| **EXPIRED** | Délai légal dépassé | Toutes les 10 min | ✅ Automatique |
| **COMPLETED** | Action suivante effectuée | Sur événement | ⚠️ **À implémenter** |

---

## 🔍 Vérification dans le Frontend

Dans votre interface frontend, vous pouvez :

1. **Afficher le statut** avec des couleurs :
   - 🟢 **PENDING** : Vert (en attente)
   - 🟡 **EXPIRED** : Orange/Rouge (expiré, action requise)
   - 🔵 **COMPLETED** : Bleu (complété)

2. **Calculer la date d'expiration** :
   ```typescript
   getExpirationDate(document: DocumentHuissier): Date {
     const creation = new Date(document.dateCreation);
     const expiration = new Date(creation);
     expiration.setDate(expiration.getDate() + document.delaiLegalDays);
     return expiration;
   }
   
   isDocumentExpired(document: DocumentHuissier): boolean {
     return new Date() > this.getExpirationDate(document);
   }
   ```

3. **Afficher un avertissement** 2 jours avant l'expiration (comme le scheduler)

---

## ⚠️ Note Importante

Le statut `COMPLETED` est défini dans l'enum mais **n'est pas automatiquement géré** dans le code actuel. Vous devrez l'implémenter selon votre logique métier.

**Recommandation** : Implémenter la logique de passage à `COMPLETED` lors de la création d'une action huissier ou d'une audience pour le même dossier.

---

**Document créé pour clarifier le fonctionnement du système de statuts ! 📝**

