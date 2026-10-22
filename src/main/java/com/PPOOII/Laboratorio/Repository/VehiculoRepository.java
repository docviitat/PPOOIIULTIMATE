package com.PPOOII.Laboratorio.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.PPOOII.Laboratorio.DTO.DocumentoVehiculoDTO;
import com.PPOOII.Laboratorio.DTO.VehiculoDTO;

/** Vehículos, su relación con documentos y las consultas del proyecto (SQL nativo). */
@Repository("IVehiculoRepository")
public class VehiculoRepository {

	@Autowired
	private JdbcTemplate jdbc;

	private static final String SEL = "SELECT v.id_vehiculo, v.tipo_vehiculo, v.placa, v.tipo_servicio, v.tipo_combustible, "
			+ "v.capacidad_pasajeros, v.color, v.modelo, v.marca, v.linea FROM vehiculos v";

	private final RowMapper<VehiculoDTO> mapper = (rs, i) -> {
		VehiculoDTO v = new VehiculoDTO();
		v.idVehiculo = rs.getInt("id_vehiculo");
		v.tipoVehiculo = rs.getString("tipo_vehiculo");
		v.placa = rs.getString("placa");
		v.tipoServicio = rs.getString("tipo_servicio");
		v.tipoCombustible = rs.getString("tipo_combustible");
		v.capacidadPasajeros = rs.getInt("capacidad_pasajeros");
		v.color = rs.getString("color");
		v.modelo = rs.getInt("modelo");
		v.marca = rs.getString("marca");
		v.linea = rs.getString("linea");
		return v;
	};

	private final RowMapper<DocumentoVehiculoDTO> docMapper = (rs, i) -> {
		DocumentoVehiculoDTO d = new DocumentoVehiculoDTO();
		d.id = rs.getLong("id");
		d.idDocumento = rs.getInt("id_documento");
		d.codigoDocumento = rs.getString("codigo_documento");
		d.nombreDocumento = rs.getString("nombre_documento");
		d.numeroDocumento = rs.getString("numero_documento");
		Date e = rs.getDate("fecha_expedicion");
		Date x = rs.getDate("fecha_vencimiento");
		d.fechaExpedicion = e == null ? null : e.toLocalDate();
		d.fechaVencimiento = x == null ? null : x.toLocalDate();
		d.estadoDocumento = rs.getString("estado_documento");
		d.tieneArchivo = rs.getInt("tiene_archivo") == 1;
		return d;
	};

	// ---------------- CRUD vehículo ----------------
	public int insertar(VehiculoDTO v) {
		KeyHolder kh = new GeneratedKeyHolder();
		jdbc.update(con -> {
			java.sql.PreparedStatement ps = con.prepareStatement(
					"INSERT INTO vehiculos (tipo_vehiculo, placa, tipo_servicio, tipo_combustible, capacidad_pasajeros, color, modelo, marca, linea) VALUES (?,?,?,?,?,?,?,?,?)",
					java.sql.Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, v.tipoVehiculo);
			ps.setString(2, v.placa);
			ps.setString(3, v.tipoServicio);
			ps.setString(4, v.tipoCombustible);
			ps.setInt(5, v.capacidadPasajeros);
			ps.setString(6, v.color);
			ps.setInt(7, v.modelo);
			ps.setString(8, v.marca);
			ps.setString(9, v.linea);
			return ps;
		}, kh);
		return kh.getKey().intValue();
	}

	public int actualizar(VehiculoDTO v) {
		return jdbc.update("UPDATE vehiculos SET tipo_vehiculo=?, placa=?, tipo_servicio=?, tipo_combustible=?, capacidad_pasajeros=?, color=?, modelo=?, marca=?, linea=? WHERE id_vehiculo=?",
				v.tipoVehiculo, v.placa, v.tipoServicio, v.tipoCombustible, v.capacidadPasajeros, v.color, v.modelo, v.marca, v.linea, v.idVehiculo);
	}

	public int eliminar(int id) {
		return jdbc.update("DELETE FROM vehiculos WHERE id_vehiculo = ?", id);
	}

	/** El vehículo no se puede borrar si hay trayectos o conductores asociados. */
	public boolean tieneRelaciones(int id) {
		Integer a = jdbc.queryForObject("SELECT COUNT(*) FROM trayecto WHERE id_vehiculo = ?", Integer.class, id);
		Integer b = jdbc.queryForObject("SELECT COUNT(*) FROM vehiculo_conductor WHERE vehiculo_id = ?", Integer.class, id);
		return (a != null && a > 0) || (b != null && b > 0);
	}

	public VehiculoDTO porId(int id) {
		return primero(jdbc.query(SEL + " WHERE v.id_vehiculo = ?", mapper, id));
	}

	public VehiculoDTO porPlaca(String placa) {
		return primero(jdbc.query(SEL + " WHERE v.placa = ?", mapper, placa));
	}

	public List<VehiculoDTO> listar(int limite, int desplazamiento) {
		return conDocumentos(jdbc.query(SEL + " ORDER BY v.id_vehiculo LIMIT ? OFFSET ?", mapper, limite, desplazamiento));
	}

	// ---------------- Consultas (E1 punto 3) ----------------
	public List<VehiculoDTO> porTipo(String tipo) {
		return conDocumentos(jdbc.query(SEL + " WHERE v.tipo_vehiculo = ? ORDER BY v.id_vehiculo", mapper, tipo));
	}

	public List<VehiculoDTO> porCodigoDocumento(String codigo) {
		return conDocumentos(jdbc.query(SEL + " WHERE EXISTS (SELECT 1 FROM vehiculo_documento d JOIN documentos t ON t.id_documento = d.id_documento "
				+ "WHERE d.id_vehiculo = v.id_vehiculo AND t.codigo_documento = ?) ORDER BY v.id_vehiculo", mapper, codigo));
	}

	public List<VehiculoDTO> porEstadoDocumento(String estado) {
		return conDocumentos(jdbc.query(SEL + " WHERE EXISTS (SELECT 1 FROM vehiculo_documento d WHERE d.id_vehiculo = v.id_vehiculo AND d.estado_documento = ?) "
				+ "ORDER BY v.id_vehiculo", mapper, estado));
	}

	/** Vehículos con documentos vencidos (por estado o por fecha de vencimiento). */
	public List<VehiculoDTO> conDocumentosVencidos() {
		return conDocumentos(jdbc.query(SEL + " WHERE EXISTS (SELECT 1 FROM vehiculo_documento d WHERE d.id_vehiculo = v.id_vehiculo "
				+ "AND (d.estado_documento = 'Vencido' OR d.fecha_vencimiento < CURDATE())) ORDER BY v.id_vehiculo", mapper));
	}

	/** Vehículos con documentos que vencen dentro de los próximos {@code dias} días. */
	public List<VehiculoDTO> conDocumentosPorVencer(int dias) {
		return conDocumentos(jdbc.query(SEL + " WHERE EXISTS (SELECT 1 FROM vehiculo_documento d WHERE d.id_vehiculo = v.id_vehiculo "
				+ "AND d.estado_documento <> 'Vencido' AND d.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)) "
				+ "ORDER BY v.id_vehiculo", mapper, dias));
	}

	// ---------------- Documentos del vehículo ----------------
	public List<DocumentoVehiculoDTO> documentosDe(int idVehiculo) {
		return jdbc.query("SELECT d.id, d.id_documento, t.codigo_documento, t.nombre_documento, d.numero_documento, d.fecha_expedicion, "
				+ "d.fecha_vencimiento, d.estado_documento, (d.documento_pdf IS NOT NULL) AS tiene_archivo "
				+ "FROM vehiculo_documento d JOIN documentos t ON t.id_documento = d.id_documento WHERE d.id_vehiculo = ? ORDER BY d.id", docMapper, idVehiculo);
	}

	public Long idRelacion(int idVehiculo, int idDocumento) {
		List<Long> l = jdbc.queryForList("SELECT id FROM vehiculo_documento WHERE id_vehiculo = ? AND id_documento = ?", Long.class, idVehiculo, idDocumento);
		return l.isEmpty() ? null : l.get(0);
	}

	public void cambiarEstadoDocumento(long idRelacion, String estado) {
		jdbc.update("UPDATE vehiculo_documento SET estado_documento=? WHERE id=?", estado, idRelacion);
	}

	public void insertarDocumento(int idVehiculo, int idDocumento, String codigo, String numero, LocalDate exp, LocalDate ven, byte[] pdf, String estado) {
		jdbc.update("INSERT INTO vehiculo_documento (id_vehiculo, id_documento, tipo_documento, numero_documento, fecha_expedicion, fecha_vencimiento, documento_pdf, estado_documento) "
				+ "VALUES (?,?,?,?,?,?,?,?)", idVehiculo, idDocumento, codigo, numero, Date.valueOf(exp), Date.valueOf(ven), pdf, estado);
	}

	/** Actualiza una relación existente; si {@code pdf} es null conserva el archivo anterior. */
	public void actualizarDocumento(long idRelacion, String numero, LocalDate exp, LocalDate ven, byte[] pdf, String estado) {
		jdbc.update("UPDATE vehiculo_documento SET numero_documento=?, fecha_expedicion=?, fecha_vencimiento=?, documento_pdf=COALESCE(?, documento_pdf), estado_documento=? WHERE id=?",
				numero, Date.valueOf(exp), Date.valueOf(ven), pdf, estado, idRelacion);
	}

	// ---------------- Conductores ----------------
	/** Conductores asociados a un vehículo: identificacion, nombre y estado (PO/EA/RO). */
	public List<Map<String, Object>> conductoresDe(int idVehiculo) {
		return jdbc.query("SELECT p.identificacion, CONCAT(p.nombres, ' ', p.apellidos) AS nombre, vc.estado FROM vehiculo_conductor vc "
				+ "JOIN persona p ON p.id = vc.persona_id WHERE vc.vehiculo_id = ? ORDER BY p.identificacion", (rs, i) -> {
					Map<String, Object> m = new LinkedHashMap<>();
					m.put("identificacion", rs.getString("identificacion"));
					m.put("nombre", rs.getString("nombre"));
					m.put("estado", rs.getString("estado"));
					return m;
				}, idVehiculo);
	}

	// ---------------- util ----------------
	private VehiculoDTO primero(List<VehiculoDTO> l) {
		if (l.isEmpty()) return null;
		VehiculoDTO v = l.get(0);
		v.documentos = documentosDe(v.idVehiculo);
		return v;
	}

	private List<VehiculoDTO> conDocumentos(List<VehiculoDTO> l) {
		List<VehiculoDTO> r = new ArrayList<>(l);
		for (VehiculoDTO v : r) v.documentos = documentosDe(v.idVehiculo);
		return r;
	}
}
