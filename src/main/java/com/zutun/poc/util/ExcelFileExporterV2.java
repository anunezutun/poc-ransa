package com.zutun.poc.util;

import com.zutun.poc.model.v2.Item;
import com.zutun.poc.model.v2.RequestDto;
import com.zutun.poc.model.v2.ResponseDto;
import com.zutun.poc.model.v2.Resume;
import com.zutun.poc.model.v2.Vehicle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExcelFileExporterV2 {

  public static final String REPORT_LOAD_SHEET_NAME = "DIMENSIONAMIENTO";
  public static final String SIZING_SHEET_OPTIMIZED_NAME = "DIMENSIONAMIENTO_OPTIMIZADO";
  public static final String RESTRICTIONS_SHEET_NAME = "RESTRICCIONES";
  public static final List<String> SIZING_HEADERS = getSizingHeaders();
  public static final List<String> RESTRICTION_HEADERS = getRestrictionHeaders();

  public static ByteArrayInputStream loadFile(ResponseDto responseDto, ResponseDto responseDtoOptimized) {
    var items = responseDto.getFixingItem().getItems();
    var resume = responseDto.getResume();
    var itemsOptimized = responseDtoOptimized.getFixingItem().getItems();
    var resumeOptimized = responseDtoOptimized.getResume();
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      Sheet sheet = workbook.createSheet(REPORT_LOAD_SHEET_NAME);
      Sheet sheetOptimized = workbook.createSheet(SIZING_SHEET_OPTIMIZED_NAME);

      writeSheet(workbook, sheet, items, resume);
      writeSheet(workbook, sheetOptimized, itemsOptimized, resumeOptimized);
      writeRestrictionSheet(workbook, responseDto.getRequestDto());

      workbook.write(out);
      return new ByteArrayInputStream(out.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<String> getSizingHeaders() {
    List<String> sizingHeaders = new ArrayList<>();
    sizingHeaders.add("Orden");
    sizingHeaders.add("Descripcion");
    sizingHeaders.add("Longitud (m)");
    sizingHeaders.add("Ancho (m)");
    sizingHeaders.add("Alto (m)");
    sizingHeaders.add("Peso (Tn)");
    sizingHeaders.add("Tipo de Vehiculo");
    sizingHeaders.add("Dimensiones Vehiculo");
    sizingHeaders.add("Observaciones");
    return sizingHeaders;
  }

  private static List<String> getRestrictionHeaders() {
    List<String> restrictionHeaders = new ArrayList<>();
    restrictionHeaders.add("Tipo de Unidad");
    restrictionHeaders.add("Configuracion");
    restrictionHeaders.add("Longitud (m)");
    restrictionHeaders.add("Ancho (m)");
    restrictionHeaders.add("Alto (m)");
    restrictionHeaders.add("Peso (Tn)");
    restrictionHeaders.add("Maximo de items");
    restrictionHeaders.add("Prioridad");
    restrictionHeaders.add("N° de vehiculos disponibles");
    return restrictionHeaders;
  }

  private static void writeRestrictionSheet(Workbook workbook, RequestDto requestDto) {
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setColor(IndexedColors.BLACK.getIndex());

    CellStyle headerCellStyle = workbook.createCellStyle();
    headerCellStyle.setFont(headerFont);

    Sheet sheet = workbook.createSheet(RESTRICTIONS_SHEET_NAME);
    Row headerRow = sheet.createRow(0);

    Cell cell;
    int indexHeader = 0;
    for (String header : RESTRICTION_HEADERS) {
      cell = headerRow.createCell(indexHeader);
      cell.setCellValue(header);
      cell.setCellStyle(headerCellStyle);
      indexHeader++;
    }

    int indexRow = 0;
    for (Vehicle vehicle : requestDto.getRestriction().getVehicles()) {
      int indexColumn = 0;

      Row dataRow = sheet.createRow(indexRow + 1);
      dataRow.createCell(indexColumn).setCellValue(vehicle.getName());
      dataRow.createCell(++indexColumn).setCellValue(vehicle.getConfiguration());
      dataRow.createCell(++indexColumn).setCellValue(vehicle.getMaxDepth().doubleValue());
      dataRow.createCell(++indexColumn).setCellValue(vehicle.getMaxWidth().doubleValue());
      dataRow.createCell(++indexColumn).setCellValue(vehicle.getMaxHeight().doubleValue());
      dataRow.createCell(++indexColumn).setCellValue(vehicle.getMaxWeight().doubleValue());
      dataRow.createCell(++indexColumn).setCellValue(vehicle.getMaxItems());
      dataRow.createCell(++indexColumn).setCellValue(vehicle.getPriority());
      dataRow.createCell(++indexColumn).setCellValue(vehicle.getMaxUnitsAvailable());

      indexRow++;
    }
  }

  private static void writeSheet(Workbook workbook, Sheet sheet, List<Item> items, Resume resume){

    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setColor(IndexedColors.BLACK.getIndex());

    CellStyle headerCellStyle = workbook.createCellStyle();
    headerCellStyle.setFont(headerFont);

    Row headerRow = sheet.createRow(0);

    Font observationFont = workbook.createFont();
    observationFont.setColor(IndexedColors.RED.getIndex());

    CellStyle observationCellStyle = workbook.createCellStyle();
    observationCellStyle.setFont(observationFont);

    Cell cell;
    int indexHeader = 0;
    for (String header : SIZING_HEADERS) {
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
      depth.setCellValue(item.getDepth().doubleValue());
      Cell width = dataRow.createCell(++indexColumn);
      width.setCellValue(item.getWidth().doubleValue());

      if (Boolean.TRUE.equals(item.getIsRotated())){
        depth.setCellValue(item.getWidth().doubleValue());
        width.setCellValue(item.getDepth().doubleValue());
      }

      Cell heigth = dataRow.createCell(++indexColumn);
      heigth.setCellValue(item.getHeight().doubleValue());
      Cell weight = dataRow.createCell(++indexColumn);
      weight.setCellValue(item.getWeight().doubleValue());
      dataRow
          .createCell(++indexColumn)
          .setCellValue(
              Objects.isNull(item.getVehicle()) ? "" : "#" + item.getVehicle().getVehicleInfo());
      dataRow
          .createCell(++indexColumn)
          .setCellValue(
              Objects.isNull(item.getVehicle()) ? "" : item.getVehicle().getDimensions());

      if (!Objects.isNull(item.getObservations()) || Boolean.TRUE.equals(item.getIsRotated())) {
        Cell observations = dataRow.createCell(++indexColumn);
        observations.setCellValue(
            Objects.isNull(item.getObservations()) ? "" : item.getObservations().get(0));

        order.setCellStyle(observationCellStyle);
        description.setCellStyle(observationCellStyle);
        depth.setCellStyle(observationCellStyle);
        width.setCellStyle(observationCellStyle);
        heigth.setCellStyle(observationCellStyle);
        weight.setCellStyle(observationCellStyle);
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
    dataRow.createCell(0).setCellValue("Peso total a transportar ");
    dataRow.createCell(1).setCellValue(resume.getTotalWeight() + " Tn.");

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

  }
}
