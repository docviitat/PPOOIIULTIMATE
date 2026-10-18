package com.PPOOII.Laboratorio.DTO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ruta con sus trayectos en orden.")
public class RutaDTO {
	@Schema(example = "T-001") public String codigoRuta;
	@Schema(example = "3") public int totalTrayectos;
	public List<TrayectoRutaDTO> trayectos;

	public RutaDTO() {}
	public RutaDTO(String codigoRuta, List<TrayectoRutaDTO> trayectos) {
		this.codigoRuta = codigoRuta;
		this.trayectos = trayectos;
		this.totalTrayectos = trayectos.size();
	}
}
