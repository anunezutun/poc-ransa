package com.zutun.poc.service;

import com.zutun.poc.model.Item;
import com.zutun.poc.model.Resume;
import com.zutun.poc.model.v2.ResponseDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface QuotationService {
    List<Item> processFile(MultipartFile file, Resume resume);
    ResponseDto processDynamicXls(MultipartFile file);
}
