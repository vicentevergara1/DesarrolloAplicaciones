> Informe histórico de la versión anterior. Las reglas actuales de RUT y solicitud están en [validacion-texto-rut.md](validacion-texto-rut.md).

# Verificación — 29/10/2026

## Base revisada

Se corrigió el ZIP actualizado `caso-NotariaX-main.zip` enviado el 29/10/2026. SHA-256 del original:

~~~text
ec0a5098a308f30f1760f74ee000930a519e0d74adeff13a0ce6386fad5b8432
~~~

La versión original también compiló y pasó su prueba aritmética de ejemplo. Sus problemas confirmados eran funcionales: adjuntos reducidos a nombres y tamaños inventados, ausencia de corrección/reenvío, cuentas demo sobrescritas al iniciar, ausencia de cierre de sesión para solicitantes, formularios reutilizados por formato, cambios de estado sin una transacción completa y posibilidad de evaluar estados finales. También había un ejemplo de RUT empresarial con verificador incorrecto. La configuración de SDK 37.1 generaba una advertencia de soporte de AGP; la corrección usa SDK 37.0 y KSP 2.3.12.

El primer commit de esta rama registra la importación del ZIP sin cachés ni configuración personal del IDE; el siguiente registra las correcciones. Se conserva el prototipo HTML del equipo y no se modifica la rama principal.

## Resultado reproducible

Ejecutado con JDK 21 y Android SDK 37.0 / Build Tools 36.0.0:

~~~sh
cd Notix
./gradlew --console=plain --max-workers=2 :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
~~~

**BUILD SUCCESSFUL. 25 pruebas, 0 fallos, 0 errores, 0 omitidas.**

| Clase | Pruebas | Cobertura principal |
|---|---:|---|
| RutValidatorTest | 4 | Verificador numérico, 0, K/k, entrada inválida y cursor |
| RepositoryTest | 9 | Login, hash, clave antigua, duplicados, transiciones, propietarios, firma y avisos |
| DocumentosLocalesTest | 3 | Copia privada, eliminación, límite real de 5 MB, vacío y MIME |
| CrearTramiteViewModelTest | 4 | RUT al editar, archivo obligatorio, envío único, solicitud nueva y descarte |
| MigrationTest | 1 | Migración real de esquema 2 a 3 sin perder datos |
| DatabaseSeederTest | 1 | Reiniciar no sobrescribe cuentas ni duplica casos |
| NavigationTest | 3 | Persona → logout → Notario, corrección/selector/rotación y error de RUT en registro |

Las pruebas Android/UI se ejecutaron con Robolectric API 28, gráficos nativos para Compose y Room real. La prueba del selector verifica la intención `ACTION_OPEN_DOCUMENT` y los tipos permitidos; la copia se prueba además con un ContentProvider de archivos. No se ejecutaron pruebas en teléfono ni emulador Android completo.

**Lint: 0 errores, 14 advertencias y 2 sugerencias.** Son advertencias sobre targetSdk 36, etiqueta redundante del manifiesto, versiones más recientes disponibles, recursos de plantilla sin uso y preferencia por declarar dependencias en el catálogo. Las sugerencias recomiendan estado especializado para enteros. No se ocultaron diagnósticos ni se desactivaron pruebas.

Los reportes se generan en `Notix/app/build/reports/tests/testDebugUnitTest/` y `Notix/app/build/reports/lint-results-debug.html`; se excluyen del ZIP fuente junto con cachés, APKs y `local.properties`.

## Comprobación manual y guion del video

1. Abrir `Notix/` en Android Studio, sincronizar con JDK 21, ejecutar `app` e ingresar con la cuenta Persona del README.
2. Abrir Perfil y verificar que se puede cerrar sesión. Ingresar como Notario, cerrar sesión y volver a Persona; las cuentas no deben mezclar formularios ni datos de perfil.
3. Crear una cuenta Empresa ficticia. Usar RUT empresarial `76.111.111-6`; la razón social se precarga al crear una solicitud de empresa.
4. Crear un trámite nuevo, completar antecedentes ficticios y adjuntar `docs/documento-ficticio.txt` (copiar previamente a Descargas del emulador). Sin archivo o sin confirmar la firma simulada, el envío debe mostrar un error.
5. Confirmar y enviar una sola vez. Crear otro trámite del mismo formato: debe abrir un formulario nuevo, sin reutilizar el éxito ni el adjunto anterior.
6. Cerrar sesión e ingresar como Notario en el mismo dispositivo. Abrir el documento, confirmar su legibilidad y devolver con un motivo. No debe permitir evaluar de nuevo el caso devuelto.
7. Volver al solicitante, abrir el trámite observado y pulsar **Corregir y reenviar**. Cambiar antecedentes y, si corresponde, reemplazar el archivo por otro ficticio. Confirmar firma simulada y reenviar.
8. Volver al Notario, revisar y aprobar. Verificar el estado final, el historial y los avisos al volver al solicitante. Un trámite finalizado no debe aceptar correcciones ni otra evaluación.
9. Forzar el cierre y volver a abrir: las cuentas y los trámites deben seguir guardados; se debe iniciar sesión de nuevo. Abrir un adjunto nuevo debe seguir funcionando.
10. Rotar durante una solicitud: debe mantenerse el formato y el ViewModel del formulario. Cancelar el selector no debe borrar el adjunto ya seleccionado.
11. En registro o solicitud, probar `22.312.422-1`, luego `22.312.422-K`: ambos deben indicar que corresponde **4**. `22.312.422-4` debe quitar ese error.
12. Probar archivo vacío, tipo no permitido, más de 5 MB y cancelar el selector. La app debe explicar el problema y seguir funcionando.

## Límites y entrega

Los datos demo de la versión anterior no contenían los bytes de sus documentos: al abrirlos se informa que son solo metadatos. Para corregir y reenviar esos casos, adjuntar un archivo ficticio nuevo. Los documentos importados en esta revisión sí se conservan.

Las firmas y biometría son simuladas; los avisos están dentro de la aplicación. No hay backend, sincronización entre dispositivos, pago ni agendamiento Android. Los tres tipos de cuenta y el ciclo de revisión son locales. La migración soportada es la versión 2 del ZIP recibido a la nueva versión 3.

Antes de entregar, el equipo debe probar en su emulador o teléfono, preparar el video, mantener Trello/mockup y comprobar que el repositorio y la rama que clonará el docente contienen estos cambios. Estas pruebas no certifican las evidencias externas ni una nota de evaluación.
