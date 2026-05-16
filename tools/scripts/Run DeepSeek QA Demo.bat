@echo off
echo DeepSeek QA Demo - DNA Folding Information
echo =========================================
echo.
echo This is a demonstration version that works without requiring an API key.
echo It will provide information about DNA folding.
echo.

cd %~dp0deepseek_qa
py -3 demo_qa.py "What is DNA folding?"

echo.
echo Press any key to exit
pause > nul 