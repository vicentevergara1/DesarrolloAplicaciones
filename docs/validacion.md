# Validación de la versión corregida

Ejecutada el **27 de octubre de 2026** con JDK 21, Gradle 9.4.1, AGP 9.2.1 y Android SDK 37.0.

## Comando

Desde Notix/:

~~~sh
./gradlew --console=plain --max-workers=2 :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
~~~

## Resultados

- **BUILD SUCCESSFUL**: APK de depuración generado.
- **37 pruebas ejecutadas, 37 correctas, 0 omitidas y 0 errores.**
- Lint sin errores. Quedan tres advertencias: target SDK 36 frente al más reciente y versiones más nuevas de Compose Compiler y coroutines-test. Las versiones usadas están fijadas y compiladas.
- Las pruebas de interfaz, SQLite y archivos se ejecutaron con Robolectric/API 28. No se ejecutó en un teléfono físico.

| Grupo | Ejecutadas | Fallidas |
|---|---:|---:|
| AndroidDocumentStoreTest | 3 | 0 |
| AuthValidationTest | 5 | 0 |
| AuthViewModelTest | 5 | 0 |
| AuthenticationFlowTest | 3 | 0 |
| DatabaseMigrationTest | 1 | 0 |
| LocalPersistenceTest | 3 | 0 |
| ProcedureWorkflowTest | 4 | 0 |
| ProceduresViewModelTest | 6 | 0 |
| RegistrationRulesTest | 6 | 0 |
| StartupTest | 1 | 0 |

## Funciones verificadas

- Registro por perfiles, ejemplos sintéticos, RUT y rechazo de contraseñas en blanco.
- Acceso, correos duplicados, sal aleatoria, derivación de contraseñas y limpieza de la sesión.
- Persistencia de cuentas, perfiles, documentos e historial al reabrir la base.
- Migración real desde el esquema de Room versión 1 a la versión 2, con cuentas y borradores conservados.
- Restricciones de propietario y rol; operaciones incompatibles con el estado son rechazadas.
- Ciclo completo: borrador, envío, observación del notario, corrección, reenvío y finalización.
- Selector de documentos de Android: acción y tipos solicitados, cancelación sin alterar el borrador.
- Lectura de un proveedor de documentos, copia privada y eliminación de la copia sin tocar el original.
- Rechazo de archivos vacíos, tipos no admitidos y archivos que exceden 5 MB aunque el proveedor no declare el tamaño.
- Inicio de MainActivity con NotixApplication y la base real, además del recorrido de Compose con repositorios de Room.

Las capturas actuales se generan en Notix/app/build/reports/ui/tramite-finalizado.png. Los reportes se generan en Notix/app/build/reports/tests/ y Notix/app/build/test-results/.

El equipo debe ejecutar además los casos manuales de docs/pruebas.md en su emulador o teléfono y grabar el video. Trello, el mockup y el conocimiento del código para la defensa son evidencias del equipo.
