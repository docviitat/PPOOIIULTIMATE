package com.PPOOII.Laboratorio.APIs.GoogleMaps;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Geocodificación con la API de Google Maps (Geocoding API).
 * Requiere una API Key (propiedad google.maps.api-key o variable de entorno GOOGLE_MAPS_API_KEY).
 * Si no hay clave configurada se usa Nominatim (OpenStreetMap) solo como respaldo de desarrollo.
 */
public class Geocoder {

    private static final String GOOGLE = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";
    private static final String NOMINATIM = "https://nominatim.openstreetmap.org/search?format=jsonv2&limit=1&q=";
    private static final String USER_AGENT = "PPOOII-Laboratorio4/1.0 (proyecto academico)";

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8)).build();

    private final String apiKey;

    /** Usa la clave de la variable de entorno GOOGLE_MAPS_API_KEY (si existe). */
    public Geocoder() {
        this(System.getenv("GOOGLE_MAPS_API_KEY"));
    }

    public Geocoder(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public boolean usaGoogle() {
        return !apiKey.isEmpty();
    }

    /** Respuesta JSON cruda del proveedor. */
    public String GeocodeSync(String query) throws IOException, InterruptedException {
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = usaGoogle()
                ? String.format(GOOGLE, q, URLEncoder.encode(apiKey, StandardCharsets.UTF_8))
                : NOMINATIM + q;
        HttpRequest request = HttpRequest.newBuilder().GET()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept-Language", "es")
                .timeout(Duration.ofSeconds(10)).build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("El servicio de geocodificación respondió HTTP " + response.statusCode());
        }
        return response.body();
    }

    /** Retorna "latitud,longitud" o lanza IOException si no hay resultados. */
    public String getLatLng(String query) throws IOException, InterruptedException {
        JsonNode raiz = new ObjectMapper().readTree(this.GeocodeSync(query));
        if (usaGoogle()) {
            String status = raiz.path("status").asText();
            JsonNode res = raiz.path("results");
            if (!"OK".equals(status) || !res.isArray() || res.size() == 0) {
                throw new IOException("Google Maps no encontró la ubicación '" + query + "' (status=" + status + ")");
            }
            JsonNode loc = res.get(0).path("geometry").path("location");
            return loc.path("lat").asText() + "," + loc.path("lng").asText();
        }
        if (raiz == null || !raiz.isArray() || raiz.size() == 0) {
            throw new IOException("No se encontró la ubicación: '" + query + "'");
        }
        return raiz.get(0).get("lat").asText() + "," + raiz.get(0).get("lon").asText();
    }
}
