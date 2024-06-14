package com.zutun.poc.rest;

import com.zutun.poc.model.Item;
import com.zutun.poc.service.CotizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cotization")
@RequiredArgsConstructor
public class CotizationController {

    private final CotizationService cotizationService;

    @PostMapping
    public List<Item> calculateCotization(@RequestBody List<Item> items) {
        return cotizationService.chooseVehicle(items);
    }
}
