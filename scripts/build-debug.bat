@echo off
REM Script para construir APK Debug en Windows
REM Uso: scripts\build-debug.bat

echo 🔨 Building Debug APK...

REM Navegar al directorio raíz del proyecto
cd /d "%~dp0.."

REM Construir APK debug
call gradlew.bat assembleDebug

REM Verificar si el APK fue creado
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ✅ APK Debug construido exitosamente!
    echo 📦 Ubicación: app\build\outputs\apk\debug\app-debug.apk
    for %%A in ("app\build\outputs\apk\debug\app-debug.apk") do echo 📊 Tamaño: %%~zA bytes
) else (
    echo ❌ Error: No se encontró el APK
    exit /b 1
)

pause

