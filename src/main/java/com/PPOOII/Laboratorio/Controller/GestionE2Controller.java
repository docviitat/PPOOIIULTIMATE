package com.PPOOII.Laboratorio.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PPOOII.Laboratorio.DTO.DocumentoVehiculoDTO;
import com.PPOOII.Laboratorio.DTO.VehiculoDTO;
import com.PPOOII.Laboratorio.Services.ConductorService;
import com.PPOOII.Laboratorio.Services.UsuarioGestionService;
import com.PPOOII.Laboratorio.Services.VehiculoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Entrega 2: usuarios, cargue de documentos y relación conductor-vehículo. Requieren token. */
@RestController
@RequestMapping("/LaboratorioV2")
@Tag(name = "02. Usuarios, documentos y conductores", description = "Usuarios, cargue de documentos y conductores (Entrega 2)")
@SecurityRequirement(name = "bearerAuth")
public class GestionE2Controller {

	@Autowired @Qualifier("UsuarioGestionService") private UsuarioGestionService usuarios;
	@Autowired @Qualifier("VehiculoService") private VehiculoService vehiculos;
	@Autowired @Qualifier("ConductorService") private ConductorService conductores;

	@Operation(summary = "Cambia la contraseña de un usuario.",
			description = "El login va en la URL y la nueva contraseña en el body: {\"password\": \"NuevaClave123\"}.")
	@PutMapping("/usuarios/{login}/password")
	public Map<String, Object> cambiarPassword(@PathVariable("login") String login, @RequestBody com.PPOOII.Laboratorio.DTO.GestionDTO.Password body) {
		return usuarios.cambiarPassword(login, body == null ? null : body.password());
	}

	@Operation(summary = "Genera nuevamente el APIKey de un usuario específico.")
	@GetMapping("/usuarios/{login}/apikey")
	public Map<String, Object> regenerarApiKey(@PathVariable("login") String login) {
		return usuarios.regenerarApiKey(login);
	}

	@Operation(summary = "Carga y/o actualiza uno o varios documentos PDF (BASE64) de un vehículo.",
			description = "Body: lista de documentos con idDocumento o codigoDocumento, numeroDocumento, fechaExpedicion, "
					+ "fechaVencimiento y archivoBase64. Si el vehículo ya tiene el documento se actualiza; el documento "
					+ "queda en 'En Verificacion'.")
	@PostMapping("/vehiculos/{placa}/documentos")
	public VehiculoDTO cargarDocumentos(@PathVariable("placa") String placa, @RequestBody List<DocumentoVehiculoDTO> documentos) {
		return vehiculos.cargarDocumentos(placa, documentos);
	}

	@Operation(summary = "Cambia el estado de un documento asociado al vehículo.",
			description = "Permite resolver el estado inicial En Verificacion. No permite habilitar un documento vencido. "
					+ "idDocumento identifica el documento paramétrico, no el id de la relación. Requiere JWT.")
	@PutMapping("/vehiculos/{placa}/documentos/{idDocumento}/estado")
	public VehiculoDTO cambiarEstadoDocumento(@PathVariable("placa") String placa,
			@PathVariable("idDocumento") int idDocumento,
			@RequestBody com.PPOOII.Laboratorio.DTO.GestionDTO.EstadoDocumento body) {
		return vehiculos.cambiarEstadoDocumento(placa, idDocumento, body == null ? null : body.estado());
	}

	@Operation(summary = "Asocia los vehículos que puede operar un conductor.",
			description = "Body: {\"placas\": [\"ABC123\", \"XYZ789\"]}. Cada asociación nueva queda en estado EA.")
	@PostMapping("/conductores/{identificacion}/vehiculos")
	public List<Map<String, Object>> asociarVehiculos(@PathVariable("identificacion") String identificacion,
			@RequestBody com.PPOOII.Laboratorio.DTO.GestionDTO.Placas body) {
		return conductores.asociarVehiculos(identificacion, body == null ? null : body.placas());
	}

	@Operation(summary = "Cambia el estado del conductor en relación con un vehículo (PO, EA, RO).",
			description = "Body: {\"estado\": \"PO\"}. PO = Puede Operar, EA = Espera de Aprobación, RO = Restringido para Operar.")
	@PutMapping("/conductores/{identificacion}/vehiculos/{placa}/estado")
	public Map<String, Object> cambiarEstado(@PathVariable("identificacion") String identificacion,
			@PathVariable("placa") String placa, @RequestBody com.PPOOII.Laboratorio.DTO.GestionDTO.Estado body) {
		return conductores.cambiarEstado(identificacion, placa, body == null ? null : body.estado());
	}
}
