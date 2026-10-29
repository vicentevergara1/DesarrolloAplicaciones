# Recordar sesión — 29/10/2026

## Comportamiento

La pantalla de acceso incluye una casilla **Recordar sesión** debajo del correo y la contraseña, desmarcada inicialmente. Su estado sobrevive a la rotación mientras se está en el formulario y se bloquea durante el acceso.

Tras validar correctamente correo, contraseña y perfil, `LoginViewModel` guarda la preferencia. Si está marcada, se persiste únicamente el identificador de la cuenta en preferencias privadas. Si está desmarcada, se elimina cualquier acceso recordado anterior. Un intento incorrecto no guarda datos de sesión.

En una apertura nueva, la app consulta el usuario guardado en Room y abre **Mis trámites** para Persona/Empresa o **Bandeja del Notario** para Notario. No exige escribir otra vez las credenciales. La cuenta debe seguir existiendo; si falta, elimina el identificador y permite ingresar normalmente. El perfil se lee de Room, no de un valor de rol almacenado en preferencias.

**Cerrar sesión** elimina la preferencia antes de borrar el estado de las pantallas y volver al formulario de acceso. La sesión actual sobrevive a rotaciones aunque no se haya marcado la casilla; al finalizar el proceso, solo se recupera si fue recordada.

No se guarda ni se rellena la contraseña. No se modifica el esquema Room, que sigue en versión 4; sus migraciones y datos anteriores se conservan. Las preferencias ya están excluidas de backup y transferencia por el manifiesto y las reglas existentes.

Se conservan las solicitudes con texto y respaldo opcional, el RUT ficticio validado por formato y el código sin comentarios.

## Verificación

~~~sh
cd Notix
./gradlew --no-daemon --console=plain --max-workers=2 :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
~~~

**BUILD SUCCESSFUL: 42 pruebas, 0 fallos, 0 errores y 0 omitidas.**

| Clase | Pruebas |
|---|---:|
| CrearTramiteViewModelTest | 6 |
| DatabaseSeederTest | 1 |
| DocumentosLocalesTest | 3 |
| MigrationTest | 2 |
| NavigationTest | 8 |
| RepositoryTest | 12 |
| RutValidatorTest | 4 |
| SesionRepositoryTest | 6 |

**Lint: 0 errores, 16 advertencias y 2 sugerencias.** Se conservan las advertencias anteriores y se añaden dos sugerencias de estilo sobre usar la extensión KTX para editar preferencias. Se usa la API directa para comprobar el resultado de `commit()`, ejecutado en `Dispatchers.IO`, antes de confirmar acceso o cierre. No se desactivaron comprobaciones ni se omitieron pruebas.

El APK debug se verificó con `apksigner`. Se comprobó además que los archivos de código siguen sin comentarios.

Las pruebas de persistencia reabren tanto la base en disco como el repositorio. Comprueban los tres perfiles, cuenta inexistente, eliminación al salir, acceso sin recordar y ausencia de correo/contraseña en preferencias. Las pruebas de interfaz cubren la casilla, acceso correcto e incorrecto y recuperación tras destruir el estado en memoria de la actividad, incluyendo el regreso al formulario después de cerrar sesión.

Las pruebas Android/UI usan Robolectric API 28 y un dispatcher de efectos controlado. No se afirma haber ejecutado un emulador completo ni un teléfono físico. Los reportes se generan en `Notix/app/build/reports/` y se excluyen del ZIP fuente.

## Prueba manual

1. Abrir el proyecto `Notix/` en Android Studio con JDK 21 y ejecutar `app`.
2. Iniciar sesión con una cuenta ficticia del README y marcar **Recordar sesión** antes de pulsar **Ingresar**.
3. Cerrar la app y volver a abrirla, o forzar su detención desde Ajustes de Android. Debe entrar a la cuenta sin pedir credenciales.
4. Verificar Persona/Empresa en **Mis trámites** y Notario en su bandeja.
5. Cerrar sesión, cerrar la app y abrirla nuevamente: debe pedir acceso otra vez.
6. Ingresar sin marcar la casilla. Cerrar el proceso y abrir nuevamente: debe pedir acceso.
7. Marcar la casilla e ingresar una contraseña incorrecta: debe mostrar el error y no conservar una sesión.
8. Rotar el dispositivo con la casilla marcada antes de ingresar: debe conservar la selección.

Solo deben usarse datos ficticios en esta aplicación académica local.
