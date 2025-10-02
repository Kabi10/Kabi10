# Extract UI Elements from XML dump for Agrimarket OTP Testing
param(
    [string]$XmlFile = "ui_initial_launch.xml"
)

function Extract-Coordinates {
    param([string]$BoundsString)
    
    if ($BoundsString -match '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') {
        $x1 = [int]$matches[1]
        $y1 = [int]$matches[2]
        $x2 = [int]$matches[3]
        $y2 = [int]$matches[4]
        $centerX = [int](($x1 + $x2) / 2)
        $centerY = [int](($y1 + $y2) / 2)
        return @{
            X = $centerX
            Y = $centerY
            Width = $x2 - $x1
            Height = $y2 - $y1
            Valid = $true
        }
    }
    return @{ Valid = $false }
}

Write-Host "=== Extracting UI Elements from $XmlFile ===" -ForegroundColor Cyan

if (-not (Test-Path $XmlFile)) {
    Write-Host "File not found: $XmlFile" -ForegroundColor Red
    exit 1
}

$xmlContent = Get-Content $XmlFile -Raw
[xml]$xmlDoc = $xmlContent

Write-Host "`n=== Phone Input Screen Analysis ===" -ForegroundColor Green

# Find phone input field
$phoneInput = $xmlDoc.SelectSingleNode("//node[@class='android.widget.EditText']")
if ($phoneInput) {
    $coords = Extract-Coordinates $phoneInput.bounds
    if ($coords.Valid) {
        Write-Host "Phone Input Field Found:" -ForegroundColor Yellow
        Write-Host "  Bounds: $($phoneInput.bounds)" -ForegroundColor White
        Write-Host "  Center: ($($coords.X), $($coords.Y))" -ForegroundColor White
        Write-Host "  Tap Command: adb shell input tap $($coords.X) $($coords.Y)" -ForegroundColor Green
    }
}

# Find Send OTP button
$sendButton = $xmlDoc.SelectSingleNode("//node[contains(@text, 'Send OTP') or contains(@text, 'OTP அனுப்பவும்')]")
if ($sendButton) {
    $coords = Extract-Coordinates $sendButton.bounds
    if ($coords.Valid) {
        Write-Host "`nSend OTP Button Found:" -ForegroundColor Yellow
        Write-Host "  Text: $($sendButton.text)" -ForegroundColor White
        Write-Host "  Bounds: $($sendButton.bounds)" -ForegroundColor White
        Write-Host "  Center: ($($coords.X), $($coords.Y))" -ForegroundColor White
        Write-Host "  Enabled: $($sendButton.enabled)" -ForegroundColor White
        Write-Host "  Clickable: $($sendButton.clickable)" -ForegroundColor White
        Write-Host "  Tap Command: adb shell input tap $($coords.X) $($coords.Y)" -ForegroundColor Green
    }
}

# Find the clickable button container
$buttonContainer = $xmlDoc.SelectSingleNode("//node[@class='android.view.View' and @clickable='true']")
if ($buttonContainer) {
    $coords = Extract-Coordinates $buttonContainer.bounds
    if ($coords.Valid) {
        Write-Host "`nButton Container Found:" -ForegroundColor Yellow
        Write-Host "  Bounds: $($buttonContainer.bounds)" -ForegroundColor White
        Write-Host "  Center: ($($coords.X), $($coords.Y))" -ForegroundColor White
        Write-Host "  Enabled: $($buttonContainer.enabled)" -ForegroundColor White
        Write-Host "  Clickable: $($buttonContainer.clickable)" -ForegroundColor White
        Write-Host "  Tap Command: adb shell input tap $($coords.X) $($coords.Y)" -ForegroundColor Green
    }
}

# List all clickable elements
Write-Host "`n=== All Clickable Elements ===" -ForegroundColor Green
$clickableElements = $xmlDoc.SelectNodes("//node[@clickable='true']")
$index = 1
foreach ($element in $clickableElements) {
    $coords = Extract-Coordinates $element.bounds
    if ($coords.Valid) {
        Write-Host "`nClickable Element ${index}:" -ForegroundColor Yellow
        Write-Host "  Class: $($element.class)" -ForegroundColor White
        Write-Host "  Text: '$($element.text)'" -ForegroundColor White
        Write-Host "  Enabled: $($element.enabled)" -ForegroundColor White
        Write-Host "  Bounds: $($element.bounds)" -ForegroundColor White
        Write-Host "  Center: ($($coords.X), $($coords.Y))" -ForegroundColor White
        Write-Host "  Tap Command: adb shell input tap $($coords.X) $($coords.Y)" -ForegroundColor Green
        $index++
    }
}

# List all text elements for reference
Write-Host "`n=== Text Elements for Reference ===" -ForegroundColor Green
$textElements = $xmlDoc.SelectNodes("//node[@text!='']")
foreach ($element in $textElements) {
    $coords = Extract-Coordinates $element.bounds
    if ($coords.Valid) {
        Write-Host "Text: '$($element.text)' at ($($coords.X), $($coords.Y))" -ForegroundColor White
    }
}

Write-Host "`n=== Analysis Complete ===" -ForegroundColor Cyan
