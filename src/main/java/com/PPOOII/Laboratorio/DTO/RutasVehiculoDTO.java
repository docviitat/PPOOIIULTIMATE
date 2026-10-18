package com.PPOOII.Laboratorio.DTO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rutas de un vehículo, agrupadas por código de ruta con sus conductores.")
public class RutasVehiculoDTO {
	@Schema(example = "TST001") public String placa;
	public List<RutaConductores> rutas;

	@Schema(description = "Código de ruta y conductores asociados a sus trayectos.")
	public static class RutaConductores {
		@Schema(example = "T-001") public String codigoRuta;
		public List<Conductor> conductores;
	}

	@Schema(description = "Conductor asociado a un trayecto.")
	public static class Conductor {
		@Schema(example = "9001") public String identificacion;
		@Schema(example = "Conductor Uno") public String nombre;
	}
}
