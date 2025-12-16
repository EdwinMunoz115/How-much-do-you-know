@echo off
REM Script para construir APK Release firmado en Windows
REM Uso: scripts\build-release.bat

echo 🔨 Building Release APK...

REM Navegar al directorio raíz del proyecto
cd /d "%~dp0.."

REM Verificar si existe keystore.properties
if not exist "keystore.properties" (
    echo ❌ Error: No se encontró keystore.properties
    echo 📝 Por favor, crea el archivo keystore.properties basándote en keystore.properties.example
    echo 📖 Ver INSTALL.md para más información sobre cómo crear un keystore
    exit /b 1
)

REM Construir APK release
call gradlew.bat assembleRelease

REM Verificar si el APK fue creado
if exist "app\build\outputs\apk\release\app-release.apk" (
    echo ✅ APK Release construido exitosamente!
    echo 📦 Ubicación: app\build\outputs\apk\release\app-release.apk
    for %%A in ("app\build\outputs\apk\release\app-release.apk") do echo 📊 Tamaño: %%~zA bytes
) else (
    echo ❌ Error: No se encontró el APK
    exit /b 1
)

pause

