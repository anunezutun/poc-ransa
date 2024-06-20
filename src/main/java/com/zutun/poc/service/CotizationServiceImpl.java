package com.zutun.poc.service;

import com.google.gson.Gson;
import com.zutun.poc.model.Item;
import com.zutun.poc.model.Assignation;
import com.zutun.poc.model.Resume;
import com.zutun.poc.model.Vehicle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class CotizationServiceImpl implements CotizationService {

    private static final String QUOATION_FILE_NAME = "quotation.csv";
    private static final String HEADER_QUOTATION = "Orden,Descripcion,Profundidad,Ancho,Altura,Peso,Stackeable,Tipo de vehiculo,Observaciones\n";
    private static final int ORDER_POSITION = 0;
    private static final int DESCRIPTION_POSITION = 1;
    private static final int DEPTH_POSITION = 2;
    private static final int WIDTH_POSITION = 3;
    private static final int HEIGTH_POSITION = 4;
    private static final int WEIGTH_POSITION = 5;
    private static final int STACKEABLE_POSITION = 6;
    private static final String DELIMIT = "|";

    @Override
    public List<Item> chooseVehicle(List<Item> items) {

        for (Item item : items) {
            //evaluar longitud
            var vehicles = getVehicles();
            List<String> observations = new ArrayList<>();
            vehicles = vehicles
                    .stream()
                    .filter(vehicle -> vehicle.getMaxDepth() >= item.getDepth())
                    .collect(Collectors.toList());
            if (vehicles.isEmpty()) {
                observations.add("La longitud sobrepasa el máximo permitido.");
            }
            vehicles = vehicles
                    .stream()
                    .filter(vehicle -> vehicle.getMaxWidth() >= item.getWidth())
                    .collect(Collectors.toList());
            if (vehicles.isEmpty()) {
                observations.add("El ancho sobrepasa el máximo permitido.");
            }
            vehicles = vehicles
                    .stream()
                    .filter(vehicle -> vehicle.getMaxHeight() >= item.getHeight())
                    .collect(Collectors.toList());
            if (vehicles.isEmpty()) {
                observations.add("El alto sobrepasa el máximo permitido.");
            }
            vehicles = vehicles
                    .stream()
                    .filter(vehicle -> vehicle.getMaxWeight() >= item.getWeight()/1000)
                    .collect(Collectors.toList());
            if (vehicles.isEmpty()) {
                observations.add("El peso sobrepasa el máximo permitido.");
            } else {
                var vehicle = vehicles.get(0);
                item.setVehicle(vehicle);
            }
            item.setObservations(observations);
        }
        return items;
    }

    @Override
    public File createQuotation(MultipartFile file) {
        String filePath = System.getProperty("java.io.tmpdir");

        File quotationFile =
                new File(filePath.concat(File.separator).concat(QUOATION_FILE_NAME));

        List<Item> items = new ArrayList<>();

        try (BufferedWriter out = new BufferedWriter(new FileWriter(quotationFile))) {

            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));

            out.append(HEADER_QUOTATION);

            // Se omite leer el encabezado
            String line = bufferedReader.readLine();

            while ((line = bufferedReader.readLine()) != null) {

                String[] columns = line.split(",");
                Item item = chooseVehicle(Item.builder().order(columns[ORDER_POSITION])
                    .description(columns[DESCRIPTION_POSITION])
                    .depth(Double.valueOf(columns[DEPTH_POSITION]))
                    .width(Double.valueOf(columns[WIDTH_POSITION]))
                    .height(Double.valueOf(columns[HEIGTH_POSITION]))
                    .weight(Double.valueOf(columns[WEIGTH_POSITION]))
                    .stackable(Boolean.valueOf(columns[STACKEABLE_POSITION])).build(),
                        items);
                out.append(line);

                if(Objects.nonNull(item.getVehicle())){
                    out.append(",").append(item.getVehicle().getVehicleFullName());
                }else {
                    out.append(",");
                    out.append(",").append(String.join(DELIMIT,item.getObservations()));
                }

                out.append("\n");
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        Resume resume = new Resume();
        resume.setTotalItems(items.size());
        items = items.stream()
                .filter(item -> Objects.nonNull(item.getVehicle()))
                .collect(Collectors.toList());
        var errorItems = items.stream()
                .filter(item -> Objects.isNull(item.getVehicle()))
                .collect(Collectors.toList()).size();
        var totalAssignations = items.size();
        resume.setTotalErrorItems(errorItems);
        resume.setTotalAssignations(totalAssignations);
        try {
            calculateVehicles(items, resume);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return quotationFile;
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
        Vehicle newVehicle = null;
        try {
            newVehicle = (Vehicle) vehicle.clone();
            newVehicle.setId(generateId());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        Assignation assignation = new Assignation();
        assignation.setVehicle(newVehicle);
        assignation.setItems(items);
        assignationList.add(assignation);
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
        Random r = new Random();
        List<Integer> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            int x = r.nextInt(9999);
            while (codes.contains(x))
                x = r.nextInt(9999);
            codes.add(x);
        }
        return String.format("%04d", codes.get(0));
    }

    public Item chooseVehicle(Item item, List<Item> items) {
        var vehicles = getVehicles();
        List<String> observations = new ArrayList<>();
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
            item.setObservations(Arrays.asList("El item no cumple con las dimensiones maximas"));
        } else {
            var vehicle = availableVehicle.get(0);
            item.setVehicle(vehicle);
        }
        items.add(item);
        return item;
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
