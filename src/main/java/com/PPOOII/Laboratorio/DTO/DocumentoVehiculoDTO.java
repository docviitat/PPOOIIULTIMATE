package com.PPOOII.Laboratorio.DTO;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

/** Documento asociado a un vehículo (relación vehículos con documentos). */
@Schema(description = "Documento asociado a un vehículo.")
public class DocumentoVehiculoDTO {
	@Schema(description = "Id de la relación (solo lectura)", accessMode = Schema.AccessMode.READ_ONLY)
	public Long id;
	@Schema(description = "Id del documento paramétrico (o envíe codigoDocumento)", example = "1")
	public Integer idDocumento;
	@Schema(description = "Código del documento paramétrico (alternativa a idDocumento)", example = "SOAT")
	public String codigoDocumento;
	@Schema(description = "Nombre del documento (solo lectura)", accessMode = Schema.AccessMode.READ_ONLY)
	public String nombreDocumento;
	@Schema(example = "SOAT-0001")
	public String numeroDocumento;
	@Schema(example = "2026-01-01")
	public LocalDate fechaExpedicion;
	@Schema(example = "2027-01-01")
	public LocalDate fechaVencimiento;
	@Schema(description = "Habilitado, Vencido o En Verificacion (solo lectura: al crear siempre es En Verificacion)", accessMode = Schema.AccessMode.READ_ONLY)
	public String estadoDocumento;
	@Schema(description = "Archivo PDF en BASE64 (obligatorio en el servicio de cargue de documentos)", accessMode = Schema.AccessMode.WRITE_ONLY)
	public String archivoBase64;
	@Schema(description = "Indica si ya hay un archivo cargado (solo lectura)", accessMode = Schema.AccessMode.READ_ONLY)
	public Boolean tieneArchivo;
}
