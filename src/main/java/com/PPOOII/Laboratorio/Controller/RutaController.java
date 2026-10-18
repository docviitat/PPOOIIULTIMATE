package com.PPOOII.Laboratorio.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PPOOII.Laboratorio.DTO.ErrorDTO;
import com.PPOOII.Laboratorio.DTO.RutaDTO;
import com.PPOOII.Laboratorio.DTO.RutasConductorDTO;
import com.PPOOII.Laboratorio.DTO.RutasVehiculoDTO;
import com.PPOOII.Laboratorio.Entities.Trayecto;
import com.PPOOII.Laboratorio.Services.Interfaces.IRutaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Entrega 3: consulta de rutas y trayectos. Todos los servicios requieren token. */
@RestController
@RequestMapping("/LaboratorioV3")
@Tag(name = "rutas-trayectos", description = "Consulta de rutas y trayectos (Entrega 3)")
@SecurityRequirement(name = "bearerAuth")
public class RutaController {

	@Autowired @Qualifier("RutaService") private IRutaService rutaService;

	@Operation(summary = "Consulta una ruta por su código: trayectos en orden con su información asociada.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Ruta encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RutaDTO.class))),
		@ApiResponse(responseCode = "404", description = "La ruta no existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@GetMapping("/rutas/{codigoRuta}")
	public RutaDTO consultarRuta(@Parameter(description = "Código de la ruta", example = "T-001") @PathVariable("codigoRuta") String codigoRuta) {
		return rutaService.consultarRuta(codigoRuta);
	}

	@Operation(summary = "Consulta los códigos de ruta (agrupados, sin repetir) de un conductor por su identificación.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Códigos de ruta del conductor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RutasConductorDTO.class))),
		@ApiResponse(responseCode = "404", description = "El conductor no existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@GetMapping("/rutas/conductor/{identificacion}")
	public RutasConductorDTO rutasPorConductor(@Parameter(description = "Identificación del conductor", example = "9001") @PathVariable("identificacion") String identificacion) {
		return rutaService.consultarRutasPorConductor(identificacion);
	}

	@Operation(summary = "Consulta por placa el código de ruta y el conductor asociado al trayecto, de forma agrupada.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Rutas del vehículo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RutasVehiculoDTO.class))),
		@ApiResponse(responseCode = "404", description = "El vehículo no existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@GetMapping("/rutas/vehiculo/{placa}")
	public RutasVehiculoDTO rutasPorVehiculo(@Parameter(description = "Placa del vehículo", example = "TST001") @PathVariable("placa") String placa) {
		return rutaService.consultarRutasPorVehiculo(placa);
	}

	@Operation(summary = "Consulta las rutas y trayectos donde el vehículo NO está habilitado o el conductor está Restringido para Operar.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Rutas con restricciones (lista vacía si no hay)", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RutaDTO.class)))),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@GetMapping("/rutas/restricciones")
	public List<RutaDTO> rutasConRestricciones() {
		return rutaService.consultarRutasConRestricciones();
	}

	@Operation(summary = "Crea un trayecto.",
			description = "Solo se crea si los documentos del vehículo están HABILITADO y el conductor tiene estado PO con ese vehículo.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "Trayecto creado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Trayecto.class))),
		@ApiResponse(responseCode = "400", description = "Datos inválidos, vehículo o conductor inexistente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
		@ApiResponse(responseCode = "409", description = "Vehículo no habilitado, conductor sin PO o trayecto duplicado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@PostMapping("/trayectos")
	public ResponseEntity<Trayecto> crearTrayecto(@RequestBody Trayecto trayecto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(rutaService.crearTrayecto(trayecto));
	}
}
