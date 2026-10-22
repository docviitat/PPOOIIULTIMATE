package com.PPOOII.Laboratorio.APIs.OpenStreetMap;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import com.fasterxml.jackson.databind.*;

/** Cliente Nominatim gratuito. No usa claves ni servicios de Google. */
public class Geocoder {
    private static final HttpClient HTTP=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    private static long ultimaPeticion;
    private final String endpoint;
    private final String userAgent;
    public Geocoder(String endpoint,String userAgent) { this.endpoint=endpoint; this.userAgent=userAgent; }
    public String getLatLng(String query) throws IOException,InterruptedException {
        String response;
        // Un solo flujo para todas las tareas; máximo 4 peticiones/minuto en trabajos periódicos.
        synchronized(Geocoder.class) {
            long espera=15000-(System.nanoTime()/1000000-ultimaPeticion);
            if(espera>0) Thread.sleep(espera);
            ultimaPeticion=System.nanoTime()/1000000;
            String url=endpoint+"?format=jsonv2&limit=1&q="+URLEncoder.encode(query,StandardCharsets.UTF_8);
            var request=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10))
                .header("User-Agent",userAgent).header("Accept-Language","es").GET().build();
            var result=HTTP.send(request,HttpResponse.BodyHandlers.ofString());
            if(result.statusCode()!=200) throw new IOException("Nominatim respondió HTTP "+result.statusCode());
            response=result.body();
        }
        JsonNode root=new ObjectMapper().readTree(response);
        if(root==null || !root.isArray() || root.isEmpty()) throw new IOException("Ubicación sin resultados: "+query);
        double lat=root.get(0).path("lat").asDouble(Double.NaN),lon=root.get(0).path("lon").asDouble(Double.NaN);
        if(!Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat)>90 || Math.abs(lon)>180)
            throw new IOException("Coordenadas inválidas del proveedor");
        return lat+","+lon;
    }
}
