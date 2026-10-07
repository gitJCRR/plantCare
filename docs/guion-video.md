# Guion del vídeo — PlantCare

Documento vivo: se amplía al terminar cada fase con lo que conviene enseñar.

## Requisitos del enunciado para el vídeo

- Duración **máxima de 15 minutos**.
- Mostrar la aplicación funcionando (emulador o móvil real).
- Explicar la **estructura del proyecto**: clases más relevantes, vistas implementadas, código a destacar.
- Deben intervenir **todos los integrantes** (práctica individual: solo el autor).
- Subirlo a **YouTube con visibilidad «Oculto»** y poner el enlace en la memoria.

Criterio de la rúbrica (1 punto): *«Muestra claramente todas las funcionalidades relevantes y explica la
estructura del código»*.

## Reparto del tiempo (objetivo ≈ 13 min, margen de 2)

| Bloque | Duración | Contenido |
|---|---|---|
| 1. Presentación | 0:30 | Quién soy, qué es PlantCare y qué problema resuelve |
| 2. Demo de la app | 6:00 | Recorrido completo por las funcionalidades |
| 3. Estructura y arquitectura | 2:00 | Paquetes y capas UI → ViewModel → Repository → datos |
| 4. Código destacado | 4:00 (≈ 25 s por archivo) | 5-6 fragmentos que justifican los requisitos |
| 5. Base de datos y Firebase | 1:00 | Database Inspector y consola de Firebase |
| 6. Cierre | 0:30 | Puntos fuertes, qué mejoraría y uso de la IA |

---

## Preparación antes de grabar

- [ ] Emulador encendido **dentro de Android Studio** y la app recién instalada con ▶️.
- [ ] Cuenta de prueba creada en Firebase (y una **segunda cuenta** para enseñar que cada usuario ve sus plantas).
- [ ] 3-4 plantas con datos variados y **foto**: alguna que **«toca regar»** (ya existe «Albahaca» con el riego atrasado), alguna con abono, distintos niveles de luz.
- [ ] Varios cuidados registrados para que el historial y el **gráfico** no estén vacíos.
- [ ] Quitar los permisos de cámara y notificaciones antes de grabar para enseñar cómo se piden:
      `adb shell pm revoke com.tareaandroid.plantcare android.permission.CAMERA` (y lo mismo con `POST_NOTIFICATIONS`),
      o desde *Ajustes → Apps → PlantCare → Permisos*.
- [ ] Abrir *Extended controls → Virtual sensors → Light* del emulador para cambiar la luz en directo.
- [ ] Pestañas abiertas en Android Studio con los archivos del bloque 4, en orden.
- [ ] Consola de Firebase abierta en *Authentication → Usuarios*.
- [ ] Notificaciones del PC silenciadas y Norton en modo silencioso.
- [ ] Probar el micro; grabar a 1080p (OBS o la grabadora de Windows `Win + Alt + R`).

---

## 1. Presentación (0:30)

> «Hola, soy Juan Carlos Ros Robles. Esta es PlantCare, mi práctica final de Aplicaciones para
> dispositivos móviles. Es una app para el cuidado de plantas: registras tus plantas, te dice
> cuándo toca regarlas o abonarlas, guarda el historial de cuidados y tiene un medidor de luz con
> el sensor del móvil. Primero enseño la app y después cómo está construida por dentro.»

## 2. Demo de la app (5:30)

| # | Acción en pantalla | Qué decir (idea) | Requisito que se demuestra |
|---|---|---|---|
| 2.1 | Abrir la app sin sesión → pantalla de login | «Si no hay sesión, la app empieza en el login» | Pantalla de login |
| 2.2 | Pulsar *Entrar* vacío; escribir un email mal | «Valido los campos antes de llamar a Firebase» | Validación, estado |
| 2.3 | Ir a *Regístrate*; contraseñas distintas → error; crear cuenta | «El registro usa Firebase Authentication» | Pantalla de registro, Firebase |
| 2.4 | *«¿Has olvidado tu contraseña?»* → diálogo | «Envía un correo de recuperación» | Extra |
| 2.5 | Inicio vacío → *Añadir planta* | «Estado vacío con instrucciones» | Pantalla de inicio |
| 2.6 | Formulario: guardar sin nombre → error; rellenar (nombre, especie, riego, abono, luz) y guardar | «Formulario validado; el abono es opcional» | Formulario, estado |
| 2.7 | Rejilla con varias plantas; señalar «Toca regar» y el orden | «Se ordenan por próximo riego» | Lista `LazyVerticalGrid` |
| 2.8 | Abrir una planta → detalle | «Al detalle le paso el id de la planta» | Detalle, **paso de parámetros** |
| 2.9 | Pulsar *Regar* y *Podar* → aviso y historial | «Cada cuidado se guarda en el historial» | `LazyColumn`, Room |
| 2.10 | *Editar* → cambiar riego → guardar | «La rejilla se actualiza sola» | Flujo reactivo |
| 2.11 | *Borrar* → diálogo → cancelar / confirmar | «Pide confirmación; borra también el historial» | Operaciones completas |
| 2.12 | Cerrar la app y abrirla | «La sesión y los datos se conservan» | Persistencia |
| 2.13 | *Perfil* → email → *Cerrar sesión* | «Al cerrar sesión vuelve al login y no se puede volver atrás» | Perfil, navegación |
| 2.14 | Entrar con la **segunda cuenta** → lista distinta | «Cada usuario solo ve sus plantas» | Firebase + Room |
| 2.15 | Girar el emulador / emulador de tablet | «La rejilla añade columnas y la barra pasa a rail lateral» | Diseño adaptativo |
| 2.16 | Inicio: tarjeta «Activa los recordatorios» → *Activar* → diálogo del sistema → permitir | «El permiso de notificaciones se pide explicando para qué sirve» | **Permisos** |
| 2.17 | *Perfil → Probar recordatorio ahora* → bajar la barra de notificaciones | «Cada día a las 9:00 WorkManager comprueba qué plantas toca regar» | Notificaciones |
| 2.18 | Editar una planta → *Hacer foto* → denegar → volver a pulsar → explicación → permitir → foto | «Si deniego el permiso, la app explica para qué lo necesita; si lo deniego para siempre, ofrece abrir los ajustes» | Cámara, **permisos** |
| 2.19 | Guardar → foto en la tarjeta y en el detalle; enseñar también *Galería* | «También puedo elegir una foto sin dar ningún permiso» | Cámara |
| 2.20 | Pestaña *Luz*; en *Extended controls* mover el sensor de luz (20 → 2500 → 30 000 lux) | «Mide la luz y me dice qué plantas estarían bien en este sitio» | **Sensor**, pantalla avanzada |
| 2.21 | Detalle de una planta → bajar hasta el gráfico | «Cuidados de los últimos 6 meses, dibujado con Canvas» | Gráficos |

## 3. Estructura y arquitectura (2:00)

Mostrar el árbol de paquetes en Android Studio (vista *Android*) y el diagrama de capas de la memoria
(apartado 2.2).

> «Sigo la arquitectura vista en clase: UI, ViewModel, Repository y fuente de datos.»

| Paquete | Qué contiene | Qué decir |
|---|---|---|
| `ui/` | Una carpeta por pantalla con `Screen` + `ViewModel` | «Cada pantalla está separada; la vista no tiene lógica» |
| `navigation/` | Rutas, `NavHost`, `SessionViewModel` | «7 destinos con rutas type-safe» |
| `model/` | `Plant`, `CareEvent`, `User` | «Modelo independiente de Room y Firebase» |
| `data/local/` | Room: entidades, DAO, base de datos | «Persistencia local» |
| `data/auth/` | `AuthRepository`, `FirebaseAuthRepository` | «Autenticación» |
| `data/repository/` | `PlantRepository` | «Única puerta de acceso a los datos» |
| `di/` | Módulos de Hilt | «Inyección de dependencias» |

## 4. Código destacado (4:00)

Abrir cada archivo y señalar las líneas clave (≈ 40 s cada uno).

| # | Archivo | Qué señalar | Requisito |
|---|---|---|---|
| 4.1 | `navigation/Routes.kt` + `PlantCareNavHost.kt` | Rutas `@Serializable`, `PlantDetailRoute(plantId)`, `NavigationSuiteScaffold`, destino inicial según la sesión | Navigation ≥ 5, parámetros, adaptativo |
| 4.2 | `ui/home/HomeScreen.kt` + `HomeViewModel.kt` | `HomeScreen` (con ViewModel) vs `HomeContent` (sin estado); `StateFlow` + `collectAsStateWithLifecycle`; `LazyVerticalGrid(GridCells.Adaptive)` | Compose, estado, lista, adaptativo |
| 4.3 | `ui/edit/PlantEditViewModel.kt` | `PlantEditUiState` + `PlantEditEvent`; `savedStateHandle.toRoute<PlantEditRoute>()`; validación | MVVM, estado, parámetros |
| 4.4 | `data/repository/PlantRepositoryImpl.kt` | `flatMapLatest` por usuario; transacción `withTransaction` al registrar un cuidado | Repository |
| 4.5 | `data/auth/FirebaseAuthRepository.kt` | `callbackFlow` con el `AuthStateListener`; traducción de errores | Firebase, Repository |
| 4.6 | `di/RepositoryModule.kt` + `DatabaseModule.kt` | `@Binds` interfaz → implementación; `@Provides @Singleton` | Inyección de dependencias |
| 4.7 | `data/sensor/AndroidLightSensor.kt` | `callbackFlow` + `awaitClose` (el sensor se apaga al salir) | Sensor |
| 4.8 | `ui/edit/PlantPhotoSection.kt` | Los tres casos del permiso: concedido, explicación, ajustes; `TakePicture` + `FileProvider` | **Permisos**, cámara |
| 4.9 | `notifications/WateringReminderWorker.kt` + `PlantCareApp.kt` | `@HiltWorker`, `HiltWorkerFactory`, trabajo periódico | Notificaciones, inyección de dependencias |
| 4.10 | `ui/detail/CareChart.kt` | Barras dibujadas con `Canvas` | Gráficos |

## 5. Base de datos y Firebase (1:00)

1. Android Studio → **App Inspection → Database Inspector** → tabla `plants`: señalar `userId`
   (uid de Firebase) y las fechas como número de días.
2. Activar *Live updates*, regar una planta en el emulador y ver aparecer la fila en `care_events`.
3. Abrir `app/schemas/.../1.json` o el diagrama de la memoria: relación 1:N con borrado en cascada.
4. Consola de Firebase → *Authentication → Usuarios*: las cuentas creadas y su UID (el mismo que
   aparece en `plants.userId`).

## 6. Cierre (0:30)

> «Como puntos fuertes destacaría la arquitectura por capas con pruebas automáticas y que cada
> usuario tiene sus propios datos. Como mejora futura, sincronizaría las plantas en la nube. Para el
> desarrollo he usado un asistente de IA, Claude, como explico en la memoria: me ayudó a planificar,
> generar y probar el código, y yo revisé y probé cada paso. Gracias.»

*(Ajustar al final con los puntos fuertes/débiles reales del apartado 7 de la memoria.)*

---

## Notas por fase

- **Fase 4:** enseñar que la rejilla se reordena sola al editar el riego (flujo reactivo de Room).
- **Fase 5:** enseñar dos cuentas distintas y la sesión recordada al reabrir la app. Mostrar la consola
  de Firebase con los usuarios.
- **Fase 7:** es la parte más vistosa; dedicarle ≈ 2 minutos de la demo. Enseñar **los permisos
  denegados y concedidos** (requisito obligatorio) y cambiar la luz del sensor en directo. Mencionar
  que son 4 funcionalidades avanzadas cuando se piden 2.
