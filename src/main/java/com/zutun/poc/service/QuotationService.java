package com.zutun.poc.service;

import com.zutun.poc.model.Item;
import com.zutun.poc.model.Resume;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface QuotationService {
    List<Item> processFile(MultipartFile file, Resume resume);
    List<com.zutun.poc.model.v2.Item> processDynamicXls(MultipartFile file, com.zutun.poc.model.v2.Resume resume);
}
