package com.PPOOII.Laboratorio.Component;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tareas programadas de la Entrega 3 (Spring Boot). Los intervalos se configuran en application.properties:
 * tareas.licencias.ms (2 min), tareas.documentos.ms (2 min) y tareas.coordenadas.ms (90 s).
 */
@Component
public class TareasProgramadasEntrega3 {

	private static final Logger logger = LoggerFactory.getLogger(TareasProgramadasEntrega3.class);

	@Autowired private JdbcTemplate jdbc;
	@Autowired private GeocodingService geocodingService;

	/** 1.1 Licencias vencidas: el conductor pasa a RO (Restringido para Operar) en su relación con el vehículo. */
	@Scheduled(fixedDelayString = "${tareas.licencias.ms:120000}", initialDelayString = "${tareas.licencias.ms:120000}")
	public void licenciasVencidas() {
		try {
			int n = jdbc.update("UPDATE vehiculo_conductor vc JOIN persona p ON p.id = vc.persona_id "
					+ "SET vc.estado = 'RO' "
					+ "WHERE p.tipo_persona = 'C' AND p.fecha_vigencia_licencia IS NOT NULL "
					+ "AND p.fecha_vigencia_licencia < CURDATE() AND vc.estado <> 'RO'");
			if (n > 0) logger.info("[TAREA licencias] {} relación(es) conductor-vehículo pasaron a RO por licencia vencida.", n);
		} catch (Exception e) {
			logger.error("[TAREA licencias] error: {}", e.toString());
		}
	}

	/** 1.2 Documentos con vigencia vencida: el estado del documento asociado al vehículo pasa a Vencido. */
	@Scheduled(fixedDelayString = "${tareas.documentos.ms:120000}", initialDelayString = "${tareas.documentos.ms:120000}")
	public void documentosVencidos() {
		try {
			int n = jdbc.update("UPDATE vehiculo_documento SET estado_documento = 'Vencido' "
					+ "WHERE fecha_vencimiento < CURDATE() AND estado_documento <> 'Vencido'");
			if (n > 0) logger.info("[TAREA documentos] {} documento(s) pasaron a Vencido.", n);
		} catch (Exception e) {
			logger.error("[TAREA documentos] error: {}", e.toString());
		}
	}

	/** 1.3 Trayectos sin latitud/longitud: se consultan con Google Maps y se guardan en la base de datos. */
	@Scheduled(fixedDelayString = "${tareas.coordenadas.ms:90000}", initialDelayString = "${tareas.coordenadas.ms:90000}")
	public void coordenadasTrayectos() {
		int actualizados = 0;
		try {
			List<Map<String, Object>> pendientes = jdbc.queryForList(
					"SELECT id_trayecto AS ID, ubicacion AS UBICACION FROM trayecto WHERE latitud IS NULL OR longitud IS NULL");
			if (pendientes.isEmpty()) return;
			if (!geocodingService.usaGoogle()) {
				logger.warn("[TAREA coordenadas] No hay API Key de Google Maps (google.maps.api-key / GOOGLE_MAPS_API_KEY); se usa Nominatim como respaldo.");
			}
			for (Map<String, Object> fila : pendientes) {
				Number id = (Number) fila.get("ID");
				String ubicacion = (String) fila.get("UBICACION");
				try {
					double[] c = geocodingService.latLng(ubicacion);
					jdbc.update("UPDATE trayecto SET latitud = ?, longitud = ? WHERE id_trayecto = ?", c[0], c[1], id.longValue());
					actualizados++;
				} catch (Exception e) {
					logger.warn("[TAREA coordenadas] no se obtuvo lat/long del trayecto {} ('{}'): {}", id, ubicacion, e.toString());
				}
				try {
					Thread.sleep(geocodingService.pausaMs());
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					break;
				}
			}
			logger.info("[TAREA coordenadas] {} de {} trayecto(s) actualizados.", actualizados, pendientes.size());
		} catch (Exception e) {
			logger.error("[TAREA coordenadas] error: {}", e.toString());
		}
	}
}
