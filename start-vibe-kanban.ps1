# Start vibe-kanban server on port 54590
# Required for Claude Code and Gemini CLI MCP integration

$port = 54590

# Check if already running
$existing = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "vibe-kanban already running on port $port" -ForegroundColor Green
    Start-Process "http://localhost:$port"
    exit 0
}

Write-Host "Starting vibe-kanban on port $port..." -ForegroundColor Cyan

$env:PORT = $port
Start-Process -FilePath "vibe-kanban" -NoNewWindow

# Wait and open browser
Start-Sleep -Seconds 3
Start-Process "http://localhost:$port"

Write-Host "vibe-kanban started at http://localhost:$port" -ForegroundColor Green
Write-Host "MCP tools in Claude Code and Gemini CLI are now active." -ForegroundColor Green
