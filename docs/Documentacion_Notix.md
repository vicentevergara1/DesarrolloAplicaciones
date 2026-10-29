# Documentación técnica — revisión del 29/10/2026

## Arquitectura y responsabilidades

La versión conserva la estructura del ZIP actualizado:

- `model`: usuarios, perfiles, trámites, documentos, antecedentes, estados, formatos e historial.
- `data/local`: entidades y DAO de Room, mapeos, seeder y copia privada de documentos.
- `data/validation`: módulo 11 del RUT y almacenamiento de contraseñas.
- `repository`: registro/login, reglas de creación, autorización por perfil/propietario y transiciones transaccionales.
- `viewmodel`: carga de datos, estado de pantalla, validación del formulario de solicitud, importación, guardado y errores.
- `ui`: pantallas Compose, eventos, modales, animaciones y lanzamiento del selector/visor Android.

La pantalla de registro mantiene la definición declarativa y las validaciones visuales del proyecto recibido. El repositorio comprueba además nombre, correo, contraseña y RUT antes de persistir. No hay llamadas a Room desde los cuerpos de las pantallas. Las fábricas construyen sus dependencias.

## Flujo de trámites

~~~text
Solicitante crea → ENVIADO → Notario revisa → FINALIZADO
                                ↓
                             DEVUELTO
                                ↓
                 Solicitante corrige y reenvía → ENVIADO
~~~

Solo una cuenta Persona/Empresa puede enviar sus propios trámites. Solo un Notario puede evaluar un ENVIADO. Una devolución exige un motivo. La aprobación exige confirmar la legibilidad. El repositorio vuelve a consultar el estado guardado; un botón pulsado dos veces o una pantalla antigua no puede alterar un trámite ya evaluado.

La corrección reutiliza el ID, conserva los eventos anteriores, sustituye documentos sin duplicarlos, limpia la observación vigente, registra un nuevo evento y crea un aviso. Cambiar estado, agregar el evento y crear el aviso forman una única transacción Room.

La firma se representa por una confirmación explícita, su tipo simulado y el evento de envío. No se captura una firma real ni se implementa criptografía de firma notarial. La biometría de la pantalla de acceso sigue siendo una simulación y exige las credenciales del perfil seleccionado.

## Persistencia

Base `notix_database`, versión **3**. Tablas: `usuarios`, `tramites`, `documentos`, `historial_eventos`, `avisos`. Las claves foráneas de documentos/historial apuntan al trámite. Los IDs de nuevos trámites y avisos usan UUID; un ID repetido no reemplaza la fila existente.

La migración **2 → 3** añade `rutaLocal` y `mime` a documentos y `firmaConfirmada` a trámites. No borra usuarios, documentos, historial, avisos ni trámites. El esquema 2 se reconstruyó y contrastó con el DDL generado por Room desde el ZIP original; el esquema 3 se exporta durante KSP. No se configura migración destructiva. No se afirma compatibilidad con esquemas históricos distintos a la versión 2 de este ZIP.

Las contraseñas nuevas se guardan como PBKDF2-HMAC-SHA1, 210.000 iteraciones, sal aleatoria de 16 bytes y derivación de 256 bits, compatible con API 24. Una clave en texto plano de la versión 2 se convierte después de un acceso correcto. El usuario entregado a la UI no incluye esa clave. El seeder crea los usuarios demo solo si no existen y no cambia sus contraseñas en cada apertura.

Los adjuntos nuevos se copian al directorio privado `files/adjuntos`. Se validan MIME, tamaño informado y bytes realmente leídos, con máximo 5 MB; se rechazan archivos vacíos. FileProvider concede acceso temporal de lectura únicamente al archivo abierto. No se necesitan permisos globales de almacenamiento. Los adjuntos de la versión 2 siguen siendo metadatos: no es posible reconstruir archivos que nunca fueron guardados. La app explica este caso al intentar abrirlos.

Las copias nuevas reemplazadas o abandonadas se eliminan; los archivos originales del selector no se modifican. La app deshabilita backup y excluye base, archivos y preferencias de extracción.

## Sesión y navegación

`SesionViewModel` conserva la cuenta y los ViewModels de sus pantallas durante cambios de configuración. Cerrar sesión destruye ese estado y los formularios de la cuenta. La sesión no se escribe en Room ni se restaura tras finalizar el proceso.

El catálogo guarda el identificador de formato para reconstruirlo al rotar. Cada solicitud usa una clave de ViewModel distinta, por lo que un envío exitoso no impide crear otro trámite del mismo formato. Perfil permite cerrar sesión tanto a Persona como a Empresa; Notario tiene su acción en la barra superior.

## Defensa y límites

- Room emite `Flow` y Compose observa el estado del ViewModel; al cambiar la base se actualizan listados y avisos.
- Las transacciones impiden guardar un estado nuevo sin su historial o aviso correspondiente.
- El RUT usa módulo 11: la K corresponde solo al resultado 10, nunca es un verificador universal.
- La copia privada conserva un adjunto después de perder el permiso temporal del selector.
- Los avisos son internos, no notificaciones del sistema. El recurso nativo exigido se cubre mediante el selector y visor de archivos.
- El catálogo contiene tres formatos ficticios. El prototipo HTML y su pago/agendamiento no forman parte de la aplicación Android.
- No hay backend: ambas cuentas operan sobre la base del mismo dispositivo. No se enviaron documentos ni credenciales a servicios externos.
- Trello, mockup, video, presentación técnica y cumplimiento del plazo deben ser aportados por el equipo.
