package com.PPOOII.Laboratorio.Config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springdoc.core.models.GroupedOpenApi;

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
        description = """
            API del proyecto final — Chequeo de Requerimientos.xlsx, entregas 1 a 4.

            **Cómo probar:** envíe username y password a POST /authenticate y la APIKey del mismo usuario
            en el encabezado APIkey. La respuesta incluye `Bearer `: quite ese prefijo antes de pegar
            el JWT en **Authorize → bearerAuth**. En **apiKeyAuth** pegue la APIKey.

            - **Entrega 1:** CRUD de vehículos y documentos; consultas por placa, tipo, documento y estado.
              Exige JWT **y** APIKey del mismo usuario. Un vehículo se crea con al menos un documento,
              siempre en En Verificacion.
            - **Entrega 2:** personas (incluye /LaboratorioV2), usuario automático para ADMINISTRATIVO,
              cambio de contraseña, regeneración de APIKey, PDF en Base64 y asociaciones PO / EA / RO.
              Las cinco consultas bajo /LaboratorioV2/publico son públicas y no requieren credenciales.
            - **Entrega 3:** rutas con paradas ordenadas, rutas agrupadas por conductor o placa y restricciones.
              Requiere JWT. Crear trayectos exige documentos habilitados y conductor PO con licencia no vencida.
              La interfaz visual está en [rutas.html](/rutas.html).
            - **Entrega 4:** descargue la plantilla, adjunte el Excel en el campo `file` y consulte el
              idCargue retornado. Se ejecutan los tres procedimientos de cargue, validación y procesado.
              Requiere JWT. Las filas con ERROR conservan sus observaciones y no crean trayectos.

            Las tareas revisan licencias y documentos cada 2 minutos y coordenadas cada 90 segundos.
            La geocodificación actual usa Nominatim/OpenStreetMap; el Excel menciona Google Maps.
            Mensajería y cargue de personas son servicios complementarios.
            """,
		contact = @Contact(name = "Equipo PPOOII")),
	servers = { @Server(url = "/", description = "Servidor actual") })
@SecurityScheme(
	name = "bearerAuth",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT",
	description = "Token obtenido en POST /authenticate. Pegue solo el token, sin el prefijo Bearer.")
@SecurityScheme(
	name = "apiKeyAuth",
	type = SecuritySchemeType.APIKEY,
	in = SecuritySchemeIn.HEADER,
	paramName = "APIkey",
	description = "APIKey del usuario: se envía en APIkey al autenticarse y en todos los servicios de vehículos/documentos de Entrega 1, junto al JWT.")
public class OpenApiConfig {
    @Bean
    org.springdoc.core.customizers.GlobalOpenApiCustomizer seguridadEntrega1() {
        return api -> {
            if (api.getPaths() == null) return;
            api.getPaths().forEach((path,item) -> {
                if (path.startsWith("/LaboratorioV1/vehiculo") || path.startsWith("/LaboratorioV1/documento")) {
                    item.readOperations().forEach(op -> op.setSecurity(java.util.List.of(
                            new io.swagger.v3.oas.models.security.SecurityRequirement()
                                    .addList("bearerAuth").addList("apiKeyAuth"))));
                }
            });
        };
    }

    @Bean
    GroupedOpenApi todasLasEntregas() {
        return GroupedOpenApi.builder().group("00-todos").pathsToMatch("/LaboratorioV1/**", "/LaboratorioV2/**",
                "/LaboratorioV3/**", "/LaboratorioV4/**", "/authenticate").build();
    }

    @Bean
    GroupedOpenApi entrega1() {
        return GroupedOpenApi.builder().group("01-vehiculos-documentos")
                .pathsToMatch("/LaboratorioV1/vehiculo/**", "/LaboratorioV1/vehiculos/**",
                        "/LaboratorioV1/documento/**", "/LaboratorioV1/documentos/**", "/authenticate").build();
    }

    @Bean
    GroupedOpenApi entrega2() {
        return GroupedOpenApi.builder().group("02-personas-usuarios-conductores")
                .pathsToMatch("/LaboratorioV2/**", "/authenticate").build();
    }

    @Bean
    GroupedOpenApi entrega3() {
        return GroupedOpenApi.builder().group("03-rutas-trayectos")
                .pathsToMatch("/LaboratorioV3/rutas/**", "/LaboratorioV3/trayectos", "/authenticate").build();
    }

    @Bean
    GroupedOpenApi entrega4() {
        return GroupedOpenApi.builder().group("04-cargue-excel")
                .pathsToMatch("/LaboratorioV4/trayectos/**", "/authenticate").build();
    }
}
