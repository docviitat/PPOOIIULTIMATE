package com.PPOOII.Laboratorio.DTO;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de ejecutar un cargue masivo de trayectos.")
public class CargueResumenDTO {

	@Schema(description = "Identificador del cargue (yyyyMMddHHmm). Úselo para consultar el detalle.", example = "202610141530")
	private Long idCargue;
	@Schema(description = "Nombre del archivo recibido", example = "Plantilla_Cargue_Trayectos.xlsx")
	private String archivo;
	@Schema(description = "Total de registros leídos del archivo", example = "6")
	private long totalRegistros;
	@Schema(description = "Cantidad de registros por estado (CARGADO, VALIDADO, PROCESADO, ERROR)")
	private Map<String, Long> porEstado;
	@Schema(description = "Mensaje descriptivo del resultado")
	private String mensaje;

	public CargueResumenDTO() {}

	public CargueResumenDTO(Long idCargue, String archivo, long totalRegistros, Map<String, Long> porEstado, String mensaje) {
		this.idCargue = idCargue;
		this.archivo = archivo;
		this.totalRegistros = totalRegistros;
		this.porEstado = porEstado;
		this.mensaje = mensaje;
	}

	public Long getIdCargue() { return idCargue; }
	public void setIdCargue(Long idCargue) { this.idCargue = idCargue; }
	public String getArchivo() { return archivo; }
	public void setArchivo(String archivo) { this.archivo = archivo; }
	public long getTotalRegistros() { return totalRegistros; }
	public void setTotalRegistros(long totalRegistros) { this.totalRegistros = totalRegistros; }
	public Map<String, Long> getPorEstado() { return porEstado; }
	public void setPorEstado(Map<String, Long> porEstado) { this.porEstado = porEstado; }
	public String getMensaje() { return mensaje; }
	public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
