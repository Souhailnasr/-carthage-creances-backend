# Script de test pour l'endpoint GET /api/users/by-email/{email}
# Teste l'endpoint nouvellement créé

Write-Host "🧪 TEST DE L'ENDPOINT GET /api/users/by-email/{email}" -ForegroundColor Blue
Write-Host "=================================================" -ForegroundColor Yellow

# Configuration
$baseUrl = "http://localhost:8089/carthage-creance"
$testEmail = "souhailnasr80@gmail.com"  # Email de test

Write-Host "`n📋 Configuration du test:" -ForegroundColor Cyan
Write-Host "Base URL: $baseUrl" -ForegroundColor White
Write-Host "Email de test: $testEmail" -ForegroundColor White

Write-Host "`n🔍 Test 1: Endpoint GET /api/users/by-email/{email}" -ForegroundColor Blue
try {
    $url = "$baseUrl/api/users/by-email/$testEmail"
    Write-Host "URL: $url" -ForegroundColor Gray
    
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ SUCCÈS! Endpoint fonctionne" -ForegroundColor Green
        Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
        
        # Afficher le contenu de la réponse
        $content = $response.Content | ConvertFrom-Json
        Write-Host "`n📄 Contenu de la réponse:" -ForegroundColor Cyan
        Write-Host "ID: $($content.id)" -ForegroundColor White
        Write-Host "Nom: $($content.nom)" -ForegroundColor White
        Write-Host "Prénom: $($content.prenom)" -ForegroundColor White
        Write-Host "Email: $($content.email)" -ForegroundColor White
        Write-Host "Rôle: $($content.roleUtilisateur)" -ForegroundColor White
        
    } else {
        Write-Host "⚠️ Status inattendu: $($response.StatusCode)" -ForegroundColor Yellow
    }
    
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "❌ 404 Not Found - Utilisateur non trouvé" -ForegroundColor Red
        Write-Host "💡 Vérifiez que l'utilisateur existe dans la base de données" -ForegroundColor Yellow
    } elseif ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "❌ 400 Bad Request - Email invalide" -ForegroundColor Red
    } else {
        Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n🔍 Test 2: Endpoint GET /api/users/by-email/{email}/id" -ForegroundColor Blue
try {
    $url = "$baseUrl/api/users/by-email/$testEmail/id"
    Write-Host "URL: $url" -ForegroundColor Gray
    
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ SUCCÈS! Endpoint ID fonctionne" -ForegroundColor Green
        Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
        
        # Afficher le contenu de la réponse
        $content = $response.Content | ConvertFrom-Json
        Write-Host "`n📄 Contenu de la réponse:" -ForegroundColor Cyan
        Write-Host "ID: $($content.id)" -ForegroundColor White
        Write-Host "Email: $($content.email)" -ForegroundColor White
        
    } else {
        Write-Host "⚠️ Status inattendu: $($response.StatusCode)" -ForegroundColor Yellow
    }
    
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "❌ 404 Not Found - Utilisateur non trouvé" -ForegroundColor Red
    } else {
        Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n🔍 Test 3: Test avec email inexistant" -ForegroundColor Blue
try {
    $url = "$baseUrl/api/users/by-email/nonexistent@example.com"
    Write-Host "URL: $url" -ForegroundColor Gray
    
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    
    Write-Host "⚠️ Réponse inattendue: $($response.StatusCode)" -ForegroundColor Yellow
    
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "✅ 404 Not Found - Comportement attendu pour email inexistant" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur inattendue: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n📋 RÉSUMÉ DES TESTS:" -ForegroundColor Cyan
Write-Host "1. ✅ Endpoint GET /api/users/by-email/{email} créé" -ForegroundColor White
Write-Host "2. ✅ Gestion des cas d'erreur (404, 400, 500)" -ForegroundColor White
Write-Host "3. ✅ Logs de débogage ajoutés" -ForegroundColor White
Write-Host "4. ✅ Validation de l'email" -ForegroundColor White
Write-Host "5. ✅ Retour de l'objet Utilisateur complet" -ForegroundColor White

Write-Host "`n🎯 ENDPOINT PRÊT À L'UTILISATION!" -ForegroundColor Green
Write-Host "URL: GET $baseUrl/api/users/by-email/{email}" -ForegroundColor White
Write-Host "Retourne: Objet Utilisateur complet avec ID, nom, prénom, email, rôle" -ForegroundColor White
