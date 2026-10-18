package com.PPOOII.Laboratorio.Services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.PPOOII.Laboratorio.DTO.CargueConsultaDTO;
import com.PPOOII.Laboratorio.DTO.CargueResumenDTO;
import com.PPOOII.Laboratorio.Entities.TrayectoTmp;
import com.PPOOII.Laboratorio.Exception.CargueException;
import com.PPOOII.Laboratorio.Repository.TrayectoTmpRepository;
import com.PPOOII.Laboratorio.Services.Interfaces.ITrayectoCargueService;
import com.PPOOII.Laboratorio.Util.ExcelUtil;

@Service("TrayectoCargueService")
public class TrayectoCargueServiceImpl implements ITrayectoCargueService {

	/** Encabezados de la plantilla (orden fijo de columnas). */
	public static final String[] ENCABEZADOS = { "CODIGO_RUTA", "ORDEN", "UBICACION", "IDENTIFICACION_CONDUCTOR", "PLACA" };
	private static final DateTimeFormatter FORMATO_ID_CARGUE = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
	private static final String[] ESTADOS = { "CARGADO", "VALIDADO", "PROCESADO", "ERROR" };

	// ========= INYECCIÓN DE DEPENDENCIAS ==========
	@Autowired
	@Qualifier("ITrayectoTmpRepository")
	private TrayectoTmpRepository ITrayectoTmpRepository;

	// ==================== LOGS ============================
	private static final Logger logger = LogManager.getLogger(TrayectoCargueServiceImpl.class);

	@Override
	public CargueResumenDTO procesarArchivo(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new CargueException(HttpStatus.BAD_REQUEST, "Debe adjuntar un archivo Excel con datos.");
		}
		String nombre = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
		String minusculas = nombre.toLowerCase();
		if (!(minusculas.endsWith(".xlsx") || minusculas.endsWith(".xls"))) {
			throw new CargueException(HttpStatus.BAD_REQUEST, "El archivo debe ser de tipo Excel (.xlsx o .xls).");
		}

		long leidos = 0;
		long fallidosCargue = 0;
		Long idCargue;

		try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
			Sheet sheet = workbook.getSheetAt(0);
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			validarEncabezados(sheet, evaluator);

			idCargue = generarIdCargue();

			for (Row row : sheet) {
				if (row.getRowNum() == 0) continue; // encabezado
				if (ExcelUtil.filaVacia(row, ENCABEZADOS.length, evaluator)) continue;
				leidos++;
				boolean ok = cargarTrayecto(
						ExcelUtil.leerCelda(row, 0, evaluator),
						ExcelUtil.leerCelda(row, 1, evaluator),
						ExcelUtil.leerCelda(row, 2, evaluator),
						ExcelUtil.leerCelda(row, 3, evaluator),
						ExcelUtil.leerCelda(row, 4, evaluator),
						idCargue);
				if (!ok) fallidosCargue++;
			}
		} catch (IOException | RuntimeException e) {
			if (e instanceof CargueException) throw (CargueException) e;
			logger.error("ERROR LEYENDO EL ARCHIVO EXCEL: {}", e.getMessage());
			throw new CargueException(HttpStatus.BAD_REQUEST, "No fue posible leer el archivo Excel: " + e.getMessage());
		}

		if (leidos == 0) {
			throw new CargueException(HttpStatus.BAD_REQUEST, "El archivo no contiene registros para cargar.");
		}

		String mensaje;
		if (!validarCargue(idCargue)) {
			mensaje = "Los registros se cargaron pero la validación falló; revise los logs del servidor.";
		} else if (!procesarCargue(idCargue)) {
			mensaje = "Los registros se validaron pero el procesado falló; revise los logs del servidor.";
		} else {
			mensaje = "Cargue finalizado. Consulte el detalle con el idCargue; los registros en ERROR traen su observación.";
		}
		if (fallidosCargue > 0) {
			mensaje += " " + fallidosCargue + " fila(s) no pudieron insertarse en la tabla temporal.";
		}
		return new CargueResumenDTO(idCargue, nombre, leidos - fallidosCargue, resumenPorEstado(idCargue), mensaje);
	}

	private void validarEncabezados(Sheet sheet, FormulaEvaluator evaluator) {
		Row header = sheet.getRow(0);
		if (header == null) {
			throw new CargueException(HttpStatus.BAD_REQUEST, "El archivo no tiene encabezados. Use la plantilla oficial.");
		}
		for (int i = 0; i < ENCABEZADOS.length; i++) {
			String encontrado = ExcelUtil.normalizarEncabezado(ExcelUtil.leerCelda(header, i, evaluator));
			if (!ENCABEZADOS[i].equals(encontrado)) {
				throw new CargueException(HttpStatus.BAD_REQUEST,
						"El archivo no tiene la estructura de la plantilla. Se esperaba la columna "
								+ (i + 1) + " = " + ENCABEZADOS[i] + " y se encontró '" + encontrado + "'.");
			}
		}
	}

	/** yyyyMMddHHmm del momento del cargue; si ya existe, avanza al siguiente minuto libre. */
	private synchronized Long generarIdCargue() {
		LocalDateTime momento = LocalDateTime.now();
		Long id = Long.parseLong(momento.format(FORMATO_ID_CARGUE));
		while (ITrayectoTmpRepository.existsByIdCargue(id)) {
			momento = momento.plusMinutes(1);
			id = Long.parseLong(momento.format(FORMATO_ID_CARGUE));
		}
		return id;
	}

	@Override
	public boolean cargarTrayecto(String codigoRuta, String orden, String ubicacion,
			String identificacionConductor, String placa, Long idCargue) {
		try {
			ITrayectoTmpRepository.CARGAR_TRAYECTOS_EXCEL(codigoRuta, orden, ubicacion, identificacionConductor, placa, idCargue);
			return true;
		} catch (Exception e) {
			logger.error("ERROR CARGAR TRAYECTO: PROBLEMAS CON LA INVOCACIÓN DEL PROCEDIMIENTO ALMACENADO DE CARGUE: {}", e.getMessage());
			return false;
		}
	}

	@Override
	public boolean validarCargue(Long idCargue) {
		try {
			ITrayectoTmpRepository.VALIDAR_TRAYECTOS_CARGUE(idCargue);
			logger.info("Validación del cargue {} finalizada.", idCargue);
			return true;
		} catch (Exception e) {
			logger.error("ERROR VALIDANDO TRAYECTOS: PROBLEMAS CON LA INVOCACIÓN DEL PROCEDIMIENTO ALMACENADO DE VALIDACIÓN: {}", e.getMessage());
			return false;
		}
	}

	@Override
	public boolean procesarCargue(Long idCargue) {
		try {
			String login = "cargue";
			org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.getName() != null) login = auth.getName();
			ITrayectoTmpRepository.PROCESAR_TRAYECTOS_EXCEL(idCargue, login);
			logger.info("Procesado del cargue {} finalizado.", idCargue);
			return true;
		} catch (Exception e) {
			logger.error("ERROR PROCESANDO TRAYECTOS: PROBLEMAS CON LA INVOCACIÓN DEL PROCEDIMIENTO ALMACENADO DE PROCESADO: {}", e.getMessage());
			return false;
		}
	}

	@Override
	public CargueConsultaDTO consultarCargue(Long idCargue, Pageable pageable) {
		if (idCargue == null || !ITrayectoTmpRepository.existsByIdCargue(idCargue)) {
			throw new CargueException(HttpStatus.NOT_FOUND, "No existe un cargue con el identificador " + idCargue + ".");
		}
		Page<TrayectoTmp> pagina = ITrayectoTmpRepository.findByIdCargue(idCargue, pageable);
		Map<String, Long> porEstado = resumenPorEstado(idCargue);
		long total = porEstado.values().stream().mapToLong(Long::longValue).sum();
		return new CargueConsultaDTO(idCargue, total, porEstado, pagina.getNumber(), pagina.getSize(), pagina.getContent());
	}

	private Map<String, Long> resumenPorEstado(Long idCargue) {
		Map<String, Long> resumen = new LinkedHashMap<>();
		for (String estado : ESTADOS) resumen.put(estado, 0L);
		List<Object[]> filas = ITrayectoTmpRepository.contarPorEstado(idCargue);
		for (Object[] fila : filas) {
			resumen.put(String.valueOf(fila[0]), ((Number) fila[1]).longValue());
		}
		return resumen;
	}

	@Override
	public byte[] generarPlantilla() {
		try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet hoja = wb.createSheet("Trayectos");
			CellStyle estilo = wb.createCellStyle();
			Font negrita = wb.createFont();
			negrita.setBold(true);
			estilo.setFont(negrita);
			estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			Row encabezado = hoja.createRow(0);
			for (int i = 0; i < ENCABEZADOS.length; i++) {
				encabezado.createCell(i).setCellValue(ENCABEZADOS[i]);
				encabezado.getCell(i).setCellStyle(estilo);
				hoja.setColumnWidth(i, 6000);
			}
			Row ejemplo = hoja.createRow(1);
			ejemplo.createCell(0).setCellValue("T-100");
			ejemplo.createCell(1).setCellValue(0);
			ejemplo.createCell(2).setCellValue("Parque Murillo Toro, Ibagué, Tolima");
			ejemplo.createCell(3).setCellValue("9001");
			ejemplo.createCell(4).setCellValue("TST001");
			hoja.setColumnWidth(2, 14000);

			wb.write(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new CargueException(HttpStatus.INTERNAL_SERVER_ERROR, "No fue posible generar la plantilla.");
		}
	}
}
