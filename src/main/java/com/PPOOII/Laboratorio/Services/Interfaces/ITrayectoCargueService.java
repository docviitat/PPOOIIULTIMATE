package com.PPOOII.Laboratorio.Services.Interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.PPOOII.Laboratorio.DTO.CargueConsultaDTO;
import com.PPOOII.Laboratorio.DTO.CargueResumenDTO;

public interface ITrayectoCargueService {

	/** Orquesta el cargue completo: lee el Excel y ejecuta cargue -> validación -> procesado. */
	CargueResumenDTO procesarArchivo(MultipartFile file);

	/** Invoca el procedimiento almacenado de CARGUE para un registro del archivo. */
	boolean cargarTrayecto(String codigoRuta, String orden, String ubicacion,
			String identificacionConductor, String placa, Long idCargue);

	/** Invoca el procedimiento almacenado de VALIDACIÓN. */
	boolean validarCargue(Long idCargue);

	/** Invoca el procedimiento almacenado de PROCESADO. */
	boolean procesarCargue(Long idCargue);

	/** Consulta de un cargue por su identificador. */
	CargueConsultaDTO consultarCargue(Long idCargue, Pageable pageable);

	/** Genera la plantilla Excel (.xlsx) definida para el cargue de trayectos. */
	byte[] generarPlantilla();

	/** Excel con tres paradas de ejemplo y guía. */
	byte[] generarEjemplo();
}
