# 🔧 Solution : Erreur de Connexion MySQL

## 🚨 Problème Identifié

L'erreur `Communications link failure` indique que l'application Spring Boot ne peut pas se connecter à la base de données MySQL.

**Erreur principale** :
```
Communications link failure
The last packet sent successfully to the server was 0 milliseconds ago.
java.net.SocketTimeoutException: Read timed out
```

---

## ✅ SOLUTIONS PAR ORDRE DE PRIORITÉ

### Solution 1 : Vérifier que MySQL est Démarré

#### Sur Windows :

1. **Vérifier le service MySQL** :
   ```powershell
   # Ouvrir PowerShell en tant qu'administrateur
   Get-Service -Name MySQL*
   ```

2. **Démarrer MySQL si arrêté** :
   ```powershell
   Start-Service MySQL80
   # ou
   Start-Service MySQL
   ```

3. **Vérifier le statut** :
   ```powershell
   Get-Service -Name MySQL* | Select-Object Name, Status
   ```

#### Alternative : Démarrer MySQL manuellement

1. Ouvrir **Services** (Win + R → `services.msc`)
2. Chercher **MySQL** ou **MySQL80**
3. Clic droit → **Démarrer**

#### Vérifier avec MySQL Workbench ou ligne de commande

```bash
# Tester la connexion
mysql -u root -p -h localhost -P 3306
```

Si cela fonctionne, MySQL est démarré. Si non, voir Solution 2.

---

### Solution 2 : Vérifier les Paramètres de Connexion

Votre configuration actuelle dans `application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/carthage_creances?useUnicode=true&createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
```

#### Modifications à Apporter :

1. **Ajouter un timeout de connexion plus long** :
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/carthage_creances?useUnicode=true&createDatabaseIfNotExist=true&serverTimezone=UTC&connectTimeout=60000&socketTimeout=60000
   ```

2. **Si vous avez un mot de passe MySQL** :
   ```properties
   spring.datasource.password=votre_mot_de_passe
   ```

3. **Si MySQL est sur un autre port** (par défaut 3306) :
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3307/carthage_creances?...
   ```

4. **Configuration complète recommandée** :

```properties
# Database Configuration (MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/carthage_creances?useUnicode=true&createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=60000&socketTimeout=60000
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Pool de connexions HikariCP
spring.datasource.hikari.connection-timeout=60000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=600000
```

---

### Solution 3 : Vérifier le Firewall

Le firewall Windows peut bloquer la connexion MySQL.

#### Désactiver temporairement le firewall pour tester :

1. Ouvrir **Pare-feu Windows Defender**
2. Désactiver temporairement
3. Relancer l'application
4. Si ça fonctionne, réactiver le firewall et ajouter une exception pour MySQL

#### Ajouter une exception pour MySQL :

```powershell
# PowerShell en tant qu'administrateur
New-NetFirewallRule -DisplayName "MySQL Server" -Direction Inbound -LocalPort 3306 -Protocol TCP -Action Allow
```

---

### Solution 4 : Vérifier que le Port 3306 est Libre

```powershell
# Vérifier si le port 3306 est utilisé
netstat -ano | findstr :3306
```

Si rien n'apparaît, MySQL n'écoute pas sur ce port.

Si un autre processus utilise le port :
```powershell
# Trouver le processus
netstat -ano | findstr :3306
# Tuer le processus (remplacer PID par le numéro trouvé)
taskkill /PID <PID> /F
```

---

### Solution 5 : Vérifier la Configuration MySQL

#### Vérifier que MySQL écoute sur localhost :

1. Ouvrir le fichier `my.ini` ou `my.cnf` (généralement dans `C:\ProgramData\MySQL\MySQL Server 8.0\`)
2. Vérifier la section `[mysqld]` :
   ```ini
   [mysqld]
   bind-address=127.0.0.1
   port=3306
   ```

3. Si `bind-address` est `0.0.0.0`, MySQL écoute sur toutes les interfaces (OK)
4. Si `bind-address` est `127.0.0.1`, MySQL écoute uniquement sur localhost (OK pour votre cas)

---

### Solution 6 : Créer la Base de Données Manuellement

Si `createDatabaseIfNotExist=true` ne fonctionne pas, créez la base manuellement :

```sql
-- Se connecter à MySQL
mysql -u root -p

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS carthage_creances CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Vérifier
SHOW DATABASES;
```

---

### Solution 7 : Configuration Alternative avec HikariCP

Ajoutez ces propriétés pour améliorer la gestion des connexions :

```properties
# HikariCP Configuration
spring.datasource.hikari.connection-timeout=60000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=600000
spring.datasource.hikari.leak-detection-threshold=60000
```

---

## 🔍 DIAGNOSTIC ÉTAPE PAR ÉTAPE

### Étape 1 : Tester la Connexion MySQL

```powershell
# Test de connexion basique
mysql -u root -h localhost -P 3306
```

**Si ça fonctionne** → MySQL est OK, problème dans l'application
**Si ça ne fonctionne pas** → MySQL n'est pas démarré ou mal configuré

### Étape 2 : Vérifier les Logs MySQL

Cherchez les logs MySQL (généralement dans `C:\ProgramData\MySQL\MySQL Server 8.0\Data\`) pour voir s'il y a des erreurs.

### Étape 3 : Tester avec un Client MySQL

Utilisez **MySQL Workbench** ou **HeidiSQL** pour tester la connexion :
- Host: `localhost`
- Port: `3306`
- Username: `root`
- Password: (vide ou votre mot de passe)

### Étape 4 : Vérifier la Version du Driver MySQL

Dans `pom.xml`, vérifiez la version du driver MySQL :

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version> <!-- ou version plus récente -->
</dependency>
```

---

## 📝 FICHIER application.properties CORRIGÉ

Voici le fichier `application.properties` complet avec toutes les corrections :

```properties
spring.application.name=Carthage-Creance_backend

# Server Configuration
server.port=8089
server.servlet.context-path=/carthage-creance

# Database Configuration (MySQL) - CORRIGÉ
spring.datasource.url=jdbc:mysql://localhost:3306/carthage_creances?useUnicode=true&createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=60000&socketTimeout=60000
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# HikariCP Pool Configuration
spring.datasource.hikari.connection-timeout=60000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=600000

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.com.zaxxer.hikari=DEBUG
logging.level.org.springframework=WARN
logging.level.root=INFO
logging.level.projet.carthagecreance_backend=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %-5level - %logger{60} - %msg%n

# App feature flags
app.validation.enabled=false

# JWT Configuration
application.security.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
application.security.jwt.expiration=86400000
jdj.secure.token.validity=3600

# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.file-size-threshold=2MB

# File Storage Configuration
file.upload-dir=./uploads
file.base-url=http://localhost:8089/carthage-creance/api/files

# Jackson Configuration for Hibernate
spring.jackson.serialization.fail-on-empty-beans=false
spring.jackson.serialization.write-dates-as-timestamps=false
```

---

## 🚀 SCRIPT DE VÉRIFICATION RAPIDE

Créez un fichier `check_mysql.ps1` :

```powershell
# Script de vérification MySQL
Write-Host "🔍 Vérification de MySQL..." -ForegroundColor Cyan

# 1. Vérifier le service
Write-Host "`n1. Vérification du service MySQL..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name MySQL* -ErrorAction SilentlyContinue
if ($mysqlService) {
    Write-Host "✅ Service MySQL trouvé: $($mysqlService.Name)" -ForegroundColor Green
    Write-Host "   Statut: $($mysqlService.Status)" -ForegroundColor $(if ($mysqlService.Status -eq 'Running') { 'Green' } else { 'Red' })
    if ($mysqlService.Status -ne 'Running') {
        Write-Host "   ⚠️  Le service n'est pas démarré. Démarrage..." -ForegroundColor Yellow
        Start-Service $mysqlService.Name
        Start-Sleep -Seconds 3
        Write-Host "   Statut après démarrage: $((Get-Service $mysqlService.Name).Status)" -ForegroundColor Green
    }
} else {
    Write-Host "❌ Aucun service MySQL trouvé" -ForegroundColor Red
    Write-Host "   Vérifiez que MySQL est installé" -ForegroundColor Yellow
}

# 2. Vérifier le port
Write-Host "`n2. Vérification du port 3306..." -ForegroundColor Yellow
$port = netstat -ano | findstr :3306
if ($port) {
    Write-Host "✅ Le port 3306 est utilisé" -ForegroundColor Green
    Write-Host "   $port" -ForegroundColor Gray
} else {
    Write-Host "❌ Le port 3306 n'est pas utilisé" -ForegroundColor Red
    Write-Host "   MySQL n'écoute probablement pas sur ce port" -ForegroundColor Yellow
}

# 3. Tester la connexion
Write-Host "`n3. Test de connexion MySQL..." -ForegroundColor Yellow
try {
    $result = mysql -u root -e "SELECT 1;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Connexion MySQL réussie" -ForegroundColor Green
    } else {
        Write-Host "❌ Échec de la connexion MySQL" -ForegroundColor Red
        Write-Host "   $result" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ MySQL n'est pas dans le PATH ou n'est pas accessible" -ForegroundColor Red
    Write-Host "   Erreur: $_" -ForegroundColor Gray
}

# 4. Vérifier la base de données
Write-Host "`n4. Vérification de la base de données..." -ForegroundColor Yellow
try {
    $dbCheck = mysql -u root -e "SHOW DATABASES LIKE 'carthage_creances';" 2>&1
    if ($dbCheck -match "carthage_creances") {
        Write-Host "✅ La base de données 'carthage_creances' existe" -ForegroundColor Green
    } else {
        Write-Host "⚠️  La base de données 'carthage_creances' n'existe pas" -ForegroundColor Yellow
        Write-Host "   Elle sera créée automatiquement au démarrage de l'application" -ForegroundColor Cyan
    }
} catch {
    Write-Host "⚠️  Impossible de vérifier la base de données" -ForegroundColor Yellow
}

Write-Host "`n✅ Vérification terminée" -ForegroundColor Green
```

Exécutez-le avec :
```powershell
.\check_mysql.ps1
```

---

## 🎯 SOLUTION RAPIDE (À ESSAYER EN PREMIER)

1. **Démarrer MySQL** :
   ```powershell
   Start-Service MySQL80
   ```

2. **Modifier `application.properties`** pour ajouter les timeouts :
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/carthage_creances?useUnicode=true&createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=60000&socketTimeout=60000
   ```

3. **Redémarrer l'application Spring Boot**

---

## 📞 SI RIEN NE FONCTIONNE

1. **Réinstaller MySQL** (dernier recours)
2. **Utiliser une base de données en mémoire H2** pour tester (temporairement)
3. **Vérifier les logs MySQL** pour des erreurs spécifiques
4. **Contacter le support MySQL** avec les logs complets

---

## ✅ CHECKLIST DE VÉRIFICATION

- [ ] MySQL est démarré (service Running)
- [ ] Le port 3306 est accessible
- [ ] Les identifiants sont corrects (root / mot de passe)
- [ ] Le firewall n'bloque pas MySQL
- [ ] Les timeouts sont configurés dans application.properties
- [ ] La base de données existe ou peut être créée
- [ ] Le driver MySQL est à jour dans pom.xml

---

**Après avoir appliqué ces solutions, redémarrez l'application Spring Boot.**

