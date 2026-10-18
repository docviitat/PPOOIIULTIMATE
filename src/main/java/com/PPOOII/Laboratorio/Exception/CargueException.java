package com.PPOOII.Laboratorio.Exception;

import org.springframework.http.HttpStatus;

/** Error de negocio del cargue masivo; se traduce a una respuesta HTTP con cuerpo JSON. */
public class CargueException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final HttpStatus status;

	public CargueException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() { return status; }
}
