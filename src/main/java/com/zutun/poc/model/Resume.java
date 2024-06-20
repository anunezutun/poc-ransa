package com.zutun.poc.model;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Resume {
    private List<Assignation> assignations;
    private Integer totalItems;
    private Integer totalSuccessItems;
    private Integer totalErrorItems;
    private Double volumeTotal;
    Map<String, Integer> frequencyVehicles;
}
