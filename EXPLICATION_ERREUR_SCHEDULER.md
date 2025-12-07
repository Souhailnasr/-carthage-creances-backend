# 🔧 Explication de l'Erreur : PasswordResetScheduler

## ❌ Erreur Rencontrée

```
org.springframework.dao.InvalidDataAccessApiUsageException: Executing an update/delete query
Caused by: jakarta.persistence.TransactionRequiredException: Executing an update/delete query
```

## 🔍 Cause du Problème

L'erreur se produit dans le `PasswordResetScheduler` lorsque les méthodes `expireTokens()` et `deleteOldTokens()` sont exécutées.

**Problème :**
- Les méthodes du repository `expireTokens()` et `deleteOldTokens()` sont annotées avec `@Modifying`
- Les méthodes `@Modifying` nécessitent une **transaction active**
- Les méthodes du scheduler n'avaient pas l'annotation `@Transactional`
- Résultat : Exception `TransactionRequiredException`

## ✅ Solution Appliquée

Ajout de l'annotation `@Transactional` sur les méthodes du scheduler :

```java
@Scheduled(fixedRate = 3600000)
@Transactional  // ✅ AJOUTÉ
public void expireTokens() {
    // ...
}

@Scheduled(cron = "0 0 2 * * ?")
@Transactional  // ✅ AJOUTÉ
public void deleteOldTokens() {
    // ...
}
```

## 📝 Explication Technique

### Pourquoi `@Modifying` nécessite une transaction ?

Les méthodes `@Modifying` dans Spring Data JPA exécutent des requêtes UPDATE/DELETE directement en SQL, sans passer par l'EntityManager. Ces opérations nécessitent une transaction active pour :

1. **Cohérence des données** : Garantir que les modifications sont atomiques
2. **Isolation** : Éviter les conflits de concurrence
3. **Rollback** : Permettre l'annulation en cas d'erreur

### Pourquoi le scheduler n'avait pas de transaction ?

Par défaut, les méthodes annotées avec `@Scheduled` ne créent pas automatiquement de transaction. Il faut explicitement ajouter `@Transactional` pour que Spring crée une transaction avant l'exécution de la méthode.

## 🔄 Autres Warnings (Non-Critiques)

### 1. Warning JTA Platform

```
HHH000489: No JTA platform available
```

**Explication :** Hibernate cherche une plateforme JTA (Java Transaction API) mais n'en trouve pas.  
**Impact :** Aucun, car vous utilisez des transactions Spring (pas JTA).  
**Action :** Aucune action nécessaire, c'est juste informatif.

### 2. Warning AuthenticationProvider

```
Global AuthenticationManager configured with an AuthenticationProvider bean. UserDetailsService beans will not be used...
```

**Explication :** Spring Security détecte que vous avez configuré un `AuthenticationProvider` manuellement dans `ApplicationConfig`.  
**Impact :** Aucun, votre configuration fonctionne correctement.  
**Action :** Aucune action nécessaire, c'est juste un avertissement informatif.

### 3. Warning Open-in-View

```
spring.jpa.open-in-view is enabled by default
```

**Explication :** Spring Boot active par défaut `open-in-view`, ce qui peut causer des problèmes de performance.  
**Impact :** Potentiel problème de performance si vous avez beaucoup de requêtes.  
**Action (Optionnel) :** Ajouter dans `application.properties` :
```properties
spring.jpa.open-in-view=false
```

## ✅ Correction Appliquée

Le fichier `PasswordResetScheduler.java` a été corrigé avec l'ajout de `@Transactional` sur les deux méthodes.

**Fichier modifié :**
- `src/main/java/projet/carthagecreance_backend/Config/PasswordResetScheduler.java`

**Changements :**
- Ajout de `import org.springframework.transaction.annotation.Transactional;`
- Ajout de `@Transactional` sur `expireTokens()`
- Ajout de `@Transactional` sur `deleteOldTokens()`

## 🧪 Test de la Correction

Après redémarrage de l'application, l'erreur ne devrait plus apparaître. Le scheduler devrait maintenant fonctionner correctement :

1. **Toutes les heures** : Les tokens expirés seront marqués comme EXPIRE
2. **Tous les jours à 2h** : Les tokens anciens (7+ jours) seront supprimés

## 📋 Vérification

Pour vérifier que la correction fonctionne :

1. Redémarrer l'application
2. Attendre quelques secondes (le scheduler s'exécute au démarrage)
3. Vérifier les logs : vous devriez voir "Tokens expirés marqués avec succès" sans erreur

---

**Date :** 2025-01-05  
**Status :** ✅ Erreur corrigée

