package com.PPOOII.Laboratorio.DTO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Códigos de ruta (sin repetir) en los que participa un conductor.")
public class RutasConductorDTO {
	@Schema(example = "9001") public String identificacion;
	@Schema(example = "Conductor Uno") public String nombre;
	@Schema(example = "[\"R-001\",\"R-005\"]") public List<String> codigosRuta;
}
