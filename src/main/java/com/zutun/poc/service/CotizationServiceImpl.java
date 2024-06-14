package com.zutun.poc.service;

import com.zutun.poc.model.Item;
import com.zutun.poc.model.VehicleType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CotizationServiceImpl implements CotizationService {


    @Override
    public List<Item> chooseVehicle(List<Item> items) {

        for (Item item : items) {
            //evaluar longitud
            var vehicles = getVehicles();
            vehicles = vehicles
                    .stream()
                    .filter(vehicleType -> vehicleType.getMaxDepth() >= item.getDepth())
                    .collect(Collectors.toList());
            vehicles = vehicles
                    .stream()
                    .filter(vehicleType -> vehicleType.getMaxWidth() >= item.getWidth())
                    .collect(Collectors.toList());
            vehicles = vehicles
                    .stream()
                    .filter(vehicleType -> vehicleType.getMaxHeight() >= item.getHeight())
                    .collect(Collectors.toList());
            vehicles = vehicles
                    .stream()
                    .filter(vehicleType -> vehicleType.getMaxWeight() >= item.getWeight())
                    .collect(Collectors.toList());
            item.setObservations(Arrays.asList("total: " + vehicles.size(), vehicles.toString()));
        }
        return items;
    }

    private List<VehicleType> getVehicles() {
        List<VehicleType> vehicles = new ArrayList<>();
        vehicles.add(VehicleType.builder()
                .name("Plataforma")
                .configuration("T3S3")
                .maxDepth(12.5)
                .maxWidth(2.6)
                .maxHeight(2.7)
                .maxWeight(30.0)
                .build());
        vehicles.add(VehicleType.builder()
                .name("Cama Baja")
                .configuration("T3S2")
                .maxDepth(9.0)
                .maxWidth(3.0)
                .maxHeight(4.0)
                .maxWeight(25.0)
                .build());
        vehicles.add(VehicleType.builder()
                .name("Cama Baja")
                .configuration("T3S3")
                .maxDepth(9.0)
                .maxWidth(3.0)
                .maxHeight(4.0)
                .maxWeight(30.0)
                .build());
        vehicles.add(VehicleType.builder()
                .name("Cama Baja")
                .configuration("T3S4")
                .maxDepth(10.0)
                .maxWidth(4.0)
                .maxHeight(4.0)
                .maxWeight(32.0)
                .build());
        vehicles.add(VehicleType.builder()
                .name("Cama cuna")
                .configuration("T3S3")
                .maxDepth(7.0)
                .maxWidth(4.0)
                .maxHeight(4.2)
                .maxWeight(30.0)
                .build());
        vehicles.add(VehicleType.builder()
                .name("Cama cuna")
                .configuration("T3S4")
                .maxDepth(7.0)
                .maxWidth(4.0)
                .maxHeight(4.2)
                .maxWeight(32.0)
                .build());
        vehicles.add(VehicleType.builder()
                .name("Plataforma Extensible")
                .configuration("T3S3")
                .maxDepth(20.0)
                .maxWidth(2.6)
                .maxHeight(2.5)
                .maxWeight(20.0)
                .build());
        vehicles.add(VehicleType.builder()
                .name("Cama Baja Extensible")
                .configuration("T3S2")
                .maxDepth(20.0)
                .maxWidth(4.0)
                .maxHeight(2.5)
                .maxWeight(25.0)
                .build());
        return vehicles;
    }
}
