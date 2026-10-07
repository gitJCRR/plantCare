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

## 2026-10-07 — Fase 4: base de datos y arquitectura MVVM

Plan acordado: un commit por paso, cada uno probado en el emulador antes de subirlo.

1. `bf4257e` Room: tablas `plants` y `care_events` (1:N, borrado en cascada), DAO con `Flow`,
   conversor de fechas, esquema exportado. 3 pruebas instrumentadas de los DAO ✅.
2. `8567c65` Modelo de dominio (`Plant` con la lógica de riego), `PlantRepository` +
   implementación con transacción, módulos de Hilt. 5 pruebas unitarias + 3 del repositorio ✅.
3. `51586f0` Inicio: `HomeViewModel` + `HomeUiState`, `LazyVerticalGrid` adaptativa, estado vacío.
4. `13490e3` Formulario: `PlantEditViewModel` con eventos, validación y `SavedStateHandle.toRoute()`.
5. `9c25285` Detalle: planta + historial combinados, registrar cuidados, `LazyColumn`, borrado.

Pruebas manuales en el emulador (con `adb` y `uiautomator`): alta con validación de nombre
vacío, edición (cambio de frecuencia de riego), rejilla reordenada por próximo riego, regar,
abonar, podar y trasplantar con su aviso, historial ordenado, borrado con confirmación y
cancelación, y persistencia de los datos tras reiniciar la app.

### Problemas encontrados

- **Gradle: `PKIX path building failed`** al descargar un componente de pruebas. Con
  `openssl s_client -connect dl.google.com:443` se vio que el certificado lo emitía
  «Norton Web/Mail Shield Root»: el antivirus intercepta HTTPS. Solución provisional:
  compilar con `--offline` (las dependencias ya estaban descargadas) y lanzar las pruebas
  con `adb shell am instrument`. **Solución definitiva:** en Norton, *Protección contra estafas → Web segura →
  Exclusiones*, añadir uno por entrada `dl.google.com`, `maven.google.com`,
  `repo.maven.apache.org`, `repo1.maven.org`, `plugins.gradle.org`, `services.gradle.org` y
  `firebase.google.com`. (Primero se pusieron todos en una sola línea y no funcionó.)
  Comprobado con `openssl` y ejecutando `connectedDebugAndroidTest` desde Gradle.
- **`hiltViewModel()` obsoleto** en `hilt-navigation-compose`: se cambió a
  `hilt-lifecycle-viewmodel-compose`.
- **El texto «Necesita abono» no activaba el interruptor**: fila completa con
  `Modifier.toggleable(role = Role.Switch)`.
- **Concordancia en el aviso** («Poda registrado»): cambiado a «Cuidado registrado: Poda».
- El emulador no incluye `sqlite3`, así que la rejilla se probó creando plantas desde el propio
  formulario; los pasos 3 y 4 se programaron juntos y se separaron después en dos commits,
  comprobando que el paso 3 compilaba por sí solo.

### Uso de la IA

- Claude explicó el plan de la fase 4 antes de empezar y, tras la aprobación, generó el código
  de cada paso, las pruebas automáticas y las pruebas en el emulador mediante `adb`.
- Detectó durante las pruebas los dos fallos de usabilidad y los corrigió antes del commit.
- Diagnosticó el problema del certificado SSL de Gradle.

## 2026-10-07 — Fase 5: autenticación con Firebase

Decisión: el enunciado admite Room y/o Firebase. Se compararon dos opciones para el login
(Firebase Authentication o una tabla de usuarios en Room con contraseñas cifradas) y se eligió
Firebase por ser la solución real, permitir recuperar la contraseña y demostrar el patrón
Repository con dos fuentes de datos.

Preparación (autor): proyecto `plantcare-cba07` en la consola de Firebase, app Android registrada,
proveedor de correo/contraseña activado y `google-services.json` en `app/`.

1. `631178a` Dependencias de Firebase (BoM 34.19.0, `firebase-auth`, plugin `google-services`).
2. `cd0b4e1` `AuthRepository` + `FirebaseAuthRepository` (`callbackFlow`, errores traducidos) y
   `FirebaseModule`.
3. `a5f65fd` Login con `LoginViewModel`, `AuthValidator` (3 pruebas unitarias) y campo de
   contraseña con mostrar/ocultar.
4. `fb6a9f8` Registro con confirmación de contraseña y diálogo de recuperación.
5. `70009cc` Sesión persistente (`SessionViewModel`), plantas filtradas por uid, migración de las
   plantas `"local"`, perfil con email y cierre de sesión. 3 pruebas nuevas con
   `FakeAuthRepository` (9/9 instrumentadas).

Pruebas reales (autor): registro, el Ficus creado antes del login aparece en la cuenta nueva,
email en Perfil, sesión recordada al reabrir, cerrar sesión, contraseña incorrecta y volver a
entrar. Todo correcto.

### Problemas encontrados

- Database Inspector vacío: la base de datos se crea al usarla por primera vez y el emulador tenía
  la pantalla apagada.
- `connectedDebugAndroidTest` desinstala la app al terminar y borró las plantas de prueba. Desde
  entonces las pruebas se lanzan con `adb shell am instrument`, que conserva los datos.
- En el script de pruebas con `uiautomator`, los textos con «?» fallaban porque es un carácter
  especial en las expresiones regulares (problema del script, no de la app).

### Uso de la IA

- Explicó la diferencia entre Firebase y Room para el login y la base de datos (dónde está el
  archivo, cómo verla con Database Inspector).
- Generó el código de la fase y lo probó en el emulador salvo el registro e inicio de sesión
  reales, que hizo el autor (la IA no introduce credenciales en servicios externos).
- Creó el guion del vídeo (`docs/guion-video.md`) a petición del autor.
