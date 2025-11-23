# 🧪 Guide de Test des APIs

## 📋 Tests avec Postman/curl

### 1. 🔐 Authentification

#### Connexion
```bash
curl -X POST http://localhost:8089/carthage-creance/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "password123"
  }'
```

#### Inscription
```bash
curl -X POST http://localhost:8089/carthage-creance/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "password123",
    "role": "AGENT_DOSSIER"
  }'
```

### 2. 👥 Utilisateurs

#### Récupérer utilisateur par email
```bash
curl -X GET http://localhost:8089/carthage-creance/api/users/by-email/admin@example.com
```

### 3. 📁 Dossiers

#### Lister tous les dossiers
```bash
curl -X GET "http://localhost:8089/carthage-creance/api/dossiers?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Lister dossiers avec filtres
```bash
curl -X GET "http://localhost:8089/carthage-creance/api/dossiers?role=AGENT&userId=1&search=test&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Créer un dossier
```bash
curl -X POST http://localhost:8089/carthage-creance/api/dossiers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "titre=Test Dossier" \
  -F "description=Description du dossier" \
  -F "montantCreance=1000" \
  -F "creancierId=1" \
  -F "debiteurId=1"
```

### 4. ✅ Validation

#### Lister les validations
```bash
curl -X GET http://localhost:8089/carthage-creance/api/validation/dossiers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Valider un dossier
```bash
curl -X POST http://localhost:8089/carthage-creance/api/validation/dossiers/1/valider \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"commentaire": "Dossier validé"}'
```

#### Rejeter un dossier
```bash
curl -X POST http://localhost:8089/carthage-creance/api/validation/dossiers/1/rejeter \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"commentaire": "Dossier rejeté"}'
```

#### Clôturer un dossier
```bash
curl -X POST http://localhost:8089/carthage-creance/api/validation/dossiers/1/cloturer \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"commentaire": "Dossier clôturé"}'
```

## 🔍 Tests Frontend Angular

### 1. Service d'authentification
```typescript
// Test de connexion
this.authService.login({
  email: 'admin@example.com',
  password: 'password123'
}).subscribe({
  next: (response) => console.log('Connexion réussie:', response),
  error: (error) => console.error('Erreur de connexion:', error)
});
```

### 2. Service des dossiers
```typescript
// Test de récupération des dossiers
this.dossierService.getAllDossiers({
  page: 0,
  size: 10,
  search: 'test'
}).subscribe({
  next: (response) => console.log('Dossiers:', response),
  error: (error) => console.error('Erreur:', error)
});
```

### 3. Service de validation
```typescript
// Test de validation
this.validationService.validerDossier(1, 'Commentaire de validation')
  .subscribe({
    next: (response) => console.log('Validation réussie:', response),
    error: (error) => console.error('Erreur de validation:', error)
  });
```

## 🐛 Dépannage

### Erreurs communes

#### 1. Erreur 401 - Non autorisé
```json
{
  "error": "JWT token manquant ou invalide"
}
```
**Solution** : Vérifier que le token JWT est correctement envoyé dans l'en-tête `Authorization: Bearer <token>`

#### 2. Erreur 500 - Erreur interne
```json
{
  "error": "No enum constant projet.carthagecreance_backend.Entity.DossierStatus.EN_COURS"
}
```
**Solution** : Exécuter le script `fix_database_enums.sql` pour corriger les enums dans la base de données

#### 3. Erreur CORS
```
Access to XMLHttpRequest at 'http://localhost:8089' from origin 'http://localhost:4200' has been blocked by CORS policy
```
**Solution** : Configurer CORS dans Spring Boot ou utiliser un proxy Angular

### Configuration CORS Spring Boot

Ajouter dans `SecurityConfiguration.java` :
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Configuration Proxy Angular

Créer `proxy.conf.json` :
```json
{
  "/api/*": {
    "target": "http://localhost:8089/carthage-creance",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "debug"
  }
}
```

Démarrer Angular avec le proxy :
```bash
ng serve --proxy-config proxy.conf.json
```

## 📊 Monitoring

### Logs Spring Boot
```bash
# Activer les logs détaillés
logging.level.projet.carthagecreance_backend=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Logs Angular
```typescript
// Dans le service
console.log('Requête envoyée:', request);
console.log('Réponse reçue:', response);
```

### Métriques de performance
- Temps de réponse des APIs
- Nombre de requêtes par seconde
- Utilisation mémoire
- Connexions base de données

## ✅ Checklist de test

- [ ] Authentification fonctionne
- [ ] JWT token est généré et valide
- [ ] Endpoints dossiers répondent
- [ ] Pagination fonctionne
- [ ] Filtres de recherche fonctionnent
- [ ] Validation des dossiers fonctionne
- [ ] Permissions utilisateur respectées
- [ ] Gestion d'erreurs appropriée
- [ ] Logs de débogage activés
- [ ] Performance acceptable

---

**🎯 Votre API est maintenant prête pour l'intégration frontend !**


























