# UI Dump Analysis Script for Agrimarket OTP Testing
# This script analyzes UI dump XML files to extract element information and coordinates

param(
    [string]$UIDumpFile = "ui_current.xml",
    [switch]$ShowAllElements,
    [switch]$FindInputFields,
    [switch]$FindButtons,
    [string]$SearchText = ""
)

function Parse-UIElement {
    param([System.Xml.XmlElement]$Element, [int]$Depth = 0)
    
    $indent = "  " * $Depth
    $bounds = $Element.bounds
    $text = $Element.text
    $contentDesc = $Element.'content-desc'
    $resourceId = $Element.'resource-id'
    $className = $Element.class
    $clickable = $Element.clickable
    $enabled = $Element.enabled
    
    # Extract coordinates from bounds
    $coordinates = ""
    if ($bounds -match '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') {
        $x1, $y1, $x2, $y2 = $matches[1..4]
        $centerX = [int](($x1 + $x2) / 2)
        $centerY = [int](($y1 + $y2) / 2)
        $coordinates = "Center: ($centerX, $centerY)"
    }
    
    $elementInfo = @{
        Text = $text
        ContentDesc = $contentDesc
        ResourceId = $resourceId
        ClassName = $className
        Clickable = $clickable
        Enabled = $enabled
        Bounds = $bounds
        Coordinates = $coordinates
        Depth = $Depth
    }
    
    return $elementInfo
}

function Find-ElementsByType {
    param([xml]$UIXml, [string]$ElementType)
    
    $elements = @()
    
    switch ($ElementType) {
        "input" {
            $inputElements = $UIXml.SelectNodes("//node[@class='android.widget.EditText' or @class='androidx.compose.ui.platform.AndroidComposeView' or contains(@content-desc, 'input') or contains(@hint, 'Enter')]")
        }
        "button" {
            $inputElements = $UIXml.SelectNodes("//node[@class='android.widget.Button' or @clickable='true' and (contains(@text, 'Send') or contains(@text, 'Verify') or contains(@text, 'அனுப்பு') or contains(@text, 'சரிபார்க்கவும்'))]")
        }
        "text" {
            $inputElements = $UIXml.SelectNodes("//node[@text!='' or @content-desc!='']")
        }
        default {
            $inputElements = $UIXml.SelectNodes("//node")
        }
    }
    
    foreach ($element in $inputElements) {
        $elements += Parse-UIElement $element
    }
    
    return $elements
}

function Search-Elements {
    param([xml]$UIXml, [string]$SearchTerm)
    
    $searchElements = $UIXml.SelectNodes("//node[contains(@text, '$SearchTerm') or contains(@content-desc, '$SearchTerm') or contains(@resource-id, '$SearchTerm')]")
    $results = @()
    
    foreach ($element in $searchElements) {
        $results += Parse-UIElement $element
    }
    
    return $results
}

function Generate-TapCommand {
    param([object]$Element)
    
    if ($Element.Coordinates -match 'Center: \((\d+), (\d+)\)') {
        $x = $matches[1]
        $y = $matches[2]
        return "adb shell input tap $x $y"
    }
    return "# Could not extract coordinates"
}

function Display-ElementInfo {
    param([object]$Element, [int]$Index)
    
    Write-Host "`n--- Element $Index ---" -ForegroundColor Cyan
    Write-Host "Text: '$($Element.Text)'" -ForegroundColor White
    Write-Host "Content-Desc: '$($Element.ContentDesc)'" -ForegroundColor White
    Write-Host "Resource-ID: '$($Element.ResourceId)'" -ForegroundColor White
    Write-Host "Class: '$($Element.ClassName)'" -ForegroundColor Gray
    Write-Host "Clickable: $($Element.Clickable)" -ForegroundColor $(if ($Element.Clickable -eq 'true') { 'Green' } else { 'Red' })
    Write-Host "Enabled: $($Element.Enabled)" -ForegroundColor $(if ($Element.Enabled -eq 'true') { 'Green' } else { 'Red' })
    Write-Host "Bounds: $($Element.Bounds)" -ForegroundColor Gray
    Write-Host "Coordinates: $($Element.Coordinates)" -ForegroundColor Yellow
    Write-Host "Tap Command: $(Generate-TapCommand $Element)" -ForegroundColor Green
}

# Main analysis
Write-Host "=== UI Dump Analysis for Agrimarket ===" -ForegroundColor Cyan
Write-Host "Analyzing file: $UIDumpFile" -ForegroundColor Yellow

if (-not (Test-Path $UIDumpFile)) {
    Write-Host "Error: UI dump file '$UIDumpFile' not found!" -ForegroundColor Red
    Write-Host "Available UI dump files:" -ForegroundColor Yellow
    Get-ChildItem -Name "ui_*.xml" | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
    exit 1
}

[xml]$uiXml = Get-Content $UIDumpFile

if ($SearchText) {
    Write-Host "`n=== Searching for: '$SearchText' ===" -ForegroundColor Cyan
    $searchResults = Search-Elements $uiXml $SearchText
    
    if ($searchResults.Count -eq 0) {
        Write-Host "No elements found matching '$SearchText'" -ForegroundColor Red
    } else {
        Write-Host "Found $($searchResults.Count) matching elements:" -ForegroundColor Green
        for ($i = 0; $i -lt $searchResults.Count; $i++) {
            Display-ElementInfo $searchResults[$i] ($i + 1)
        }
    }
}

if ($FindInputFields) {
    Write-Host "`n=== Input Fields ===" -ForegroundColor Cyan
    $inputFields = Find-ElementsByType $uiXml "input"
    
    if ($inputFields.Count -eq 0) {
        Write-Host "No input fields found" -ForegroundColor Red
    } else {
        Write-Host "Found $($inputFields.Count) input fields:" -ForegroundColor Green
        for ($i = 0; $i -lt $inputFields.Count; $i++) {
            Display-ElementInfo $inputFields[$i] ($i + 1)
        }
    }
}

if ($FindButtons) {
    Write-Host "`n=== Buttons ===" -ForegroundColor Cyan
    $buttons = Find-ElementsByType $uiXml "button"
    
    if ($buttons.Count -eq 0) {
        Write-Host "No buttons found" -ForegroundColor Red
    } else {
        Write-Host "Found $($buttons.Count) buttons:" -ForegroundColor Green
        for ($i = 0; $i -lt $buttons.Count; $i++) {
            Display-ElementInfo $buttons[$i] ($i + 1)
        }
    }
}

if ($ShowAllElements) {
    Write-Host "`n=== All Elements ===" -ForegroundColor Cyan
    $allElements = Find-ElementsByType $uiXml "all"
    
    Write-Host "Found $($allElements.Count) total elements:" -ForegroundColor Green
    for ($i = 0; $i -lt $allElements.Count; $i++) {
        Display-ElementInfo $allElements[$i] ($i + 1)
        if ($i -gt 0 -and ($i + 1) % 10 -eq 0) {
            Write-Host "`nPress any key to continue or Ctrl+C to stop..." -ForegroundColor Yellow
            $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        }
    }
}

# Generate quick reference commands
Write-Host "`n=== Quick Reference Commands ===" -ForegroundColor Cyan

# Look for common OTP-related elements
$otpElements = Search-Elements $uiXml "otp"
$phoneElements = Search-Elements $uiXml "phone"
$verifyElements = Search-Elements $uiXml "verify"
$sendElements = Search-Elements $uiXml "send"

if ($otpElements.Count -gt 0) {
    Write-Host "`nOTP-related elements:" -ForegroundColor Green
    foreach ($element in $otpElements) {
        if ($element.Coordinates) {
            Write-Host "  $(Generate-TapCommand $element)  # $($element.Text) $($element.ContentDesc)" -ForegroundColor White
        }
    }
}

if ($phoneElements.Count -gt 0) {
    Write-Host "`nPhone-related elements:" -ForegroundColor Green
    foreach ($element in $phoneElements) {
        if ($element.Coordinates) {
            Write-Host "  $(Generate-TapCommand $element)  # $($element.Text) $($element.ContentDesc)" -ForegroundColor White
        }
    }
}

if ($verifyElements.Count -gt 0) {
    Write-Host "`nVerify-related elements:" -ForegroundColor Green
    foreach ($element in $verifyElements) {
        if ($element.Coordinates) {
            Write-Host "  $(Generate-TapCommand $element)  # $($element.Text) $($element.ContentDesc)" -ForegroundColor White
        }
    }
}

if ($sendElements.Count -gt 0) {
    Write-Host "`nSend-related elements:" -ForegroundColor Green
    foreach ($element in $sendElements) {
        if ($element.Coordinates) {
            Write-Host "  $(Generate-TapCommand $element)  # $($element.Text) $($element.ContentDesc)" -ForegroundColor White
        }
    }
}

Write-Host "`n=== Analysis Complete ===" -ForegroundColor Cyan
Write-Host "Use the generated tap commands in your test scripts" -ForegroundColor Green
