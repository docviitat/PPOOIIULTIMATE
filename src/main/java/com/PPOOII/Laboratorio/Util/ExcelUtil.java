package com.PPOOII.Laboratorio.Util;

import java.math.BigDecimal;
import java.text.Normalizer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

/** Utilidades de lectura de celdas Excel (el dato se traslada sin alteraciones). */
public final class ExcelUtil {

	private static final ThreadLocal<DataFormatter> FORMATTER = ThreadLocal.withInitial(DataFormatter::new);

	private ExcelUtil() {}

	/**
	 * Devuelve el contenido de la celda como texto. Los números enteros se devuelven
	 * sin notación científica ni ".0" (ej. 1234567890123 -> "1234567890123").
	 * Una celda vacía devuelve "" (cadena vacía).
	 */
	public static String leerCelda(Row row, int columna, FormulaEvaluator evaluator) {
		Cell cell = row.getCell(columna);
		if (cell == null) return "";
		CellType tipo = cell.getCellType();
		if (tipo == CellType.FORMULA) {
			return FORMATTER.get().formatCellValue(cell, evaluator);
		}
		if (tipo == CellType.NUMERIC && !DateUtil.isCellDateFormatted(cell)
            && "General".equals(cell.getCellStyle().getDataFormatString())) {
			return new BigDecimal(String.valueOf(cell.getNumericCellValue())).stripTrailingZeros().toPlainString();
		}
		if (tipo == CellType.BLANK) return "";
		return FORMATTER.get().formatCellValue(cell);
	}

	/** Una fila está vacía cuando ninguna de las primeras {@code columnas} tiene contenido. */
	public static boolean filaVacia(Row row, int columnas, FormulaEvaluator evaluator) {
		for (int i = 0; i < columnas; i++) {
			if (!leerCelda(row, i, evaluator).trim().isEmpty()) return false;
		}
		return true;
	}

	/** Normaliza un encabezado: sin tildes, mayúsculas y espacios -> '_'. */
	public static String normalizarEncabezado(String texto) {
		if (texto == null) return "";
		String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		return sinTildes.toUpperCase().replaceAll("[\\s]+", "_");
	}
}
