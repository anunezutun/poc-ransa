package com.zutun.poc.rest;

import com.zutun.poc.model.Item;
import com.zutun.poc.model.Resume;
import com.zutun.poc.service.SizingService;
import java.util.List;

import com.zutun.poc.model.v2.ResponseDto;
import com.zutun.poc.service.QuotationService;
import com.zutun.poc.util.ExcelFileExporter;

import com.zutun.poc.util.ExcelFileExporterV2;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
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

  private final QuotationService quotationService;
  private final SizingService sizingService;

  @PostMapping("/load")
  public ResponseEntity<InputStreamResource> bulkLoad(
          @RequestPart(value = "file") MultipartFile file) {
    List<Item> items = null;
    List<Item> itemsOptimized = null;
    Resume resume = new Resume();
    Resume resumeOptimized = new Resume();
    if (file != null) {
      items = quotationService.processFile(file, resume);
      itemsOptimized = sizingService.processFile(file, resumeOptimized);
    }

    HttpHeaders headers = new HttpHeaders();
    var filename = "RESULTADO-" + file.getOriginalFilename();
    headers.add("Content-Disposition", "attachment; filename=" + filename);

    return ResponseEntity
            .ok()
            .headers(headers)
            .body(new InputStreamResource(ExcelFileExporter.loadFile(items, resume, itemsOptimized, resumeOptimized)));
  }

  @PostMapping("v2/load")
  public ResponseEntity<InputStreamResource> loadDynamicXls(
          @RequestPart(value = "file") MultipartFile file) {
    ResponseDto responseDto = new ResponseDto();
    if (file != null) {
      responseDto = quotationService.processDynamicXls(file);
    }
    HttpHeaders headers = new HttpHeaders();
    var filename = "RESULTADO-" + file.getOriginalFilename();
    headers.add("Content-Disposition", "attachment; filename=" + filename);

    return ResponseEntity
            .ok()
            .headers(headers)
            .body(new InputStreamResource(ExcelFileExporterV2.loadFile(responseDto.getFixingItem().getItems(), responseDto.getResume())));
  }

}
