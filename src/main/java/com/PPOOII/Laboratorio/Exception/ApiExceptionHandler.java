package com.PPOOII.Laboratorio.Exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import com.PPOOII.Laboratorio.DTO.ErrorDTO;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(CargueException.class)
	public ResponseEntity<ErrorDTO> cargue(CargueException e) {
		return ResponseEntity.status(e.getStatus()).body(new ErrorDTO(e.getStatus().value(), e.getMessage()));
	}

	@ExceptionHandler({ MissingServletRequestPartException.class, MissingServletRequestParameterException.class })
	public ResponseEntity<ErrorDTO> parametroFaltante(Exception e) {
		return ResponseEntity.badRequest().body(new ErrorDTO(400, "Debe enviar el archivo Excel en el campo 'file' (multipart/form-data)."));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorDTO> tamano(MaxUploadSizeExceededException e) {
		return ResponseEntity.status(413).body(new ErrorDTO(413, "El archivo supera el tamaño máximo permitido."));
	}

	@ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
	public ResponseEntity<ErrorDTO> integridad(org.springframework.dao.DataIntegrityViolationException e) {
		return ResponseEntity.status(409).body(new ErrorDTO(409, "La operación viola una restricción de la base de datos (dato duplicado, inexistente o no permitido)."));
	}

	@ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorDTO> cuerpoInvalido(org.springframework.http.converter.HttpMessageNotReadableException e) {
		return ResponseEntity.badRequest().body(new ErrorDTO(400, "El cuerpo de la petición no es un JSON válido o tiene valores con formato incorrecto (p. ej. fechas yyyy-MM-dd)."));
	}
}
