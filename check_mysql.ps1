# Script de vérification MySQL
# Exécutez ce script pour diagnostiquer les problèmes de connexion MySQL

Write-Host "🔍 Vérification de MySQL..." -ForegroundColor Cyan
Write-Host "=========================" -ForegroundColor Cyan

# 1. Vérifier le service
Write-Host "`n1. Vérification du service MySQL..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name MySQL* -ErrorAction SilentlyContinue
if ($mysqlService) {
    Write-Host "✅ Service MySQL trouvé: $($mysqlService.Name)" -ForegroundColor Green
    Write-Host "   Statut: $($mysqlService.Status)" -ForegroundColor $(if ($mysqlService.Status -eq 'Running') { 'Green' } else { 'Red' })
    if ($mysqlService.Status -ne 'Running') {
        Write-Host "   ⚠️  Le service n'est pas démarré. Tentative de démarrage..." -ForegroundColor Yellow
        try {
            Start-Service $mysqlService.Name
            Start-Sleep -Seconds 3
            $newStatus = (Get-Service $mysqlService.Name).Status
            Write-Host "   Statut après démarrage: $newStatus" -ForegroundColor $(if ($newStatus -eq 'Running') { 'Green' } else { 'Red' })
        } catch {
            Write-Host "   ❌ Impossible de démarrer le service: $_" -ForegroundColor Red
            Write-Host "   💡 Essayez de démarrer MySQL manuellement depuis les Services Windows" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "❌ Aucun service MySQL trouvé" -ForegroundColor Red
    Write-Host "   Vérifiez que MySQL est installé" -ForegroundColor Yellow
    Write-Host "   Services disponibles:" -ForegroundColor Cyan
    Get-Service | Where-Object { $_.Name -like "*sql*" } | Select-Object Name, Status | Format-Table
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
    Write-Host "   Vérifiez la configuration MySQL (my.ini)" -ForegroundColor Yellow
}

# 3. Tester la connexion MySQL
Write-Host "`n3. Test de connexion MySQL..." -ForegroundColor Yellow
$mysqlPath = Get-Command mysql -ErrorAction SilentlyContinue
if ($mysqlPath) {
    try {
        $result = mysql -u root -e "SELECT 1;" 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Connexion MySQL réussie" -ForegroundColor Green
        } else {
            Write-Host "❌ Échec de la connexion MySQL" -ForegroundColor Red
            Write-Host "   Sortie: $result" -ForegroundColor Gray
            Write-Host "   💡 Vérifiez votre mot de passe MySQL" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ Erreur lors du test de connexion: $_" -ForegroundColor Red
    }
} else {
    Write-Host "⚠️  MySQL n'est pas dans le PATH" -ForegroundColor Yellow
    Write-Host "   Le test de connexion sera ignoré" -ForegroundColor Gray
    Write-Host "   💡 Ajoutez MySQL au PATH ou utilisez le chemin complet" -ForegroundColor Yellow
}

# 4. Vérifier la base de données
Write-Host "`n4. Vérification de la base de données 'carthage_creances'..." -ForegroundColor Yellow
if ($mysqlPath) {
    try {
        $dbCheck = mysql -u root -e "SHOW DATABASES LIKE 'carthage_creances';" 2>&1
        if ($dbCheck -match "carthage_creances") {
            Write-Host "✅ La base de données 'carthage_creances' existe" -ForegroundColor Green
        } else {
            Write-Host "⚠️  La base de données 'carthage_creances' n'existe pas" -ForegroundColor Yellow
            Write-Host "   Elle sera créée automatiquement au démarrage de l'application" -ForegroundColor Cyan
            Write-Host "   (si createDatabaseIfNotExist=true est activé)" -ForegroundColor Gray
        }
    } catch {
        Write-Host "⚠️  Impossible de vérifier la base de données" -ForegroundColor Yellow
        Write-Host "   Erreur: $_" -ForegroundColor Gray
    }
} else {
    Write-Host "⚠️  Impossible de vérifier (MySQL non dans PATH)" -ForegroundColor Yellow
}

# 5. Vérifier la configuration application.properties
Write-Host "`n5. Vérification de la configuration application.properties..." -ForegroundColor Yellow
$appProps = "src\main\resources\application.properties"
if (Test-Path $appProps) {
    $content = Get-Content $appProps -Raw
    if ($content -match "connectTimeout") {
        Write-Host "✅ Les timeouts sont configurés" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Les timeouts ne sont pas configurés" -ForegroundColor Yellow
        Write-Host "   Ajoutez &connectTimeout=60000&socketTimeout=60000 à l'URL JDBC" -ForegroundColor Cyan
    }
    
    if ($content -match "spring.datasource.password=") {
        Write-Host "⚠️  Le mot de passe MySQL est vide" -ForegroundColor Yellow
        Write-Host "   Si MySQL a un mot de passe, ajoutez-le dans application.properties" -ForegroundColor Cyan
    }
} else {
    Write-Host "❌ Fichier application.properties non trouvé" -ForegroundColor Red
}

# Résumé
Write-Host "`n=========================" -ForegroundColor Cyan
Write-Host "📋 RÉSUMÉ" -ForegroundColor Cyan
Write-Host "=========================" -ForegroundColor Cyan

Write-Host "`n✅ Actions recommandées :" -ForegroundColor Green
Write-Host "1. Assurez-vous que MySQL est démarré" -ForegroundColor White
Write-Host "2. Vérifiez que le port 3306 est accessible" -ForegroundColor White
Write-Host "3. Vérifiez les identifiants dans application.properties" -ForegroundColor White
Write-Host "4. Ajoutez les timeouts à l'URL JDBC si nécessaire" -ForegroundColor White
Write-Host "5. Redémarrez l'application Spring Boot" -ForegroundColor White

Write-Host "`n📚 Consultez SOLUTION_ERREUR_CONNEXION_MYSQL.md pour plus de détails" -ForegroundColor Cyan

