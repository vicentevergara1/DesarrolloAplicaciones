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

## Texto y documentos de respaldo

Cada solicitud nueva contiene texto obligatorio de 10 a 5000 caracteres. Se puede enviar sin documentos; si se selecciona un respaldo se mantienen las restricciones de tipo y tamaño. El solicitante puede quitarlo antes de enviar o al corregir. `textoSolicitud` vive en el modelo, entidad y mapeos, y el repositorio y ViewModel validan la misma regla mediante `SolicitudValidator`. El texto aparece en confirmación, detalle y revisión.

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

Base `notix_database`, versión **4**. Tablas: `usuarios`, `tramites`, `documentos`, `historial_eventos`, `avisos`. Las claves foráneas de documentos/historial apuntan al trámite. Los IDs de nuevos trámites y avisos usan UUID; un ID repetido no reemplaza la fila existente.

La migración **3 → 4** añade `textoSolicitud TEXT NOT NULL DEFAULT ''` a trámites. Conserva cuentas, estados, historial, avisos y respaldos; los casos anteriores quedan sin texto y la UI lo indica. Al corregirlos se exige completar el contenido. Se prueban la actualización desde versión 2 y desde versión 3, y el guardado del texto al reabrir la base.

La migración **2 → 3** añade `rutaLocal` y `mime` a documentos y `firmaConfirmada` a trámites. No borra usuarios, documentos, historial, avisos ni trámites. El esquema 2 se reconstruyó y contrastó con el DDL generado por Room desde el ZIP original; el esquema 3 se exporta durante KSP. No se configura migración destructiva. La cadena soporta las versiones 2 y 3 previas. No se afirma compatibilidad con esquemas históricos no documentados.

Las contraseñas nuevas se guardan como PBKDF2-HMAC-SHA1, 210.000 iteraciones, sal aleatoria de 16 bytes y derivación de 256 bits, compatible con API 24. Una clave en texto plano de la versión 2 se convierte después de un acceso correcto. El usuario entregado a la UI no incluye esa clave. El seeder crea los usuarios demo solo si no existen y no cambia sus contraseñas en cada apertura.

Los adjuntos nuevos se copian al directorio privado `files/adjuntos`. Se validan MIME, tamaño informado y bytes realmente leídos, con máximo 5 MB; se rechazan archivos vacíos. FileProvider concede acceso temporal de lectura únicamente al archivo abierto. No se necesitan permisos globales de almacenamiento. Los adjuntos de la versión 2 siguen siendo metadatos: no es posible reconstruir archivos que nunca fueron guardados. La app explica este caso al intentar abrirlos.

Las copias nuevas reemplazadas o abandonadas se eliminan; los archivos originales del selector no se modifican. La app deshabilita backup y excluye base, archivos y preferencias de extracción.

## Sesión y navegación

`SesionViewModel` conserva la cuenta y los ViewModels de sus pantallas durante cambios de configuración. Cerrar sesión destruye ese estado y los formularios de la cuenta. La casilla **Recordar sesión** determina si se conserva el acceso al cerrar el proceso. `SesionRepository` guarda solo el ID en preferencias privadas, excluidas de backup. Al iniciar se consulta Room para recuperar el perfil actual y abrir la pantalla adecuada; si la cuenta no existe se elimina el ID guardado. No se persiste la contraseña. Un acceso incorrecto no guarda la sesión. Al cerrar sesión se borra la preferencia antes de liberar el estado y volver al login.

El catálogo guarda el identificador de formato para reconstruirlo al rotar. Cada solicitud usa una clave de ViewModel distinta, por lo que un envío exitoso no impide crear otro trámite del mismo formato. Perfil permite cerrar sesión tanto a Persona como a Empresa; Notario tiene su acción en la barra superior.

## Defensa y límites

- Room emite `Flow` y Compose observa el estado del ViewModel; al cambiar la base se actualizan listados y avisos.
- Las transacciones impiden guardar un estado nuevo sin su historial o aviso correspondiente.
- El RUT ficticio se valida solo por formato. Cualquier último carácter 0–9, K o k es válido; no se calcula el verificador matemático. La regla aplica a Persona, Empresa, representante, Notario y solicitudes.
- La copia privada conserva un adjunto después de perder el permiso temporal del selector.
- Los avisos son internos, no notificaciones del sistema. El recurso nativo exigido se cubre mediante el selector y visor de archivos.
- El catálogo contiene tres formatos ficticios. El prototipo HTML y su pago/agendamiento no forman parte de la aplicación Android.
- No hay backend: ambas cuentas operan sobre la base del mismo dispositivo. No se enviaron documentos ni credenciales a servicios externos.
- Trello, mockup, video, presentación técnica y cumplimiento del plazo deben ser aportados por el equipo.
