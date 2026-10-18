package com.PPOOII.Laboratorio.Services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PPOOII.Laboratorio.DTO.DocumentoDTO;
import com.PPOOII.Laboratorio.DTO.DocumentoVehiculoDTO;
import com.PPOOII.Laboratorio.DTO.VehiculoDTO;
import com.PPOOII.Laboratorio.Repository.DocumentoRepository;
import com.PPOOII.Laboratorio.Repository.VehiculoRepository;

/** Vehículos y su relación con documentos (Entrega 1) + cargue de documentos (Entrega 2). */
@Service("VehiculoService")
public class VehiculoService {

	public static final String EN_VERIFICACION = "En Verificacion";
	private static final String[] TIPOS = { "Automovil", "Motocicleta" };
	private static final String[] COMBUSTIBLES = { "Gasolina", "Diesel", "Gas", "Electrico", "Hibrido" };
	private static final String[] ESTADOS_DOC = { "Habilitado", "Vencido", EN_VERIFICACION };

	@Autowired @Qualifier("IVehiculoRepository") private VehiculoRepository repo;
	@Autowired @Qualifier("IDocumentoRepository") private DocumentoRepository docRepo;

	// ------------------------------------------------------------------ CRUD
	/** Crea el vehículo. Debe traer al menos un documento; todos quedan En Verificacion. */
	@Transactional
	public VehiculoDTO crear(VehiculoDTO v) {
		validar(v);
		if (v.documentos == null || v.documentos.isEmpty()) {
			throw Validaciones.error(HttpStatus.BAD_REQUEST, "No se puede crear un vehículo sin un documento asociado.");
		}
		if (repo.porPlaca(v.placa) != null) {
			throw Validaciones.error(HttpStatus.CONFLICT, "Ya existe un vehículo con la placa " + v.placa + ".");
		}
		int id = repo.insertar(v);
		guardarDocumentos(id, v.tipoVehiculo, v.documentos, false, false);
		return repo.porId(id);
	}

	@Transactional
	public VehiculoDTO actualizar(VehiculoDTO v) {
		if (v == null || v.idVehiculo == null || v.idVehiculo == 0) {
			throw Validaciones.error(HttpStatus.BAD_REQUEST, "El idVehiculo es obligatorio para actualizar.");
		}
		if (repo.porId(v.idVehiculo) == null) throw noExiste(v.idVehiculo);
		validar(v);
		VehiculoDTO otro = repo.porPlaca(v.placa);
		if (otro != null && !otro.idVehiculo.equals(v.idVehiculo)) {
			throw Validaciones.error(HttpStatus.CONFLICT, "Ya existe otro vehículo con la placa " + v.placa + ".");
		}
		repo.actualizar(v);
		return repo.porId(v.idVehiculo);
	}

	@Transactional
	public void eliminar(int id) {
		if (repo.porId(id) == null) throw noExiste(id);
		if (repo.tieneRelaciones(id)) {
			throw Validaciones.error(HttpStatus.CONFLICT, "El vehículo tiene trayectos o conductores asociados y no se puede eliminar.");
		}
		repo.eliminar(id);
	}

	public VehiculoDTO consultar(int id) {
		VehiculoDTO v = repo.porId(id);
		if (v == null) throw noExiste(id);
		return v;
	}

	public List<VehiculoDTO> listar(int pagina, int tamano) {
		int t = Math.max(1, Math.min(tamano, 200));
		return repo.listar(t, Math.max(0, pagina) * t);
	}

	// ------------------------------------------------------------ Consultas
	public VehiculoDTO buscarPorPlaca(String placa) {
		VehiculoDTO v = repo.porPlaca(placa == null ? "" : placa.trim().toUpperCase());
		if (v == null) throw Validaciones.error(HttpStatus.NOT_FOUND, "No existe un vehículo con placa " + placa + ".");
		return v;
	}

	public List<VehiculoDTO> buscarPorTipo(String tipo) {
		String t = Validaciones.canon(tipo, TIPOS);
		if (t == null) throw Validaciones.error(HttpStatus.BAD_REQUEST, "El tipo de vehículo debe ser Automovil o Motocicleta.");
		return repo.porTipo(t);
	}

	/** Vehículos que tienen en común un tipo de documento (por su código, p. ej. SOAT). */
	public List<VehiculoDTO> buscarPorTipoDocumento(String codigoDocumento) {
		if (docRepo.porCodigo(codigoDocumento == null ? "" : codigoDocumento.trim()) == null) {
			throw Validaciones.error(HttpStatus.NOT_FOUND, "No existe el tipo de documento " + codigoDocumento + ".");
		}
		return repo.porCodigoDocumento(codigoDocumento.trim());
	}

	public List<VehiculoDTO> buscarPorEstadoDocumento(String estado) {
		String e = Validaciones.canon(estado, ESTADOS_DOC);
		if (e == null) throw Validaciones.error(HttpStatus.BAD_REQUEST, "El estado debe ser Habilitado, Vencido o En Verificacion.");
		return repo.porEstadoDocumento(e);
	}

	// ----------------------------------------------- Documentos del vehículo
	/** E1: agrega documentos a un vehículo (quedan En Verificacion). */
	@Transactional
	public VehiculoDTO agregarDocumentos(int idVehiculo, List<DocumentoVehiculoDTO> docs) {
		VehiculoDTO v = consultar(idVehiculo);
		guardarDocumentos(idVehiculo, v.tipoVehiculo, docs, false, false);
		return repo.porId(idVehiculo);
	}

	/** E2: carga y/o actualiza uno o varios documentos PDF (BASE64) de un vehículo. */
	@Transactional
	public VehiculoDTO cargarDocumentos(String placa, List<DocumentoVehiculoDTO> docs) {
		VehiculoDTO v = buscarPorPlaca(placa);
		guardarDocumentos(v.idVehiculo, v.tipoVehiculo, docs, true, true);
		return repo.porId(v.idVehiculo);
	}

	private void guardarDocumentos(int idVehiculo, String tipoVehiculo, List<DocumentoVehiculoDTO> docs,
			boolean upsert, boolean exigirArchivo) {
		if (docs == null || docs.isEmpty()) {
			throw Validaciones.error(HttpStatus.BAD_REQUEST, "Debe enviar al menos un documento.");
		}
		String letra = "Automovil".equals(tipoVehiculo) ? "A" : "M";
		Set<Integer> vistos = new HashSet<>();
		for (DocumentoVehiculoDTO d : docs) {
			if (d == null) throw Validaciones.error(HttpStatus.BAD_REQUEST, "Hay un documento vacío en la lista.");
			DocumentoDTO param = null;
			if (d.idDocumento != null) param = docRepo.porId(d.idDocumento);
			else if (!Validaciones.vacio(d.codigoDocumento)) param = docRepo.porCodigo(d.codigoDocumento.trim());
			if (param == null) {
				throw Validaciones.error(HttpStatus.BAD_REQUEST, "Cada documento debe indicar un idDocumento o codigoDocumento existente.");
			}
			if (!"AM".equals(param.tiposVehiculo) && !letra.equals(param.tiposVehiculo)) {
				throw Validaciones.error(HttpStatus.BAD_REQUEST, "El documento " + param.codigoDocumento + " no aplica a vehículos tipo " + tipoVehiculo + ".");
			}
			if (!vistos.add(param.idDocumento)) {
				throw Validaciones.error(HttpStatus.BAD_REQUEST, "El documento " + param.codigoDocumento + " está repetido en la solicitud.");
			}
			if (Validaciones.vacio(d.numeroDocumento) || d.fechaExpedicion == null || d.fechaVencimiento == null) {
				throw Validaciones.error(HttpStatus.BAD_REQUEST, "El documento " + param.codigoDocumento + " requiere numeroDocumento, fechaExpedicion y fechaVencimiento.");
			}
			if (!d.fechaVencimiento.isAfter(d.fechaExpedicion)) {
				throw Validaciones.error(HttpStatus.BAD_REQUEST, "En " + param.codigoDocumento + " la fecha de vencimiento debe ser posterior a la de expedición.");
			}
			byte[] pdf = null;
			if (!Validaciones.vacio(d.archivoBase64)) pdf = Validaciones.decodificarPdf(d.archivoBase64);
			else if (exigirArchivo) {
				throw Validaciones.error(HttpStatus.BAD_REQUEST, "El documento " + param.codigoDocumento + " requiere archivoBase64 (PDF).");
			}
			Long existente = repo.idRelacion(idVehiculo, param.idDocumento);
			if (existente != null) {
				if (!upsert) {
					throw Validaciones.error(HttpStatus.CONFLICT, "El vehículo ya tiene asociado el documento " + param.codigoDocumento + ".");
				}
				repo.actualizarDocumento(existente, d.numeroDocumento.trim(), d.fechaExpedicion, d.fechaVencimiento, pdf, EN_VERIFICACION);
			} else {
				repo.insertarDocumento(idVehiculo, param.idDocumento, param.codigoDocumento, d.numeroDocumento.trim(),
						d.fechaExpedicion, d.fechaVencimiento, pdf, EN_VERIFICACION);
			}
		}
	}

	// ------------------------------------------------- Servicios públicos E2
	/** Vehículos con documentos vencidos; cada vehículo trae solo sus documentos vencidos. */
	public List<VehiculoDTO> vehiculosConDocumentosVencidos() {
		LocalDate hoy = LocalDate.now();
		List<VehiculoDTO> r = repo.conDocumentosVencidos();
		for (VehiculoDTO v : r) {
			List<DocumentoVehiculoDTO> f = new ArrayList<>();
			for (DocumentoVehiculoDTO d : v.documentos) {
				if ("Vencido".equals(d.estadoDocumento) || (d.fechaVencimiento != null && d.fechaVencimiento.isBefore(hoy))) f.add(d);
			}
			v.documentos = f;
		}
		return r;
	}

	/** Vehículos con documentos que vencen en los próximos {@code dias} días. */
	public List<VehiculoDTO> vehiculosConDocumentosPorVencer(int dias) {
		if (dias < 0) throw Validaciones.error(HttpStatus.BAD_REQUEST, "El parámetro dias no puede ser negativo.");
		LocalDate hoy = LocalDate.now();
		LocalDate limite = hoy.plusDays(dias);
		List<VehiculoDTO> r = repo.conDocumentosPorVencer(dias);
		for (VehiculoDTO v : r) {
			List<DocumentoVehiculoDTO> f = new ArrayList<>();
			for (DocumentoVehiculoDTO d : v.documentos) {
				if (!"Vencido".equals(d.estadoDocumento) && d.fechaVencimiento != null
						&& !d.fechaVencimiento.isBefore(hoy) && !d.fechaVencimiento.isAfter(limite)) f.add(d);
			}
			v.documentos = f;
		}
		return r;
	}

	/** Vehículo por placa con la información de sus conductores y de sus documentos. */
	public Map<String, Object> vehiculoConConductores(String placa) {
		VehiculoDTO v = buscarPorPlaca(placa);
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("vehiculo", v);
		m.put("conductores", repo.conductoresDe(v.idVehiculo));
		return m;
	}

	// ------------------------------------------------------------- Validación
	private void validar(VehiculoDTO v) {
		if (v == null) throw Validaciones.error(HttpStatus.BAD_REQUEST, "El cuerpo es obligatorio.");
		if (Validaciones.vacio(v.placa) || !v.placa.trim().toUpperCase().matches("[A-Z0-9]{6}")) {
			throw Validaciones.error(HttpStatus.BAD_REQUEST, "La placa es obligatoria y debe tener 6 caracteres alfanuméricos.");
		}
		v.placa = v.placa.trim().toUpperCase();
		String tv = Validaciones.canon(v.tipoVehiculo, TIPOS);
		if (tv == null) throw Validaciones.error(HttpStatus.BAD_REQUEST, "tipoVehiculo debe ser Automovil o Motocicleta.");
		v.tipoVehiculo = tv;
		String ts = Validaciones.canon(v.tipoServicio, "A", "M", "AM");
		if (ts == null) throw Validaciones.error(HttpStatus.BAD_REQUEST, "tipoServicio debe ser A, M o AM.");
		v.tipoServicio = ts;
		String tc = Validaciones.canon(v.tipoCombustible, COMBUSTIBLES);
		if (tc == null) throw Validaciones.error(HttpStatus.BAD_REQUEST, "tipoCombustible debe ser Gasolina, Diesel, Gas, Electrico o Hibrido.");
		v.tipoCombustible = tc;
		if (v.capacidadPasajeros == null || v.capacidadPasajeros <= 0) {
			throw Validaciones.error(HttpStatus.BAD_REQUEST, "capacidadPasajeros debe ser mayor a 0.");
		}
		if (v.modelo == null || v.modelo < 1900 || v.modelo > LocalDate.now().getYear() + 1) {
			throw Validaciones.error(HttpStatus.BAD_REQUEST, "El modelo no es válido.");
		}
		if (Validaciones.vacio(v.color) || Validaciones.vacio(v.marca) || Validaciones.vacio(v.linea)) {
			throw Validaciones.error(HttpStatus.BAD_REQUEST, "color, marca y linea son obligatorios.");
		}
	}

	private RuntimeException noExiste(int id) {
		return Validaciones.error(HttpStatus.NOT_FOUND, "No existe el vehículo con id " + id + ".");
	}
}
