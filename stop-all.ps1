Write-Host "Oprire toate serviciile..." -ForegroundColor Yellow

# Opreste json-server (port 4000)
$nodeProcesses = Get-NetTCPConnection -LocalPort 4000 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($pid in $nodeProcesses) {
    Write-Host "  Oprire proces pe port 4000 (PID: $pid)" -ForegroundColor Red
    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
}

# Opreste GraphQL Server (port 3000)
$graphqlProcesses = Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($pid in $graphqlProcesses) {
    Write-Host "  Oprire proces pe port 3000 (PID: $pid)" -ForegroundColor Red
    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
}

# Opreste MCP Server (port 8082)
$serverProcesses = Get-NetTCPConnection -LocalPort 8082 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($pid in $serverProcesses) {
    Write-Host "  Oprire proces pe port 8082 (PID: $pid)" -ForegroundColor Red
    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
}

# Opreste MCP Client (port 8081)
$clientProcesses = Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($pid in $clientProcesses) {
    Write-Host "  Oprire proces pe port 8081 (PID: $pid)" -ForegroundColor Red
    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
}

Write-Host "Toate serviciile au fost oprite!" -ForegroundColor Green
