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
REM taskkill /f /im java.exe 2>nul
call gradlew --stop
timeout /t 1 /nobreak
if exist "app\build\intermediates" rd /s /q "app\build\intermediates" 2>nul
call gradlew clean
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
adb logcat | findstr "%LOG_TAG%"

endlocal
exit /b 0
@rem -------------------------------------------------------------------