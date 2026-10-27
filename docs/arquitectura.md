# Arquitectura y preparación de la defensa

## Responsabilidades

- **Vista (ui/screens)**: presenta estado y emite eventos. No consulta Room ni calcula dígitos verificadores.
- **ViewModel (ui/auth, ui/procedures)**: coordina formularios, navegación, validación y llamadas suspendidas; expone StateFlow. Conserva el estado al rotar.
- **Dominio (domain)**: modelos, perfiles, estados, reglas e interfaces. No depende de Compose.
- **Repositorios (data)**: normalización, contraseñas, permisos de negocio y transacciones.
- **Room (data/local)**: entidades, relaciones, DAOs, consultas observables, esquema y migración.
- **AndroidDocumentStore**: lee el archivo autorizado por el selector, verifica tipo y tamaño y crea una copia privada. El ViewModel usa su interfaz.
- **NotixApplication y NotixApp**: crean una base por proceso e inyectan repositorios. El ActivityResult del selector queda en la interfaz.

## Preguntas para practicar

**¿Qué diferencia Room de rememberSaveable?** Room almacena datos duraderos en SQLite, con consultas, relaciones y migraciones. El estado de pantalla no sustituye una base de datos.

**¿Cómo evita un solicitante finalizar su propio trámite?** El repositorio consulta la cuenta almacenada, comprueba su rol y exige estado En revisión. La regla se aplica también fuera de la vista.

**¿Por qué hay transacciones?** Un cambio de estado y su evento deben guardarse juntos. Si falla una parte, Room revierte ambas.

**¿Cómo llegan las observaciones a la otra cuenta?** Room emite relaciones actualizadas mediante Flow. El ViewModel las transforma en estado y Compose actualiza la pantalla. Ambos perfiles comparten la base del dispositivo.

**¿Se pueden usar dos teléfonos?** Este MVP es local. Las cuentas de distintos dispositivos no se sincronizan; el intercambio se demuestra cerrando e iniciando sesión en una misma instalación.

**¿Dónde se guarda la contraseña?** Se guarda una derivación PBKDF2 y una sal aleatoria, no el texto original. La operación se ejecuta fuera del hilo principal.

**¿Qué hace realmente la firma avanzada?** Guarda una selección y una confirmación de prueba. No usa certificados ni conecta servicios externos.

**¿Qué recurso nativo se utiliza?** El selector de documentos. Se copia el archivo elegido al almacenamiento privado. FileProvider expone la copia al visor cuando lo solicita el usuario.

**¿Qué pasa al actualizar la base?** La migración 1→2 conserva datos y agrega campos, tablas y eventos. Una prueba abre un esquema real de la versión 1 y verifica los datos migrados.

**¿Qué probaron automáticamente?** Reglas y ViewModels con JUnit, y Room/interfaz/archivos con Robolectric. La instalación en un teléfono y el video deben verificarse como parte de la entrega del equipo.
