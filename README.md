# NotariaX · Notix

MVP Android académico para DSY1105. Registra perfiles ficticios, gestiona documentos y representa un ciclo completo de trámite notarial. Usa Kotlin, Jetpack Compose, MVVM y Room. **No realiza trámites reales ni conecta proveedores de firma.**

## Abrir y ejecutar en Android Studio

1. Descarga y extrae el ZIP. Abre **la carpeta Notix/**, donde están settings.gradle.kts y gradlew.bat.
2. Configura **JDK 21 completo** como Gradle JDK en Settings → Build Tools → Gradle.
3. Instala **Android SDK Platform 37.0**, **Build Tools 36.0.0** y Platform Tools en SDK Manager. Usa Android Studio compatible con **AGP 9.2.1**.
4. Sincroniza Gradle y ejecuta **app** en un teléfono o emulador **API 24 o superior**.
5. La primera compilación descarga dependencias; la aplicación funciona después sin conexión a internet.

Desde terminal:

~~~sh
cd Notix
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
~~~

En Windows usa gradlew.bat. Configura JAVA_HOME y ANDROID_HOME, o sdk.dir en Notix/local.properties. No subas rutas locales, llaves ni credenciales. El APK queda en **Notix/app/build/outputs/apk/debug/app-debug.apk**.

## Probar rápidamente

En **Crear una cuenta de prueba**, elige un perfil y pulsa **Completar ejemplo ficticio**. Se completan datos sintéticos válidos, incluido el dígito verificador del RUT.

| Perfil | Correo de ejemplo | Contraseña de demostración |
|---|---|---|
| Persona | persona.demo@example.com | Demo2026! |
| Empresa | empresa.demo@example.com | Demo2026! |
| Notario | notario.demo@example.com | Demo2026! |

Estas cuentas se crean al registrarlas; no están preinstaladas. Si un correo ya existe, usa **Ingresar**. Nunca emplees datos o contraseñas reales. Persona y Empresa tienen su propio listado; Notario ve los trámites enviados por los solicitantes.

Los RUT/RUN de prueba se validan con módulo 11: el dígito puede ser un número de 0 a 9 o K, según el cuerpo del identificador. Por ejemplo, **22.312.422-4** pasa el cálculo y **22.312.422-K** no. Esto no verifica una identidad real. Después de intentar registrarse, los errores se actualizan al editar y muestran el dígito esperado.

## Flujo implementado

1. Registra Persona o Empresa y crea un trámite a partir de uno de tres formatos ficticios.
2. Completa antecedentes, consulta el documento de ejemplo y opcionalmente adjunta otros archivos.
3. Elige una representación de firma y confírmala. Envía el trámite mediante el modal de confirmación.
4. Cierra sesión e ingresa con un perfil Notario de prueba.
5. Revisa antecedentes y documentos. Devuelve con observaciones o finaliza la simulación.
6. Si hay observaciones, vuelve como solicitante, corrige y reenvía.
7. Finaliza como Notario y consulta el estado y el historial desde ambos perfiles.

Estados: **Borrador → En revisión → Con observaciones → En revisión → Finalizado**. También puede finalizarse directamente un trámite en revisión.

El repositorio verifica rol, propiedad del trámite, requisitos de envío y transiciones. Los cambios de estado y sus eventos se guardan en una misma transacción. Los documentos e historial se actualizan mediante Flow.

## Documentos y recurso nativo

**Adjuntar archivo ficticio** abre el selector nativo de Android mediante OpenDocument. Acepta PDF, TXT, PNG y JPG, hasta **5 MB por archivo** y **cinco documentos por trámite**, contando el formato incluido.

La aplicación copia el archivo elegido a su almacenamiento privado con un nombre interno aleatorio y conserva su referencia en Room. No modifica el original ni solicita permisos generales de almacenamiento. FileProvider permite abrir la copia en un visor instalado con permiso temporal de lectura. Si no existe un visor compatible, se muestra un aviso y el trámite se conserva.

Copia [docs/documento-ficticio.txt](docs/documento-ficticio.txt) al dispositivo para probarlo. Los formatos incluidos se consultan en un modal sin instalar un visor. Los archivos vacíos, los tipos no admitidos y los archivos que superan el límite se rechazan.

## Requisitos cubiertos en código

- Formulario y validaciones para tres perfiles; rechazo de contraseñas en blanco y correos duplicados.
- MVVM: pantallas, ViewModels, reglas de dominio, repositorios y DAOs separados.
- Room: usuarios, perfiles, trámites, documentos y eventos.
- Navegación entre acceso, registro, listado y detalle; confirmación al descartar ediciones.
- Imagen vectorial propia visible en la interfaz.
- Animaciones de estados, mensajes e historial.
- Modales de documentos, creación, envío, observaciones, finalización y cierre de sesión.
- Recurso nativo: selector de archivos y apertura mediante aplicaciones del dispositivo.
- Paleta verde del diseño de registro, tema oscuro y textos en español.

## Persistencia y migración

Las contraseñas se derivan con PBKDF2 y sal aleatoria por cuenta; nunca se guardan como texto. El perfil no contiene la contraseña. La sesión activa permanece en memoria y se solicita acceso al volver a abrir el proceso. Cuentas, documentos y eventos sobreviven al cierre de la aplicación.

La base **versión 2** migra desde la versión 1 del proyecto anterior. Conserva cuentas y borradores, asigna Persona a las cuentas antiguas y agrega un evento inicial a sus historiales. Los borradores anteriores sin documentos pueden completarse adjuntando un archivo ficticio. No se usa borrado destructivo.

Los respaldos están deshabilitados y la configuración de extracción excluye archivos y base de datos. Borrar los datos de la aplicación o desinstalarla elimina la información local.

## Documentación y pruebas

- [Arquitectura y guía de defensa](docs/arquitectura.md).
- [Casos de prueba y guion para el video](docs/pruebas.md).
- [Contexto del caso](docs/contexto.md).
- [Resultados de validación](docs/validacion.md).

Las pruebas locales usan Robolectric y no requieren emulador. Cubren reglas, ViewModels, cuentas, aislamiento, transiciones, historial, migración, archivos y un recorrido de interfaz con solicitante y notario. Las capturas quedan en **Notix/app/build/reports/ui/**.

## Evidencias del equipo

El equipo debe mantener su historial de commits, mockup y tarjetas de Trello, grabar el video y practicar la defensa. Estos entregables requieren su trabajo y no se sustituyen con el código del ZIP. La integración con firmas, e-token, certificados y autorización notarial es exclusivamente simulada.
