package com.zutun.poc.model.v2;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Vehicle implements Cloneable {
    private String id;
    private String name;
    private String configuration;
    private BigDecimal maxDepth;
    private BigDecimal maxWidth;
    private BigDecimal maxHeight;
    private BigDecimal maxWeight;
    private Integer maxItems;
    private Integer priority;
    private Integer maxUnitsAvailable;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getVehicleFullName() {
        return this.getName().concat(" ").concat(this.getConfiguration());
    }

    public String getVehicleInfo() {
        return String.format("%s %s-%s",
                this.id,
                this.name,
                this.configuration);
    }

    public String getDimensions() {
        return String.format("Long:%s Ancho:%s Alto:%s Peso max:%s Tn",
                this.maxDepth,
                this.maxWidth,
                this.maxHeight,
                this.maxWeight);
    }

}
