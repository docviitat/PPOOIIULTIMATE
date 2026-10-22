package com.PPOOII.Laboratorio.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.PPOOII.Laboratorio.APIs.OpenStreetMap.Geocoder;

/** Nominatim con caché persistente compartida por las tareas del proyecto. */
@Component
public class GeocodingService {
    private final Geocoder geocoder;
    private final JdbcTemplate jdbc;
    public GeocodingService(JdbcTemplate jdbc,
        @Value("${geocoding.url:https://nominatim.openstreetmap.org/search}") String url,
        @Value("${geocoding.user-agent:PPOOII-Laboratorio4/1.0 (proyecto academico)}") String userAgent) {
        this.jdbc=jdbc; this.geocoder=new Geocoder(url,userAgent);
    }
    public synchronized double[] latLng(String ubicacion) throws Exception {
        String clave=ubicacion.trim();
        var cache=jdbc.queryForList("SELECT latitud,longitud,consultado FROM geocoding_cache WHERE ubicacion=?",clave);
        if(!cache.isEmpty()) {
            var fila=cache.get(0);
            if(fila.get("latitud")!=null) return new double[]{((Number)fila.get("latitud")).doubleValue(),((Number)fila.get("longitud")).doubleValue()};
            if(((Timestamp)fila.get("consultado")).toInstant().isAfter(Instant.now().minusSeconds(86400)))
                throw new IOException("Ubicación sin coordenadas; se reintentará después de 24 horas.");
        }
        try {
            String[] p=geocoder.getLatLng(clave).split(",");
            double[] c={Double.parseDouble(p[0]),Double.parseDouble(p[1])};
            guardar(clave,c[0],c[1]); return c;
        } catch(InterruptedException e) { Thread.currentThread().interrupt(); throw e; }
          catch(Exception e) { guardar(clave,null,null); throw e; }
    }
    private void guardar(String ubicacion,Double lat,Double lon) {
        jdbc.update("INSERT INTO geocoding_cache(ubicacion,latitud,longitud) VALUES(?,?,?) "
            + "ON DUPLICATE KEY UPDATE latitud=VALUES(latitud),longitud=VALUES(longitud),consultado=CURRENT_TIMESTAMP",ubicacion,lat,lon);
    }
}
