package com.zutun.poc.service;

import com.zutun.poc.model.Item;

import java.io.File;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface CotizationService {
    public List<Item> chooseVehicle(List<Item> items);
    File createQuotation(MultipartFile file);
    public Item chooseVehicle(Item item, List<Item> items);
}
