package com.PPOOII.Laboratorio.Services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Relación conductor-vehículo (Entrega 2) y consultas públicas de personas. */
@Service("ConductorService")
public class ConductorService {

	@Autowired private JdbcTemplate jdbc;

	/** Asocia vehículos (por placa) a un conductor. Estado inicial EA (Espera de Aprobación). */
	@Transactional
	public List<Map<String, Object>> asociarVehiculos(String identificacion, List<String> placas) {
		Long idPersona = idConductor(identificacion);
		if (placas == null || placas.isEmpty()) {
			throw Validaciones.error(HttpStatus.BAD_REQUEST, "Debe enviar al menos una placa.");
		}
		List<Map<String, Object>> res = new ArrayList<>();
		List<String> faltantes = new ArrayList<>();
		List<Integer> ids = new ArrayList<>();
		List<String> limpias = new ArrayList<>();
		for (String p : placas) {
			String placa = p == null ? "" : p.trim().toUpperCase();
			List<Integer> l = jdbc.queryForList("SELECT id_vehiculo FROM vehiculos WHERE placa = ?", Integer.class, placa);
			if (l.isEmpty()) faltantes.add(placa); else { ids.add(l.get(0)); limpias.add(placa); }
		}
		if (!faltantes.isEmpty()) {
			throw Validaciones.error(HttpStatus.BAD_REQUEST, "No existen vehículos con las placas: " + String.join(", ", faltantes) + ".");
		}
		for (int i = 0; i < ids.size(); i++) {
			Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM vehiculo_conductor WHERE persona_id = ? AND vehiculo_id = ?",
					Integer.class, idPersona, ids.get(i));
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("placa", limpias.get(i));
			if (n != null && n > 0) {
				m.put("resultado", "YA_ASOCIADO");
			} else {
				jdbc.update("INSERT INTO vehiculo_conductor (estado, fecha_asociacion, persona_id, vehiculo_id) VALUES ('EA', CURDATE(), ?, ?)",
						idPersona, ids.get(i));
				m.put("resultado", "ASOCIADO");
				m.put("estado", "EA");
			}
			res.add(m);
		}
		return res;
	}

	/** Cambia el estado del conductor en relación con el vehículo: PO, EA o RO. */
	@Transactional
	public Map<String, Object> cambiarEstado(String identificacion, String placa, String estado) {
		String e = Validaciones.canon(estado, "PO", "EA", "RO");
		if (e == null) throw Validaciones.error(HttpStatus.BAD_REQUEST, "El estado debe ser PO, EA o RO.");
		Long idPersona = idConductor(identificacion);
		String pl = placa == null ? "" : placa.trim().toUpperCase();
		if ("PO".equals(e)) {
			List<java.sql.Date> f = jdbc.queryForList("SELECT fecha_vigencia_licencia FROM persona WHERE id = ?", java.sql.Date.class, idPersona);
			if (!f.isEmpty() && f.get(0) != null && f.get(0).toLocalDate().isBefore(LocalDate.now())) {
				throw Validaciones.error(HttpStatus.CONFLICT, "La licencia del conductor está vencida; no puede quedar en PO.");
			}
		}
		int n = jdbc.update("UPDATE vehiculo_conductor vc JOIN vehiculos v ON v.id_vehiculo = vc.vehiculo_id SET vc.estado = ? "
				+ "WHERE vc.persona_id = ? AND v.placa = ?", e, idPersona, pl);
		if (n == 0) {
			throw Validaciones.error(HttpStatus.NOT_FOUND, "El conductor " + identificacion + " no está asociado al vehículo " + pl + ".");
		}
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("identificacion", identificacion);
		m.put("placa", pl);
		m.put("estado", e);
		return m;
	}

	/** Público: conductores que pueden operar (PO), con las placas en que pueden hacerlo. */
	public List<Map<String, Object>> conductoresQuePuedenOperar() {
		Map<String, Map<String, Object>> porId = new LinkedHashMap<>();
		jdbc.query("SELECT p.identificacion, CONCAT(p.nombres, ' ', p.apellidos) AS nombre, v.placa FROM persona p "
				+ "JOIN vehiculo_conductor vc ON vc.persona_id = p.id JOIN vehiculos v ON v.id_vehiculo = vc.vehiculo_id "
				+ "WHERE p.tipo_persona = 'C' AND vc.estado = 'PO' ORDER BY p.identificacion, v.placa", rs -> {
					String id = rs.getString("identificacion");
					Map<String, Object> m = porId.get(id);
					if (m == null) {
						m = new LinkedHashMap<>();
						m.put("identificacion", id);
						m.put("nombre", rs.getString("nombre"));
						m.put("placas", new ArrayList<String>());
						porId.put(id, m);
					}
					@SuppressWarnings("unchecked")
					List<String> placas = (List<String>) m.get("placas");
					placas.add(rs.getString("placa"));
				});
		return new ArrayList<>(porId.values());
	}

	/** Público: total de personas agrupadas por tipo. */
	public List<Map<String, Object>> totalPersonasPorTipo() {
		return jdbc.query("SELECT tipo_persona, COUNT(*) AS total FROM persona GROUP BY tipo_persona ORDER BY tipo_persona", (rs, i) -> {
			Map<String, Object> m = new LinkedHashMap<>();
			String t = rs.getString("tipo_persona");
			m.put("tipoPersona", "C".equals(t) ? "CONDUCTOR" : "A".equals(t) ? "ADMINISTRATIVO" : t);
			m.put("total", rs.getLong("total"));
			return m;
		});
	}

	private Long idConductor(String identificacion) {
		List<Long> l = jdbc.queryForList("SELECT id FROM persona WHERE identificacion = ? AND tipo_persona = 'C'", Long.class,
				identificacion == null ? "" : identificacion.trim());
		if (l.isEmpty()) {
			throw Validaciones.error(HttpStatus.NOT_FOUND, "No existe un conductor con identificación " + identificacion + ".");
		}
		return l.get(0);
	}
}
