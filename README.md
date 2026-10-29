# Notix — MVP de trámites notariales digitales

Aplicación Android académica para el caso **NotariaX** (DSY1105, Duoc UC). Permite registrar usuarios, crear trámites notariales simulados, adjuntar un documento, enviarlos al notario, revisarlos y consultar su estado con historial y avisos.

> ⚠️ Proyecto académico con **datos ficticios**. No realiza trámites notariales reales, no usa firmas reales, biometría real ni servicios de NotariaX.

## Documentación

- **[Documentación técnica y estado del proyecto](docs/Documentacion_Notix.md)** → arquitectura, persistencia Room, funcionalidades listas, pendientes y checklist de pruebas.
- [Contexto del caso](docs/contexto.md) → alcance y requisitos originales.
- [Guía de implementación](docs/Guia_implementacion_Notix.md) → plan por etapas (documento histórico).

## Estado rápido

| Área | Estado |
|---|---|
| Login / registro Persona, Empresa y Notario | ✅ |
| Catálogo y creación de trámites | ✅ |
| Persistencia con Room | ✅ |
| Bandeja del notario, aprobar y devolver | ✅ |
| Historial y avisos en base de datos | ✅ |
| Firma simulada real | ⚠️ Pendiente (solo declaración) |
| Corregir y reenviar tras devolución | ❌ Pendiente |
| Notificaciones del sistema | ❌ Pendiente |
| Pruebas unitarias/UI | ⚠️ Solo de ejemplo |

## Persistencia

La app usa **Room** (`notix_database`) como fuente de verdad. Se guardan:

- Usuarios
- Trámites
- Documentos (nombre, tamaño y estado; **no el archivo**)
- Historial de cambios de estado
- Avisos y su estado de lectura

Los datos **sobreviven al cierre de la app**. La sesión iniciada **no** se conserva. Los cambios de esquema usan `fallbackToDestructiveMigration()`, por lo que una nueva versión de la base puede borrar los datos.

Todos los detalles están en [docs/Documentacion_Notix.md](docs/Documentacion_Notix.md#6-persistencia-de-datos-room).

## Cómo ejecutar

Requisitos: Android Studio, JDK 21, SDK Android 37 y un emulador/dispositivo API 24+.

```bash
cd Notix
chmod +x gradlew        # si hace falta
./gradlew assembleDebug
```

O abrir la carpeta `Notix/` en Android Studio y ejecutar `app`.

## Cuentas demo

| Perfil | Correo | Contraseña |
|---|---|---|
| Persona | `ana.demo@notariax.test` | `demo123` |
| Notario | `notario.demo@notariax.test` | `notario123` |

También se puede crear una cuenta desde la pantalla de registro.

## Estructura

```text
caso-NotariaX/
├── docs/          # Contexto, guía y documentación técnica
├── index.html     # Prototipo web legado (referencia)
├── views/         # Pantallas HTML del prototipo legado
└── Notix/         # Proyecto Android (entrega principal)
    └── app/src/main/java/com/duoc/notix/
        ├── data/local/     # Room
        ├── repository/     # Acceso a datos
        ├── viewmodel/      # Estado de pantalla
        ├── ui/             # Compose
        └── model/          # Dominio
```

## Tecnologías

Kotlin · Jetpack Compose · Material 3 · Room · KSP · ViewModel · StateFlow · Coroutines.
