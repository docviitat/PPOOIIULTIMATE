package com.PPOOII.Laboratorio.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trayecto (parada) de una ruta con la información del conductor y del vehículo.")
public class TrayectoRutaDTO {
	@Schema(example = "1") public int orden;
	@Schema(example = "Parque Murillo Toro, Ibagué, Tolima") public String ubicacion;
	@Schema(example = "4.4389", nullable = true) public Double latitud;
	@Schema(example = "-75.2322", nullable = true) public Double longitud;
	@Schema(example = "9001") public String identificacionConductor;
	@Schema(example = "Conductor Uno") public String nombreConductor;
	@Schema(description = "Estado del conductor con el vehículo: PO, EA o RO", example = "PO", nullable = true) public String estadoConductor;
	@Schema(example = "TST001") public String placa;
	@Schema(description = "true si todos los documentos del vehículo están HABILITADO", example = "true") public boolean vehiculoHabilitado;
	@Schema(description = "Motivo de la restricción (solo en la consulta de restricciones)", nullable = true)
	@JsonInclude(JsonInclude.Include.NON_NULL) public String motivo;
}
