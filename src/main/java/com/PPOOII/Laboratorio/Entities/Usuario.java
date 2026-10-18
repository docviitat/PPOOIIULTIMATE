package com.PPOOII.Laboratorio.Entities;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity(name = "UsrANDPer")
@Table(name = "usuario")
@Schema(description = "Usuario de la aplicación asociado a una persona.")
public class Usuario implements Serializable{
	private static final long serialVersionUID = 1L;
	@EmbeddedId
	@Schema(description = "Llave compuesta: login + id de la persona")
	private UsuarioPK id;
	@Column(name = "password")
	@Schema(description = "Contraseña del usuario", accessMode = Schema.AccessMode.WRITE_ONLY)
	private String password;
	@Column(name = "apikey")
	@Schema(description = "APIKey del usuario, se envía en el header APIkey al autenticarse")
	private String apikey;
	
	public Usuario() {}
	public Usuario(String login, String password) {
		this.id.setLogin(login);
		this.password = password;
	}

	public UsuarioPK getId() {
		return id;
	}
	public void setId(UsuarioPK id) {
		this.id = id;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getApikey() {
		return apikey;
	}
	public void setApikey(String apikey) {
		this.apikey = apikey;
	}
}
