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
| Mis plantas (inicio) | `HomeRoute` | Rejilla con las plantas del usuario; destaca las que toca regar |
| Detalle | `PlantDetailRoute(plantId)` | Datos de la planta, historial de cuidados, botón «Regada» |
| Añadir / editar | `PlantEditRoute(plantId)` | Formulario con foto de la cámara; `plantId = -1` crea una nueva |
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

- **UI (Compose):** pantallas sin lógica de negocio; reciben un estado y emiten eventos.
- **ViewModel:** expone un `StateFlow<UiState>` inmutable y recibe las acciones del usuario.
- **Repository:** única puerta de acceso a los datos; oculta si vienen de Room o de Firebase.
- **Fuentes de datos:** Room (plantas y cuidados) y Firebase Authentication (usuarios).
- **Hilt** crea e inyecta todas las dependencias (base de datos, DAO, repositorios, ViewModels).

Organización de paquetes:

```
com.tareaandroid.plantcare
├── PlantCareApp.kt        (@HiltAndroidApp)
├── MainActivity.kt        (@AndroidEntryPoint)
├── navigation/            rutas, NavHost y destinos principales
├── ui/
│   ├── auth/              login y registro
│   ├── home/              lista de plantas
│   ├── detail/            detalle
│   ├── edit/              alta / edición
│   ├── light/             medidor de luz
│   ├── settings/          perfil y ajustes
│   ├── components/        composables reutilizables
│   └── theme/             Material 3
├── data/                  (pendiente) entidades, DAO, base de datos, repositorios
└── di/                    (pendiente) módulos de Hilt
```

### 2.3 Diseño adaptativo

- `NavigationSuiteScaffold` de Material 3 Adaptive: **barra de navegación inferior** en móviles y
  **rail lateral** en pantallas anchas (tablets, plegables, horizontal).
- *(Pendiente: rejilla con número de columnas adaptativo y vista lista-detalle en tablet.)*

## 3. Base de datos empleada

*(Pendiente de implementar.)* Diseño previsto:

- **Firebase Authentication** para el registro e inicio de sesión de usuarios.
- **Room** (SQLite) para los datos de la aplicación, con dos entidades:
  - `Plant`: id, userId, nombre, especie, ubicación, foto, frecuencia de riego (días),
    fecha del último riego, frecuencia de abono, notas.
  - `CareEvent`: id, plantId, tipo (riego, abono, poda, trasplante), fecha, nota.
  - Relación **1:N**: una planta tiene muchos cuidados (clave foránea con borrado en cascada).
- El esquema se exporta automáticamente a `app/schemas/` para documentarlo y versionarlo.

*(Pendiente: diagrama entidad-relación y consultas principales del DAO.)*

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
| — Documentación | 29/09/2026 | Borrador de memoria y seguimiento de requisitos | `b590f0b` |
| 4. Base de datos | *(pendiente)* | Entidades, DAO, repositorio, ViewModel, rejilla de plantas | |
| 5. Autenticación | *(pendiente)* | Firebase Auth en login y registro | |
| 6. Detalle y edición | *(pendiente)* | Formulario, historial de cuidados | |
| 7. Funcionalidades avanzadas | *(pendiente)* | Sensor de luz, cámara, notificaciones | |
| 8. Adaptativo y pulido | *(pendiente)* | Lista-detalle, tema, accesibilidad | |

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

### 5.3 Justificación de los requisitos técnicos obligatorios

Cada requisito del enunciado, cómo se ha implementado y dónde puede comprobarse en el código.

| # | Requisito del enunciado | Implementación y justificación | Dónde | Estado |
|---|---|---|---|---|
| 1 | Jetpack Compose para la interfaz | Toda la UI se declara con funciones `@Composable`; no hay layouts XML. | `ui/**` | ✅ |
| 2 | Material 3 | Tema `PlantCareTheme` (M3) y componentes M3: `Button`, `NavigationSuiteScaffold`… | `ui/theme/`, `ui/**` | ✅ |
| 3 | Navigation Compose, ≥ 5 destinos | 7 destinos declarados como clases `@Serializable` en un `NavHost`. | `navigation/Routes.kt`, `navigation/PlantCareNavHost.kt` | ✅ |
| 4 | Paso de parámetros entre pantallas | `PlantDetailRoute(plantId)` y `PlantEditRoute(plantId)`; se recuperan con `toRoute<>()`, con tipo comprobado en compilación. | `navigation/` | ✅ |
| 5 | Gestión correcta del estado | *(fase 4)* Cada ViewModel expone un `StateFlow<UiState>` inmutable; la UI lo observa con `collectAsStateWithLifecycle` (flujo unidireccional de datos). | | ⬜ |
| 6 | Lista LazyColumn / LazyRow / Grid | *(fase 4)* `LazyVerticalGrid` de plantas en Inicio; *(fase 6)* `LazyColumn` con el historial de cuidados. | | ⬜ |
| 7 | Arquitectura MVVM | *(fase 4)* UI → ViewModel → Repository → Room / Firebase. | | ⬜ |
| 8 | Uso de ViewModel | *(fase 4)* Un `@HiltViewModel` por pantalla con lógica. | | ⬜ |
| 9 | Patrón Repository | *(fases 4-5)* `PlantRepository` y `AuthRepository` como interfaces con su implementación. | | ⬜ |
| 10 | Persistencia Room y/o Firebase | Room para plantas y cuidados (datos locales, sin conexión); Firebase Auth para usuarios. *(fases 4-5)* | | 🟡 |
| 11 | Inyección de dependencias | Hilt: `@HiltAndroidApp`, `@AndroidEntryPoint`; *(fase 4)* módulos en `di/`. | `PlantCareApp.kt`, `MainActivity.kt` | 🟡 |
| 12 | Gestión de permisos | *(fase 7)* `CAMERA` y `POST_NOTIFICATIONS` solicitados en tiempo de ejecución, con explicación y manejo de la denegación. | | ⬜ |
| 13 | Interfaz adaptativa | `NavigationSuiteScaffold`: barra inferior en móvil y rail lateral en pantalla ancha; *(fase 8)* rejilla adaptativa y lista-detalle. | `navigation/PlantCareNavHost.kt` | 🟡 |

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

*(Pendiente: explicación del código más relevante de cada fase, con fragmentos.)*

## 6. Problemas encontrados y soluciones

| Problema | Solución |
|---|---|
| El emulador mostraba la pantalla en negro dentro de Android Studio aunque la app estaba en ejecución | Era un retraso al refrescar el panel del emulador; se comprobó con `adb` que la app estaba en primer plano |
| `git push` fallaba con *SSL peer certificate or SSH remote key was not OK* | Configurar git para usar los certificados de Windows: `git config http.sslBackend schannel` |
| GitHub rechazaba la autenticación por contraseña | Iniciar sesión mediante el navegador desde Android Studio (Git Credential Manager guarda la credencial) |

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

**Valoración.** *(Pendiente, al final: qué ha aportado, qué limitaciones se han encontrado, qué
ha habido que corregir y qué se ha aprendido.)*

## Anexo A. Seguimiento de requisitos

Ver [`requisitos.md`](requisitos.md): revisión punto por punto del enunciado y de los criterios de
evaluación, con el estado de cada uno.

## Anexo B. Correspondencia con los criterios de evaluación

| Criterio (puntos) | Qué se evalúa (enunciado) | Cómo lo cubre PlantCare | Dónde se justifica |
|---|---|---|---|
| 1. Funcionamiento y requisitos funcionales (2) | Login, registro, inicio, detalle, perfil/ajustes y pantallas de la temática; navegación coherente; operaciones principales completas y sin errores importantes | Las 7 pantallas; alta, edición, borrado y registro de cuidados de plantas; login/registro reales con Firebase; pila de navegación limpia tras login/logout | 2.1 · capturas *(pendiente)* · vídeo |
| 2. Compose, navegación y estado (1) | Uso correcto de Compose, navegación y estado; componentes bien separados | Una carpeta por pantalla; composables *stateless* que reciben estado y eventos; rutas type-safe; `UiState` + `StateFlow` | 5.3, requisitos 1-6 |
| 3. Arquitectura y organización (1) | UI → ViewModel → Repository → fuente de datos; inyección de dependencias; organización de paquetes | Capas separadas en `ui/`, `data/`, `di/`, `navigation/`; Hilt inyecta base de datos, DAO, repositorios y ViewModels | 2.2 · 5.3, requisitos 7-9 y 11 |
| 4. Base de datos (1) | Uso de Firebase o Room | Room con `Plant` y `CareEvent` (1:N, clave foránea, esquema exportado) + Firebase Auth | 3 · 5.3, requisito 10 |
| 5. Funcionalidades avanzadas (1) | Al menos dos: cámara, sensores, QR, mapas, notificaciones, audio, vídeo, gráficos… | Sensor de luz, cámara, notificaciones y gráficos (cuatro, el doble del mínimo) | 5.4 |
| 6. Diseño adaptativo (1) | Diseño adaptativo para distintos dispositivos | Barra/rail de navegación, rejilla con columnas según el ancho, lista-detalle en tablet | 2.3 · 5.3, requisito 13 |
| 7. Documentación (1) | Calidad de la documentación | Todos los apartados del enunciado, justificación por requisito, capturas, diagramas y fragmentos de código | Esta memoria |
| 8. GitHub (1) | Repositorio bien estructurado y evolución razonable mediante commits | Commits pequeños por fase con mensajes descriptivos; README; `docs/` | 5.2 · <https://github.com/gitJCRR/plantCare> |
| 9. Vídeo (1) | Muestra todas las funcionalidades relevantes y explica la estructura del código | Guion: demo de cada pantalla y funcionalidad, luego recorrido por paquetes, capas y código destacado (≤ 15 min) | Enlace en la portada *(pendiente)* |
