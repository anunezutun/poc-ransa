package com.zutun.poc.model.v2;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UnitMeasurement {
    private String dimension;
    private String weight;
    private Integer decimals;
}
