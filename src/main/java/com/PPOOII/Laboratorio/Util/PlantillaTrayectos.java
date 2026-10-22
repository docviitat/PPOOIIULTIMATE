package com.PPOOII.Laboratorio.Util;

import java.io.*;
import java.nio.file.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Una misma definición para la descarga web y los archivos Excel del proyecto. */
public final class PlantillaTrayectos {
    public static final String[] COLUMNAS={"CODIGO_RUTA","ORDEN","UBICACION","IDENTIFICACION_CONDUCTOR","PLACA","NOMBRE_CONDUCTOR","EDAD_CONDUCTOR"};
    private PlantillaTrayectos() {}
    public static byte[] generar(boolean ejemplo) {
        try(var wb=new XSSFWorkbook();var out=new ByteArrayOutputStream()) {
            var sheet=wb.createSheet("Trayectos");sheet.createFreezePane(0,1);
            var title=wb.createCellStyle();title.setFillForegroundColor(IndexedColors.TEAL.getIndex());title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            var font=wb.createFont();font.setBold(true);font.setColor(IndexedColors.WHITE.getIndex());title.setFont(font);title.setWrapText(true);
            var text=wb.createCellStyle();text.setDataFormat(wb.createDataFormat().getFormat("@"));text.setVerticalAlignment(VerticalAlignment.CENTER);
            var number=wb.createCellStyle();number.setDataFormat(wb.createDataFormat().getFormat("0"));
            var header=sheet.createRow(0);header.setHeightInPoints(34);
            int[] widths={22,12,58,30,18,30,22};
            var drawing=sheet.createDrawingPatriarch();
            String[] tips={"Obligatorio. Código de ruta, máximo 30 caracteres. Use un código nuevo para una ruta nueva.",
                "Obligatorio. Entero entre 0 y 99999. No repita el mismo orden dentro de una ruta.",
                "Obligatorio. Dirección o lugar de la parada, con ciudad. Máximo 255 caracteres.",
                "Obligatorio. Identificación de un conductor registrado y autorizado para el vehículo. Conserve los ceros iniciales.",
                "Obligatorio. Placa de un vehículo registrado con documentos habilitados y vigentes.",
                "Opcional. Nombre de referencia del conductor; no crea una persona nueva.",
                "Opcional. Edad de referencia; no modifica los datos del conductor."};
            for(int c=0;c<COLUMNAS.length;c++) {
                var cell=header.createCell(c);cell.setCellValue(COLUMNAS[c]);cell.setCellStyle(title);
                sheet.setColumnWidth(c,widths[c]*256);sheet.setDefaultColumnStyle(c,c==1?number:text);
                var anchor=wb.getCreationHelper().createClientAnchor();anchor.setCol1(c);anchor.setCol2(c+3);anchor.setRow1(0);anchor.setRow2(4);
                var comment=drawing.createCellComment(anchor);comment.setString(wb.getCreationHelper().createRichTextString(tips[c]));comment.setAuthor("Rutas y trayectos");cell.setCellComment(comment);
            }
            sheet.setAutoFilter(new CellRangeAddress(0,0,0,6));
            var helper=sheet.getDataValidationHelper();var constraint=helper.createIntegerConstraint(DataValidationConstraint.OperatorType.BETWEEN,"0","99999");
            var validation=helper.createValidation(constraint,new CellRangeAddressList(1,5000,1,1));
            validation.setShowErrorBox(true);validation.setErrorStyle(DataValidation.ErrorStyle.STOP);validation.createErrorBox("Orden inválido","Use un número entero entre 0 y 99999.");sheet.addValidationData(validation);
            for(int c:new int[]{0,2,3,4}) {
                int max=c==0?30:c==2?255:c==3?20:6;
                var rule=helper.createTextLengthConstraint(DataValidationConstraint.OperatorType.BETWEEN,"1",String.valueOf(max));
                var v=helper.createValidation(rule,new CellRangeAddressList(1,5000,c,c));v.setShowErrorBox(true);v.setErrorStyle(DataValidation.ErrorStyle.STOP);
                v.createErrorBox("Revise este campo","Complete este dato. Longitud máxima: "+max+" caracteres.");sheet.addValidationData(v);
            }
            if(ejemplo) {
                String[] ubicaciones={"Parque Murillo Toro, Ibagué, Tolima","Terminal de Transportes, Ibagué, Tolima","Universidad del Tolima, Ibagué, Tolima"};
                for(int i=0;i<3;i++) {
                    var row=sheet.createRow(i+1);row.setHeightInPoints(24);
                    String[] values={"NUEVA-001",String.valueOf(i),ubicaciones[i],"9001","TST001","Pedro Lopez","35"};
                    for(int c=0;c<7;c++) { var cell=row.createCell(c);if(c==1){cell.setCellValue(i);cell.setCellStyle(number);}else{cell.setCellValue(values[c]);cell.setCellStyle(text);} }
                }
            }
            var guia=wb.createSheet("Guía de uso");guia.setColumnWidth(0,32*256);guia.setColumnWidth(1,105*256);
            String[][] instrucciones={
                {"CARGUE MASIVO DE TRAYECTOS","Complete la primera hoja: Trayectos. Esta hoja es solo una guía."},
                {"1. Prepare los datos","Una fila representa una parada. Puede incluir varias rutas en el mismo archivo."},
                {"2. Complete los obligatorios","CODIGO_RUTA, ORDEN, UBICACION, IDENTIFICACION_CONDUCTOR y PLACA."},
                {"3. Datos de referencia","NOMBRE_CONDUCTOR y EDAD_CONDUCTOR son opcionales. No crean ni modifican conductores."},
                {"4. Verifique las relaciones","El conductor debe estar registrado y en PO con el vehículo. Todos los documentos deben estar habilitados y vigentes."},
                {"5. Evite duplicados","El par código de ruta + orden debe ser único en el archivo y en la aplicación."},
                {"6. Suba el archivo","En rutas.html, inicie sesión, abra Importar Excel, seleccione el archivo y pulse Importar trayectos."},
                {"7. Revise el resultado","Se agregan las filas válidas; cada fila rechazada muestra los motivos. Conserve el ID del cargue para consultarlo."},
                {"Formato","Guarde como .xlsx o .xls. No renombre ni reordene los encabezados. Máximo 10 MB."},
                {"Identificaciones y placas","Use texto para conservar los ceros iniciales. No se generan conductores ni vehículos desde este archivo."},
                {"Un archivo por minuto","No envíe dos archivos del mismo tipo en un mismo minuto. Si recibe 409, espere al siguiente minuto."},
                {"Sobre el ejemplo",ejemplo?"Usa el conductor 9001 y el vehículo TST001 de los datos iniciales. Adapte estos valores a su instalación y cambie NUEVA-001 si ya existe.":"Esta plantilla está vacía y no agrega paradas de demostración. Empiece en la fila 2."}
            };
            var wrap=wb.createCellStyle();wrap.setWrapText(true);wrap.setVerticalAlignment(VerticalAlignment.CENTER);
            for(int i=0;i<instrucciones.length;i++) {var row=guia.createRow(i);row.setHeightInPoints(i==0?36:48);for(int c=0;c<2;c++){var cell=row.createCell(c);cell.setCellValue(instrucciones[i][c]);cell.setCellStyle(i==0?title:wrap);}}
            wb.write(out);return out.toByteArray();
        } catch(IOException e) {throw new IllegalStateException("No fue posible generar el Excel de trayectos",e);}
    }
    public static void main(String[] args) throws IOException {
        Path folder=Path.of(args.length==0?"docs/plantillas":args[0]);Files.createDirectories(folder);
        Files.write(folder.resolve("Plantilla_Cargue_Trayectos.xlsx"),generar(false));
        Files.write(folder.resolve("Ejemplo_Cargue_Trayectos.xlsx"),generar(true));
    }
}
