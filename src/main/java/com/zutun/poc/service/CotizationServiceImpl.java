package com.zutun.poc.service;

import com.zutun.poc.model.Item;
import com.zutun.poc.model.VehicleType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class CotizationServiceImpl implements CotizationService {

    private static final String QUOATION_FILE_NAME = "quotation.csv";
    private static final String HEADER_QUOTATION = "Orden,Descripcion,Profundidad,Ancho,Altura,Peso,Stackeable,Observaciones\n";
    private static final int DEPTH_POSITION = 2;
    private static final int WIDTH_POSITION = 3;
    private static final int HEIGTH_POSITION = 4;
    private static final int WEIGTH_POSITION = 5;
    private static final int STACKEABLE_POSITION = 6;

    @Override
    public List<Item> chooseVehicle(List<Item> items) {

        for (Item item : items) {
            //evaluar longitud
            var vehicles = getVehicles();
            List<String> observations = new ArrayList<>();
            vehicles = vehicles
                    .stream()
                    .filter(vehicleType -> vehicleType.getMaxDepth() >= item.getDepth())
                    .collect(Collectors.toList());
            if (vehicles.isEmpty()) {
                observations.add("La longitud sobrepasa el máximo permitido.");
            }
            vehicles = vehicles
                    .stream()
                    .filter(vehicleType -> vehicleType.getMaxWidth() >= item.getWidth())
                    .collect(Collectors.toList());
            if (vehicles.isEmpty()) {
                observations.add("El ancho sobrepasa el máximo permitido.");
            }
            vehicles = vehicles
                    .stream()
                    .filter(vehicleType -> vehicleType.getMaxHeight() >= item.getHeight())
                    .collect(Collectors.toList());
            if (vehicles.isEmpty()) {
                observations.add("El alto sobrepasa el máximo permitido.");
            }
            vehicles = vehicles
                    .stream()
                    .filter(vehicleType -> vehicleType.getMaxWeight() >= item.getWeight()/1000)
                    .collect(Collectors.toList());
            if (vehicles.isEmpty()) {
                observations.add("El peso sobrepasa el máximo permitido.");
            } else {
                var vehicle = vehicles.get(0);
                item.setAvailableVehicle(vehicle.getName().concat(" ").concat(vehicle.getConfiguration()));
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

        try (BufferedWriter out = new BufferedWriter(new FileWriter(quotationFile))) {

            BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));

            out.append(HEADER_QUOTATION);

            // Se omite leer el encabezado
            String line = bufferedReader.readLine();

            while ((line = bufferedReader.readLine()) != null) {

                // Se eliminan los espacios y se separan los valores
                line = line.replaceAll("\\s", "");
                String[] columns = line.split(",");
                
                StringBuilder observation = new StringBuilder();

                Item.builder().depth(Double.valueOf(columns[DEPTH_POSITION]))
                    .width(Double.valueOf(columns[WIDTH_POSITION]))
                    .height(Double.valueOf(columns[HEIGTH_POSITION]))
                    .weight(Double.valueOf(columns[WEIGTH_POSITION]))
                    .stackable(Boolean.valueOf(columns[STACKEABLE_POSITION])).build();

                out.append(line);
                out.append(",").append(observation.toString());
                out.append("\n");
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return quotationFile;
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
