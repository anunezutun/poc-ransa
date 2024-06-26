package com.zutun.poc.service;

import com.google.gson.Gson;
import com.zutun.poc.model.Assignation;
import com.zutun.poc.model.Item;
import com.zutun.poc.model.Resume;
import com.zutun.poc.model.Vehicle;
import com.zutun.poc.model.v2.ResponseDto;
import com.zutun.poc.util.Excel;
import com.zutun.poc.util.GeneratorId;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotationServiceImpl implements QuotationService {

    private final VehicleCalculationService vehicleCalculationService;

    @Override
    public List<Item> processFile(MultipartFile file, Resume resume) {
        List<Item> items;
        try {
            Excel excel = new Excel(file);
            List<Map<Integer, String>> values = excel.getValues();
            items = getItems(values);
            chooseVehicle(items);
            setResume(items, resume);
            System.out.println(new Gson().toJson(items));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return items;
    }

    @Override
    public ResponseDto processDynamicXls(MultipartFile file) {
        return vehicleCalculationService.calculate(file);
    }

    private void setResume(List<Item> items, Resume resume) {
        resume.setTotalItems(items.size());
        var errorItems = items.stream()
                .filter(item -> Objects.isNull(item.getVehicle()))
                .collect(Collectors.toList()).size();
        items = items.stream()
                .filter(item -> Objects.nonNull(item.getVehicle()))
                .collect(Collectors.toList());
        var totalAssignations = items.size();
        resume.setTotalErrorItems(errorItems);
        resume.setTotalSuccessItems(totalAssignations);
        resume.setTotalVolume(calculateTotalVolume(items));
        resume.setTotalWeight(calculateTotalWeight(items));
        try {
            calculateVehicles(items, resume);
            calculateQuantityVehicles(resume);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    private void calculateQuantityVehicles(Resume resume) {

        List<String> vehicles = resume.getAssignations().stream()
                .map(assignation -> assignation.getVehicle())
                .map(vehicle -> vehicle.getVehicleFullName())
                .collect(Collectors.toList());
        Map<String, Integer> frequencyVehicles = new HashMap<>();
        for (Assignation assignation : resume.getAssignations()) {
            var vehicleName = assignation.getVehicle().getVehicleFullName();
            var frequency = Collections.frequency(vehicles, vehicleName);
            if (Objects.isNull(frequencyVehicles.get(vehicleName))) {
                frequencyVehicles.put(vehicleName, frequency);
            }
        }
        resume.setFrequencyVehicles(frequencyVehicles);
    }

    private Double calculateTotalVolume(List<Item> items) {
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.UP);
        var totalVolume = items.stream()
                .mapToDouble(item -> item.getVolume())
                .sum();
        return Double.parseDouble(df.format(totalVolume));
    }

    private Double calculateTotalWeight(List<Item> items) {
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.UP);
        var totalWeight = items.stream()
                .mapToDouble(item -> item.getWeight())
                .sum()/1000;
        return Double.parseDouble(df.format(totalWeight));
    }

    private void calculateVehicles(List<Item> items, Resume resume) throws CloneNotSupportedException {

        List<Assignation> assignationList = new ArrayList<>();
        List<List<Item>> result = groupVehicles(items);
        for (List<Item> groupItems : result) {
            if (groupItems.size() == 1) {
                var item = groupItems.get(0);
                Vehicle vehicle = item.getVehicle();
                addResume(assignationList, Arrays.asList(item), vehicle);
            } else {
                //para no apilables
                var vehicleDepth = groupItems.get(0).getVehicle().getMaxDepth();
                var firstItem = groupItems.get(0);
                var initialDepth = firstItem.getDepth();
                List<Item> joinItems = new ArrayList<>();
                joinItems.add(firstItem);
                var vehicle = firstItem.getVehicle();
                for (int i = 1; i < groupItems.size(); i++) {
                    var currentItem = groupItems.get(i);
                    var nextDepth = currentItem.getDepth();
                    var totalDepth = initialDepth + nextDepth;
                    if (totalDepth < vehicleDepth) {
                        initialDepth = totalDepth;
                        joinItems.add(currentItem);
                    } else {
                        addResume(assignationList, joinItems, vehicle);
                        //reiniciar
                        joinItems = new ArrayList<>();
                        initialDepth = currentItem.getDepth();
                        joinItems.add(currentItem);
                    }
                }
                addResume(assignationList, joinItems, vehicle);
            }
        }
        resume.setAssignations(assignationList);
        System.out.println(new Gson().toJson(resume));
    }

    private void addResume(List<Assignation> assignationList, List<Item> items, Vehicle vehicle) {
        Vehicle newVehicle;
        try {
            newVehicle = (Vehicle) vehicle.clone();
            newVehicle.setId(generateId(assignationList).toString());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        Assignation assignation = new Assignation();
        assignation.setVehicle(newVehicle);
        assignation.setItems(items);
        assignationList.add(assignation);

        items.forEach(item -> item.setVehicle(newVehicle));
    }

    private Integer generateId(List<Assignation> assignationList) {
        if (assignationList.isEmpty()) {
            return 1;
        } else {
            return Integer.parseInt(assignationList.get(assignationList.size() - 1).getVehicle().getId()) + 1;
        }
    }

    private List<List<Item>> groupVehicles(List<Item> items) {

        List<List<Item>> result = new ArrayList<>();
        List<Item> currentList = new ArrayList<>();

        String currentVehicle = items.get(0).getVehicle().getVehicleFullName();
        currentList.add(items.get(0));

        for (int i = 1; i < items.size(); i++) {
            if (items.get(i).getVehicle().getVehicleFullName().equals(currentVehicle)) {
                currentList.add(items.get(i));
            } else {
                result.add(currentList);
                currentList = new ArrayList<>();
                currentVehicle = items.get(i).getVehicle().getVehicleFullName();
                currentList.add(items.get(i));
            }
        }

        result.add(currentList);

        return result;
    }

    private String generateId() {
        return GeneratorId.generateId();
    }

    private List<Item> getItems(List<Map<Integer, String>> rows) {
        List<Item> itemList = new ArrayList<>();
        for (Map<Integer, String> row : rows) {
            Item item = Item.builder()
                    .order(row.get(0))
                    .description(row.get(1))
                    .depth(Double.parseDouble(row.get(2)))
                    .width(Double.parseDouble(row.get(3)))
                    .height(Double.parseDouble(row.get(4)))
                    .weight(Double.parseDouble(row.get(5)))
                    //.stackable(row.get(6))
                    .build();

            itemList.add(item);
        }
        return itemList;
    }

    public void chooseVehicle(List<Item> items) {
        for (Item item : items) {
            var vehicles = getVehicles();
            List<Vehicle> availableVehicle = new ArrayList<>();
            vehicles.forEach(vehicle -> {
                if (vehicle.getMaxDepth() >= item.getDepth()
                        && vehicle.getMaxWidth() >= item.getWidth()
                        && vehicle.getMaxHeight() >= item.getHeight()
                        && vehicle.getMaxWeight() >= item.getWeight()/1000) {
                    availableVehicle.add(vehicle);
                }
            });
            if (availableVehicle.isEmpty()) {
                item.setObservations(Arrays.asList("El item excede las dimensiones maximas"));
            } else {
                var vehicle = availableVehicle.get(0);
                item.setVehicle(vehicle);
            }
        }
    }

    private List<Vehicle> getVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(Vehicle.builder()
                .name("Plataforma")
                .configuration("T3S3")
                .maxDepth(12.5)
                .maxWidth(2.6)
                .maxHeight(2.7)
                .maxWeight(30.0)
                .maxVolume(87.75)
                .build());
        vehicles.add(Vehicle.builder()
                .name("Cama Baja")
                .configuration("T3S2")
                .maxDepth(9.0)
                .maxWidth(3.0)
                .maxHeight(4.0)
                .maxWeight(25.0)
                .maxVolume(108.0)
                .build());
        vehicles.add(Vehicle.builder()
                .name("Cama Baja")
                .configuration("T3S3")
                .maxDepth(9.0)
                .maxWidth(3.0)
                .maxHeight(4.0)
                .maxWeight(30.0)
                .maxVolume(108.0)
                .build());
        vehicles.add(Vehicle.builder()
                .name("Cama Baja")
                .configuration("T3S4")
                .maxDepth(10.0)
                .maxWidth(4.0)
                .maxHeight(4.0)
                .maxWeight(32.0)
                .maxVolume(160.0)
                .build());
        vehicles.add(Vehicle.builder()
                .name("Cama cuna")
                .configuration("T3S3")
                .maxDepth(7.0)
                .maxWidth(4.0)
                .maxHeight(4.2)
                .maxWeight(30.0)
                .maxVolume(117.6)
                .build());
        vehicles.add(Vehicle.builder()
                .name("Cama cuna")
                .configuration("T3S4")
                .maxDepth(7.0)
                .maxWidth(4.0)
                .maxHeight(4.2)
                .maxWeight(32.0)
                .maxVolume(117.6)
                .build());
        vehicles.add(Vehicle.builder()
                .name("Plataforma Extensible")
                .configuration("T3S3")
                .maxDepth(20.0)
                .maxWidth(2.6)
                .maxHeight(2.5)
                .maxWeight(20.0)
                .maxVolume(130.0)
                .build());
        vehicles.add(Vehicle.builder()
                .name("Cama Baja Extensible")
                .configuration("T3S2")
                .maxDepth(20.0)
                .maxWidth(4.0)
                .maxHeight(2.5)
                .maxWeight(25.0)
                .maxVolume(200.0)
                .build());
        return vehicles;
    }
}
