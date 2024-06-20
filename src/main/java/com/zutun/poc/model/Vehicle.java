package com.zutun.poc.model;

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
}
