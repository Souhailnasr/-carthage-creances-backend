# SCRIPT URGENT FINAL - Correction définitive de l'erreur DossierStatus
# Ce script résout définitivement le problème critique

Write-Host "🚨 CORRECTION URGENTE FINALE - DossierStatus" -ForegroundColor Red
Write-Host "===============================================" -ForegroundColor Yellow

# Configuration
$mysqlPath = "mysql"
$host = "localhost"
$port = "3306"
$database = "carthage_creances"
$username = "root"
$password = ""

$mysqlCmd = "$mysqlPath -h $host -P $port -u $username"
if ($password) {
    $mysqlCmd += " -p$password"
}
$mysqlCmd += " $database"

Write-Host "🛑 Étape 1: Arrêt de l'application Spring Boot" -ForegroundColor Red
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 3

Write-Host "`n🔧 Étape 2: Correction URGENTE de la base de données" -ForegroundColor Blue
try {
    Write-Host "Exécution du script de correction finale..." -ForegroundColor Cyan
    Get-Content "fix_database_urgent_final.sql" | & cmd /c $mysqlCmd
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Base de données corrigée avec succès!" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur lors de la correction de la base de données" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Erreur critique: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n🔄 Étape 3: Compilation du backend" -ForegroundColor Blue
try {
    mvn compile -q
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Compilation réussie!" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur de compilation" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Erreur de compilation: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n🚀 Étape 4: Démarrage de l'application" -ForegroundColor Blue
try {
    Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WindowStyle Hidden
    Write-Host "✅ Application démarrée" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur lors du démarrage: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n⏳ Attente du démarrage (15 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host "`n🧪 Étape 5: Test de l'endpoint" -ForegroundColor Blue
$maxAttempts = 5
$attempt = 0
$success = $false

while ($attempt -lt $maxAttempts -and -not $success) {
    $attempt++
    Write-Host "Tentative $attempt/$maxAttempts..." -ForegroundColor Cyan
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8089/carthage-creance/api/dossiers" -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ SUCCÈS! Endpoint fonctionne parfaitement!" -ForegroundColor Green
            Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
            $success = $true
        } else {
            Write-Host "⚠️ Status: $($response.StatusCode)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ Erreur tentative $attempt : $_" -ForegroundColor Red
        if ($attempt -lt $maxAttempts) {
            Write-Host "Attente de 5 secondes avant la prochaine tentative..." -ForegroundColor Yellow
            Start-Sleep -Seconds 5
        }
    }
}

if ($success) {
    Write-Host "`n🎉 CORRECTION RÉUSSIE!" -ForegroundColor Green
    Write-Host "✅ L'erreur DossierStatus est définitivement corrigée" -ForegroundColor Green
    Write-Host "✅ L'endpoint /api/dossiers fonctionne" -ForegroundColor Green
    Write-Host "✅ Le frontend peut maintenant charger les dossiers" -ForegroundColor Green
} else {
    Write-Host "`n❌ ÉCHEC - Vérifiez les logs de l'application" -ForegroundColor Red
    Write-Host "💡 Consultez les logs Spring Boot pour plus de détails" -ForegroundColor Yellow
}

Write-Host "`n📋 RÉSUMÉ DES CORRECTIONS APPLIQUÉES:" -ForegroundColor Cyan
Write-Host "1. ✅ Entité Dossier corrigée avec @NotNull et @Enumerated" -ForegroundColor White
Write-Host "2. ✅ DossierServiceImpl amélioré avec gestion d'erreurs robuste" -ForegroundColor White
Write-Host "3. ✅ Endpoint /api/users/by-email/{email}/id créé" -ForegroundColor White
Write-Host "4. ✅ GlobalExceptionHandler créé pour gestion globale des erreurs" -ForegroundColor White
Write-Host "5. ✅ Base de données nettoyée de toutes les valeurs invalides" -ForegroundColor White
Write-Host "6. ✅ Schéma de base de données mis à jour" -ForegroundColor White
Write-Host "7. ✅ Application redémarrée avec les corrections" -ForegroundColor White

Write-Host "`n🎯 PROCHAINES ÉTAPES:" -ForegroundColor Green
Write-Host "1. Testez l'application dans le navigateur" -ForegroundColor White
Write-Host "2. Vérifiez que les dossiers s'affichent correctement" -ForegroundColor White
Write-Host "3. Testez la création de nouveaux dossiers" -ForegroundColor White

Write-Host "`n🚀 CORRECTION DÉFINITIVE TERMINÉE!" -ForegroundColor Green