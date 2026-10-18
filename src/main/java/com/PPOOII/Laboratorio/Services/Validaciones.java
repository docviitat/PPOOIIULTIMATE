package com.PPOOII.Laboratorio.Services;

import java.text.Normalizer;
import java.util.Base64;

import org.springframework.http.HttpStatus;

import com.PPOOII.Laboratorio.Exception.CargueException;

/** Utilidades comunes de validación. */
public final class Validaciones {

	private Validaciones() {}

	public static boolean vacio(String s) {
		return s == null || s.trim().isEmpty();
	}

	public static CargueException error(HttpStatus status, String msg) {
		return new CargueException(status, msg);
	}

	/** Quita tildes, espacios y pasa a minúsculas: "En Verificación" -> "enverificacion". */
	public static String clave(String s) {
		if (s == null) return "";
		String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		return n.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}

	/** Retorna la opción canónica que coincide (sin importar mayúsculas/tildes/espacios) o null. */
	public static String canon(String valor, String... opciones) {
		String k = clave(valor);
		for (String o : opciones) if (clave(o).equals(k)) return o;
		return null;
	}

	/** Decodifica un PDF en BASE64 (acepta prefijo data:...;base64,) y verifica la firma %PDF. */
	public static byte[] decodificarPdf(String base64) {
		String s = base64.trim();
		int c = s.indexOf(',');
		if (s.startsWith("data:") && c > 0) s = s.substring(c + 1);
		byte[] b;
		try {
			b = Base64.getMimeDecoder().decode(s);
		} catch (IllegalArgumentException e) {
			throw error(HttpStatus.BAD_REQUEST, "El archivo no es un BASE64 válido.");
		}
		if (b.length < 5 || b[0] != '%' || b[1] != 'P' || b[2] != 'D' || b[3] != 'F') {
			throw error(HttpStatus.BAD_REQUEST, "El archivo debe ser un PDF (en BASE64).");
		}
		return b;
	}
}
