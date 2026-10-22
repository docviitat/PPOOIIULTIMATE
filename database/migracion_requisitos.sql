-- Actualización sin DROP DATABASE y sin borrar datos existentes.
-- Ejecutar UNA VEZ sobre la base elegida: mariadb ppooii < database/migracion_requisitos.sql
-- Compatible con MySQL 8.0.16+ y MariaDB 10.6+.
CREATE TABLE IF NOT EXISTS schema_migration (version VARCHAR(80) PRIMARY KEY, aplicada TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS cargue_archivo (
  tipo VARCHAR(10) NOT NULL,
  id_cargue BIGINT NOT NULL,
  PRIMARY KEY (tipo, id_cargue)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS persona_tmp (
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

CREATE TABLE IF NOT EXISTS mensaje_recibido (
  id VARCHAR(36) PRIMARY KEY,
  contenido TEXT NOT NULL,
  recibido TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS geocoding_cache (
  ubicacion VARCHAR(255) PRIMARY KEY,
  latitud DOUBLE,
  longitud DOUBLE,
  consultado TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DELIMITER $$
DROP PROCEDURE IF EXISTS MIGRAR_REQUISITOS $$
CREATE PROCEDURE MIGRAR_REQUISITOS()
BEGIN
  IF NOT EXISTS(SELECT 1 FROM schema_migration WHERE version='20261019_requisitos') THEN
    ALTER TABLE persona MODIFY identificacion VARCHAR(20) NULL,
      MODIFY tipo_identificacion VARCHAR(5) NULL,
      MODIFY apellidos VARCHAR(100) NULL, MODIFY correo VARCHAR(100) NULL,
      MODIFY tipo_persona VARCHAR(1) NULL;
    IF NOT EXISTS(SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_schema=DATABASE() AND table_name='persona' AND constraint_name='chk_persona_completa') THEN
      ALTER TABLE persona ADD CONSTRAINT chk_persona_completa CHECK (
        (tipo_persona IS NULL AND identificacion IS NULL AND tipo_identificacion IS NULL)
        OR (tipo_persona IS NOT NULL AND identificacion IS NOT NULL AND tipo_identificacion IS NOT NULL
          AND apellidos IS NOT NULL AND correo IS NOT NULL));
    END IF;
    ALTER TABLE trayecto_tmp MODIFY codigo_ruta TEXT, MODIFY orden TEXT,
      MODIFY ubicacion TEXT, MODIFY identificacion_conductor TEXT, MODIFY placa TEXT;
    IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='trayecto_tmp' AND column_name='nombre_conductor') THEN
      ALTER TABLE trayecto_tmp ADD COLUMN nombre_conductor TEXT NULL;
    END IF;
    IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='trayecto_tmp' AND column_name='edad_conductor') THEN
      ALTER TABLE trayecto_tmp ADD COLUMN edad_conductor TEXT NULL;
    END IF;
    IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='trayecto_tmp' AND column_name='login_registro') THEN
      ALTER TABLE trayecto_tmp ADD COLUMN login_registro VARCHAR(100) NULL;
    END IF;
    IF NOT EXISTS(SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_schema=DATABASE() AND table_name='trayecto_tmp' AND constraint_name='chk_trayecto_tmp_estado') THEN
      ALTER TABLE trayecto_tmp ADD CONSTRAINT chk_trayecto_tmp_estado CHECK (estado IN ('CARGADO','VALIDADO','PROCESADO','ERROR'));
    END IF;
    IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='usuario' AND index_name='uq_usuario_login') THEN
      CREATE UNIQUE INDEX uq_usuario_login ON usuario(login);
    END IF;
    IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='usuario' AND index_name='uq_usuario_persona') THEN
      CREATE UNIQUE INDEX uq_usuario_persona ON usuario(idpersona);
    END IF;
    IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='trayecto_tmp' AND index_name='idx_trayecto_tmp_cargue') THEN
      CREATE INDEX idx_trayecto_tmp_cargue ON trayecto_tmp(id_cargue);
    END IF;
    INSERT INTO schema_migration(version) VALUES('20261019_requisitos');
  END IF;
END $$
CALL MIGRAR_REQUISITOS() $$
DROP PROCEDURE MIGRAR_REQUISITOS $$
DROP PROCEDURE IF EXISTS CARGAR_DATOS_EXCEL $$
CREATE PROCEDURE CARGAR_DATOS_EXCEL(IN pnombre TEXT, IN edad TEXT, IN ubicacion TEXT, IN idCargue BIGINT)
BEGIN
  INSERT INTO persona_tmp(pnombre,edad,ubicacion,estado,observacion,id_cargue)
  VALUES(pnombre,edad,ubicacion,'CARGADO','',idCargue);
END $$
DROP PROCEDURE IF EXISTS VALIDAR_DATOS_CARGUE $$
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
DROP PROCEDURE IF EXISTS PROCESAR_DATOS_EXCEL $$
CREATE PROCEDURE PROCESAR_DATOS_EXCEL(IN p_id BIGINT)
BEGIN
  -- El Excel complementario solo suministra nombre, edad y ubicación.
  -- No se inventan documentos de identidad, correos ni usuarios administrativos.
  INSERT INTO persona(nombres,pnombre,edad,ubicacion)
  SELECT pnombre,pnombre,CAST(edad AS UNSIGNED),ubicacion FROM persona_tmp
  WHERE id_cargue=p_id AND estado='VALIDADO';
  UPDATE persona_tmp SET estado='PROCESADO' WHERE id_cargue=p_id AND estado='VALIDADO';
END $$
DROP PROCEDURE IF EXISTS CARGAR_TRAYECTOS_EXCEL $$
CREATE PROCEDURE CARGAR_TRAYECTOS_EXCEL(IN codigoRuta TEXT, IN orden TEXT, IN ubicacion TEXT,
  IN identificacionConductor TEXT, IN placa TEXT, IN nombreConductor TEXT, IN edadConductor TEXT,
  IN loginRegistro VARCHAR(100), IN idCargue BIGINT)
BEGIN
  INSERT INTO trayecto_tmp(codigo_ruta,orden,ubicacion,identificacion_conductor,placa,
    nombre_conductor,edad_conductor,login_registro,id_cargue,estado,observacion)
  VALUES(codigoRuta,orden,ubicacion,identificacionConductor,placa,nombreConductor,edadConductor,
    loginRegistro,idCargue,'CARGADO','');
END $$
DROP PROCEDURE IF EXISTS VALIDAR_TRAYECTOS_CARGUE $$
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
DROP PROCEDURE IF EXISTS PROCESAR_TRAYECTOS_EXCEL $$
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
