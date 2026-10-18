package com.PPOOII.Laboratorio.Entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity(name="COOR")
@Table(name = "coordenadas")
@Schema(description = "Coordenadas geográficas de una persona, obtenidas con la API de Google Maps.")
public class Coordenadas implements Serializable{
	private static final long serialVersionUID = 1L;
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id_coordenada")
	@Schema(description = "Identificador de la coordenada", example = "1")
	public int id;
	@Column(name="persona")
	@Schema(description = "Id de la persona a la que pertenece la coordenada", example = "1")
	public int persona;
	@Column(name="marca")
	@Schema(description = "Marca o nombre con el que se identifica el punto", example = "Homero")
	public String marca;
	@Column(name="longitud")
	@Schema(description = "Longitud geográfica", example = "-75.2322")
	public double longitud;
	@Column(name="latitud")
	@Schema(description = "Latitud geográfica", example = "4.4389")
	public double latitud;
	
	public Coordenadas() {}
	
	public Coordenadas(int persona, String marca, double longitud, double latitud) {
		this.persona = persona;
		this.marca = marca;
		this.longitud = longitud;
		this.latitud = latitud;
	}

	public Coordenadas(int id, int persona, String marca, double longitud, double latitud) {
		this.id = id;
		this.persona = persona;
		this.marca = marca;
		this.longitud = longitud;
		this.latitud = latitud;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPersona() {
		return persona;
	}

	public void setPersona(int persona) {
		this.persona = persona;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public double getLongitud() {
		return longitud;
	}

	public void setLongitud(double longitud) {
		this.longitud = longitud;
	}

	public double getLatitud() {
		return latitud;
	}

	public void setLatitud(double latitud) {
		this.latitud = latitud;
	}
	
	
}
