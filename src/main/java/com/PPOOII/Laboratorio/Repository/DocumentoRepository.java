package com.PPOOII.Laboratorio.Repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.PPOOII.Laboratorio.DTO.DocumentoDTO;

/** Entidad paramétrica DOCUMENTOS (SQL nativo). */
@Repository("IDocumentoRepository")
public class DocumentoRepository {

	@Autowired
	private JdbcTemplate jdbc;

	private static final String SEL = "SELECT id_documento, codigo_documento, nombre_documento, tipos_vehiculo, obligatoriedad, descripcion FROM documentos";

	private final RowMapper<DocumentoDTO> mapper = (rs, i) -> {
		DocumentoDTO d = new DocumentoDTO();
		d.idDocumento = rs.getInt("id_documento");
		d.codigoDocumento = rs.getString("codigo_documento");
		d.nombreDocumento = rs.getString("nombre_documento");
		d.tiposVehiculo = rs.getString("tipos_vehiculo");
		d.obligatoriedad = rs.getString("obligatoriedad");
		d.descripcion = rs.getString("descripcion");
		return d;
	};

	public int insertar(DocumentoDTO d) {
		KeyHolder kh = new GeneratedKeyHolder();
		jdbc.update(con -> {
			java.sql.PreparedStatement ps = con.prepareStatement(
					"INSERT INTO documentos (codigo_documento, nombre_documento, tipos_vehiculo, obligatoriedad, descripcion) VALUES (?,?,?,?,?)",
					java.sql.Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, d.codigoDocumento);
			ps.setString(2, d.nombreDocumento);
			ps.setString(3, d.tiposVehiculo);
			ps.setString(4, d.obligatoriedad);
			ps.setString(5, d.descripcion);
			return ps;
		}, kh);
		return kh.getKey().intValue();
	}

	public int actualizar(DocumentoDTO d) {
		return jdbc.update("UPDATE documentos SET codigo_documento=?, nombre_documento=?, tipos_vehiculo=?, obligatoriedad=?, descripcion=? WHERE id_documento=?",
				d.codigoDocumento, d.nombreDocumento, d.tiposVehiculo, d.obligatoriedad, d.descripcion, d.idDocumento);
	}

	public int eliminar(int id) {
		return jdbc.update("DELETE FROM documentos WHERE id_documento = ?", id);
	}

	public boolean enUso(int id) {
		Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM vehiculo_documento WHERE id_documento = ?", Integer.class, id);
		return n != null && n > 0;
	}

	public DocumentoDTO porId(int id) {
		List<DocumentoDTO> l = jdbc.query(SEL + " WHERE id_documento = ?", mapper, id);
		return l.isEmpty() ? null : l.get(0);
	}

	public DocumentoDTO porCodigo(String codigo) {
		List<DocumentoDTO> l = jdbc.query(SEL + " WHERE codigo_documento = ?", mapper, codigo);
		return l.isEmpty() ? null : l.get(0);
	}

	public List<DocumentoDTO> listar(int limite, int desplazamiento) {
		return jdbc.query(SEL + " ORDER BY id_documento LIMIT ? OFFSET ?", mapper, limite, desplazamiento);
	}
}
