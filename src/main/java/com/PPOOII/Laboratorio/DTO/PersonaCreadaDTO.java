package com.PPOOII.Laboratorio.DTO;

import com.PPOOII.Laboratorio.Entities.Persona;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Persona guardada y, si es ADMINISTRATIVO y se creó usuario, sus credenciales generadas.")
public class PersonaCreadaDTO {
	public Persona persona;
	@Schema(description = "Credenciales generadas (null si la persona no es ADMINISTRATIVO o ya tenía usuario). La contraseña solo se muestra en esta respuesta.")
	public UsuarioGenerado usuario;

	public static class UsuarioGenerado {
		public String login;
		public String password;
		public String apikey;
	}
}
