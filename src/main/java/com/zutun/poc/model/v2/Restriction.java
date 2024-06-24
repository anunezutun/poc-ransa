package com.zutun.poc.model.v2;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Restriction {
    private List<Vehicle> vehicles;
    private UnitMeasurement unitMeasurementInput;
    private UnitMeasurement unitMeasurementOutput;
}
