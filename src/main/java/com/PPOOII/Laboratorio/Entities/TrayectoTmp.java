package com.PPOOII.Laboratorio.Entities;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad temporal TRAYECTO_TMP. Replica los campos de TRAYECTO como texto (tal
 * como llegan del Excel) y agrega ESTADO, OBSERVACION e ID_CARGUE.
 */
@Entity(name = "TrayectoTmp")
@Table(name = "trayecto_tmp")
@Schema(description = "Registro temporal de un cargue masivo de trayectos desde Excel.")
public class TrayectoTmp implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	@Schema(description = "Identificador del registro temporal (PK)", example = "15")
	private Long id;

	@Column(name = "CODIGO_RUTA")
	@Schema(description = "Código de ruta tal como viene en el archivo", example = "R-001")
	private String codigoRuta;

	@Column(name = "ORDEN")
	@Schema(description = "Orden del trayecto (texto, tal como viene en el archivo)", example = "1")
	private String orden;

	@Column(name = "UBICACION")
	@Schema(description = "Ubicación de la parada", example = "Parque Murillo Toro, Ibagué, Tolima")
	private String ubicacion;

	@Column(name = "LATITUD")
	@Schema(description = "Latitud (no viene en la plantilla; la completa la tarea programada)", nullable = true)
	private String latitud;

	@Column(name = "LONGITUD")
	@Schema(description = "Longitud (no viene en la plantilla; la completa la tarea programada)", nullable = true)
	private String longitud;

	@Column(name = "IDENTIFICACION_CONDUCTOR")
	@Schema(description = "Identificación del conductor", example = "1001")
	private String identificacionConductor;

	@Column(name = "PLACA")
	@Schema(description = "Placa del vehículo", example = "ABC123")
	private String placa;

	@Column(name = "ESTADO")
	@Schema(description = "Estado del registro en el proceso de cargue",
			allowableValues = { "CARGADO", "VALIDADO", "PROCESADO", "ERROR" }, example = "VALIDADO")
	private String estado;

	@Column(name = "OBSERVACION")
	@Schema(description = "Errores o restricciones encontradas (acumulativo). Vacío si no hay.",
			example = " El vehiculo tiene documentos que no estan en estado HABILITADO.")
	private String observacion;

	@Column(name = "ID_CARGUE")
	@Schema(description = "Identificador del cargue: año, mes, día, hora y minutos (yyyyMMddHHmm)", example = "202610141530")
	private Long idCargue;

    @Column(name="nombre_conductor")
    @Schema(description="Nombre del conductor tal como viene en Excel (opcional)")
    public String nombreConductor;
    @Column(name="edad_conductor")
    @Schema(description="Edad del conductor tal como viene en Excel (opcional)")
    public String edadConductor;
    @Column(name="login_registro")
    @Schema(description="Usuario que realizó el cargue")
    public String loginRegistro;

	public TrayectoTmp() {}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getCodigoRuta() { return codigoRuta; }
	public void setCodigoRuta(String codigoRuta) { this.codigoRuta = codigoRuta; }
	public String getOrden() { return orden; }
	public void setOrden(String orden) { this.orden = orden; }
	public String getUbicacion() { return ubicacion; }
	public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
	public String getLatitud() { return latitud; }
	public void setLatitud(String latitud) { this.latitud = latitud; }
	public String getLongitud() { return longitud; }
	public void setLongitud(String longitud) { this.longitud = longitud; }
	public String getIdentificacionConductor() { return identificacionConductor; }
	public void setIdentificacionConductor(String identificacionConductor) { this.identificacionConductor = identificacionConductor; }
	public String getPlaca() { return placa; }
	public void setPlaca(String placa) { this.placa = placa; }
	public String getEstado() { return estado; }
	public void setEstado(String estado) { this.estado = estado; }
	public String getObservacion() { return observacion; }
	public void setObservacion(String observacion) { this.observacion = observacion; }
	public Long getIdCargue() { return idCargue; }
	public void setIdCargue(Long idCargue) { this.idCargue = idCargue; }
}
