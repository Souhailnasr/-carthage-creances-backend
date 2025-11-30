# 🔧 Correction de l'Erreur "Transaction silently rolled back" lors de la Création d'Audience

## 🎯 Problème Identifié

L'erreur `Transaction silently rolled back because it has been marked as rollback-only` se produit lors de la création d'une audience, même si le service fonctionnait correctement avant l'ajout de la logique huissier.

## 🔍 Causes Possibles

1. **Exception silencieuse dans la sauvegarde JPA**
2. **Contrainte de base de données non respectée**
3. **Problème avec la colonne `huissier_id` (corrigée de `hussier_id`)**
4. **Exception dans les notifications (même si isolées)**

## ✅ Solutions Appliquées

### 1. Correction du Nom de Colonne
- ✅ `hussier_id` → `huissier_id` dans l'entité `Audience`

### 2. Isolation des Notifications
- ✅ Notifications déplacées APRÈS le commit de la transaction principale
- ✅ `@Transactional(propagation = Propagation.REQUIRES_NEW)` sur les méthodes de notification
- ✅ Try-catch pour isoler les erreurs de notification

### 3. Amélioration du Logging
- ✅ Logging SQL activé pour voir les requêtes
- ✅ Logging détaillé à chaque étape
- ✅ Capture des exceptions avec stack trace complète

## 🔧 Vérifications à Effectuer

### 1. Vérifier la Structure de la Table en Base de Données

Exécutez cette requête SQL pour vérifier la structure :

```sql
DESCRIBE audiences;
-- ou
SHOW CREATE TABLE audiences;
```

**Vérifiez que** :
- La colonne s'appelle bien `huissier_id` (et non `hussier_id`)
- La colonne est `nullable = true`
- Il n'y a pas de contraintes étrangères manquantes

### 2. Vérifier les Logs du Serveur

Avec le logging activé, vous devriez voir dans les logs :

```
=== DÉBUT Création d'une audience depuis DTO ===
DTO reçu: ...
Audience builder créé
Chargement du dossier avec ID: 38
Dossier 38 assigné à l'audience
Aucun avocat fourni (optionnel)
Aucun huissier fourni (optionnel)
Tentative de sauvegarde de l'audience...
AVANT save() - Audience: dossier=38, avocat=3, huissier=NULL
APRÈS save() - Audience ID: X
✅ Audience sauvegardée avec succès
```

**Si vous ne voyez pas ces logs**, cela signifie que :
- Le serveur n'a pas été redémarré
- L'exception se produit avant d'atteindre notre code
- Le niveau de logging n'est pas correct

### 3. Vérifier les Requêtes SQL

Avec `spring.jpa.show-sql=true`, vous devriez voir la requête INSERT :

```sql
INSERT INTO audiences (date_audience, date_prochaine, tribunal_type, lieu_tribunal, 
                       commentaire_decision, resultat, dossier_id, avocat_id, huissier_id) 
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
```

**Vérifiez que** :
- La colonne `huissier_id` est bien dans la requête
- La valeur est `NULL` (et non une erreur)

## 🛠️ Solution de Dernier Recours

Si le problème persiste, essayez cette approche :

### Option 1 : Désactiver Temporairement les Notifications

Dans `AudienceController.createAudience()`, commentez temporairement les notifications :

```java
// Envoyer les notifications APRÈS le commit de la transaction principale
// TEMPORAIREMENT DÉSACTIVÉ POUR TEST
/*
try {
    if (createdAudience.getDossier() != null) {
        automaticNotificationService.notifierCreationAudience(createdAudience, createdAudience.getDossier());
        if (createdAudience.getDateProchaine() != null) {
            automaticNotificationService.notifierAudienceProchaine(createdAudience, createdAudience.getDossier());
        }
    }
} catch (Exception e) {
    logger.error("Erreur lors de l'envoi des notifications (non bloquante): {}", e.getMessage(), e);
}
*/
```

Si cela fonctionne, le problème vient des notifications.

### Option 2 : Vérifier les Contraintes de Base de Données

Exécutez cette requête pour vérifier les contraintes :

```sql
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME = 'audiences'
AND TABLE_SCHEMA = 'carthage_creances';
```

### Option 3 : Vérifier les Données du Dossier

Vérifiez que le dossier existe et est valide :

```sql
SELECT * FROM dossiers WHERE id = 38;
```

## 📋 Checklist de Diagnostic

- [ ] Le serveur a été redémarré après les modifications
- [ ] Les logs détaillés apparaissent dans la console
- [ ] La colonne `huissier_id` existe dans la table `audiences`
- [ ] La colonne `huissier_id` est `nullable = true`
- [ ] Le dossier avec l'ID fourni existe dans la base de données
- [ ] L'avocat avec l'ID fourni existe (si fourni)
- [ ] L'huissier avec l'ID fourni existe (si fourni)
- [ ] Les requêtes SQL s'affichent dans les logs
- [ ] Aucune exception n'est visible dans les logs avant "Transaction silently rolled back"

## 🎯 Prochaines Étapes

1. **Redémarrer le serveur** avec les modifications
2. **Tester la création d'audience** avec les logs activés
3. **Partager les logs complets** du serveur (pas seulement l'erreur finale)
4. **Vérifier la structure de la table** en base de données
5. **Tester sans notifications** si le problème persiste

---

**Note** : Le problème est probablement lié à une exception qui se produit lors de la sauvegarde mais qui n'est pas correctement propagée. Les logs SQL et le logging détaillé devraient permettre d'identifier la cause exacte.

