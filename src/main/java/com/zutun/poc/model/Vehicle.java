package com.zutun.poc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.RoundingMode;
import java.text.DecimalFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Vehicle implements Cloneable{
    private String name;
    private String configuration;
    private Double maxDepth;
    private Double maxWidth;
    private Double maxHeight;
    private Double maxWeight;
    private Double maxVolume;
    private String id;

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

    public Double getVolume() {
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.UP);
        var volume = this.maxDepth * this.maxWidth * this.maxHeight;
        return Double.parseDouble(df.format(volume));
    }

}
