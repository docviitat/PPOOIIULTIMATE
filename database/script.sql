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
  identificacion VARCHAR(20) NOT NULL,
  tipo_identificacion VARCHAR(5) NOT NULL CHECK (tipo_identificacion IN ('CC','CE','PAS')),
  nombres VARCHAR(100) NOT NULL,
  pnombre VARCHAR(100) DEFAULT NULL,          
  apellidos VARCHAR(100) NOT NULL,
  edad INT DEFAULT NULL,                      
  ubicacion VARCHAR(255) DEFAULT NULL,        
  correo VARCHAR(100) NOT NULL,
  tipo_persona VARCHAR(1) NOT NULL CHECK (tipo_persona IN ('C','A')),
  licencia_conduccion LONGBLOB DEFAULT NULL,
  fecha_vigencia_licencia DATE DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY identificacion (identificacion)
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
  KEY fk_usuario_persona (idpersona),
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

-- ---------------------------------------------------------------------
-- 8. ENTIDAD TRAYECTO_TMP (Temporal para Entrega 4 - Cargue Masivo)
-- ---------------------------------------------------------------------
CREATE TABLE trayecto_tmp (
  ID                       BIGINT        NOT NULL AUTO_INCREMENT,
  CODIGO_RUTA              VARCHAR(255)  DEFAULT NULL,
  ORDEN                    VARCHAR(255)  DEFAULT NULL,
  UBICACION                VARCHAR(1000) DEFAULT NULL,
  LATITUD                  VARCHAR(50)   DEFAULT NULL,
  LONGITUD                 VARCHAR(50)   DEFAULT NULL,
  IDENTIFICACION_CONDUCTOR VARCHAR(255)  DEFAULT NULL,
  PLACA                    VARCHAR(255)  DEFAULT NULL,
  ESTADO                   VARCHAR(10)   NOT NULL,
  OBSERVACION              VARCHAR(2000) DEFAULT NULL,
  ID_CARGUE                BIGINT        NOT NULL,
  PRIMARY KEY (ID),
  KEY idx_trayecto_tmp_cargue (ID_CARGUE),
  CONSTRAINT trayecto_tmp_estado_check CHECK (ESTADO IN ('CARGADO','VALIDADO','PROCESADO','ERROR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 9. ENTIDAD PERSONA_TMP (Requerida por PersonaRepository de Java)
-- ---------------------------------------------------------------------
CREATE TABLE persona_tmp (
  ID          BIGINT       NOT NULL AUTO_INCREMENT,
  PNOMBRE     VARCHAR(100) DEFAULT NULL,
  EDAD        VARCHAR(3)   DEFAULT NULL,
  UBICACION   VARCHAR(100) DEFAULT NULL,
  ESTADO      VARCHAR(10)  NOT NULL,
  OBSERVACION VARCHAR(500) DEFAULT NULL,
  ID_CARGUE   DOUBLE       NOT NULL,
  PRIMARY KEY (ID),
  KEY idx_persona_tmp_cargue (ID_CARGUE),
  CONSTRAINT persona_tmp_check CHECK (ESTADO IN ('CARGADO','VALIDADO','PROCESADO','ERROR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ---------------------------------------------------------------------
-- 10. COORDENADAS (la llena la tarea programada de personas)
-- ---------------------------------------------------------------------
CREATE TABLE coordenadas (
  id_coordenada INT NOT NULL AUTO_INCREMENT,
  persona INT NOT NULL,
  marca VARCHAR(100) DEFAULT NULL,
  longitud DOUBLE NOT NULL,
  latitud DOUBLE NOT NULL,
  PRIMARY KEY (id_coordenada),
  KEY idx_coordenadas_persona (persona)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- PROCEDIMIENTOS ALMACENADOS
-- =====================================================================

-- 1. Compatibilidad con PersonaRepository (Evita error 1305)
DROP PROCEDURE IF EXISTS CARGAR_DATOS_EXCEL;
DELIMITER $$
CREATE PROCEDURE CARGAR_DATOS_EXCEL(
    IN pnombre   VARCHAR(100),
    IN edad      VARCHAR(3),
    IN ubicacion VARCHAR(100),
    IN idCargue  DOUBLE)
BEGIN
    INSERT INTO persona_tmp (PNOMBRE, EDAD, UBICACION, ESTADO, OBSERVACION, ID_CARGUE)
    VALUES (pnombre, edad, ubicacion, 'CARGADO', '', idCargue);
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS VALIDAR_DATOS_CARGUE;
DELIMITER $$
CREATE PROCEDURE VALIDAR_DATOS_CARGUE(IN idCargue DOUBLE)
BEGIN
    START TRANSACTION;
    UPDATE persona_tmp SET ESTADO = 'VALIDADO' WHERE ID_CARGUE = idCargue AND ESTADO = 'CARGADO';
    COMMIT;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS PROCESAR_DATOS_EXCEL;
DELIMITER $$
CREATE PROCEDURE PROCESAR_DATOS_EXCEL(IN idCargue DOUBLE)
BEGIN
    START TRANSACTION;
    UPDATE persona_tmp SET ESTADO = 'PROCESADO' WHERE ID_CARGUE = idCargue AND ESTADO = 'VALIDADO';
    COMMIT;
END $$
DELIMITER ;


-- 2. Cargue de Trayectos (Entrega 4)
DROP PROCEDURE IF EXISTS CARGAR_TRAYECTOS_EXCEL;
DELIMITER $$
CREATE PROCEDURE CARGAR_TRAYECTOS_EXCEL(
    IN p_codigo_ruta  VARCHAR(255),
    IN p_orden        VARCHAR(255),
    IN p_ubicacion    VARCHAR(1000),
    IN p_identificacion_conductor VARCHAR(255),
    IN p_placa        VARCHAR(255),
    IN p_id_cargue    BIGINT)
BEGIN
    INSERT INTO trayecto_tmp
        (CODIGO_RUTA, ORDEN, UBICACION, IDENTIFICACION_CONDUCTOR, PLACA, ESTADO, OBSERVACION, ID_CARGUE)
    VALUES
        (p_codigo_ruta, p_orden, p_ubicacion, p_identificacion_conductor, p_placa, 'CARGADO', '', p_id_cargue);
END $$
DELIMITER ;


-- 3. Validación de Trayectos (Entrega 4)
DROP PROCEDURE IF EXISTS VALIDAR_TRAYECTOS_CARGUE;
DELIMITER $$
CREATE PROCEDURE VALIDAR_TRAYECTOS_CARGUE(IN p_id_cargue BIGINT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    UPDATE trayecto_tmp SET ESTADO='ERROR',
           OBSERVACION = CONCAT(IFNULL(OBSERVACION,''),' El codigo de ruta no puede ser vacio.')
     WHERE ID_CARGUE = p_id_cargue AND ESTADO IN ('CARGADO','ERROR')
       AND (CODIGO_RUTA IS NULL OR TRIM(CODIGO_RUTA) = '');

    UPDATE trayecto_tmp SET ESTADO='ERROR',
           OBSERVACION = CONCAT(IFNULL(OBSERVACION,''),' El orden del trayecto no puede ser vacio.')
     WHERE ID_CARGUE = p_id_cargue AND ESTADO IN ('CARGADO','ERROR')
       AND (ORDEN IS NULL OR TRIM(ORDEN) = '');

    UPDATE trayecto_tmp SET ESTADO='ERROR',
           OBSERVACION = CONCAT(IFNULL(OBSERVACION,''),' El orden del trayecto debe ser un numero entero mayor o igual a 0 (maximo 5 digitos).')
     WHERE ID_CARGUE = p_id_cargue AND ESTADO IN ('CARGADO','ERROR')
       AND ORDEN IS NOT NULL AND TRIM(ORDEN) <> ''
       AND ORDEN NOT REGEXP '^[0-9]{1,5}$';

    UPDATE trayecto_tmp SET ESTADO='ERROR',
           OBSERVACION = CONCAT(IFNULL(OBSERVACION,''),' La ubicacion no puede ser vacia.')
     WHERE ID_CARGUE = p_id_cargue AND ESTADO IN ('CARGADO','ERROR')
       AND (UBICACION IS NULL OR TRIM(UBICACION) = '');

    UPDATE trayecto_tmp SET ESTADO='ERROR',
           OBSERVACION = CONCAT(IFNULL(OBSERVACION,''),' La identificacion del conductor no puede ser vacia.')
     WHERE ID_CARGUE = p_id_cargue AND ESTADO IN ('CARGADO','ERROR')
       AND (IDENTIFICACION_CONDUCTOR IS NULL OR TRIM(IDENTIFICACION_CONDUCTOR) = '');

    UPDATE trayecto_tmp SET ESTADO='ERROR',
           OBSERVACION = CONCAT(IFNULL(OBSERVACION,''),' La placa del vehiculo no puede ser vacia.')
     WHERE ID_CARGUE = p_id_cargue AND ESTADO IN ('CARGADO','ERROR')
       AND (PLACA IS NULL OR TRIM(PLACA) = '');

    UPDATE trayecto_tmp t SET t.ESTADO='ERROR',
           t.OBSERVACION = CONCAT(IFNULL(t.OBSERVACION,''),' El vehiculo con la placa indicada no existe.')
     WHERE t.ID_CARGUE = p_id_cargue AND t.ESTADO IN ('CARGADO','ERROR')
       AND t.PLACA IS NOT NULL AND TRIM(t.PLACA) <> ''
       AND NOT EXISTS (SELECT 1 FROM vehiculos v WHERE v.placa = TRIM(t.PLACA));

    UPDATE trayecto_tmp t SET t.ESTADO='ERROR',
           t.OBSERVACION = CONCAT(IFNULL(t.OBSERVACION,''),' El vehiculo tiene documentos que no estan en estado Habilitado.')
     WHERE t.ID_CARGUE = p_id_cargue AND t.ESTADO IN ('CARGADO','ERROR')
       AND t.PLACA IS NOT NULL AND TRIM(t.PLACA) <> ''
       AND EXISTS (SELECT 1 FROM vehiculos v WHERE v.placa = TRIM(t.PLACA))
       AND EXISTS (SELECT 1 FROM vehiculo_documento d
                     JOIN vehiculos v ON v.id_vehiculo = d.id_vehiculo
                    WHERE v.placa = TRIM(t.PLACA) AND d.estado_documento <> 'Habilitado');

    UPDATE trayecto_tmp t SET t.ESTADO='ERROR',
           t.OBSERVACION = CONCAT(IFNULL(t.OBSERVACION,''),' El conductor no existe o la persona no es de tipo Conductor.')
     WHERE t.ID_CARGUE = p_id_cargue AND t.ESTADO IN ('CARGADO','ERROR')
       AND t.IDENTIFICACION_CONDUCTOR IS NOT NULL AND TRIM(t.IDENTIFICACION_CONDUCTOR) <> ''
       AND NOT EXISTS (SELECT 1 FROM persona p
                        WHERE p.identificacion = TRIM(t.IDENTIFICACION_CONDUCTOR)
                          AND p.tipo_persona = 'C');

    UPDATE trayecto_tmp t SET t.ESTADO='ERROR',
           t.OBSERVACION = CONCAT(IFNULL(t.OBSERVACION,''),' El conductor no tiene estado PO (Puede Operar) en la relacion con el vehiculo.')
     WHERE t.ID_CARGUE = p_id_cargue AND t.ESTADO IN ('CARGADO','ERROR')
       AND t.IDENTIFICACION_CONDUCTOR IS NOT NULL AND TRIM(t.IDENTIFICACION_CONDUCTOR) <> ''
       AND t.PLACA IS NOT NULL AND TRIM(t.PLACA) <> ''
       AND EXISTS (SELECT 1 FROM persona p
                    WHERE p.identificacion = TRIM(t.IDENTIFICACION_CONDUCTOR) AND p.tipo_persona = 'C')
       AND NOT EXISTS (SELECT 1 FROM persona p
                         JOIN vehiculo_conductor vc ON vc.persona_id = p.id
                         JOIN vehiculos v ON v.id_vehiculo = vc.vehiculo_id
                        WHERE p.identificacion = TRIM(t.IDENTIFICACION_CONDUCTOR)
                          AND p.tipo_persona = 'C'
                          AND v.placa = TRIM(t.PLACA)
                          AND vc.estado = 'PO');

    UPDATE trayecto_tmp t
      JOIN (SELECT CODIGO_RUTA, CAST(ORDEN AS UNSIGNED) AS ORD
              FROM trayecto_tmp
             WHERE ID_CARGUE = p_id_cargue
               AND CODIGO_RUTA IS NOT NULL AND TRIM(CODIGO_RUTA) <> ''
               AND ORDEN REGEXP '^[0-9]{1,5}$'
             GROUP BY CODIGO_RUTA, CAST(ORDEN AS UNSIGNED)
            HAVING COUNT(*) > 1) d
        ON d.CODIGO_RUTA = t.CODIGO_RUTA AND t.ORDEN REGEXP '^[0-9]{1,5}$' AND d.ORD = CAST(t.ORDEN AS UNSIGNED)
       SET t.ESTADO='ERROR',
           t.OBSERVACION = CONCAT(IFNULL(t.OBSERVACION,''),' La combinacion codigo de ruta + orden esta repetida en el archivo.')
     WHERE t.ID_CARGUE = p_id_cargue AND t.ESTADO IN ('CARGADO','ERROR');

    UPDATE trayecto_tmp t SET t.ESTADO='ERROR',
           t.OBSERVACION = CONCAT(IFNULL(t.OBSERVACION,''),' Ya existe un trayecto con ese codigo de ruta y orden.')
     WHERE t.ID_CARGUE = p_id_cargue AND t.ESTADO IN ('CARGADO','ERROR')
       AND t.CODIGO_RUTA IS NOT NULL AND t.ORDEN REGEXP '^[0-9]{1,5}$'
       AND EXISTS (SELECT 1 FROM trayecto y
                    WHERE y.codigo_ruta = TRIM(t.CODIGO_RUTA) AND y.orden_parada = CAST(t.ORDEN AS UNSIGNED));

    UPDATE trayecto_tmp SET ESTADO='VALIDADO'
     WHERE ID_CARGUE = p_id_cargue AND ESTADO = 'CARGADO'
       AND (OBSERVACION IS NULL OR OBSERVACION = '');

    COMMIT;
END $$
DELIMITER ;


-- 4. Procesamiento de Trayectos (Entrega 4)
DROP PROCEDURE IF EXISTS PROCESAR_TRAYECTOS_EXCEL;
DELIMITER $$
CREATE PROCEDURE PROCESAR_TRAYECTOS_EXCEL(IN p_id_cargue BIGINT, IN p_login VARCHAR(100))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    INSERT INTO trayecto (id_persona, id_vehiculo, codigo_ruta, ubicacion, orden_parada, latitud, longitud, login_registro)
    SELECT p.id, v.id_vehiculo, TRIM(t.CODIGO_RUTA), TRIM(t.UBICACION), CAST(t.ORDEN AS UNSIGNED),
           CAST(NULLIF(TRIM(t.LATITUD),'')  AS DOUBLE),
           CAST(NULLIF(TRIM(t.LONGITUD),'') AS DOUBLE),
           p_login
      FROM trayecto_tmp t
      JOIN persona   p ON p.identificacion = TRIM(t.IDENTIFICACION_CONDUCTOR) AND p.tipo_persona = 'C'
      JOIN vehiculos v ON v.placa = TRIM(t.PLACA)
     WHERE t.ID_CARGUE = p_id_cargue AND t.ESTADO = 'VALIDADO';

    UPDATE trayecto_tmp SET ESTADO = 'PROCESADO'
     WHERE ID_CARGUE = p_id_cargue AND ESTADO = 'VALIDADO';

    COMMIT;
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
