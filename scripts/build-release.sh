#!/bin/bash

# Script para construir APK Release firmado
# Uso: ./scripts/build-release.sh

set -e

echo "🔨 Building Release APK..."

# Navegar al directorio raíz del proyecto
cd "$(dirname "$0")/.."

# Verificar si existe keystore.properties
if [ ! -f "keystore.properties" ]; then
    echo "❌ Error: No se encontró keystore.properties"
    echo "📝 Por favor, crea el archivo keystore.properties basándote en keystore.properties.example"
    echo "📖 Ver INSTALL.md para más información sobre cómo crear un keystore"
    exit 1
fi

# Dar permisos de ejecución a gradlew si no los tiene
chmod +x gradlew

# Construir APK release
./gradlew assembleRelease

# Mostrar ubicación del APK
APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ -f "$APK_PATH" ]; then
    echo "✅ APK Release construido exitosamente!"
    echo "📦 Ubicación: $APK_PATH"
    echo "📊 Tamaño: $(du -h "$APK_PATH" | cut -f1)"
else
    echo "❌ Error: No se encontró el APK en $APK_PATH"
    exit 1
fi

