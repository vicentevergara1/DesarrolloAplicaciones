# Contexto del proyecto — NotariaX (DSY1105)

> Documento de enfoque. Se irá modificando a medida que se definan los flujos y los datos.
> Fuente: Formato de caso DSY1105 · Duoc UC · Escuela de Informática y Telecomunicaciones · CITT.

---

## 0. Identificación del caso

| Antecedente | Información |
|---|---|
| Desafío | Gestión y seguimiento móvil de trámites notariales |
| Organización | NotariaX |
| Rubro | Servicios de notaría digital en Chile |
| Sede / coordinador | CITT Duoc UC, Sede San Joaquín · Jorge Niochet |
| Asignatura | DSY1105 – Desarrollo de Aplicaciones Móviles |
| Entrega del caso al docente | 3 de septiembre de 2026 |

### La organización
NotariaX es una infraestructura digital que facilita la atención notarial mediante una plataforma en línea para personas naturales, empresas y notarios. Es una microempresa con más de **600 usuarios registrados** y una atención diaria de **1 a 5 usuarios**. Ofrece distintos tipos de trámites y mecanismos de firma, para facilitar la gestión documental y la interacción entre quien solicita un trámite y el notario que lo revisa o autoriza.

### Situación actual
La plataforma está operativa y tiene tres flujos generales:

1. **Firma electrónica simple:** firma manuscrita digital.
2. **Firma electrónica avanzada:** puede involucrar e-token, certificado digital o la adquisición de una firma avanzada.
3. **Firma notariada:** más de **60 formatos predefinidos**, o adjuntar documentos propios para autorización ante notario.

Algunas etapas requieren gestionar varios documentos, usar distintos mecanismos de firma o **cambiar de dispositivo** durante el proceso. Para el desarrollo académico se trabaja con un **flujo simplificado**: carga o selección de documentos → ingreso y verificación de antecedentes → representación de la firma → envío al notario → revisión → eventual devolución con observaciones → consulta del estado.

### Problema
Mejorar la experiencia móvil de gestión y seguimiento de trámites notariales digitales: facilitar la interacción cuando hay varios documentos, distintos tipos de firma y etapas de revisión, reducir errores de interacción y que las personas sepan con claridad el estado de sus solicitudes. Acciones como el uso de e-token mantienen dependencia del computador; el desafío **no busca eliminar todas las restricciones tecnológicas actuales**, sino validar con un MVP una experiencia más simple, clara y continua para lo que se pueda representar dentro del alcance.

---

## 1. ¿Qué es esto?

Un **MVP académico** que representa el ciclo básico de un **trámite notarial digital** para NotariaX: carga y gestión de documentos, revisión de antecedentes, representación de la firma, intercambio de observaciones y consulta del estado del trámite.

**No es** una solución productiva, no se publica en tiendas y **no debe usarse para realizar, validar o autorizar trámites notariales reales**.

## 2. Objetivo

Proponer y validar una experiencia **móvil más simple, clara y continua** para las etapas del trámite que puedan representarse dentro del alcance de la asignatura, reduciendo errores de interacción y permitiendo que las personas conozcan con claridad el estado de sus solicitudes.

## 3. Alcance

### Dentro del alcance
- Un **flujo principal completo** (cada equipo lo prioriza), con las etapas que el equipo incorpore: envío → revisión → observación → corrección → finalización.
- Solo los **perfiles necesarios** para demostrar ese flujo.
- Uso de **datos ficticios, sintéticos, públicos o anonimizados**.
- Integraciones representadas mediante **mocks / APIs de prueba / servicios simulados**.
- Casos de prueba y documentación suficiente para que el MVP pueda continuarse después.

### Fuera del alcance
- Trámites notariales reales.
- Integración real con proveedores de firma electrónica avanzada, e-token, certificados digitales o sistemas de NotariaX.
- Publicación en tiendas de aplicaciones.
- Acceso a sistemas internos, bases de datos, infraestructura o servicios productivos de NotariaX.

> Cualquier funcionalidad adicional debe justificarse por su aporte al desafío y mantenerse dentro del alcance del semestre.

## 4. Perfiles de usuario

Perfiles de referencia (se priorizan solo los necesarios para el flujo elegido):

| Perfil | Rol en el flujo |
|---|---|
| Persona natural | Inicia el trámite, carga documentos, corrige observaciones, consulta estado |
| Representante de persona jurídica / empresa | Igual que el anterior, en nombre de una empresa |
| Notario | Revisa antecedentes y documentos, acepta/finaliza o devuelve con observaciones |
| Administrador | Gestión general (catálogos, usuarios, etc.) — a definir si entra al MVP |

## 5. Requisitos funcionales (RF)

| ID | Requisito |
|---|---|
| RF-01 | Cargar uno o varios documentos ficticios, o seleccionar un formato predeterminado |
| RF-02 | Completar o verificar la información necesaria para representar un trámite |
| RF-03 | Seleccionar o representar el tipo de firma requerido dentro del escenario de prueba |
| RF-04 | Enviar la documentación al notario para su revisión |
| RF-05 | Revisar antecedentes y documentos asociados al trámite (rol notario) |
| RF-06 | Aceptar/finalizar el trámite simulado, o devolverlo con observaciones para corrección |
| RF-07 | Visualizar observaciones y permitir la corrección y reenvío |
| RF-08 | Consultar el estado de trámites y documentos |
| RF-09 | Mantener trazabilidad básica: historial de cambios de estado con fecha/hora simuladas y rol responsable |
| RF-10 | Simular, cuando corresponda, mecanismos externos (e-token, firma electrónica avanzada) |
| RF-11 | (Opcional) Simular validación biométrica del dispositivo (huella / reconocimiento facial), sin capturar datos reales |
| RF-12 | (Opcional) Notificaciones cuando aporten valor al flujo (ej. cambio de estado) |

## 6. Requisitos no funcionales (RNF)

| ID | Requisito |
|---|---|
| RNF-01 | **Plataforma:** uso principal desde dispositivos móviles **Android**, en contexto académico y de demostración |
| RNF-02 | **Usabilidad:** interfaz clara, legible, con navegación simple y fácil de usar desde un teléfono |
| RNF-03 | **Claridad de estado:** el usuario debe poder saber en todo momento en qué estado está su solicitud |
| RNF-04 | **Prevención de errores:** reducir omisiones y errores de interacción en los escenarios de prueba |
| RNF-05 | **Datos:** exclusivamente ficticios, sintéticos, públicos o anonimizados |
| RNF-06 | **Privacidad/seguridad:** sin RUT, nombres, teléfonos, domicilios, firmas, biometría, certificados, credenciales, tokens ni respaldos reales |
| RNF-07 | **Biometría:** solo simulada; no se captura ni almacena ningún dato biométrico real |
| RNF-08 | **Integraciones:** mediante mocks / APIs de prueba; sin credenciales, llaves ni accesos productivos |
| RNF-09 | **Conectividad:** puede usar internet para comunicarse con servicios de prueba |
| RNF-10 | **Trazabilidad:** el historial debe ser suficiente para entender la evolución de un trámite |
| RNF-11 | **Mantenibilidad / continuidad:** documentación suficiente para que otro equipo pueda continuar el MVP |
| RNF-12 | **Tecnologías:** ajustadas a las definidas para DSY1105 |
| RNF-13 | **Demostrabilidad:** funcionamiento verificable mediante casos de prueba |

## 7. Modelo de información (datos del trámite)

Elementos que el MVP puede manejar para representar el proceso:

- Identificador interno del trámite
- Tipo o categoría de trámite
- Formato o documento asociado
- Uno o varios documentos ficticios de prueba
- Tipo de firma requerida
- Estado del trámite
- Fecha y hora simuladas
- Rol participante o responsable
- Antecedentes necesarios para completar el flujo
- Observaciones de revisión o corrección
- Historial básico de cambios de estado

## 7.1 Flujos de firma de NotariaX (referencia para modelar)

| Flujo | Descripción | En el MVP |
|---|---|---|
| Firma electrónica simple | Firma manuscrita digital | Fácil de implementar de verdad (trazo en pantalla con datos ficticios) |
| Firma electrónica avanzada | e-token, certificado digital o adquisición de firma avanzada | Simulada con mock |
| Firma notariada | +60 formatos predefinidos o documento propio, autorizado ante notario | Flujo de revisión del notario (aceptar / devolver con observaciones) |

> Cada equipo elige cuál(es) representar. Hay que demostrar **un flujo principal completo**.

---

## 8. Integraciones (todas simuladas)

- **Firma electrónica avanzada (API de proveedor):** necesidad futura de NotariaX → mock.
- **e-token:** depende del computador en la realidad → se representa por simulación.
- **Certificados digitales:** simulados.
- **Biometría del dispositivo:** simulada.
- **Servicios de otras empresas:** posibilidad futura, no se implementa.

## 9. Criterios de éxito

- El MVP representa de forma comprensible un flujo principal de gestión de trámites notariales digitales.
- Se puede cargar o seleccionar documentación ficticia y consultar el estado del trámite.
- Se representan las etapas de envío, revisión, observación, corrección y finalización incorporadas por el equipo.
- Existe trazabilidad básica para entender la evolución de un trámite.
- Disminuyen las omisiones y errores de interacción en los escenarios de prueba.
- La experiencia móvil es clara, legible y coherente con los perfiles considerados.
- Las dependencias externas o firmas avanzadas se representan de manera comprensible mediante simulaciones.
- El equipo demuestra el funcionamiento con casos de prueba y entrega documentación suficiente.

## 10. Material de apoyo (pendiente de entrega por la notaría / docente)

| Material | Para qué se usa | Estado |
|---|---|---|
| Descripción conceptual del flujo de trámites notariales digitales | Comprender contexto, perfiles y etapas generales | ☐ Depurado |
| Catálogo ficticio de tipos de trámite, estados y tipos de firma | Modelar formularios, reglas y flujos del MVP | ☐ Depurado |
| Documentos y registros sintéticos de ejemplo | Pruebas, demostraciones y validación funcional | ☐ Depurado |

## 11. Pendientes antes de construir

- [ ] Obtener el material de apoyo de la sección 10
- [ ] Definir el **flujo principal** que se va a demostrar (¿qué tipo de firma: simple, avanzada o notariada?)
- [ ] Elegir un subconjunto pequeño de los +60 formatos predefinidos para el catálogo ficticio
- [ ] Definir qué perfiles entran al MVP
- [ ] Definir catálogo de tipos de trámite, estados y tipos de firma
- [ ] Diagramar flujos (estados y transiciones del trámite)
- [ ] Decidir qué integraciones se simulan y cómo
- [ ] Confirmar tecnologías permitidas en DSY1105
- [ ] Redactar casos de prueba

## 12. Notas / decisiones

_(Espacio para ir anotando decisiones del equipo.)_
