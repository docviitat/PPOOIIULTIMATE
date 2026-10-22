package com.PPOOII.Laboratorio.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PPOOII.Laboratorio.DTO.DocumentoDTO;
import com.PPOOII.Laboratorio.Repository.DocumentoRepository;

/** CRUD de la entidad paramétrica de documentos. */
@Service("DocumentoService")
public class DocumentoService {

	@Autowired @Qualifier("IDocumentoRepository") private DocumentoRepository repo;

	@Transactional
	public DocumentoDTO crear(DocumentoDTO d) {
		validar(d);
		if (repo.porCodigo(d.codigoDocumento) != null) {
			throw Validaciones.error(HttpStatus.CONFLICT, "Ya existe un documento con el código " + d.codigoDocumento + ".");
		}
		d.idDocumento = repo.insertar(d);
		return repo.porId(d.idDocumento);
	}

	@Transactional
	public DocumentoDTO actualizar(DocumentoDTO d) {
		if (d == null || d.idDocumento == null || d.idDocumento == 0) {
			throw Validaciones.error(HttpStatus.BAD_REQUEST, "El idDocumento es obligatorio para actualizar.");
		}
		if (repo.porId(d.idDocumento) == null) throw noExiste(d.idDocumento);
		validar(d);
		DocumentoDTO otro = repo.porCodigo(d.codigoDocumento);
		if (otro != null && !otro.idDocumento.equals(d.idDocumento)) {
			throw Validaciones.error(HttpStatus.CONFLICT, "Ya existe otro documento con el código " + d.codigoDocumento + ".");
		}
		if (repo.tieneVehiculosIncompatibles(d.idDocumento, d.tiposVehiculo)) {
			throw Validaciones.error(HttpStatus.CONFLICT, "El tipo de documento no puede excluir vehículos que ya lo tienen asociado.");
		}
		repo.actualizar(d);
		return repo.porId(d.idDocumento);
	}

	@Transactional
	public void eliminar(int id) {
		if (repo.porId(id) == null) throw noExiste(id);
		if (repo.enUso(id)) {
			throw Validaciones.error(HttpStatus.CONFLICT, "El documento está asociado a vehículos y no se puede eliminar.");
		}
		repo.eliminar(id);
	}

	public DocumentoDTO consultar(int id) {
		DocumentoDTO d = repo.porId(id);
		if (d == null) throw noExiste(id);
		return d;
	}

	public List<DocumentoDTO> listar(int pagina, int tamano) {
		int t = Math.max(1, Math.min(tamano, 200));
		return repo.listar(t, Math.max(0, pagina) * t);
	}

	private void validar(DocumentoDTO d) {
		if (d == null) throw Validaciones.error(HttpStatus.BAD_REQUEST, "El cuerpo es obligatorio.");
		if (Validaciones.vacio(d.codigoDocumento) || Validaciones.vacio(d.nombreDocumento)) {
			throw Validaciones.error(HttpStatus.BAD_REQUEST, "codigoDocumento y nombreDocumento son obligatorios.");
		}
		d.codigoDocumento = d.codigoDocumento.trim();
		d.nombreDocumento = d.nombreDocumento.trim();
		String tv = Validaciones.canon(d.tiposVehiculo, "A", "M", "AM");
		if (tv == null) throw Validaciones.error(HttpStatus.BAD_REQUEST, "tiposVehiculo debe ser A, M o AM.");
		d.tiposVehiculo = tv;
		String ob = Validaciones.canon(d.obligatoriedad, "RA", "RM", "RR");
		if (ob == null) throw Validaciones.error(HttpStatus.BAD_REQUEST, "obligatoriedad debe ser RA, RM o RR.");
		d.obligatoriedad = ob;
	}

	private RuntimeException noExiste(int id) {
		return Validaciones.error(HttpStatus.NOT_FOUND, "No existe el documento con id " + id + ".");
	}
}
