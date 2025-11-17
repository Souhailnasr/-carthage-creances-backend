# Test de l'endpoint dossiers
$uri = "http://localhost:8089/carthage-creance/api/dossiers"
$headers = @{
    "Authorization" = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzb3VoYWlsbmFzcjgwQGdtYWlsLmNvbSIsImlhdCI6MTczMjM2NzU0MCwiZXhwIjoxNzMyNDUzOTQwfQ.8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q"
    "Content-Type" = "application/json"
}

try {
    Write-Host "Test de l'endpoint /api/dossiers..."
    $response = Invoke-WebRequest -Uri $uri -Method GET -Headers $headers
    Write-Host "Status Code: $($response.StatusCode)"
    Write-Host "Response: $($response.Content)"
} catch {
    Write-Host "Erreur: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody"
    }
}


















