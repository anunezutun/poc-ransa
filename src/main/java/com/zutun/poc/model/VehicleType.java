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
public class VehicleType {
    private String name;
    private String configuration;
    private Double maxDepth;
    private Double maxWidth;
    private Double maxHeight;
    private Double maxWeight;
}
