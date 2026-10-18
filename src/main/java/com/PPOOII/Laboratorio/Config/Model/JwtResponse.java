package com.PPOOII.Laboratorio.Config.Model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación con el token JWT.")
public class JwtResponse implements Serializable {

	private static final long serialVersionUID = -8091879091924046844L;
	private final String jwttoken;

//	private final String user;
//
//	@JsonIgnore
//	private final String pass;
	
	public JwtResponse(String jwttoken/*, String login, String password*/) {
		this.jwttoken = jwttoken;
//		this.user = login;
//		this.pass = password;
	}

	@Schema(description = "Token JWT (incluye el prefijo Bearer)", example = "Bearer eyJhbGciOiJIUzUxMiJ9...")
	public String getToken() {
		return this.jwttoken;
	}

//	public String getJwttoken() {
//		return jwttoken;
//	}
//
//	public String getUser() {
//		return user;
//	}
//
//	public String getPass() {
//		return pass;
//	}
	
}