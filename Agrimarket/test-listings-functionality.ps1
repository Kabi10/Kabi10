# Test Listings Functionality for Agrimarket App
# This script tests the listings features according to TESTING_GUIDE.md

param(
    [string]$DeviceId = "57221FDCQ000D7",
    [int]$DelayBetweenSteps = 3
)

# Set up environment
$env:PATH += ";C:\Users\Tharma\AppData\Local\Android\Sdk\platform-tools"
$PackageName = "com.senthapps.slagrimarket"

# Helper functions
function Write-TestStep {
    param([string]$Message)
    Write-Host "`n=== $Message ===" -ForegroundColor Cyan
}

function Wait-ForSeconds {
    param([int]$Seconds)
    Write-Host "Waiting $Seconds seconds..." -ForegroundColor Yellow
    Start-Sleep -Seconds $Seconds
}

function Capture-UIState {
    param([string]$StepName)
    Write-Host "Capturing UI state for: $StepName" -ForegroundColor Green
    adb -s $DeviceId shell uiautomator dump /sdcard/ui_$StepName.xml
    adb -s $DeviceId pull /sdcard/ui_$StepName.xml ./ui_$StepName.xml
    adb -s $DeviceId shell screencap /sdcard/screenshot_$StepName.png
    adb -s $DeviceId pull /sdcard/screenshot_$StepName.png ./screenshot_$StepName.png
}

function Get-ElementCoordinates {
    param([string]$XmlFile, [string]$SearchText, [string]$SearchAttribute = "text")
    
    if (Test-Path $XmlFile) {
        [xml]$uiXml = Get-Content $XmlFile
        $element = $uiXml.SelectSingleNode("//node[@$SearchAttribute='$SearchText']")
        if ($element) {
            $bounds = $element.bounds
            if ($bounds -match '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') {
                $x = [int](($matches[1] + $matches[3]) / 2)
                $y = [int](($matches[2] + $matches[4]) / 2)
                return @{ X = $x; Y = $y; Found = $true }
            }
        }
    }
    return @{ Found = $false }
}

function Tap-Element {
    param([int]$X, [int]$Y)
    Write-Host "Tapping at coordinates: $X, $Y" -ForegroundColor Yellow
    adb -s $DeviceId shell input tap $X $Y
}

function Test-ListingsScreen {
    Write-TestStep "Testing Listings Screen Access"
    
    # Capture current state
    Capture-UIState "before_listings_test"
    
    # Look for Browse button or listings navigation
    $coords = Get-ElementCoordinates "ui_before_listings_test.xml" "Browse"
    if ($coords.Found) {
        Write-Host "Found Browse button, tapping..." -ForegroundColor Green
        Tap-Element $coords.X $coords.Y
        Wait-ForSeconds $DelayBetweenSteps
        Capture-UIState "listings_screen"
        return $true
    }
    
    # Try to find listings in navigation
    $coords = Get-ElementCoordinates "ui_before_listings_test.xml" "Listings"
    if ($coords.Found) {
        Write-Host "Found Listings navigation, tapping..." -ForegroundColor Green
        Tap-Element $coords.X $coords.Y
        Wait-ForSeconds $DelayBetweenSteps
        Capture-UIState "listings_screen"
        return $true
    }
    
    Write-Host "Could not find listings navigation" -ForegroundColor Red
    return $false
}

function Test-CreateListing {
    Write-TestStep "Testing Create Listing (FAB)"
    
    # Look for FAB (Floating Action Button)
    $coords = Get-ElementCoordinates "ui_listings_screen.xml" "Add" "content-desc"
    if (-not $coords.Found) {
        # Try looking for FAB by resource-id
        if (Test-Path "ui_listings_screen.xml") {
            [xml]$uiXml = Get-Content "ui_listings_screen.xml"
            $fab = $uiXml.SelectSingleNode("//node[contains(@resource-id, 'fab')]")
            if ($fab) {
                $bounds = $fab.bounds
                if ($bounds -match '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') {
                    $coords = @{ 
                        X = [int](($matches[1] + $matches[3]) / 2)
                        Y = [int](($matches[2] + $matches[4]) / 2)
                        Found = $true 
                    }
                }
            }
        }
    }
    
    if ($coords.Found) {
        Write-Host "Found FAB, tapping to create listing..." -ForegroundColor Green
        Tap-Element $coords.X $coords.Y
        Wait-ForSeconds $DelayBetweenSteps
        Capture-UIState "create_listing_screen"
        return $true
    }
    
    Write-Host "Could not find FAB for creating listing" -ForegroundColor Red
    return $false
}

function Test-ListingForm {
    Write-TestStep "Testing Listing Form Fields"
    
    if (-not (Test-Path "ui_create_listing_screen.xml")) {
        Write-Host "Create listing screen not captured" -ForegroundColor Red
        return $false
    }
    
    [xml]$uiXml = Get-Content "ui_create_listing_screen.xml"
    
    # Check for form fields
    $formFields = @("Crop Type", "Quantity", "Price", "Description")
    $foundFields = @()
    
    foreach ($field in $formFields) {
        $element = $uiXml.SelectSingleNode("//node[contains(@text, '$field') or contains(@hint, '$field')]")
        if ($element) {
            $foundFields += $field
            Write-Host "Found form field: $field" -ForegroundColor Green
        }
    }
    
    Write-Host "Found form fields: $($foundFields -join ', ')" -ForegroundColor Cyan
    return $foundFields.Count -gt 0
}

function Test-SearchAndFilter {
    Write-TestStep "Testing Search and Filter Features"
    
    # Go back to listings screen if needed
    adb -s $DeviceId shell input keyevent 4  # Back button
    Wait-ForSeconds 2
    Capture-UIState "listings_for_search"
    
    # Look for search functionality
    $searchCoords = Get-ElementCoordinates "ui_listings_for_search.xml" "Search"
    if (-not $searchCoords.Found) {
        # Try looking for search icon
        if (Test-Path "ui_listings_for_search.xml") {
            [xml]$uiXml = Get-Content "ui_listings_for_search.xml"
            $searchElement = $uiXml.SelectSingleNode("//node[contains(@content-desc, 'search') or contains(@resource-id, 'search')]")
            if ($searchElement) {
                $bounds = $searchElement.bounds
                if ($bounds -match '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') {
                    $searchCoords = @{ 
                        X = [int](($matches[1] + $matches[3]) / 2)
                        Y = [int](($matches[2] + $matches[4]) / 2)
                        Found = $true 
                    }
                }
            }
        }
    }
    
    if ($searchCoords.Found) {
        Write-Host "Found search functionality" -ForegroundColor Green
        Tap-Element $searchCoords.X $searchCoords.Y
        Wait-ForSeconds 2
        Capture-UIState "search_active"
        return $true
    }
    
    Write-Host "Search functionality not found" -ForegroundColor Yellow
    return $false
}

# Main test execution
Write-TestStep "Starting Listings Functionality Testing"

# Check if app is running
Write-Host "Checking if Agrimarket app is running..." -ForegroundColor Yellow
$runningApps = adb -s $DeviceId shell "ps | grep $PackageName"
if (-not $runningApps) {
    Write-Host "Starting Agrimarket app..." -ForegroundColor Yellow
    adb -s $DeviceId shell am start -n "$PackageName/.MainActivity"
    Wait-ForSeconds 5
}

# Test sequence
$testResults = @{
    "ListingsScreenAccess" = Test-ListingsScreen
    "CreateListingFAB" = Test-CreateListing
    "ListingFormFields" = Test-ListingForm
    "SearchAndFilter" = Test-SearchAndFilter
}

# Report results
Write-TestStep "Listings Functionality Test Results"
foreach ($test in $testResults.GetEnumerator()) {
    $status = if ($test.Value) { "PASS" } else { "FAIL" }
    $color = if ($test.Value) { "Green" } else { "Red" }
    Write-Host "$($test.Key): $status" -ForegroundColor $color
}

Write-Host "`nTest completed. Check captured screenshots and UI dumps for details." -ForegroundColor Cyan
