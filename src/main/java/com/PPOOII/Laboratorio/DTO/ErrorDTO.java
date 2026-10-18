package com.PPOOII.Laboratorio.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de error de la API.")
public class ErrorDTO {
	@Schema(description = "Código HTTP", example = "400")
	private int status;
	@Schema(description = "Descripción del error", example = "El archivo no tiene la estructura de la plantilla.")
	private String error;

	public ErrorDTO() {}
	public ErrorDTO(int status, String error) { this.status = status; this.error = error; }
	public int getStatus() { return status; }
	public void setStatus(int status) { this.status = status; }
	public String getError() { return error; }
	public void setError(String error) { this.error = error; }
}
