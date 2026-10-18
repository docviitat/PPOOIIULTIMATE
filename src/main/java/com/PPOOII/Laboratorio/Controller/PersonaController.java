package com.PPOOII.Laboratorio.Controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PPOOII.Laboratorio.DTO.PersonaCreadaDTO;
import com.PPOOII.Laboratorio.Entities.Persona;
import com.PPOOII.Laboratorio.Services.PersonaServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/LaboratorioV1")
@Tag(name = "persona-controller", description = "CRUD y búsquedas de personas")
@SecurityRequirement(name = "bearerAuth")
public class PersonaController {
	
	// ==========INYECCION DEL SERVICE==========
	@Autowired
	@Qualifier("PersonaService")
	private PersonaServiceImpl personaService;
	
	// ==========MÉTODOS HTTP====================
	// METODO POST
	@Operation(summary = "Crea una persona.",
			description = "Si la persona es ADMINISTRATIVO la aplicación genera su usuario: login por nemotecnia "
					+ "(inicial del primer nombre + primer apellido), password y APIKey automáticos. Las credenciales "
					+ "se devuelven solo en esta respuesta. La licencia (BLOB) viaja en BASE64.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Persona creada (y credenciales si es ADMINISTRATIVO)",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = PersonaCreadaDTO.class))),
		@ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
		@ApiResponse(responseCode = "409", description = "La identificación ya existe", content = @Content),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@PostMapping("/persona")
	public PersonaCreadaDTO agregarPersona(@RequestBody Persona persona) {
		return personaService.crear(persona);
	}
	// MÉTODO PUT
	@Operation(summary = "Actualiza una persona existente.", description = "El id de la persona es obligatorio y debe ser distinto de 0. Si pasa a ADMINISTRATIVO y no tiene usuario, se le crea.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Persona actualizada",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = PersonaCreadaDTO.class))),
		@ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
		@ApiResponse(responseCode = "404", description = "La persona no existe", content = @Content),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@PutMapping("/persona")
	public PersonaCreadaDTO editarPersona(@RequestBody Persona persona) {
		return personaService.modificar(persona);
	}
	// MÉTODO DELETE
	@Operation(summary = "Elimina una persona por su id.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Resultado de la operación (true/false)",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@DeleteMapping("/persona/{id}")
	public boolean eliminarPersona(@Parameter(description = "Id de la persona", example = "1") @PathVariable("id") int id) {
		return personaService.eliminar(id);
	}
	// MÉTODO GET
	@Operation(summary = "Lista las personas (paginado).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Listado de personas",
			content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Persona.class)))),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@GetMapping("/personas")
	public List<Persona> listadoPersona(@ParameterObject Pageable pageable) {
		return personaService.consultarPersona(pageable);
	}

	// ==============MÉTODOS HTTP DE BÚSQUEDA =============
	// ---GET---
	@Operation(summary = "Busca una persona por su id.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Persona encontrada (null si no existe)",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = Persona.class))),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@GetMapping("/persona/id/{id}")
	public Persona getById(@Parameter(description = "Id de la persona", example = "1") @PathVariable("id") int id) {
		return personaService.findById(id);
	}
	// ---GET---
	@Operation(summary = "Busca personas por primer nombre.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Personas con ese nombre",
			content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Persona.class)))),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@GetMapping("/persona/pnombre/{pnombre}")
	public List<Persona> getByPNombre(@Parameter(description = "Primer nombre", example = "Homero") @PathVariable("pnombre") String pnombre) {
		return personaService.findByNombre(pnombre);
	}
	// ---GET---
	@Operation(summary = "Busca personas por edad.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Personas con esa edad",
			content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Persona.class)))),
		@ApiResponse(responseCode = "403", description = "Token ausente o inválido", content = @Content) })
	@GetMapping("/persona/edad/{edad}")
	public List<Persona> getByEdad(@Parameter(description = "Edad en años", example = "25") @PathVariable("edad") int edad) {
		return personaService.findByEdad(edad);
	}
}
