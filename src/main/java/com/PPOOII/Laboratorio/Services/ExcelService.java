package com.PPOOII.Laboratorio.Services;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.PPOOII.Laboratorio.Exception.CargueException;
import com.PPOOII.Laboratorio.Repository.PersonaRepository;
import com.PPOOII.Laboratorio.Services.Interfaces.IExcelService;
import com.PPOOII.Laboratorio.Util.ExcelUtil;

@Service("ExcelService")
public class ExcelService implements IExcelService{

	// ========= INYECCIÓN DE DEPENDENCIAS ==========
	@Autowired
	@Qualifier("IPersonaRepository")
	private PersonaRepository IPersonaRepository;
	//==================== LOGS ============================
	private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(ExcelService.class);

	@Override
	public double processExcel(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new CargueException(HttpStatus.BAD_REQUEST, "Debe adjuntar un archivo Excel con datos.");
		}
		double idCargue = Double.parseDouble(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
		int leidos = 0;
		try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
			// Se procesa la primera hoja del libro
			Sheet sheet = workbook.getSheetAt(0);
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			for (Row row : sheet) {
				if (row.getRowNum() == 0) continue; // encabezado: NOMBRE | EDAD | UBICACIÓN
				if (ExcelUtil.filaVacia(row, 3, evaluator)) continue;
				String pnombre = ExcelUtil.leerCelda(row, 0, evaluator);
				String edad = ExcelUtil.leerCelda(row, 1, evaluator);
				String ubicacion = ExcelUtil.leerCelda(row, 2, evaluator);
				logger.info("Dato: {} - {} - {} - {}", pnombre, edad, ubicacion, idCargue);
				if (this.cargarDatosExcel(pnombre, edad, ubicacion, idCargue)) leidos++;
			}
		} catch (IOException | RuntimeException e) {
			if (e instanceof CargueException) throw (CargueException) e;
			logger.error("ERROR LEYENDO EL ARCHIVO EXCEL: {}", e.getMessage());
			throw new CargueException(HttpStatus.BAD_REQUEST, "No fue posible leer el archivo Excel: " + e.getMessage());
		}
		if (leidos == 0) {
			throw new CargueException(HttpStatus.BAD_REQUEST, "El archivo no contiene registros para cargar.");
		}
		if (this.validarDatosExcel(idCargue)) {
			logger.info("Validación de datos cargados. EXITOSO");
			if (this.procesarDatosExcel(idCargue)) {
				logger.info("Procesado de datos validados. EXITOSO");
			}
		}
		return idCargue;
	}

	@Override
	public boolean cargarDatosExcel(String pnombre, String edad, String ubicacion, Double idCargue) {
		try {
			IPersonaRepository.CARGAR_DATOS_EXCEL(pnombre, edad, ubicacion, idCargue);
			logger.info("Se carga registro de excel a tabla temporal de forma exitosa!!!!");
			return true;
		}catch(Exception e) {
			logger.error("ERROR CARGAR DATOS DE ARCHIVO EXCEL: PROBLEMAS CON LA INVOCACIÓN DEL PROCEDIMIENTO ALMACENADO DE CARGUE DE DATOS!");
			return false;
		}
	}

	@Override
	public boolean validarDatosExcel(Double idCargue) {
		try {
			IPersonaRepository.VALIDAR_DATOS_CARGUE(idCargue);
			logger.info("Se validan los registros de la tabla temporal de forma exitosa!!!!");
			return true;
		}catch(Exception e) {
			logger.error("ERROR VALIDANDO DATOS DE ARCHIVO EXCEL: PROBLEMAS CON LA INVOCACIÓN DEL PROCEDIMIENTO ALMACENADO DE VALIDACIÓN DE DATOS!");
			return false;
		}
	}

	@Override
	public boolean procesarDatosExcel(Double idCargue) {
		try {
			IPersonaRepository.PROCESAR_DATOS_EXCEL(idCargue);
			logger.info("Se procesan los datos validados de forma exitosa y se trasladan a la tabla transaccional!!!!");
			return true;
		}catch(Exception e) {
			logger.error("ERROR PROCESAR DATOS DE ARCHIVO EXCEL: PROBLEMAS CON LA INVOCACIÓN DEL PROCEDIMIENTO ALMACENADO DE PROCESADO DE DATOS!");
			return false;
		}
	}
}
