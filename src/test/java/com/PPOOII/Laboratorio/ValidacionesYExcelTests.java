package com.PPOOII.Laboratorio;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import com.PPOOII.Laboratorio.Util.ExcelUtil;
import com.PPOOII.Laboratorio.Services.Validaciones;
import com.PPOOII.Laboratorio.Component.GeocodingService;
import com.PPOOII.Laboratorio.APIs.OpenStreetMap.Geocoder;

class ValidacionesYExcelTests {
    @Test void conservaTextoYFormatoDeIdentificacion() throws Exception {
        try(var wb=new XSSFWorkbook()) {
            var row=wb.createSheet().createRow(0);row.createCell(0).setCellValue("  Juan  ");
            var cell=row.createCell(1);cell.setCellValue(123);var style=wb.createCellStyle();
            style.setDataFormat(wb.createDataFormat().getFormat("000000"));cell.setCellStyle(style);
            row.createCell(2).setCellValue(1234567890123d);
            var evaluator=wb.getCreationHelper().createFormulaEvaluator();
            assertThat(ExcelUtil.leerCelda(row,0,evaluator)).isEqualTo("  Juan  ");
            assertThat(ExcelUtil.leerCelda(row,1,evaluator)).isEqualTo("000123");
            assertThat(ExcelUtil.leerCelda(row,2,evaluator)).isEqualTo("1234567890123");
        }
    }
    @Test void pdfRechazaBase64InvalidoYFirmaIncompleta() {
        for(String s:List.of("%%%","JVBERiE=","")) assertThatThrownBy(()->Validaciones.decodificarPdf(s)).isInstanceOf(RuntimeException.class);
        assertThat(Validaciones.decodificarPdf("data:application/pdf;base64,JVBERi0xLjQ=")).startsWith((byte)'%',(byte)'P',(byte)'D',(byte)'F',(byte)'-');
    }
    @Test void encabezadosNormalizanTildes() {
        assertThat(ExcelUtil.normalizarEncabezado("  identificación conductor ")).isEqualTo("IDENTIFICACION_CONDUCTOR");
    }
    @Test void coordenadasDesdeCacheNoConsultanProveedor() throws Exception {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(),eq("Parque"))).thenReturn(List.of(Map.of("latitud",4.4,"longitud",-75.2,"consultado",Timestamp.from(Instant.now()))));
        var servicio=new GeocodingService(jdbc,"http://127.0.0.1:1/no-se-consulta","test");
        assertThat(servicio.latLng(" Parque ")).containsExactly(4.4,-75.2);
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void erroresDeGeocodificacionTienenCacheDe24Horas() {
        var jdbc=mock(JdbcTemplate.class);var fila=new HashMap<String,Object>();fila.put("latitud",null);fila.put("longitud",null);fila.put("consultado",Timestamp.from(Instant.now()));
        when(jdbc.queryForList(anyString(),eq("Sin resultado"))).thenReturn(List.of(fila));
        var servicio=new GeocodingService(jdbc,"http://127.0.0.1:1/no-se-consulta","test");
        assertThatThrownBy(()->servicio.latLng("Sin resultado")).hasMessageContaining("24 horas");
    }
    @Test void clienteNominatimLeeRespuestaSinClaves() throws Exception {
        var server=com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/search",exchange -> {
            assertThat(exchange.getRequestHeaders().getFirst("User-Agent")).isEqualTo("PruebaPPOOII");
            byte[] data="[{\"lat\":\"4.44\",\"lon\":\"-75.23\"}]".getBytes();
            exchange.sendResponseHeaders(200,data.length);exchange.getResponseBody().write(data);exchange.close();
        });server.start();
        try {
            var geo=new Geocoder("http://127.0.0.1:"+server.getAddress().getPort()+"/search","PruebaPPOOII");
            assertThat(geo.getLatLng("Parque Ibagué")).isEqualTo("4.44,-75.23");
        } finally {server.stop(0);}
    }
}
