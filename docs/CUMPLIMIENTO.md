# Revisión de requisitos — 19 de octubre de 2026

Se revisaron el libro `Chequeo de Requerimientos.xlsx` y los siete PDF de la carpeta superior.
Los tres archivos `Proyecto E4 V2*.pdf` son idénticos entre sí; los dos `Laboratorio 4 R*.pdf` también.
El contenido único se corresponde con entregas 1–4 del Excel, Swagger, cargue de personas y RabbitMQ.

**Excepción solicitada por el usuario:** Google Maps se sustituye por las alternativas gratuitas existentes:
Nominatim para geocodificar y Leaflet/OpenStreetMap para visualizar. No se leen claves de Google ni se invocan sus servicios.
La [política de Nominatim](https://operations.osmfoundation.org/policies/nominatim/) limita los trabajos periódicos a cuatro consultas por minuto, un solo flujo y caché; estas condiciones se aplican en el cliente y en la caché de BD.

## Trazabilidad

| Fuente y requisito | Implementación / corrección | Verificación |
|---|---|---|
| E1 BD: vehículos, documentos y relación | `database/script.sql`, `VehiculoRepository`, `DocumentoRepository` | Esquema creado en servidor aislado; CRUD HTTP |
| E1 BD: CHECK de tipo vehículo, servicio y combustible | CHECK de `vehiculos` y validación del servicio | Rechazo de valores fuera del catálogo; SQL de restricciones |
| E1 BD: CHECK aplicabilidad y obligatoriedad | CHECK de `documentos`; validación canónica | CRUD documentos y rechazo SQL |
| E1 BD: estado del documento | CHECK de `vehiculo_documento` | Creación fuerza `En Verificacion` |
| E1 API: CRUD vehículo y mínimo un documento | `VehiculoService` y controlador; operación transaccional | Rechazo sin documentos; creación, consulta, actualización y eliminación |
| E1 API: CRUD documentos paramétricos | `DocumentoService` y controlador | POST, GET, PUT, DELETE reales |
| E1 API: placa, tipo vehículo, tipo documento y estado | Consultas de `VehiculoRepository` | Cuatro consultas HTTP |
| E1 API: agregar documentos | `agregarDocumentos`; evita duplicados e incompatibilidad | Usa validación común con creación y cargue PDF; pruebas de creación/cargue |
| E2 BD: persona, usuario y conductor–vehículo | Tablas, FK, CHECK, usuario/login únicos | Creación de administrativo, asociación y cambios de estado |
| E2 BD: archivos Base64 como BLOB | Licencia y documento PDF | Cargue PDF válido y rechazo de contenido incorrecto |
| E2 BD: identificación, tipo persona y PO/EA/RO | CHECK en SQL y normalización en servicios | Revisión del esquema y pruebas de creación/cambio |
| E2 API: crear/consultar/modificar persona | `PersonaServiceImpl`, `PersonaController` | Alta administrativa; consultas; modificación transaccional revisada |
| E2 API: administrativo siempre con usuario | Alta y conversión a A en la misma transacción | Credenciales generadas y usuario persistido |
| E2 API: login nemotécnico, password y APIKey automáticos | Inicial del primer nombre + primer apellido; sin tildes; sufijo si colisión | Caso Álvaro Ñúñez → `anunez`; contraseña de 12 caracteres |
| E2 API: cambiar password y regenerar APIKey | `UsuarioGestionService`, DTO de entrada documentados | Ambos endpoints HTTP |
| E2 API: cargar/actualizar varios PDF | Lista de documentos en una transacción; firma `%PDF-` y Base64 estricto | PDF actualizado; archivo incorrecto rechazado |
| E2 API: asociar vehículos y cambiar PO/EA/RO | `ConductorService`; estado inicial EA | Asociación y cambio a PO; controles de tipo/licencia |
| E2 API: cinco consultas públicas | `PublicoController` | Las cinco responden sin JWT |
| E2 API: asegurar servicios de E1 con token y APIKey | JWT válido y APIKey del mismo usuario en vehículos/documentos | Sin token, sin APIKey y token malformado/firma inválida rechazados |
| E3 BD: trayecto condicionado a documentos y conductor PO | Validación consistente en API y procedimientos, incluye vigencia actual y ausencia de documentos | Rechazo de documento vencido y vehículo sin documentos |
| E3 BD: fecha licencia y archivo BLOB | Campos de `Persona` y tabla | Mapeo revisado; tarea de vencimiento comprobada |
| E3 API: ruta ordenada protegida | `RutaRepository.trayectosDeRuta`, `RutaServiceImpl` | Consulta de parada con orden 0 y JWT |
| E3 API: rutas por conductor, agrupadas | SELECT DISTINCT; DTO de códigos | Endpoint real |
| E3 API: rutas/conductores por vehículo agrupados | SELECT DISTINCT y agrupación por ruta | Endpoint real |
| E3 API: rutas restringidas | Consulta por documento no habilitado/vencido, ausencia de documentos o conductor RO | Ruta restringida después de vencer un documento |
| E3 técnica: licencias cada 2 min → RO | `TareasProgramadasEntrega3.licenciasVencidas` | Invocación con fecha vencida; anotación 120000 ms |
| E3 técnica: documentos cada 2 min → Vencido | `documentosVencidos` | Estado actualizado en BD |
| E3 técnica: coordenadas cada 90 s persistidas | `coordenadasTrayectos`, Nominatim, caché BD | Respuesta del proveedor simulada; valores persistidos; cliente HTTP contra servidor local |
| E3 interfaz: paradas de una ruta | `static/rutas.html`, tabla ordenada y mapa Leaflet existente | Recurso HTTP accesible; consultas de rutas verificadas |
| E4 BD: temporal con estado, observación e idCargue | `trayecto_tmp`, CHECK, campos de origen como texto | Estado inválido rechazado; metadatos Excel conservados |
| E4: idCargue numérico año/mes/día/hora/minuto | Reserva atómica de minuto real en `cargue_archivo` | Segundo archivo del mismo tipo/minuto devuelve 409 sin mezclarse |
| E4 BD: procedimiento de cargue sin alterar datos | `CARGAR_TRAYECTOS_EXCEL`, invocado por JPA | Espacios y edad `025` permanecen intactos en temporal |
| E4 BD: procedimiento de validación acumulativa | `VALIDAR_TRAYECTOS_CARGUE`, una sola definición | Una fila acumula ruta, orden, ubicación, vehículo y conductor; duplicados `1`/`01` |
| E4 BD: procedimiento de procesado | `PROCESAR_TRAYECTOS_EXCEL`; solo validados; revalidación e idempotencia | Cargue mixto 1 procesado/1 error; repetir procesado no duplica |
| E4 API: Excel y tres procedimientos | `TrayectoCargueServiceImpl`, `TrayectoTmpRepository` | Cargue completo y fallo forzado que revierte temporal, definitivo y reserva |
| E4 API: consulta por idCargue | `consultarCargue` con paginación y totales | GET real del id devuelto |
| PDF Swagger: accesos sin token, servicios y schemas | OpenAPI, anotaciones existentes, DTO de gestión y mensajería | `/api-docs` sin token; seguridad E1 documenta JWT + APIKey |
| PDF complementario: persona_tmp y tres procedimientos | Tabla y procedimientos faltantes añadidos; JPA existente conectado | Persona válida procesada, edad no numérica y menor de 18 rechazadas; errores acumulados |
| PDF investigativo: productor y consumidor API RabbitMQ | `MensajeriaController`, `MensajeriaService`, cola durable, mensajes persistentes y confirmación | HTTP 202 con consumidor detenido; monitoreo informa pendiente; consumidor reanudado lo guarda |
| PDF investigativo: monitoreo y actores independientes | Estado de cola, historial, control de consumidor, configuración por instancia | Productor opera sin consumidor; consumidor drena sin nuevas publicaciones |

## Decisiones de compatibilidad

- Se mantienen los catálogos de valores y la nemotecnia ya establecidos en el proyecto. El Excel no detalla otros valores ni otra fórmula de login.
- Se mantiene el orden inicial 0 de la aplicación y sus ejemplos; los documentos entregados no exigen que comience en 1.
- La plantilla mínima de trayectos conserva sus cinco columnas. Nombre y edad, añadidos previamente al proyecto, son opcionales y ahora se guardan en la temporal en lugar de ignorarse.
- El Excel complementario solo aporta nombre, edad y ubicación. Esos registros de persona quedan sin clasificación; no se generan identidades ni correos ficticios. La API de personas continúa exigiendo esos datos para registrar conductores y administrativos.
- Con identificador exactamente `yyyyMMddHHmm` no se pueden distinguir dos archivos del mismo tipo recibidos en el mismo minuto. Se responde 409 al segundo; no se le asigna una hora futura inventada.

## Resultados y ejecución

`bash scripts/verificar.sh` crea un servidor MariaDB aislado con datos desechables y una cola RabbitMQ de prueba exclusiva.
Ejecuta 21 pruebas (15 de integración y 6 unitarias), con JDBC/JPA, MockMvc, procedimientos reales y un broker RabbitMQ real.
El cliente de Nominatim se prueba contra un servidor HTTP local; no se envían direcciones a terceros durante las pruebas.

La migración `database/migracion_requisitos.sql` también se probó sobre el SQL previo: conserva las 4 personas, 3 vehículos y 2 trayectos iniciales, y deja los 6 procedimientos requeridos.
Se comprobó su ejecución repetida. No contiene `DROP DATABASE` ni elimina los datos de negocio.

La validación del motor se realizó con **MariaDB**, el motor disponible localmente. Se agregó el conector MySQL y se evitó fijar un dialecto incompatible; el esquema y la migración están escritos para MySQL 8.0.16+ y MariaDB 10.6+. No se declara una ejecución contra un servidor MySQL que no se haya realizado.
La interfaz se renovó en tema claro. Se verificó con Chromium: login, consulta, descarga de plantilla y ejemplo, importación real de tres filas, consulta de la ruta creada, rechazo de formato, paginación, cierre de sesión y vista móvil de 390 px sin desbordamiento horizontal.

Para una instalación existente, aplicar la migración y reiniciar la aplicación con el nuevo JAR. El script inicial `database/script.sql` sigue siendo exclusivamente para instalaciones nuevas: recrea la base de datos.

## Actualización de Excel e interfaz

Se generaron dos archivos con la misma clase utilizada por los endpoints web: una plantilla vacía y un ejemplo con tres paradas.
Incluyen guía, encabezados inmovilizados, identificaciones como texto, comentarios de ayuda y validaciones de Excel.
La prueba de integración adicional descarga ambos y carga el ejemplo en la base desechable: tres trayectos procesados y cero errores.

La instancia local de `http://localhost:9001` fue actualizada al nuevo JAR. Antes de la migración se guardó una copia de seguridad en
`/home/rouu/Backups/Laboratorio4/antes_excel_ui_20261019.sql`. Se conservaron las 4 personas, 3 vehículos y 2 trayectos existentes.
Se verificaron en esa instancia el HTML, Swagger, autenticación y ambas descargas Excel. Los datos de demostración se importaron únicamente en la base de pruebas.

## Corrección de endpoints y Swagger — 20 de octubre de 2026

Se contrastaron los controladores y servicios de las cuatro entregas con el Excel original.
La revisión conserva las funcionalidades y la alternativa de geocodificación que ya tenía el proyecto.

| Requisito / flujo | Corrección |
|---|---|
| E2: POST, GET y PUT de personas | También disponibles en `/LaboratorioV2`; se mantienen los alias de V1. Una persona inexistente responde 404. |
| E2: cinco consultas públicas | Un JWT vencido o inválido enviado por el cliente no impide utilizar estos servicios públicos. |
| E1: aplicabilidad de documentos | PUT rechaza cambiar el tipo de documento si excluye vehículos que ya lo tienen asociado. Renombrar el código actualiza también la relación. |
| Flujo E1–E3: documentos inicialmente En Verificacion | Nuevo PUT `/LaboratorioV2/vehiculos/{placa}/documentos/{idDocumento}/estado`. Requiere JWT; acepta Habilitado, Vencido o En Verificacion y rechaza habilitar documentos vencidos. Esta operación complementa el Excel para completar el flujo desde la API. |
| E3: consulta de restricciones | La licencia vencida se refleja como RO inmediatamente, incluso antes de la tarea periódica. La tarea sigue persistiendo el estado. |
| Contratos HTTP | Parámetros, encabezados y archivos faltantes tienen errores específicos; formatos de parámetros inválidos responden 400. |
| Swagger | Cinco grupos: todos y una vista por entrega; JWT + APIKey en E1 también en la documentación agrupada; consultas públicas sin candado. Catálogos con opciones explícitas, guía de autenticación y archivo multipart binario. |
| Cliente de prueba | Postman actualizado con personas en V2 y verificación de documentos. |

**Verificación de esta sesión:** 19 pruebas aprobadas: 9 de contratos HTTP, seguridad y Swagger con
MockMvc, 5 de servicios y 5 pruebas previas de Excel, PDF y caché. Se utilizaron repositorios simulados
para las pruebas de servicios y controladores; no se afirma una ejecución de SQL real en esta sesión.
Los cinco documentos OpenAPI y el selector de Swagger se generaron y comprobaron con MockMvc.

La suite completa `scripts/verificar.sh` no pudo iniciar: el entorno rechaza crear sockets con
`Operation not permitted`. Esto impide levantar MariaDB desechable, conectar RabbitMQ y ejecutar
la prueba HTTP local del proveedor. Se añadieron tres regresiones de integración para el ciclo
vehículo → verificación → conductor PO → trayecto, la actualización de documentos en uso y la
consulta de licencias vencidas. Compilan y quedan pendientes de ejecución con esos servicios disponibles.
No se aplicaron cambios a una base de datos en ejecución ni se reinició la instancia local.
