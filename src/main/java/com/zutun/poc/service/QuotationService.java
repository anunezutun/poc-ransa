package com.zutun.poc.service;

import com.zutun.poc.model.Item;
import com.zutun.poc.model.Resume;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuotationService {
    List<Item> processFile(MultipartFile file, Resume resume);
}
