package com.zutun.poc.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class Item {

    //private String name;
    private String description;
    private Double depth;
    private Double width;
    private Double height;
    private Double weight;
    private Boolean stackable;
    private List<String> observations;

}
