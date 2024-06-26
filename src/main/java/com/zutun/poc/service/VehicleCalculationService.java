package com.zutun.poc.service;

import com.zutun.poc.model.v2.Item;
import com.zutun.poc.model.v2.Resume;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface VehicleCalculationService {
    List<Item> calculate(MultipartFile file, Resume resume);
}
