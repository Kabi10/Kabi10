@echo off
echo ============================================================
echo  Closing ALL Chrome processes (including background ones)...
echo ============================================================

:kill_loop
taskkill /f /im chrome.exe >nul 2>&1
tasklist /fi "imagename eq chrome.exe" 2>nul | find /i "chrome.exe" >nul 2>&1
if not errorlevel 1 (
    echo  Still running... waiting...
    timeout /t 1 /nobreak >nul
    goto kill_loop
)

echo  All Chrome processes gone.
echo.
echo  Launching Chrome with remote debugging on port 9222...
timeout /t 1 /nobreak >nul

"C:\Program Files\Google\Chrome\Application\chrome.exe" ^
    --remote-debugging-port=9222 ^
    --no-first-run ^
    --no-default-browser-check ^
    --user-data-dir="C:\Users\Tharma\AppData\Local\Google\Chrome\User Data" ^
    --profile-directory=Default ^
    "https://distrokid.com/new"
