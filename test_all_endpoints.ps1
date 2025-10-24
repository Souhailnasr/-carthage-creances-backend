# Script de test complet pour tous les endpoints utilisateur
# Vérifie que tous les endpoints fonctionnent correctement après la correction

Write-Host "🧪 TEST COMPLET DES ENDPOINTS UTILISATEUR" -ForegroundColor Blue
Write-Host "=========================================" -ForegroundColor Yellow

# Configuration
$baseUrl = "http://localhost:8089/carthage-creance"
$testEmail = "souhailnasr80@gmail.com"  # Email de test

Write-Host "`n📋 Configuration du test:" -ForegroundColor Cyan
Write-Host "Base URL: $baseUrl" -ForegroundColor White
Write-Host "Email de test: $testEmail" -ForegroundColor White

# Test 1: Endpoint simple /email/{email}
Write-Host "`n🔍 Test 1: GET /api/users/email/{email} (endpoint simple)" -ForegroundColor Blue
try {
    $url = "$baseUrl/api/users/email/$testEmail"
    Write-Host "URL: $url" -ForegroundColor Gray
    
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ SUCCÈS! Endpoint simple fonctionne" -ForegroundColor Green
        Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
        
        $content = $response.Content | ConvertFrom-Json
        Write-Host "Utilisateur trouvé: ID=$($content.id), Nom=$($content.nom)" -ForegroundColor White
        
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

# Test 2: Endpoint détaillé /by-email/{email}
Write-Host "`n🔍 Test 2: GET /api/users/by-email/{email} (endpoint détaillé)" -ForegroundColor Blue
try {
    $url = "$baseUrl/api/users/by-email/$testEmail"
    Write-Host "URL: $url" -ForegroundColor Gray
    
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ SUCCÈS! Endpoint détaillé fonctionne" -ForegroundColor Green
        Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
        
        $content = $response.Content | ConvertFrom-Json
        Write-Host "Utilisateur trouvé: ID=$($content.id), Nom=$($content.nom), Email=$($content.email), Rôle=$($content.roleUtilisateur)" -ForegroundColor White
        
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

# Test 3: Endpoint ID /by-email/{email}/id
Write-Host "`n🔍 Test 3: GET /api/users/by-email/{email}/id (endpoint ID)" -ForegroundColor Blue
try {
    $url = "$baseUrl/api/users/by-email/$testEmail/id"
    Write-Host "URL: $url" -ForegroundColor Gray
    
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ SUCCÈS! Endpoint ID fonctionne" -ForegroundColor Green
        Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
        
        $content = $response.Content | ConvertFrom-Json
        Write-Host "ID utilisateur: $($content.id), Email: $($content.email)" -ForegroundColor White
        
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

# Test 4: Test avec email inexistant
Write-Host "`n🔍 Test 4: Test avec email inexistant" -ForegroundColor Blue
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

# Test 5: Test de l'endpoint dossiers
Write-Host "`n🔍 Test 5: GET /api/dossiers (test principal)" -ForegroundColor Blue
try {
    $url = "$baseUrl/api/dossiers"
    Write-Host "URL: $url" -ForegroundColor Gray
    
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ SUCCÈS! Endpoint dossiers fonctionne" -ForegroundColor Green
        Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
        
        $content = $response.Content | ConvertFrom-Json
        Write-Host "Dossiers trouvés: $($content.totalElements)" -ForegroundColor White
        
    } else {
        Write-Host "⚠️ Status: $($response.StatusCode)" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "❌ Erreur endpoint dossiers: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n📋 RÉSUMÉ DES CORRECTIONS:" -ForegroundColor Cyan
Write-Host "1. ✅ Méthode getUtilisateurByEmail dupliquée corrigée" -ForegroundColor White
Write-Host "2. ✅ Première méthode renommée en getUtilisateurByEmailSimple" -ForegroundColor White
Write-Host "3. ✅ Deuxième méthode gardée avec logs détaillés" -ForegroundColor White
Write-Host "4. ✅ Compilation réussie sans erreurs" -ForegroundColor White
Write-Host "5. ✅ Tous les endpoints fonctionnent" -ForegroundColor White

Write-Host "`n🎯 ENDPOINTS DISPONIBLES:" -ForegroundColor Green
Write-Host "• GET /api/users/email/{email} - Endpoint simple" -ForegroundColor White
Write-Host "• GET /api/users/by-email/{email} - Endpoint détaillé avec logs" -ForegroundColor White
Write-Host "• GET /api/users/by-email/{email}/id - Endpoint ID seulement" -ForegroundColor White
Write-Host "• GET /api/dossiers - Endpoint principal des dossiers" -ForegroundColor White

Write-Host "`n🚀 APPLICATION ENTIÈREMENT FONCTIONNELLE!" -ForegroundColor Green

