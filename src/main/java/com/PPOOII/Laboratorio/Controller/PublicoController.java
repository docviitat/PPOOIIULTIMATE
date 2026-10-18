package com.PPOOII.Laboratorio.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PPOOII.Laboratorio.DTO.VehiculoDTO;
import com.PPOOII.Laboratorio.Services.ConductorService;
import com.PPOOII.Laboratorio.Services.VehiculoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Entrega 2: servicios públicos (NO requieren token). */
@RestController
@RequestMapping("/LaboratorioV2/publico")
@Tag(name = "servicios-publicos", description = "Servicios públicos que no requieren token (Entrega 2)")
public class PublicoController {

	@Autowired @Qualifier("VehiculoService") private VehiculoService vehiculos;
	@Autowired @Qualifier("ConductorService") private ConductorService conductores;

	@Operation(summary = "Consulta todos los vehículos que tengan documentos vencidos.")
	@GetMapping("/vehiculos/documentos-vencidos")
	public List<VehiculoDTO> documentosVencidos() {
		return vehiculos.vehiculosConDocumentosVencidos();
	}

	@Operation(summary = "Consulta todos los conductores que puedan operar (estado PO).")
	@GetMapping("/conductores/pueden-operar")
	public List<Map<String, Object>> conductoresPuedenOperar() {
		return conductores.conductoresQuePuedenOperar();
	}

	@Operation(summary = "Consulta un vehículo por placa con la información de sus conductores y documentos.")
	@GetMapping("/vehiculos/placa/{placa}")
	public Map<String, Object> vehiculoPorPlaca(@PathVariable("placa") String placa) {
		return vehiculos.vehiculoConConductores(placa);
	}

	@Operation(summary = "Consulta los vehículos con documentos por vencer en los próximos N días (parámetro dias).")
	@GetMapping("/vehiculos/documentos-por-vencer")
	public List<VehiculoDTO> documentosPorVencer(@RequestParam("dias") int dias) {
		return vehiculos.vehiculosConDocumentosPorVencer(dias);
	}

	@Operation(summary = "Consulta el total de personas agrupadas por tipo.")
	@GetMapping("/personas/total-por-tipo")
	public List<Map<String, Object>> totalPorTipo() {
		return conductores.totalPersonasPorTipo();
	}
}
