package com.PPOOII.Laboratorio.Entities;

import java.io.Serializable;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Persona (CONDUCTOR = 'C' o ADMINISTRATIVO = 'A'). Mapea la tabla persona. */
@Entity(name = "persona")
@Table(name = "persona")
@Schema(description = "Persona registrada en la aplicación.")
public class Persona implements Serializable {

	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	@Schema(description = "Identificador de la persona (autogenerado)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
	public int id;

	@Column(name = "identificacion")
	@Schema(description = "Número de identificación (único)", example = "1001234567")
	private String identificacion;

	@Column(name = "tipo_identificacion")
	@Schema(description = "Tipo de identificación: CC, CE o PAS", example = "CC")
	private String tipoIdentificacion;

	@Column(name = "nombres")
	@Schema(description = "Nombres", example = "Homero Jay")
	private String nombres;

	@Column(name = "pnombre")
	@Schema(description = "Primer nombre (si no se envía se toma de nombres)", example = "Homero")
	private String pnombre;

	@Column(name = "apellidos")
	@Schema(description = "Apellidos", example = "Simpson")
	private String apellidos;

	@Column(name = "edad")
	@Schema(description = "Edad en años", example = "31")
	private Integer edad;

	@Column(name = "ubicacion")
	@Schema(description = "Ubicación (dirección o lugar) de la persona", example = "Centro Comercial Multicentro, Ibagué, Tolima")
	private String ubicacion;

	@Column(name = "correo")
	@Schema(description = "Correo electrónico", example = "homero@example.com")
	private String correo;

	@Column(name = "tipo_persona")
	@Schema(description = "Tipo de persona: C / CONDUCTOR o A / ADMINISTRATIVO (se guarda C o A)", example = "C")
	private String tipoPersona;

	@Column(name = "fecha_vigencia_licencia")
	@Schema(description = "Fecha de vigencia de la licencia de conducción", example = "2030-12-31", nullable = true)
	private LocalDate fechaLicencia;

	@Column(name = "licencia_conduccion", columnDefinition = "LONGBLOB")
	@Schema(description = "Archivo de la licencia. En el JSON viaja en BASE64; en la BD se guarda como BLOB", nullable = true)
	private byte[] licencia;

	public Persona() {}

	public Persona(String pnombre, int edad, String ubicacion) {
		this.pnombre = pnombre;
		this.edad = edad;
		this.ubicacion = ubicacion;
	}

	public Persona(int id, String pnombre, int edad, String ubicacion) {
		this.id = id;
		this.pnombre = pnombre;
		this.edad = edad;
		this.ubicacion = ubicacion;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public String getIdentificacion() { return identificacion; }
	public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
	public String getTipoIdentificacion() { return tipoIdentificacion; }
	public void setTipoIdentificacion(String tipoIdentificacion) { this.tipoIdentificacion = tipoIdentificacion; }
	public String getNombres() { return nombres; }
	public void setNombres(String nombres) { this.nombres = nombres; }
	public String getPnombre() { return pnombre; }
	public void setPnombre(String pnombre) { this.pnombre = pnombre; }
	public String getApellidos() { return apellidos; }
	public void setApellidos(String apellidos) { this.apellidos = apellidos; }
	public Integer getEdad() { return edad; }
	public void setEdad(Integer edad) { this.edad = edad; }
	public String getUbicacion() { return ubicacion; }
	public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
	public String getCorreo() { return correo; }
	public void setCorreo(String correo) { this.correo = correo; }
	public String getTipoPersona() { return tipoPersona; }
	public void setTipoPersona(String tipoPersona) { this.tipoPersona = tipoPersona; }
	public LocalDate getFechaLicencia() { return fechaLicencia; }
	public void setFechaLicencia(LocalDate fechaLicencia) { this.fechaLicencia = fechaLicencia; }
	public byte[] getLicencia() { return licencia; }
	public void setLicencia(byte[] licencia) { this.licencia = licencia; }

	@Override
	public String toString() {
		return "Persona [id=" + id + ", identificacion=" + identificacion + ", pnombre=" + pnombre
				+ ", tipoPersona=" + tipoPersona + ", ubicacion=" + ubicacion + "]";
	}
}
