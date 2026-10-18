package com.PPOOII.Laboratorio.Component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.PPOOII.Laboratorio.APIs.GoogleMaps.Geocoder;

/** Envoltorio de la API externa Google Maps (facilita pruebas y reemplazo). */
@Component
public class GeocodingService {

	private final Geocoder geocoder;

	public GeocodingService(@Value("${google.maps.api-key:}") String apiKey) {
		String k = (apiKey == null || apiKey.isBlank()) ? System.getenv("GOOGLE_MAPS_API_KEY") : apiKey;
		this.geocoder = new Geocoder(k);
	}

	/** Retorna {latitud, longitud} para una ubicación o lanza excepción si no se encuentra. */
	public double[] latLng(String ubicacion) throws Exception {
		String[] p = geocoder.getLatLng(ubicacion).split(",");
		return new double[] { Double.parseDouble(p[0]), Double.parseDouble(p[1]) };
	}

	/** Pausa entre peticiones: Nominatim permite 1 por segundo; Google admite mucho más. */
	public long pausaMs() {
		return geocoder.usaGoogle() ? 100 : 1100;
	}

	public boolean usaGoogle() {
		return geocoder.usaGoogle();
	}
}
