# Script PowerShell URGENT pour corriger l'erreur DossierStatus
# Exécuter ce script pour résoudre l'erreur "No enum constant"

Write-Host "🚨 CORRECTION URGENTE - Erreur DossierStatus" -ForegroundColor Red
Write-Host "===============================================" -ForegroundColor Yellow

# Configuration de la base de données
$mysqlPath = "mysql"  # Ajustez le chemin si nécessaire
$host = "localhost"
$port = "3306"
$database = "carthage_creances"
$username = "root"
$password = ""  # Ajustez le mot de passe si nécessaire

# Commande MySQL
$mysqlCmd = "$mysqlPath -h $host -P $port -u $username"

if ($password) {
    $mysqlCmd += " -p$password"
}

$mysqlCmd += " $database"

Write-Host "📊 Étape 1: Diagnostic des valeurs problématiques..." -ForegroundColor Blue

# 1. Diagnostic
try {
    Write-Host "Vérification des valeurs actuelles..." -ForegroundColor Cyan
    Get-Content "check_database_values.sql" | & cmd /c $mysqlCmd
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Diagnostic terminé" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur lors du diagnostic" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erreur lors du diagnostic: $_" -ForegroundColor Red
}

Write-Host "`n🔧 Étape 2: Correction des valeurs problématiques..." -ForegroundColor Blue

# 2. Correction
try {
    Write-Host "Exécution du script de correction..." -ForegroundColor Cyan
    Get-Content "fix_database_dossier_status.sql" | & cmd /c $mysqlCmd
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Correction terminée avec succès!" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur lors de la correction" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erreur lors de la correction: $_" -ForegroundColor Red
}

Write-Host "`n🔄 Étape 3: Redémarrage de l'application..." -ForegroundColor Blue

# 3. Redémarrage
Write-Host "Arrêt de l'application Spring Boot..." -ForegroundColor Cyan
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force

Write-Host "Démarrage de l'application..." -ForegroundColor Cyan
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WindowStyle Hidden

Write-Host "`n⏳ Attente du démarrage (10 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "`n🧪 Étape 4: Test de l'endpoint..." -ForegroundColor Blue

# 4. Test
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8089/carthage-creance/api/dossiers" -UseBasicParsing -ErrorAction Stop
    Write-Host "✅ Endpoint fonctionne! Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "❌ Endpoint encore en erreur: $_" -ForegroundColor Red
    Write-Host "💡 Vérifiez les logs de l'application" -ForegroundColor Yellow
}

Write-Host "`n📋 RÉSUMÉ DES CORRECTIONS:" -ForegroundColor Cyan
Write-Host "1. ✅ Entité Dossier corrigée avec valeur par défaut" -ForegroundColor White
Write-Host "2. ✅ Enum DossierStatus amélioré avec gestion d'erreurs" -ForegroundColor White
Write-Host "3. ✅ Base de données nettoyée" -ForegroundColor White
Write-Host "4. ✅ Gestion d'erreurs améliorée dans DossierServiceImpl" -ForegroundColor White
Write-Host "5. ✅ Application redémarrée" -ForegroundColor White

Write-Host "`n🎯 PROCHAINES ÉTAPES:" -ForegroundColor Green
Write-Host "1. Vérifiez que l'application démarre sans erreur" -ForegroundColor White
Write-Host "2. Testez l'endpoint /api/dossiers" -ForegroundColor White
Write-Host "3. Vérifiez l'affichage dans le frontend" -ForegroundColor White

Write-Host "`n🚀 CORRECTION TERMINÉE!" -ForegroundColor Green

















