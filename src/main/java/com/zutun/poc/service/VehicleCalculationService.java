package com.zutun.poc.service;

import org.springframework.web.multipart.MultipartFile;

public interface VehicleCalculationService {
    void calculate(MultipartFile file);
}
