package com.PPOOII.Laboratorio.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Documento (entidad paramétrica).")
public class DocumentoDTO {
	@Schema(description="Identificador: se genera en POST y se requiere en PUT", example = "1")
	public Integer idDocumento;
	@Schema(example = "SOAT")
	public String codigoDocumento;
	@Schema(example = "Seguro Obligatorio de Accidentes de Transito")
	public String nombreDocumento;
	@Schema(description = "Tipo de vehículo al que aplica: A (automóvil), M (moto) o AM (ambos)", example = "AM", allowableValues = {"A", "M", "AM"}, requiredMode = Schema.RequiredMode.REQUIRED)
	public String tiposVehiculo;
	@Schema(description = "Obligatoriedad según tipo de vehículo: RA (obligatorio automóvil), RM (obligatorio moto) o RR (obligatorio ambos)", example = "RR", allowableValues = {"RA", "RM", "RR"}, requiredMode = Schema.RequiredMode.REQUIRED)
	public String obligatoriedad;
	@Schema(example = "Seguro obligatorio para todos los vehiculos")
	public String descripcion;
}
