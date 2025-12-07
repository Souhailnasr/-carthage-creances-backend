# ✅ Correction : Suppression de Dossier avec Tarifs

## 🎯 Problème Identifié

Lors de la suppression d'un dossier, une erreur `SQLIntegrityConstraintViolationException` se produisait :

```
Cannot delete or update a parent row: a foreign key constraint fails 
(`carthage_creances`.`tarif_dossier`, CONSTRAINT `FKt846w4bhcdwkjbjx9kr5fpmb6` 
FOREIGN KEY (`dossier_id`) REFERENCES `dossier` (`id`))
```

**Cause :** La table `tarif_dossier` contient des enregistrements qui référencent le dossier à supprimer. La contrainte de clé étrangère empêche la suppression du dossier tant que ces tarifs existent.

---

## ✅ Solution Appliquée

Modification de la méthode `deleteDossier()` dans `DossierServiceImpl` pour supprimer **d'abord** tous les `TarifDossier` associés au dossier **avant** de supprimer le dossier lui-même.

---

## 📋 Modifications Backend

### Fichier : `src/main/java/projet/carthagecreance_backend/Service/Impl/DossierServiceImpl.java`

**Méthode modifiée :** `deleteDossier(Long id)` (lignes 402-447)

#### Code ajouté :

```java
// Supprimer tous les tarifs associés au dossier avant de supprimer le dossier
List<TarifDossier> tarifs = tarifDossierRepository.findByDossierId(id);
if (!tarifs.isEmpty()) {
    logger.info("deleteDossier: suppression de {} tarif(s) associé(s) au dossier {}", tarifs.size(), id);
    tarifDossierRepository.deleteAll(tarifs);
}

// Supprimer le dossier
dossierRepository.deleteById(id);
```

#### Ordre de suppression :

1. ✅ Vérification des validations EN_ATTENTE (bloque si présentes)
2. ✅ **Suppression de tous les `TarifDossier` associés** (NOUVEAU)
3. ✅ Suppression du dossier
4. ✅ Envoi de notification

---

## 🔍 Détails Techniques

### Entité concernée : `TarifDossier`

- **Relation :** `@ManyToOne` vers `Dossier`
- **Contrainte :** `FOREIGN KEY (dossier_id) REFERENCES dossier(id)`
- **Repository :** `TarifDossierRepository.findByDossierId(Long dossierId)`

### Méthode utilisée :

- `tarifDossierRepository.findByDossierId(id)` : Récupère tous les tarifs du dossier
- `tarifDossierRepository.deleteAll(tarifs)` : Supprime tous les tarifs en une seule opération

---

## ✅ Résultat

Maintenant, lors de la suppression d'un dossier :

1. ✅ Les tarifs associés sont supprimés automatiquement
2. ✅ Le dossier peut être supprimé sans erreur de contrainte
3. ✅ Aucune modification frontend nécessaire

---

## 🚀 Test

Pour tester la correction :

1. **Redémarrer le backend**
2. **Ouvrir l'application frontend**
3. **Aller sur "Gestion des Dossiers"**
4. **Essayer de supprimer un dossier** qui a des tarifs associés
5. **Vérifier** que la suppression fonctionne sans erreur

---

## 📝 Notes

- Cette correction suit le même pattern que les corrections précédentes pour la suppression d'utilisateurs (suppression des `PerformanceAgent`, `Token`, `PasswordResetToken` avant la suppression de l'utilisateur).
- La suppression est **cascade** : tous les tarifs associés sont supprimés en même temps que le dossier.
- Les logs indiquent le nombre de tarifs supprimés pour faciliter le débogage.

---

**Date :** 2025-01-05  
**Status :** ✅ Correction appliquée - Prêt pour test

