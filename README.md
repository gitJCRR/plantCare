# 🌱 PlantCare

Aplicación Android para el cuidado y mantenimiento de plantas: registra tus plantas, recibe
recordatorios de riego y abono, lleva un historial de cuidados y comprueba si un lugar
tiene la luz adecuada.

Práctica final de **Aplicaciones para dispositivos móviles** (Grado en Ingeniería Informática).

## Tecnologías

- Kotlin + Jetpack Compose + Material 3
- Navigation Compose
- Arquitectura MVVM (UI → ViewModel → Repository → Fuente de datos)
- Hilt (inyección de dependencias)
- Room (persistencia local) y Firebase Authentication (login/registro)
- Funcionalidades avanzadas: cámara, sensor de luz, notificaciones, gráficos

## Pantallas previstas

| Pantalla | Descripción |
|---|---|
| Login / Registro | Acceso con Firebase Auth |
| Inicio | Rejilla con mis plantas y las que toca regar |
| Detalle | Información de la planta e historial de cuidados |
| Añadir / editar planta | Formulario con foto desde la cámara |
| Medidor de luz | Sensor de luz ambiental |
| Perfil / Ajustes | Recordatorios, tema, cerrar sesión |

## Estado

🚧 En desarrollo — ver el historial de commits para la evolución del proyecto.

## Cómo ejecutar

1. Clonar el repositorio y abrirlo con Android Studio.
2. Ejecutar la configuración `app` en un emulador o dispositivo (Android 8.0+ / API 26).
