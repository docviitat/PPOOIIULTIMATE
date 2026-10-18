package com.PPOOII.Laboratorio.Config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Configuración general de la documentación OpenAPI / Swagger.
 *  - Swagger UI: /swagger-ui.html      - Definición JSON: /api-docs
 * Define los esquemas de seguridad (JWT y APIKey) para poder probar los servicios
 * protegidos con el botón "Authorize" de Swagger UI.
 */
@Configuration
@OpenAPIDefinition(
	info = @Info(
		title = "API Laboratorio PPOOII",
		version = "v4",
		description = "Servicios REST de Profundización en Programación Orientada a Objetos II: "
				+ "personas, coordenadas (Google Maps), autenticación JWT y cargue masivo desde Excel "
				+ "(personas y trayectos).",
		contact = @Contact(name = "Equipo PPOOII")),
	servers = { @Server(url = "http://localhost:9001", description = "Servidor local") })
@SecurityScheme(
	name = "bearerAuth",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT",
	description = "Token obtenido en POST /authenticate. Pegue solo el token (el prefijo 'Bearer ' es opcional).")
@SecurityScheme(
	name = "apiKeyAuth",
	type = SecuritySchemeType.APIKEY,
	in = SecuritySchemeIn.HEADER,
	paramName = "APIkey",
	description = "APIKey del usuario (se envía en el header APIkey al autenticarse).")
public class OpenApiConfig {
}
