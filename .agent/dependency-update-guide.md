# Guía de Actualización de Dependencias - WeatherApp
*Generado: 2 de Octubre, 2025*

## Resumen Ejecutivo

Este documento detalla las actualizaciones disponibles para las dependencias del proyecto WeatherApp, identificando cambios de ruptura (breaking changes) y proporcionando guías de migración para cada biblioteca.

---

## 📊 Estado Actual vs Versiones Disponibles

| Dependencia | Versión Actual | Última Versión | Prioridad | Riesgo |
|-------------|---------------|----------------|-----------|--------|
| AGP | 8.13.0 | 8.13.0 ✅ | - | - |
| Kotlin | 2.0.21 | 2.2.20 | ALTA | MEDIO |
| Compose BOM | 2025.05.00 | 2025.09.01 | ALTA | BAJO |
| Core KTX | 1.16.0 | 1.16.0 ✅ | - | - |
| Lifecycle Runtime | 2.9.0 | 2.9.1 | MEDIA | BAJO |
| Activity Compose | 1.10.1 | 1.11.0 | MEDIA | BAJO |
| Navigation | 2.9.0 | 2.9.1 | MEDIA | MEDIO |
| Hilt | 2.51.1 | 2.57 | ALTA | ALTO |
| Retrofit | 2.12.0 | 2.12.0 (3.0.0 disponible) | BAJA | ALTO* |
| Moshi | 1.15.2 | 1.15.2 ✅ | - | - |
| Firebase BOM | 33.13.0 | 34.0.0 | MEDIA | BAJO |
| DataStore | 1.1.6 | 1.1.6 ✅ | - | - |
| Play Services Location | 21.3.0 | 21.3.0 ✅ | - | - |
| Google Places | 4.4.1 | 5.0.0 | MEDIA | BAJO |

*Retrofit 3.0.0 es una actualización mayor opcional con cambios significativos

---

## 🔴 Cambios Críticos de Ruptura (Breaking Changes)

### 1. Hilt 2.51.1 → 2.57

#### Breaking Changes:
1. **Visibilidad de constructores generados**: Los constructores de `Factory` y `MembersInjector` generados cambiaron de `public` a `private`
2. **Requisitos de nulabilidad en @Binds**: Se requieren declaraciones explícitas de nulabilidad
3. **Soporte Jakarta annotations**: Cambio de `javax` a `jakarta` en algunos contextos

#### Ejemplo de Migración:

**ANTES (2.51.1):**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    
    @Binds
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository  // Nulabilidad implícita
}
```

**DESPUÉS (2.57):**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    
    @Binds
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository  // Ahora requiere declaración explícita
    
    // Si el método puede retornar null, debe ser explícito:
    // @Binds
    // abstract fun bindOptionalService(impl: ServiceImpl?): Service?
}
```

**Acciones Requeridas:**
- ✅ Verificar todos los módulos Hilt para declaraciones `@Binds`
- ✅ Asegurar nulabilidad explícita en todas las interfaces
- ✅ Si usas custom factories, verificar que no dependan de constructores públicos

---

### 2. Google Places 4.4.1 → 5.0.0

#### Breaking Changes:
1. **MinSDK aumentado**: Ahora requiere API 23+ (Android 6.0) - ✅ Tu proyecto usa minSDK 27, cumple el requisito
2. **Java 8 requerido**: Deshabilitación automática de desugaring de bibliotecas Java 8
3. **APIs deprecadas eliminadas**: Algunas APIs antiguas fueron removidas

#### Ejemplo de Migración:

**build.gradle.kts (app):**

```kotlin
android {
    defaultConfig {
        minSdk = 27  // ✅ Ya cumple con el requisito de API 23+
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.google.android.libraries.places:places:5.0.0")  // Actualizar versión
}
```

**Código de Places API:**

**ANTES:**
```kotlin
// Inicialización antigua
Places.initialize(applicationContext, apiKey)
val placesClient = Places.createClient(this)

// Autocomplete antigua
val autocompleteIntent = Autocomplete.IntentBuilder(
    AutocompleteActivityMode.FULLSCREEN, fields
).build()
```

**DESPUÉS:**
```kotlin
// La API principal se mantiene similar, pero verifica deprecaciones
Places.initialize(applicationContext, apiKey)
val placesClient = Places.createClient(this)

// Si usabas APIs deprecadas, migra a las nuevas
val autocompleteIntent = Autocomplete.IntentBuilder(
    AutocompleteActivityMode.FULLSCREEN, fields
)
    .setCountries(listOf("MX"))  // Configura países específicos
    .build()
```

**Acciones Requeridas:**
- ✅ **No requiere cambio de minSDK** - Tu proyecto usa minSDK 27, ya cumple con API 23+
- ✅ Actualizar versión en `libs.versions.toml` a 5.0.0
- ✅ Revisar toda la integración de Places API
- ✅ Probar funcionalidad de autocompletado
- ✅ Verificar permisos de ubicación

---

### 3. Firebase BOM 33.13.0 → 34.0.0

#### Breaking Changes:
1. **MinSDK aumentado a API 23**: Requiere Android 6.0+ - ✅ Tu proyecto usa minSDK 27, cumple el requisito
2. **Migración de módulos KTX**: Algunos módulos se consolidaron

#### Ejemplo de Migración:

**build.gradle.kts:**

```kotlin
android {
    defaultConfig {
        minSdk = 27  // ✅ Ya cumple con el requisito de API 23+
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))  // Actualizar versión
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    // Las extensiones KTX ahora están integradas en los módulos principales
}
```

**Acciones Requeridas:**
- ✅ **No requiere cambio de minSDK** - Tu proyecto usa minSDK 27
- ✅ Actualizar versión en `libs.versions.toml` a 34.0.0
- ✅ Verificar logs de Crashlytics
- ✅ Probar eventos de Analytics

---

### 4. Navigation Compose 2.9.0 → 2.9.1

#### Mejora Principal:
**Type-Safe Navigation** usando Kotlin Serialization (nueva característica)

#### Ejemplo de Migración a Type-Safe Navigation:

**ANTES (String-based):**
```kotlin
// Definición de rutas
object Routes {
    const val HOME = "home"
    const val WEATHER_DETAIL = "weather/{cityId}"
    
    fun weatherDetail(cityId: String) = "weather/$cityId"
}

// Navigation Graph
NavHost(navController, startDestination = Routes.HOME) {
    composable(Routes.HOME) {
        HomeScreen(
            onCityClick = { cityId ->
                navController.navigate(Routes.weatherDetail(cityId))
            }
        )
    }
    
    composable(
        route = Routes.WEATHER_DETAIL,
        arguments = listOf(navArgument("cityId") { type = NavType.StringType })
    ) { backStackEntry ->
        val cityId = backStackEntry.arguments?.getString("cityId")
        WeatherDetailScreen(cityId = cityId ?: "")
    }
}
```

**DESPUÉS (Type-Safe):**
```kotlin
import kotlinx.serialization.Serializable

// Definición de destinos con tipos seguros
@Serializable
object HomeDestination

@Serializable
data class WeatherDetailDestination(
    val cityId: String,
    val cityName: String? = null
)

// Navigation Graph con type-safety
NavHost(navController, startDestination = HomeDestination) {
    composable<HomeDestination> {
        HomeScreen(
            onCityClick = { cityId, cityName ->
                navController.navigate(
                    WeatherDetailDestination(
                        cityId = cityId,
                        cityName = cityName
                    )
                )
            }
        )
    }
    
    composable<WeatherDetailDestination> { backStackEntry ->
        val destination = backStackEntry.toRoute<WeatherDetailDestination>()
        WeatherDetailScreen(
            cityId = destination.cityId,
            cityName = destination.cityName
        )
    }
}
```

**Beneficios:**
- ✅ Seguridad de tipos en tiempo de compilación
- ✅ Autocompletado en el IDE
- ✅ Refactorización más segura
- ✅ Eliminación de errores de tipeo en rutas

---

### 5. Kotlin 2.0.21 → 2.2.20

#### Nuevas Características:
1. **Guard conditions en when**: Condiciones adicionales en expresiones when
2. **Non-local break/continue**: Mayor flexibilidad en bucles
3. **Multi-dollar string interpolation**: Mejor manejo de strings con templates

#### Ejemplos de Nuevas Características:

**1. Guard Conditions en When:**

**ANTES:**
```kotlin
fun analyzeWeather(weather: Weather) = when (weather.condition) {
    WeatherCondition.RAIN -> {
        if (weather.temperature < 10) {
            "Lluvia fría - abrígate bien"
        } else {
            "Lluvia templada - lleva paraguas"
        }
    }
    WeatherCondition.SUNNY -> "Soleado"
    else -> "Otro clima"
}
```

**DESPUÉS (Con Guard Conditions):**
```kotlin
fun analyzeWeather(weather: Weather) = when (weather.condition) {
    WeatherCondition.RAIN if weather.temperature < 10 -> 
        "Lluvia fría - abrígate bien"
    WeatherCondition.RAIN -> 
        "Lluvia templada - lleva paraguas"
    WeatherCondition.SUNNY if weather.uvIndex > 7 -> 
        "Soleado con UV alto - usa protector"
    WeatherCondition.SUNNY -> 
        "Soleado"
    else -> "Otro clima"
}
```

**2. Non-local Break/Continue:**

**ANTES:**
```kotlin
fun processWeatherData(cities: List<City>) {
    var foundTarget = false
    
    cities.forEach { city ->
        if (foundTarget) return@forEach
        
        city.forecasts.forEach { forecast ->
            if (forecast.isTarget) {
                foundTarget = true
                return@forEach
            }
            // procesar forecast
        }
    }
}
```

**DESPUÉS:**
```kotlin
fun processWeatherData(cities: List<City>) {
    loop@ cities.forEach { city ->
        city.forecasts.forEach { forecast ->
            if (forecast.isTarget) {
                break@loop  // ⚡ Sale de ambos loops
            }
            // procesar forecast
        }
    }
}
```

**Acciones Requeridas:**
- ✅ Actualizar versión de Kotlin en `libs.versions.toml`
- ✅ Habilitar K2 mode en Android Studio (2024.1+) para mejor rendimiento
- ✅ Opcional: Refactorizar código para usar nuevas características

---

### 6. Compose BOM 2025.05.00 → 2025.09.01

#### Breaking Changes:
Principalmente correcciones de bugs y mejoras de rendimiento. Cambios menores en APIs.

#### Mejoras Principales:
- compose-runtime: 1.7.2 → 1.9.2
- compose-material3: 1.3.2 → 1.4.0
- Mejor rendimiento en recomposiciones
- Nuevos componentes Material3

#### Ejemplo de Migración:

**build.gradle.kts:**
```kotlin
dependencies {
    // Actualizar solo el BOM
    implementation(platform("androidx.compose:compose-bom:2025.09.01"))
    
    // Todas estas versiones se manejan automáticamente por el BOM
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material3:material3-adaptive")
}
```

**Verificar nuevos componentes Material3:**
```kotlin
// Ejemplo: Nuevos componentes de navegación adaptativa
@Composable
fun WeatherAppScaffold() {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            // Items de navegación
        }
    ) {
        // Contenido principal
    }
}
```

**Acciones Requeridas:**
- ✅ Actualizar BOM en `libs.versions.toml`
- ✅ Probar todas las pantallas de la app
- ✅ Verificar que las recomposiciones funcionen correctamente
- ✅ Aprovechar nuevos componentes Material3 si es necesario

---

### 7. Retrofit 2.12.0 → 3.0.0 (OPCIONAL - ALTO RIESGO)

⚠️ **ADVERTENCIA**: Esta es una actualización mayor con cambios significativos. Considerar solo si necesitas las nuevas características.

#### Breaking Changes:
1. **OkHttp 4.x requerido**: Migración de OkHttp 3.x a 4.x
2. **APIs deprecadas eliminadas**: Varias APIs antiguas fueron removidas
3. **Cambios en manejo de errores**: Nueva estructura para error handling

#### Ejemplo de Migración:

**build.gradle.kts:**
```kotlin
dependencies {
    // Retrofit 3.0 requiere OkHttp 4.x
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-moshi:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")  // Nueva versión
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
```

**Configuración de OkHttp:**

**ANTES (OkHttp 3.x):**
```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()
```

**DESPUÉS (OkHttp 4.x):**
```kotlin
import java.time.Duration

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .connectTimeout(Duration.ofSeconds(30))  // Nueva API de Duration
    .readTimeout(Duration.ofSeconds(30))
    .build()
```

**Recomendación**: 
- 🔴 **NO actualizar** a menos que sea absolutamente necesario
- ✅ Mantener Retrofit 2.12.0 que es estable y compatible
- ✅ Considerar actualización solo si necesitas características específicas de 3.0

---

## 📋 Plan de Migración Recomendado

### Fase 1: Actualizaciones de Bajo Riesgo (Prioridad ALTA)
**Tiempo estimado: 1-2 días**

1. **Lifecycle Runtime KTX**: 2.9.0 → 2.9.1
   ```toml
   lifecycleRuntimeKtx = "2.9.1"
   ```

2. **Activity Compose**: 1.10.1 → 1.11.0
   ```toml
   activityCompose = "1.11.0"
   ```

3. **Navigation Compose**: 2.9.0 → 2.9.1
   ```toml
   navigation = "2.9.1"
   ```

**Testing requerido:**
- ✅ Navegación entre pantallas
- ✅ SavedStateHandle en ViewModels
- ✅ Ciclo de vida de composables

---

### Fase 2: Actualizaciones de Riesgo Medio (Prioridad ALTA)
**Tiempo estimado: 2-3 días**

1. **Compose BOM**: 2025.05.00 → 2025.09.01
   ```toml
   composeBom = "2025.09.01"
   ```

2. **Kotlin**: 2.0.21 → 2.2.20
   ```toml
   kotlin = "2.2.20"
   ksp = "2.2.20-1.0.30"  # Actualizar KSP también
   ```

**Testing requerido:**
- ✅ UI completa de la aplicación
- ✅ Recomposiciones
- ✅ Compilación completa del proyecto
- ✅ Tests unitarios e integración

---

### Fase 3: Actualizaciones de Riesgo Moderado (Prioridad MEDIA)
**Tiempo estimado: 2-3 días**

✅ **NOTA**: Tu proyecto usa minSDK 27, por lo que Firebase y Places no presentan problemas de compatibilidad

1. **Hilt**: 2.51.1 → 2.57
   ```toml
   hilt = "2.57"
   ```

2. **Firebase BOM**: 33.13.0 → 34.0.0
   ```toml
   firebaseBom = "34.0.0"
   ```

3. **Google Places**: 4.4.1 → 5.0.0
   ```toml
   googlePlaces = "5.0.0"
   ```

**Testing requerido:**
- ✅ Todas las funcionalidades de Hilt/DI
- ✅ Firebase Analytics y Crashlytics
- ✅ Places API y autocompletado
- ✅ Permisos de ubicación
- ✅ Regresión completa de funcionalidades

---

### Fase 4: Actualizaciones Opcionales (Prioridad BAJA)
**Solo si es necesario**

1. **Retrofit**: 2.12.0 → 3.0.0 (NO RECOMENDADO)
   - Solo actualizar si necesitas características específicas de 3.0
   - Requiere migración completa de OkHttp a versión 4.x

---

## 🧪 Checklist de Testing

### Para cada fase:

#### Pre-actualización:
- [ ] Crear branch de feature para las actualizaciones
- [ ] Documentar versiones actuales
- [ ] Ejecutar suite completa de tests
- [ ] Tomar screenshot del estado actual de la app

#### Post-actualización:
- [ ] Compilación exitosa sin warnings críticos
- [ ] Suite de tests unitarios pasa al 100%
- [ ] Suite de tests de integración pasa
- [ ] Navegación funciona correctamente
- [ ] UI se renderiza correctamente
- [ ] No hay regresiones visuales
- [ ] Performance no degradada

#### Testing manual:
- [ ] Búsqueda de ciudades (Places API)
- [ ] Obtención de ubicación actual
- [ ] Carga de datos del clima (Retrofit)
- [ ] Navegación entre pantallas
- [ ] Estado guardado al rotar dispositivo
- [ ] Funcionamiento de Firebase Analytics
- [ ] Logs de Crashlytics

---

## ⚠️ Decisiones Críticas Requeridas

### 1. Retrofit 3.0.0 - NO RECOMENDADO ACTUALIZAR

**Razón**: Cambio mayor con alto riesgo y baja recompensa para tu proyecto actual.

**Recomendación**: Mantener Retrofit 2.12.0 que es estable y suficiente.

---

## 📚 Recursos Adicionales

### Documentación oficial:
- [Kotlin 2.2.20 Release Notes](https://kotlinlang.org/docs/releases.html)
- [Compose BOM Mapping](https://developer.android.com/develop/ui/compose/bom)
- [Navigation Type Safety](https://developer.android.com/guide/navigation/design/type-safety)
- [Hilt Migration Guide](https://github.com/google/dagger/releases)
- [Firebase Android Release Notes](https://firebase.google.com/support/release-notes/android)
- [Google Places SDK Migration](https://developers.google.com/maps/documentation/places/android-sdk/release-notes)

---

## 📝 Notas Finales

### Ventanas de compatibilidad:
- **AGP 8.13.0** requiere Gradle 8.13+
- **Kotlin 2.2.20** es compatible con AGP 8.13.0
- **KSP** debe coincidir con versión de Kotlin: use `2.2.20-1.0.30`

### Backup y rollback:
- Crear tags de Git antes de cada fase
- Mantener branches separados por fase
- Documentar cualquier issue encontrado
- Tener plan de rollback para cada actualización

### Siguientes pasos:
1. Revisar este documento con el equipo
2. Planificar sprints para cada fase
3. Asignar recursos de testing
4. Ejecutar fase por fase con testing completo
5. Considerar adopción de Type-Safe Navigation (altamente recomendado)

---

**Generado automáticamente usando análisis de Perplexity AI**
