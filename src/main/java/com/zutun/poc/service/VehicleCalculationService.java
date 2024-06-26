package com.zutun.poc.service;

import com.zutun.poc.model.v2.ResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface VehicleCalculationService {
    ResponseDto calculate(MultipartFile file, Boolean optimized);
}
