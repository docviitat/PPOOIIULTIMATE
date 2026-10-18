package com.PPOOII.Laboratorio.Services;

import java.security.SecureRandom;
import java.util.Map;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PPOOII.Laboratorio.DTO.PersonaCreadaDTO;
import com.PPOOII.Laboratorio.Entities.Persona;
import com.PPOOII.Laboratorio.Entities.Usuario;
import com.PPOOII.Laboratorio.Entities.UsuarioPK;
import com.PPOOII.Laboratorio.Repository.UsuarioRepository;

/**
 * Gestión de usuarios: creación automática para personas ADMINISTRATIVO, cambio de password
 * y regeneración de APIKey.
 *
 * Nemotecnia del login: inicial del primer nombre + primer apellido, en minúsculas y sin tildes
 * (Juan Pérez -> jperez). Si ya existe se agrega un consecutivo (jperez2, jperez3...).
 */
@Service("UsuarioGestionService")
public class UsuarioGestionService {

	private static final SecureRandom RND = new SecureRandom();
	private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

	@Autowired
	@Qualifier("IUsuarioRepository")
	private UsuarioRepository usuarios;

	public boolean tieneUsuario(int idPersona) {
		return !usuarios.findByPersona(idPersona).isEmpty();
	}

	/** Crea el usuario de una persona con login por nemotecnia, password y APIKey automáticos. */
	@Transactional
	public PersonaCreadaDTO.UsuarioGenerado crearParaPersona(Persona p) {
		String login = generarLogin(p.getNombres(), p.getApellidos());
		PersonaCreadaDTO.UsuarioGenerado g = new PersonaCreadaDTO.UsuarioGenerado();
		g.login = login;
		g.password = generarPassword();
		g.apikey = generarApiKey();
		Usuario u = new Usuario();
		u.setId(new UsuarioPK(login, p.getId()));
		u.setPassword(g.password);
		u.setApikey(g.apikey);
		usuarios.save(u);
		return g;
	}

	/** Cambia la contraseña del usuario identificado por su login. */
	@Transactional
	public Map<String, Object> cambiarPassword(String login, String nueva) {
		if (Validaciones.vacio(nueva) || nueva.length() < 6) {
			throw Validaciones.error(HttpStatus.BAD_REQUEST, "La nueva contraseña es obligatoria y debe tener al menos 6 caracteres.");
		}
		Usuario u = buscar(login);
		u.setPassword(nueva);
		usuarios.save(u);
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("login", login);
		m.put("mensaje", "Contraseña actualizada.");
		return m;
	}

	/** Genera de nuevo la APIKey del usuario. */
	@Transactional
	public Map<String, Object> regenerarApiKey(String login) {
		Usuario u = buscar(login);
		u.setApikey(generarApiKey());
		usuarios.save(u);
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("login", login);
		m.put("apikey", u.getApikey());
		return m;
	}

	private Usuario buscar(String login) {
		Usuario u = Validaciones.vacio(login) ? null : usuarios.findByUsername(login.trim());
		if (u == null) throw Validaciones.error(HttpStatus.NOT_FOUND, "No existe el usuario " + login + ".");
		return u;
	}

	public String generarLogin(String nombres, String apellidos) {
		String n = Validaciones.clave(primero(nombres));
		String a = Validaciones.clave(primero(apellidos));
		String base = (n.isEmpty() ? "u" : n.substring(0, 1)) + (a.isEmpty() ? "usuario" : a);
		String login = base;
		int i = 2;
		while (usuarios.contarPorLogin(login) > 0) login = base + (i++);
		return login;
	}

	public static String generarPassword() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 12; i++) sb.append(ALFABETO.charAt(RND.nextInt(ALFABETO.length())));
		return sb.toString();
	}

	public static String generarApiKey() {
		return java.util.UUID.randomUUID().toString().replace("-", "") + Long.toHexString(RND.nextLong());
	}

	private static String primero(String s) {
		if (s == null || s.trim().isEmpty()) return "";
		return s.trim().split("\\s+")[0];
	}
}
