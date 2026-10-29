# Notix --- Guía de implementación por etapas

> **Documento histórico / plan de trabajo.** Describe el proyecto en una etapa anterior.
> Para conocer el estado real, la arquitectura y la persistencia implementada, ver
> [`Documentacion_Notix.md`](Documentacion_Notix.md).

## Objetivo

Completar la app Android **Notix** de forma gradual, manteniendo el
proyecto entendible y alineado con la rúbrica. No intentes implementar
todo de una vez: termina y prueba una etapa antes de avanzar.

## 1. Estado actual del proyecto

Según la estructura compartida:

``` text
notix/
├── MainActivity.kt
├── model/
│   ├── Aviso.kt
│   ├── EstadoTramite.kt
│   ├── FormatoTramite.kt
│   ├── TipoCuenta.kt
│   ├── TipoFirma.kt
│   ├── Tramite.kt
│   └── Usuario.kt
├── repository/       # vacío
├── ui/
│   ├── screens/
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   └── WelcomeScreen.kt
│   └── theme/
└── viewmodel/        # vacío
```

Ya existe una navegación inicial entre Bienvenida, Login y Crear cuenta.
Los callbacks de inicio de sesión todavía no navegan a las bandejas.

**Antes de crear archivos:** revisa el `package` declarado en cada
archivo y conserva una convención coherente con `com.duoc.notix`. No
copies código de una guía sin adaptar sus imports y modelos a los que ya
existen.

## 2. Orden de trabajo recomendado

### Etapa 1 --- Preparar Git y el proyecto

1.  Comprueba que el proyecto compile y que las tres pantallas actuales
    funcionen.
2.  Sube el estado inicial a GitHub.
3.  Trabaja con ramas por funcionalidad y commits pequeños con mensajes
    descriptivos.
4.  Si es un trabajo grupal, reparte tareas y asegúrate de que todos
    contribuyan con commits.
5.  Crea o actualiza un `README.md` con instrucciones para ejecutar la
    app.

**Criterio relacionado:** control de versiones (20%). Guarda evidencia
real del trabajo colaborativo; no dejes todos los commits para el final.

### Etapa 2 --- Revisar los modelos existentes

Abre los archivos de `model/` y comprueba qué campos y tipos tienen
`Usuario`, `Tramite`, `Aviso`, `FormatoTramite`, `EstadoTramite`,
`TipoCuenta` y `TipoFirma`.

-   Reutiliza los modelos existentes cuando representen los datos
    necesarios.
-   Identifica qué falta para el flujo: documentos adjuntos,
    antecedentes del solicitante, historial, observaciones, confirmación
    de legibilidad y firma simulada.
-   Añade solo los campos necesarios y actualiza los lugares donde se
    usan.
-   Decide cómo representar fechas y estados de forma consistente.

No reemplaces los modelos existentes por otros de una respuesta anterior
sin compararlos primero.

### Etapa 3 --- Configurar persistencia local con Room

La rúbrica exige persistencia local. Los trámites y sus cambios no deben
desaparecer al cerrar la aplicación.

1.  Revisa los archivos Gradle del proyecto y confirma las versiones de
    Kotlin y Android Gradle Plugin.
2.  Configura KSP con una versión compatible con la versión de Kotlin
    del proyecto.
3.  Añade las dependencias de Room compatibles con tu proyecto.
4.  Crea la capa local, por ejemplo:
    -   `data/local/Entities.kt`: entidades de base de datos.
    -   `data/local/TramiteDao.kt`: consultas e inserciones.
    -   `data/local/NotixDatabase.kt`: definición de la base de datos.
    -   `data/local/Mappers.kt`: conversión entre entidades y modelos.
5.  Guarda trámites, documentos, historial y avisos. Usa tablas
    relacionadas cuando corresponda.
6.  Crea la instancia compartida de la base de datos y comprueba que
    puedes insertar y consultar un trámite.

**Importante:** no pegues las dependencias o versiones a ciegas.
Comprueba primero las versiones que ya usa el proyecto y sincroniza
Gradle antes de seguir.

### Etapa 4 --- Crear el repositorio y centralizar las validaciones

Crea `TramiteRepository` para coordinar el acceso a Room y las
operaciones del trámite. Crea también `TramiteValidator` para que las
reglas no estén mezcladas con las pantallas.

El repositorio debe permitir, como mínimo:

-   Observar los trámites de una persona.
-   Observar la bandeja del notario.
-   Consultar un trámite y sus avisos.
-   Crear y enviar un trámite.
-   Corregir y reenviar un trámite devuelto.
-   Confirmar la legibilidad de documentos.
-   Aprobar o devolver un trámite.
-   Marcar avisos como leídos.
-   Registrar cada cambio en el historial y generar el aviso
    correspondiente.

Validaciones necesarias:

-   Los antecedentes obligatorios deben estar completos.
-   El correo debe tener un formato válido.
-   Debe existir al menos un documento.
-   No se puede enviar sin completar la firma simulada.
-   No se puede devolver un trámite sin escribir una observación.
-   No se puede aprobar sin confirmar la legibilidad.
-   Las acciones deben respetar el estado actual del trámite.

Usa un resultado claro para distinguir éxito de errores de validación.
Devuelve errores identificados por campo para que la interfaz pueda
mostrarlos donde corresponde.

### Etapa 5 --- Crear ViewModels y estados de pantalla

La carpeta `viewmodel/` está vacía. Crea un ViewModel por flujo o
pantalla compleja, comenzando por Login y Registro.

Cada ViewModel debe: - Mantener el estado de la pantalla. - Recibir los
cambios de los campos. - Ejecutar validaciones. - Llamar al repositorio
para realizar operaciones. - Exponer estados de carga, éxito y error.

La pantalla Compose solo debe mostrar el estado y enviar eventos al
ViewModel. Evita poner reglas de negocio o consultas directas a Room
dentro de la UI.

Para formularios, guarda errores por campo; por ejemplo, `emailError` y
`passwordError`. Muestra el borde de error, un mensaje claro y un icono
cuando corresponda.

### Etapa 6 --- Completar los flujos de la aplicación

Implementa y prueba los flujos en este orden:

**A. Acceso** 1. Bienvenida. 2. Login y selección del tipo de cuenta. 3.
Registro. 4. Validaciones por campo. 5. Navegación según el rol.

**B. Persona o empresa** 1. Mis trámites. 2. Crear un trámite y
seleccionar el formato. 3. Cargar documentos. 4. Completar antecedentes.
5. Realizar la firma simulada. 6. Revisar y enviar. 7. Consultar estado,
historial y observaciones. 8. Corregir y reenviar si el notario lo
devuelve.

**C. Notario** 1. Bandeja de trámites recibidos. 2. Abrir el detalle y
revisar documentos. 3. Confirmar legibilidad. 4. Aprobar o devolver con
una observación obligatoria. 5. Verificar que el estado, el historial y
el aviso del solicitante se actualicen.

Conecta estos flujos desde `MainActivity` o desde el mecanismo de
navegación que elijas. Evita que los callbacks de Login queden como
`TODO`.

### Etapa 7 --- Integrar dos recursos del dispositivo

La propuesta más natural para el caso es:

1.  **Selector de archivos o cámara:** permitir adjuntar documentos al
    trámite. Empieza por el selector de archivos si quieres simplificar
    los permisos; conserva la referencia del archivo de forma adecuada.
2.  **Notificaciones:** avisar cuando un trámite sea devuelto o
    finalizado. Implementa el permiso de notificaciones requerido por la
    versión de Android y prueba el flujo completo.

Los avisos guardados en Room y las notificaciones del sistema son cosas
relacionadas, pero distintas: guardar un aviso en la base de datos no
muestra automáticamente una notificación del sistema.

La firma biométrica del caso debe seguir siendo simulada, salvo que el
docente autorice expresamente otra cosa.

### Etapa 8 --- Añadir animaciones y mejorar la interfaz

Cuando los flujos funcionen:

-   Usa `AnimatedVisibility` para mostrar y ocultar errores.
-   Usa una transición sencilla al cambiar de pantalla o tipo de cuenta.
-   Anima la expansión de tarjetas cuando aporte claridad.
-   Si el formulario tiene varios pasos, anima el progreso.
-   Mantén consistentes colores, tipografía, espaciado, iconos y estados
    de botones.

No priorices las animaciones antes de que la navegación, las
validaciones y el guardado funcionen.

### Etapa 9 --- Probar y documentar

Prueba manualmente, como mínimo:

-   Login y registro con campos vacíos y datos inválidos.
-   Envío bloqueado si faltan antecedentes, documentos o firma.
-   Trámite enviado visible en la bandeja del notario.
-   Devolución bloqueada si no hay observación.
-   Trámite devuelto visible para la persona y corregible.
-   Aprobación bloqueada si no se confirma la legibilidad.
-   Historial y avisos actualizados tras cada cambio.
-   Datos que siguen existiendo después de cerrar y abrir la app.
-   Selector de archivos y notificaciones en el dispositivo o emulador.

Añade pruebas unitarias para las validaciones cuando sea posible.
Documenta cómo ejecutar el proyecto y qué funcionalidades están
simuladas.

## 3. Estructura objetivo sugerida

No es necesario mover todo de golpe. La estructura puede evolucionar
gradualmente hacia:

``` text
com.duoc.notix/
├── data/
│   ├── local/          # Room: entidades, DAO, base de datos y mapeadores
│   ├── validation/     # Reglas de validación independientes de la UI
│   └── repository/     # Acceso a datos y operaciones de negocio
├── model/              # Modelos de dominio actuales
├── ui/
│   ├── screens/
│   ├── components/
│   └── theme/
├── viewmodel/
├── navigation/
└── MainActivity.kt
```

Si cambias la ubicación de los paquetes, actualiza los `package` y los
imports. Hazlo en pasos pequeños para detectar errores fácilmente.

## 4. Qué hacer ahora mismo

Sigue esta lista en orden:

-   [ ] Confirmar que el proyecto actual compila.
-   [ ] Revisar Gradle y preparar GitHub, ramas y reparto de tareas.
-   [ ] Leer los modelos actuales y anotar los campos que faltan.
-   [ ] Configurar Room y comprobar una inserción y consulta sencilla.
-   [ ] Implementar `TramiteValidator` y `TramiteRepository`.
-   [ ] Crear ViewModels con estado por campo.
-   [ ] Conectar Login y Registro.
-   [ ] Completar el flujo de la persona y luego el del notario.
-   [ ] Integrar selector de archivos y notificaciones.
-   [ ] Añadir animaciones, pruebas y documentación.

**Regla de trabajo:** implementa una etapa a la vez. Después de cada
etapa, compila y prueba antes de continuar. Si aparece un error,
resuélvelo antes de añadir más código.
