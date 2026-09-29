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

| Fase | Fecha | Contenido | Commits |
|---|---|---|---|
| 0. Planificación | 29/09/2026 | Análisis del enunciado, elección de temática y stack | — |
| 1. Proyecto base | 29/09/2026 | Plantilla Compose (minSdk 26), README, repositorio | `00e7b41` |
| 2. Dependencias | 29/09/2026 | Hilt, Room, Navigation, KSP, Serialization | `c27c7c0` |
| 3. Navegación | 29/09/2026 | 7 destinos type-safe, paso de parámetros, barra/rail adaptativo | `0c9051f` |
| 4. Base de datos | *(pendiente)* | Entidades, DAO, repositorio, ViewModel, rejilla de plantas | |
| 5. Autenticación | *(pendiente)* | Firebase Auth en login y registro | |
| 6. Detalle y edición | *(pendiente)* | Formulario, historial de cuidados | |
| 7. Funcionalidades avanzadas | *(pendiente)* | Sensor de luz, cámara, notificaciones | |
| 8. Adaptativo y pulido | *(pendiente)* | Lista-detalle, tema, accesibilidad | |

### Tecnologías y versiones

| Tecnología | Versión |
|---|---|
| Android Gradle Plugin | 9.4.1 |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.09.00 |
| Navigation Compose | 2.10.2 |
| Hilt | 2.60.1 |
| Room | 2.8.5 |
| minSdk / targetSdk | 26 / 37 |

*(Pendiente: explicación del código más relevante de cada fase.)*

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
