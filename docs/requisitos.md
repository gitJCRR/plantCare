# Seguimiento de requisitos

Revisión punto por punto del enunciado de la práctica final (*Aplicaciones para dispositivos
móviles*). Se actualiza en cada fase del desarrollo.

**Leyenda:** ✅ hecho · 🟡 en progreso / provisional · ⬜ pendiente

## 1. Requisitos visuales

| Requisito | Estado | Implementación en PlantCare |
|---|---|---|
| Pantalla de login | 🟡 | `ui/auth/LoginScreen.kt`: pantalla provisional; falta Firebase Auth |
| Pantalla de registro si no estás logueado | 🟡 | `ui/auth/RegisterScreen.kt`: provisional |
| Pantalla de inicio con elementos de la temática | ✅ | `ui/home/HomeScreen.kt`: rejilla de plantas desde Room, aviso de riego, estado vacío |
| Pantalla de detalle | ✅ | `ui/detail/PlantDetailScreen.kt`: ficha, cuidados, historial, editar y borrar |
| Pantalla de perfil/ajustes | 🟡 | `ui/settings/SettingsScreen.kt`: solo cerrar sesión |
| Pantalla de funcionalidades avanzadas | 🟡 | `ui/light/LightMeterScreen.kt` (sensor de luz); cámara en `ui/edit` |

## 2. Requisitos técnicos obligatorios

| Requisito | Estado | Implementación |
|---|---|---|
| Jetpack Compose para la interfaz | ✅ | Toda la UI es Compose; sin XML de layouts |
| Material 3 | ✅ | `material3` + `PlantCareTheme`; falta paleta propia |
| Navigation Compose con ≥ 5 destinos | ✅ | 7 destinos en `navigation/Routes.kt` |
| Paso de parámetros entre pantallas | ✅ | `PlantDetailRoute(plantId)`, `PlantEditRoute(plantId)` (type-safe) |
| Gestión correcta del estado de la interfaz | ✅ | `HomeUiState`, `PlantEditUiState`, `PlantDetailUiState` en `StateFlow` + `collectAsStateWithLifecycle`; eventos `PlantEditEvent` |
| Lista con LazyColumn / LazyRow / Grid | ✅ | `LazyVerticalGrid` en Inicio, `LazyColumn` en Detalle |
| Arquitectura MVVM | ✅ | UI → ViewModel → `PlantRepository` → Room |
| Uso de ViewModel | ✅ | `HomeViewModel`, `PlantEditViewModel`, `PlantDetailViewModel` (`@HiltViewModel`) |
| Patrón Repository | ✅ | `PlantRepository` + `PlantRepositoryImpl`; `AuthRepository` en la fase 5 |
| Persistencia Room y/o Firebase | ✅ | Room: `plants` y `care_events` (1:N); Firebase Auth en la fase 5 |
| Inyección de dependencias | ✅ | Hilt: `DatabaseModule`, `RepositoryModule`, `@HiltViewModel` |
| Gestión de permisos Android | ⬜ | Previsto: `CAMERA`, `POST_NOTIFICATIONS` |
| Interfaz adaptativa | 🟡 | Barra/rail, rejilla `GridCells.Adaptive`, anchos máximos; falta lista-detalle en tablet |

## 3. Funcionalidades avanzadas (mínimo 2)

| Funcionalidad | Estado | Uso en la app |
|---|---|---|
| Sensor de luz | ⬜ | Medir los lux de un lugar y compararlos con lo que necesita la planta |
| Cámara | ⬜ | Foto de cada planta |
| Notificaciones | ⬜ | Recordatorios de riego con WorkManager |
| Gráficos (opcional) | ⬜ | Historial de cuidados por mes |

## 4. Documentación (memoria)

| Apartado obligatorio | Estado |
|---|---|
| Introducción | 🟡 |
| Diseño de la aplicación | 🟡 |
| Base de datos empleada | ✅ (Room; falta Firebase) |
| División del trabajo (individual, se indica) | ✅ |
| Desarrollo de la aplicación | 🟡 |
| Problemas encontrados y solución | 🟡 |
| Puntos fuertes / débiles | ⬜ |
| Conclusiones y vías futuras | ⬜ |
| Uso de la IA | 🟡 |

Borrador en [`memoria.md`](memoria.md); notas del día a día en [`diario.md`](diario.md).

## 5. Vídeo (≤ 15 min, YouTube «oculto»)

- ⬜ Demostración de la app en el emulador o en un móvil
- ⬜ Estructura del proyecto: paquetes, clases más relevantes, vistas, código a destacar
- ⬜ Subido a YouTube con privacidad **oculta**

## 6. Entrega (PDF)

- ⬜ Memoria en PDF
- ✅ Enlace a GitHub con commits que muestran la evolución: <https://github.com/gitJCRR/plantCare>
- ⬜ Enlace al vídeo de YouTube

## 7. Criterios de evaluación

| Criterio | Puntos | Cómo se cubre | Estado |
|---|---|---|---|
| Funcionamiento y requisitos funcionales | 2 | Login, registro, inicio, detalle, perfil y pantallas propias sin errores | 🟡 inicio, detalle y formulario hechos |
| Compose, navegación y estado | 1 | Componentes separados, navegación type-safe, `UiState` | ✅ |
| Arquitectura y organización | 1 | UI → ViewModel → Repository → fuente de datos, Hilt, paquetes por capa | ✅ |
| Base de datos | 1 | Room (`Plant`, `CareEvent`, relación 1:N) + Firebase Auth | ✅ Room · 🟡 Firebase |
| Funcionalidades avanzadas | 1 | Sensor de luz, cámara, notificaciones (+ gráficos) | ⬜ |
| Diseño adaptativo | 1 | `NavigationSuiteScaffold`, rejilla adaptativa, lista-detalle en tablet | 🟡 |
| Documentación | 1 | `docs/memoria.md` → PDF | 🟡 |
| GitHub | 1 | Commits pequeños y descriptivos por fase | ✅ en curso |
| Vídeo | 1 | Guion con demo + recorrido por el código | ⬜ |
