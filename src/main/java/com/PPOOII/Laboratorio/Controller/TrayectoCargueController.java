package com.PPOOII.Laboratorio.Controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PPOOII.Laboratorio.DTO.CargueConsultaDTO;
import com.PPOOII.Laboratorio.DTO.CargueResumenDTO;
import com.PPOOII.Laboratorio.DTO.ErrorDTO;
import com.PPOOII.Laboratorio.Services.Interfaces.ITrayectoCargueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Entrega 4 del proyecto: cargue masivo de trayectos desde Excel. Servicios protegidos (token). */
@RestController
@RequestMapping("/LaboratorioV4")
@Tag(name = "cargue-trayectos", description = "Cargue masivo de trayectos desde Excel (Entrega 4) y consulta de cargues")
@SecurityRequirement(name = "bearerAuth")
public class TrayectoCargueController {

	@Autowired
	@Qualifier("TrayectoCargueService")
	private ITrayectoCargueService trayectoCargueService;

	@Operation(summary = "Carga trayectos desde la plantilla Excel.",
			description = "Recibe la plantilla Excel (CODIGO_RUTA, ORDEN, UBICACION, IDENTIFICACION_CONDUCTOR, PLACA) "
					+ "y ejecuta los procedimientos almacenados CARGAR_TRAYECTOS_EXCEL, VALIDAR_TRAYECTOS_CARGUE y "
					+ "PROCESAR_TRAYECTOS_EXCEL. Retorna el idCargue y el total de registros por estado.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Cargue ejecutado",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = CargueResumenDTO.class))),
		@ApiResponse(responseCode = "400", description = "Archivo ausente, vacío, no es Excel o no cumple la plantilla",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@PostMapping(value = "/trayectos/cargue", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CargueResumenDTO> cargarTrayectos(
			@Parameter(description = "Archivo Excel (.xlsx) con la plantilla de trayectos", required = true)
			@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(trayectoCargueService.procesarArchivo(file));
	}

	@Operation(summary = "Consulta un cargue de trayectos por su identificador.",
			description = "Retorna los registros de trayecto_tmp del cargue, con su estado y observaciones acumuladas, "
					+ "y el total por estado (CARGADO, VALIDADO, PROCESADO, ERROR).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Cargue encontrado",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = CargueConsultaDTO.class))),
		@ApiResponse(responseCode = "404", description = "No existe un cargue con ese identificador",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@GetMapping("/trayectos/cargue/{idCargue}")
	public ResponseEntity<CargueConsultaDTO> consultarCargue(
			@Parameter(description = "Identificador del cargue (yyyyMMddHHmm)", example = "202610141530")
			@PathVariable("idCargue") Long idCargue,
			@ParameterObject @PageableDefault(size = 50) Pageable pageable) {
		return ResponseEntity.ok(trayectoCargueService.consultarCargue(idCargue, pageable));
	}

	@Operation(summary = "Descarga la plantilla Excel para el cargue de trayectos.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Plantilla .xlsx",
			content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@GetMapping("/trayectos/plantilla")
	public ResponseEntity<byte[]> descargarPlantilla() {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Plantilla_Cargue_Trayectos.xlsx")
				.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(trayectoCargueService.generarPlantilla());
	}
}
