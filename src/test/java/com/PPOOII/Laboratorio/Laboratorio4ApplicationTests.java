package com.PPOOII.Laboratorio;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import com.PPOOII.Laboratorio.Component.*;
import com.PPOOII.Laboratorio.Config.JWTAuthtenticationConfig;
import com.PPOOII.Laboratorio.Services.*;
import org.springframework.amqp.core.AmqpAdmin;

/** Ejecutar solo sobre una BD desechable preparada por scripts/verificar.sh. */
@SpringBootTest(properties={
    "spring.datasource.url=${TEST_DB_URL}","spring.datasource.username=${TEST_DB_USER:audit}",
    "spring.datasource.password=${TEST_DB_PASSWORD:audit}","tareas.enabled=false",
    "tareas.personas.enabled=false","mensajeria.cola=${TEST_RABBIT_QUEUE:ppooii.audit.requisitos}",
    "mensajeria.consumidor.enabled=false","spring.jpa.show-sql=false"})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named="TEST_DB_URL",matches=".+")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Laboratorio4ApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate db;
    @Autowired ObjectMapper json;
    @Autowired JWTAuthtenticationConfig jwt;
    @MockBean GeocodingService geo;
    @Autowired MensajeriaService mensajes;
    @Autowired AmqpAdmin admin;
    @org.springframework.beans.factory.annotation.Value("${mensajeria.cola}") String cola;
    String token;

    @BeforeEach void preparar() {
        token=jwt.getJWTToken("mgomez");
        db.update("DELETE FROM trayecto WHERE codigo_ruta<>'T-001'");
        db.update("DELETE FROM trayecto_tmp"); db.update("DELETE FROM persona_tmp"); db.update("DELETE FROM cargue_archivo");
        db.update("UPDATE vehiculo_conductor SET estado='PO' WHERE persona_id IN (1,3,4)");
        db.update("UPDATE vehiculo_documento SET estado_documento='Habilitado',fecha_vencimiento='2030-12-31' WHERE id_vehiculo=1");
        db.update("UPDATE persona SET fecha_vigencia_licencia='2030-12-31' WHERE id=1");
    }
    org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder autorizado(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder req) {
        return req.header("Authorization",token).header("APIkey","api_key_67890");
    }
    JsonNode enviar(String path,Object body,int status) throws Exception {
        return json.readTree(mvc.perform(autorizado(post(path)).contentType("application/json").content(json.writeValueAsBytes(body)))
            .andExpect(status().is(status)).andReturn().getResponse().getContentAsString());
    }
    MockMultipartFile excel(String[] headers,String[][] rows) throws Exception {
        try(var wb=new XSSFWorkbook();var out=new ByteArrayOutputStream()) {
            var sheet=wb.createSheet("Datos"); var h=sheet.createRow(0);
            for(int i=0;i<headers.length;i++) h.createCell(i).setCellValue(headers[i]);
            for(int n=0;n<rows.length;n++) { var row=sheet.createRow(n+1);for(int i=0;i<rows[n].length;i++) row.createCell(i).setCellValue(rows[n][i]); }
            wb.write(out);return new MockMultipartFile("file","datos.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",out.toByteArray());
        }
    }
    static final String[] TRAY={"CODIGO_RUTA","ORDEN","UBICACION","IDENTIFICACION_CONDUCTOR","PLACA","NOMBRE_CONDUCTOR","EDAD_CONDUCTOR"};
    Map<String,Object> parada(String ruta) {
        return Map.of("codigoRuta",ruta,"orden",0,"ubicacion","Parque","identificacionConductor","1001234567","placa","ABC123");
    }
    Map<String,Object> vehiculo(String placa,List<?> docs) {
        return Map.of("placa",placa,"tipoVehiculo","Automovil","tipoServicio","A","tipoCombustible","Gasolina",
            "capacidadPasajeros",4,"modelo",2024,"marca","Marca","linea","Linea","color","Azul","documentos",docs);
    }
    Map<String,Object> documento() {
        return Map.of("idDocumento",1,"numeroDocumento","PRUEBA","fechaExpedicion","2026-01-01","fechaVencimiento","2030-01-01","estadoDocumento","Habilitado");
    }
    @Test @Order(1) void autenticacionYProteccion() throws Exception {
        mvc.perform(post("/authenticate").header("APIkey","api_key_67890").contentType("application/json")
            .content("{\"username\":\"mgomez\",\"password\":\"admin123\"}")).andExpect(status().isOk()).andExpect(jsonPath("token").isString());
        mvc.perform(post("/authenticate").header("APIkey","incorrecta").contentType("application/json")
            .content("{\"username\":\"mgomez\",\"password\":\"admin123\"}")).andExpect(status().isUnauthorized());
        mvc.perform(get("/LaboratorioV1/vehiculos")).andExpect(status().isForbidden());
        mvc.perform(get("/LaboratorioV1/vehiculos").header("Authorization",token)).andExpect(status().isForbidden());
        mvc.perform(autorizado(get("/LaboratorioV1/vehiculos"))).andExpect(status().isOk());
        mvc.perform(get("/LaboratorioV3/rutas/T-001").header("Authorization","Bearer invalido")).andExpect(status().isForbidden());
        String malo=jwt.getJWTToken("mgomez"); malo=malo.substring(0,malo.length()-8)+"aaaaaaaa";
        mvc.perform(get("/LaboratorioV3/rutas/T-001").header("Authorization",malo)).andExpect(status().isForbidden());
    }
    @Test @Order(2) void swaggerYServiciosPublicos() throws Exception {
        var api=json.readTree(mvc.perform(get("/api-docs")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(api.path("paths").has("/LaboratorioV4/mensajes")).isTrue();
        assertThat(api.path("paths").path("/LaboratorioV1/vehiculos").path("get").path("security").get(0).has("apiKeyAuth")).isTrue();
        for(String p:List.of("vehiculos/documentos-vencidos","conductores/pueden-operar","vehiculos/placa/ABC123","vehiculos/documentos-por-vencer?dias=30","personas/total-por-tipo"))
            mvc.perform(get("/LaboratorioV2/publico/"+p)).andExpect(status().isOk());
        mvc.perform(get("/rutas.html")).andExpect(status().isOk());
    }
    @Test @Order(3) void crudVehiculoYConsultas() throws Exception {
        enviar("/LaboratorioV1/vehiculo",vehiculo("AUD001",List.of()),400);
        var v=enviar("/LaboratorioV1/vehiculo",vehiculo("AUD001",List.of(documento())),201);
        int id=v.path("idVehiculo").asInt(); assertThat(v.path("documentos").get(0).path("estadoDocumento").asText()).isEqualTo("En Verificacion");
        for(String p:List.of("vehiculo/placa/AUD001","vehiculos/tipo/Automovil","vehiculos/documento/SOAT","vehiculos/estado-documento/Habilitado"))
            mvc.perform(autorizado(get("/LaboratorioV1/"+p))).andExpect(status().isOk());
        var body=new HashMap<>(vehiculo("AUD001",List.of(documento())));body.put("idVehiculo",id);body.put("color","Rojo");
        mvc.perform(autorizado(put("/LaboratorioV1/vehiculo")).contentType("application/json").content(json.writeValueAsBytes(body))).andExpect(status().isOk()).andExpect(jsonPath("color").value("Rojo"));
        mvc.perform(autorizado(delete("/LaboratorioV1/vehiculo/"+id))).andExpect(status().isNoContent());
    }
    @Test @Order(4) void crudDocumentosYChecks() throws Exception {
        var d=enviar("/LaboratorioV1/documento",Map.of("codigoDocumento","AUD","nombreDocumento","Auditoria","tiposVehiculo","AM","obligatoriedad","RR"),201);
        int id=d.path("idDocumento").asInt();
        mvc.perform(autorizado(get("/LaboratorioV1/documento/"+id))).andExpect(status().isOk());
        var body=new HashMap<String,Object>();body.put("idDocumento",id);body.put("codigoDocumento","AUD");body.put("nombreDocumento","Actualizado");body.put("tiposVehiculo","AM");body.put("obligatoriedad","RR");
        mvc.perform(autorizado(put("/LaboratorioV1/documento")).contentType("application/json").content(json.writeValueAsBytes(body))).andExpect(status().isOk());
        mvc.perform(autorizado(delete("/LaboratorioV1/documento/"+id))).andExpect(status().isNoContent());
        assertThatThrownBy(()->db.update("UPDATE vehiculos SET tipo_combustible='INVALIDO' WHERE id_vehiculo=1")).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThatThrownBy(()->db.update("UPDATE documentos SET obligatoriedad='XX' WHERE id_documento=1")).isInstanceOf(org.springframework.dao.DataAccessException.class);
    }
    @Test @Order(5) void administrativoUsuarioAutomaticoYCredenciales() throws Exception {
        var p=enviar("/LaboratorioV1/persona",Map.of("identificacion","AUD001","tipoIdentificacion","CC","nombres","Álvaro","apellidos","Ñúñez","correo","alvaro@example.com","tipoPersona","ADMINISTRATIVO"),200);
        String login=p.path("usuario").path("login").asText();assertThat(login).isEqualTo("anunez");
        assertThat(p.path("usuario").path("password").asText()).hasSize(12);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM usuario WHERE login=?",Integer.class,login)).isEqualTo(1);
        mvc.perform(autorizado(put("/LaboratorioV2/usuarios/"+login+"/password")).contentType("application/json").content("{\"password\":\"Cambio123\"}")).andExpect(status().isOk());
        mvc.perform(autorizado(get("/LaboratorioV2/usuarios/"+login+"/apikey"))).andExpect(status().isOk()).andExpect(jsonPath("apikey").isString());
        db.update("DELETE FROM persona WHERE identificacion='AUD001'");
    }
    @Test @Order(6) void documentosPdfYAsociaciones() throws Exception {
        var d=new HashMap<>(documento());d.put("archivoBase64",Base64.getEncoder().encodeToString("%PDF-1.4 prueba".getBytes()));
        enviar("/LaboratorioV2/vehiculos/ABC123/documentos",List.of(d),200);
        assertThat(db.queryForObject("SELECT estado_documento FROM vehiculo_documento WHERE id=1",String.class)).isEqualTo("En Verificacion");
        d.put("archivoBase64",Base64.getEncoder().encodeToString("archivo sin PDF".getBytes()));
        enviar("/LaboratorioV2/vehiculos/ABC123/documentos",List.of(d),400);
        enviar("/LaboratorioV2/conductores/1001234567/vehiculos",Map.of("placas",List.of("TST001")),200);
        mvc.perform(autorizado(put("/LaboratorioV2/conductores/1001234567/vehiculos/TST001/estado")).contentType("application/json").content("{\"estado\":\"PO\"}")).andExpect(status().isOk());
        db.update("DELETE FROM vehiculo_conductor WHERE persona_id=1 AND vehiculo_id=3");
    }
    @Test @Order(7) void trayectosOrdenAgrupacionYRestricciones() throws Exception {
        enviar("/LaboratorioV3/trayectos",parada("AUD-RUTA"),201);
        mvc.perform(autorizado(get("/LaboratorioV3/rutas/AUD-RUTA"))).andExpect(status().isOk()).andExpect(jsonPath("trayectos[0].orden").value(0));
        for(String p:List.of("conductor/1001234567","vehiculo/ABC123")) mvc.perform(autorizado(get("/LaboratorioV3/rutas/"+p))).andExpect(status().isOk());
        db.update("UPDATE vehiculo_documento SET fecha_vencimiento=DATE_SUB(CURDATE(),INTERVAL 1 DAY) WHERE id=1");
        enviar("/LaboratorioV3/trayectos",parada("AUD-OTRA"),409);
        mvc.perform(autorizado(get("/LaboratorioV3/rutas/restricciones"))).andExpect(status().isOk()).andExpect(jsonPath("$[0].trayectos[0].vehiculoHabilitado").value(false));
    }
    @Test @Order(8) void cargueMixtoConObservacionesAcumuladasYSinAlteraciones() throws Exception {
        var f=excel(TRAY,new String[][]{{"AUD-EXCEL","0","  Parque  ","1001234567","ABC123"," Juan ","025"},{"","x","","no-existe","FAL000","",""}});
        var res=json.readTree(mvc.perform(autorizado(multipart("/LaboratorioV4/trayectos/cargue").file(f))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        long id=res.path("idCargue").asLong();assertThat(res.path("porEstado").path("PROCESADO").asInt()).isEqualTo(1);assertThat(res.path("porEstado").path("ERROR").asInt()).isEqualTo(1);
        assertThat(db.queryForObject("SELECT ubicacion FROM trayecto_tmp WHERE id_cargue=? AND estado='PROCESADO'",String.class,id)).isEqualTo("  Parque  ");
        assertThat(db.queryForObject("SELECT edad_conductor FROM trayecto_tmp WHERE estado='PROCESADO'",String.class)).isEqualTo("025");
        String error=db.queryForObject("SELECT observacion FROM trayecto_tmp WHERE estado='ERROR'",String.class);
        assertThat(error).contains("codigo de ruta","orden","ubicacion","vehiculo","conductor");
        mvc.perform(autorizado(get("/LaboratorioV4/trayectos/cargue/"+id))).andExpect(status().isOk());
        mvc.perform(autorizado(multipart("/LaboratorioV4/trayectos/cargue").file(f))).andExpect(status().isConflict());
        db.update("CALL PROCESAR_TRAYECTOS_EXCEL(?,?)",id,"mgomez");
        assertThat(db.queryForObject("SELECT COUNT(*) FROM trayecto WHERE codigo_ruta='AUD-EXCEL'",Integer.class)).isEqualTo(1);
        assertThatThrownBy(()->db.update("UPDATE trayecto_tmp SET estado='INVALIDO'")).isInstanceOf(org.springframework.dao.DataAccessException.class);
    }
    @Test @Order(9) void duplicadosYVehiculoSinDocumentosNoSeProcesan() throws Exception {
        var f=excel(TRAY,new String[][]{{"AUD-DUP","1","A","1001234567","ABC123"},{" AUD-DUP ","01","B","1001234567","ABC123"}});
        mvc.perform(autorizado(multipart("/LaboratorioV4/trayectos/cargue").file(f))).andExpect(status().isOk()).andExpect(jsonPath("porEstado.ERROR").value(2));
        db.update("DELETE FROM vehiculo_documento WHERE id_vehiculo=3");
        var r=new HashMap<>(parada("AUD-SINDOC"));r.put("placa","TST001");r.put("identificacionConductor","9001");
        enviar("/LaboratorioV3/trayectos",r,409);
        db.update("INSERT INTO vehiculo_documento(id_vehiculo,id_documento,numero_documento,fecha_expedicion,fecha_vencimiento,estado_documento) VALUES(3,1,'SOAT','2026-01-01','2030-12-31','Habilitado'),(3,2,'TM','2026-01-01','2030-12-31','Habilitado')");
    }
    @Test @Order(10) void excelPersonasValidacionYProcesamiento() throws Exception {
        var f=excel(new String[]{"NOMBRE","EDAD","UBICACIÓN"},new String[][]{{"Homero","31","Centro"},{"","abc",""},{"Bart","17","Centro"}});
        mvc.perform(multipart("/LaboratorioV3/upload-excel").file(f)).andExpect(status().isOk());
        assertThat(db.queryForObject("SELECT COUNT(*) FROM persona_tmp WHERE estado='PROCESADO'",Integer.class)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT COUNT(*) FROM persona_tmp WHERE estado='ERROR'",Integer.class)).isEqualTo(2);
        assertThat(db.queryForObject("SELECT observacion FROM persona_tmp WHERE edad='abc'",String.class)).contains("Nombre","Edad","ubicacion");
        assertThat(db.queryForObject("SELECT COUNT(*) FROM persona WHERE pnombre='Homero' AND edad=31",Integer.class)).isEqualTo(1);
        db.update("DELETE FROM persona WHERE identificacion IS NULL");
    }
    @Test @Order(11) void plantillaArchivosInvalidosYRollback() throws Exception {
        var bytes=mvc.perform(autorizado(get("/LaboratorioV4/trayectos/plantilla"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        try(var wb=new XSSFWorkbook(new java.io.ByteArrayInputStream(bytes))) { assertThat(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue()).isEqualTo("CODIGO_RUTA"); }
        mvc.perform(autorizado(multipart("/LaboratorioV4/trayectos/cargue").file(excel(new String[]{"MAL"},new String[][]{{"x"}})))).andExpect(status().isBadRequest());
        mvc.perform(autorizado(multipart("/LaboratorioV4/trayectos/cargue").file(excel(TRAY,new String[][]{})))).andExpect(status().isBadRequest());
        assertThat(db.queryForObject("SELECT COUNT(*) FROM cargue_archivo",Integer.class)).isZero();
    }
    @Test @Order(12) void tareasDeVencimientoYCoordenadas() throws Exception {
        var tasks=new TareasProgramadasEntrega3();
        org.springframework.test.util.ReflectionTestUtils.setField(tasks,"jdbc",db);
        org.springframework.test.util.ReflectionTestUtils.setField(tasks,"geocodingService",geo);
        db.update("UPDATE persona SET fecha_vigencia_licencia=DATE_SUB(CURDATE(),INTERVAL 1 DAY) WHERE id=1");
        tasks.licenciasVencidas();assertThat(db.queryForObject("SELECT estado FROM vehiculo_conductor WHERE persona_id=1 AND vehiculo_id=1",String.class)).isEqualTo("RO");
        db.update("UPDATE vehiculo_documento SET fecha_vencimiento=DATE_SUB(CURDATE(),INTERVAL 1 DAY) WHERE id=1");
        tasks.documentosVencidos();assertThat(db.queryForObject("SELECT estado_documento FROM vehiculo_documento WHERE id=1",String.class)).isEqualTo("Vencido");
        db.update("UPDATE trayecto SET latitud=NULL,longitud=NULL WHERE codigo_ruta='T-001'");
        when(geo.latLng(anyString())).thenReturn(new double[]{4.44,-75.23});tasks.coordenadasTrayectos();
        assertThat(db.queryForObject("SELECT COUNT(*) FROM trayecto WHERE codigo_ruta='T-001' AND latitud=4.44 AND longitud=-75.23",Integer.class)).isEqualTo(2);
        assertThat(TareasProgramadasEntrega3.class.getMethod("licenciasVencidas").getAnnotation(org.springframework.scheduling.annotation.Scheduled.class).fixedRateString()).contains("120000");
    }
    @Test @Order(13) void rabbitProductorYConsumidorIndependientes() throws Exception {
        mensajes.activar(false);admin.purgeQueue(cola,false);db.update("DELETE FROM mensaje_recibido");
        var published=enviar("/LaboratorioV4/mensajes",Map.of("contenido","Prueba sin consumidor"),202);
        String id=published.path("id").asText();
        assertThat(mensajes.estado().get("pendientes")).isEqualTo(1);
        mensajes.activar(true);
        long limite=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
        while(db.queryForObject("SELECT COUNT(*) FROM mensaje_recibido WHERE id=?",Integer.class,id)==0 && System.nanoTime()<limite) Thread.sleep(100);
        assertThat(db.queryForObject("SELECT contenido FROM mensaje_recibido WHERE id=?",String.class,id)).isEqualTo("Prueba sin consumidor");
        // Sin nuevas publicaciones el consumidor continúa activo; un nuevo mensaje también se consume.
        assertThat(mensajes.estado().get("consumidorActivo")).isEqualTo(true);
        mvc.perform(autorizado(get("/LaboratorioV4/mensajes/recibidos"))).andExpect(status().isOk());
        mensajes.activar(false);admin.deleteQueue(cola);
    }
    @Test @Order(14) void falloDeProcesadoRevierteArchivoCompleto() throws Exception {
        db.execute("CREATE TRIGGER aud_fallo BEFORE INSERT ON trayecto FOR EACH ROW BEGIN IF NEW.codigo_ruta='AUD-FAIL' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fallo de prueba'; END IF; END");
        try {
            var file=excel(TRAY,new String[][]{{"AUD-FAIL","0","Parque","1001234567","ABC123"}});
            mvc.perform(autorizado(multipart("/LaboratorioV4/trayectos/cargue").file(file))).andExpect(status().isInternalServerError());
            assertThat(db.queryForObject("SELECT COUNT(*) FROM trayecto_tmp",Integer.class)).isZero();
            assertThat(db.queryForObject("SELECT COUNT(*) FROM cargue_archivo",Integer.class)).isZero();
        } finally { db.execute("DROP TRIGGER aud_fallo"); }
    }
    @Test @Order(15) void excelDescargableIncluyeGuiaYEjemploImportable() throws Exception {
        byte[] plantilla=mvc.perform(autorizado(get("/LaboratorioV4/trayectos/plantilla")))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        try(var wb=new XSSFWorkbook(new java.io.ByteArrayInputStream(plantilla))) {
            assertThat(wb.getNumberOfSheets()).isEqualTo(2);
            assertThat(wb.getSheetAt(0).getLastRowNum()).isZero();
            assertThat(wb.getSheetAt(0).getPaneInformation().isFreezePane()).isTrue();
            assertThat(wb.getSheetAt(0).getDataValidations()).hasSize(5);
            assertThat(wb.getSheetAt(0).getColumnStyle(3).getDataFormatString()).isEqualTo("@");
            assertThat(wb.getSheetAt(0).getRow(0).getCell(3).getCellComment().getString().getString()).contains("ceros iniciales");
        }
        byte[] ejemplo=mvc.perform(autorizado(get("/LaboratorioV4/trayectos/ejemplo")))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        var archivo=new MockMultipartFile("file","ejemplo.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",ejemplo);
        mvc.perform(autorizado(multipart("/LaboratorioV4/trayectos/cargue").file(archivo)))
            .andExpect(status().isOk()).andExpect(jsonPath("porEstado.PROCESADO").value(3))
            .andExpect(jsonPath("porEstado.ERROR").value(0));
        assertThat(db.queryForObject("SELECT COUNT(*) FROM trayecto WHERE codigo_ruta='NUEVA-001'",Integer.class)).isEqualTo(3);
    }

    @Test @Order(16) @org.springframework.transaction.annotation.Transactional
    void cicloVehiculoVerificacionConductorYTrayectoPorHttp() throws Exception {
        enviar("/LaboratorioV1/vehiculo", vehiculo("AUD016", List.of(documento())), 201);
        enviar("/LaboratorioV2/conductores/1001234567/vehiculos", Map.of("placas", List.of("AUD016")), 200);
        mvc.perform(autorizado(put("/LaboratorioV2/conductores/1001234567/vehiculos/AUD016/estado"))
                .contentType("application/json").content("{\"estado\":\"PO\"}")).andExpect(status().isOk());
        var parada = new HashMap<>(parada("AUD-CICLO")); parada.put("placa", "AUD016");
        enviar("/LaboratorioV3/trayectos", parada, 409);
        mvc.perform(autorizado(put("/LaboratorioV2/vehiculos/AUD016/documentos/1/estado"))
                .contentType("application/json").content("{\"estado\":\"Habilitado\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("documentos[0].estadoDocumento").value("Habilitado"));
        enviar("/LaboratorioV3/trayectos", parada, 201);
        mvc.perform(autorizado(get("/LaboratorioV3/rutas/AUD-CICLO"))).andExpect(status().isOk());
        var pdf = new HashMap<>(documento()); pdf.put("archivoBase64", "JVBERi0xLjQ=");
        enviar("/LaboratorioV2/vehiculos/AUD016/documentos", List.of(pdf), 200);
        parada.put("codigoRuta", "AUD-CICLO-2");
        enviar("/LaboratorioV3/trayectos", parada, 409);
    }

    @Test @Order(17) @org.springframework.transaction.annotation.Transactional
    void documentoEnUsoConservaAplicabilidadYActualizaCodigoRelacionado() throws Exception {
        var doc = new HashMap<String,Object>();
        doc.put("idDocumento", 1); doc.put("codigoDocumento", "SOAT"); doc.put("nombreDocumento", "Seguro");
        doc.put("tiposVehiculo", "M"); doc.put("obligatoriedad", "RM");
        mvc.perform(autorizado(put("/LaboratorioV1/documento")).contentType("application/json")
                .content(json.writeValueAsBytes(doc))).andExpect(status().isConflict());
        doc.put("tiposVehiculo", "AM"); doc.put("obligatoriedad", "RR"); doc.put("codigoDocumento", "SOAT-PRUEBA");
        mvc.perform(autorizado(put("/LaboratorioV1/documento")).contentType("application/json")
                .content(json.writeValueAsBytes(doc))).andExpect(status().isOk());
        assertThat(db.queryForList("SELECT DISTINCT tipo_documento FROM vehiculo_documento WHERE id_documento=1", String.class))
                .containsExactly("SOAT-PRUEBA");
    }

    @Test @Order(18) @org.springframework.transaction.annotation.Transactional
    void consultaRestriccionesDetectaLicenciaVencidaAntesDeLaTarea() throws Exception {
        db.update("UPDATE persona SET fecha_vigencia_licencia=DATE_SUB(CURDATE(),INTERVAL 1 DAY) WHERE id=1");
        mvc.perform(autorizado(get("/LaboratorioV3/rutas/restricciones"))).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trayectos[0].estadoConductor").value("RO"));
    }
}
