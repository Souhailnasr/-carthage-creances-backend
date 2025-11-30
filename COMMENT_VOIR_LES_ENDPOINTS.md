# 📋 Comment Visualiser les Endpoints de l'Application

## 🎯 Méthode 1 : Logs de Démarrage Spring Boot (RECOMMANDÉ)

### **Où regarder :**

Quand vous démarrez le serveur Spring Boot, **cherchez dans la console** les lignes qui commencent par `Mapped`.

### **Exemple de ce que vous devriez voir :**

```
Mapped "{[/api/huissier/document],methods=[POST]}"
Mapped "{[/api/huissier/document/{id}],methods=[GET]}"
Mapped "{[/api/huissier/documents],methods=[GET]}"
Mapped "{[/api/huissier/document/{id}/complete],methods=[PUT]}"  ← Votre endpoint
Mapped "{[/api/huissier/document/{id}/expire],methods=[PUT]}"
```

### **Comment activer ces logs :**

Le logging est **déjà activé** dans `application.properties` :

```properties
logging.level.org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping=DEBUG
```

### **Où trouver les logs :**

1. **Console de votre IDE** (IntelliJ, Eclipse, VS Code)
2. **Terminal** où vous avez lancé `mvn spring-boot:run`
3. **Fichier de log** (si configuré)

---

## 🎯 Méthode 2 : Endpoint de Diagnostic (NOUVEAU)

J'ai créé **deux endpoints** pour lister tous les endpoints disponibles.

### **Endpoint 1 : Tous les endpoints de l'application**

**URL :**
```
GET http://localhost:8089/carthage-creance/api/test/endpoints
```

**Résultat attendu :**
```json
{
  "total": 50,
  "endpoints": [
    {
      "methods": ["POST"],
      "paths": ["/api/huissier/document"],
      "controller": "HuissierDocumentController",
      "method": "createDocument",
      "fullUrl": "http://localhost:8089/carthage-creance/api/huissier/document"
    },
    {
      "methods": ["PUT"],
      "paths": ["/api/huissier/document/{id}/complete"],
      "controller": "HuissierDocumentController",
      "method": "markDocumentAsCompleted",
      "fullUrl": "http://localhost:8089/carthage-creance/api/huissier/document/{id}/complete"
    }
  ],
  "timestamp": 1234567890
}
```

### **Endpoint 2 : Uniquement les endpoints HuissierDocumentController**

**URL :**
```
GET http://localhost:8089/carthage-creance/api/test/endpoints/huissier-document
```

**Résultat attendu :**
```json
{
  "controller": "HuissierDocumentController",
  "total": 5,
  "endpoints": [
    {
      "method": "POST",
      "path": "/api/huissier/document",
      "javaMethod": "createDocument",
      "fullUrl": "http://localhost:8089/carthage-creance/api/huissier/document"
    },
    {
      "method": "GET",
      "path": "/api/huissier/document/{id}",
      "javaMethod": "getDocumentById",
      "fullUrl": "http://localhost:8089/carthage-creance/api/huissier/document/{id}"
    },
    {
      "method": "GET",
      "path": "/api/huissier/documents",
      "javaMethod": "getDocumentsByDossier",
      "fullUrl": "http://localhost:8089/carthage-creance/api/huissier/documents"
    },
    {
      "method": "PUT",
      "path": "/api/huissier/document/{id}/complete",
      "javaMethod": "markDocumentAsCompleted",
      "fullUrl": "http://localhost:8089/carthage-creance/api/huissier/document/{id}/complete"
    },
    {
      "method": "PUT",
      "path": "/api/huissier/document/{id}/expire",
      "javaMethod": "markDocumentAsExpired",
      "fullUrl": "http://localhost:8089/carthage-creance/api/huissier/document/{id}/expire"
    }
  ],
  "timestamp": 1234567890
}
```

**✅ Utilisez cet endpoint pour vérifier rapidement si `/complete` est enregistré !**

---

## 🎯 Méthode 3 : Recherche dans les Logs

### **Dans la console, cherchez :**

1. **Tous les endpoints** : Cherchez `Mapped`
2. **Endpoints spécifiques** : Cherchez `HuissierDocumentController` ou `complete`

### **Exemple de recherche :**

Dans votre console, utilisez `Ctrl+F` et cherchez :
- `Mapped` → Voir tous les endpoints
- `complete` → Voir si votre endpoint est enregistré
- `HuissierDocumentController` → Voir tous les endpoints de ce contrôleur

---

## 🎯 Méthode 4 : Vérifier un Endpoint Spécifique

Pour vérifier si un endpoint spécifique est enregistré :

### **Cherchez dans les logs :**
```
Mapped "{[/api/huissier/document/{id}/complete],methods=[PUT]}"
```

**Si cette ligne apparaît** → L'endpoint est enregistré ✅
**Si cette ligne n'apparaît PAS** → L'endpoint n'est pas enregistré ❌

---

## 🔍 Diagnostic Rapide

### **Étape 1 : Redémarrer le serveur**

### **Étape 2 : Chercher dans les logs**

Dans la console, cherchez :
```
Mapped
```

### **Étape 3 : Filtrer pour votre contrôleur**

Cherchez :
```
HuissierDocumentController
```

ou

```
document/complete
```

---

## 📝 Exemple Complet de Logs

Voici à quoi ressemblent les logs de démarrage :

```
2025-11-30 07:15:00 - INFO  - o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/api/huissier/document],methods=[POST]}" onto public org.springframework.http.ResponseEntity<?> projet.carthagecreance_backend.Controller.HuissierDocumentController.createDocument(...)
2025-11-30 07:15:00 - INFO  - o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/api/huissier/document/{id}],methods=[GET]}" onto public org.springframework.http.ResponseEntity<?> projet.carthagecreance_backend.Controller.HuissierDocumentController.getDocumentById(...)
2025-11-30 07:15:00 - INFO  - o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/api/huissier/documents],methods=[GET]}" onto public org.springframework.http.ResponseEntity<?> projet.carthagecreance_backend.Controller.HuissierDocumentController.getDocumentsByDossier(...)
2025-11-30 07:15:00 - INFO  - o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/api/huissier/document/{id}/complete],methods=[PUT]}" onto public org.springframework.http.ResponseEntity<?> projet.carthagecreance_backend.Controller.HuissierDocumentController.markDocumentAsCompleted(...)
2025-11-30 07:15:00 - INFO  - o.s.w.s.m.m.a.RequestMappingHandlerMapping - Mapped "{[/api/huissier/document/{id}/expire],methods=[PUT]}" onto public org.springframework.http.ResponseEntity<?> projet.carthagecreance_backend.Controller.HuissierDocumentController.markDocumentAsExpired(...)
```

---

## ✅ Checklist

- [ ] Le serveur a été redémarré
- [ ] Les logs de démarrage sont visibles
- [ ] La recherche `Mapped` trouve des résultats
- [ ] L'endpoint `/complete` apparaît dans les logs
- [ ] Le contrôleur `HuissierDocumentController` est listé

---

**Les logs de démarrage sont la meilleure façon de vérifier que vos endpoints sont enregistrés ! 📊**

