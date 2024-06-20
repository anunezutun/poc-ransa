package com.zutun.poc.rest;

import com.zutun.poc.model.Item;
import com.zutun.poc.model.Resume;
import com.zutun.poc.service.CotizationService;
import java.io.IOException;
import java.util.List;

import com.zutun.poc.service.QuotationService;
import com.zutun.poc.util.ExcelFileExporter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/quotation")
@RequiredArgsConstructor
@CrossOrigin
public class QuotationController {

  private final CotizationService cotizationService;
  private final QuotationService quotationService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ByteArrayResource> createQuotation(
      @RequestPart(value = "quotation") MultipartFile quotation) throws IOException {
    ByteArrayResource fileResponse =
        new ByteArrayResource(
            FileUtils.readFileToByteArray(cotizationService.createQuotation(quotation)));
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=Cotizacion.csv")
        .header("Content-type", "application/octet-stream")
        .contentLength(fileResponse.contentLength())
        .body(fileResponse);
  }

  @PostMapping("/load")
  public ResponseEntity<InputStreamResource> bulkLoad(
          @RequestPart(value = "file") MultipartFile file) {
    List<Item> items = null;
    Resume resume = new Resume();
    if (file != null) {
      items = quotationService.processFile(file, resume);
    }

    HttpHeaders headers = new HttpHeaders();
    var name = file.getName();
    String reportFileName = "resultado".concat(".xlsx");
    headers.add("Content-Disposition", "attachment; filename=" + reportFileName);

    return ResponseEntity
            .ok()
            .headers(headers)
            .body(new InputStreamResource(ExcelFileExporter.loadFile(items, resume)));
  }
}
