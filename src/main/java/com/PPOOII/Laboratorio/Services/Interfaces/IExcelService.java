package com.PPOOII.Laboratorio.Services.Interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface IExcelService {

	/** Lee el Excel de personas y ejecuta cargue -> validación -> procesado. Retorna el idCargue. */
	double processExcel(MultipartFile file);

	boolean cargarDatosExcel(String pnombre, String edad, String ubicacion, Double idCargue);

	boolean validarDatosExcel(Double idCargue);

	boolean procesarDatosExcel(Double idCargue);
}
