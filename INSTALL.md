# Guía de Instalación - How much do you know

Esta guía te ayudará a instalar la aplicación en tu dispositivo Android y en el de tu pareja.

## 📱 Opción 1: Instalar desde GitHub Releases (Recomendado)

### Paso 1: Descargar el APK

1. Ve a la página de tu repositorio en GitHub
2. Haz clic en **"Releases"** en el menú lateral
3. Descarga el APK más reciente (recomendamos `app-release.apk` para producción o `app-debug.apk` para pruebas)

### Paso 2: Habilitar instalación desde fuentes desconocidas

1. En tu dispositivo Android, ve a **Configuración** → **Seguridad** (o **Privacidad**)
2. Activa **"Instalar aplicaciones desconocidas"** o **"Fuentes desconocidas"**
3. Si usas Android 8.0+, necesitarás permitir la instalación para la app específica que usarás para descargar (Chrome, Drive, etc.)

### Paso 3: Instalar el APK

1. Abre el archivo APK descargado
2. Toca **"Instalar"**
3. Espera a que termine la instalación
4. Toca **"Abrir"** o busca la app en el menú de aplicaciones

---

## 🔌 Opción 2: Instalar vía ADB (Android Debug Bridge)

Esta opción es útil para desarrolladores o si quieres instalar directamente desde tu computadora.

### Requisitos previos

- Android SDK Platform Tools instalado
- USB Debugging habilitado en tu dispositivo
- Cable USB para conectar tu dispositivo

### Paso 1: Habilitar Opciones de Desarrollador

1. Ve a **Configuración** → **Acerca del teléfono**
2. Toca **"Número de compilación"** 7 veces
3. Verás un mensaje que dice "Ahora eres desarrollador"

### Paso 2: Habilitar Depuración USB

1. Ve a **Configuración** → **Opciones de desarrollador** (o **Sistema** → **Opciones de desarrollador**)
2. Activa **"Depuración USB"**
3. Conecta tu dispositivo a la computadora con un cable USB
4. En tu dispositivo, aparecerá un diálogo pidiendo permiso. Toca **"Permitir"**

### Paso 3: Instalar el APK

1. Abre una terminal en tu computadora
2. Navega a la carpeta donde descargaste el APK
3. Ejecuta:
   ```bash
   adb devices
   ```
   Deberías ver tu dispositivo listado

4. Instala el APK:
   ```bash
   adb install app-debug.apk
   ```
   o
   ```bash
   adb install app-release.apk
   ```

---

## 📤 Opción 3: Compartir APK entre dispositivos

### Método 1: Compartir por email/Drive

1. Descarga el APK en tu dispositivo
2. Comparte el archivo por:
   - Email
   - Google Drive
   - WhatsApp
   - Cualquier método de compartir archivos
3. En el otro dispositivo, descarga el APK y sigue los pasos de la **Opción 1**

### Método 2: Compartir vía Bluetooth

1. Descarga el APK en tu dispositivo
2. Comparte el archivo vía Bluetooth
3. En el otro dispositivo, acepta el archivo y sigue los pasos de la **Opción 1**

---

## 🔨 Opción 4: Compilar y construir localmente

Si quieres construir el APK tú mismo:

### Windows

```bash
# APK Debug
scripts\build-debug.bat

# APK Release (requiere keystore)
scripts\build-release.bat
```

### Linux/Mac

```bash
# Dar permisos de ejecución (solo la primera vez)
chmod +x scripts/build-debug.sh
chmod +x scripts/build-release.sh

# APK Debug
./scripts/build-debug.sh

# APK Release (requiere keystore)
./scripts/build-release.sh
```

Los APKs se generarán en:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

---

## 🔐 Crear un Keystore para APKs Release

Si quieres construir APKs firmados (release), necesitas crear un keystore:

### Paso 1: Crear el keystore

Abre una terminal en el directorio raíz del proyecto y ejecuta:

```bash
keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias key0
```

Completa la información solicitada:
- Contraseña del keystore (guárdala bien)
- Información personal (nombre, organización, etc.)
- Contraseña de la clave (puede ser la misma que la del keystore)

### Paso 2: Configurar keystore.properties

1. Copia `keystore.properties.example` a `keystore.properties`
2. Edita `keystore.properties` y completa los valores:
   ```properties
   storeFile=app/keystore.jks
   storePassword=tu_contraseña_del_keystore
   keyAlias=key0
   keyPassword=tu_contraseña_de_la_clave
   ```

### ⚠️ IMPORTANTE

- **Guarda una copia segura del keystore.jks y las contraseñas**
- Si pierdes el keystore, NO podrás actualizar la app en Google Play Store
- El archivo `keystore.properties` NO debe subirse a GitHub (ya está en .gitignore)

---

## 🚀 Instalación Automática con GitHub Actions

Cada vez que hagas push a la rama `main` o `master`, GitHub Actions automáticamente:

1. Compilará la app
2. Generará APKs debug y release
3. Creará un nuevo Release en GitHub con los APKs adjuntos

Los APKs estarán disponibles en: **GitHub → Releases**

---

## ❓ Solución de Problemas

### "No se puede instalar esta app"

- Verifica que tengas habilitada la instalación desde fuentes desconocidas
- Asegúrate de que el APK no esté corrupto (descárgalo nuevamente)

### "La app no se abre"

- Verifica que tu dispositivo cumpla con los requisitos mínimos (Android 9.0+)
- Reinstala la app

### "Error al instalar vía ADB"

- Verifica que la depuración USB esté habilitada
- Asegúrate de que el dispositivo esté conectado correctamente
- Ejecuta `adb devices` para verificar la conexión

### "No puedo encontrar Opciones de Desarrollador"

- Asegúrate de haber tocado "Número de compilación" 7 veces
- En algunos dispositivos, está en **Configuración** → **Sistema** → **Opciones de desarrollador**

---

## 📞 Soporte

Si tienes problemas con la instalación, revisa:
- Los logs de GitHub Actions (si usas builds automáticos)
- Los mensajes de error en tu dispositivo
- La documentación de Android sobre instalación de APKs

