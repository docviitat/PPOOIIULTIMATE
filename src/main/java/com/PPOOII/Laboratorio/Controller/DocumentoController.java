package com.PPOOII.Laboratorio.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PPOOII.Laboratorio.DTO.DocumentoDTO;
import com.PPOOII.Laboratorio.Services.DocumentoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Entrega 1: CRUD de la entidad paramétrica de documentos. Requiere token. */
@RestController
@RequestMapping("/LaboratorioV1")
@Tag(name = "documento-controller", description = "CRUD de la entidad paramétrica de documentos (Entrega 1)")
@SecurityRequirement(name = "bearerAuth")
public class DocumentoController {

	@Autowired @Qualifier("DocumentoService") private DocumentoService service;

	@Operation(summary = "Crea un documento paramétrico (POST).")
	@PostMapping("/documento")
	public ResponseEntity<DocumentoDTO> crear(@RequestBody DocumentoDTO d) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(d));
	}

	@Operation(summary = "Consulta un documento por su id (GET).")
	@GetMapping("/documento/{id}")
	public DocumentoDTO consultar(@PathVariable("id") int id) {
		return service.consultar(id);
	}

	@Operation(summary = "Lista los documentos (GET, paginado).")
	@GetMapping("/documentos")
	public List<DocumentoDTO> listar(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
		return service.listar(page, size);
	}

	@Operation(summary = "Actualiza un documento (PUT). El idDocumento es obligatorio.")
	@PutMapping("/documento")
	public DocumentoDTO actualizar(@RequestBody DocumentoDTO d) {
		return service.actualizar(d);
	}

	@Operation(summary = "Elimina un documento (DELETE). No se puede si está asociado a vehículos.")
	@DeleteMapping("/documento/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
