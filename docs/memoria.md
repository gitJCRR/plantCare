# PlantCare — Memoria de la práctica final

**Asignatura:** Aplicaciones para dispositivos móviles · Grado en Ingeniería Informática
**Autor:** Juan Carlos Ros Robles
**Repositorio:** <https://github.com/gitJCRR/plantCare>
**Vídeo:** *(pendiente: enlace de YouTube, oculto)*

> **Borrador vivo.** Se completa a medida que avanza el desarrollo. Al final se exporta a PDF
> para la entrega. Las notas en *cursiva entre paréntesis* indican contenido pendiente.

---

## 1. Introducción

PlantCare es una aplicación Android para el cuidado y mantenimiento de plantas de interior y
exterior. Permite registrar las plantas del usuario con su foto, especie y ubicación, indicar
cada cuánto necesitan riego y abono, llevar un historial de cuidados y recibir recordatorios
cuando toca regarlas. Además incluye un medidor de luz que usa el sensor del móvil para saber si
un lugar de la casa tiene la luz adecuada para una planta.

**Motivación.** Muchas plantas mueren por exceso o falta de riego o por estar en un lugar con
poca luz. Una app sencilla que recuerde los cuidados y ayude a elegir la ubicación resuelve un
problema real y permite aplicar la mayoría de los contenidos de la asignatura.

**Objetivos de la práctica** (según el enunciado):

- Aplicar los conceptos básicos de programación Android y sus buenas prácticas.
- Usar Jetpack Compose con Material 3, Navigation Compose, arquitectura MVVM, patrón
  Repository, persistencia (Room / Firebase) e inyección de dependencias.
- Integrar funcionalidades avanzadas del dispositivo (sensores, cámara, notificaciones…).
- Diseñar una interfaz adaptativa a distintos tamaños de pantalla.

## 2. Diseño de la aplicación

### 2.1 Pantallas y navegación

| Pantalla | Ruta | Descripción |
|---|---|---|
| Login | `LoginRoute` | Acceso con email y contraseña (destino inicial) |
| Registro | `RegisterRoute` | Alta de usuario si no tiene cuenta |
| Mis plantas (inicio) | `HomeRoute` | Rejilla con las plantas del usuario, ordenadas por próximo riego; aviso «Toca regar» |
| Detalle | `PlantDetailRoute(plantId)` | Ficha de la planta, botones Regar / Abonar / Podar / Trasplantar, historial de cuidados, editar y borrar |
| Añadir / editar | `PlantEditRoute(plantId)` | Formulario validado (y foto de la cámara en la fase 7); `plantId = -1` crea una nueva |
| Medidor de luz | `LightMeterRoute` | Lectura del sensor de luz ambiental en lux |
| Perfil / ajustes | `SettingsRoute` | Datos del usuario, recordatorios, cerrar sesión |

La navegación usa **Navigation Compose con rutas type-safe**: cada destino es una clase
`@Serializable` y los parámetros (como `plantId`) viajan como propiedades tipadas en lugar de
cadenas, lo que evita errores en tiempo de ejecución.

Flujo principal:

```
Login ──► Registro
  │           │
  └─────┬─────┘  (se limpia la pila: no se puede volver al login con «atrás»)
        ▼
  ┌── Mis plantas ──► Detalle(plantId) ──► Editar(plantId)
  │        └────────► Añadir planta
  ├── Luz
  └── Perfil ──► Cerrar sesión ──► Login (se limpia la pila)
```

*(Pendiente: capturas de cada pantalla.)*

### 2.2 Arquitectura

Se sigue la arquitectura vista en clase: **UI → ViewModel → Repository → fuente de datos**.

```
┌──────────────────┐  eventos (onEvent, clics)   ┌───────────────────┐
│  Pantalla        │ ──────────────────────────► │  ViewModel        │
│  (Compose)       │ ◄────────────────────────── │  @HiltViewModel   │
└──────────────────┘  estado (StateFlow<UiState>)└─────────┬─────────┘
                                                           │ suspend / Flow
                                                 ┌─────────▼─────────┐
                                                 │  PlantRepository  │ (interfaz)
                                                 │  PlantRepositoryImpl
                                                 └─────────┬─────────┘
                                                           │
                                                 ┌─────────▼─────────┐
                                                 │ Room: PlantDao,   │
                                                 │ CareEventDao      │
                                                 └───────────────────┘
                 Hilt crea e inyecta cada pieza (di/DatabaseModule, di/RepositoryModule)
```

- **UI (Compose):** cada pantalla tiene dos funciones. `XxxScreen` obtiene el ViewModel con
  `hiltViewModel()` y observa su estado; `XxxContent` es *stateless*: solo recibe el estado y
  emite eventos, por lo que puede previsualizarse con `@Preview` sin base de datos.
- **ViewModel:** expone un `StateFlow<UiState>` inmutable y recibe las acciones del usuario. Los
  parámetros de navegación los lee de su `SavedStateHandle`, no los recibe de la pantalla.
- **Repository:** única puerta de acceso a los datos. Los ViewModels dependen de la interfaz
  `PlantRepository`, no de Room; además contiene la lógica de negocio (registrar un cuidado
  actualiza historial y fechas en una transacción).
- **Modelo de dominio** (`model/`): `Plant` y `CareEvent` son independientes de Room. La lógica de
  riego (próximo riego, días restantes, si toca regar) está en el modelo y tiene pruebas unitarias.
- **Fuentes de datos:** Room (plantas y cuidados) y Firebase Authentication (usuarios, fase 5).
- **Hilt** crea e inyecta todas las dependencias (base de datos, repositorios, ViewModels).

Organización de paquetes:

```
com.tareaandroid.plantcare
├── PlantCareApp.kt            @HiltAndroidApp
├── MainActivity.kt            @AndroidEntryPoint
├── model/                     modelo de dominio: Plant, CareEvent, LightLevel, CareType
├── data/
│   ├── local/                 PlantCareDatabase, Converters, Mappers (entidad <-> modelo)
│   │   ├── entity/            PlantEntity, CareEventEntity (tablas)
│   │   └── dao/               PlantDao, CareEventDao (consultas)
│   └── repository/            PlantRepository (interfaz) y PlantRepositoryImpl
├── di/                        DatabaseModule, RepositoryModule (Hilt)
├── navigation/                Routes, TopLevelDestination, PlantCareNavHost
└── ui/
    ├── auth/                  login y registro
    ├── home/                  HomeScreen, HomeViewModel, PlantCard
    ├── detail/                PlantDetailScreen, PlantDetailViewModel
    ├── edit/                  PlantEditScreen, PlantEditViewModel
    ├── light/                 medidor de luz
    ├── settings/              perfil y ajustes
    ├── components/            componentes y textos reutilizables
    └── theme/                 tema Material 3
```

### 2.3 Diseño adaptativo

- `NavigationSuiteScaffold` de Material 3 Adaptive: **barra de navegación inferior** en móviles y
  **rail lateral** en pantallas anchas (tablets, plegables, horizontal).
- **Rejilla de plantas adaptativa:** `LazyVerticalGrid` con `GridCells.Adaptive(160.dp)`; el
  número de columnas depende del ancho disponible (2 en un móvil vertical, más en horizontal o
  tablet) sin código específico por dispositivo.
- **Ancho máximo de lectura:** el formulario (600 dp) y el detalle (720 dp) se centran en pantallas
  anchas en lugar de estirarse.
- *(Pendiente, fase 8: vista lista-detalle en tablet y pruebas en distintos tamaños.)*

## 3. Base de datos empleada

Se combinan dos tecnologías según la naturaleza de cada dato:

- **Room (SQLite local)** para las plantas y su historial: son datos del dispositivo que deben
  estar disponibles sin conexión y consultarse de forma reactiva.
- **Firebase Authentication** para los usuarios *(fase 5)*: gestiona registro, contraseñas y
  sesión de forma segura sin guardar credenciales en el móvil.

### 3.1 Modelo entidad-relación

```
┌──────────────────────────────┐          ┌──────────────────────────────┐
│ plants                       │          │ care_events                  │
├──────────────────────────────┤          ├──────────────────────────────┤
│ PK id            INTEGER     │ 1      N │ PK id            INTEGER     │
│    userId        TEXT  (idx) │──────────│ FK plantId       INTEGER (idx)│
│    name          TEXT        │          │    type          TEXT        │
│    species       TEXT        │          │    date          INTEGER     │
│    location      TEXT        │          │    note          TEXT        │
│    photoUri      TEXT?       │          └──────────────────────────────┘
│    waterEveryDays INTEGER    │           ON DELETE CASCADE
│    lastWatered   INTEGER?    │
│    fertilizeEveryDays INTEGER?│
│    lastFertilized INTEGER?   │
│    lightLevel    TEXT        │
│    notes         TEXT        │
└──────────────────────────────┘
```

**Decisiones de diseño:**

- **Relación 1:N con clave foránea y `ON DELETE CASCADE`:** al borrar una planta se borra su
  historial automáticamente; no pueden quedar cuidados huérfanos.
- **Índices** en `plants.userId` (se filtra por usuario en cada consulta) y en
  `care_events.plantId` (se consulta el historial de una planta).
- **`userId` desde el principio:** cada usuario verá solo sus plantas. Hasta la fase 5 vale
  `"local"`; después será el uid de Firebase, sin necesidad de migrar la base de datos.
- **Fechas** (`LocalDate`) guardadas como número de días desde 1970 mediante un `TypeConverter`;
  los **enums** (`LightLevel`, `CareType`) se guardan por nombre, legibles en la base de datos.
- **Campos opcionales** (`photoUri`, `fertilizeEveryDays`, fechas) son anulables: no todas las
  plantas tienen foto ni necesitan abono.
- El **esquema se exporta** a `app/schemas/.../1.json` para documentarlo y preparar migraciones.

### 3.2 Consultas (DAO)

| DAO | Método | Consulta | Uso |
|---|---|---|---|
| `PlantDao` | `observePlants(userId)` | `SELECT * FROM plants WHERE userId = ? ORDER BY name COLLATE NOCASE` | Rejilla de inicio (`Flow`, se actualiza sola) |
| `PlantDao` | `observePlant(id)` / `getPlant(id)` | `SELECT * FROM plants WHERE id = ?` | Detalle (`Flow`) y formulario de edición |
| `PlantDao` | `insert` / `update` / `deleteById` | `@Insert`, `@Update`, `DELETE … WHERE id = ?` | Alta, edición y borrado |
| `CareEventDao` | `observeEvents(plantId)` | `SELECT * FROM care_events WHERE plantId = ? ORDER BY date DESC, id DESC` | Historial del detalle |
| `CareEventDao` | `insert` | `@Insert` | Registrar un cuidado |

Registrar un riego o un abono modifica dos tablas (nuevo `care_event` y fecha de la planta), por
lo que el repositorio lo hace dentro de una **transacción** (`database.withTransaction`).

### 3.3 Pruebas de la base de datos

Pruebas instrumentadas sobre una base de datos en memoria (`app/src/androidTest/.../data/`):

| Prueba | Qué comprueba |
|---|---|
| `insertAndReadPlant_keepsDatesAndEnums` | Las fechas y enums se guardan y recuperan correctamente |
| `observePlants_filtersByUserAndSortsByName` | Solo se ven las plantas del usuario, ordenadas sin distinguir mayúsculas |
| `deletingPlant_cascadesToCareEvents` | El borrado en cascada elimina el historial |
| `savePlant_insertsThenUpdates` | El repositorio inserta las plantas nuevas y actualiza las existentes |
| `registerWatering_updatesPlantAndHistory` | Regar añade el cuidado y actualiza la fecha del último riego |
| `registerPruning_onlyAddsToHistory` | Podar solo añade el cuidado, sin tocar las fechas de riego |

Resultado: **6/6 pruebas superadas** en el emulador.

## 4. División del trabajo

La práctica se ha realizado **de forma individual**, por lo que todo el diseño, desarrollo,
documentación y vídeo son obra del autor.

## 5. Desarrollo de la aplicación

El desarrollo se ha hecho de forma incremental; cada fase corresponde a uno o varios commits en
GitHub.

### 5.1 Fases del desarrollo

| Fase | Fecha | Contenido | Commits |
|---|---|---|---|
| 0. Planificación | 29/09/2026 | Análisis del enunciado, elección de temática y stack | — |
| 1. Proyecto base | 29/09/2026 | Plantilla Compose (minSdk 26), README, repositorio | `00e7b41` |
| 2. Dependencias | 29/09/2026 | Hilt, Room, Navigation, KSP, Serialization | `c27c7c0` |
| 3. Navegación | 29/09/2026 | 7 destinos type-safe, paso de parámetros, barra/rail adaptativo | `0c9051f` |
| — Documentación | 29/09 y 07/10/2026 | Borrador de memoria, seguimiento de requisitos, justificación y rúbrica | `b590f0b`, `3151909`, `bfad30a` |
| 4. Datos y MVVM | 07/10/2026 | Room, repositorio, Hilt, ViewModels con estado, rejilla de inicio, formulario y detalle con historial | `bf4257e` … `9c25285` (5 commits) |
| 5. Autenticación | *(pendiente)* | Firebase Auth en login y registro, `AuthRepository` | |
| 6. Perfil y ajustes | *(pendiente)* | Datos del usuario, preferencias, cierre de sesión real | |
| 7. Funcionalidades avanzadas | *(pendiente)* | Sensor de luz, cámara, notificaciones, gráficos; permisos | |
| 8. Adaptativo y pulido | *(pendiente)* | Lista-detalle en tablet, tema propio, accesibilidad | |

**Fase 4 en detalle.** Se dividió en pasos pequeños, cada uno probado en el emulador antes de
hacer su commit:

1. **Base de datos Room** (`bf4257e`): entidades, DAO, base de datos y pruebas de los DAO.
2. **Repositorio y Hilt** (`8567c65`): modelo de dominio con la lógica de riego, repositorio,
   módulos de Hilt y pruebas unitarias e instrumentadas.
3. **Inicio** (`51586f0`): `HomeViewModel` con `HomeUiState` y rejilla `LazyVerticalGrid`.
4. **Formulario** (`13490e3`): `PlantEditViewModel` con eventos, validación y alta/edición.
5. **Detalle** (`9c25285`): ficha, registro de cuidados, historial en `LazyColumn` y borrado.

### 5.2 Historial de commits

Se usan mensajes en español con prefijos convencionales (`feat` nueva funcionalidad, `fix`
corrección, `build` dependencias/Gradle, `docs` documentación, `refactor`, `chore` tareas
generales) para que la evolución del proyecto se lea directamente en GitHub. Los commits en los
que ha participado la IA incluyen la línea `Co-Authored-By: Claude` (ver apartado 9).

| Commit | Fecha | Mensaje | Qué aporta | Requisitos que cubre |
|---|---|---|---|---|
| `00e7b41` | 29/09/2026 | chore: proyecto inicial con Jetpack Compose | Plantilla *Empty Activity*, README, `.gitignore`, diario | Compose, Material 3 |
| `c27c7c0` | 29/09/2026 | build: añadir Hilt, Room, Navigation Compose y KSP | Catálogo de versiones, plugins, `PlantCareApp` con `@HiltAndroidApp` | Inyección de dependencias (base), Room (base) |
| `0c9051f` | 29/09/2026 | feat: navegación type-safe con 7 pantallas provisionales | Rutas `@Serializable`, `NavHost`, `NavigationSuiteScaffold`, textos en `strings.xml` | Navigation (≥5 destinos), paso de parámetros, adaptativo (base) |
| `b590f0b` | 29/09/2026 | docs: borrador de memoria y seguimiento de requisitos | `docs/memoria.md`, `docs/requisitos.md` | Documentación |
| `3151909` | 07/10/2026 | docs: justificación de requisitos e historial de commits en la memoria | Apartados 5.2, 5.3, 5.4 y Anexo B | Documentación, GitHub |
| `bfad30a` | 07/10/2026 | docs: detallar en la memoria qué evalúa cada criterio de la rúbrica | Anexo B ampliado | Documentación |
| `bf4257e` | 07/10/2026 | feat: base de datos Room con entidades Plant y CareEvent | Tablas, relación 1:N, DAO con `Flow`, `TypeConverter`, esquema exportado, 3 pruebas | Persistencia (Room) |
| `8567c65` | 07/10/2026 | feat: PlantRepository y módulos de Hilt | Modelo de dominio, repositorio con transacción, `DatabaseModule`, `RepositoryModule`, 8 pruebas | Repository, inyección de dependencias |
| `51586f0` | 07/10/2026 | feat: pantalla de inicio con rejilla de plantas y HomeViewModel | `HomeUiState` + `StateFlow`, `LazyVerticalGrid` adaptativa, estado vacío | ViewModel, estado, lista Lazy, MVVM, adaptativo |
| `13490e3` | 07/10/2026 | feat: formulario para añadir y editar plantas | `PlantEditUiState` + `PlantEditEvent`, validación, `SavedStateHandle.toRoute()` | Estado, paso de parámetros, funcionamiento |
| `9c25285` | 07/10/2026 | feat: detalle de planta con registro de cuidados y borrado | `combine` de planta e historial, `LazyColumn`, diálogo de borrado, Snackbar | Pantalla de detalle, lista Lazy, estado |

### 5.3 Justificación de los requisitos técnicos obligatorios

Cada requisito del enunciado, cómo se ha implementado y dónde puede comprobarse en el código.

| # | Requisito del enunciado | Implementación y justificación | Dónde | Estado |
|---|---|---|---|---|
| 1 | Jetpack Compose para la interfaz | Toda la UI se declara con funciones `@Composable`; no hay layouts XML. | `ui/**` | ✅ |
| 2 | Material 3 | Tema `PlantCareTheme` (M3) y componentes M3: `Button`, `NavigationSuiteScaffold`… | `ui/theme/`, `ui/**` | ✅ |
| 3 | Navigation Compose, ≥ 5 destinos | 7 destinos declarados como clases `@Serializable` en un `NavHost`. | `navigation/Routes.kt`, `navigation/PlantCareNavHost.kt` | ✅ |
| 4 | Paso de parámetros entre pantallas | `PlantDetailRoute(plantId)` y `PlantEditRoute(plantId)`; se recuperan con `toRoute<>()`, con tipo comprobado en compilación. | `navigation/` | ✅ |
| 5 | Gestión correcta del estado | Flujo unidireccional de datos: cada ViewModel expone un `StateFlow` con un estado inmutable (`HomeUiState`, `PlantEditUiState`, `PlantDetailUiState`) que modela también la carga y los errores; la UI lo observa con `collectAsStateWithLifecycle` (deja de escuchar en segundo plano) y envía eventos (`PlantEditEvent`). El estado sobrevive a la rotación porque vive en el ViewModel; el estado puramente visual (diálogo de borrado) usa `rememberSaveable`. | `ui/home/HomeViewModel.kt`, `ui/edit/PlantEditViewModel.kt`, `ui/detail/PlantDetailViewModel.kt` | ✅ |
| 6 | Lista LazyColumn / LazyRow / Grid | `LazyVerticalGrid` adaptativa con las plantas en Inicio y `LazyColumn` con la ficha y el historial en Detalle; ambas con `key` estable por id. | `ui/home/HomeScreen.kt`, `ui/detail/PlantDetailScreen.kt` | ✅ |
| 7 | Arquitectura MVVM | UI → ViewModel → Repository → Room. Las pantallas no acceden a datos; los ViewModels no conocen Room (dependen de la interfaz del repositorio). | `ui/`, `data/repository/`, `data/local/` | ✅ |
| 8 | Uso de ViewModel | `HomeViewModel`, `PlantEditViewModel` y `PlantDetailViewModel`, todos `@HiltViewModel`; leen el parámetro de navegación de su `SavedStateHandle`. | `ui/*/…ViewModel.kt` | ✅ |
| 9 | Patrón Repository | Interfaz `PlantRepository` e implementación `PlantRepositoryImpl` sobre Room, con la lógica de negocio y transacciones. *(Fase 5: `AuthRepository`.)* | `data/repository/` | ✅ |
| 10 | Persistencia Room y/o Firebase | Room con dos tablas relacionadas 1:N, consultas reactivas y esquema exportado (ver apartado 3). *(Fase 5: Firebase Auth.)* | `data/local/` | ✅ |
| 11 | Inyección de dependencias | Hilt: `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`; `DatabaseModule` (`@Provides @Singleton`) crea la base de datos y `RepositoryModule` (`@Binds`) asocia interfaz e implementación. | `di/`, `PlantCareApp.kt` | ✅ |
| 12 | Gestión de permisos | *(fase 7)* `CAMERA` y `POST_NOTIFICATIONS` solicitados en tiempo de ejecución, con explicación y manejo de la denegación. | | ⬜ |
| 13 | Interfaz adaptativa | `NavigationSuiteScaffold` (barra inferior / rail lateral), rejilla `GridCells.Adaptive`, anchos máximos en formulario y detalle; *(fase 8)* lista-detalle en tablet. | `navigation/PlantCareNavHost.kt`, `ui/home/HomeScreen.kt` | 🟡 |

### 5.4 Funcionalidades avanzadas (mínimo 2)

| Funcionalidad | Uso en la app | Dónde | Estado |
|---|---|---|---|
| Sensor de luz | Mide los lux de un lugar y los compara con la luz que necesita la planta | `ui/light/` | ⬜ |
| Cámara | Foto de cada planta | `ui/edit/` | ⬜ |
| Notificaciones | Recordatorios de riego (WorkManager) | *(pendiente)* | ⬜ |
| Gráficos (extra) | Historial de cuidados por mes | *(pendiente)* | ⬜ |

### 5.5 Tecnologías y versiones

| Tecnología | Versión |
|---|---|
| Android Gradle Plugin | 9.4.1 |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.09.00 |
| Navigation Compose | 2.10.2 |
| Hilt | 2.60.1 |
| Room | 2.8.5 |
| minSdk / targetSdk | 26 / 37 |

### 5.6 Código más relevante

**ViewModel con estado reactivo** (`ui/home/HomeViewModel.kt`). El estado se deriva del `Flow` de
Room: cuando se guarda o se riega una planta en otra pantalla, la rejilla se actualiza sola.

```kotlin
val uiState: StateFlow<HomeUiState> = repository.observePlants()
    .map { plants ->
        val today = LocalDate.now()
        HomeUiState.Success(plants.sortedBy { it.daysUntilWatering(today) }, today)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
```

**Pantalla separada en dos funciones** (`ui/home/HomeScreen.kt`). `HomeScreen` conecta con el
ViewModel; `HomeContent` solo pinta, lo que permite previsualizarla y probarla aislada.

```kotlin
@Composable
fun HomeScreen(onPlantClick: (Long) -> Unit, onAddPlant: () -> Unit,
               viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(uiState, onPlantClick, onAddPlant)
}
```

**Parámetro de navegación leído en el ViewModel** (`ui/detail/PlantDetailViewModel.kt`). El
`plantId` llega por la ruta type-safe y se recupera con tipo comprobado:

```kotlin
private val plantId = savedStateHandle.toRoute<PlantDetailRoute>().plantId
```

**Transacción en el repositorio** (`data/repository/PlantRepositoryImpl.kt`). El historial y la
fecha de la planta se actualizan juntos o no se actualiza ninguno:

```kotlin
database.withTransaction {
    careEventDao.insert(CareEventEntity(plantId = plantId, type = type, date = date))
    val plant = plantDao.getPlant(plantId) ?: return@withTransaction
    when (type) {
        CareType.WATER -> plantDao.update(plant.copy(lastWatered = date))
        CareType.FERTILIZE -> plantDao.update(plant.copy(lastFertilized = date))
        CareType.PRUNE, CareType.REPOT -> Unit
    }
}
```

**Inyección de dependencias** (`di/RepositoryModule.kt`). Hilt entrega `PlantRepositoryImpl` a
quien pida un `PlantRepository`; para las pruebas basta con cambiar la implementación.

```kotlin
@Binds
abstract fun bindPlantRepository(impl: PlantRepositoryImpl): PlantRepository
```

### 5.7 Pruebas

| Tipo | Ubicación | Pruebas | Resultado |
|---|---|---|---|
| Unitarias (JVM) | `app/src/test/.../model/PlantTest.kt` | Lógica de riego y abono (5) | ✅ 5/5 |
| Instrumentadas | `app/src/androidTest/.../data/` | DAO y repositorio sobre Room en memoria (6) | ✅ 6/6 |
| Manuales | Emulador Medium Phone (API 37) | Alta con validación, edición, rejilla ordenada, riego/abono/poda/trasplante, historial, borrado con confirmación, persistencia tras reiniciar | ✅ |

## 6. Problemas encontrados y soluciones

| Problema | Solución |
|---|---|
| El emulador mostraba la pantalla en negro dentro de Android Studio aunque la app estaba en ejecución | Era un retraso al refrescar el panel del emulador; se comprobó con `adb` que la app estaba en primer plano |
| `git push` fallaba con *SSL peer certificate or SSH remote key was not OK* | Configurar git para usar los certificados de Windows: `git config http.sslBackend schannel` |
| GitHub rechazaba la autenticación por contraseña | Iniciar sesión mediante el navegador desde Android Studio (Git Credential Manager guarda la credencial) |
| Tras editar los archivos de Gradle fuera de Android Studio, el botón ▶️ desaparecía | Sincronizar el proyecto: *File → Sync Project with Gradle Files* |
| Gradle no podía descargar dependencias nuevas: *PKIX path building failed* | El antivirus (Norton Web/Mail Shield) intercepta las conexiones HTTPS con su propio certificado, en el que Java no confía. Se comprobó el emisor del certificado con `openssl s_client`. Mientras tanto se compiló en modo `--offline` con las dependencias ya descargadas y las pruebas se lanzaron con `adb shell am instrument` |
| La función `hiltViewModel()` de `hilt-navigation-compose` está obsoleta | Usar la nueva librería `hilt-lifecycle-viewmodel-compose` |
| Al pulsar el texto «Necesita abono» no se activaba el interruptor (solo respondía el `Switch`) | Hacer toda la fila pulsable con `Modifier.toggleable(role = Role.Switch)` |
| El aviso «Poda registrado» tenía mal la concordancia de género | Cambiar el texto a «Cuidado registrado: Poda» |

*(Se amplía durante el desarrollo; ver `diario.md`.)*

## 7. Puntos fuertes y puntos débiles

*(Pendiente, al final del desarrollo.)*

## 8. Conclusiones y vías futuras

*(Pendiente: conclusiones de la práctica y de la asignatura.)*

Posibles vías futuras (ideas iniciales):

- Sincronizar las plantas en la nube (Firestore) para usarlas en varios dispositivos.
- Identificar la especie a partir de una foto mediante una API de reconocimiento de plantas.
- Widget en la pantalla de inicio con las plantas que toca regar hoy.

## 9. Uso de la IA

Durante el desarrollo se ha utilizado **Claude (Anthropic)**, a través de Claude Code en la
aplicación de escritorio, como asistente de programación. Su uso se ha registrado en cada fase
en `docs/diario.md`, y los commits en los que ha participado incluyen la línea
`Co-Authored-By: Claude`.

**Para qué se ha usado:**

| Fase | Uso de la IA | Trabajo propio / verificación |
|---|---|---|
| Planificación | Lectura del enunciado, propuesta de temática, stack y calendario por fases | Elección de la temática (plantas) y decisión de hacerla individual |
| Configuración | Guía para instalar Android Studio y crear el proyecto (nombre, paquete, minSdk) | Instalación y creación del proyecto |
| Git / GitHub | Inicialización del repositorio, `.gitignore`, diagnóstico del error SSL | Creación del repositorio en GitHub e inicio de sesión |
| Dependencias | Consulta de las últimas versiones estables y configuración de Gradle | Compilación y ejecución en el emulador |
| Navegación | Generación del esqueleto de rutas y pantallas | Revisión del código y prueba de todos los flujos |
| Documentación | Revisión punto por punto del enunciado y de la rúbrica; borrador de la memoria | Indicación de documentar y justificar todo según la rúbrica; revisión del texto |
| Fase 4 (datos y MVVM) | Diseño de las tablas, generación del código de datos, ViewModels y pantallas, pruebas automáticas y pruebas en el emulador mediante `adb` | Aprobación del plan de la fase, decisión de hacer un commit probado por paso, revisión de las pantallas |
| Problemas | Diagnóstico del error SSL de Gradle (antivirus) y de los fallos de usabilidad detectados al probar | — |

**Valoración.** *(Pendiente, al final: qué ha aportado, qué limitaciones se han encontrado, qué
ha habido que corregir y qué se ha aprendido.)*

## Anexo A. Seguimiento de requisitos

Ver [`requisitos.md`](requisitos.md): revisión punto por punto del enunciado y de los criterios de
evaluación, con el estado de cada uno.

## Anexo B. Correspondencia con los criterios de evaluación

| Criterio (puntos) | Qué se evalúa (enunciado) | Cómo lo cubre PlantCare | Dónde se justifica |
|---|---|---|---|
| 1. Funcionamiento y requisitos funcionales (2) | Login, registro, inicio, detalle, perfil/ajustes y pantallas de la temática; navegación coherente; operaciones principales completas y sin errores importantes | Las 7 pantallas; alta, edición, borrado y registro de cuidados de plantas; login/registro reales con Firebase; pila de navegación limpia tras login/logout | 2.1 · 5.7 Pruebas · capturas *(pendiente)* · vídeo |
| 2. Compose, navegación y estado (1) | Uso correcto de Compose, navegación y estado; componentes bien separados | Una carpeta por pantalla; composables *stateless* que reciben estado y eventos; rutas type-safe; `UiState` + `StateFlow` | 5.3, requisitos 1-6 |
| 3. Arquitectura y organización (1) | UI → ViewModel → Repository → fuente de datos; inyección de dependencias; organización de paquetes | Capas separadas en `ui/`, `data/`, `di/`, `navigation/`; Hilt inyecta base de datos, DAO, repositorios y ViewModels | 2.2 · 5.3, requisitos 7-9 y 11 |
| 4. Base de datos (1) | Uso de Firebase o Room | Room con `Plant` y `CareEvent` (1:N, clave foránea, esquema exportado) + Firebase Auth | 3 · 5.3, requisito 10 |
| 5. Funcionalidades avanzadas (1) | Al menos dos: cámara, sensores, QR, mapas, notificaciones, audio, vídeo, gráficos… | Sensor de luz, cámara, notificaciones y gráficos (cuatro, el doble del mínimo) | 5.4 |
| 6. Diseño adaptativo (1) | Diseño adaptativo para distintos dispositivos | Barra/rail de navegación, rejilla con columnas según el ancho, lista-detalle en tablet | 2.3 · 5.3, requisito 13 |
| 7. Documentación (1) | Calidad de la documentación | Todos los apartados del enunciado, justificación por requisito, capturas, diagramas y fragmentos de código | Esta memoria |
| 8. GitHub (1) | Repositorio bien estructurado y evolución razonable mediante commits | Commits pequeños por fase con mensajes descriptivos; README; `docs/` | 5.2 · <https://github.com/gitJCRR/plantCare> |
| 9. Vídeo (1) | Muestra todas las funcionalidades relevantes y explica la estructura del código | Guion: demo de cada pantalla y funcionalidad, luego recorrido por paquetes, capas y código destacado (≤ 15 min) | Enlace en la portada *(pendiente)* |
