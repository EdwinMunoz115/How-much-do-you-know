#!/bin/bash

# Script para construir APK Debug
# Uso: ./scripts/build-debug.sh

set -e

echo "🔨 Building Debug APK..."

# Navegar al directorio raíz del proyecto
cd "$(dirname "$0")/.."

# Dar permisos de ejecución a gradlew si no los tiene
chmod +x gradlew

# Construir APK debug
./gradlew assembleDebug

# Mostrar ubicación del APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo "✅ APK Debug construido exitosamente!"
    echo "📦 Ubicación: $APK_PATH"
    echo "📊 Tamaño: $(du -h "$APK_PATH" | cut -f1)"
else
    echo "❌ Error: No se encontró el APK en $APK_PATH"
    exit 1
fi

