# Script PowerShell pour corriger la base de données
# Exécuter ce script pour aligner la base de données avec les enums Java

Write-Host "🔧 Correction de la base de données..." -ForegroundColor Yellow

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

Write-Host "📝 Exécution du script SQL..." -ForegroundColor Blue

# Exécuter le script SQL
try {
    Get-Content "fix_database_enums.sql" | & cmd /c $mysqlCmd
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Base de données corrigée avec succès !" -ForegroundColor Green
        Write-Host "🔄 Redémarrez votre application Spring Boot" -ForegroundColor Cyan
    } else {
        Write-Host "❌ Erreur lors de l'exécution du script SQL" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
    Write-Host "💡 Assurez-vous que MySQL est installé et accessible" -ForegroundColor Yellow
}

Write-Host "`n📋 Prochaines étapes :" -ForegroundColor Cyan
Write-Host "1. Vérifiez que la base de données est corrigée" -ForegroundColor White
Write-Host "2. Redémarrez l'application Spring Boot" -ForegroundColor White
Write-Host "3. Testez les endpoints avec le frontend Angular" -ForegroundColor White
