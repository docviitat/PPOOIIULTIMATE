package com.PPOOII.Laboratorio.Entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import io.swagger.v3.oas.annotations.media.Schema;

@Embeddable
@Schema(description = "Llave primaria compuesta del usuario.")
public class UsuarioPK implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Column(name = "login")
	@Schema(description = "Login del usuario", example = "jperez")
	private String login;
	
@Column(name = "idpersona")
	@Schema(description = "Id de la persona asociada", example = "1")
	private int persona;
	
	public UsuarioPK () {}
	
	public UsuarioPK (String login, int persoana) {
		this.login = login;
		this.persona = persoana;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public int getPersona() {
		return persona;
	}

	public void setPersona(int persona) {
		this.persona = persona;
	}
	
    @Override public boolean equals(Object o) {
        return o instanceof UsuarioPK p && persona==p.persona && java.util.Objects.equals(login,p.login);
    }
    @Override public int hashCode() { return java.util.Objects.hash(login,persona); }
}
