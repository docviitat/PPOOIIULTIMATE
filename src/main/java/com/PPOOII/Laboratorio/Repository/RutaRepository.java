package com.PPOOII.Laboratorio.Repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.PPOOII.Laboratorio.DTO.TrayectoRutaDTO;

/** Consultas de rutas/trayectos (SQL nativo con joins sobre trayecto, persona, vehiculos, vehiculo_conductor y vehiculo_documento). */
@Repository("IRutaRepository")
public class RutaRepository {

	@Autowired
	private JdbcTemplate jdbc;

	private static final String BASE =
		"SELECT t.codigo_ruta AS CODIGO_RUTA, t.orden_parada AS ORDEN, t.ubicacion AS UBICACION, t.latitud AS LATITUD, t.longitud AS LONGITUD, "
		+ "p.identificacion AS IDENTIFICACION_CONDUCTOR, CONCAT(p.nombres, ' ', p.apellidos) AS NOMBRE_CONDUCTOR, "
		+ "vc.estado AS ESTADO_CONDUCTOR, v.placa AS PLACA, "
		+ "(v.id_vehiculo IS NOT NULL AND NOT EXISTS (SELECT 1 FROM vehiculo_documento d "
		+ "  WHERE d.id_vehiculo = t.id_vehiculo AND d.estado_documento <> 'Habilitado')) AS HABILITADO "
		+ "FROM trayecto t "
		+ "LEFT JOIN vehiculos v ON v.id_vehiculo = t.id_vehiculo "
		+ "LEFT JOIN persona p ON p.id = t.id_persona "
		+ "LEFT JOIN vehiculo_conductor vc ON vc.persona_id = t.id_persona AND vc.vehiculo_id = t.id_vehiculo";

	private final RowMapper<TrayectoRutaDTO> mapper = (rs, i) -> {
		TrayectoRutaDTO t = new TrayectoRutaDTO();
		t.orden = rs.getInt("ORDEN");
		t.ubicacion = rs.getString("UBICACION");
		t.latitud = rs.getObject("LATITUD", Double.class);
		t.longitud = rs.getObject("LONGITUD", Double.class);
		t.identificacionConductor = rs.getString("IDENTIFICACION_CONDUCTOR");
		t.nombreConductor = rs.getString("NOMBRE_CONDUCTOR");
		t.estadoConductor = rs.getString("ESTADO_CONDUCTOR");
		t.placa = rs.getString("PLACA");
		t.vehiculoHabilitado = rs.getBoolean("HABILITADO");
		return t;
	};

	/** Trayectos de una ruta, en orden. */
	public List<TrayectoRutaDTO> trayectosDeRuta(String codigoRuta) {
		return jdbc.query(BASE + " WHERE t.CODIGO_RUTA = ? ORDER BY t.orden_parada", mapper, codigoRuta);
	}

	/** Trayectos (con su código de ruta) donde el vehículo NO está habilitado o el conductor está RO. */
	public List<Object[]> trayectosRestringidos() {
		String sql = "SELECT * FROM (" + BASE + ") x WHERE x.HABILITADO = 0 OR x.ESTADO_CONDUCTOR = 'RO' ORDER BY x.CODIGO_RUTA, x.ORDEN";
		return jdbc.query(sql, (rs, i) -> new Object[] { rs.getString("CODIGO_RUTA"), mapper.mapRow(rs, i) });
	}

	public boolean existeConductor(String identificacion) {
		Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM persona WHERE identificacion = ? AND tipo_persona = 'C'", Integer.class, identificacion);
		return n != null && n > 0;
	}

	public String nombreConductor(String identificacion) {
		List<String> l = jdbc.queryForList("SELECT CONCAT(nombres, ' ', apellidos) FROM persona WHERE identificacion = ? AND tipo_persona = 'C'", String.class, identificacion);
		return l.isEmpty() ? null : l.get(0);
	}

	public List<String> codigosRutaDeConductor(String identificacion) {
		return jdbc.queryForList("SELECT DISTINCT t.codigo_ruta FROM trayecto t JOIN persona p ON p.id = t.id_persona "
				+ "WHERE p.identificacion = ? ORDER BY t.codigo_ruta", String.class, identificacion);
	}

	public boolean existeVehiculo(String placa) {
		Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM vehiculos WHERE placa = ?", Integer.class, placa);
		return n != null && n > 0;
	}

	/** Filas [codigoRuta, identificacionConductor, nombre] de los trayectos de un vehículo. */
	public List<String[]> rutasYConductoresDeVehiculo(String placa) {
		return jdbc.query("SELECT DISTINCT t.codigo_ruta, p.identificacion, CONCAT(p.nombres, ' ', p.apellidos) FROM trayecto t "
				+ "JOIN vehiculos v ON v.id_vehiculo = t.id_vehiculo "
				+ "LEFT JOIN persona p ON p.id = t.id_persona "
				+ "WHERE v.placa = ? ORDER BY t.codigo_ruta, p.identificacion",
				(rs, i) -> new String[] { rs.getString(1), rs.getString(2), rs.getString(3) }, placa);
	}

	// ---- Reglas para crear un trayecto ----
	public boolean vehiculoConDocumentosHabilitados(String placa) {
		Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM vehiculo_documento d JOIN vehiculos v ON v.id_vehiculo = d.id_vehiculo "
				+ "WHERE v.placa = ? AND d.estado_documento <> 'Habilitado'", Integer.class, placa);
		return n != null && n == 0;
	}

	public boolean conductorPuedeOperar(String identificacion, String placa) {
		Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM persona p JOIN vehiculo_conductor vc ON vc.persona_id = p.id "
				+ "JOIN vehiculos v ON v.id_vehiculo = vc.vehiculo_id "
				+ "WHERE p.identificacion = ? AND p.tipo_persona = 'C' AND v.placa = ? AND vc.estado = 'PO'", Integer.class, identificacion, placa);
		return n != null && n > 0;
	}

	public boolean existeTrayecto(String codigoRuta, int orden) {
		Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM trayecto WHERE codigo_ruta = ? AND orden_parada = ?", Integer.class, codigoRuta, orden);
		return n != null && n > 0;
	}

	/** id de la persona conductora (tipo 'C') por su identificación; null si no existe. */
	public Long idConductor(String identificacion) {
		List<Long> l = jdbc.queryForList("SELECT id FROM persona WHERE identificacion = ? AND tipo_persona = 'C'", Long.class, identificacion);
		return l.isEmpty() ? null : l.get(0);
	}

	/** id del vehículo por su placa; null si no existe. */
	public Integer idVehiculo(String placa) {
		List<Integer> l = jdbc.queryForList("SELECT id_vehiculo FROM vehiculos WHERE placa = ?", Integer.class, placa);
		return l.isEmpty() ? null : l.get(0);
	}
}
