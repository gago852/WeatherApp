# Google Play Store Deployment Guide

Este documento explica cómo configurar y usar el workflow de despliegue a Google Play Store.

## 📋 Requisitos Previos

### 1. Secrets de GitHub

Debes configurar los siguientes secrets en tu repositorio de GitHub (`Settings` → `Secrets and variables` → `Actions` → `New repository secret`):

#### Secrets existentes (ya configurados):
- `GOOGLE_SERVICES_JSON`: Archivo google-services.json codificado en base64

#### Secrets nuevos requeridos:

1. **KEYSTORE_BASE64**
   - Tu archivo de firma (keystore) codificado en base64
   - Para generarlo:
     ```bash
     base64 -w 0 your-release-keystore.jks > keystore.txt
     ```
   - En Windows (PowerShell):
     ```powershell
     [Convert]::ToBase64String([IO.File]::ReadAllBytes("your-release-keystore.jks")) | Out-File keystore.txt
     ```
   - Copia el contenido de `keystore.txt` al secret

2. **SIGNING_KEY_ALIAS**
   - El alias de tu clave de firma
   - Ejemplo: `my-app-key`

3. **SIGNING_KEY_PASSWORD**
   - La contraseña de tu clave de firma

4. **SIGNING_STORE_PASSWORD**
   - La contraseña de tu keystore

5. **PLAY_STORE_SERVICE_ACCOUNT_JSON**
   - JSON de la cuenta de servicio de Google Play
   - Para obtenerlo:
     1. Ve a [Google Play Console](https://play.google.com/console)
     2. Ve a `Setup` → `API access`
     3. Crea o selecciona un proyecto de Google Cloud
     4. Crea una cuenta de servicio
     5. Otorga permisos necesarios (Release manager o Admin)
     6. Descarga el archivo JSON
     7. Copia todo el contenido del JSON al secret

### 2. Configuración de Google Play Console

1. Tu app debe estar creada en Google Play Console
2. Debes haber subido al menos una versión manualmente primero
3. La cuenta de servicio debe tener permisos de `Release Manager` o superior

## 🚀 Uso del Workflow

### Ejecución Manual

El workflow `Deploy to Play Store` puede ejecutarse manualmente desde la pestaña `Actions` de GitHub:

1. Ve a `Actions` → `Deploy to Play Store` → `Run workflow`
2. Selecciona las opciones:
   - **Play Store track**: Elige el canal donde subir el AAB
     - `internal`: Testing interno (usuarios de prueba internos)
     - `alpha`: Alpha testing (grupo cerrado)
     - `beta`: Beta testing (grupo abierto o cerrado)
     - `production`: Producción (tienda pública)
   - **GitHub Release type**: Tipo de release en GitHub
     - `prerelease`: Pre-lanzamiento (para betas, alphas, etc.)
     - `release`: Lanzamiento oficial

3. Haz clic en `Run workflow`

### Ejecución Automática

El workflow se ejecuta automáticamente cuando:
- El workflow `gradle-test` termina exitosamente en la rama `main`
- En este caso, se sube al track `internal` como `prerelease` por defecto

## 📦 Qué hace el Workflow

1. **Espera a que pasen los tests**: Solo se ejecuta si el workflow de tests fue exitoso
2. **Extrae la versión**: Lee `versionName` y `versionCode` de `app/build.gradle.kts`
3. **Construye el AAB firmado**: Genera el archivo `.aab` con firma de release
4. **Sube a Play Store**: Sube el AAB al track seleccionado en modo **draft** (no publicado)
5. **Crea GitHub Release**: Genera un release en GitHub con:
   - Tag basado en la versión y el track (ej: `v0.1.4-beta`)
   - Archivos adjuntos (AAB y mapping file)
   - Release notes automáticas
   - Tipo de release (prerelease o release)

## 📝 Estructura de Tags

Los tags se generan automáticamente según el track:

- **Production**: `v0.1.4` (si es release) o `v0.1.4` (si es prerelease)
- **Beta**: `v0.1.4-beta`
- **Alpha**: `v0.1.4-alpha`
- **Internal**: `v0.1.4-internal`

## 🔐 Seguridad

- El AAB se firma automáticamente usando el keystore configurado
- Todos los secretos están protegidos en GitHub Secrets
- El mapping file (ProGuard) se sube automáticamente a Play Store para debugging
- Los archivos se mantienen como artefactos por 30-90 días

## ⚠️ Notas Importantes

1. **El AAB NO se publica automáticamente**: Se sube en modo `draft`, debes publicarlo manualmente desde Play Console
2. **Versión**: Asegúrate de incrementar `versionCode` y `versionName` en `app/build.gradle.kts` antes de ejecutar el workflow
3. **Tests**: El workflow solo se ejecuta si los tests pasan exitosamente
4. **Primera subida**: Debes hacer la primera subida a Play Store manualmente antes de usar el workflow

## 🐛 Troubleshooting

### Error: "Package not found"
- Asegúrate de que la app ya existe en Play Console
- Verifica que `packageName` en el workflow coincida con `applicationId` en `build.gradle.kts`

### Error: "Version code X has already been used"
- Incrementa el `versionCode` en `app/build.gradle.kts`

### Error: "Insufficient permissions"
- Verifica que la cuenta de servicio tenga permisos de `Release Manager`
- Revisa que el JSON de la cuenta de servicio sea correcto

### Error de firma
- Verifica que todos los secrets de firma estén configurados correctamente
- Asegúrate de que el keystore esté codificado en base64 correctamente

## 📚 Referencias

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Google Play Console API](https://developers.google.com/android-publisher)
- [Android App Signing](https://developer.android.com/studio/publish/app-signing)
