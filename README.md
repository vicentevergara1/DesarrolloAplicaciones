# Notix — NotariaX, DSY1105

Aplicación académica nativa Android en Kotlin, Compose y MVVM. Esta revisión parte del ZIP actualizado enviado el 29/10/2026 y conserva sus pantallas, catálogo, perfiles y datos de demostración. El prototipo HTML se mantiene como referencia visual.

**Solo datos ficticios.** Los trámites, firmas, biometría y revisión notarial son simulaciones locales sin validez legal. No existe un servidor ni sincronización entre dispositivos: la demostración de solicitante y notario se realiza en la misma instalación, cambiando de cuenta.

## Abrir en Android Studio

1. Extraer el ZIP en una carpeta nueva.
2. Abrir **la carpeta `Notix/`** que contiene `settings.gradle.kts`.
3. Configurar **Gradle JDK 21** en Settings → Build, Execution, Deployment → Build Tools → Gradle.
4. Instalar Android SDK Platform **37.0**, Build Tools **36.0.0** y Platform Tools desde SDK Manager.
5. Sincronizar Gradle, seleccionar `app` y un emulador o teléfono API 24 o superior, y pulsar Run ▶.

En Windows se puede usar `gradlew.bat`; en macOS/Linux:

~~~sh
cd Notix
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
~~~

La primera compilación requiere conexión para descargar dependencias. El APK de prueba queda en `Notix/app/build/outputs/apk/debug/app-debug.apk`.

## Cuentas ficticias de demostración

Se crean automáticamente al abrir la aplicación por primera vez. Son credenciales públicas de prueba.

| Perfil | Correo | Contraseña de prueba |
|---|---|---|
| Persona | ana.demo@notariax.test | demo123 |
| Notario | notario.demo@notariax.test | notario123 |

Para Empresa, registrar una cuenta ficticia. El ejemplo de RUT de empresa **76.111.111-6** tiene verificador numérico; también se aceptan verificadores 0 y K cuando corresponden. Para el ejemplo **22.312.422**, el verificador correcto es **4**.

## Funciones y correcciones

- Registro y login por perfil con Room, correo normalizado y prevención de envíos repetidos. Las contraseñas nuevas se derivan con PBKDF2 y sal aleatoria; las antiguas se actualizan al ingresar correctamente. Las cuentas demo no se sobrescriben al iniciar.
- Catálogo y solicitudes con antecedentes validados y confirmación explícita de firma simulada. No se inventan adjuntos ni tamaños.
- Selector nativo para PDF, TXT, PNG y JPEG ficticios, máximo 5 MB. El archivo se copia al almacenamiento privado y puede abrirse desde el detalle o la revisión mediante FileProvider.
- Bandeja del notario: devolver con un motivo obligatorio o aprobar tras confirmar la legibilidad. Los trámites devueltos y finalizados no pueden evaluarse de nuevo sin reenvío.
- Corrección y reenvío del mismo trámite por su solicitante, conservando historial y generando avisos.
- Estado, historial y aviso se guardan en una transacción. Los IDs evitan colisiones y los reenvíos no duplican documentos.
- Perfil y cierre de sesión para Persona/Empresa; navegación atrás y selección de formato conservada al rotar. La sesión queda en memoria y se elimina al cerrar el proceso.
- Migración Room **2 → 3** sin borrado de datos. Los adjuntos antiguos que solo contenían metadatos se conservan y se avisa al abrirlos; se deben reemplazar al corregir un trámite.

## Documentación

- [Guía técnica actualizada](docs/Documentacion_Notix.md)
- [Verificación y pruebas manuales](docs/validacion-29-octubre.md)
- [Contexto del caso](docs/contexto.md)
- [Plan original de implementación](docs/Guia_implementacion_Notix.md), documento histórico

El equipo debe aportar el mockup y el uso de Trello, el video y la evidencia de colaboración. El repositorio y la rama evaluados deben contener esta versión antes de la entrega.
