package com.zutun.poc.rest;

import com.zutun.poc.service.CotizationService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ByteArrayResource;
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

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ByteArrayResource> createQuotation(
      @RequestPart(value = "quotation") MultipartFile quotation) throws IOException {
    cotizationService.createQuotation(quotation);
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
}
