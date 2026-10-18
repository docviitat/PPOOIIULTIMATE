package com.PPOOII.Laboratorio.Controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PPOOII.Laboratorio.Entities.Coordenadas;
import com.PPOOII.Laboratorio.Services.CoordenadasServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/LaboratorioV3")
@Tag(name = "coordenadas-controller", description = "Consulta de coordenadas obtenidas con la API de Google Maps")
@SecurityRequirement(name = "bearerAuth")
public class CoordenadasController {

	// ==========INYECCION DEL SERVICE==========
	@Autowired
	@Qualifier("CoordenadasService")
	private CoordenadasServiceImpl coordenadaService;
	
	// ==========MÉTODOS HTTP====================
	// MÉTODO GET
	@Operation(summary = "Se obtienen las coordenadas de las personas (paginado).",
			description = "Las coordenadas (latitud y longitud) se calculan con la integración con la API de Google Maps "
					+ "mediante una tarea programada y se almacenan en base de datos.")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "Coordenadas encontradas", 
	    content = { @Content(mediaType = "application/json", 
	      array = @ArraySchema(schema = @Schema(implementation = Coordenadas.class))) }),
	  @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos", 
	    content = @Content), 
	  @ApiResponse(responseCode = "403", description = "Token ausente o inválido", 
	    content = @Content) })
	@CrossOrigin(origins = "http://localhost")
	@GetMapping("/coordenadas")
	public List<Coordenadas> consultarAllCoordenadas(@ParameterObject Pageable pageable) {
		return coordenadaService.consultarAllCoordenadas(pageable);
	}
	
}
