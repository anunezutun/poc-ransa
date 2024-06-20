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
      numeric = String.format(Locale.US, "%.3f", value);
    }
    return numeric;
  }
}
