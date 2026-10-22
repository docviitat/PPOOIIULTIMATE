package com.PPOOII.Laboratorio.Config;

import com.PPOOII.Laboratorio.Controller.*;
import com.PPOOII.Laboratorio.Exception.ApiExceptionHandler;
import com.PPOOII.Laboratorio.Repository.UsuarioRepository;
import com.PPOOII.Laboratorio.Services.*;
import com.PPOOII.Laboratorio.Services.Interfaces.ITrayectoCargueService;
import com.PPOOII.Laboratorio.Services.Interfaces.IRutaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Contratos HTTP y seguridad reales, sin base de datos ni puertos de red. */
@WebMvcTest(excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class, controllers = {PersonaController.class, PublicoController.class, GestionE2Controller.class,
        VehiculoController.class, DocumentoController.class, RutaController.class,
        TrayectoCargueController.class, JwtAuthenticationController.class})
@Import({WebSecurityConfig.class, JWTAuthorizationFilter.class, JWTAuthtenticationConfig.class,
        ApiExceptionHandler.class, OpenApiConfig.class,
        org.springdoc.core.configuration.SpringDocConfiguration.class,
        org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration.class,
        org.springdoc.webmvc.core.configuration.MultipleOpenApiSupportConfiguration.class,
        org.springdoc.webmvc.ui.SwaggerConfig.class})
@org.springframework.boot.context.properties.EnableConfigurationProperties({
        org.springdoc.core.properties.SpringDocConfigProperties.class,
        org.springdoc.core.properties.SwaggerUiConfigProperties.class,
        org.springdoc.core.properties.SwaggerUiOAuthProperties.class})
class ApiRequisitosTests {
    @Autowired MockMvc mvc;
    @Autowired JWTAuthtenticationConfig jwt;
    @MockBean UsuarioRepository usuariosRepo;
    @MockBean(name = "VehiculoService") VehiculoService vehiculos;
    @MockBean(name = "DocumentoService") DocumentoService documentos;
    @MockBean(name = "ConductorService") ConductorService conductores;
    @MockBean(name = "UsuarioGestionService") UsuarioGestionService usuarios;
    @MockBean(name = "PersonaService") PersonaServiceImpl personas;
    @MockBean(name = "RutaService") IRutaService rutas;
    @MockBean(name = "TrayectoCargueService") ITrayectoCargueService cargues;
    @MockBean(name = "UsuarioService") UsuarioServiceImpl usuarioService;

    String token() { return jwt.getJWTToken("prueba"); }

    @Test void swaggerAgrupaEntregasYExponeSeguridadCorrecta() throws Exception {
        mvc.perform(get("/api-docs").header("Authorization", "Bearer invalido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("paths['/LaboratorioV2/persona'].post").exists())
                .andExpect(jsonPath("paths['/LaboratorioV1/vehiculos'].get.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("paths['/LaboratorioV1/vehiculos'].get.security[0].apiKeyAuth").isArray())
                .andExpect(jsonPath("paths['/LaboratorioV2/publico/personas/total-por-tipo'].get.security").doesNotExist());
        mvc.perform(get("/api-docs/01-vehiculos-documentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("paths['/LaboratorioV1/vehiculos'].get.security[0].apiKeyAuth").isArray())
                .andExpect(jsonPath("paths['/LaboratorioV2/persona']").doesNotExist());
        mvc.perform(get("/api-docs/02-personas-usuarios-conductores"))
                .andExpect(status().isOk()).andExpect(jsonPath("paths['/LaboratorioV2/persona'].post").exists());
        mvc.perform(get("/api-docs/03-rutas-trayectos"))
                .andExpect(status().isOk()).andExpect(jsonPath("paths['/LaboratorioV3/rutas/{codigoRuta}'].get").exists());
        mvc.perform(get("/api-docs/04-cargue-excel"))
                .andExpect(status().isOk()).andExpect(jsonPath("paths['/LaboratorioV4/trayectos/cargue'].post").exists())
                .andExpect(jsonPath("paths['/LaboratorioV4/trayectos/cargue'].post.requestBody.content['multipart/form-data'].schema.properties.file.format").value("binary"));
        mvc.perform(get("/api-docs/swagger-config")).andExpect(status().isOk())
                .andExpect(jsonPath("urls.length()").value(5));
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
    }

    @Test void entrega1ExigeTokenYApiKeyDelMismoUsuario() throws Exception {
        mvc.perform(get("/LaboratorioV1/vehiculos")).andExpect(status().isForbidden());
        mvc.perform(get("/LaboratorioV1/vehiculos").header("Authorization", token()))
                .andExpect(status().isForbidden());
        when(usuariosRepo.findByUsernameANDAPIKey("prueba", "clave"))
                .thenReturn(new com.PPOOII.Laboratorio.Entities.Usuario());
        mvc.perform(get("/LaboratorioV1/vehiculos").header("Authorization", token()).header("APIkey", "otra"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/LaboratorioV1/vehiculos").header("Authorization", token()).header("APIkey", "clave"))
                .andExpect(status().isOk());
        mvc.perform(get("/LaboratorioV1/documentos").header("Authorization", token()).header("APIkey", "clave"))
                .andExpect(status().isOk());
    }

    @Test void cincoConsultasPublicasIgnoranTokenInvalido() throws Exception {
        when(vehiculos.vehiculoConConductores("ABC123")).thenReturn(Map.of());
        for (String path : List.of("vehiculos/documentos-vencidos", "conductores/pueden-operar",
                "vehiculos/placa/ABC123", "vehiculos/documentos-por-vencer?dias=30", "personas/total-por-tipo")) {
            mvc.perform(get("/LaboratorioV2/publico/" + path)).andExpect(status().isOk());
            mvc.perform(get("/LaboratorioV2/publico/" + path).header("Authorization", "Bearer invalido"))
                    .andExpect(status().isOk());
        }
        verifyNoInteractions(usuariosRepo);
    }

    @Test void diasFaltanteOInvalidoInformaParametroReal() throws Exception {
        mvc.perform(get("/LaboratorioV2/publico/vehiculos/documentos-por-vencer"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("error").value("El parámetro 'dias' es obligatorio."));
        mvc.perform(get("/LaboratorioV2/publico/vehiculos/documentos-por-vencer?dias=abc"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("error").value("El parámetro 'dias' tiene un formato incorrecto."));
    }

    @Test void personaDisponibleEnEntrega2YUrlAnterior() throws Exception {
        for (String version : List.of("LaboratorioV1", "LaboratorioV2")) {
            mvc.perform(get("/" + version + "/personas").header("Authorization", token())).andExpect(status().isOk());
            mvc.perform(post("/" + version + "/persona").header("Authorization", token())
                    .contentType("application/json").content("{}" )).andExpect(status().isOk());
            mvc.perform(put("/" + version + "/persona").header("Authorization", token())
                    .contentType("application/json").content("{}" )).andExpect(status().isOk());
        }
        verify(personas, times(2)).crear(any());
        verify(personas, times(2)).modificar(any());
    }

    @Test void rutasYCargueExigenJwtPeroNoApiKey() throws Exception {
        for (String path : List.of("/LaboratorioV3/rutas/T-001", "/LaboratorioV3/rutas/conductor/9001",
                "/LaboratorioV3/rutas/vehiculo/ABC123", "/LaboratorioV3/rutas/restricciones",
                "/LaboratorioV4/trayectos/cargue/202610201500")) {
            mvc.perform(get(path)).andExpect(status().isForbidden());
            mvc.perform(get(path).header("Authorization", "Bearer invalido")).andExpect(status().isForbidden());
            mvc.perform(get(path).header("Authorization", token())).andExpect(status().isOk());
        }
    }

    @Test void cambiarEstadoDocumentoEstaProtegidoYEnviaDatosAlServicio() throws Exception {
        String path = "/LaboratorioV2/vehiculos/ABC123/documentos/1/estado";
        mvc.perform(put(path).contentType("application/json").content("{\"estado\":\"Habilitado\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put(path).header("Authorization", token()).contentType("application/json")
                .content("{\"estado\":\"Habilitado\"}")).andExpect(status().isOk());
        verify(vehiculos).cambiarEstadoDocumento("ABC123", 1, "Habilitado");
    }

    @Test void autenticarSinApiKeyDevuelveErrorDeEncabezado() throws Exception {
        mvc.perform(post("/authenticate").contentType("application/json")
                .content("{\"username\":\"prueba\",\"password\":\"clave\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("error").value("El encabezado 'APIkey' es obligatorio."));
    }

    @Test void cargueSinArchivoNoInvocaProcedimientos() throws Exception {
        mvc.perform(multipart("/LaboratorioV4/trayectos/cargue").header("Authorization", token()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("error").value("Debe enviar el campo 'file' (multipart/form-data)."));
        verifyNoInteractions(cargues);
    }
}
