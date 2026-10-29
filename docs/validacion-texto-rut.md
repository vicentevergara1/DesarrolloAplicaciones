> Informe de la revisión de texto y RUT. Para la casilla y la persistencia de sesión, consultar [validacion-recordar-sesion.md](validacion-recordar-sesion.md).

# Revisión de texto, RUT y comentarios — 29/10/2026

## Base y alcance

Se usó la rama `main` de `Benja-ignacio/caso-NotariaX`, commit `e50128f2ccd2d6cc8bac923d9316f935fe238ded`, con autorización expresa del usuario. Los ZIP adjuntos superaban el límite de transferencia de 32 MB; no se afirma haber abierto esos archivos. Se conserva la estructura, pantallas y flujo de esa versión de GitHub.

## Cambios solicitados

- Se quitaron los comentarios del código Android, pruebas, archivos de configuración, scripts Gradle y prototipo HTML/CSS. Se conserva la directiva ejecutable `#!/bin/sh`; los avisos originales de licencia de los scripts están en `gradle-wrapper-notice.txt`.
- El RUT ficticio se valida por formato. Acepta 7 u 8 dígitos de cuerpo, sin cero inicial, y cualquier último carácter 0–9 o K/k, con o sin puntos y guion. No se calcula módulo 11. La regla se aplica al registro de todos los perfiles y al formulario de solicitud.
- Se agregó **Texto de la solicitud**, obligatorio de 10 a 5000 caracteres. Se conserva al rotar y aparece en el resumen antes de enviar, detalle del solicitante y revisión del notario.
- El archivo pasa a ser un **respaldo opcional**. Se permite crear y reenviar solo con texto, agregar un archivo o quitar un respaldo ya seleccionado. Los archivos que sí se adjuntan mantienen validación de MIME, máximo 5 MB y copia privada.
- Room usa versión 4 y migra desde las versiones 2 y 3. Los casos previos conservan sus cuentas, estados, historial, avisos y archivos; si no tenían texto, la UI lo indica y permite completarlo al corregir.

## Verificación

El comando reproducible desde `Notix/` es:

~~~sh
./gradlew --no-daemon --console=plain --max-workers=2 :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
~~~

**Resultado: BUILD SUCCESSFUL. 32 pruebas, 0 fallos, 0 errores y 0 omitidas.**

| Clase | Pruebas |
|---|---:|
| CrearTramiteViewModelTest | 6 |
| DatabaseSeederTest | 1 |
| DocumentosLocalesTest | 3 |
| MigrationTest | 2 |
| NavigationTest | 4 |
| RepositoryTest | 12 |
| RutValidatorTest | 4 |

**Lint: 0 errores, 14 advertencias y 2 sugerencias.** Las advertencias conservadas corresponden a targetSdk 36, versiones más recientes disponibles, recursos de plantilla sin uso, una etiqueta redundante del manifiesto y declaración de dependencias fuera del catálogo. Las sugerencias recomiendan estado especializado para enteros. No se desactivaron comprobaciones ni se omitieron pruebas. La prueba de interfaz usa un dispatcher de efectos controlado para ejecutar la recomposición en el hilo del entorno de pruebas.

Los reportes se generan en `Notix/app/build/reports/tests/testDebugUnitTest/` y `Notix/app/build/reports/lint-results-debug.html`. Se excluyen del ZIP fuente junto con cachés y configuración local. El APK debug se verificó con `apksigner`.

La comprobación de comentarios recorre los tipos de archivo de código y respeta literales, URLs y expresiones de cadenas Kotlin. No se eliminaron cadenas de la aplicación ni documentación explicativa. Los XML se comprobaron con un parser y el script POSIX con `sh -n`.

Las pruebas cubren todos los verificadores admitidos, entradas mal formadas, registro, solicitud con texto y sin documento, límites del texto, respaldo opcional, descarte, envíos repetidos, corrección/reenvío, autorización por perfil, conservación del texto al reabrir la base y visualización para el notario. Las pruebas Android/UI usan Robolectric API 28; no se ejecutaron en un emulador completo ni teléfono físico.

## Prueba manual

1. Abrir `Notix/` con JDK 21 y ejecutar `app` en Android Studio.
2. En registro de Persona, Empresa o Notario, probar un cuerpo de 7 u 8 dígitos con cada verificador 0–9 y K/k. Todos deben aceptarse por formato. Letras distintas de K, entradas incompletas y cuerpos no numéricos deben rechazarse o filtrarse al escribir.
3. Ingresar como Persona con la cuenta demo del README. Crear un Poder simple, escribir la solicitud y confirmar la firma simulada sin seleccionar un archivo. Revisar el texto en el modal y enviar.
4. Abrir el detalle: deben aparecer el texto completo y la indicación de que no hay documento de respaldo.
5. Cambiar a Notario en la misma instalación, abrir el trámite y comprobar el texto. Devolver con un motivo; regresar como solicitante, corregir el texto y reenviar sin documento.
6. Aprobar desde Notario después de confirmar la revisión. Comprobar estado, historial y avisos.
7. Repetir con un archivo ficticio. Probar **Quitar respaldo** antes de enviar y al corregir; comprobar que se puede continuar solo con texto y que el archivo original del dispositivo no se borra.
8. Probar texto vacío, solo espacios, menos de 10 y más de 5000 caracteres. No debe permitir enviar. Rotar antes de enviar debe conservar lo escrito.
9. Actualizar una instalación de la versión anterior, iniciar sesión y comprobar que los datos siguen presentes. Los trámites antiguos sin texto deben mostrar el aviso correspondiente.

El proyecto sigue siendo una simulación académica local. El video, Trello, pruebas en el dispositivo del equipo y la integración a la rama que evaluará el docente siguen a cargo del equipo.
