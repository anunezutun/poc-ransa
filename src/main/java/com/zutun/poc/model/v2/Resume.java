package com.zutun.poc.model.v2;

import java.math.BigDecimal;
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
    private BigDecimal totalWeight;
    Map<String, Integer> frequencyVehicles;
}
