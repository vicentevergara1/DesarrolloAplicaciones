# Validación de la entrega

Validación ejecutada el 22 de octubre de 2026 en el entorno de nube.

## Comando

Desde `Notix/`, con JDK 21 y Android SDK 37.0:

```sh
./gradlew --console=plain --max-workers=2 :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

## Resultados

- Compilación de depuración: **correcta**; APK generado.
- Pruebas: **14 ejecutadas, 14 correctas, 0 omitidas**.
- Validación del formulario: 5 pruebas.
- Estados de autenticación: 3 pruebas.
- Carga, errores y creación de borradores: 3 pruebas.
- Room: 2 pruebas, incluyendo reapertura de la base, acceso, correo duplicado y aislamiento de usuarios.
- Interfaz Compose: 1 recorrido de registro, creación de borrador, cierre de sesión y nuevo acceso.
- Lint: **sin errores**. Conserva advertencias sobre el target SDK, versiones más recientes de dos dependencias y recomendaciones de configuración de respaldo.

Las pruebas de Android se ejecutaron localmente con Robolectric/API 28. Las capturas del recorrido quedan en `Notix/app/build/reports/ui/`. No se ejecutó en un teléfono ni en un emulador del SDK.

El APK es de depuración. El alcance validado comprende acceso y borradores locales; la revisión notarial y el recurso nativo permanecen pendientes, como indica el README.
