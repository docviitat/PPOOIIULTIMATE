package com.PPOOII.Laboratorio.Config.Model;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciales para solicitar el token JWT.")
public class JwtRequest implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Schema(description = "Login del usuario", example = "jperez")
	private String username;
	@Schema(description = "Contraseña del usuario", example = "ClaveSegura123", accessMode = Schema.AccessMode.WRITE_ONLY)
	private String password;
	
	public JwtRequest () {}
	
	public JwtRequest (String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	
}
