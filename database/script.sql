-- =====================================================================
-- SCRIPT SQL DEFINITIVO Y UNIFICADO - PROYECTO PPOOII
-- Esquema: ppooii
-- =====================================================================
DROP DATABASE IF EXISTS ppooii;
CREATE DATABASE ppooii CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ppooii;

-- Eliminar tablas en orden inverso de dependencia para evitar conflictos
DROP TABLE IF EXISTS coordenadas;
DROP TABLE IF EXISTS trayecto;
DROP TABLE IF EXISTS trayecto_tmp;
DROP TABLE IF EXISTS persona_tmp;
DROP TABLE IF EXISTS vehiculo_conductor;
DROP TABLE IF EXISTS vehiculo_persona;
DROP TABLE IF EXISTS vehiculo_documento;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS documentos;
DROP TABLE IF EXISTS persona;
DROP TABLE IF EXISTS vehiculos;

-- ---------------------------------------------------------------------
-- 1. ENTIDAD PERSONA
-- ---------------------------------------------------------------------
CREATE TABLE persona (
  id BIGINT NOT NULL AUTO_INCREMENT,
  identificacion VARCHAR(20) DEFAULT NULL,
  tipo_identificacion VARCHAR(5) DEFAULT NULL CHECK (tipo_identificacion IN ('CC','CE','PAS')),
  nombres VARCHAR(100) NOT NULL,
  pnombre VARCHAR(100) DEFAULT NULL,          
  apellidos VARCHAR(100) DEFAULT NULL,
  edad INT DEFAULT NULL,                      
  ubicacion VARCHAR(255) DEFAULT NULL,        
  correo VARCHAR(100) DEFAULT NULL,
  tipo_persona VARCHAR(1) DEFAULT NULL CHECK (tipo_persona IN ('C','A')),
  licencia_conduccion LONGBLOB DEFAULT NULL,
  fecha_vigencia_licencia DATE DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY identificacion (identificacion),
  CONSTRAINT chk_persona_completa CHECK (
    (tipo_persona IS NULL AND identificacion IS NULL AND tipo_identificacion IS NULL)
    OR (tipo_persona IS NOT NULL AND identificacion IS NOT NULL AND tipo_identificacion IS NOT NULL
        AND apellidos IS NOT NULL AND correo IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 2. ENTIDAD VEHICULOS
-- ---------------------------------------------------------------------
CREATE TABLE vehiculos (
  id_vehiculo INT NOT NULL AUTO_INCREMENT,
  tipo_vehiculo VARCHAR(20) NOT NULL CHECK (tipo_vehiculo IN ('Automovil','Motocicleta')),
  placa VARCHAR(6) NOT NULL,
  tipo_servicio VARCHAR(2) NOT NULL CHECK (tipo_servicio IN ('A','M','AM')),
  tipo_combustible VARCHAR(20) NOT NULL CHECK (tipo_combustible IN ('Gasolina','Diesel','Gas','Electrico','Hibrido')),
  capacidad_pasajeros INT NOT NULL,
  color VARCHAR(30) NOT NULL,
  modelo INT NOT NULL,
  marca VARCHAR(100) NOT NULL,
  linea VARCHAR(100) NOT NULL,
  PRIMARY KEY (id_vehiculo),
  UNIQUE KEY placa (placa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 3. ENTIDAD DOCUMENTOS (Paramétrica)
-- ---------------------------------------------------------------------
CREATE TABLE documentos (
  id_documento INT NOT NULL AUTO_INCREMENT,
  codigo_documento VARCHAR(20) NOT NULL,
  nombre_documento VARCHAR(100) NOT NULL,
  tipos_vehiculo VARCHAR(2) NOT NULL,
  obligatoriedad VARCHAR(2) NOT NULL,
  descripcion VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id_documento),
  UNIQUE KEY codigo_documento (codigo_documento),
  CONSTRAINT chk_tipos_vehiculo CHECK (tipos_vehiculo IN ('A','M','AM')),
  CONSTRAINT chk_obligatoriedad CHECK (obligatoriedad IN ('RA','RM','RR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 4. ENTIDAD USUARIO
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
  login VARCHAR(100) NOT NULL,
  idpersona BIGINT NOT NULL,
  password VARCHAR(100) NOT NULL,
  apikey VARCHAR(100) NOT NULL,
  PRIMARY KEY (login, idpersona),
  UNIQUE KEY uq_usuario_login (login),
  UNIQUE KEY fk_usuario_persona (idpersona),
  CONSTRAINT fk_usuario_persona FOREIGN KEY (idpersona) REFERENCES persona (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 5. ENTIDAD VEHICULO_CONDUCTOR
-- ---------------------------------------------------------------------
CREATE TABLE vehiculo_conductor (
  id BIGINT NOT NULL AUTO_INCREMENT,
  estado VARCHAR(2) NOT NULL CHECK (estado IN ('PO','EA','RO')),
  fecha_asociacion DATE NOT NULL,
  persona_id BIGINT NOT NULL,
  vehiculo_id INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_vehiculo_conductor (persona_id, vehiculo_id),
  KEY FKbndhle29qvq74qkp2pffxfwy9 (persona_id),
  KEY FKai18v148tmnnl0ivxei0ft5es (vehiculo_id),
  CONSTRAINT FKai18v148tmnnl0ivxei0ft5es FOREIGN KEY (vehiculo_id) REFERENCES vehiculos (id_vehiculo),
  CONSTRAINT FKbndhle29qvq74qkp2pffxfwy9 FOREIGN KEY (persona_id) REFERENCES persona (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- ENTIDAD TRAYECTO_TMP (Tabla Temporal para Cargue Masivo de Excel)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS trayecto_tmp (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_cargue BIGINT NOT NULL,
  codigo_ruta TEXT DEFAULT NULL,
  orden TEXT DEFAULT NULL,
  ubicacion TEXT DEFAULT NULL,
  identificacion_conductor TEXT DEFAULT NULL,
  placa TEXT DEFAULT NULL,
  latitud VARCHAR(50) DEFAULT NULL,
  longitud VARCHAR(50) DEFAULT NULL,
  nombre_conductor TEXT DEFAULT NULL,
  edad_conductor TEXT DEFAULT NULL,
  login_registro VARCHAR(100) DEFAULT NULL,
  estado VARCHAR(30) NOT NULL DEFAULT 'CARGADO'
    CHECK (estado IN ('CARGADO','VALIDADO','PROCESADO','ERROR')),
  observacion TEXT DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_trayecto_tmp_cargue (id_cargue)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 6. ENTIDAD VEHICULO_DOCUMENTO
-- ---------------------------------------------------------------------
CREATE TABLE vehiculo_documento (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_vehiculo INT NOT NULL,
  tipo_documento VARCHAR(50) DEFAULT NULL,
  numero_documento VARCHAR(50) DEFAULT NULL,
  fecha_expedicion DATE NOT NULL,
  fecha_vencimiento DATE NOT NULL,
  documento_pdf LONGBLOB DEFAULT NULL,
  estado_documento VARCHAR(20) NOT NULL DEFAULT 'En Verificacion'
    CHECK (estado_documento IN ('Habilitado','Vencido','En Verificacion')),
  id_documento INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_vehiculo_documento (id_vehiculo, id_documento),
  KEY fk_documento_vehiculo (id_vehiculo),
  KEY fk_vd_documento (id_documento),
  CONSTRAINT fk_documento_vehiculo FOREIGN KEY (id_vehiculo) REFERENCES vehiculos (id_vehiculo) ON DELETE CASCADE,
  CONSTRAINT fk_vd_documento FOREIGN KEY (id_documento) REFERENCES documentos (id_documento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS coordenadas (
    id_coordenada BIGINT AUTO_INCREMENT PRIMARY KEY,
    latitud DOUBLE NOT NULL,
    longitud DOUBLE NOT NULL,
    marca VARCHAR(255),
    persona BIGINT,
    CONSTRAINT fk_coordenada_persona FOREIGN KEY (persona) REFERENCES persona(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 7. ENTIDAD TRAYECTO (Definitiva)
-- ---------------------------------------------------------------------
CREATE TABLE trayecto (
  id_trayecto BIGINT NOT NULL AUTO_INCREMENT,
  id_persona BIGINT NOT NULL,
  id_vehiculo INT NOT NULL,
  codigo_ruta VARCHAR(30) NOT NULL,
  ubicacion VARCHAR(255) NOT NULL,
  orden_parada INT NOT NULL,
  latitud DOUBLE DEFAULT NULL,
  longitud DOUBLE DEFAULT NULL,
  login_registro VARCHAR(100) NOT NULL,
  PRIMARY KEY (id_trayecto),
  UNIQUE KEY uq_trayecto_ruta_orden (codigo_ruta, orden_parada),
  KEY idx_trayecto_persona (id_persona),
  KEY idx_trayecto_vehiculo (id_vehiculo),
  CONSTRAINT fk_trayecto_persona FOREIGN KEY (id_persona) REFERENCES persona (id),
  CONSTRAINT fk_trayecto_vehiculo FOREIGN KEY (id_vehiculo) REFERENCES vehiculos (id_vehiculo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Registro atómico: el identificador representa el minuto REAL del cargue.
CREATE TABLE cargue_archivo (
  tipo VARCHAR(10) NOT NULL,
  id_cargue BIGINT NOT NULL,
  PRIMARY KEY (tipo, id_cargue)
) ENGINE=InnoDB;

CREATE TABLE persona_tmp (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  pnombre TEXT,
  edad TEXT,
  ubicacion TEXT,
  estado VARCHAR(10) NOT NULL DEFAULT 'CARGADO'
    CHECK (estado IN ('CARGADO','VALIDADO','PROCESADO','ERROR')),
  observacion TEXT,
  id_cargue BIGINT NOT NULL,
  KEY idx_persona_tmp_cargue (id_cargue)
) ENGINE=InnoDB;

CREATE TABLE mensaje_recibido (
  id VARCHAR(36) PRIMARY KEY,
  contenido TEXT NOT NULL,
  recibido TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Los procedimientos participan en la transacción del servicio JPA.
DELIMITER $$
CREATE PROCEDURE CARGAR_DATOS_EXCEL(IN pnombre TEXT, IN edad TEXT, IN ubicacion TEXT, IN idCargue BIGINT)
BEGIN
  INSERT INTO persona_tmp(pnombre,edad,ubicacion,estado,observacion,id_cargue)
  VALUES(pnombre,edad,ubicacion,'CARGADO','',idCargue);
END $$
CREATE PROCEDURE VALIDAR_DATOS_CARGUE(IN p_id BIGINT)
BEGIN
  UPDATE persona_tmp SET observacion=CONCAT(
    IF(pnombre IS NULL OR TRIM(pnombre)='', 'El campo Nombre no puede ser vacio. ', ''),
    IF(CHAR_LENGTH(pnombre)>100, 'El nombre supera 100 caracteres. ', ''),
    IF(edad IS NULL OR TRIM(edad)='', 'El campo Edad no puede ser vacio. ',
      IF(edad NOT REGEXP '^[0-9]{1,3}$', 'El campo Edad no cumple con las especificaciones numericas. ',
        IF(CAST(edad AS UNSIGNED) NOT BETWEEN 18 AND 35, 'La edad debe estar entre 18 y 35. ', ''))),
    IF(ubicacion IS NULL OR TRIM(ubicacion)='', 'El campo ubicacion no puede ser vacio. ', ''),
    IF(CHAR_LENGTH(ubicacion)>255, 'La ubicacion supera 255 caracteres. ', ''))
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR','VALIDADO');
  UPDATE persona_tmp SET estado=IF(observacion='', 'VALIDADO', 'ERROR')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR','VALIDADO');
END $$
CREATE PROCEDURE PROCESAR_DATOS_EXCEL(IN p_id BIGINT)
BEGIN
  -- El Excel complementario solo suministra nombre, edad y ubicación.
  -- No se inventan documentos de identidad, correos ni usuarios administrativos.
  INSERT INTO persona(nombres,pnombre,edad,ubicacion)
  SELECT pnombre,pnombre,CAST(edad AS UNSIGNED),ubicacion FROM persona_tmp
  WHERE id_cargue=p_id AND estado='VALIDADO';
  UPDATE persona_tmp SET estado='PROCESADO' WHERE id_cargue=p_id AND estado='VALIDADO';
END $$
CREATE PROCEDURE CARGAR_TRAYECTOS_EXCEL(IN codigoRuta TEXT, IN orden TEXT, IN ubicacion TEXT,
  IN identificacionConductor TEXT, IN placa TEXT, IN nombreConductor TEXT, IN edadConductor TEXT,
  IN loginRegistro VARCHAR(100), IN idCargue BIGINT)
BEGIN
  INSERT INTO trayecto_tmp(codigo_ruta,orden,ubicacion,identificacion_conductor,placa,
    nombre_conductor,edad_conductor,login_registro,id_cargue,estado,observacion)
  VALUES(codigoRuta,orden,ubicacion,identificacionConductor,placa,nombreConductor,edadConductor,
    loginRegistro,idCargue,'CARGADO','');
END $$
CREATE PROCEDURE VALIDAR_TRAYECTOS_CARGUE(IN p_id BIGINT)
BEGIN
  -- Revalidar no toca los procesados ni duplica observaciones.
  UPDATE trayecto_tmp SET observacion='',estado='CARGADO'
  WHERE id_cargue=p_id AND estado IN ('CARGADO','VALIDADO','ERROR');
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'El codigo de ruta no puede ser vacio. ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (codigo_ruta IS NULL OR TRIM(codigo_ruta)='');
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'El codigo de ruta supera 30 caracteres. ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (CHAR_LENGTH(TRIM(codigo_ruta))>30);
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'El orden del trayecto no puede ser vacio. ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (orden IS NULL OR TRIM(orden)='');
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'El orden debe ser un entero entre 0 y 99999. ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (orden IS NOT NULL AND TRIM(orden)<>'' AND (TRIM(orden) NOT REGEXP '^[0-9]{1,5}$'));
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'La ubicacion no puede ser vacia. ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (ubicacion IS NULL OR TRIM(ubicacion)='');
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'La ubicacion supera 255 caracteres. ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (CHAR_LENGTH(TRIM(ubicacion))>255);
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'La identificacion del conductor no puede ser vacia. ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (identificacion_conductor IS NULL OR TRIM(identificacion_conductor)='');
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'La placa no puede ser vacia. ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (placa IS NULL OR TRIM(placa)='');
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'El vehiculo no existe. ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (TRIM(placa)<>'' AND NOT EXISTS (SELECT 1 FROM vehiculos v WHERE v.placa=TRIM(t.placa)));
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'El vehiculo no tiene todos sus documentos habilitados y vigentes. ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (EXISTS (SELECT 1 FROM vehiculos v WHERE v.placa=TRIM(t.placa) AND (NOT EXISTS (SELECT 1 FROM vehiculo_documento d WHERE d.id_vehiculo=v.id_vehiculo) OR EXISTS (SELECT 1 FROM vehiculo_documento d WHERE d.id_vehiculo=v.id_vehiculo AND (d.estado_documento<>'Habilitado' OR d.fecha_vencimiento<CURDATE())))));
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'El conductor no existe o no es de tipo Conductor. ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (TRIM(identificacion_conductor)<>'' AND NOT EXISTS (SELECT 1 FROM persona p WHERE p.identificacion=TRIM(t.identificacion_conductor) AND p.tipo_persona='C'));
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'El conductor no puede operar este vehiculo (requiere PO y licencia no vencida). ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (TRIM(identificacion_conductor)<>'' AND TRIM(placa)<>'' AND NOT EXISTS (SELECT 1 FROM persona p JOIN vehiculo_conductor vc ON vc.persona_id=p.id JOIN vehiculos v ON v.id_vehiculo=vc.vehiculo_id WHERE p.identificacion=TRIM(t.identificacion_conductor) AND p.tipo_persona='C' AND v.placa=TRIM(t.placa) AND vc.estado='PO' AND (p.fecha_vigencia_licencia IS NULL OR p.fecha_vigencia_licencia>=CURDATE())));
  UPDATE trayecto_tmp t SET estado='ERROR', observacion=CONCAT(observacion,'Ya existe un trayecto con ese codigo de ruta y orden. ')
  WHERE id_cargue=p_id AND estado IN ('CARGADO','ERROR') AND (TRIM(orden) REGEXP '^[0-9]{1,5}$' AND EXISTS (SELECT 1 FROM trayecto y WHERE y.codigo_ruta=TRIM(t.codigo_ruta) AND y.orden_parada=CAST(TRIM(t.orden) AS UNSIGNED)));
  UPDATE trayecto_tmp t JOIN (
    SELECT TRIM(codigo_ruta) ruta,CAST(TRIM(orden) AS UNSIGNED) numero FROM trayecto_tmp
    WHERE id_cargue=p_id AND TRIM(orden) REGEXP '^[0-9]{1,5}$'
    GROUP BY TRIM(codigo_ruta),CAST(TRIM(orden) AS UNSIGNED) HAVING COUNT(*)>1
  ) duplicado ON TRIM(t.codigo_ruta)=duplicado.ruta
    AND TRIM(t.orden) REGEXP '^[0-9]{1,5}$' AND CAST(TRIM(t.orden) AS UNSIGNED)=duplicado.numero
  SET t.estado='ERROR',t.observacion=CONCAT(t.observacion,'Ruta y orden repetidos en el archivo. ')
  WHERE t.id_cargue=p_id AND t.estado IN ('CARGADO','ERROR');
  UPDATE trayecto_tmp SET estado='VALIDADO' WHERE id_cargue=p_id AND estado='CARGADO';
END $$
CREATE PROCEDURE PROCESAR_TRAYECTOS_EXCEL(IN p_id BIGINT, IN p_login VARCHAR(100))
BEGIN
  -- Revalidar inmediatamente antes de insertar evita procesar un estado obsoleto.
  CALL VALIDAR_TRAYECTOS_CARGUE(p_id);
  INSERT INTO trayecto(id_persona,id_vehiculo,codigo_ruta,ubicacion,orden_parada,latitud,longitud,login_registro)
  SELECT p.id,v.id_vehiculo,TRIM(t.codigo_ruta),TRIM(t.ubicacion),CAST(TRIM(t.orden) AS UNSIGNED),
    CAST(NULLIF(t.latitud,'') AS DECIMAL(10,7)),CAST(NULLIF(t.longitud,'') AS DECIMAL(10,7)),p_login
  FROM trayecto_tmp t JOIN persona p ON p.identificacion=TRIM(t.identificacion_conductor)
  JOIN vehiculos v ON v.placa=TRIM(t.placa) WHERE t.id_cargue=p_id AND t.estado='VALIDADO';
  UPDATE trayecto_tmp SET estado='PROCESADO' WHERE id_cargue=p_id AND estado='VALIDADO';
END $$
DELIMITER ;

-- =====================================================================
-- DATOS INICIALES DE PRUEBA (SEED DATA)
-- =====================================================================
INSERT INTO persona (id, identificacion, tipo_identificacion, nombres, apellidos, correo, tipo_persona, licencia_conduccion, fecha_vigencia_licencia, pnombre, edad, ubicacion)
VALUES 
(1, '1001234567', 'CC', 'Juan', 'Perez', 'juan.perez@example.com', 'C', FROM_BASE64('TElDRU5DSUEgREUgRUpFTVBMTw=='), '2030-12-31', 'Juan', 25, 'Ibague'),
(2, '1007654321', 'CE', 'Maria', 'Gomez', 'maria.gomez@example.com', 'A', NULL, NULL, 'Maria', 30, 'Ibague'),
(3, '1012345678', 'CC', 'Carlos', 'Ruiz', 'carlos.ruiz@example.com', 'C', FROM_BASE64('TElDRU5DSUEgREUgRUpFTVBMTw=='), '2025-01-01', 'Carlos', 28, 'Ibague');

INSERT INTO vehiculos (id_vehiculo, tipo_vehiculo, placa, tipo_servicio, tipo_combustible, capacidad_pasajeros, color, modelo, marca, linea)
VALUES 
(1, 'Automovil', 'ABC123', 'A', 'Gasolina', 5, 'Negro', 2022, 'Toyota', 'Corolla'),
(2, 'Motocicleta', 'XYZ789', 'M', 'Gasolina', 2, 'Rojo', 2021, 'Yamaha', 'FZ');

INSERT INTO documentos (id_documento, codigo_documento, nombre_documento, tipos_vehiculo, obligatoriedad, descripcion)
VALUES 
(1, 'SOAT', 'Seguro Obligatorio de Accidentes de Transito', 'AM', 'RA', 'Seguro obligatorio para todos los vehiculos'),
(2, 'TM', 'Tecno Mecanica', 'AM', 'RA', 'Revision tecnico-mecanica y de gases');

INSERT INTO usuario (login, idpersona, password, apikey)
VALUES 
('jperez', 1, 'conductor123', 'api_key_12345'),
('mgomez', 2, 'admin123', 'api_key_67890');

INSERT INTO vehiculo_conductor (id, estado, fecha_asociacion, persona_id, vehiculo_id)
VALUES 
(1, 'PO', '2026-01-15', 1, 1),
(2, 'PO', '2026-01-15', 3, 2);

INSERT INTO vehiculo_documento (id, id_vehiculo, tipo_documento, numero_documento, fecha_expedicion, fecha_vencimiento, documento_pdf, estado_documento, id_documento)
VALUES
(1, 1, 'SOAT', 'SOAT-0001', '2026-01-01', '2030-12-31', FROM_BASE64('JVBERi0xLjQ='), 'Habilitado', 1),
(2, 1, 'TM',   'TM-0001',   '2026-01-01', '2030-12-31', FROM_BASE64('JVBERi0xLjQ='), 'Habilitado', 2),
(3, 2, 'SOAT', 'SOAT-0002', '2026-01-01', '2030-12-31', FROM_BASE64('JVBERi0xLjQ='), 'Habilitado', 1),
(4, 2, 'TM',   'TM-0002',   '2026-01-01', '2030-12-31', FROM_BASE64('JVBERi0xLjQ='), 'En Verificacion', 2);

-- Datos adicionales para los ejemplos (Postman / README): conductor 9001, placa TST001, ruta T-001
INSERT INTO persona (id, identificacion, tipo_identificacion, nombres, apellidos, correo, tipo_persona, licencia_conduccion, fecha_vigencia_licencia, pnombre, edad, ubicacion)
VALUES (4, '9001', 'CC', 'Pedro', 'Lopez', 'pedro.lopez@example.com', 'C', FROM_BASE64('JVBERi0xLjQ='), '2030-12-31', 'Pedro', 35, 'Ibague');
INSERT INTO vehiculos (id_vehiculo, tipo_vehiculo, placa, tipo_servicio, tipo_combustible, capacidad_pasajeros, color, modelo, marca, linea)
VALUES (3, 'Automovil', 'TST001', 'A', 'Gasolina', 4, 'Blanco', 2023, 'Mazda', '3');
INSERT INTO vehiculo_documento (id, id_vehiculo, tipo_documento, numero_documento, fecha_expedicion, fecha_vencimiento, documento_pdf, estado_documento, id_documento)
VALUES (5, 3, 'SOAT', 'SOAT-0003', '2026-01-01', '2030-12-31', FROM_BASE64('JVBERi0xLjQ='), 'Habilitado', 1),
       (6, 3, 'TM',   'TM-0003',   '2026-01-01', '2030-12-31', FROM_BASE64('JVBERi0xLjQ='), 'Habilitado', 2);
INSERT INTO vehiculo_conductor (id, estado, fecha_asociacion, persona_id, vehiculo_id) VALUES (3, 'PO', '2026-01-15', 4, 3);
INSERT INTO trayecto (id_persona, id_vehiculo, codigo_ruta, ubicacion, orden_parada, latitud, longitud, login_registro)
VALUES (4, 3, 'T-001', 'Parque Murillo Toro, Ibague, Tolima', 0, NULL, NULL, 'seed'),
       (4, 3, 'T-001', 'Terminal de Transportes, Ibague, Tolima', 1, NULL, NULL, 'seed');

CREATE TABLE geocoding_cache (
  ubicacion VARCHAR(255) PRIMARY KEY,
  latitud DOUBLE,
  longitud DOUBLE,
  consultado TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
