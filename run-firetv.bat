@echo off
setlocal

REM ===============================
REM CONFIG — CHANGE THESE
REM ===============================

set FIRE_TV_IP=192.168.1.104
set APP_ID=com.replex.tv
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
set LOG_TAG=RePlex

REM ===============================
REM ADB SETUP
REM ===============================

echo.
echo [0/5] Cleaning build artifacts and building APK...
taskkill /f /im java.exe 2>nul
timeout /t 1 /nobreak
call gradlew assembleDebug
if errorlevel 1 (
    echo BUILD FAILED
    pause
    exit /b 1
)

echo.
echo [1/5] Restarting ADB...
adb kill-server
adb start-server

echo.
echo [2/5] Connecting to Fire TV at %FIRE_TV_IP%...
adb connect %FIRE_TV_IP%:5555

echo.
echo [3/5] Clearing app data and installing APK...
adb shell pm clear %APP_ID%
timeout /t 1 /nobreak
adb install -r "%APK_PATH%"
if errorlevel 1 (
    echo INSTALL FAILED
    pause
    exit /b 1
)

echo.
echo [4/5] Launching app...
adb shell monkey -p %APP_ID% -c android.intent.category.LEANBACK_LAUNCHER 1

echo.
echo [5/5] Clearing logcat and monitoring logs (CTRL+C to stop)...
adb logcat -c
timeout /t 2 /nobreak
adb logcat | findstr "RePlex"

endlocal
exit /b 0
@rem -------------------------------------------------------------------