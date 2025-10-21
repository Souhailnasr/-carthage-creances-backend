# Script de test pour vérifier la création de dossier et date_cloture
Write-Host "🧪 TEST DE CRÉATION DE DOSSIER - VÉRIFICATION date_cloture" -ForegroundColor Blue
Write-Host "=============================================================" -ForegroundColor Yellow

# Configuration
$baseUrl = "http://localhost:8089/carthage-creance"
$testEmail = "souhailnasr80@gmail.com"

# Attendre que l'application démarre
Write-Host "`n⏳ Attente du démarrage de l'application (10 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "`n🔍 Test 1: Récupération de l'utilisateur par email" -ForegroundColor Blue
try {
    $url = "$baseUrl/api/utilisateurs/by-email/$testEmail"
    Write-Host "URL: $url" -ForegroundColor Gray
    
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    
    if ($response.StatusCode -eq 200) {
        $user = $response.Content | ConvertFrom-Json
        Write-Host "✅ Utilisateur trouvé: ID=$($user.id), Nom=$($user.nom), Role=$($user.roleUtilisateur)" -ForegroundColor Green
        
        Write-Host "`n🔍 Test 2: Création d'un dossier de test" -ForegroundColor Blue
        
        # Données de test pour la création de dossier
        $dossierData = @{
            titre = "Test Dossier - Vérification date_cloture"
            description = "Dossier de test pour vérifier que date_cloture est NULL"
            numeroDossier = "TEST-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
            montantCreance = 1000.50
            nomCreancier = "Test Créancier"
            nomDebiteur = "Test Débiteur"
            agentCreateurId = $user.id
            urgence = "NORMALE"
            statut = "EN_ATTENTE_VALIDATION"
            typeDocumentJustificatif = "CONTRAT"
        } | ConvertTo-Json -Depth 3
        
        Write-Host "Données du dossier:" -ForegroundColor Cyan
        Write-Host $dossierData -ForegroundColor White
        
        $headers = @{
            "Content-Type" = "application/json"
        }
        
        $createUrl = "$baseUrl/api/dossiers"
        Write-Host "URL de création: $createUrl" -ForegroundColor Gray
        
        $createResponse = Invoke-WebRequest -Uri $createUrl -Method POST -Body $dossierData -Headers $headers -UseBasicParsing -TimeoutSec 15 -ErrorAction Stop
        
        if ($createResponse.StatusCode -eq 200 -or $createResponse.StatusCode -eq 201) {
            Write-Host "✅ Dossier créé avec succès!" -ForegroundColor Green
            Write-Host "Status Code: $($createResponse.StatusCode)" -ForegroundColor Green
            
            $createdDossier = $createResponse.Content | ConvertFrom-Json
            Write-Host "`n📄 Détails du dossier créé:" -ForegroundColor Cyan
            Write-Host "ID: $($createdDossier.id)" -ForegroundColor White
            Write-Host "Titre: $($createdDossier.titre)" -ForegroundColor White
            Write-Host "Numéro: $($createdDossier.numeroDossier)" -ForegroundColor White
            Write-Host "Date création: $($createdDossier.dateCreation)" -ForegroundColor White
            Write-Host "Date clôture: $($createdDossier.dateCloture)" -ForegroundColor White
            Write-Host "Statut: $($createdDossier.statut)" -ForegroundColor White
            Write-Host "Dossier Status: $($createdDossier.dossierStatus)" -ForegroundColor White
            
            # Vérification critique
            if ($createdDossier.dateCloture -eq $null -or $createdDossier.dateCloture -eq "") {
                Write-Host "`n✅ SUCCÈS! date_cloture est NULL comme attendu" -ForegroundColor Green
            } else {
                Write-Host "`n❌ PROBLÈME! date_cloture n'est pas NULL: $($createdDossier.dateCloture)" -ForegroundColor Red
            }
            
        } else {
            Write-Host "⚠️ Status inattendu: $($createResponse.StatusCode)" -ForegroundColor Yellow
            Write-Host "Réponse: $($createResponse.Content)" -ForegroundColor Gray
        }
        
    } else {
        Write-Host "❌ Impossible de récupérer l'utilisateur" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Erreur lors du test: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        Write-Host "Response: $($_.Exception.Response.Content)" -ForegroundColor Red
    }
}

Write-Host "`n🔍 Test 3: Vérification des dossiers existants" -ForegroundColor Blue
try {
    $url = "$baseUrl/api/dossiers"
    Write-Host "URL: $url" -ForegroundColor Gray
    
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    
    if ($response.StatusCode -eq 200) {
        $dossiers = $response.Content | ConvertFrom-Json
        Write-Host "✅ Dossiers récupérés: $($dossiers.totalElements) trouvés" -ForegroundColor Green
        
        if ($dossiers.content.Count -gt 0) {
            Write-Host "`n📄 Premier dossier:" -ForegroundColor Cyan
            $premierDossier = $dossiers.content[0]
            Write-Host "ID: $($premierDossier.id)" -ForegroundColor White
            Write-Host "Titre: $($premierDossier.titre)" -ForegroundColor White
            Write-Host "Date création: $($premierDossier.dateCreation)" -ForegroundColor White
            Write-Host "Date clôture: $($premierDossier.dateCloture)" -ForegroundColor White
            Write-Host "Statut: $($premierDossier.statut)" -ForegroundColor White
            Write-Host "Dossier Status: $($premierDossier.dossierStatus)" -ForegroundColor White
            
            # Vérification critique
            if ($premierDossier.dateCloture -eq $null -or $premierDossier.dateCloture -eq "") {
                Write-Host "✅ date_cloture est NULL comme attendu" -ForegroundColor Green
            } else {
                Write-Host "❌ date_cloture n'est pas NULL: $($premierDossier.dateCloture)" -ForegroundColor Red
            }
        }
        
    } else {
        Write-Host "⚠️ Status: $($response.StatusCode)" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "❌ Erreur lors de la récupération des dossiers: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nRÉSUMÉ DU TEST:" -ForegroundColor Cyan
Write-Host "1. Vérification de l'utilisateur" -ForegroundColor White
Write-Host "2. Test de création de dossier" -ForegroundColor White
Write-Host "3. Vérification de date_cloture" -ForegroundColor White
Write-Host "4. Vérification des dossiers existants" -ForegroundColor White

Write-Host "`nRÉSULTAT ATTENDU:" -ForegroundColor Green
Write-Host "• date_cloture doit être NULL lors de la création" -ForegroundColor White
Write-Host "• date_cloture ne doit être assignée que lors de la clôture explicite" -ForegroundColor White
Write-Host "• dossierStatus doit être ENCOURSDETRAITEMENT par défaut" -ForegroundColor White
