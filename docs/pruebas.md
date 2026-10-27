# Casos de prueba y guion de demostración

Usa únicamente ejemplos ficticios y un dispositivo API 24 o superior. Prepara Persona y Notario en la misma instalación.

| Caso | Acción | Resultado esperado |
|---|---|---|
| Campos vacíos | Registra sin completar datos | Errores por campo |
| Contraseña en blanco | Usa ocho espacios en ambos campos | Rechazo explícito |
| Tres perfiles | Cambia tipo y completa el ejemplo | Campos independientes y ejemplos válidos |
| RUT de empresa | Prueba 76.111.111-K y 76.111.111-6 | Rechaza el primero; acepta el segundo |
| Correo duplicado | Repite correo variando mayúsculas | Solicita iniciar sesión |
| Acceso incorrecto | Usa otra contraseña | Mensaje de error |
| Crear trámite | Elige un formato | Borrador, documento e historial inicial |
| Documento incluido | Pulsa Ver documento | Modal con texto ficticio |
| Recurso nativo | Adjunta documento-ficticio.txt | Copia local asociada |
| Archivo inválido | Vacío, más de 5 MB o tipo no admitido | Rechazo sin copia incompleta |
| Envío incompleto | Omite descripción o confirmación | Errores; sigue en Borrador |
| Firma avanzada | Selecciona opción de prueba | Explica el mock |
| Envío | Completa y confirma | En revisión y evento |
| Bandeja | Ingresa como Notario | Muestra enviados y oculta borradores |
| Observación | Devuelve con nota | Con observaciones; nota visible al dueño |
| Corrección | Modifica y reenvía | En revisión y evento de reenvío |
| Finalización | Confirma como Notario | Finalizado con historial |
| Aislamiento | Usa otra Persona o Empresa | No muestra trámites ajenos |
| Persistencia | Cierra proceso y vuelve a acceder | Conserva cuentas y trámites |
| Rotación | Rota formulario o detalle | Conserva estado |
| Cambios pendientes | Edita y vuelve atrás | Confirma antes de descartar |
| Visor | Abre copia adjunta | Abre visor o informa si no hay uno |

## Guion breve para el video

1. Explica que es una demostración con datos ficticios.
2. Muestra un error y registra una Persona.
3. Crea un trámite, revisa el formato y adjunta el archivo mediante el selector nativo.
4. Escribe antecedentes, representa la firma y confirma el envío.
5. Ingresa como Notario y devuelve con observaciones.
6. Ingresa como solicitante, corrige y reenvía.
7. Finaliza como Notario y muestra estado e historial.
8. Reabre la aplicación para demostrar persistencia.

El equipo debe grabar este recorrido en un dispositivo, y relacionarlo con las tarjetas de Trello y el mockup.
