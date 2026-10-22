# Laboratorio 4 + Entregas 3 y 4 del proyecto (Spring Boot · MySQL · JPA · Maven)

 mysql -u ppooii -p -e "SELECT login, password, apikey FROM ppooii.usuario;"

 
Contiene: **(1)** documentación Swagger/OpenAPI de todos los servicios (Lab 4), **(2)** cargue masivo de personas desde Excel
(Lab 4 complementario) y **(3)** cargue masivo de trayectos desde Excel con tres procedimientos almacenados (Entrega 4).
La prueba de concepto RabbitMQ está integrada en esta API: productor HTTP, consumidor independiente, cola durable y monitoreo.

## Puesta en marcha
1. Ejecutar en MariaDB/MySQL **un solo script**: `database/script.sql` (crea la base `ppooii` desde cero —incluye `DROP DATABASE`—,
   todas las tablas, restricciones CHECK, los 6 procedimientos almacenados y datos de prueba).
   Con el cliente `mysql`: `mysql -u root -p < database/script.sql`.
2. Revisar usuario/clave en `src/main/resources/application.properties` o definir `DB_URL`, `DB_USER` y `DB_PASSWORD`.
   MySQL 8: `DB_URL=jdbc:mysql://localhost:3306/ppooii`; MariaDB: `jdbc:mariadb://localhost:3306/ppooii`.
   Para actualizar una instalación existente conservando datos usar **`database/migracion_requisitos.sql`**, en lugar del script inicial.
3. Las coordenadas usan **Nominatim / OpenStreetMap**, sin Google ni claves de pago. El mapa usa Leaflet.
   Se conserva caché en la BD y un máximo de cuatro consultas por minuto entre todas las tareas.
   Proveedor configurable mediante `GEOCODING_URL`; véase la [política de Nominatim](https://operations.osmfoundation.org/policies/nominatim/).
   Para trabajos periódicos esa política exige caché, un solo flujo y cuatro consultas por minuto.
4. `mvn spring-boot:run` (puerto **9001**).

Usuarios de prueba (login / password / APIKey): `jperez / conductor123 / api_key_12345` y `mgomez / admin123 / api_key_67890`.
Para obtener el token: `POST /authenticate` con header `APIkey` y body `{"username":"mgomez","password":"admin123"}`.

## Entrega 1 – Vehículos y documentos (JWT y APIKey)  `/LaboratorioV1`
| Método | URL | Descripción |
|---|---|---|
| POST/GET/PUT/DELETE | `/vehiculo`, `/vehiculo/{id}`, `/vehiculos` | CRUD de vehículo. Al crear exige ≥ 1 documento; todos quedan `En Verificacion` |
| POST/GET/PUT/DELETE | `/documento`, `/documento/{id}`, `/documentos` | CRUD de la entidad paramétrica de documentos |
| GET | `/vehiculo/placa/{placa}` | Buscar por placa |
| GET | `/vehiculos/tipo/{tipo}` | Buscar por tipo de vehículo (Automovil / Motocicleta) |
| GET | `/vehiculos/documento/{codigoDocumento}` | Vehículos que tienen en común un tipo de documento |
| GET | `/vehiculos/estado-documento/{estado}` | Por estado del documento: Habilitado, Vencido, En Verificacion |
| POST | `/vehiculo/{id}/documentos` | Agregar documentos a un vehículo |

## Entrega 2 – Personas, usuarios, conductores  `/LaboratorioV2` (con alias antiguos en `/LaboratorioV1`)
| Método | URL | Token | Descripción |
|---|---|---|---|
| POST / PUT / GET | `/LaboratorioV2/persona`, `/LaboratorioV2/personas`, `/LaboratorioV2/persona/id/{id}` | Sí | Persona; si es ADMINISTRATIVO crea usuario con login (inicial nombre + primer apellido), password y APIKey automáticos |
| PUT | `/LaboratorioV2/usuarios/{login}/password` | Sí | Cambia la contraseña (body `{"password": "..."}`) |
| GET | `/LaboratorioV2/usuarios/{login}/apikey` | Sí | Regenera la APIKey |
| POST | `/LaboratorioV2/vehiculos/{placa}/documentos` | Sí | Carga/actualiza uno o varios PDF en Base64 |
| PUT | `/LaboratorioV2/vehiculos/{placa}/documentos/{idDocumento}/estado` | Sí | Verifica el documento: Habilitado, Vencido o En Verificacion; no permite habilitar uno vencido |
| POST | `/LaboratorioV2/conductores/{identificacion}/vehiculos` | Sí | Asocia vehículos a un conductor (estado inicial EA) |
| PUT | `/LaboratorioV2/conductores/{identificacion}/vehiculos/{placa}/estado` | Sí | Cambia el estado PO / EA / RO |
| GET | `/LaboratorioV2/publico/vehiculos/documentos-vencidos` | **No** | Vehículos con documentos vencidos |
| GET | `/LaboratorioV2/publico/conductores/pueden-operar` | **No** | Conductores con estado PO |
| GET | `/LaboratorioV2/publico/vehiculos/placa/{placa}` | **No** | Vehículo con conductores y documentos |
| GET | `/LaboratorioV2/publico/vehiculos/documentos-por-vencer?dias=N` | **No** | Documentos por vencer en N días |
| GET | `/LaboratorioV2/publico/personas/total-por-tipo` | **No** | Total de personas por tipo |

Los servicios de vehículos y documentos de la Entrega 1 requieren **Authorization: Bearer TOKEN** y **APIkey** del mismo usuario. El login requiere contraseña y APIKey.
Archivos (licencia y PDF de documentos): viajan en BASE64 en el JSON y se guardan como BLOB.

## Swagger (sin token)

Selector por entrega: `00-todos`, `01-vehiculos-documentos`, `02-personas-usuarios-conductores`,
`03-rutas-trayectos` y `04-cargue-excel`. Cada definición se consulta en `/api-docs/{grupo}`.
Los grupos muestran los requisitos de seguridad efectivos y ejemplos de los catálogos.

* Interfaz: http://localhost:9001/swagger-ui.html  ·  JSON: http://localhost:9001/api-docs
* Botón **Authorize** → pegar el token sin el prefijo `Bearer` y la APIKey. La respuesta de `/authenticate` conserva el prefijo para compatibilidad; Postman lo elimina al guardar la variable.

## Servicios nuevos
| Método | URL | Token | Descripción |
|---|---|---|---|
| POST | `/LaboratorioV3/upload-excel` | No | Cargue de personas (`file` = Excel NOMBRE, EDAD, UBICACIÓN) |
| POST | `/LaboratorioV4/trayectos/cargue` | Sí | Cargue de trayectos (plantilla CIPAS). Devuelve `idCargue` y totales por estado |
| GET | `/LaboratorioV4/trayectos/cargue/{idCargue}` | Sí | Consulta de un cargue (registros, estado, observaciones, totales) |
| GET | `/LaboratorioV4/trayectos/plantilla` | Sí | Descarga la plantilla Excel |

Postman: `docs/postman/Laboratorio4_Entrega4.postman_collection.json`. Plantillas: `docs/plantillas/`.

## Entrega 3 (rutas, tareas programadas y front-end)
| Método | URL | Descripción |
|---|---|---|
| GET | `/LaboratorioV3/rutas/{codigoRuta}` | Trayectos de la ruta, en orden, con conductor, estado PO/EA/RO y si el vehículo está habilitado |
| GET | `/LaboratorioV3/rutas/conductor/{identificacion}` | Solo los códigos de ruta del conductor, agrupados |
| GET | `/LaboratorioV3/rutas/vehiculo/{placa}` | Código de ruta y conductores del vehículo, agrupados |
| GET | `/LaboratorioV3/rutas/restricciones` | Rutas/trayectos con vehículo NO habilitado o conductor RO (con motivo) |
| POST | `/LaboratorioV3/trayectos` | Crea un trayecto solo si documentos HABILITADO y conductor PO |

Todos requieren token. **Front-end:** http://localhost:9001/rutas.html (login con usuario, contraseña y APIKey; consulta una ruta, tabla de paradas y mapa Leaflet/OpenStreetMap; botón de rutas con restricciones).

Tareas programadas (`Component/TareasProgramadasEntrega3.java`, intervalos configurables en `application.properties`):
licencias vencidas → conductor `RO` (cada 2 min) · documentos con vigencia vencida → `VENCIDO` (cada 2 min) · trayectos sin latitud/longitud → Nominatim / OpenStreetMap y se guardan (cada 90 s).
`Persona` incluye `identificacion`, `tipoPersona`, `fechaLicencia` y `licencia` (BLOB; el JSON la envía/recibe en BASE64).

## Flujo del cargue de trayectos
`Excel → CARGAR_TRAYECTOS_EXCEL (trayecto_tmp, CARGADO) → VALIDAR_TRAYECTOS_CARGUE (VALIDADO / ERROR + observación acumulativa) → PROCESAR_TRAYECTOS_EXCEL (trayecto, PROCESADO)`

Reglas de validación: campos obligatorios · orden entero entre 0 y 99999 · vehículo existente con **todos** sus documentos HABILITADO ·
conductor existente, tipo CONDUCTOR y estado **PO** en su relación con el vehículo · sin duplicados (ruta + orden) en el archivo ni en `trayecto`.

## Modelo de datos
Tablas: `persona`, `usuario`, `vehiculos`, `documentos`, `vehiculo_documento`, `vehiculo_conductor`, `trayecto`, `trayecto_tmp`, `persona_tmp`, `coordenadas`.
CHECK: tipo de vehículo / servicio / combustible, tipo y obligatoriedad de documentos, estado del documento del vehículo
(`Habilitado`, `Vencido`, `En Verificacion`), tipo de identificación y tipo de persona (`C`/`A`), estado del conductor (`PO`/`EA`/`RO`) y estado de `trayecto_tmp`.
Interpretaciones asumidas: `tipos_vehiculo` A = automóvil, M = moto, AM = ambos; `obligatoriedad` RA / RM / RR = obligatorio para automóvil / moto / ambos.

## Cargues y compatibilidad

La plantilla de trayectos requiere las cinco primeras columnas: `CODIGO_RUTA`, `ORDEN`, `UBICACION`, `IDENTIFICACION_CONDUCTOR`, `PLACA`.
`NOMBRE_CONDUCTOR` y `EDAD_CONDUCTOR` son columnas adicionales opcionales; se conservan sin alteración en `trayecto_tmp` y en la consulta del cargue.
Las plantillas antiguas de cinco columnas siguen siendo válidas. Se conserva el orden inicial **0** del proyecto existente.
Los errores se acumulan por fila y solo se procesan las válidas. Si falla un procedimiento se revierte todo el archivo y se devuelve HTTP 500.

`idCargue` representa el minuto **real** (`yyyyMMddHHmm`). Para evitar mezclar archivos, un segundo cargue del mismo tipo en ese minuto devuelve HTTP 409; reintentar al minuto siguiente.
El Excel complementario solo contiene nombre, edad y ubicación: sus personas se guardan sin clasificación, identificación ni usuario inventados.
La creación de una persona administrativa mediante la API sigue generando obligatoriamente su usuario en la misma transacción.

## RabbitMQ (PDF del proyecto investigativo)

Iniciar RabbitMQ en `localhost:5672`, o configurar `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`.
La cola `ppooii.mensajes` es durable y los mensajes persistentes. La publicación espera confirmación del broker;
la recepción se confirma al guardar el mensaje en BD, con identificador único para evitar duplicados por reentrega.
Los servicios requieren JWT:

| Método | URL | Entrada o resultado |
|---|---|---|
| POST | `/LaboratorioV4/mensajes` | `{"contenido":"Prueba"}`; HTTP 202 confirmado por RabbitMQ |
| GET | `/LaboratorioV4/mensajes/estado` | Cola, pendientes y consumidores activos |
| PUT | `/LaboratorioV4/mensajes/consumidor` | `{"activo":false}` detiene; `true` reanuda |
| GET | `/LaboratorioV4/mensajes/recibidos` | Últimos 100 mensajes guardados |

Demostración reproducible en Postman: detener consumidor → publicar → verificar pendientes → reanudar → consultar recibidos.
Mientras el consumidor está detenido el productor sigue publicando. Al reanudar, el consumidor drena los mensajes sin nuevas publicaciones.
También pueden ejecutarse instancias separadas con `RABBITMQ_CONSUMER_ENABLED=false` en el productor y `true` en el consumidor,
usando la misma BD/cola y distintos puertos HTTP. `MENSAJERIA_ENABLED=false` deshabilita el módulo en entornos sin RabbitMQ.
El monitoreo HTTP permite comprobar el estado del broker sin requerir su complemento de administración.
Implementación basada en [Spring AMQP / RabbitMQ](https://www.rabbitmq.com/tutorials/tutorial-two-spring-amqp).

## Verificación de requisitos

- `bash mvnw test`: pruebas unitarias sin conexiones a la BD de la aplicación; la integración se omite si no existe `TEST_DB_URL`.
- `bash scripts/verificar.sh`: crea MariaDB desechable, ejecuta el esquema, aplica dos veces la migración y corre las pruebas HTTP/JPA/procedimientos/RabbitMQ.
  Requiere Java 17, Python 3, `mariadb-install-db`, `mariadbd`, cliente MariaDB y RabbitMQ accesible. Usa una cola de prueba exclusiva y no requiere privilegios de administrador.
- Para integrar contra un **MySQL de pruebas** ya inicializado: `TEST_DB_URL=jdbc:mysql://localhost:3306/ppooii TEST_DB_USER=... TEST_DB_PASSWORD=... bash mvnw test`.
  Las pruebas modifican datos: emplear exclusivamente una BD desechable.

La trazabilidad y los resultados de la revisión están en [docs/CUMPLIMIENTO.md](docs/CUMPLIMIENTO.md).

## Interfaz e importación de Excel

Abrir **http://localhost:9001/rutas.html** e iniciar sesión. En **Importar Excel**:

1. Descargar **Plantilla vacía** o **Excel de ejemplo**. Ambos tienen una segunda hoja con instrucciones.
2. Completar una fila por parada. Las identificaciones se conservan como texto; la columna de orden admite enteros desde 0.
3. Seleccionar o arrastrar el archivo y pulsar **Importar trayectos**.
4. Revisar las filas agregadas y los motivos de rechazo. El detalle se pagina de 50 en 50 y se puede recuperar mediante el ID del cargue.

Archivos locales: [plantilla vacía](docs/plantillas/Plantilla_Cargue_Trayectos.xlsx) y [ejemplo con tres paradas](docs/plantillas/Ejemplo_Cargue_Trayectos.xlsx).
Descargas protegidas: `GET /LaboratorioV4/trayectos/plantilla` y `GET /LaboratorioV4/trayectos/ejemplo`.
La plantilla vacía no contiene filas de demostración. El ejemplo usa conductor `9001`, placa `TST001` y ruta `NUEVA-001`; adaptar los valores a la instalación.

**Swagger UI:** http://localhost:9001/swagger-ui.html · **OpenAPI JSON:** http://localhost:9001/api-docs.
La interfaz incluye accesos directos a Swagger y utiliza un tema claro de verdes y crema, navegación adaptable, estados de carga y avisos por operación.

## Flujo para registrar una ruta por API

1. Autenticarse en `/authenticate` con username, password y encabezado APIkey.
2. Crear el vehículo en `POST /LaboratorioV1/vehiculo` con al menos un documento; requiere JWT y APIkey.
3. Cargar los PDF con `POST /LaboratorioV2/vehiculos/{placa}/documentos`; quedan En Verificacion.
4. Resolver la verificación con `PUT /LaboratorioV2/vehiculos/{placa}/documentos/{idDocumento}/estado`,
   body `{"estado":"Habilitado"}`. El identificador corresponde al documento paramétrico.
5. Crear el conductor con `POST /LaboratorioV2/persona`, asociarlo al vehículo y cambiar la asociación a PO.
6. Crear las paradas en `POST /LaboratorioV3/trayectos`, o cargar la plantilla en
   `POST /LaboratorioV4/trayectos/cargue` usando multipart/form-data, campo file.
7. Consultar la ruta por código y, para Excel, el idCargue devuelto.

Las consultas públicas funcionan también si el cliente conserva un JWT vencido.
La consulta de restricciones refleja una licencia vencida inmediatamente; la tarea de 2 minutos persiste RO.
