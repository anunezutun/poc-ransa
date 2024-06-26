package com.zutun.poc.service;

import com.google.gson.Gson;
import com.zutun.poc.model.v2.Assignation;
import com.zutun.poc.model.v2.Item;
import com.zutun.poc.model.v2.RequestDto;
import com.zutun.poc.model.v2.ResponseDto;
import com.zutun.poc.model.v2.Restriction;
import com.zutun.poc.model.v2.Resume;
import com.zutun.poc.model.v2.FixingItem;
import com.zutun.poc.model.v2.UnitMeasurement;
import com.zutun.poc.model.v2.Vehicle;
import com.zutun.poc.util.Constants;
import com.zutun.poc.util.Excel;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
public class VehicleCalculationServiceImpl implements VehicleCalculationService {

    @Override
    public ResponseDto calculate(MultipartFile file, Boolean optimized) {
        RequestDto requestDto = getRequest(file);
        FixingItem fixingItem = sizingBuilder(requestDto);
        unitConversion(fixingItem);
        assignVehicle(fixingItem);
        var resume = new Resume();
        setResume(fixingItem.getItems(), resume, optimized);//queda calcular las dimensiones de salida
        System.out.println(new Gson().toJson(fixingItem));

        var responseDto = new ResponseDto();
        responseDto.setResume(resume);
        responseDto.setRequestDto(requestDto);
        responseDto.setFixingItem(fixingItem);
        return responseDto;
    }

    private FixingItem sizingBuilder(RequestDto requestDto) {
        var fixingItem = new FixingItem();
        fixingItem.setItems(requestDto.getItems().stream().map(item ->
                        Item.builder()
                                .order(item.getOrder())
                                .description(item.getDescription())
                                .depth(item.getDepth())
                                .width(item.getWidth())
                                .height(item.getHeight())
                                .weight(item.getWeight())
                                .build())
                .collect(Collectors.toList()));
        var restriction = new Restriction();
        restriction.setVehicles(requestDto.getRestriction().getVehicles()
                .stream()
                .map(vehicle -> Vehicle.builder()
                        .name(vehicle.getName())
                        .configuration(vehicle.getConfiguration())
                        .maxDepth(vehicle.getMaxDepth())
                        .maxWidth(vehicle.getMaxWidth())
                        .maxHeight(vehicle.getMaxHeight())
                        .maxWeight(vehicle.getMaxWeight())
                        .maxItems(vehicle.getMaxItems())
                        .priority(vehicle.getPriority())
                        .maxUnitsAvailable(vehicle.getMaxUnitsAvailable())
                        .build())
                .collect(Collectors.toList()));
        var unitMeasurementInput = requestDto.getRestriction().getUnitMeasurementInput();
        var unitMeasurementOutput = requestDto.getRestriction().getUnitMeasurementOutput();
        restriction.setUnitMeasurementInput(UnitMeasurement.builder()
                        .dimension(unitMeasurementInput.getDimension())
                        .weight(unitMeasurementInput.getWeight())
                        .decimals(unitMeasurementInput.getDecimals())
                .build());
        restriction.setUnitMeasurementOutput(UnitMeasurement.builder()
                        .dimension(unitMeasurementOutput.getDimension())
                        .weight(unitMeasurementOutput.getWeight())
                        .decimals(unitMeasurementOutput.getDecimals())
                .build());
        fixingItem.setRestriction(restriction);
        return fixingItem;
    }

    private void setResume(List<Item> items, Resume resume, Boolean optimized) {
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
        resume.setTotalWeight(calculateTotalWeight(items));
        if (Boolean.FALSE.equals(optimized)) {
            countVehicles(items, resume);
        } else {
            countVehiclesOptimized(items, resume);
        }
        calculateQuantityVehicles(resume);
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

    private BigDecimal calculateTotalWeight(List<Item> items) {

        var totalWeight = items.stream()
                .map(item -> item.getWeight())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalWeight;
    }

    private void unitConversion(FixingItem fixingItem) {//convierte a la unidad estandar metro y tonelada

        for (Item item : fixingItem.getItems()) {
            var unitMeasurementInput = fixingItem.getRestriction().getUnitMeasurementInput();
            var dimensionUnit = unitMeasurementInput.getDimension();
            var weightUnit = unitMeasurementInput.getWeight();
            var decimals = unitMeasurementInput.getDecimals();

            item.setDepth(DimensionConverter
                    .convert(item.getDepth(), dimensionUnit, "metro", decimals));
            item.setWidth(DimensionConverter
                    .convert(item.getWidth(), dimensionUnit, "metro", decimals));
            item.setHeight(DimensionConverter
                    .convert(item.getHeight(), dimensionUnit, "metro", decimals));
            item.setWeight(WeightConverter
                    .convert(item.getWeight(), weightUnit, "tonelada", decimals));
        }
    }

    private RequestDto getRequest(MultipartFile file) {
        try {
            var request = new RequestDto();
            var restriction = new Restriction();
            request.setRestriction(restriction);
            Excel excel = new Excel(file);
            setItems(excel, request);
            setInformationVehicles(excel, restriction);
            setUnitMeasurement(excel, restriction);
            return request;
        } catch (IOException e) {
            return null;
        }
    }

    private void setItems(Excel excel, RequestDto requestDto) {
        List<Map<Integer, String>> rows = excel.getCellBlock(Constants.SIZING_TAB,
                Constants.FIRST_ROW_INDEX,
                Constants.LAST_COLUMN_VEHICLE_SIZING);
        List<Item> items = new ArrayList<>();
        for (Map<Integer, String> row : rows) {
            Item item = Item.builder()
                    .order(row.get(0))
                    .description(row.get(1))
                    .depth(new BigDecimal(row.get(2)))
                    .width(new BigDecimal(row.get(3)))
                    .height(new BigDecimal(row.get(4)))
                    .weight(new BigDecimal(row.get(5)))
                    .build();

            items.add(item);
        }
        requestDto.setItems(items);
    }

    private void setUnitMeasurement(Excel excel, Restriction restriction) {
        restriction.setUnitMeasurementInput(UnitMeasurement.builder()
                .dimension(excel.getCellValue(Constants.SIZING_TAB, 2, 8).toString())
                .weight(excel.getCellValue(Constants.SIZING_TAB, 3, 8).toString())
                .decimals(Integer.parseInt(
                        excel.getCellValue(Constants.SIZING_TAB, 4, 8).toString()))
                .build());
        restriction.setUnitMeasurementOutput(UnitMeasurement.builder()
                .dimension(excel.getCellValue(Constants.SIZING_TAB, 6, 8).toString())
                .weight(excel.getCellValue(Constants.SIZING_TAB, 7, 8).toString())
                .decimals(Integer.parseInt(
                        excel.getCellValue(Constants.SIZING_TAB, 8, 8).toString()))
                .build());
    }

    private void setInformationVehicles(Excel excel, Restriction restriction) {
        List<Map<Integer, String>> rows = excel.getCellBlock(Constants.RESTRICTION_TAB,
                Constants.FIRST_ROW_INDEX,
                Constants.LAST_COLUMN_VEHICLE_RESTRICTION);
        List<Vehicle> vehicles = new ArrayList<>();
        for (Map<Integer, String> row : rows) {
            vehicles.add(Vehicle.builder()
                    .name(row.get(0))
                    .configuration(row.get(1))
                    .maxDepth(new BigDecimal(row.get(2)))
                    .maxWidth(new BigDecimal(row.get(3)))
                    .maxHeight(new BigDecimal(row.get(4)))
                    .maxWeight(new BigDecimal(row.get(5)))
                    .maxItems(Integer.parseInt(row.get(6)))
                    .priority(Integer.parseInt(row.get(7)))
                    .maxUnitsAvailable(Integer.parseInt(row.get(8)))
                    .build());
        }
        restriction.setVehicles(vehicles);
    }

    private void assignVehicle(FixingItem fixingItem) {

        List<Item> items = fixingItem.getItems();
        for (Item item : items) {
            var availableVehicles = getAvailableVehicles(fixingItem);
            List<Vehicle> candidates = new ArrayList<>();
            availableVehicles.forEach(vehicle -> {
                if (vehicle.getMaxDepth().compareTo(item.getDepth()) >= 0
                        && vehicle.getMaxWidth().compareTo(item.getWidth()) >= 0
                        && vehicle.getMaxHeight().compareTo(item.getHeight()) >= 0
                        && vehicle.getMaxWeight().compareTo(item.getWeight()) >= 0) {
                    candidates.add(vehicle);
                }
            });

            if (!candidates.isEmpty()) {
                //ordena segun prioridad
                Collections.sort(candidates, Comparator.comparingInt(Vehicle::getPriority));
                var vehicle = candidates.get(0);
                item.setVehicle(vehicle);
                //descuenta la cantidad de unidades vehiculares
                vehicle.setMaxUnitsAvailable(vehicle.getMaxUnitsAvailable() - 1);
            } else {
                item.setObservations(Arrays.asList("El item excede las dimensiones maximas"));
            }
        }
    }

    private void countVehicles(List<Item> items, Resume resume) {

        List<Assignation> assignationList = new ArrayList<>();
        List<List<Item>> result = groupVehicles(items);
        for (List<Item> groupItems : result) {
            if (groupItems.size() == 1) {
                var item = groupItems.get(0);
                Vehicle vehicle = item.getVehicle();
                addResume(assignationList, List.of(item), vehicle);
            } else {
                //para no apilables
                var vehicleDepth = groupItems.get(0).getVehicle().getMaxDepth();
                var vehicleWeight = groupItems.get(0).getVehicle().getMaxWeight();
                var vehicleMaxItems = groupItems.get(0).getVehicle().getMaxItems();
                var firstItem = groupItems.get(0);
                var initialDepth = firstItem.getDepth();
                var initialWeigth = firstItem.getWeight();
                var initialCountItems = 1;

                List<Item> joinItems = new ArrayList<>();
                joinItems.add(firstItem);
                var vehicle = firstItem.getVehicle();
                for (int i = 1; i < groupItems.size(); i++) {
                    var currentItem = groupItems.get(i);
                    var nextDepth = currentItem.getDepth();
                    var totalDepth = initialDepth.add(nextDepth);
                    var nextWeight = currentItem.getWeight();
                    var totalWeight = initialWeigth.add(nextWeight);
                    initialCountItems ++;
                    if (totalDepth.compareTo(vehicleDepth) < 0
                            && totalWeight.compareTo(vehicleWeight) < 0
                            && initialCountItems <= vehicleMaxItems) {
                        initialDepth = totalDepth;
                        initialWeigth = totalWeight;
                        joinItems.add(currentItem);
                    } else {
                        addResume(assignationList, joinItems, vehicle);
                        //reiniciar
                        joinItems = new ArrayList<>();
                        initialDepth = currentItem.getDepth();
                        initialWeigth = currentItem.getWeight();
                        initialCountItems = 1;
                        joinItems.add(currentItem);
                    }
                }
                addResume(assignationList, joinItems, vehicle);
            }
        }
        resume.setAssignations(assignationList);
        System.out.println(new Gson().toJson(resume));
    }

    private void countVehiclesOptimized(List<Item> items, Resume resume) {

        List<Assignation> assignationList = new ArrayList<>();
        List<List<Item>> result = groupVehicles(items);
        for (List<Item> groupItems : result) {
            if (groupItems.size() == 1) {
                var item = groupItems.get(0);
                Vehicle vehicle = item.getVehicle();
                addResume(assignationList, List.of(item), vehicle);
            } else {
                //para no apilables
                var vehicleDepth = groupItems.get(0).getVehicle().getMaxDepth();
                var vehicleWeight = groupItems.get(0).getVehicle().getMaxWeight();
                var vehicleMaxItems = groupItems.get(0).getVehicle().getMaxItems();
                var firstItem = groupItems.get(0);
                var initialDepth = firstItem.getWidth();
                var initialWeigth = firstItem.getWeight();
                var initialCountItems = 1;

                List<Item> joinItems = new ArrayList<>();
                joinItems.add(firstItem);
                var vehicle = firstItem.getVehicle();
                for (int i = 1; i < groupItems.size(); i++) {
                    var currentItem = groupItems.get(i);
                    var nextDepth = currentItem.getWidth();
                    var totalDepth = initialDepth.add(nextDepth);
                    var nextWeight = currentItem.getWeight();
                    var totalWeight = initialWeigth.add(nextWeight);
                    initialCountItems ++;
                    if (totalDepth.compareTo(vehicleDepth) < 0
                        && totalWeight.compareTo(vehicleWeight) < 0
                        && initialCountItems <= vehicleMaxItems) {
                        initialDepth = totalDepth;
                        initialWeigth = totalWeight;
                        joinItems.add(currentItem);
                    } else {
                        addResume(assignationList, joinItems, vehicle);
                        //reiniciar
                        joinItems = new ArrayList<>();
                        initialDepth = currentItem.getWidth();
                        initialWeigth = currentItem.getWeight();
                        initialCountItems = 1;
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
            return Integer.parseInt(
                    assignationList.get(assignationList.size() - 1).getVehicle().getId()) + 1;
        }
    }
    private List<List<Item>> groupVehicles(List<Item> items) {

        List<List<Item>> result = new ArrayList<>();
        List<Item> currentList = new ArrayList<>();

        var currentVehicle = items.get(0).getVehicle();
        var currentVehicleFullName = currentVehicle.getVehicleFullName();
        currentList.add(items.get(0));

        for (int i = 1; i < items.size(); i++) {
            var item = items.get(i);
            var vehicle = item.getVehicle();
            if (currentVehicle.getMaxItems() > 1
                    && vehicle.getVehicleFullName().equals(currentVehicleFullName)) {
                currentList.add(item);
            } else {
                result.add(currentList);
                currentList = new ArrayList<>();
                currentVehicle = vehicle;
                currentVehicleFullName = vehicle.getVehicleFullName();
                currentList.add(item);
            }
        }

        result.add(currentList);

        return result;
    }

    private List<Vehicle> getAvailableVehicles(FixingItem fixingItem) {
        return fixingItem.getRestriction().getVehicles()
                .stream()
                .filter(vehicle -> vehicle.getMaxUnitsAvailable() > 0)
                .collect(Collectors.toList());
    }
}
