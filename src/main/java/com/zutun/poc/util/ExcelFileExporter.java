package com.zutun.poc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.zutun.poc.model.Item;
import com.zutun.poc.model.Resume;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelFileExporter {

  public static final String REPORT_LOAD_SHEET_NAME = "RESULTADO";
  public static final List<String> REPORT_HEADERS = getReportHeaders();

  public static ByteArrayInputStream loadFile(List<Item> items, Resume resume) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerFont.setColor(IndexedColors.BLACK.getIndex());

      CellStyle headerCellStyle = workbook.createCellStyle();
      headerCellStyle.setFont(headerFont);

      Sheet sheet = workbook.createSheet(REPORT_LOAD_SHEET_NAME);
      Row headerRow = sheet.createRow(0);

      List<String> headersReport = REPORT_HEADERS;

      Font observationFont = workbook.createFont();
      observationFont.setColor(IndexedColors.RED.getIndex());

      CellStyle observationCellStyle = workbook.createCellStyle();
      observationCellStyle.setFont(observationFont);

      Cell cell;
      int indexHeader = 0;
      for (String header : headersReport) {
        cell = headerRow.createCell(indexHeader);
        cell.setCellValue(header);
        cell.setCellStyle(headerCellStyle);
        indexHeader++;
      }

      int indexRow = 0;
      for (Item item : items) {
        int indexColumn = 0;

        Row dataRow = sheet.createRow(indexRow + 1);

        Cell order = dataRow.createCell(indexColumn);
        order.setCellValue(item.getOrder());
        Cell description = dataRow.createCell(++indexColumn);
        description.setCellValue(item.getDescription());
        Cell depth = dataRow.createCell(++indexColumn);
        depth.setCellValue(item.getDepth());
        Cell width = dataRow.createCell(++indexColumn);
        width.setCellValue(item.getWidth());
        Cell heigth = dataRow.createCell(++indexColumn);
        heigth.setCellValue(item.getHeight());
        Cell weight = dataRow.createCell(++indexColumn);
        weight.setCellValue(item.getWeight());
        Cell volume = dataRow.createCell(++indexColumn);
        volume.setCellValue(item.getVolume());
        dataRow
            .createCell(++indexColumn)
            .setCellValue(
                Objects.isNull(item.getVehicle()) ? "" : item.getVehicle().getVehicleInfo());
        dataRow
            .createCell(++indexColumn)
            .setCellValue(
                Objects.isNull(item.getVehicle()) ? "" : item.getVehicle().getDimensions());
        dataRow
            .createCell(++indexColumn)
            .setCellValue(
                Objects.isNull(item.getVehicle()) ? "" : item.getVehicle().getVolume().toString());

        if (!Objects.isNull(item.getObservations())) {
          Cell observations = dataRow.createCell(++indexColumn);
          observations.setCellValue(
              Objects.isNull(item.getObservations()) ? "" : item.getObservations().get(0));

          order.setCellStyle(observationCellStyle);
          description.setCellStyle(observationCellStyle);
          depth.setCellStyle(observationCellStyle);
          width.setCellStyle(observationCellStyle);
          heigth.setCellStyle(observationCellStyle);
          weight.setCellStyle(observationCellStyle);
          volume.setCellStyle(observationCellStyle);
          observations.setCellStyle(observationCellStyle);
        }

        indexRow++;
      }

      // Resume
      indexRow = indexRow + 2;
      Row dataRow = sheet.createRow(indexRow);
      dataRow.createCell(0).setCellValue("RESUMEN");

      indexRow++;
      dataRow = sheet.createRow(indexRow);
      dataRow.createCell(0).setCellValue("Total items procesados ");
      dataRow.createCell(1).setCellValue(resume.getTotalItems());

      indexRow++;
      dataRow = sheet.createRow(indexRow);
      dataRow.createCell(0).setCellValue("Total items asignados ");
      dataRow.createCell(1).setCellValue(resume.getTotalSuccessItems());

      indexRow++;
      dataRow = sheet.createRow(indexRow);
      dataRow.createCell(0).setCellValue("Total items observados ");
      dataRow.createCell(1).setCellValue(resume.getTotalErrorItems());

      indexRow++;
      dataRow = sheet.createRow(indexRow);
      dataRow.createCell(0).setCellValue("Volumen total a transportar ");
      dataRow.createCell(1).setCellValue(resume.getVolumeTotal());

      indexRow++;
      dataRow = sheet.createRow(indexRow);
      dataRow.createCell(0).setCellValue("Número de vehiculos a utilizar ");
      dataRow.createCell(1).setCellValue(resume.getAssignations().size());

      for (Map.Entry<String, Integer> entry : resume.getFrequencyVehicles().entrySet()) {
        indexRow++;
        dataRow = sheet.createRow(indexRow);
        dataRow.createCell(0).setCellValue("  " + entry.getKey());
        dataRow.createCell(1).setCellValue(entry.getValue());
      }
      workbook.write(out);
      return new ByteArrayInputStream(out.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<String> getReportHeaders() {
    List<String> reportHeaders = new ArrayList<>();
    reportHeaders.add("Orden");
    reportHeaders.add("Descripcion");
    reportHeaders.add("Longitud (m)");
    reportHeaders.add("Ancho (m)");
    reportHeaders.add("Alto (m)");
    reportHeaders.add("Peso (Kg)");
    reportHeaders.add("Volumen Item (m3)");
    reportHeaders.add("Tipo de Vehiculo");
    reportHeaders.add("Dimensiones Vehiculo");
    reportHeaders.add("Volumen Max Vehiculo (m3)");
    reportHeaders.add("Observaciones");
    return reportHeaders;
  }
}
