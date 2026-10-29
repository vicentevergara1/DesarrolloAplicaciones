# Notix — Documentación técnica y estado del proyecto

> Proyecto académico DSY1105 · Caso NotariaX · Duoc UC
> Última actualización: 29/10/2026

---

## 1. Resumen

**Notix** es el MVP Android del caso **NotariaX**: una app para gestionar y seguir trámites notariales digitales con datos ficticios.

El flujo implementado cubre:

1. Registro e inicio de sesión con tres perfiles: **Persona**, **Empresa** y **Notario**.
2. Catálogo ficticio de formatos de trámite.
3. Creación de un trámite con antecedentes, documento adjunto y confirmación.
4. Envío simulado al notario.
5. Bandeja del notario con filtros y contadores.
6. Revisión del trámite: **aprobar/finalizar** o **devolver con observaciones**.
7. Actualización automática de estado, historial y avisos.
8. Consulta del estado, historial y observaciones por parte del solicitante.

La app **no debe usarse para trámites notariales reales**. No integra servicios productivos, firmas reales, biometría real ni bases de datos de NotariaX.

---

## 2. Estado general

| Área | Estado |
|---|---|
| Proyecto Android + Jetpack Compose | ✅ Funcional |
| Persistencia local con Room | ✅ Implementada |
| Autenticación local (login/registro) | ✅ Implementada |
| Flujo Persona/Empresa | ✅ Implementado en su mayoría |
| Flujo Notario | ✅ Implementado en su mayoría |
| Historial y avisos en base de datos | ✅ Implementado |
| Selector nativo de archivos | ✅ Integrado (solo metadatos) |
| Notificaciones del sistema | ❌ Pendiente |
| Firma simulada (trazo/mock) | ⚠️ Solo declaración, sin representación real |
| Corrección y reenvío tras devolución | ❌ Pendiente |
| Confirmación de legibilidad por el notario | ⚠️ Campo existe, no se exige en UI |
| Pruebas unitarias/UI | ⚠️ Solo pruebas de ejemplo |
| Navegación completa (perfil, atrás, logout persona) | ⚠️ Parcial |
| Documentación | ✅ Este documento + README actualizado |

> El proyecto **compila** con el SDK configurado. Ver sección 12 para la verificación y requisitos de entorno.

---

## 3. Estructura del repositorio

```text
caso-NotariaX/
├── README.md                      # Entrada rápida: cómo ejecutar y estado
├── docs/
│   ├── contexto.md                # Enunciado y alcance del caso
│   ├── Guia_implementacion_Notix.md  # Plan de trabajo por etapas (histórico)
│   └── Documentacion_Notix.md     # Este documento (estado real)
│
├── index.html                     # Prototipo web legado (no es la app)
├── css/styles.css                 # Estilos del prototipo web legado
├── views/                         # Pantallas HTML del prototipo legado
│
└── Notix/                         # Proyecto Android (entrega principal)
    ├── app/
    │   ├── build.gradle.kts
    │   └── src/main/
    │       ├── AndroidManifest.xml
    │       ├── java/com/duoc/notix/
    │       │   ├── MainActivity.kt
    │       │   ├── model/                 # Modelos de dominio
    │       │   ├── data/
    │       │   │   ├── local/             # Room: entidades, DAO, DB, mappers, seeder
    │       │   │   └── validation/        # Validación de RUN
    │       │   ├── repository/            # AuthRepository + TramiteRepository
    │       │   ├── viewmodel/             # Un ViewModel por flujo
    │       │   ├── ui/
    │       │   │   ├── screens/           # Pantallas Compose
    │       │   │   ├── components/        # Componentes reutilizables
    │       │   │   └── theme/             # Colores, tipografía, tema
    │       │   └── util/                  # Formateo de RUT, documento y fechas
    │       └── res/                       # Recursos Android
    ├── gradle/libs.versions.toml
    └── gradlew / gradlew.bat
```

### Sobre el prototipo web

`index.html`, `css/` y `views/` son el **prototipo navegable original** en HTML. No forman parte de la app Android ni comparten código con ella. Se conservan como referencia visual y de flujo. No tienen persistencia: son formularios estáticos que navegan por URL.

---

## 4. Tecnologías

| Elemento | Versión / decisión |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | UI → ViewModel → Repository → Room |
| Persistencia | Room 2.8.5 con KSP |
| SDK | compileSdk 37, targetSdk 36, minSdk 24 |
| Build | Android Gradle Plugin 9.2.1 |
| Java | 11 |
| Navegación | Estado en `MainActivity` (`enum Pantalla`), sin Navigation Compose |
| Datos | 100 % ficticios, definidos en el seeder |

Dependencias relevantes declaradas en `Notix/gradle/libs.versions.toml`:

- `androidx.room:room-runtime`, `room-ktx`, `room-compiler` (KSP)
- `androidx.lifecycle:lifecycle-viewmodel-compose`
- `androidx.compose.material3` y `material-icons-extended`
- `androidx.activity:activity-compose`

---

## 5. Arquitectura

```text
┌──────────────────────────────┐
│  Compose Screens             │  Solo muestran estado y envían eventos
│  ui/screens/*                │
└──────────────┬───────────────┘
               │ eventos / StateFlow / mutableStateOf
┌──────────────▼───────────────┐
│  ViewModels                  │  Estado de pantalla, validaciones de UI,
│  viewmodel/*                 │  llamadas al repositorio
└──────────────┬───────────────┘
               │ suspend / Flow
┌──────────────▼───────────────┐
│  Repositories                │  Operaciones de negocio y coordinación
│  repository/*                │  (crear trámite, cambiar estado, avisos)
└──────────────┬───────────────┘
               │ DAOs / entidades
┌──────────────▼───────────────┐
│  Room                        │  Persistencia local en SQLite
│  data/local/*                │
└──────────────────────────────┘
```

Decisiones actuales:

- La UI **no consulta Room directamente**; excepto la construcción de dependencias en los `factory`.
- `AuthRepository` y `TramiteRepository` encapsulan las operaciones.
- La validación de RUN vive en `data/validation/RutValidator`.
- Algunas validaciones de formulario están en el ViewModel y otras en la pantalla de registro (deuda técnica menor).
- La sesión se maneja con estado en memoria/`rememberSaveable` en `MainActivity`; **no hay sesión persistida**.

---

## 6. Persistencia de datos (Room)

Esta es la parte central para la rúbrica: **los trámites, usuarios, documentos, historial y avisos sobreviven al cierre de la app**.

### 6.1 Base de datos

| Dato | Valor |
|---|---|
| Clase | `NotixDatabase` (`data/local/NotixDatabase.kt`) |
| Nombre del archivo | `notix_database` (SQLite dentro del sandbox de la app) |
| Versión | `2` |
| Exportación de esquema | `exportSchema = false` |
| Migraciones | `fallbackToDestructiveMigration()` |
| Instancia | Singleton con `getDatabase(context)` |
| Convertidores | `NotixTypeConverters` |

> ⚠️ **Importante:** al cambiar la versión del esquema sin migración, Room **borra y recrea las tablas** (pérdida de datos). Es aceptable para el MVP, pero debe cambiarse antes de una entrega con datos que se quieran conservar. Ver sección 10.

### 6.2 Tablas

#### `usuarios`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT (PK) | `USR-...` |
| `tipoCuenta` | TEXT | `PERSONA`, `EMPRESA`, `NOTARIO` |
| `nombreCompleto` | TEXT | En empresa, representante legal |
| `correo` | TEXT | Índice único |
| `contrasena` | TEXT | **Texto plano** (solo demo) |
| `run` | TEXT | Limpio, sin puntos ni guion |
| `numeroDocumento` | TEXT | Limpio |
| `telefono` | TEXT | Limpio |
| `comuna` | TEXT | Residencia / domicilio / comuna de notaría |
| `razonSocialEmpresa` | TEXT? | Solo empresa |
| `rutEmpresa` | TEXT? | Solo empresa |
| `giroEmpresa` | TEXT? | Solo empresa |
| `numeroNotaria` | INTEGER? | Solo notario |

#### `tramites`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT (PK) | `TX-2026-XXXX` |
| `formatoId`, `formatoNombre`, `formatoCategoria` | TEXT | Datos denormalizados del catálogo |
| `solicitanteId` | TEXT | Referencia lógica a `usuarios.id` (sin FK) |
| `solicitanteNombre` | TEXT | Copia del nombre al crear |
| `actuaEnNombreDe` | TEXT? | Razón social si aplica |
| `tipoFirma` | TEXT | `SIMPLE`, `AVANZADA`, `NOTARIADA` |
| `estado` | TEXT | `ENVIADO`, `DEVUELTO`, `FINALIZADO` |
| `antecedentes*` | TEXT | Nombre, documento, correo y comuna |
| `observacion` | TEXT? | Motivo de devolución |
| `legibilidadConfirmada` | INTEGER | Booleano; **aún no se usa en el flujo** |

#### `documentos`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | INTEGER (PK autoincremental) | |
| `tramiteId` | TEXT (FK → `tramites.id`) | `ON DELETE CASCADE` |
| `nombre` | TEXT | Solo nombre del archivo |
| `tamanoKb` | INTEGER | Tamaño simulado o estimado |
| `estado` | TEXT | `ACEPTADO`, `OBSERVADO` |

> **No se guarda el archivo ni su contenido.** El selector nativo entrega una URI, pero solo se persiste el nombre. No se copia el archivo al almacenamiento de la app ni se solicita permiso persistente de lectura. Ver sección 10.

#### `historial_eventos`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | INTEGER (PK autoincremental) | |
| `tramiteId` | TEXT (FK → `tramites.id`) | `ON DELETE CASCADE` |
| `descripcion` | TEXT | Texto del evento |
| `fechaHora` | TEXT | `dd/MM/yyyy HH:mm` (fecha real del dispositivo) |
| `rol` | TEXT | Quién realiza el cambio |

#### `avisos`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT (PK) | `AVS-...` |
| `destinatarioId` | TEXT | Usuario que recibe el aviso |
| `tramiteId` | TEXT | Trámite relacionado |
| `mensaje` | TEXT | Texto mostrado |
| `fechaHora` | TEXT | Fecha y hora |
| `leido` | INTEGER | Booleano |

> `avisos` no tiene FK ni índice; se consulta por `destinatarioId` y se ordena por `fechaHora` como texto.

### 6.3 DAOs

`TramiteDao`

- `insertTramite`, `insertDocumentos`, `insertHistorial`
- `insertTramiteCompleto(...)`: transacción que inserta trámite + documentos + historial
- `getTramiteConDetalles(id)`: `Flow<TramiteConDetalles?>`
- `getTramitesPorSolicitante(id)`: `Flow<List<TramiteConDetalles>>`
- `getTodosLosTramites()`: `Flow<List<TramiteConDetalles>>`
- `updateTramite(...)`

`UsuarioDao`

- `insertUsuario` (falla si hay duplicados)
- `upsertUsuario` (usado por el seeder)
- `getUsuarioPorCorreo(correo)`
- `getUsuarioPorId(id)`: `Flow`

`AvisoDao`

- `insertAviso`
- `getAvisosPorDestinatario(id)`: `Flow`
- `marcarAvisoLeido(id)`

Las consultas con `Flow` hacen que las pantallas se actualicen automáticamente cuando cambia la base de datos. Por eso, cuando el notario aprueba o devuelve un trámite, la persona ve el cambio sin reiniciar la app.

### 6.4 Mapeo dominio ↔ base de datos

- `Mappers.kt` convierte `Usuario`, `Tramite` y `Aviso` hacia/desde entidades.
- `TramiteConDetalles` usa `@Relation` para traer documentos e historial.
- El formato se guarda **denormalizado** dentro del trámite, de modo que un cambio futuro del catálogo no altere trámites históricos.

### 6.5 TypeConverters

Los enums se guardan como `String` (`name`) y al leerlos se usa `runCatching` con valor por defecto:

- `TipoCuenta` → `PERSONA`
- `EstadoTramite` → `ENVIADO`
- `TipoFirma` → `SIMPLE`
- `EstadoDocumento` → `ACEPTADO`

Esto evita crashes ante valores desconocidos, pero puede ocultar datos corruptos.

### 6.6 Datos iniciales (seeder)

`DatabaseSeeder.seedIfEmpty()` se ejecuta al iniciar `MainActivity`.

- Es **idempotente**: crea/actualiza los usuarios demo y solo inserta trámites si la tabla está vacía.
- Usuarios demo:

| Perfil | Correo | Contraseña |
|---|---|---|
| Persona | `ana.demo@notariax.test` | `demo123` |
| Notario | `notario.demo@notariax.test` | `notario123` |

- Trámites demo:

| ID | Formato | Estado |
|---|---|---|
| `TX-2026-0042` | Poder simple | Devuelto (con anexo observado) |
| `TX-2026-0039` | Declaración jurada simple | Enviado |
| `TX-2026-0031` | Autorización de viaje | Finalizado |
| `TX-2026-0001` | Poder simple | Enviado |

- Aviso demo: `AVS-001`.

> La contraseña de empresa no está sembrada; debe registrarse una cuenta desde la app.

### 6.7 Qué persiste y qué no

| Dato | ¿Persiste al cerrar la app? | Dónde |
|---|---|---|
| Usuarios registrados | ✅ Sí | Room (`usuarios`) |
| Trámites creados | ✅ Sí | Room (`tramites`) |
| Documentos (nombre, tamaño, estado) | ✅ Sí | Room (`documentos`) |
| Historial de estados | ✅ Sí | Room (`historial_eventos`) |
| Avisos y estado leído | ✅ Sí | Room (`avisos`) |
| Sesión iniciada / usuario actual | ❌ No | Estado de `MainActivity` |
| Contraseña | ⚠️ Sí, en texto plano | Room (`usuarios`) |
| Archivo adjunto real (contenido/URI) | ❌ No | Solo se guarda el nombre |
| Firma dibujada | ❌ No existe | — |
| Notificaciones del sistema | ❌ No | Solo avisos internos en Room |
| Filtros y selección de pantalla | ❌ No | Estado Compose en memoria |

### 6.8 Backup de Android

`AndroidManifest.xml` declara:

- `android:allowBackup="true"`
- `android:fullBackupContent="@xml/backup_rules"`
- `android:dataExtractionRules="@xml/data_extraction_rules"`

Esto significa que Android **podría respaldar la base de datos** según las reglas del dispositivo. Para un MVP con datos ficticios no es crítico, pero conviene revisarlo si se quiere evitar cualquier respaldo.

### 6.9 Consecuencias prácticas

- Cerrar y abrir la app **mantiene** trámites, usuarios, avisos e historial.
- Cambiar la versión de la base de datos **borra todo** por `fallbackToDestructiveMigration()`.
- Desinstalar la app **borra** la base de datos.
- Borrar datos de la app desde Ajustes **borra** la base de datos.
- La app vuelve siempre a la pantalla de bienvenida porque la sesión no se guarda.
- El archivo elegido con el selector **no se conserva**; solo su nombre.

---

## 7. Funcionalidades implementadas

### 7.1 Acceso y cuentas

- `WelcomeScreen`: entrada a login y registro.
- `LoginScreen`: selector Persona / Empresa / Notario, correo, contraseña, mostrar/ocultar clave.
- Botón “Simular huella digital”: ejecuta el mismo login; **no usa biometría real**.
- `RegisterScreen`: formulario dinámico según tipo de cuenta.
  - Persona: nombre, RUT, N° documento, correo, teléfono, comuna.
  - Empresa: razón social, RUT empresa, giro, representante legal, RUN representante, documento, correo, teléfono, comuna.
  - Notario: nombre, RUN, documento, N° notaría, comuna, correo, teléfono.
  - Validaciones: RUN con dígito verificador, documento de 9 dígitos, correo, teléfono, número de notaría, contraseña mínima y confirmación.
- `AuthRepository`: evita correos duplicados, normaliza el correo y valida tipo de cuenta/clave.
- Al autenticarse, redirige a Mis Trámites (Persona/Empresa) o Bandeja del Notario.

### 7.2 Flujo Persona / Empresa

- `CatalogoFormatosScreen`: 3 formatos ficticios (`F01`, `F02`, `F03`) con firma requerida.
- `CrearTramiteScreen`:
  - Carga antecedentes desde el usuario.
  - Edición de nombre, RUN, correo y comuna con validaciones.
  - Campo “en representación de” para Empresa.
  - Selector nativo de archivos (`ActivityResultContracts.GetContent`).
  - Declaración de legibilidad/veracidad.
  - Modal de confirmación con resumen.
  - Guarda el trámite y crea un aviso.
- `MisTramitesScreen`:
  - Contadores: en revisión, observados, finalizados.
  - Filtros: todos, en curso, observados, finalizados.
  - Banner de alerta para trámite devuelto.
  - Detalle con antecedentes, documentos, observación del notario e historial.
- `AvisosScreen`: lista de avisos, marca como leído al tocar.

### 7.3 Flujo Notario

- `BandejaNotarioScreen`:
  - Muestra todos los trámites (sin asignación por notaría).
  - Contadores y filtros.
  - Tarjetas con solicitante, comuna, correo, observación y estado.
- Diálogo de revisión:
  - Muestra antecedentes y documentos.
  - **Aprobar** → estado `FINALIZADO`, nuevo evento de historial y aviso al solicitante.
  - **Devolver** → exige observación obligatoria; estado `DEVUELTO`, historial y aviso.

### 7.4 UI y recursos

- Tema propio (`NotixColors`, `Theme`, `Type`).
- Componentes: `TramiteCard`, `EstadoChip`, `BarraInferiorPersona`, `NotixTextField`, transformaciones visuales de RUT/documento.
- Animaciones: contadores, chips, visibilidad de errores, transiciones básicas.
- Formato de fechas y documentos.
- Icono/logo de la app.

---

## 8. Modelo de datos de dominio

```text
Usuario
 ├── id, tipoCuenta, nombreCompleto, correo, contrasena
 ├── run, numeroDocumento, telefono, comuna
 ├── empresa: DatosEmpresa? (razón social, RUT, giro)
 └── numeroNotaria: Int?

Tramite
 ├── id, formato: FormatoTramite, solicitanteId, solicitanteNombre
 ├── actuaEnNombreDe: String?
 ├── tipoFirma: TipoFirma
 ├── estado: EstadoTramite
 ├── documentos: List<Documento>
 ├── antecedentes: Antecedentes
 ├── observacion: String?
 ├── legibilidadConfirmada: Boolean
 └── historial: List<EventoHistorial>

Aviso
 ├── id, destinatarioId, tramiteId
 ├── mensaje, fechaHora
 └── leido: Boolean
```

Estados:

- `ENVIADO`: visible para la persona como “Enviado”; para el notario como “Pendiente”.
- `DEVUELTO`: “Observado” / “Devuelto”.
- `FINALIZADO`: “Finalizado”.

---

## 9. Cómo ejecutar

### Requisitos

- Android Studio reciente con soporte para AGP 9.2.1.
- JDK 11 o superior.
- SDK Android 37 instalado.
- Emulador o dispositivo Android 7.0+ (API 24+).

### Pasos

```bash
cd Notix
./gradlew assembleDebug
```

> En este entorno `gradlew` no tenía permiso de ejecución. Si ocurre, usar `sh gradlew ...` o `chmod +x gradlew`.

También se puede abrir la carpeta `Notix/` directamente en Android Studio y ejecutar la configuración `app`.

### Credenciales demo

```
Persona:  ana.demo@notariax.test / demo123
Notario:  notario.demo@notariax.test / notario123
```

También se puede crear una cuenta nueva desde “Crear cuenta”.

---

## 10. Pendientes y deuda técnica

Ordenados por impacto para la rúbrica y el flujo principal.

### Alta prioridad

1. **Firma simulada real**
   - Hoy el flujo solo muestra una declaración y una nota.
   - Falta representar la firma según `TipoFirma`:
     - `SIMPLE`: trazo en pantalla (Canvas) con datos ficticios.
     - `AVANZADA`: mock de e-token/certificado.
     - `NOTARIADA`: confirmación simulada ante notario.
   - Marcarla como requisito para enviar y guardar evidencia ficticia en el trámite.

2. **Corrección y reenvío tras devolución**
   - Actualmente el trámite devuelto solo se puede consultar.
   - Falta editar antecedentes/documentos, registrar corrección y volver a `ENVIADO`.
   - Debe generar evento de historial y aviso.

3. **Confirmación de legibilidad obligatoria del notario**
   - `legibilidadConfirmada` existe en el modelo y la tabla, pero no se usa.
   - El diálogo permite aprobar sin confirmar legibilidad.
   - Agregar checkbox y bloquear “Aprobar” hasta marcarlo; persistir el valor.

4. **Persistencia real del documento adjunto**
   - Hoy solo se guarda el nombre.
   - Copiar el archivo a almacenamiento interno o guardar URI con permiso persistente.
   - Permitir múltiples documentos y estados por documento.
   - Validar tipo/tamaño y evitar rutas nulas.

5. **Respetar el estado en acciones del notario**
   - Se puede aprobar o devolver un trámite ya `FINALIZADO`.
   - Ocultar o bloquear acciones según estado actual.
   - Evitar dobles envíos/devoluciones.

### Media prioridad

6. **Notificaciones del sistema**
   - Los avisos en Room no generan notificación Android.
   - Implementar permiso `POST_NOTIFICATIONS` (API 33+) y `NotificationManager`.
   - Notificar al devolver o finalizar un trámite.

7. **Navegación y sesión**
   - `BarraInferiorPersona` tiene varias acciones vacías (`onInicio`, `onPerfil`).
   - No hay pantalla de perfil ni cierre de sesión para Persona/Empresa.
   - No se maneja el botón atrás del sistema.
   - Considerar `Navigation Compose` o un manejo centralizado de rutas.
   - Persistir la sesión (DataStore) si se desea mantener al usuario autenticado.

8. **Avisos navegables**
   - `Aviso.tramiteId` no se usa para abrir el trámite.
   - Al tocar un aviso conviene marcar leído y navegar al detalle.

9. **Asignación de trámites al notario**
   - El notario ve todos los trámites de la base.
   - En un escenario real debería ver solo los de su notaría o los asignados.

10. **Validaciones centralizadas**
    - Hoy hay reglas en pantallas, ViewModels y repositorio.
    - Extraer un `TramiteValidator` (como propone la guía) para evitar duplicación.

### Baja prioridad / calidad

11. **Migraciones Room**
    - Reemplazar `fallbackToDestructiveMigration()` por migraciones explícitas.
    - Activar `exportSchema = true` y versionar el esquema.

12. **Seguridad**
    - Contraseñas en texto plano; para el MVP es aceptable, pero documentar que no es producción.
    - No hay cifrado de base de datos ni control de acceso a nivel de datos.
    - Revisar `allowBackup`.

13. **Fechas**
    - Se usa la fecha real del dispositivo, no una fecha simulada controlada.
    - El orden de trámites se hace por `id` descendente, no por fecha de creación.
    - Considerar `fechaCreacion` y ordenar por ella.

14. **IDs**
    - Los trámites nuevos usan `TX-2026-${1000..9999}.random()`, lo que puede colisionar.
    - Usar secuencia o UUID.

15. **Documentación y código**
    - `docs/Guia_implementacion_Notix.md` describe un estado anterior (repository vacío).
    - Actualizar esa guía o marcarla como histórica.
    - `README.md` debe mantenerse como entrada principal.
    - Revisar nombres de archivo: `AuthViewmodel.kt` vs `AuthViewModel`, `TramitesViewmodel.kt`.

16. **Pruebas**
    - Agregar pruebas unitarias para `RutValidator`, validaciones de formularios y transiciones de estado.
    - Agregar pruebas de DAO/instrumentadas para persistencia.
    - Agregar pruebas de UI para login, creación y revisión.

17. **Limpieza**
    - Eliminar `.idea/`, `build/` y archivos generados del control de versiones si no son necesarios.
    - Revisar `.gitignore` en raíz y en `Notix/`.

---

## 11. Checklist de pruebas manuales

- [ ] Registro Persona con RUT válido e inválido.
- [ ] Registro Empresa y Notario con campos específicos.
- [ ] Login con correo/clave correctos e incorrectos.
- [ ] Login con tipo de cuenta incorrecto.
- [ ] Crear trámite sin nombre, RUN inválido, correo inválido o comuna vacía.
- [ ] Crear trámite con y sin documento adjunto.
- [ ] Confirmar que el botón de envío exige la declaración de legibilidad.
- [ ] Ver el trámite creado en “Mis trámites”.
- [ ] Ver el trámite en la bandeja del notario.
- [ ] Aprobar un trámite y verificar estado, historial y aviso.
- [ ] Intentar devolver sin observación y verificar que se bloquea.
- [ ] Devolver con observación y verificar el aviso.
- [ ] Ver la observación en el detalle de la persona.
- [ ] Marcar un aviso como leído.
- [ ] Cerrar y abrir la app y verificar que los datos siguen.
- [ ] Verificar que la sesión no se mantiene al reiniciar.
- [ ] Verificar que el archivo adjunto no se conserva tras reiniciar (comportamiento actual).

---

## 12. Verificación del build

Comando usado:

```bash
cd Notix
JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk sh gradlew :app:compileDebugKotlin --offline
```

Resultado: **BUILD SUCCESSFUL** con el entorno local (AGP 9.2.1, Kotlin 2.2.10, Room 2.8.5, Gradle 9.4.1).

Observaciones del entorno:

- `gradlew` no tenía permiso de ejecución; se usó `sh gradlew` (o `chmod +x gradlew`).
- `JAVA_HOME` apuntaba a un directorio inválido (`/usr/lib/jvm/java-21-openjdk`); se usó el JDK Temurin 21 disponible.
- Gradle advierte que AGP 9.2.1 fue probado hasta compileSdk 37.0 y el proyecto usa 37.1. No impide compilar, pero conviene alinear versiones.
- Si falta alguna dependencia, ejecutar sin `--offline` con conexión a internet.

---

## 13. Recomendación de siguiente etapa

Para cerrar el MVP con el mayor impacto:

1. Implementar la **firma simulada** y exigirla al enviar.
2. Implementar **corregir y reenviar** un trámite devuelto.
3. Exigir **legibilidad confirmada** antes de aprobar.
4. Guardar el **documento adjunto** de forma persistente.
5. Agregar **notificaciones del sistema**.
6. Completar **navegación, perfil y cierre de sesión**.
7. Agregar **pruebas unitarias** de validaciones y persistencia.
8. Actualizar la guía histórica y mantener este documento al día.

---

## 14. Decisiones y notas

- Se priorizó un flujo completo Persona/Empresa ↔ Notario con persistencia local real.
- La autenticación es local y con contraseña en texto plano por tratarse de un MVP académico.
- Los documentos se representan por metadatos; no se almacena contenido real.
- La biometría, e-token y firma avanzada son simulaciones.
- La base de datos es la fuente de verdad de la app; los `Flow` mantienen la UI sincronizada.
- El prototipo HTML se conserva como referencia, no como parte funcional de la entrega Android.

---

## 15. Glosario rápido

| Término | Significado |
|---|---|
| DAO | Interfaz de acceso a datos de Room |
| Entidad | Tabla de la base de datos |
| Mapper | Conversión entre entidad y modelo de dominio |
| Seeder | Carga inicial de datos ficticios |
| Flow | Stream reactivo de datos |
| StateFlow | Estado observable para la UI |
| KSP | Procesador de símbolos usado por Room |
| MVP | Producto mínimo viable académico |
