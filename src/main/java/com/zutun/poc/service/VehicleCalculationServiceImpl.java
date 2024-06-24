package com.zutun.poc.service;

import com.google.gson.Gson;
import com.zutun.poc.model.v2.Item;
import com.zutun.poc.model.v2.Request;
import com.zutun.poc.model.v2.Restriction;
import com.zutun.poc.model.v2.UnitMeasurement;
import com.zutun.poc.model.v2.Vehicle;
import com.zutun.poc.util.Constants;
import com.zutun.poc.util.Excel;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleCalculationServiceImpl implements VehicleCalculationService {

    @Override
    public void calculate(MultipartFile file) {
        Request request = getRequest(file);
        unitConversion(request);
        chooseVehicle(request);
    }

    private void unitConversion(Request request) {//convierte a la unidad estandar metro y tonelada

        for (Item item : request.getItems()) {
            var unitMeasurementInput = request.getRestriction().getUnitMeasurementInput();
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
        System.out.println(new Gson().toJson(request));
    }

    private Request getRequest(MultipartFile file) {
        try {
            var request = new Request();
            var restriction = new Restriction();
            request.setRestriction(restriction);
            Excel excel = new Excel(file);
            setItems(excel, request);
            setInformationVehicles(excel, restriction);
            setUnitMeasurement(excel, restriction);
            System.out.println(new Gson().toJson(request));
            return request;
        } catch (IOException e) {
            return null;
        }
    }

    private void setItems(Excel excel, Request request) {
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
        request.setItems(items);
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

    public void chooseVehicle(Request request) {
        List<Item> items = request.getItems();
        for (Item item : items) {
            List<Vehicle> availableVehicle = getAvailableVehicle(request, item);
            if (availableVehicle.isEmpty()) {
                item.setObservations(Arrays.asList("El item excede las dimensiones maximas"));
            } else {
                var vehicle = availableVehicle.get(0);
                item.setVehicle(vehicle);
            }
        }
        System.out.println(new Gson().toJson(request.getItems()));
    }

    private static List<Vehicle> getAvailableVehicle(Request request, Item item) {
        var vehicles = request.getRestriction().getVehicles();
        List<Vehicle> availableVehicle = new ArrayList<>();
        vehicles.forEach(vehicle -> {
            if (vehicle.getMaxDepth().compareTo(item.getDepth()) >= 0
                    && vehicle.getMaxWidth().compareTo(item.getWidth()) >= 0
                    && vehicle.getMaxHeight().compareTo(item.getHeight()) >= 0
                    && vehicle.getMaxWeight().compareTo(item.getWeight()) >= 0) {
                availableVehicle.add(vehicle);
            }
        });
        return availableVehicle;
    }
}
