package com.zutun.poc.model.v2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    private String order;
    private String description;
    private BigDecimal depth;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal weight;
    private Vehicle vehicle;
    private List<String> observations = new ArrayList<>();
    private Boolean isRotated;

}
