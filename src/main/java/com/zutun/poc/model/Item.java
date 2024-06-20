package com.zutun.poc.model;

import lombok.Builder;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class Item {

    private String order;
    private String description;
    private Double depth;
    private Double width;
    private Double height;
    private Double weight;
    private Boolean stackable;
    private Vehicle vehicle;
    private List<String> observations = new ArrayList<>();

    public Double getVolume() {
        return this.getDepth() * this.getWidth() * this.getHeight();
    }
}
