package com.zutun.poc.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Assignation {
    private Vehicle vehicle;
    private List<Item> items;
}
