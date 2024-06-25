package com.zutun.poc.service;

import com.zutun.poc.model.Item;
import com.zutun.poc.model.Resume;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface SizingService {
  List<Item> processFile(MultipartFile file, Resume resume);
}
