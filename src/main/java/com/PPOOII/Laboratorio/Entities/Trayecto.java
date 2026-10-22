package com.PPOOII.Laboratorio.Entities;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Entidad transaccional TRAYECTO. A ella solo llegan los registros del cargue
 * masivo que fueron VALIDADOS por el procedimiento almacenado de validación.
 */
@Entity(name = "Trayecto")
@Table(name = "trayecto")
@Schema(description = "Trayecto (parada) de una ruta realizado por un conductor con un vehículo específico.")
public class Trayecto implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_trayecto")
	@Schema(description = "Identificador del trayecto", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@Column(name = "codigo_ruta")
	@Schema(description = "Código de la ruta a la que pertenece el trayecto", example = "T-001")
	private String codigoRuta;

	@Column(name = "orden_parada")
	@Schema(description = "Posición del trayecto dentro de la ruta (inicia en 0)", example = "1")
	private Integer orden;

	@Column(name = "ubicacion")
	@Schema(description = "Ubicación (dirección o lugar) de la parada", example = "Parque Murillo Toro, Ibagué, Tolima")
	private String ubicacion;

	@Column(name = "latitud")
	@Schema(description = "Latitud. La completa la tarea programada con Nominatim / OpenStreetMap", example = "4.4389", nullable = true)
	private Double latitud;

	@Column(name = "longitud")
	@Schema(description = "Longitud. La completa la tarea programada con Nominatim / OpenStreetMap", example = "-75.2322", nullable = true)
	private Double longitud;

	@Transient // no es columna: se resuelve a id_persona
	@Schema(description = "Número de identificación del conductor", example = "9001")
	private String identificacionConductor;

	@Transient // no es columna: se resuelve a id_vehiculo
	@Schema(description = "Placa del vehículo", example = "TST001")
	private String placa;

	@Column(name = "id_persona")
	@Schema(description = "Id de la persona conductora (se calcula a partir de identificacionConductor)", accessMode = Schema.AccessMode.READ_ONLY)
	private Long idPersona;

	@Column(name = "id_vehiculo")
	@Schema(description = "Id del vehículo (se calcula a partir de la placa)", accessMode = Schema.AccessMode.READ_ONLY)
	private Integer idVehiculo;

	@Column(name = "login_registro")
	@Schema(description = "Usuario que registró el trayecto (se toma del token)", accessMode = Schema.AccessMode.READ_ONLY)
	private String loginRegistro;

	public Trayecto() {}

	public Long getIdPersona() { return idPersona; }
	public void setIdPersona(Long idPersona) { this.idPersona = idPersona; }
	public Integer getIdVehiculo() { return idVehiculo; }
	public void setIdVehiculo(Integer idVehiculo) { this.idVehiculo = idVehiculo; }
	public String getLoginRegistro() { return loginRegistro; }
	public void setLoginRegistro(String loginRegistro) { this.loginRegistro = loginRegistro; }

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getCodigoRuta() { return codigoRuta; }
	public void setCodigoRuta(String codigoRuta) { this.codigoRuta = codigoRuta; }
	public Integer getOrden() { return orden; }
	public void setOrden(Integer orden) { this.orden = orden; }
	public String getUbicacion() { return ubicacion; }
	public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
	public Double getLatitud() { return latitud; }
	public void setLatitud(Double latitud) { this.latitud = latitud; }
	public Double getLongitud() { return longitud; }
	public void setLongitud(Double longitud) { this.longitud = longitud; }
	public String getIdentificacionConductor() { return identificacionConductor; }
	public void setIdentificacionConductor(String identificacionConductor) { this.identificacionConductor = identificacionConductor; }
	public String getPlaca() { return placa; }
	public void setPlaca(String placa) { this.placa = placa; }
}
