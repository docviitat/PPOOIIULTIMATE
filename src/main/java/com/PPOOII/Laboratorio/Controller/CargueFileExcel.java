package com.PPOOII.Laboratorio.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PPOOII.Laboratorio.DTO.ErrorDTO;
import com.PPOOII.Laboratorio.Services.Interfaces.IExcelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/LaboratorioV3")
@Tag(name = "cargue-personas-excel", description = "Cargue masivo de personas desde un archivo de Excel (Laboratorio 4 complementario)")
public class CargueFileExcel {

	@Autowired
	private IExcelService excelService;

	@Operation(summary = "Carga masivamente personas desde un archivo Excel.",
			description = "Lee la primera hoja del archivo (columnas NOMBRE, EDAD, UBICACIÓN) y ejecuta los tres "
					+ "procedimientos almacenados: CARGAR_DATOS_EXCEL (a persona_tmp), VALIDAR_DATOS_CARGUE y "
					+ "PROCESAR_DATOS_EXCEL (a persona). Este servicio no requiere token.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Archivo cargado y procesado. Retorna el identificador del cargue.",
			content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
		@ApiResponse(responseCode = "400", description = "Archivo vacío, ausente o ilegible.",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))) })
	@PostMapping(value = "/upload-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String handleFileUpload(
			@Parameter(description = "Archivo Excel (.xlsx) con las columnas NOMBRE, EDAD, UBICACIÓN")
			@RequestParam("file") MultipartFile file) {
		double idCargue = excelService.processExcel(file);
		return "Archivo subido y procesado exitosamente. idCargue=" + String.format("%.0f", idCargue);
	}
}
