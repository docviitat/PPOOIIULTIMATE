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
	@Autowired private CargueIdService cargueIds;
	//==================== LOGS ============================
	private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(ExcelService.class);

	@Override
	@org.springframework.transaction.annotation.Transactional
	public double processExcel(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new CargueException(HttpStatus.BAD_REQUEST, "Debe adjuntar un archivo Excel con datos.");
		}
		String nombre = file.getOriginalFilename()==null ? "" : file.getOriginalFilename().toLowerCase();
        if (!nombre.endsWith(".xlsx") && !nombre.endsWith(".xls"))
            throw new CargueException(HttpStatus.BAD_REQUEST,"El archivo debe ser Excel (.xls o .xlsx).");
        double idCargue = cargueIds.reservar("PERSONAS");
		int leidos = 0;
		try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
			// Se procesa la primera hoja del libro
			Sheet sheet = workbook.getSheetAt(0);
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Row header=sheet.getRow(0);
            if (header==null) throw new CargueException(HttpStatus.BAD_REQUEST,"Faltan los encabezados NOMBRE, EDAD, UBICACION.");
            String[] columnas={"NOMBRE","EDAD","UBICACION"};
            for(int i=0;i<3;i++) {
                String h=ExcelUtil.normalizarEncabezado(ExcelUtil.leerCelda(header,i,evaluator));
                if (i==0 && "PNOMBRE".equals(h)) h="NOMBRE";
                if(!columnas[i].equals(h)) throw new CargueException(HttpStatus.BAD_REQUEST,"Se esperaba la columna "+columnas[i]);
            }
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
			throw new CargueException(HttpStatus.INTERNAL_SERVER_ERROR,"Falló un procedimiento del cargue de personas; se revierte el archivo.");
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
			throw new CargueException(HttpStatus.INTERNAL_SERVER_ERROR,"Falló un procedimiento del cargue de personas; se revierte el archivo.");
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
			throw new CargueException(HttpStatus.INTERNAL_SERVER_ERROR,"Falló un procedimiento del cargue de personas; se revierte el archivo.");
		}
	}
}
