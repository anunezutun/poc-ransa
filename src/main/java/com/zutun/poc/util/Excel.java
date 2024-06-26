package com.zutun.poc.util;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

public class Excel {

  private Workbook excelWorkbook;

  public Excel(MultipartFile file) throws IOException {
    InputStream inputStream = new ByteArrayInputStream(file.getBytes());
    excelWorkbook = new XSSFWorkbook(inputStream);
  }

  public List<Map<Integer, String>> getValues() {
    List<Map<Integer, String>> rows = new ArrayList<>();
    Sheet sheet;
    sheet = excelWorkbook.getSheetAt(0);

    int indexRow = 0;
    for (Row row : sheet) {
      if (indexRow > 0) {
        Map<Integer, String> product = new HashMap<>();
        int indexCell = 0;
        int numCells = row.getLastCellNum();
        int numNullCells = 0;
        for (int i = 0; i < numCells; i++) {
          Cell cell = row.getCell(i, RETURN_BLANK_AS_NULL);
          if (cell == null) {
            product.put(indexCell, "");
            numNullCells++;
          } else {
            product.put(indexCell, getExcelField(cell));
          }
          indexCell++;
        }
        if (numNullCells < numCells) {
          rows.add(product);
        }
      }
      indexRow++;
    }
    return rows;
  }

  public List<Map<Integer, String>> getCellBlock(Integer indexTab,
                                                 Integer firstRow,
                                                 Integer lastColumn) {
    List<Map<Integer, String>> rows = new ArrayList<>();
    Sheet sheet = excelWorkbook.getSheetAt(indexTab);

    int indexRow = 0;
    for (Row row : sheet) {
      if (indexRow >= firstRow) {
        Map<Integer, String> rowData = new HashMap<>();
        int indexColumn = 0;
        for (int i = 0; i < lastColumn; i++) {
          Cell cell = row.getCell(i, RETURN_BLANK_AS_NULL);
          if (cell == null) {
            rowData.put(indexColumn, "");
          } else {
            rowData.put(indexColumn, getExcelField(cell));
          }
          indexColumn++;
        }
        rows.add(rowData);
      }
      indexRow++;
    }
    return rows;
  }

  public Object getCellValue(Integer indexTab, Integer rowIndex, Integer colIndex) {
    Sheet sheet = excelWorkbook.getSheetAt(indexTab);
    Row row = sheet.getRow(rowIndex);
    Object unitMeasurement = null;
    if (row != null) {
      Cell cell = row.getCell(colIndex, RETURN_BLANK_AS_NULL);
      if (Objects.nonNull(cell)) {
        unitMeasurement = getExcelField(cell);
      }
    }
    return unitMeasurement;
  }

  private String getExcelField(Cell cell) {
    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        return getNumericCell(cell);
      default:
        return "";
    }
  }

  private String getNumericCell(Cell cell) {
    double value = cell.getNumericCellValue();
    long intValue = (long) value;
    String numeric;
    if (value == intValue) {
      numeric = String.format("%d", intValue);
    } else {
      numeric = String.format(Locale.US, "%.2f", value);
    }
    return numeric;
  }
}
