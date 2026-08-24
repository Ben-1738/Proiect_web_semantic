Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   Pornire Proiect Web - MCP Cars          " -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# --- Pasul 1: Build ---
Write-Host "[1/5] Building project..." -ForegroundColor Yellow
Push-Location $projectDir
mvn clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "BUILD FAILED! Oprire." -ForegroundColor Red
    Pop-Location
    exit 1
}
Write-Host "  Build OK!" -ForegroundColor Green
Pop-Location

# --- Pasul 2: JSON Server (port 4000) ---
Write-Host "[2/5] Pornire json-server pe portul 4000..." -ForegroundColor Yellow
$jsonServer = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectDir'; npx json-server --watch db.json --port 4000 --host 0.0.0.0" -PassThru
Write-Host "  json-server PID: $($jsonServer.Id)" -ForegroundColor Green

Start-Sleep -Seconds 3

# --- Pasul 3: GraphQL Server (port 3000) ---
Write-Host "[3/5] Pornire json-graphql-server pe portul 3000..." -ForegroundColor Yellow
$graphqlServer = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectDir'; npx json-graphql-server db-graphql.json --port 3000 --host 0.0.0.0" -PassThru
Write-Host "  GraphQL Server PID: $($graphqlServer.Id)" -ForegroundColor Green

Start-Sleep -Seconds 3

# --- Pasul 4: MCP Server (port 8082) ---
Write-Host "[4/5] Pornire MCP Server pe portul 8082..." -ForegroundColor Yellow
$mcpServer = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectDir'; java '-Djava.net.useSystemProxies=false' -jar mcp-server/target/mcp-server-0.0.1-SNAPSHOT.jar" -PassThru
Write-Host "  MCP Server PID: $($mcpServer.Id)" -ForegroundColor Green

Start-Sleep -Seconds 5

# --- Pasul 5: MCP Client (port 8081) ---
Write-Host "[5/5] Pornire MCP Client pe portul 8081..." -ForegroundColor Yellow
$mcpClient = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectDir'; java -jar mcp-client/target/mcp-client-0.0.1-SNAPSHOT.jar" -PassThru
Write-Host "  MCP Client PID: $($mcpClient.Id)" -ForegroundColor Green

# --- Done ---
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   Toate serviciile sunt pornite!           " -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  json-server:      http://localhost:4000" -ForegroundColor White
Write-Host "  GraphQL Server:   http://localhost:3000/graphql" -ForegroundColor White
Write-Host "  MCP Server:       http://localhost:8082" -ForegroundColor White
Write-Host "  MCP Client:       http://localhost:8081" -ForegroundColor White
Write-Host "  Frontend:         deschide frontend/index.html" -ForegroundColor White
Write-Host ""
Write-Host "Pentru oprire: inchide ferestrele PowerShell sau ruleaza:" -ForegroundColor Gray
Write-Host "  .\stop-all.ps1" -ForegroundColor Gray
