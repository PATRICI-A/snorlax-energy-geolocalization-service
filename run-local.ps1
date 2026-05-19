# Carga las variables del .env y levanta el servicio
Get-Content .env | Where-Object { $_ -match '^\s*[^#]' -and $_ -match '=' } | ForEach-Object {
    $parts = $_ -split '=', 2
    $key   = $parts[0].Trim()
    $value = $parts[1].Trim().Trim('"')
    [System.Environment]::SetEnvironmentVariable($key, $value, 'Process')
}

Write-Host "Puerto: $env:SERVER_PORT"
Write-Host "MongoDB URI: $($env:MONGODB_URI.Substring(0,40))..."
Write-Host "Perfil activo: $env:SPRING_PROFILES_ACTIVE"
Write-Host ""
Write-Host "Iniciando geolocation-service..."

mvn spring-boot:run "-Dspring-boot.run.profiles=$env:SPRING_PROFILES_ACTIVE"
