package com.PPOOII.Laboratorio.DTO;

import java.util.List;
import java.util.Map;

import com.PPOOII.Laboratorio.Entities.TrayectoTmp;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de un cargue: totales por estado y registros (paginados).")
public class CargueConsultaDTO {

	@Schema(description = "Identificador del cargue", example = "202610141530")
	private Long idCargue;
	@Schema(description = "Total de registros del cargue", example = "6")
	private long totalRegistros;
	@Schema(description = "Cantidad de registros por estado")
	private Map<String, Long> porEstado;
	@Schema(description = "Página actual (inicia en 0)", example = "0")
	private int pagina;
	@Schema(description = "Tamaño de la página", example = "20")
	private int tamanoPagina;
	@Schema(description = "Registros del cargue en la página solicitada")
	private List<TrayectoTmp> registros;

	public CargueConsultaDTO() {}

	public CargueConsultaDTO(Long idCargue, long totalRegistros, Map<String, Long> porEstado,
			int pagina, int tamanoPagina, List<TrayectoTmp> registros) {
		this.idCargue = idCargue;
		this.totalRegistros = totalRegistros;
		this.porEstado = porEstado;
		this.pagina = pagina;
		this.tamanoPagina = tamanoPagina;
		this.registros = registros;
	}

	public Long getIdCargue() { return idCargue; }
	public void setIdCargue(Long idCargue) { this.idCargue = idCargue; }
	public long getTotalRegistros() { return totalRegistros; }
	public void setTotalRegistros(long totalRegistros) { this.totalRegistros = totalRegistros; }
	public Map<String, Long> getPorEstado() { return porEstado; }
	public void setPorEstado(Map<String, Long> porEstado) { this.porEstado = porEstado; }
	public int getPagina() { return pagina; }
	public void setPagina(int pagina) { this.pagina = pagina; }
	public int getTamanoPagina() { return tamanoPagina; }
	public void setTamanoPagina(int tamanoPagina) { this.tamanoPagina = tamanoPagina; }
	public List<TrayectoTmp> getRegistros() { return registros; }
	public void setRegistros(List<TrayectoTmp> registros) { this.registros = registros; }
}
