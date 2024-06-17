package com.zutun.poc.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Item {

    private String order;
    private String description;
    private Double depth;
    private Double width;
    private Double height;
    private Double weight;
    private Boolean stackable;
    private String availableVehicle;
    private List<String> observations = new ArrayList<>();

}
