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

import com.PPOOII.Laboratorio.DTO.DocumentoVehiculoDTO;
import com.PPOOII.Laboratorio.DTO.VehiculoDTO;
import com.PPOOII.Laboratorio.Services.VehiculoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Entrega 1: CRUD de vehículos, consultas y asociación de documentos. Requiere token. */
@RestController
@RequestMapping("/LaboratorioV1")
@Tag(name = "vehiculo-controller", description = "CRUD, consultas y documentos de vehículos (Entrega 1)")
@SecurityRequirement(name = "bearerAuth")
public class VehiculoController {

	@Autowired @Qualifier("VehiculoService") private VehiculoService service;

	@Operation(summary = "Crea un vehículo (POST).",
			description = "Debe enviarse al menos un documento asociado; todos los documentos quedan en estado 'En Verificacion'.")
	@PostMapping("/vehiculo")
	public ResponseEntity<VehiculoDTO> crear(@RequestBody VehiculoDTO v) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(v));
	}

	@Operation(summary = "Consulta un vehículo por id con sus documentos (GET).")
	@GetMapping("/vehiculo/{id}")
	public VehiculoDTO consultar(@PathVariable("id") int id) {
		return service.consultar(id);
	}

	@Operation(summary = "Lista los vehículos (GET, paginado).")
	@GetMapping("/vehiculos")
	public List<VehiculoDTO> listar(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
		return service.listar(page, size);
	}

	@Operation(summary = "Actualiza los datos de un vehículo (PUT). El idVehiculo es obligatorio; los documentos no se modifican.")
	@PutMapping("/vehiculo")
	public VehiculoDTO actualizar(@RequestBody VehiculoDTO v) {
		return service.actualizar(v);
	}

	@Operation(summary = "Elimina un vehículo y sus documentos (DELETE). No se puede si tiene trayectos o conductores asociados.")
	@DeleteMapping("/vehiculo/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	// ---------------- Consultas ----------------
	@Operation(summary = "Busca un vehículo por número de placa.")
	@GetMapping("/vehiculo/placa/{placa}")
	public VehiculoDTO porPlaca(@PathVariable("placa") String placa) {
		return service.buscarPorPlaca(placa);
	}

	@Operation(summary = "Busca vehículos por tipo de vehículo (Automovil o Motocicleta).")
	@GetMapping("/vehiculos/tipo/{tipo}")
	public List<VehiculoDTO> porTipo(@PathVariable("tipo") String tipo) {
		return service.buscarPorTipo(tipo);
	}

	@Operation(summary = "Busca vehículos que tengan en común un tipo de documento (por código, p. ej. SOAT).")
	@GetMapping("/vehiculos/documento/{codigoDocumento}")
	public List<VehiculoDTO> porDocumento(@PathVariable("codigoDocumento") String codigo) {
		return service.buscarPorTipoDocumento(codigo);
	}

	@Operation(summary = "Busca vehículos según el estado de un documento asociado: Habilitado, Vencido o En Verificacion.")
	@GetMapping("/vehiculos/estado-documento/{estado}")
	public List<VehiculoDTO> porEstadoDocumento(@PathVariable("estado") String estado) {
		return service.buscarPorEstadoDocumento(estado);
	}

	// ---------------- Documentos de un vehículo ----------------
	@Operation(summary = "Agrega documentos a un vehículo (quedan 'En Verificacion').")
	@PostMapping("/vehiculo/{id}/documentos")
	public ResponseEntity<VehiculoDTO> agregarDocumentos(@PathVariable("id") int id, @RequestBody List<DocumentoVehiculoDTO> documentos) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.agregarDocumentos(id, documentos));
	}
}
