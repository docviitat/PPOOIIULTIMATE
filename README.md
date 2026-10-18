# Laboratorio 4 + Entregas 3 y 4 del proyecto (Spring Boot · MySQL · JPA · Maven)

Contiene: **(1)** documentación Swagger/OpenAPI de todos los servicios (Lab 4), **(2)** cargue masivo de personas desde Excel
(Lab 4 complementario) y **(3)** cargue masivo de trayectos desde Excel con tres procedimientos almacenados (Entrega 4).
La prueba de concepto RabbitMQ está en el proyecto hermano `rabbitmq-poc`.

## Puesta en marcha
1. Ejecutar en MariaDB/MySQL **un solo script**: `database/script.sql` (crea la base `ppooii` desde cero —incluye `DROP DATABASE`—,
   todas las tablas, restricciones CHECK, los 6 procedimientos almacenados y datos de prueba).
   Con el cliente `mysql`: `mysql -u root -p < database/script.sql`.
2. Revisar usuario/clave en `src/main/resources/application.properties`.
3. Para las coordenadas de los trayectos (tarea cada 90 s) definir la API Key de Google Maps:
   variable de entorno `GOOGLE_MAPS_API_KEY` o propiedad `google.maps.api-key`. Sin clave se usa Nominatim como respaldo.
4. `mvn spring-boot:run` (puerto **9001**).

Usuarios de prueba (login / password / APIKey): `jperez / conductor123 / api_key_12345` y `mgomez / admin123 / api_key_67890`.
Para obtener el token: `POST /authenticate` con header `APIkey` y body `{"username":"mgomez","password":"admin123"}`.

## Entrega 1 – Vehículos y documentos (token)  `/LaboratorioV1`
| Método | URL | Descripción |
|---|---|---|
| POST/GET/PUT/DELETE | `/vehiculo`, `/vehiculo/{id}`, `/vehiculos` | CRUD de vehículo. Al crear exige ≥ 1 documento; todos quedan `En Verificacion` |
| POST/GET/PUT/DELETE | `/documento`, `/documento/{id}`, `/documentos` | CRUD de la entidad paramétrica de documentos |
| GET | `/vehiculo/placa/{placa}` | Buscar por placa |
| GET | `/vehiculos/tipo/{tipo}` | Buscar por tipo de vehículo (Automovil / Motocicleta) |
| GET | `/vehiculos/documento/{codigoDocumento}` | Vehículos que tienen en común un tipo de documento |
| GET | `/vehiculos/estado-documento/{estado}` | Por estado del documento: Habilitado, Vencido, En Verificacion |
| POST | `/vehiculo/{id}/documentos` | Agregar documentos a un vehículo |

## Entrega 2 – Personas, usuarios, conductores  `/LaboratorioV2` (y `/LaboratorioV1/persona`)
| Método | URL | Token | Descripción |
|---|---|---|---|
| POST / PUT / GET | `/LaboratorioV1/persona`, `/personas`, `/persona/id/{id}` | Sí | Persona; si es ADMINISTRATIVO crea usuario con login (inicial nombre + primer apellido), password y APIKey automáticos |
| PUT | `/LaboratorioV2/usuarios/{login}/password` | Sí | Cambia la contraseña (body `{"password": "..."}`) |
| GET | `/LaboratorioV2/usuarios/{login}/apikey` | Sí | Regenera la APIKey |
| POST | `/LaboratorioV2/vehiculos/{placa}/documentos` | Sí | Carga/actualiza uno o varios PDF en Base64 |
| POST | `/LaboratorioV2/conductores/{identificacion}/vehiculos` | Sí | Asocia vehículos a un conductor (estado inicial EA) |
| PUT | `/LaboratorioV2/conductores/{identificacion}/vehiculos/{placa}/estado` | Sí | Cambia el estado PO / EA / RO |
| GET | `/LaboratorioV2/publico/vehiculos/documentos-vencidos` | **No** | Vehículos con documentos vencidos |
| GET | `/LaboratorioV2/publico/conductores/pueden-operar` | **No** | Conductores con estado PO |
| GET | `/LaboratorioV2/publico/vehiculos/placa/{placa}` | **No** | Vehículo con conductores y documentos |
| GET | `/LaboratorioV2/publico/vehiculos/documentos-por-vencer?dias=N` | **No** | Documentos por vencer en N días |
| GET | `/LaboratorioV2/publico/personas/total-por-tipo` | **No** | Total de personas por tipo |

Los servicios de la Entrega 1 requieren token (que a su vez requiere login + password + APIKey).
Archivos (licencia y PDF de documentos): viajan en BASE64 en el JSON y se guardan como BLOB.

## Swagger (sin token)
* Interfaz: http://localhost:9001/swagger-ui.html  ·  JSON: http://localhost:9001/api-docs
* Botón **Authorize** → pegar el token obtenido en `POST /authenticate` (header `APIkey` + body con login/password).

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
licencias vencidas → conductor `RO` (cada 2 min) · documentos con vigencia vencida → `VENCIDO` (cada 2 min) · trayectos sin latitud/longitud → Google Maps y se guardan (cada 90 s).
`Persona` incluye `identificacion`, `tipoPersona`, `fechaLicencia` y `licencia` (BLOB; el JSON la envía/recibe en BASE64).

## Flujo del cargue de trayectos
`Excel → CARGAR_TRAYECTOS_EXCEL (trayecto_tmp, CARGADO) → VALIDAR_TRAYECTOS_CARGUE (VALIDADO / ERROR + observación acumulativa) → PROCESAR_TRAYECTOS_EXCEL (trayecto, PROCESADO)`

Reglas de validación: campos obligatorios · orden entero ≥ 1 · vehículo existente con **todos** sus documentos HABILITADO ·
conductor existente, tipo CONDUCTOR y estado **PO** en su relación con el vehículo · sin duplicados (ruta + orden) en el archivo ni en `trayecto`.

## Modelo de datos
Tablas: `persona`, `usuario`, `vehiculos`, `documentos`, `vehiculo_documento`, `vehiculo_conductor`, `trayecto`, `trayecto_tmp`, `persona_tmp`, `coordenadas`.
CHECK: tipo de vehículo / servicio / combustible, tipo y obligatoriedad de documentos, estado del documento del vehículo
(`Habilitado`, `Vencido`, `En Verificacion`), tipo de identificación y tipo de persona (`C`/`A`), estado del conductor (`PO`/`EA`/`RO`) y estado de `trayecto_tmp`.
Interpretaciones asumidas: `tipos_vehiculo` A = automóvil, M = moto, AM = ambos; `obligatoriedad` RA / RM / RR = obligatorio para automóvil / moto / ambos.
