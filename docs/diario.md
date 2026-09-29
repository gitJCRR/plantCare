# Diario de desarrollo

Notas para la memoria final (problemas encontrados, soluciones y uso de la IA).

## 2026-09-29 — Proyecto inicial

- Proyecto creado con Android Studio (plantilla *Empty Activity*, Kotlin DSL, minSdk 26).
- Repositorio en GitHub y primer commit.
- **Uso de la IA:** consulta a un asistente (Claude) para planificar la práctica por fases,
  elegir el stack tecnológico y configurar el repositorio.

### Problemas encontrados

- (ninguno todavía)

## 2026-09-29 — Dependencias y navegación

- Añadidas dependencias: Hilt, Room, Navigation Compose, KSP, Kotlin Serialization,
  Material 3 adaptive (NavigationSuiteScaffold) e iconos extendidos.
- Navegación type-safe con 7 destinos (Login, Registro, Inicio, Detalle, Editar, Luz, Perfil).
  Paso de parámetros: `PlantDetailRoute(plantId)` y `PlantEditRoute(plantId)`.
- Barra inferior en móvil / rail lateral en pantallas anchas con `NavigationSuiteScaffold`.
- Tras login/registro y al cerrar sesión se limpia la pila de navegación.

### Problemas encontrados

- `git push` fallaba con *SSL peer certificate or SSH remote key was not OK*.
  **Solución:** `git config http.sslBackend schannel` para usar los certificados de Windows.
- `git push` pedía usuario/contraseña y GitHub ya no admite contraseña.
  **Solución:** iniciar sesión mediante el navegador desde Android Studio (Git Credential Manager).

### Uso de la IA

- Claude consultó las versiones estables más recientes de cada librería, configuró Gradle
  y generó el esqueleto de navegación; se verificó compilando y recorriendo las pantallas
  en el emulador.
