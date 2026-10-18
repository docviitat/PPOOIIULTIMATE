package com.PPOOII.Laboratorio.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Documento (entidad paramétrica).")
public class DocumentoDTO {
	@Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
	public Integer idDocumento;
	@Schema(example = "SOAT")
	public String codigoDocumento;
	@Schema(example = "Seguro Obligatorio de Accidentes de Transito")
	public String nombreDocumento;
	@Schema(description = "Tipo de vehículo al que aplica: A (automóvil), M (moto) o AM (ambos)", example = "AM")
	public String tiposVehiculo;
	@Schema(description = "Obligatoriedad según tipo de vehículo: RA (obligatorio automóvil), RM (obligatorio moto) o RR (obligatorio ambos)", example = "RR")
	public String obligatoriedad;
	@Schema(example = "Seguro obligatorio para todos los vehiculos")
	public String descripcion;
}
