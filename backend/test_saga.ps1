$ErrorActionPreference = "Stop"
$baseUrl = "http://127.0.0.1:8081"

Write-Host "`n=== STONK SAGA E2E TEST ===" -ForegroundColor Cyan

# 1. Register a new user
$rand = Get-Random
Write-Host "`n[1/8] Registering user saga_test_$rand..." -ForegroundColor Yellow
$authResp = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -ContentType "application/json" `
    -Body "{`"username`":`"saga_test_$rand`",`"password`":`"password`",`"email`":`"saga_$rand@test.com`"}"
$token = $authResp.token
$userId = $authResp.userId
$headers = @{ Authorization = "Bearer $token" }
Write-Host "  OK: userId=$userId" -ForegroundColor Green

# 2. Create wallet
Write-Host "`n[2/8] Creating wallet..." -ForegroundColor Yellow
$wallet = Invoke-RestMethod -Uri "$baseUrl/wallet/$userId/create" -Method Post -Headers $headers
Write-Host "  OK: walletId=$($wallet.id), balance=$($wallet.balance)" -ForegroundColor Green

# 3. Deposit funds
Write-Host "`n[3/8] Depositing `$50,000..." -ForegroundColor Yellow
$wallet = Invoke-RestMethod -Uri "$baseUrl/wallet/$userId/deposit" -Method Post -ContentType "application/json" `
    -Headers $headers -Body '{"amount":50000}'
Write-Host "  OK: balance=$($wallet.balance)" -ForegroundColor Green

# 4. Execute BUY trade
Write-Host "`n[4/8] Executing BUY trade (5 shares of AAPL)..." -ForegroundColor Yellow
$buyResp = Invoke-RestMethod -Uri "$baseUrl/trades/buy" -Method Post -ContentType "application/json" `
    -Headers $headers -Body "{`"userId`":$userId,`"symbol`":`"AAPL`",`"quantity`":5}"
Write-Host "  Trade created: id=$($buyResp.id), status=$($buyResp.status), price=$($buyResp.price), total=$($buyResp.totalAmount)" -ForegroundColor Green

# 5. Wait for Saga to complete
Write-Host "`n[5/8] Waiting 8 seconds for Saga chain to complete..." -ForegroundColor Yellow
Start-Sleep -Seconds 8

# 6. Check trade status
Write-Host "`n[6/8] Checking trade status..." -ForegroundColor Yellow
$trades = Invoke-RestMethod -Uri "$baseUrl/trades/$userId" -Method Get -Headers $headers
foreach ($t in $trades) {
    Write-Host "  Trade #$($t.id): status=$($t.status), type=$($t.type), symbol=$($t.symbol), qty=$($t.quantity), total=$($t.totalAmount)" -ForegroundColor $(if ($t.status -eq 'COMPLETED') { 'Green' } else { 'Red' })
}

# 7. Check portfolio
Write-Host "`n[7/8] Checking portfolio..." -ForegroundColor Yellow
$portfolio = Invoke-RestMethod -Uri "$baseUrl/portfolio/$userId" -Method Get -Headers $headers
if ($portfolio.Count -gt 0) {
    foreach ($h in $portfolio) {
        Write-Host "  Holding: symbol=$($h.symbol), qty=$($h.quantity), avgPrice=$($h.averageCost)" -ForegroundColor Green
    }
} else {
    Write-Host "  EMPTY - Saga may not have completed!" -ForegroundColor Red
}

# 8. Check orders
Write-Host "`n[8/8] Checking orders..." -ForegroundColor Yellow
$orders = Invoke-RestMethod -Uri "$baseUrl/orders/$userId" -Method Get -Headers $headers
if ($orders.Count -gt 0) {
    foreach ($o in $orders) {
        Write-Host "  Order #$($o.id): type=$($o.type), symbol=$($o.symbol), qty=$($o.quantity), status=$($o.status)" -ForegroundColor Green
    }
} else {
    Write-Host "  EMPTY - Order saga listener may not have completed!" -ForegroundColor Red
}

# 9. Check wallet balance after trade
Write-Host "`n[BONUS] Checking wallet balance after BUY..." -ForegroundColor Yellow
$walletAfter = Invoke-RestMethod -Uri "$baseUrl/wallet/$userId" -Method Get -Headers $headers
Write-Host "  Wallet balance: $($walletAfter.balance)" -ForegroundColor Green

Write-Host "`n=== TEST COMPLETE ===" -ForegroundColor Cyan
