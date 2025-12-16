# Configuración de GitHub Actions para Builds Automáticos

Este documento explica cómo configurar GitHub Actions para generar APKs automáticamente en cada push.

## 🔧 Configuración Inicial

### 1. Habilitar GitHub Actions

GitHub Actions está habilitado por defecto. El workflow se ejecutará automáticamente cuando hagas push a la rama `main` o `master`.

### 2. Configurar Secrets para APK Release (Opcional)

Para generar APKs firmados (release), necesitas configurar los siguientes secrets en GitHub:

1. Ve a tu repositorio en GitHub
2. Haz clic en **Settings** → **Secrets and variables** → **Actions**
3. Haz clic en **New repository secret** y agrega los siguientes:

#### Secrets Requeridos:

- **`KEYSTORE_PASSWORD`**: Contraseña del keystore
- **`KEY_ALIAS`**: Alias de la clave (normalmente `key0`)
- **`KEY_PASSWORD`**: Contraseña de la clave
- **`KEYSTORE_BASE64`**: El archivo keystore codificado en Base64

#### Cómo obtener KEYSTORE_BASE64:

**En Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("app\keystore.jks")) | Out-File -Encoding ASCII keystore_base64.txt
```

**En Linux/Mac:**
```bash
base64 -i app/keystore.jks -o keystore_base64.txt
```

Luego copia el contenido del archivo `keystore_base64.txt` y pégalo en el secret `KEYSTORE_BASE64`.

### 3. Crear el Keystore (si aún no lo tienes)

Si no tienes un keystore, créalo primero:

```bash
keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias key0
```

**⚠️ IMPORTANTE:** Guarda una copia segura del keystore y las contraseñas. Si lo pierdes, no podrás actualizar la app.

## 🚀 Cómo Funciona

### Flujo Automático

1. **Push a GitHub**: Cuando haces push a `main` o `master`
2. **GitHub Actions se activa**: El workflow comienza automáticamente
3. **Build Debug APK**: Siempre se genera un APK debug
4. **Build Release APK**: Solo si los secrets están configurados
5. **Crear Release**: Se crea un nuevo release en GitHub con los APKs

### Acceder a los APKs

1. Ve a tu repositorio en GitHub
2. Haz clic en **Releases** en el menú lateral
3. Descarga el APK más reciente

## 📝 Notas Importantes

- **Debug APK**: Siempre se genera, incluso sin secrets
- **Release APK**: Solo se genera si los secrets están configurados
- **Versionado**: El número de versión se lee de `app/build.gradle.kts`
- **Tags**: Cada release crea un tag automático (v1.0, v1.1, etc.)

## 🔄 Actualizar la Versión

Para actualizar la versión de la app, edita `app/build.gradle.kts`:

```kotlin
versionCode = 2  // Incrementa este número
versionName = "1.1"  // Actualiza esta versión
```

Luego haz commit y push. El nuevo release se creará automáticamente.

## 🐛 Solución de Problemas

### El workflow falla

- Verifica que el código compile localmente
- Revisa los logs de GitHub Actions para ver el error específico

### No se genera el Release APK

- Verifica que todos los secrets estén configurados correctamente
- Asegúrate de que el keystore esté codificado correctamente en Base64

### El APK no se instala

- Verifica que el APK no esté corrupto (descárgalo nuevamente)
- Asegúrate de tener habilitada la instalación desde fuentes desconocidas

## 📚 Recursos Adicionales

- [Documentación de GitHub Actions](https://docs.github.com/en/actions)
- [Guía de Instalación](INSTALL.md)
- [Documentación de Android sobre Firma](https://developer.android.com/studio/publish/app-signing)

