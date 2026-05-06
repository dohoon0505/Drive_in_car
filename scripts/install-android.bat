@echo off
rem Drive in Car — Android 빌드 + 실기기 재설치 (Windows 더블클릭용 래퍼)
rem
rem 사용법:
rem   install-android.bat            # 기본 (빌드 + 재설치)
rem   install-android.bat -Launch    # 설치 후 자동 실행
rem   install-android.bat -Release   # release 빌드

setlocal
chcp 65001 > nul
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0install-android.ps1" %*
set EXITCODE=%ERRORLEVEL%
if not "%~1"=="--no-pause" pause
endlocal & exit /b %EXITCODE%
