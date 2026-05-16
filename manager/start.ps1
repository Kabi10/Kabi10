Write-Host "Starting Dev Manager on http://localhost:3737" -ForegroundColor Cyan
Start-Process "http://localhost:3737"
python "$PSScriptRoot\server.py"
